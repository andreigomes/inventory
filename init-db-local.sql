-- Create databases for each microservice
CREATE DATABASE inventory_db;
CREATE DATABASE store_db;
CREATE DATABASE notification_db;

-- Create users
CREATE USER inventory_user WITH PASSWORD 'inventory_pass';
CREATE USER store_user WITH PASSWORD 'store_pass';
CREATE USER notification_user WITH PASSWORD 'notification_pass';

-- Grant privileges for inventory_db
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;

-- Grant privileges for store_db
GRANT ALL PRIVILEGES ON DATABASE store_db TO store_user;

-- Grant privileges for notification_db
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;

-- Connect to inventory_db and create extensions
\c inventory_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Connect to store_db and create extensions
\c store_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Connect to notification_db and create extensions
\c notification_db;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
