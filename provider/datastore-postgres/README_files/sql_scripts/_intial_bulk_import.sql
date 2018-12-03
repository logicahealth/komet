-- ------------------------------------------------------------
-- File: _import_all.sql
-- ------------------------------------------------------------
-- The following command sequence will do an initial bulk load.
--
-- 1. Run "File > Export in native format to file…" menu in the ISAAC Komet Application
-- 2. Run "File > Import from native format file…" menu item in the Komet
--    These resulting CSV files are written into …/komet/application/target/csv
-- 3. Quit Komet

-- \set ECHO all

-- 4. Create all tables and sequences created by the ISAAC Komet application.
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

-- 8. Verify row count
-- Get stats for what actually loaded.
\i /PATH_TO/…/provider/datastore-postgres/README_files/sql_scripts/stats.sql

-- \set ECHO none
