CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

-- table for spring modulith event publication
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    completion_date TIMESTAMP(9) WITH TIME ZONE,
    completion_attempts INT DEFAULT 0,
    last_resubmission_date TIMESTAMP(9) WITH TIME ZONE,
    status VARCHAR(50),
    event_type VARCHAR(512) NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    publication_date TIMESTAMP(9) WITH TIME ZONE NOT NULL,
    serialized_event VARCHAR(4000) NOT NULL
    );

CREATE TABLE IF NOT EXISTS person (
    id UUID PRIMARY KEY,
    version INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT person_unique_name UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS city (
    id UUID PRIMARY KEY,
    postal_code INTEGER NOT NULL,
    en VARCHAR(255) NOT NULL,
    de VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS sample (
    id UUID PRIMARY KEY,
    version INT NOT NULL,
    en VARCHAR(255) NOT NULL,
    de VARCHAR(255) NOT NULL,
    description VARCHAR(2048) NOT NULL,
    state VARCHAR(20) NOT NULL,
    owner VARCHAR(36) NOT NULL,
    -- TODO: fix mapping of embedded value objects
    -- city_postal_code INTEGER NOT NULL,
    -- city_name_en VARCHAR(255) NOT NULL,
    -- city_name_de VARCHAR(255) NOT NULL,
    FOREIGN KEY (owner) REFERENCES person(id)
    );

create table airplane (id uuid not null, inventory_code varchar(255), status tinyint check ((status between 0 and 2)), primary key (id));
create table maintenance_report (id uuid not null, confirmed_at timestamp(6), document_id varchar(255), maintenance_reports_id uuid, primary key (id));
alter table if exists sample alter column description set data type varchar(255);
alter table if exists sample alter column owner set data type uuid;
alter table if exists sample alter column state set data type tinyint;
alter table if exists maintenance_report add constraint FK4ii3uoih6soxjhw7pqhrbtuk foreign key (maintenance_reports_id) references airplane;
