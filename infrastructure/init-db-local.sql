-- Initialization script for local development database
-- This script creates the necessary databases and users for all microservices

-- Create databases for each microservice
CREATE DATABASE inventory_db;
CREATE DATABASE store_db;
CREATE DATABASE notification_db;

-- Create users
CREATE USER inventory_user WITH PASSWORD 'inventory_pass';
CREATE USER store_user WITH PASSWORD 'store_pass';
CREATE USER notification_user WITH PASSWORD 'notification_pass';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;
GRANT ALL PRIVILEGES ON DATABASE store_db TO store_user;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;

-- Connect to inventory_db and create schema
\c inventory_db;
CREATE SCHEMA IF NOT EXISTS inventory;
GRANT ALL ON SCHEMA inventory TO inventory_user;

-- Connect to store_db and create schema
\c store_db;
CREATE SCHEMA IF NOT EXISTS store;
GRANT ALL ON SCHEMA store TO store_user;

-- Connect to notification_db and create schema
\c notification_db;
CREATE SCHEMA IF NOT EXISTS notification;
GRANT ALL ON SCHEMA notification TO notification_user;

-- Switch back to inventory_db for main operations
\c inventory_db;
