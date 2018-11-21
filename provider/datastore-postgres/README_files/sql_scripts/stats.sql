\set ECHO all

SELECT COUNT(*) FROM datastore_id_table;

SELECT COUNT(*) FROM uuid_table;
SELECT COUNT(*) FROM uuid_additional_table;
SELECT COUNT(*) FROM uuid_primordial_table;

-- SOLOR_ROOT "7c21b6c5-cf11-5af9-893b-743f004c97f5"
SELECT * FROM uuid_primordial_table WHERE ouid = '7c21b6c5-cf11-5af9-893b-743f004c97f5'::uuid;

SELECT COUNT(*) FROM concepts_table;
SELECT COUNT(*) FROM semantics_table;
SELECT COUNT(*) FROM identified_objects_table;

SELECT COUNT(*) FROM type_for_assemblage_table;

SELECT COUNT(*) FROM taxonomy_data_table;

SELECT COUNT(*) FROM stamp_committed_table;
SELECT COUNT(*) FROM stamp_uncommitted_table;

\set ECHO none
