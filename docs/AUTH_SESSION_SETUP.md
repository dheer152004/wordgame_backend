# Authentication Sessions

The default Spring configuration uses the local H2 database. Deploy with
`SPRING_PROFILES_ACTIVE=prod` to use PostgreSQL for user sessions and the rest of
the application data.

Configure these production environment variables:

- `DB_URL`: PostgreSQL JDBC URL, for example
  `jdbc:postgresql://db-host:5432/nroq?sslmode=require`
- `DB_USERNAME` and `DB_PASSWORD`: database credentials
- `JWT_SECRET_KEY`: a randomly generated secret of at least 32 bytes
- `ADMIN_CREATE_SECRET`: optional secret used by the admin registration endpoint
- `JPA_DDL_AUTO`: optional Hibernate schema mode; defaults to `update`

The database account must be allowed to create and update the `user_sessions`
table when using the default schema mode. Existing access and refresh tokens do
not have server-side session rows, so users must sign in again after deployment.