#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Define database connection parameters
PGHOST="${POSTGRES_HOSTNAME:-postgresdb}"
PGUSER="postgres"
PGPASSWORD="$POSTGRES_PASSWORD"
export PGHOST PGUSER PGPASSWORD

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
for i in {1..30}; do
  if pg_isready -h "$PGHOST" -U "$PGUSER" > /dev/null 2>&1; then
    echo "PostgreSQL is ready!"
    break
  fi
  echo "Waiting for PostgreSQL... ($i/30)"
  sleep 2
done

# Define database setup commands
echo "Setting up auth_service_db..."

psql -h "$PGHOST" -U "$PGUSER" -c "CREATE DATABASE auth_service_db;" || echo "Database already exists."
psql -h "$PGHOST" -U "$PGUSER" -d auth_service_db -f /workspaces/my-ws-demo/src/auth-service/database/schema.sql
psql -h "$PGHOST" -U "$PGUSER" -d auth_service_db -f /workspaces/my-ws-demo/src/auth-service/database/seed.sql


echo "auth_service_db setup completed."

# Define database setup commands for user_service_db
echo "Setting up user_service_db..."

psql -h "$PGHOST" -U "$PGUSER" -c "CREATE DATABASE user_service_db;" || echo "Database already exists."
psql -h "$PGHOST" -U "$PGUSER" -d user_service_db -f /workspaces/my-ws-demo/src/user-service/database/schema.sql
psql -h "$PGHOST" -U "$PGUSER" -d user_service_db -f /workspaces/my-ws-demo/src/user-service/database/seed.sql

echo "user_service_db setup completed."

# Define database setup commands for point_service_db
echo "Setting up point_service_db..."

psql -h "$PGHOST" -U "$PGUSER" -c "CREATE DATABASE point_service_db;" || echo "Database already exists."
psql -h "$PGHOST" -U "$PGUSER" -d point_service_db -f /workspaces/my-ws-demo/src/point-service/database/schema.sql
psql -h "$PGHOST" -U "$PGUSER" -d point_service_db -f /workspaces/my-ws-demo/src/point-service/database/seed.sql

echo "point_service_db setup completed."
