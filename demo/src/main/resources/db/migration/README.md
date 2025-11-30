# Database Migrations

This folder contains Flyway migration scripts for the BFB Management System.

## Migration Naming Convention

Flyway migrations follow this naming pattern:
```
V{version}__{description}.sql
```

Examples:
- `V1__Initial_schema.sql` - Initial database schema
- `V2__Sample_data.sql` - Sample data for development
- `V3__Add_user_table.sql` - Future migration example

## Version Control

- **V1__Initial_schema.sql**: Creates the initial database schema (clients, vehicles, contracts)
- **V2__Sample_data.sql**: Inserts sample data for development/testing (can be skipped in production)

## Migration Strategy

### Development Environment
- All migrations run automatically on application startup
- Sample data (V2) is included for easier testing

### Production Environment
To skip sample data in production, add to application-prod.yml:
```yaml
spring:
  flyway:
    locations: classpath:db/migration
    ignore-missing-migrations: true
```

Or use Flyway's callback mechanism to conditionally skip certain migrations.

## Best Practices

1. **Never modify existing migrations** - Once applied to production, migrations are immutable
2. **Always use a new version number** for changes
3. **Test migrations** on a copy of production data before deploying
4. **Keep migrations small** - One logical change per migration
5. **Add rollback scripts** if needed (in a separate file like `U{version}__description.sql`)

## Useful Flyway Commands

Clean database (⚠️ dangerous - drops all objects):
```bash
./mvnw flyway:clean
```

Show migration status:
```bash
./mvnw flyway:info
```

Apply pending migrations:
```bash
./mvnw flyway:migrate
```

Validate applied migrations:
```bash
./mvnw flyway:validate
```

## Migration Workflow

1. **Create new migration**: Add `V{next_version}__description.sql`
2. **Test locally**: Run application and verify schema changes
3. **Commit to version control**: Migrations are code
4. **Deploy**: Flyway runs automatically on application startup
5. **Verify**: Check `flyway_schema_history` table for applied migrations

## Troubleshooting

### Migration checksum mismatch
If you see "Migration checksum mismatch", it means a previously applied migration was modified.
- **Solution**: Never modify applied migrations. Create a new migration instead.

### Baseline existing database
If adding Flyway to an existing database:
```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
```

### Skip specific migrations
Use Flyway callbacks or conditional SQL to skip migrations in certain environments.
