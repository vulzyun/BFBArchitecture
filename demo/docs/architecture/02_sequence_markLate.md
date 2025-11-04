# Diagramme de Séquence - Job Mark Late

Ce diagramme illustre le fonctionnement du job `markLateAndCancelBlocked()`.

```mermaid
sequenceDiagram
    participant Scheduler as Job Scheduler
    participant Controller as ContratController
    participant Service as ContratService
    participant Repo as ContratRepository
    participant Metrics as MeterRegistry
    participant Contrat as Contrat Entity

    Scheduler->>Controller: POST /api/contrats/jobs/mark-late
    Controller->>Service: markLateAndCancelBlocked()
    
    Note over Service: Étape 1: Marquer les contrats en retard
    Service->>Repo: findByEtat(EN_COURS)
    Repo-->>Service: List<Contrat> (contrats en cours)
    
    loop Pour chaque contrat EN_COURS
        Service->>Service: if dateFin < today
        alt Contrat dépassé
            Service->>Contrat: markLate()
            Contrat->>Contrat: etat = EN_RETARD
            Service->>Repo: save(contrat)
            
            Note over Service: Étape 2: Annuler contrats bloqués
            Service->>Repo: findByVehiculeIdAndEtat(vehiculeId, EN_ATTENTE)
            Repo-->>Service: List<Contrat> (contrats en attente)
            
            loop Pour chaque contrat EN_ATTENTE du véhicule
                Service->>Service: if dateDebut <= today
                alt Contrat bloqué
                    Service->>Contrat: cancel()
                    Contrat->>Contrat: etat = ANNULE
                    Service->>Repo: save(contrat)
                    Service->>Metrics: contractsCanceledByLateBlock.increment()
                else Contrat futur
                    Note over Service: Pas d'annulation (contrat débute plus tard)
                end
            end
        else Contrat non dépassé
            Note over Service: Pas de modification
        end
    end
    
    Service-->>Controller: int (nombre de contrats modifiés)
    Controller-->>Scheduler: 202 Accepted {contratsMarkedLate: count}
```

## Logique métier

### Étape 1 : Marquer en retard
- Récupère tous les contrats `EN_COURS`
- Pour chacun, vérifie si `dateFin < aujourd'hui`
- Si oui : transition `EN_COURS` → `EN_RETARD`

### Étape 2 : Annuler les bloqués
- Pour chaque contrat passé en `EN_RETARD`
- Récupère les contrats `EN_ATTENTE` du même véhicule
- Annule ceux dont `dateDebut <= aujourd'hui` (considérés comme bloqués)
- Les contrats avec `dateDebut > aujourd'hui` restent inchangés (futurs)

### Métriques
- Incrémente `contracts.canceled.byLateBlock` pour chaque annulation

## Idempotence
Le job peut être exécuté plusieurs fois sans effet de bord :
- Un contrat déjà `EN_RETARD` ne sera pas retraité
- Un contrat déjà `ANNULE` ne peut plus être modifié
