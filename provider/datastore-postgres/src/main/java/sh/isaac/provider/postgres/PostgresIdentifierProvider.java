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

import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.uuidnidmap.UuidToIntMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- classes ----------------------------------------------------------------
/**
 *
 */
public class PostgresIdentifierProvider
    implements IdentifierService {

    private static final Logger LOG = LogManager.getLogger();
    private static final boolean LOG_SQL_FLAG = false;

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

    //~--- fields --------------------------------------------------------------
    /*
        nid -> assemblage nid
        nid -> entry sequence
        nid -> uuid[] (store as single byte array?)
        entry sequence + assemblage nid -> nid
        uuid -> nid with generation...
     */
    private final HikariDataSource ds;
    private final DataStore store;
    private UuidToIntMap uuidIntMapMap;

    public PostgresIdentifierProvider(DataStore store, HikariDataSource ds) {
        LOG.info("Contructor PostgresIdentifierProvider()");
        this.store = store;
        this.ds = ds;
    }

    //~--- sql -----------------------------------------------------------------
    String sqlReadUuidAdditionalForNid() {
        return "SELECT ouid FROM uuid_additional_table "
            + "WHERE u_nid = ?; ";
    }

    String sqlReadUuidPrimordialForNid() {
        return "SELECT ouid FROM uuid_primordial_table "
            + "WHERE u_nid = ?; ";
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void addUuidForNid(UUID uuid, int nidNew) {
        OptionalInt nidOld = this.uuidIntMapMap.get(uuid);
        if (nidOld.isPresent() && nidOld.getAsInt() != nidNew) {
            throw new RuntimeException("Reassignment of nid for " + uuid
                + " from " + nidOld + " to " + nidNew);
        }
        this.uuidIntMapMap.put(uuid, nidNew);
    }

    private boolean isSolorRootUuidPresent() {
        // SOLOR_ROOT "7c21b6c5-cf11-5af9-893b-743f004c97f5"
        String sql = "SELECT * FROM uuid_primordial_table "
            + "WHERE ouid = '7c21b6c5-cf11-5af9-893b-743f004c97f5'::uuid;";
        try (Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {
            logSqlString(sql);
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                return true;
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    /**
     * Start me.
     */
    @PostConstruct
    public void startMe() {
        LOG.info("Starting PostgresIdentifierProvider to (or at) runlevel: {}",
            LookupService.getProceedingToRunLevel());

        try {

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
                    String sqlCreate = "CREATE TABLE IF NOT EXISTS uuid_table ( "
                        + "u_nid INTEGER, "
                        + "ouid uuid, "
                        + "UNIQUE (ouid), "
                        + "PRIMARY KEY (u_nid, ouid) ); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE TABLE IF NOT EXISTS uuid_primordial_table ( "
                        + "UNIQUE (u_nid) "
                        + ") INHERITS (uuid_table); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS uuid_primordial_table_ouid_key "
                        + "ON uuid_primordial_table USING btree (ouid); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS uuid_primordial_table_pkey "
                        + "ON uuid_primordial_table USING btree (u_nid, ouid); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE TABLE IF NOT EXISTS uuid_additional_table ( "
                        + ") INHERITS (uuid_table); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS uuid_additional_table_ouid_key "
                        + "ON uuid_additional_table USING btree (ouid); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE UNIQUE INDEX IF NOT EXISTS uuid_additional_table_pkey "
                        + "ON uuid_additional_table USING btree (u_nid, ouid); ";
                    logSqlString(sqlCreate);
                    stmt.execute(sqlCreate);
                }

                try (Statement stmt = conn.createStatement()) {
                    String sqlCreate = "CREATE SEQUENCE IF NOT EXISTS nid_sequence "
                        + "MINVALUE -2147483647 START WITH -2147483647; ";
                    // :PSQL10: AS INTEGER
                    logSqlString(sqlCreate);
                    boolean result = stmt.execute(sqlCreate);
                    if (result) {
                        // is_called false: nextval will return -2147483647 before advancing
                        String sqlSetVal = "SELECT setval('nid_sequence', -2147483647, false); ";
                        logSqlString(sqlSetVal);
                        stmt.execute(sqlSetVal);
                        // Next `nextval` will return -2147483647
                    }

                }
            }

            this.uuidIntMapMap = new PostgresUuidToIntMap(ds);

            if (isSolorRootUuidPresent() == false) {
                //bootstrap our nids for core metadata concepts.  
                for (ConceptSpecification cs : TermAux.getAllSpecs()) {
                    assignNid(cs.getUuids());
                }
            }

        } catch (SQLException | IllegalArgumentException | UnsupportedOperationException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        } // Failed to initialize pool: Connection to localhost:5432 refused.
        //  Connection to localhost:5432 refused.
        // Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections.
        finally {
            LOG.info("Connection closed.");
        }

    }

    /**
     * Stop me.
     */
    @PreDestroy
    protected void stopMe() {
        if (this.ds != null) {
            this.ds.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupNid(int nid, int assemblageNid, IsaacObjectType isaacObjectType, VersionType versionType) {
        if (versionType == VersionType.UNKNOWN) {
            throw new IllegalStateException("versionType may not be unknown. ");
        }
        this.store.setAssemblageForNid(nid, assemblageNid);

        IsaacObjectType oldObjectType = this.store.getIsaacObjectTypeForAssemblageNid(assemblageNid);
        if (oldObjectType == IsaacObjectType.UNKNOWN) {
            this.store.putAssemblageIsaacObjectType(assemblageNid, isaacObjectType);
        }

        VersionType oldVersionType = this.store.getVersionTypeForAssemblageNid(assemblageNid);
        if (oldVersionType == VersionType.UNKNOWN) {
            this.store.putAssemblageVersionType(assemblageNid, versionType);
        }
    }

    //~--- getValueSpliterator methods ---------------------------------------------------------
    @Override
    public IsaacObjectType getObjectTypeForComponent(int componentNid) {
        OptionalInt optionalAssemblageNid = getAssemblageNid(componentNid);
        if (optionalAssemblageNid.isPresent()) {
            IsaacObjectType temp = this.store.getIsaacObjectTypeForAssemblageNid(optionalAssemblageNid.getAsInt());
            if (temp == IsaacObjectType.UNKNOWN) {
                Optional<? extends Chronology> temp2 = Get.identifiedObjectService().getChronology(componentNid);
                if (temp2.isPresent()) {
                    LOG.error("Object {} in store, but not in object type map?", componentNid);
                    return temp2.get().getIsaacObjectType();
                }
            }
            return temp;
        }
        return IsaacObjectType.UNKNOWN;
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws NoSuchElementException {
        return getNidForUuids(uuids.toArray(new UUID[uuids.size()]));
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws NoSuchElementException {

        for (final UUID uuid : uuids) {
            final OptionalInt nid = this.uuidIntMapMap.get(uuid);

            if (nid.isPresent()) {
                return nid.getAsInt();
            }
        }
        throw new NoSuchElementException("No nid found for " + Arrays.toString(uuids));
    }

    @Override
    public int assignNid(UUID... uuids) throws IllegalArgumentException {
        int lastFoundNid = Integer.MAX_VALUE;
        ArrayList<UUID> uuidsWithoutNid = new ArrayList<>(uuids.length);
        for (final UUID uuid : uuids) {
            final OptionalInt nid = this.uuidIntMapMap.get(uuid);

            if (nid.isPresent()) {
                if (lastFoundNid != Integer.MAX_VALUE && lastFoundNid != nid.getAsInt()) {
                    LOG.trace("Two UUIDs are being merged onto a single nid!  "
                        + "Found " + lastFoundNid + " and " + nid);
                    // I don't want to update lastFoundNid in this case, because
                    // the uuid -> nid mapping is for the previously checked UUID.
                    // This UUID will need to be remaped to a new nid:
                    uuidsWithoutNid.add(uuid);
                } else {
                    lastFoundNid = nid.getAsInt();
                }
            } else {
                uuidsWithoutNid.add(uuid);
            }
        }

        if (lastFoundNid != Integer.MAX_VALUE) {
            for (UUID uuid : uuidsWithoutNid) {
                addUuidForNid(uuid, lastFoundNid);
            }
            return lastFoundNid;
        }
        final int nid = this.uuidIntMapMap.getWithGeneration(uuids[0]);

        for (int i = 1; i < uuids.length; i++) {
            this.uuidIntMapMap.put(uuids[i], nid);
        }
        return nid;
    }

    @Override
    public boolean hasUuid(Collection<UUID> uuids) throws IllegalArgumentException {
        if (uuids == null || uuids.isEmpty()) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }
        for (UUID uuid : uuids) {
            if (this.uuidIntMapMap.containsKey(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasUuid(UUID... uuids) throws IllegalArgumentException {
        if (uuids == null || uuids.length == 0) {
            throw new IllegalArgumentException("A UUID must be specified.");
        }

        for (UUID uuid : uuids) {
            if (this.uuidIntMapMap.containsKey(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public UUID getUuidPrimordialForNid(int nid) throws NoSuchElementException {
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadUuidPrimordialForNid())) {
            stmt.setInt(1, nid);
            logSqlStmt(stmt);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                UUID uuid = resultSet.getObject(1, UUID.class);
                return uuid;
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
            throw new NoSuchElementException("getUuidPrimordialForNid() SQL error");
        }
        throw new NoSuchElementException("getUuidPrimordialForNid() not found");
    }

    @Override
    public List<UUID> getUuidsForNid(int nid) throws NoSuchElementException {
        final ArrayList<UUID> uuidList = new ArrayList<>();
        uuidList.add(getUuidPrimordialForNid(nid));
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlReadUuidAdditionalForNid())) {
            stmt.setInt(1, nid);
            logSqlStmt(stmt);
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                UUID uuid = resultSet.getObject(1, UUID.class);
                uuidList.add(uuid);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return uuidList;
    }

    @Override
    public OptionalInt getAssemblageNid(int componentNid) {
        return this.store.getAssemblageOfNid(componentNid);
    }

    @Override
    public int[] getAssemblageNids() {
        return store.getAssemblageConceptNids();
    }

    @Override
    public IntStream getNidsForAssemblage(int assemblageNid) {
        return store.getNidsForAssemblage(assemblageNid);
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return store.getDataStoreId();
    }

    @Override
    public Path getDataStorePath() {
        return store.getDataStorePath();
    }

    @Override
    public DatastoreServices.DataStoreStartState getDataStoreStartState() {
        return store.getDataStoreStartState();
    }

    @Override
    public IntStream getNidStreamOfType(IsaacObjectType objectType) {
        int maxNid = this.uuidIntMapMap.getMaxNid();
        NidSet allowedAssemblages = this.store.getAssemblageNidsForType(objectType);

        return IntStream.rangeClosed(Integer.MIN_VALUE + 1, maxNid)
            .filter((value) -> {
                return allowedAssemblages.contains(this.store.getAssemblageOfNid(value).orElseGet(() -> Integer.MAX_VALUE));
            });
    }

    @Override
    public Future<?> sync() {
        return CompletableFuture.completedFuture(true);
    }

    //TODO [refactor] need to see if I'm reporting this as part of the datastore
    @Override
    public long getMemoryInUse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getSizeOnDisk() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // :NYI: This private PostgresUuidToIntMap class was used 
    // to support rapid integration of PostgreSQL.
    // PostgresUuidToIntMap could be potentially be reduced and
    // absorbed directly into ProstgresIdentifierProvider. 
    private class PostgresUuidToIntMap implements UuidToIntMap {

        HikariDataSource ds;

        public PostgresUuidToIntMap(HikariDataSource ds) {
            this.ds = ds;
            int nextNidProvider = nextNidProvider_Set();
            LOG.debug("- NEXT_NID_PROVIDER at " + nextNidProvider);
        }

        //~--- sql -----------------------------------------------------------------
        String sqlCreateUuidAdditional() {
            return "INSERT INTO uuid_additional_table (u_nid, ouid) "
                + "VALUES (?,?) ON CONFLICT DO NOTHING; ";
        }

        String sqlCreateUuidPrimordial() {
            return "INSERT INTO uuid_primordial_table (u_nid, ouid) "
                + "VALUES (?,?) ON CONFLICT DO NOTHING; ";
        }

        String sqlReadNidToUuid() {
            return "SELECT ouid FROM uuid_table WHERE u_nid = ?; ";
        }

        String sqlReadUuidToNid() {
            return "SELECT u_nid FROM uuid_table WHERE ouid = ?; ";
        }

        //~--- methods -------------------------------------------------------------
        //////////////////////
        //// UuidToIntMap ////
        //////////////////////
        // 
        @Override // UuidToIntMap
        public boolean containsKey(UUID keyUuid) {
            if (keyUuid == null) {
                throw new IllegalStateException("UUIDs cannot be null. ");
            }

            try (Connection conn = this.ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlReadUuidToNid())) {
                stmt.setObject(1, keyUuid);
                logSqlStmt(stmt);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    return true;
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        @Override // UuidToIntMap
        public boolean containsValue(int value) {
            try (Connection conn = this.ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlReadNidToUuid())) {
                stmt.setInt(1, value);
                logSqlStmt(stmt);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    return true;
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        @Override // UuidToIntMap :NYI:
        public boolean inverseCacheEnabled() {
            return false;
        }

        @Override
        public void enableInverseCache() {
            // not supported
        }

        private boolean putUuidNid(UUID uuidKey, int nid) {
            try (Connection conn = this.ds.getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlCreateUuidPrimordial())) {
                    stmt.setInt(1, nid);
                    stmt.setObject(2, uuidKey);
                    logSqlStmt(stmt);
                    int count = stmt.executeUpdate();
                    if (count > 0) {
                        return true;
                    }
                }
                try (PreparedStatement stmt = conn.prepareStatement(sqlCreateUuidAdditional())) {
                    stmt.setInt(1, nid);
                    stmt.setObject(2, uuidKey);
                    logSqlStmt(stmt);
                    int count = stmt.executeUpdate();
                    if (count > 0) {
                        return true;
                    }
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        @Override // UuidToIntMap
        public boolean put(UUID uuidKey, int nid) {
            // return true if the receiver did not already contain such a key;
            // return false if the receiver did already contain such a key - 
            // the new value has now replaced the formerly associated value.
            return putUuidNid(uuidKey, nid);
        }

        @Override // UuidToIntMap  // :_!_:
        public OptionalInt get(UUID keyUuid) {
            try (Connection conn = this.ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlReadUuidToNid())) {
                stmt.setObject(1, keyUuid);
                logSqlStmt(stmt);
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    return OptionalInt.of(resultSet.getInt(1));
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return OptionalInt.empty();
        }

        // :EDIT: replace NEXT_NID_PROVIDER
        // private final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);
        /**
         * The Constant NEXT_NID_PROVIDER.
         */
        private final AtomicInteger NEXT_NID_PROVIDER = new AtomicInteger(Integer.MIN_VALUE);

        private int nextNidProvider_IncrementAndGet() { // NEXT_NID_PROVIDER.incrementAndGet()
            String sqlSequenceNextval = "SELECT nextval('nid_sequence'); ";
            try (Connection conn = this.ds.getConnection();
                Statement stmt = conn.createStatement()) {
                logSqlString(sqlSequenceNextval);
                ResultSet resultSet = stmt.executeQuery(sqlSequenceNextval);
                while (resultSet.next()) {
                    int nextNidProvider = resultSet.getInt(1);
                    NEXT_NID_PROVIDER.set(nextNidProvider);
                    return nextNidProvider;
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            throw new NoSuchElementException("No 'nextval()' from 'nid_sequence'.");
        }

        private int nextNidProvider_Get() { // NEXT_NID_PROVIDER.get()
            return NEXT_NID_PROVIDER.get();
        }

        // Call once during startup to initize with the last value used.
        private int nextNidProvider_Set() { // NEXT_NID_PROVIDER.get()
            String sqlSequenceLastValue = "SELECT last_value FROM nid_sequence; ";
            try (Connection conn = this.ds.getConnection();
                Statement stmt = conn.createStatement()) {
                logSqlString(sqlSequenceLastValue);
                ResultSet resultSet = stmt.executeQuery(sqlSequenceLastValue);
                while (resultSet.next()) {
                    int nextNidProvider = resultSet.getInt(1);
                    NEXT_NID_PROVIDER.set(nextNidProvider);
                    return nextNidProvider;
                }
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return NEXT_NID_PROVIDER.get();
        }

        @Override // UuidToIntMap
        public int getMaxNid() {
            // :WAS: return NEXT_NID_PROVIDER.get();
            return nextNidProvider_Get();
        }

        @Override // UuidToIntMap
        public int getWithGeneration(UUID uuidKey) {
            OptionalInt nid = get(uuidKey);
            if (nid.isPresent()) {
                return nid.getAsInt();
            }

            // :NYI: handle primordial vs. additional
            int nidNew = nextNidProvider_IncrementAndGet();

            try (Connection conn = this.ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlCreateUuidPrimordial())) {
                stmt.setInt(1, nidNew);
                stmt.setObject(2, uuidKey);
                logSqlStmt(stmt);
                stmt.execute();
            } catch (SQLException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
            return nidNew;
        }

        @Override // UuidToIntMap
        public UUID[] getKeysForValue(int nid) {
//            final ArrayList<UUID> uuids = new ArrayList<>();
//            try (Connection conn = this.ds.getConnection();
//                PreparedStatement stmt = conn.prepareStatement(sqlReadNidToUuid())) {
//                stmt.setInt(1, nid);
//                logSqlStmt(stmt);
//                ResultSet resultSet = stmt.executeQuery();
//                while (resultSet.next()) {
//                    UUID uuid = resultSet.getObject(1, UUID.class);
//                    uuids.add(uuid);
//                }
//            } catch (SQLException ex) {
//                LOG.error(ex.getLocalizedMessage(), ex);
//            }
//            final UUID[] temp = uuids.toArray(new UUID[uuids.size()]);
//            return temp;
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override // UuidToIntMap :NYI: cacheContainsNid(int nid)
        public boolean cacheContainsNid(int nid) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override // UuidToIntMap :NYI:
        public int getDiskSpaceUsed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override // UuidToIntMap :NYI:
        public int getMemoryInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    /**
     * @see sh.isaac.api.IdentifierService#optimizeForOutOfOrderLoading()
     */
    @Override
    public void optimizeForOutOfOrderLoading() {
        uuidIntMapMap.enableInverseCache();
    }
}
