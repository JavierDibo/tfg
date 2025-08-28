-- Reset Database Script
-- This script completely resets the database to ensure clean schema creation

-- Drop all tables in the correct order to avoid foreign key constraints
DROP TABLE IF EXISTS entrega_archivos CASCADE;
DROP TABLE IF EXISTS entregas_ejercicio CASCADE;
DROP TABLE IF EXISTS ejercicios CASCADE;
DROP TABLE IF EXISTS materiales CASCADE;
DROP TABLE IF EXISTS clases_alumnos CASCADE;
DROP TABLE IF EXISTS clases_profesores CASCADE;
DROP TABLE IF EXISTS clases CASCADE;
DROP TABLE IF EXISTS alumnos CASCADE;
DROP TABLE IF EXISTS profesores CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- Drop any sequences that might exist
DROP SEQUENCE IF EXISTS ejercicios_id_seq CASCADE;
DROP SEQUENCE IF EXISTS entregas_ejercicio_id_seq CASCADE;
DROP SEQUENCE IF EXISTS materiales_id_seq CASCADE;
DROP SEQUENCE IF EXISTS clases_id_seq CASCADE;
DROP SEQUENCE IF EXISTS alumnos_id_seq CASCADE;
DROP SEQUENCE IF EXISTS profesores_id_seq CASCADE;
DROP SEQUENCE IF EXISTS usuarios_id_seq CASCADE;
