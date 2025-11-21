#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define database setup commands
echo "Setting up auth_service_db..."


# Example: Create database and run schema/seed files for PostgreSQL
PGUSER="postgres"
PGPASSWORD="$POSTGRES_PASSWORD"
export PGUSER PGPASSWORD

psql -h localhost -U "$PGUSER" -c "CREATE DATABASE auth_service_db;" || echo "Database already exists."
psql -h localhost -U "$PGUSER" -d auth_service_db -f /workspaces/my-ws-demo/src/auth-service/database/schema.sql
psql -h localhost -U "$PGUSER" -d auth_service_db -f /workspaces/my-ws-demo/src/auth-service/database/seed.sql


echo "auth_service_db setup completed."

# Define database setup commands for user_service_db
echo "Setting up user_service_db..."

psql -h localhost -U "$PGUSER" -c "CREATE DATABASE user_service_db;" || echo "Database already exists."
psql -h localhost -U "$PGUSER" -d user_service_db -f /workspaces/my-ws-demo/src/user-service/database/schema.sql
psql -h localhost -U "$PGUSER" -d user_service_db -f /workspaces/my-ws-demo/src/user-service/database/seed.sql

echo "user_service_db setup completed."

# Define database setup commands for point_service_db
echo "Setting up point_service_db..."

psql -h localhost -U "$PGUSER" -c "CREATE DATABASE point_service_db;" || echo "Database already exists."
psql -h localhost -U "$PGUSER" -d point_service_db -f /workspaces/my-ws-demo/src/point-service/database/schema.sql
psql -h localhost -U "$PGUSER" -d point_service_db -f /workspaces/my-ws-demo/src/point-service/database/seed.sql

echo "point_service_db setup completed."
