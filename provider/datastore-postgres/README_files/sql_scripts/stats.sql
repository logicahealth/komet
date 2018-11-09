\set ECHO all
-- SELECT * FROM datastore_id_table;
SELECT COUNT(*) FROM datastore_id_table;

-- SELECT * FROM uuid_table;
-- Initial write 263 UUIDs
SELECT COUNT(*) FROM uuid_table;
SELECT COUNT(*) FROM uuid_additional_table;
SELECT COUNT(*) FROM uuid_primordial_table;

-- SOLOR_ROOT "7c21b6c5-cf11-5af9-893b-743f004c97f5"
SELECT * FROM uuid_primordial_table WHERE ouid = '7c21b6c5-cf11-5af9-893b-743f004c97f5'::uuid;

-- SELECT * FROM concepts_table;
SELECT COUNT(*) FROM concepts_table;
SELECT COUNT(*) FROM semantics_table;
SELECT COUNT(*) FROM identified_objects_table;

SELECT COUNT(*) FROM type_for_assemblage_table;

SELECT COUNT(*) FROM taxonomy_data_table;
-- SEQUENCE nid_sequence;

\set ECHO none
