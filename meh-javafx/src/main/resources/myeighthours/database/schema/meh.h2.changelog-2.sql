-- Changelog de BD de v1 a v2

-- 1. Crear tabla CHANGELOG
CREATE TABLE IF NOT EXISTS CHANGELOG(ID BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL, VERSION BIGINT, TIMESTAMP BIGINT);

-- 2. Crear columna "sincronizado" de fichaje
ALTER TABLE `FICHAJE` ADD `SINCRONIZADO` BOOLEAN DEFAULT true NOT NULL;