-- ------------------
-- Datastore ID Table
-- ------------------
CREATE TABLE IF NOT EXISTS datastore_id_table (
    datastore_puid uuid,
    PRIMARY KEY (datastore_puid)
);

-- -----------------
-- Identifier Tables
-- -----------------
CREATE TABLE IF NOT EXISTS uuid_table (
    u_nid  INTEGER,
    ouid uuid,
    UNIQUE (ouid),
    PRIMARY KEY (u_nid, ouid)
);

CREATE TABLE IF NOT EXISTS uuid_primordial_table ( 
    UNIQUE (u_nid) 
    ) INHERITS (uuid_table);

CREATE TABLE IF NOT EXISTS uuid_additional_table
    () INHERITS (uuid_table);

CREATE SEQUENCE IF NOT EXISTS nid_sequence 
    MINVALUE -2147483647 START WITH -2147483647;
-- :PSQLv10: can use "AS INTEGER"


-- ------------
-- STAMP Tables
-- ------------
CREATE TABLE IF NOT EXISTS stamp_committed_table (
    stamp_committed_sequence INTEGER, 
    stamp_committed_data     bytea, 
    UNIQUE (stamp_committed_data), 
    PRIMARY KEY (stamp_committed_sequence) );

CREATE TABLE IF NOT EXISTS stamp_uncommitted_table (
    stamp_uncommitted_sequence INTEGER, 
    stamp_uncommitted_data     bytea, 
    UNIQUE (stamp_uncommitted_data), 
    PRIMARY KEY (stamp_uncommitted_sequence) );

CREATE SEQUENCE IF NOT EXISTS stamp_next_sequence
    MINVALUE 1 START WITH 1;
-- :PSQLv10: can use "AS INTEGER"

-- ------------------
-- Identified Objects
-- ------------------
-- create a -1 stamped version_data where the binary stores the UUID, class type, etc. 
-- maybe create an Object table...
-- version_data table...
CREATE TABLE IF NOT EXISTS identified_objects_table (
    o_nid INTEGER,
    assemblage_nid INTEGER,
    version_stamp  INTEGER, -- -1 for the first record
    version_data   bytea, --- actual data goes here.
    PRIMARY KEY (o_nid, version_stamp)
);

-- ------------------
-- Concepts Table
-- ------------------
CREATE TABLE IF NOT EXISTS concepts_table
  () INHERITS (identified_objects_table);

-- ------------------
-- Semantics Table
-- ------------------
CREATE TABLE IF NOT EXISTS semantics_table
  (referenced_component_nid INTEGER)
  INHERITS (identified_objects_table);

-- ------------------------
-- TypeForAssemblage Table
-- ------------------------
CREATE TABLE IF NOT EXISTS type_for_assemblage_table (
    assemblage_nid        INTEGER,
    assemblage_type_token INTEGER,
    version_type_token    INTEGER,
    PRIMARY KEY(assemblage_nid)
);

-- ------------------------
-- Taxonomy Data Table
-- ------------------------
CREATE TABLE IF NOT EXISTS taxonomy_data_table (
    t_nid INTEGER,
    assemblage_nid INTEGER,
    taxonomy_data bytea,
    CONSTRAINT taxonomy_data_pk UNIQUE (t_nid,assemblage_nid),
    PRIMARY KEY(t_nid, assemblage_nid)
);