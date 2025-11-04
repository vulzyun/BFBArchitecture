# ADR-002: Retard et Annulation Automatique des Contrats

## Statut
**Accepté** - 2025-11-01

## Contexte
Le système doit gérer automatiquement deux scénarios critiques :

1. **Contrats en retard** : Lorsqu'un contrat EN_COURS dépasse sa `dateFin`, il doit passer automatiquement à EN_RETARD
2. **Annulation suite à panne** : Lorsqu'un véhicule tombe en panne, les contrats EN_ATTENTE doivent être annulés
3. **Annulation de contrats bloqués** : Lorsqu'un contrat passe EN_RETARD, les contrats EN_ATTENTE du même véhicule peuvent être bloqués

Nous devons décider du mécanisme d'automatisation et de la cohérence des règles.

## Décision

### 1. Job périodique `markLateAndCancelBlocked()`

#### Mécanisme choisi : Job applicatif exposé via API
```java
@PostMapping("/jobs/mark-late")
public ResponseEntity<MarkLateResponse> markLate() {
    int count = contratService.markLateAndCancelBlocked();
    return ResponseEntity.accepted().body(new MarkLateResponse(count));
}
```

#### Logique implémentée
1. **Marquer EN_RETARD** : `EN_COURS` avec `dateFin < today` → `EN_RETARD`
2. **Annuler les bloqués** : Pour chaque véhicule avec un contrat EN_RETARD, annuler les EN_ATTENTE dont `dateDebut <= today`

#### Critère de "bloqué"
Un contrat EN_ATTENTE est considéré comme **bloqué** si :
- Son véhicule a un contrat EN_RETARD actif
- Sa `dateDebut <= aujourd'hui` (le contrat aurait dû commencer)

**Justification** : Si `dateDebut > aujourd'hui`, le retard pourrait être résolu d'ici là → on ne l'annule pas prématurément.

### 2. Événement `vehicleMarkedDown`

#### Mécanisme choisi : Endpoint interne pour événements
```java
@PostMapping("/internal/events/vehicules/marked-down")
public ResponseEntity<?> handleVehicleMarkedDown(@RequestBody VehicleMarkedDownRequest req) {
    int count = contratService.cancelPendingContractsForVehicle(req.vehiculeId());
    return ResponseEntity.accepted().body(...);
}
```

#### Logique implémentée
- Annuler **uniquement** les contrats `EN_ATTENTE` du véhicule
- Les contrats `EN_COURS` ou `EN_RETARD` ne sont **pas annulés** (le client a déjà le véhicule)

#### Cohérence métier
- **EN_ATTENTE** : Futur → annulation sans impact client
- **EN_COURS/EN_RETARD** : Actif → gestion manuelle nécessaire (compensation, véhicule de remplacement...)

## Alternatives considérées

### 1. Triggers de base de données
```sql
CREATE TRIGGER check_late_contracts
AFTER UPDATE ON contrats
FOR EACH ROW ...
```

**Rejeté** :
- ❌ Logique métier dans la BDD → difficile à tester et à versionner
- ❌ Couplage fort à PostgreSQL/MySQL (pas portable sur H2 pour tests)
- ❌ Métriques Micrometer non disponibles dans la BDD

### 2. Spring `@Scheduled` interne
```java
@Scheduled(cron = "0 0 * * * *")
public void markLateJob() { ... }
```

**Rejeté (pour l'instant)** :
- ❌ Moins flexible (redémarrage nécessaire pour changer la fréquence)
- ❌ Difficile à déclencher manuellement en tests
- ✅ **Peut être ajouté plus tard** pour automatiser l'appel API

**Choix actuel** : Endpoint API → permet déclenchement manuel OU via scheduler externe (Kubernetes CronJob, AWS EventBridge...)

### 3. Event-Driven avec Message Queue (Kafka, RabbitMQ)
```java
@EventListener(VehicleMarkedDownEvent.class)
public void onVehicleDown(VehicleMarkedDownEvent event) { ... }
```

**Rejeté (pour l'instant)** :
- ❌ Overhead d'infrastructure (broker Kafka/RabbitMQ)
- ❌ Complexité de gestion des erreurs et retry
- ✅ **Évolution future possible** si architecture microservices

**Choix actuel** : API REST synchrone → suffisant pour MVP, peut être wrappé dans un event listener plus tard

### 4. Annulation immédiate de TOUS les contrats du véhicule
**Rejeté** :
- ❌ Annuler EN_COURS/EN_RETARD = retirer le véhicule au client en cours d'utilisation
- ❌ Incohérent avec la réalité métier (le client a déjà le véhicule physiquement)

**Choix actuel** : Seulement EN_ATTENTE, gestion manuelle pour les actifs

## Conséquences

### Positives ✅
1. **Idempotence** : Les deux jobs peuvent être appelés plusieurs fois sans effet de bord
2. **Observabilité** : Métriques Micrometer `contracts.canceled.byVehicleDown` et `byLateBlock`
3. **Testabilité** : Endpoints testables en intégration (ContratControllerIT)
4. **Flexibilité** : Déclenchement manuel ou automatisé (CronJob, EventBridge...)
5. **Cohérence** : Règles de gestion claires et documentées

### Négatives ❌
1. **Latence** : Le job n'est pas temps réel (dépend de la fréquence d'appel)
   - **Mitigation** : Appel fréquent (ex: toutes les 15 minutes) ou event-driven si critique
2. **Race conditions possibles** : Si deux instances appellent le job simultanément
   - **Mitigation** : `@Transactional` + vérifications d'état avant modification
3. **Pas de notification** : Le client n'est pas notifié automatiquement de l'annulation
   - **Évolution future** : Ajouter un service de notification (email/SMS)

### Métriques ajoutées
```java
Counter contractsCanceledByVehicleDown;
Counter contractsCanceledByLateBlock;
```

Accessibles via `/actuator/metrics/contracts.canceled.byVehicleDown`

## Implémentation

### Code clé : markLateAndCancelBlocked()
```java
public int markLateAndCancelBlocked() {
    LocalDate today = LocalDate.now();
    List<Contrat> contratsEnCours = repository.findByEtat(EN_COURS);
    
    for (Contrat contrat : contratsEnCours) {
        if (contrat.getDateFin().isBefore(today)) {
            // 1. Marquer EN_RETARD
            contrat.markLate();
            repository.save(contrat);
            
            // 2. Annuler les EN_ATTENTE bloqués
            List<Contrat> awaiting = repository.findByVehiculeIdAndEtat(
                contrat.getVehiculeId(), EN_ATTENTE
            );
            for (Contrat blocked : awaiting) {
                if (!blocked.getDateDebut().isAfter(today)) {
                    blocked.cancel();
                    repository.save(blocked);
                    canceledByLateBlock.increment();
                }
            }
        }
    }
}
```

### Garanties de cohérence
1. **Transactionnalité** : `@Transactional` sur le service
2. **Vérification d'état** : `if etat == EN_ATTENTE` avant annulation
3. **Règles domain** : `Rules.transitionAllowed()` dans `Contrat.cancel()`

## Évolution future

### Phase 2 : Scheduler automatique
```java
@Scheduled(cron = "0 */15 * * * *") // Toutes les 15 minutes
public void scheduledMarkLate() {
    markLateAndCancelBlocked();
}
```

### Phase 3 : Event-Driven
```java
@KafkaListener(topics = "vehicle.events")
public void onVehicleEvent(VehicleEvent event) {
    if (event.type == MARKED_DOWN) {
        cancelPendingContractsForVehicle(event.vehicleId);
    }
}
```

### Phase 4 : Notifications
```java
// Dans cancelPendingContractsForVehicle()
for (Contrat contrat : canceled) {
    notificationService.sendCancellationEmail(contrat.getClientId(), contrat.getId());
}
```

## Validation
- ✅ Tests unitaires : `ContratServiceTest.mark_late_cancels_next_awaiting_if_blocked_on_same_vehicle()`
- ✅ Tests d'intégration : `ContratControllerIT.vehicle_marked_down_endpoint_triggers_cancel_pending()`
- ✅ Métriques vérifiées : `/actuator/metrics` expose les compteurs

## Références
- [Idempotence in Distributed Systems](https://www.enterpriseintegrationpatterns.com/patterns/messaging/IdempotentReceiver.html)
- [Micrometer Metrics](https://micrometer.io/docs/concepts#_counters)
- [Spring Scheduling](https://spring.io/guides/gs/scheduling-tasks/)
