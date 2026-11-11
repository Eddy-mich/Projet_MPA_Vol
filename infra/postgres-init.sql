-- Bases VolApp : une base par service.
-- Les tables sont creees par Hibernate (ddl-auto: update) a partir des entites Java.
-- Ce script ne fait que creer les bases au premier demarrage de PostgreSQL.

CREATE DATABASE reservations;
CREATE DATABASE suivi;
CREATE DATABASE utilisateurs;
