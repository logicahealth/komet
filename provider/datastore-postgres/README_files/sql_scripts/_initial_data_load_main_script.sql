-- ------------------------------------------------------------
-- File: _initial_bulk_import.sql
-- ------------------------------------------------------------
-- The following command sequence will do an initial bulk load.
--
-- 1. Run "File > Export in native format to file…" menu in the ISAAC Komet Application
-- 2. Run "File > Import from native format file…" menu item in the Komet
--    These resulting CSV files are written into …/komet/application/target/csv
-- 3. Quit Komet

-- \set ECHO all

-- 4. Create all tables and sequences created by the ISAAC Komet application.
-- -- Note: load databases as the same user that will be run with application.
-- \du                           -- display user and roles
\connect isaac_db isaac_user
\i /PATH_TO/…/provider/datastore-postgres/README_files/sql_scripts/drop_all.sql

-- 5. Create new tables
\i /PATH_TO/…/provider/datastore-postgres/README_files/sql_scripts/create_table_schema.sql

-- 6. Change directories to folder where the CSV files were created.
\cd /PATH_TO/…/komet/application/target/csv

-- \! pwd
-- \! ls

-- 7. Load the data
-- toggle timing on/off
\timing on
\i initial_data_load.sql
\timing off

-- 8. Add any index which is not otherwise automatically created
\timing on
CREATE UNIQUE INDEX uuid_primordial_table_ouid_key ON uuid_primordial_table USING btree (ouid);
CREATE UNIQUE INDEX uuid_primordial_table_pkey     ON uuid_primordial_table USING btree (u_nid, ouid);

CREATE UNIQUE INDEX uuid_additional_table_ouid_key ON uuid_additional_table USING btree (ouid);
CREATE UNIQUE INDEX uuid_additional_table_pkey     ON uuid_additional_table USING btree (u_nid, ouid);

CREATE UNIQUE INDEX concepts_table_pkey     ON concepts_table  USING btree (o_nid, version_stamp);

CREATE UNIQUE INDEX semantics_table_pkey    ON semantics_table USING btree (o_nid, version_stamp);
CREATE INDEX semantics_table_assemblage_idx ON semantics_table USING btree (assemblage_nid);
CREATE INDEX semantics_table_referenced_component_idx ON semantics_table USING btree (referenced_component_nid);
\timing off

-- 9. Verify row count
-- Get stats for what actually loaded.
\i /PATH_TO/…/provider/datastore-postgres/README_files/sql_scripts/stats.sql

-- \set ECHO none
