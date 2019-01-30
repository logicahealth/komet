/*
 * Copyright 2018 ISAAC's KOMET Collaborators.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.fx.stage;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
//import sh.isaac.provider.postgres.PsqlIdentifierRow;

/**
 *
 * @author kec
 */
public class NativeImport extends TimedTaskWithProgressTracker<Integer> {

    final File importFile;

    // Caches
//    private final ConcurrentHashMap<UUID, Integer> cacheUuidToNidMap = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<IsaacObjectType, Integer> cacheObjectTypeToNidMap = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<VersionType, Integer> cacheVersionTypeToNidMap = new ConcurrentHashMap<>();
//
//    private final ConcurrentHashMap<Integer, Stamp> cacheStampSequenceToStampObjectMap = new ConcurrentHashMap<>();
    public NativeImport(File importFile) {
        this.importFile = importFile;
        updateTitle("Native import from " + importFile.getName());
        Get.activeTasks().add(this);
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info(":TIME: Clearing table row data");
        //serviceDataStore.clear();
        //serviceId.clear();
        //serviceStamp.clear();

        int nidSequenceCurrVal = Integer.MIN_VALUE;
        int stampSequenceCurrVal = 1;

        File csvDir = new File("target", "csv");
        csvDir.mkdirs();

        try (ZipFile zipFile = new ZipFile(importFile, Charset.forName("UTF-8"))) {

            updateTitle("Native import Identifiers...");
            nidSequenceCurrVal = importIdentifiers(zipFile, csvDir);
            updateTitle("Native import Assemblage Types ...");
            importTypes(zipFile, csvDir);
            updateTitle("Native import STAMP ...");
            stampSequenceCurrVal = importStampSequences(zipFile, csvDir);
            updateTitle("Native import Taxonomy...");
            importTaxonomyData(zipFile, csvDir);
            updateTitle("Native import Concepts and Semantics...");
            importConceptsAndSemantics(zipFile, csvDir);
        }

        LOG.info(":TIME: write load script");
        writeSqlLoadScript(csvDir, nidSequenceCurrVal, stampSequenceCurrVal);
        LOG.info(":TIME: NativeImport completed");
        return 0;
    }

    protected int importIdentifiers(ZipFile zipFile, File csvDir) throws IOException {
        int nidSequenceCurrVal = Integer.MIN_VALUE;
        ZipEntry zipEntryIdentifier = zipFile.getEntry("identifiers");

        // PASS Identifier 1: Primordial UUIDs
        int uuidPrimordialCount = 0;
        //PsqlIdentifierRow[] uuidPrimordialArray;
        int uuidAdditionalCount = 0;
        //ArrayList<PsqlIdentifierRow> uuidAdditionalArray = new ArrayList();
        try (DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntryIdentifier))) {
            uuidPrimordialCount = dis.readInt();
            LOG.info(":TIME:Identifiers: begin processing (" + uuidPrimordialCount + ")");

            //uuidPrimordialArray = new PsqlIdentifierRow[uuidPrimordialCount];
            BufferedWriter uuidPrimordialWriter = new BufferedWriter(
                new FileWriter(new File(csvDir, "uuid_primordial_table.csv"))
            );

            BufferedWriter uuidAdditionalWriter = new BufferedWriter(
                new FileWriter(new File(csvDir, "uuid_additional_table.csv"))
            );

            uuidPrimordialWriter.write("u_nid,ouid\n");
            uuidAdditionalWriter.write("u_nid,ouid\n");

            for (int i = 0; i < uuidPrimordialCount; i++) {
                int nid = dis.readInt();
                if (nid > nidSequenceCurrVal) {
                    nidSequenceCurrVal = nid;
                }
                int uuidLength = dis.readInt();
                for (int uuidIndex = 0; uuidIndex < uuidLength; uuidIndex++) {
                    UUID uuid = new UUID(dis.readLong(), dis.readLong());
                    //PsqlIdentifierRow row = new PsqlIdentifierRow(nid, uuid);
                    if (uuidIndex == 0) {
                        uuidPrimordialWriter.write(nid + "," + uuid.toString() + "\n");
                        //uuidPrimordialArray[i] = row;
                        //IsaacObjectType objectType = IsaacObjectType.fromToken(dis.readByte());
                        dis.readByte(); // skip  objectType
                        // :!!!: cacheObjectTypeToNidMap.put(objectType, nid);
                        // Version Type for Assemblage
                        // VersionType versionType = VersionType.getFromToken(dis.readByte());
                        dis.readByte(); // skip  versionType
                    } else {
                        uuidAdditionalCount++;
                        //uuidAdditionalArray.add(row);
                        uuidAdditionalWriter.write(nid + "," + uuid.toString() + "\n");
                    }
                }
                if (i % 1000000 == 0) {
                    LOG.info("    ... " + i);
                }
            }

            uuidAdditionalWriter.flush();
            uuidAdditionalWriter.close();
            uuidPrimordialWriter.flush();
            uuidPrimordialWriter.close();
        }
        return nidSequenceCurrVal;
    }

    protected void importTypes(ZipFile zipFile, File csvDir) throws IOException {
        updateMessage("Importing Types...");
        ZipEntry typeEntry = zipFile.getEntry("types");
        DataInputStream dis = new DataInputStream(zipFile.getInputStream(typeEntry));
        BufferedWriter writer = new BufferedWriter(
            new FileWriter(new File(csvDir, "type_for_assemblage_table.csv"))
        );
        writer.write("assemblage_nid,assemblage_type_token,version_type_token\n");
        int typeCount = dis.readInt();
        LOG.info(":TIME:ASSEMBLAGE_TYPE: begin processing (" + typeCount + ")");
        for (int i = 0; i < typeCount; i++) {
            int assemblageNid = dis.readInt();
            //IsaacObjectType objectType = IsaacObjectType.fromToken((byte) dis.readInt());
            int assemblageTypeToken = dis.readInt();
            // VersionType versionType = VersionType.getFromToken((byte) dis.readInt());
            int versionTypeToken = dis.readInt();
            writer.write(assemblageNid + ","
                + assemblageTypeToken + ","
                + versionTypeToken + "\n");
        }
        dis.close();
        writer.flush();
        writer.close();
    }

    protected int importStampSequences(ZipFile zipFile, File csvDir) throws IOException {
        int stampSequenceCurrVal = 1;
        ZipEntry zipEntryStamp = zipFile.getEntry("stamp");
        try (DataInputStream stampDis = new DataInputStream(zipFile.getInputStream(zipEntryStamp));
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(csvDir, "stamp_committed_table.csv"))
            )) {
            writer.write("stamp_committed_sequence,stamp_committed_data\n");
            int stampCount = stampDis.readInt();
            LOG.info(":TIME:STAMP: begin processing (" + stampCount + ")");
            for (int i = 0; i < stampCount; i++) {
                int stampSequence = stampDis.readInt();
                if (stampSequence > stampSequenceCurrVal) {
                    stampSequenceCurrVal = stampSequence;
                }
                Status status = Status.valueOf(stampDis.readUTF());
                long time = stampDis.readLong();
                int authorNid = stampDis.readInt();
                int moduleNid = stampDis.readInt();
                int pathNid = stampDis.readInt();
                // :PROCESS_DATA:STAMP:
                Stamp stamp = new Stamp(status, time, authorNid, moduleNid, pathNid);
                byte[] data = convertStampToBytes(stamp);
                writer.write(stampSequence + "," + encodeHexString(data) + "\n");
                //cacheStampSequenceToStampObjectMap.put(stampSequence, stamp);
            }
            writer.flush();
        }
        return stampSequenceCurrVal;
    }

    protected void importTaxonomyData(ZipFile zipFile, File csvDir) throws IOException {
        // Note: limit ... only processing one assemblage
        int assemblageNid = TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid();
        ZipEntry zipEntryTaxonomy = zipFile.getEntry("taxonomy");
        try (DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntryTaxonomy));
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(csvDir, "taxonomy_data_table.csv"))
            )) {
            writer.write("t_nid,assemblage_nid,taxonomy_data\n");

            int taxonomyCount = dis.readInt();
            LOG.info(":TIME:Taxonomy: begin processing (" + taxonomyCount + ")");

            for (int i = 0; i < taxonomyCount; i++) {
                int nid = dis.readInt();
                // int assemblageNid = Note: limit ... only processing one assemblage
                int taxonomyArraySize = dis.readInt();
                int[] taxonomyData = new int[taxonomyArraySize + 1];
                taxonomyData[0] = taxonomyArraySize;
                for (int j = 1; j < taxonomyArraySize + 1; j++) {
                    taxonomyData[j] = dis.readInt();
                }

                writer.write(nid + ","
                    + assemblageNid + ","
                    + encodeHexString(taxonomyData) + "\n");
                if (i % 100000 == 0) {
                    LOG.info("    ... " + i);
                }
            }
            writer.flush();
        }
    }

    protected void importConceptsAndSemantics(ZipFile zipFile, File csvDir) throws IOException {
        ZipEntry zipEntryIbdf = zipFile.getEntry("ibdf");
        DataInputStream dis = new DataInputStream(zipFile.getInputStream(zipEntryIbdf));
        LOG.info(":TIME:Objects: begin processing");
        int objectCount = 0;

        BufferedWriter conceptWriter = new BufferedWriter(
            new FileWriter(new File(csvDir, "concepts_table.csv"))
        );
        conceptWriter.write("o_nid,assemblage_nid,version_stamp,version_data\n");

        BufferedWriter semanticWriter = new BufferedWriter(
            new FileWriter(new File(csvDir, "semantics_table.csv"))
        );
        semanticWriter.write("o_nid,assemblage_nid,referenced_component_nid,version_stamp,version_data\n");

        boolean moreToRead = true;
        while (moreToRead) {
            // :Object:1: IsaacObjectType
            IsaacObjectType objectType = IsaacObjectType.fromToken(dis.readByte());

            switch (objectType) {
            case CONCEPT:
                int conceptRowCount = dis.readInt(); // :Object:2: row count
                for (int i = 0; i < conceptRowCount; i++) {
                    int chronologyNid = dis.readInt(); //:1: o_nid 
                    int assemblageNid = dis.readInt(); //:2: assemblage_nid 
                    int versionStamp = dis.readInt();  //:3: version_stamp 
                    int byteArraySize = dis.readInt(); //:4: version_data size
                    byte[] bytea = new byte[byteArraySize];
                    dis.readFully(bytea);              //:5: version_data
                    conceptWriter.write(
                        chronologyNid + "," // o_nid
                        + assemblageNid + "," // assemblage_nid
                        + versionStamp + "," // version_stamp
                        + encodeHexString(bytea) + "\n"); // version_data
                }
                break;

            case SEMANTIC:
                int semanticRowCount = dis.readInt(); // :Object:2: row count
                for (int i = 0; i < semanticRowCount; i++) {
                    int chronologyNid = dis.readInt();          //:1: o_nid 
                    int assemblageNid = dis.readInt();          //:2: assemblage_nid 
                    int referencedComponentNid = dis.readInt(); //:3: referenced_component_nid 
                    int versionStamp = dis.readInt();           //:4: version_stamp 
                    int byteArraySize = dis.readInt();          //:5: version_data size
                    byte[] bytea = new byte[byteArraySize];
                    dis.readFully(bytea);                       //:6: version_data
                    semanticWriter.write(
                        chronologyNid + "," // o_nid
                        + assemblageNid + "," // assemblage_nid
                        + referencedComponentNid + "," // referenced_component_nid
                        + versionStamp + "," // version_stamp
                        + encodeHexString(bytea) + "\n"); // version_data
                }
                break;
            case UNKNOWN:
                moreToRead = false;
                break; // done.
            default:
                LOG.error("    ... unexpected IssacObjectType="
                    + objectType.name() + " at objectCount=" + objectCount);
            }

            objectCount++;
            if (objectCount % 1000000 == 0) {
                LOG.info("    ... " + objectCount);
            }
        }

        conceptWriter.flush();
        conceptWriter.close();
        semanticWriter.flush();
        semanticWriter.close();
    }

    private void writeSqlLoadScript(File csvDir, int nidSequenceCurrent, int stampNextSequenceCurrent) throws IOException {
        try (BufferedWriter sqlWriter = new BufferedWriter(
            new FileWriter(new File(csvDir, "initial_data_load.sql"))
        )) {
            sqlWriter.write("-- --------------------------------- \n");
            sqlWriter.write("-- ----- initial_data_load.sql ----- \n");
            sqlWriter.write("-- --------------------------------- \n");

            sqlWriter.write("-- This file was automatically generated. \n");
            sqlWriter.write("-- Use `drop_all.sql` and `create_table_schema` "
                + "prior to initial bulk data load. \n");
            sqlWriter.write("-- \\cd [PATH_TO_CSV_DATA] \n");
            sqlWriter.write("-- \\timing \n\n");

            sqlWriter.write("-- ----------------------------------- \n");
            sqlWriter.write("-- ----- Initial Discrete Values ----- \n");
            sqlWriter.write("-- ----------------------------------- \n");
            sqlWriter.write("INSERT INTO datastore_id_table (datastore_puid) VALUES ('"
                + UUID.randomUUID().toString()
                + "'::uuid) ; \n");
            sqlWriter.write("SELECT setval('nid_sequence', "
                + nidSequenceCurrent + ", true); \n");
            sqlWriter.write("SELECT setval('stamp_next_sequence', "
                + stampNextSequenceCurrent + ", true); \n");

            sqlWriter.write("-- ---------------------------------- \n");
            sqlWriter.write("-- ----- Bulk Initial Data Load ----- \n");
            sqlWriter.write("-- ---------------------------------- \n");
            // Identifiers
            sqlWriter.write("\\copy uuid_primordial_table "
                + "(u_nid,ouid) "
                + "FROM 'uuid_primordial_table.csv' WITH (FORMAT CSV, HEADER); \n");
            sqlWriter.write("\\copy uuid_additional_table "
                + "(u_nid,ouid) "
                + "FROM 'uuid_additional_table.csv' WITH (FORMAT CSV, HEADER); \n");
            // STAMP
            sqlWriter.write("\\copy stamp_committed_table "
                + "(stamp_committed_sequence,stamp_committed_data) "
                + "FROM 'stamp_committed_table.csv' WITH (FORMAT CSV, HEADER); \n");
            // Taxonomy Data
            sqlWriter.write("\\copy taxonomy_data_table "
                + "(t_nid,assemblage_nid,taxonomy_data) "
                + "FROM 'taxonomy_data_table.csv' WITH (FORMAT CSV, HEADER); \n");
            // Identified Objects
            sqlWriter.write("\\copy concepts_table "
                + "(o_nid,assemblage_nid,version_stamp,version_data) "
                + "FROM 'concepts_table.csv' WITH (FORMAT CSV, HEADER); \n");
            sqlWriter.write("\\copy semantics_table "
                + "(o_nid,assemblage_nid,referenced_component_nid,version_stamp,version_data) "
                + "FROM 'semantics_table.csv' WITH (FORMAT CSV, HEADER); \n");
            // Types for Assemblage
            sqlWriter.write("\\copy type_for_assemblage_table "
                + "(assemblage_nid,assemblage_type_token,version_type_token) "
                + "FROM 'type_for_assemblage_table.csv' WITH (FORMAT CSV, HEADER); \n");

            sqlWriter.flush();
        }
    }

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String encodeHexString(final byte[] data) {
        final int dataSize = data.length;
        final char[] out = new char[(dataSize << 1) + 2];
        int outIdx = 0;
        out[outIdx++] = '\\';
        out[outIdx++] = 'x';
        for (int dataIdx = 0; dataIdx < dataSize; dataIdx++) {
            out[outIdx++] = HEX_DIGITS[(0xF0 & data[dataIdx]) >>> 4];
            out[outIdx++] = HEX_DIGITS[0x0F & data[dataIdx]];
        }
        return new String(out);
    }

    public static String encodeHexString(final int[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);
        for (int i : data) {
            buffer.putInt(i);
        }
        return encodeHexString(buffer.array());
    }

    // Note: Import copy of PostgresStampProvider convertStampToBytes(Stamp stamp)
    public byte[] convertStampToBytes(Stamp stamp) {
        ByteArrayDataBuffer srcData = new ByteArrayDataBuffer();

        srcData.putUTF(stamp.getStatus().name());
        srcData.putLong(stamp.getTime());
        srcData.putInt(stamp.getAuthorNid());
        srcData.putInt(stamp.getModuleNid());
        srcData.putInt(stamp.getPathNid());

        int length = srcData.getPosition();
        byte[] destBytes = new byte[length];
        System.arraycopy(srcData.getData(), 0, destBytes, 0, length);
        return destBytes;
    }

}
