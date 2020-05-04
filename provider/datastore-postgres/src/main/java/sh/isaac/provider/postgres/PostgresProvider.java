/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.list.IntArrayList;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.datastore.ChronologySerializeable;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.DataWriteListener;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.ModelGet;
import sh.isaac.model.collections.SpinedIntIntArrayMap;
import sh.isaac.model.collections.SpinedNidIntMap;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.SemanticVersionImpl;
import sh.isaac.provider.datastore.cache.CacheBootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

import static sh.isaac.api.externalizable.ByteArrayDataBuffer.getInt;

/**
 *
 * @author kec
 */
public class PostgresProvider
    implements DataStoreSubService, IdentifierService, CacheBootstrap { // ExtendedStore

    private static final Logger LOG = LogManager.getLogger();
    private static final boolean LOG_SQL_FLAG = false;
    private static final boolean LOG_BYTECHECK_FLAG = false;

    private void logSqlBytea(Statement stmt, byte[] bytes) {
        if (LOG_SQL_FLAG) {
            LOG.debug(":SQL: " + stmt.toString()
                + "; -- '"
                + ByteArrayDataBuffer.printHexBinary(bytes)
                + "'::bytea");
        }
    }

    private void logSqlStmt(Statement stmt) {
        if (LOG_SQL_FLAG) {
            logSqlString(stmt.toString());
        }
    }

    private void logSqlString(String sql) {
        if (LOG_SQL_FLAG) {
            LOG.debug(":SQL: " + sql);
        }
    }

    private UUID datastoreId = null; // :???: verify not `static`
    private PostgresIdentifierProvider identifierProvider;

    public HikariDataSource ds;

    public PostgresProvider() {
        //Construct with HK2 only
        LOG.info("Contructor PostgresProvider()");
    }

    //
    // DataStoreSubService
    //     startup(), shutdown()
    //   DataStore
    //     
    //     DatastoreServices
    //        validate that databases & lucene directories
    //        uniformly exist and are 
    //        uniformly populated during startup.
    @Override // DataStoreSubService
    public void startup() {
        startup(this);
    }

    public void startup(DataStore store) {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Postgres provider");
        Get.executor().execute(progressTask);
        LOG.info("Starting PostgresProvider proceeding to (or at) runlevel: {}", LookupService.getProceedingToRunLevel());
        try {
            try {
                String isaacDbUrl = System.getProperty("ISAAC_PSQL_URL", "jdbc:postgresql://localhost/isaac_db");
                String isaacUsername = System.getProperty("ISAAC_PSQL_UNAME", "isaac_user");
                String isaacUserpwd = System.getProperty("ISAAC_PSQL_UPWD", "isaac_pwd");

                HikariConfig config = new HikariConfig();

                config.setJdbcUrl(isaacDbUrl);
                config.setUsername(isaacUsername);
                config.setPassword(isaacUserpwd);
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                this.ds = new HikariDataSource(config);

                // ? table uuid_oid UUID primary key, nid serial
                // ? table uuid_oid UUID primary key, nid int foreign key
                try (Connection conn = ds.getConnection()) {

                    //conn.setAutoCommit(false);
                    LOG.info("Connection: " + conn.getClientInfo());
                    // A database contains one or more named schemas, which in turn contain tables
                    LOG.info("Schema: " + conn.getSchema());
                    // name of current database (called “catalog” in the SQL standard)
                    LOG.info("Catalog: " + conn.getCatalog());

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS datastore_id_table "
                                + "( datastore_puid uuid, PRIMARY KEY (datastore_puid) ); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        logSqlString(sqlReadDatastoreId());
                        ResultSet resultSet = stmt.executeQuery(sqlReadDatastoreId());

                        if (resultSet.next()) {
                            datastoreId = resultSet.getObject(1, UUID.class);
                        } else {
                            String sql = "INSERT INTO datastore_id_table (datastore_puid) VALUES (?) ; ";
                            try (PreparedStatement stmt1 = conn.prepareStatement(sql)) {
                                stmt1.setObject(1, UUID.randomUUID());
                                logSqlStmt(stmt1);
                                stmt1.execute();
                            }

                            try (Statement stmt2 = conn.createStatement()) {
                                logSqlString(sqlReadDatastoreId());
                                resultSet = stmt2.executeQuery(sqlReadDatastoreId());
                                if (resultSet.next()) {
                                    datastoreId = resultSet.getObject(1, UUID.class);
                                } else {
                                    LOG.error("PostgresProvider failed to setup DatastoreID.");
                                }
                            }

                        }
                    } catch (SQLException ex) {
                        LOG.error(ex.getLocalizedMessage(), ex);
                    }

                    // create a -1 stamped version where the binary stores the UUID, class type, etc.
                    // maybe create an Object table...
                    // version table...
                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS identified_objects_table "
                                + "(o_nid INTEGER,"
                                + " assemblage_nid INTEGER,"
                                + " version_stamp INTEGER," // -1 for the first record
                                + " version_data bytea,"
                                + " PRIMARY KEY (o_nid, version_stamp) ); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS concepts_table "
                                + "() INHERITS (identified_objects_table); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS concepts_table_pkey "
                                + "ON concepts_table  USING btree (o_nid, version_stamp); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS semantics_table "
                                + "(referenced_component_nid INTEGER) "
                                + "INHERITS (identified_objects_table); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS semantics_table_pkey "
                                + "ON semantics_table USING btree (o_nid, version_stamp); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE INDEX IF NOT EXISTS semantics_table_assemblage_idx "
                                + "ON semantics_table USING btree (assemblage_nid); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE INDEX IF NOT EXISTS semantics_table_referenced_component_idx "
                                + "ON semantics_table USING btree (referenced_component_nid); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS type_for_assemblage_table "
                                + "(assemblage_nid INTEGER,"
                                + " assemblage_type_token INTEGER,"
                                + " version_type_token INTEGER,"
                                + " PRIMARY KEY(assemblage_nid) ); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }

                    try (Statement stmt = conn.createStatement()) {
                        String sqlCreate = "CREATE TABLE IF NOT EXISTS taxonomy_data_table "
                                + "(t_nid INTEGER,"
                                + " assemblage_nid INTEGER,"
                                + " taxonomy_data bytea,"
                                + " CONSTRAINT taxonomy_data_pk UNIQUE (t_nid,assemblage_nid),"
                                + " PRIMARY KEY(t_nid, assemblage_nid) ); ";
                        logSqlString(sqlCreate);
                        stmt.execute(sqlCreate);
                    }
                }

            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            } catch (Exception e) {
                // Failed to initialize pool: Connection to localhost:5432 refused.
                //  Connection to localhost:5432 refused.
                // Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
                LOG.error(e.getLocalizedMessage(), e);
            } finally {
                LOG.info("Connection closed.");
            }
            this.identifierProvider = new PostgresIdentifierProvider(store, this.ds);

            this.identifierProvider.startMe();
        } finally {
            progressTask.finished();
        }
    }

    @Override // DataStoreSubService
    public void shutdown() {
        this.identifierProvider.stopMe();
        if (this.ds != null) {
            this.ds.close();
        }
    }
// Use inheritence for UUID nid tables... ? Similar to partition scheme?

    @Override
    public int getMaxNid() {
        return this.identifierProvider.getMaxNid();
    }

    String sqlCreateSemantic() {
        // if not exists partition, insert it. 
        // then insert. Turn autocommit off?
        return "INSERT INTO semantics_table "
            + "(o_nid, assemblage_nid, referenced_component_nid, version_stamp, version_data) "
            + "VALUES (?,?,?,?,?) "
            + "ON CONFLICT DO NOTHING; ";
    }

    String sqlReadTaxonomyData() {
        return "SELECT taxonomy_data "
                + "FROM taxonomy_data_table "
                + "WHERE t_nid = ? AND assemblage_nid = ?; ";
    }

    String sqlReadAllTaxonomyData() {
        return "SELECT t_nid, taxonomy_data "
                + "FROM taxonomy_data_table "
                + "WHERE assemblage_nid = ?; ";
    }

    String sqlReadSemanticNidsForComponent() {
        return "SELECT o_nid FROM semantics_table "
            + "WHERE referenced_component_nid = ?; ";
    }

    String sqlCreateConcept() {
        return "INSERT INTO concepts_table "
            + "(o_nid, assemblage_nid, version_stamp, version_data) "
            + "VALUES (?,?,?,?) "
            + "ON CONFLICT DO NOTHING; ";
    }

    String sqlReadAssemblageNids() {
        return "SELECT DISTINCT(assemblage_nid) FROM type_for_assemblage_table; ";
    }

    String sqlReadAssemblageType() {
        return "SELECT assemblage_type_token FROM type_for_assemblage_table "
            + "WHERE assemblage_nid = ?; ";
    }

    String sqlReadAssemblageNidForObjectNid() {
        return "SELECT assemblage_nid FROM identified_objects_table "
            + "WHERE o_nid = ?; ";
    }
    String loadAssemblageNidForObjectNid() {
        return "SELECT o_nid, assemblage_nid FROM identified_objects_table ORDER BY o_nid ASC;";
    }

    String sqlReadAssemblageNidsForAssemblageType() {
        return "SELECT assemblage_nid FROM type_for_assemblage_table "
            + "WHERE assemblage_type_token = ?; ";
    }

    String sqlReadIdentifiedObjectData() {
        return "SELECT version_data "
            + "FROM identified_objects_table "
            + "WHERE o_nid = ? "
            + "ORDER BY version_stamp ASC; ";
    }

    String sqlReadVersionTypeForAssemblage() {
        return "SELECT version_type_token FROM type_for_assemblage_table "
            + "WHERE assemblage_nid = ?; ";
    }

    String sqlUpsertAssemblageVersionType() {
        return "INSERT INTO type_for_assemblage_table "
            + "(assemblage_nid, assemblage_type_token, version_type_token) "
            + "VALUES (?,?,?) "
            + "ON CONFLICT (assemblage_nid) "
            + "DO UPDATE SET version_type_token = ?; ";
    }

    String sqlUpsertAssemblageType() {
        return "INSERT INTO type_for_assemblage_table "
            + "(assemblage_nid, assemblage_type_token, version_type_token) "
            + "VALUES (?,?,?) "
            + "ON CONFLICT (assemblage_nid) "
            + "DO UPDATE SET assemblage_type_token = ?; ";
    }

    String sqlReadConceptVersions() {
        return "SELECT assemblage_nid, version_stamp, version_data "
            + "FROM concepts_table WHERE o_nid = ?; ";
    }

    String sqlReadDatastoreId() {
        return "SELECT * FROM datastore_id_table; ";
    }

    String sqlReadSemanticVersions() {
        return "SELECT assemblage_nid, referenced_component_nid, version_stamp, version_data FROM semantics_table WHERE o_nid = ?; ";
    }

    String sqlReadNidsForAssemblage() {
        return "SELECT o_nid FROM identified_objects_table WHERE assemblage_nid = ?; ";
    }

    String sqlReadUuidPrimordialForNid() {
        return "SELECT ouid FROM uuid_primordial_table WHERE u_nid = ?; ";
    }

    String sqlReadUuidAdditionalForNid() {
        return "SELECT ouid FROM uuid_additional_table WHERE u_nid = ?; ";
    }

    String sqlUpsertTaxonomyData() {
        return "INSERT INTO taxonomy_data_table "
            + "(t_nid, assemblage_nid, taxonomy_data) "
            + "VALUES (?,?,?) "
            + "ON CONFLICT ON CONSTRAINT taxonomy_data_pk "
            + "DO UPDATE SET taxonomy_data = ?; ";
    }

    ///////////////////////////////////////
    //// DataStoreSubService:DataStore ////
    ///////////////////////////////////////
    // 
    private List<byte[]> getDataList(ChronologySerializeable chronology) {

        List<byte[]> dataArray = new ArrayList<>();

        byte[] dataToSplit = chronology.getChronologyVersionDataToWrite();
        int versionStartPosition = ((ChronologyImpl) chronology).getVersionStartPosition();
        if (versionStartPosition < 0) {
            throw new IllegalStateException("versionStartPosition is not set");
        }

        // +4 for the zero integer to start.
        //byte[] chronicleBytes = new byte[versionStartPosition + 4];
        //for (int i = 0; i < chronicleBytes.length; i++) {
        //    if (i < 4) {
        //        chronicleBytes[i] = 0;
        //    } else {
        //        chronicleBytes[i] = dataToSplit[i - 4];
        //    }
        //}
        //dataArray.add(chronicleBytes);
        // identified object chronical bytes which preceed versions' bytes
        byte[] chronicleBytes = new byte[versionStartPosition];
        for (int i = 0; i < chronicleBytes.length; i++) {
            chronicleBytes[i] = dataToSplit[i];
        }
        dataArray.add(chronicleBytes);

        int versionStart = versionStartPosition;
        int versionSize = getInt(dataToSplit, versionStart);

        while (versionSize != 0) {
            int versionTo = versionStart + versionSize;
            int newLength = versionTo - versionStart;
            if (versionTo < 0) {
                LOG.error("Error versionTo: " + versionTo);
            }
            if (newLength < 0) {
                LOG.error("Error newLength: " + newLength);
            }
            dataArray.add(Arrays.copyOfRange(dataToSplit, versionStart, versionTo));
            versionStart = versionStart + versionSize;
            versionSize = getInt(dataToSplit, versionStart);
        }

        return dataArray;
    }

    @Override // DataStoreSubService:DataStore
    public void putChronologyData(ChronologySerializeable chronology) {
        try {
            int chronologyNid = chronology.getNid();
            int assemblageNid = chronology.getAssemblageNid();
            int[] versionStampSequences = chronology.getVersionStampSequences();
            List<byte[]> dataList = getDataList(chronology);

            // :DEBUG:BEGIN:
            if (LOG_BYTECHECK_FLAG) {
                StringBuilder sb = new StringBuilder();
                dataList.forEach((dv) -> {
                    sb.append(ByteArrayDataBuffer.printHexBinary(dv)).append(" * ");
                });
                LOG.debug("--+ :" + chronologyNid + ":BYTECHECK:PUT: " + sb.toString());
            }
            // :DEBUG:END:

            if (chronology instanceof ConceptChronologyImpl) {
                ConceptChronologyImpl concept = (ConceptChronologyImpl) chronology;
                try (
                    Connection conn = this.ds.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlCreateConcept())) {
                    for (int i = 0; i < dataList.size(); i++) {
                        byte[] bytes = dataList.get(i);
                        stmt.setInt(1, chronologyNid); // o_nid
                        stmt.setInt(2, assemblageNid); // assemblage_nid
                        if (i == 0) {
                            stmt.setInt(3, -1); // version_stamp, base row.
                        } else {
                            stmt.setInt(3, versionStampSequences[i - 1]);
                        }
                        stmt.setBytes(4, bytes);   // version_data
                        logSqlBytea(stmt, bytes);
                        stmt.addBatch();
                    }
                    int[] updateCounts = stmt.executeBatch();
                } catch (SQLException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            } else {
                SemanticChronologyImpl semantic = (SemanticChronologyImpl) chronology;
                int referencedComponentNid = semantic.getReferencedComponentNid();
                try (
                    Connection conn = this.ds.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlCreateSemantic())) {
                    for (int i = 0; i < dataList.size(); i++) {
                        byte[] bytes = dataList.get(i);
                        stmt.setInt(1, chronologyNid); // o_nid
                        stmt.setInt(2, assemblageNid); // assemblage_nid
                        stmt.setInt(3, referencedComponentNid); // referenced_component_nid
                        if (i == 0) {
                            stmt.setInt(4, -1); // version_stamp
                        } else {
                            stmt.setInt(4, versionStampSequences[i - 1]);
                        }
                        stmt.setBytes(5, bytes);   // version_data
                        logSqlBytea(stmt, bytes);
                        stmt.addBatch();
                    }
                    int[] updateCounts = stmt.executeBatch();
                } catch (SQLException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
            }

        } catch (NoSuchElementException e) {
            LOG.error("Unexpected error putting chronology data!", e);
            throw e;
        }
    }

    protected byte[] getVersionBytes(SemanticVersionImpl semanticVersion) {
        ByteArrayDataBuffer srcData = new ByteArrayDataBuffer();
        semanticVersion.writeVersionData(srcData);
        int length = srcData.getPosition();
        byte[] destBytes = new byte[length];
        System.arraycopy(srcData.getData(), 0, destBytes, 0, length);
        return destBytes;
    }

    @Override // DataStoreSubService:DataStore
    public int[] getAssemblageConceptNids() {
        // :NOTE: returns nids for concepts that define assemblages.
        IntArrayList results = new IntArrayList();
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadAssemblageNids())) {
            logSqlStmt(stmt);
            ResultSet resultsSet = stmt.executeQuery();

            while (resultsSet.next()) {
                results.add(resultsSet.getInt(1));
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        results.trimToSize();
        return results.elements();
    }

    @Override // DataStoreSubService:DataStore
    public IsaacObjectType getIsaacObjectTypeForAssemblageNid(int assemblageNid) {
        // :NOTE: return type or IsaacObjectType#UNKNOWN
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadAssemblageType())) {
            stmt.setInt(1, assemblageNid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    return IsaacObjectType.fromToken((byte) resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return IsaacObjectType.UNKNOWN;
    }

    @Override // DataStoreSubService:DataStore
    public NidSet getAssemblageNidsForType(IsaacObjectType type) {
        // :NOTE: concept nids of all assemblages that are of the specified type
        NidSet results = new NidSet();
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadAssemblageNidsForAssemblageType())) {
            stmt.setInt(1, type.getToken());
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    results.add(resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return results;
    }

    @Override // DataStoreSubService:DataStore
    public void putAssemblageIsaacObjectType(int assemblageNid, IsaacObjectType type) throws IllegalStateException {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlUpsertAssemblageType())) {
            stmt.setInt(1, assemblageNid);   // assemblage_nid
            stmt.setInt(2, type.getToken()); // assemblage_type_token
            stmt.setInt(3, VersionType.UNKNOWN.getVersionTypeToken()); // version_type_token
            stmt.setInt(4, type.getToken()); // assemblage_type_token
            logSqlStmt(stmt);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    @Override  // DataStoreSubService:DataStore
    public Optional<ByteArrayDataBuffer> getChronologyVersionData(int nid) {
        OptionalInt assemblageNidOptional = ModelGet.identifierService().getAssemblageNid(nid);
        if (!assemblageNidOptional.isPresent()) {
            return Optional.empty();
        }
        final List<byte[]> dataList = new ArrayList();
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadIdentifiedObjectData())) {
            stmt.setInt(1, nid);
            logSqlStmt(stmt);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                byte[] bytes = resultSet.getBytes(1);
                dataList.add(bytes);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }

        if (dataList.isEmpty()) {
            return Optional.empty();
        }

        int size = 0;
        for (byte[] dataEntry : dataList) {
            size = size + dataEntry.length;
        }

        // +4 bytes for 0 int value at end to indicate last version
        ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(size + 4);

        for (int i = 0; i < dataList.size(); i++) {
            byte[] data = dataList.get(i);
            //if (i == 0) {
            //    // discard the 0 integer at the beginning of the record. 
            //    // 0 put in to enable the chronicle to sort before the versions. 
            //    if (data[0] != 0 && data[1] != 0 && data[2] != 0 && data[3] != 0) {
            //        throw new IllegalStateException("Record does not start with zero...");
            //    }
            //    // skip initial four x0000 bytes.
            //    byteBuffer.put(data, 4, data.length - 4);
            //} else {
            //    byteBuffer.put(data);
            //}
            byteBuffer.put(data);
        }

        byteBuffer.putInt(0); // x00000000 termination 
        byteBuffer.rewind();

        // :DEBUG:BEGIN:
        if (LOG_BYTECHECK_FLAG) {
            StringBuilder sb = new StringBuilder();
            dataList.forEach((dv) -> {
                sb.append(ByteArrayDataBuffer.printHexBinary(dv)).append(" * ");
            });
            LOG.debug("--+ :" + nid + ":BYTECHECK:GET: " + sb.toString());
            byte[] byteBufferBytes = byteBuffer.getData();
            LOG.debug("--+ :" + nid + ":BYTECHECK-GET: " + ByteArrayDataBuffer.printHexBinary(byteBufferBytes));
        }
        // :DEBUG:END:

        // if (byteBuffer.getUsed() != size) { // used for x00000000 header|termination
        if (byteBuffer.getUsed() != size + 4) {
            throw new IllegalStateException("Size+4 = " + size + 4 + " used = " + byteBuffer.getUsed());
        }

        return Optional.of(byteBuffer);
    }

    @Override // DataStoreSubService:DataStore
    public int[] getSemanticNidsForComponent(int componentNid) {
        NidSet results = new NidSet();
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadSemanticNidsForComponent())) {
            stmt.setInt(1, componentNid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    results.add(resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return results.asArray();
    }

    @Override // DataStoreSubService:DataStore
    public OptionalInt getAssemblageOfNid(int nid) {
        // Get the assemblage nid id that contains the identified object nid.
        // param: nid The nid of the object to find the assemblage container for
        // return: the assemblage nid that contains the nid
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadAssemblageNidForObjectNid())) {
            stmt.setInt(1, nid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    return OptionalInt.of(resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return OptionalInt.empty();
    }

    @Override // DataStoreSubService:DataStore
    public void setAssemblageForNid(int nid, int assemblage) throws IllegalArgumentException {
        // NO-OP. or redundant write to IdentifiedObjectTable.
    }

    @Override // DataStoreSubService:DataStore
    public int[] getTaxonomyData(int assemblageNid, int conceptNid) {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadTaxonomyData())) {
            stmt.setInt(1, conceptNid); // t_nid
            stmt.setInt(2, assemblageNid); // assemblage_nid
            logSqlStmt(stmt);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    byte[] taxonomyBytes = resultSet.getBytes(1);
                    ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(taxonomyBytes);
                    int[] taxonomyData = byteBuffer.getIntArray();
                    return taxonomyData;
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return new int[0];
    }

    private void putTaxonomyData(int assemblageNid, int conceptNid, int[] taxonomyData) {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlUpsertTaxonomyData())) {
            stmt.setInt(1, conceptNid); // t_nid
            stmt.setInt(2, assemblageNid); // assemblage_nid

            // 4 bytes inbound taxonomyData int[] length + 4 bytes per element
            ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer((taxonomyData.length * 4) + 4);
            byteBuffer.putIntArray(taxonomyData);

            byte[] taxonomyBytes = byteBuffer.getData();

            stmt.setBytes(3, taxonomyBytes); // taxonomy_data
            stmt.setBytes(4, taxonomyBytes); // taxonomy_data

            //System.out.println(":DEBUG:taxonomyData:" + Arrays.toString(taxonomyData)); // :DEBUG:
            //System.out.println(":DEBUG:taxonomyBytes:" + Arrays.toString(taxonomyBytes)); // :DEBUG:
            logSqlStmt(stmt);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    @Override // DataStoreSubService:DataStore
    public int[] accumulateAndGetTaxonomyData(int assemblageNid, int conceptNid, int[] newData, BinaryOperator<int[]> accumulatorFunction) {
        StringBuilder sb = new StringBuilder();
        sb.append(":TAXONOMY: ");
        sb.append(debugGetUuidFromInt(assemblageNid).toString());
        sb.append(" concept=");
        sb.append(debugGetUuidFromInt(conceptNid).toString());
        LOG.debug(sb.toString());

        int[] oldData = getTaxonomyData(assemblageNid, conceptNid);

        if (oldData.length == 0) {
            if (newData.length == 0) {
                return new int[0];
            }
            putTaxonomyData(assemblageNid, conceptNid, newData);
            return newData;
        } else {
            if (newData.length == 0) {
                return oldData;
            }
            int[] mergedData = accumulatorFunction.apply(oldData, newData);
            if (!Arrays.equals(mergedData, oldData)) {
                putTaxonomyData(assemblageNid, conceptNid, mergedData);
            }
            return mergedData;
        }
    }

    private UUID debugGetUuidFromInt(int nid) {
        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlReadUuidPrimordialForNid())) {
                stmt.setInt(1, nid);
                logSqlStmt(stmt);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        UUID uuidOid = resultSet.getObject(1, UUID.class);
                        return uuidOid;
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return new UUID(0L, 0L);
    }

    @Override // DataStoreSubService:DataStore
    public VersionType getVersionTypeForAssemblageNid(int assemblageNid) {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadVersionTypeForAssemblage())) {
            stmt.setInt(1, assemblageNid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    return VersionType.getFromToken((byte) resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return VersionType.UNKNOWN;
    }

    @Override // DataStoreSubService:DataStore
    public void putAssemblageVersionType(int assemblageNid, VersionType type) throws IllegalStateException {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlUpsertAssemblageVersionType())) {
            stmt.setInt(1, assemblageNid); // assemblage_nid
            stmt.setInt(2, IsaacObjectType.UNKNOWN.getToken()); // assemblage_type_token
            stmt.setInt(3, type.getVersionTypeToken()); // version_type_token
            stmt.setInt(4, type.getVersionTypeToken());
            logSqlStmt(stmt);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    @Override // DataStoreSubService:DataStore
    public int getAssemblageMemoryInUse(int assemblageNid) {
        return -1;
    }

    @Override // DataStoreSubService:DataStore
    public int getAssemblageSizeOnDisk(int assemblageNid) {
        return -1;
    }

    @Override // DataStoreSubService:DataStore
    public boolean hasChronologyData(int nid, IsaacObjectType ofType) {
        if (null == ofType) {
            throw new UnsupportedOperationException("Can't handle null.");
        } else {
            switch (ofType) {
            case CONCEPT:
                try (Connection conn = this.ds.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlReadConceptVersions())) {
                    stmt.setInt(1, nid);
                    logSqlStmt(stmt);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        return resultSet.next();
                    }
                } catch (SQLException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
                break;

            case SEMANTIC:
                try (Connection conn = this.ds.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sqlReadSemanticVersions())) {
                    stmt.setInt(1, nid);
                    logSqlStmt(stmt);
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        return resultSet.next();
                    }
                } catch (SQLException ex) {
                    LOG.error(ex.getLocalizedMessage(), ex);
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't handle " + ofType);
            }
        }
        return false;
    }

    @Override // DataStoreSubService:DataStore
    public void registerDataWriteListener(DataWriteListener dataWriteListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override // DataStoreSubService:DataStore
    public void unregisterDataWriteListener(DataWriteListener dataWriteListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override // DataStoreSubService:DataStore
    public IntStream getNidsForAssemblage(int assemblageNid) {
        NidSet results = new NidSet();
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadNidsForAssemblage())) {
            stmt.setInt(1, assemblageNid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    results.add(resultSet.getInt(1));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return results.stream();
    }

    @Override // DataStoreSubService:DataStore
    public IntStream getNidsForAssemblageParallel(int assemblageNid) {
        return getNidsForAssemblage(assemblageNid).parallel();
    }

    @Override // DataStoreSubService:DataStore
    public boolean implementsSequenceStore() {
        return false;
    }

    // DataStoreSubService:DataStore:DatastoreServices
    //        validate that databases & lucene directories
    //        uniformly exist and are 
    //        uniformly populated during startup.
    @Override // DataStoreStartState:DataStore:DatastoreServices
    public Path getDataStorePath() {
        // The path where the data store provider stores its on-disk data.
        return Paths.get("PostgresProviderPath");
    }

    @Override // DataStoreStartState:DataStore:DatastoreServices
    public DataStoreStartState getDataStoreStartState() {
        // Returns database validity status
        // NOT_YET_CHECKED // Initial & shutdown state. Starting point.
        // NO_DATASTORE // datastore directory is missing or empty
        // EXISTING_DATASTORE // An existing data store is present
        // :NYI: DataStoreStartState.EXISTING_DATASTORE state changes
        return DataStoreStartState.NO_DATASTORE;
    }

    @Override // DataStoreStartState:DataStore:DatastoreServices
    public Optional<UUID> getDataStoreId() {
        // :NOTE: Return the UUID that was generated when the datastore was first created
        // :NOTE: empty during NOT_YET_CHECKED and NO_DATASTORE states
        //return Optional.empty();
        return Optional.of(datastoreId);
    }

    @Override // DataStoreStartState:DataStore:DatastoreServices
    public Future<?> sync() {
        // :NOTE: needs to write any pending data to disk.
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void addUuidForNid(UUID uuid, int nid) {
        this.identifierProvider.addUuidForNid(uuid, nid);
    }

    @Override
    public OptionalInt getAssemblageNid(int componentNid) {
        return this.identifierProvider.getAssemblageNid(componentNid);
    }

    @Override
    public int[] getAssemblageNids() {
        return this.identifierProvider.getAssemblageNids();
    }

    @Override
    public int assignNid(UUID... uuids) throws IllegalArgumentException {
        return this.identifierProvider.assignNid(uuids);
    }

    @Override
    public IntStream getNidStreamOfType(IsaacObjectType objectType) {
        return this.identifierProvider.getNidStreamOfType(objectType);
    }

    @Override
    public IsaacObjectType getObjectTypeForComponent(int componentNid) {
        return this.identifierProvider.getObjectTypeForComponent(componentNid);
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
        return this.identifierProvider.getNidForUuids(uuids);
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws NoSuchElementException {
        return this.identifierProvider.getNidForUuids(uuids);
    }

    @Override
    public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
        return this.identifierProvider.hasUuid(uuids);
    }

    @Override
    public boolean hasUuid(UUID... uuids) throws IllegalArgumentException {
        return this.identifierProvider.hasUuid(uuids);
    }

    @Override
    public UUID getUuidPrimordialForNid(int nid) throws NoSuchElementException {
        return this.identifierProvider.getUuidPrimordialForNid(nid);
    }

    @Override
    public List<UUID> getUuidsForNid(int nid) throws NoSuchElementException {
        return this.identifierProvider.getUuidsForNid(nid);
    }

    @Override
    public long getMemoryInUse() {
        return this.identifierProvider.getMemoryInUse();
    }

    @Override
    public long getSizeOnDisk() {
        return this.identifierProvider.getSizeOnDisk();
    }

    @Override
    public void setupNid(int nid, int assemblageNid, IsaacObjectType objectType, VersionType versionType) throws IllegalStateException {
        this.identifierProvider.setupNid(nid, assemblageNid, objectType, versionType);
    }

    /**
     * @see sh.isaac.api.IdentifierService#optimizeForOutOfOrderLoading()
     */
    @Override
    public void optimizeForOutOfOrderLoading() {
        this.identifierProvider.optimizeForOutOfOrderLoading();
    }

    @Override
    public void loadAssemblageOfNid(SpinedNidIntMap nidToAssemblageNidMap) {
        // Get the assemblage nid id that contains the identified object nid.
        // param: nid The nid of the object to find the assemblage container for
        // return: the assemblage nid that contains the nid
        try (Connection conn = this.ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(loadAssemblageNidForObjectNid())) {
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    nidToAssemblageNidMap.put(resultSet.getInt(1), resultSet.getInt(2));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public void loadTaxonomyData(int assemblageNid, SpinedIntIntArrayMap taxonomyDataMap) {
        try (Connection conn = this.ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlReadAllTaxonomyData())) {
            stmt.setInt(1, assemblageNid);
            logSqlStmt(stmt);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    byte[] taxonomyBytes = resultSet.getBytes(2);
                    ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(taxonomyBytes);
                    int[] taxonomyData = byteBuffer.getIntArray();
                    taxonomyDataMap.put(resultSet.getInt(1), taxonomyData);
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }
}
