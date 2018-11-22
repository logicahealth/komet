/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.provider.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import javafx.concurrent.Task;
import javax.xml.bind.DatatypeConverter;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.Status;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.util.DataToBytesUtils;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author mc
 */
@Service(name = "Stamp Provider")
@RunLevel(value = LookupService.SL_L2)

public class PostgresStampProvider
    implements StampService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();
    private static final boolean LOG_SQL_FLAG = false;

    private void logSqlBytea(Statement stmt, byte[] bytes) {
        if (LOG_SQL_FLAG) {
            LOG.debug(":SQL: " + stmt.toString()
                + "; -- '"
                + DatatypeConverter.printHexBinary(bytes)
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

    private final AtomicReference<ConcurrentHashMap<UncommittedStamp, Integer>> cacheUncommittedStampToStampSequenceMap
        = new AtomicReference<>(
            new ConcurrentHashMap<>());

    //~--- fields --------------------------------------------------------------
    /**
     * The stamp lock.
     */
    private final ReentrantLock stampLock = new ReentrantLock();

    /**
     * The next stamp sequence.
     */
    private final AtomicInteger nextStampSequence = new AtomicInteger(FIRST_STAMP_SEQUENCE);

    /**
     * Persistent map of stamp sequences to a Stamp object.
     */
    private final ConcurrentHashMap<Stamp, Integer> cacheStampObjectToStampSequenceMap = new ConcurrentHashMap<>();

    /**
     * The stamp sequence path nid map.
     */
    ConcurrentHashMap<Integer, Integer> cacheStampSequenceToPathNidMap = new ConcurrentHashMap<>();

    /**
     * Persistent as a result of reading and writing the
     * cacheStampObjectToStampSequenceMap.
     */
    private final ConcurrentHashMap<Integer, Stamp> cacheStampSequenceToStampObjectMap;

    public HikariDataSource ds;
    private DataStore dataStore = null;
    //private ExtendedStoreData<Integer, Stamp> sequenceToStamp;
    //private ExtendedStoreData<Integer, UncommittedStamp> sequenceToUncommittedStamp;

    //~--- constructors --------------------------------------------------------

    /*
    * Instantiates a new stamp provider.  For HK2 only
    *
    * @throws IOException Signals that an I/O exception has occurred.
     */
    private PostgresStampProvider()
        throws IOException {
        ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
        Path dataStorePath = configurationService.getDataStoreFolderPath();

        // :!!!: setup sequenceToStamp
        // :!!!: setup sequenceToUncommittedStamp
        // :!!!: initialize `ds`
        this.cacheStampSequenceToStampObjectMap = new ConcurrentHashMap<>();

        // // -- if (Get.dataStore().implementsExtendedStoreAPI()) --
        dataStore = Get.dataStore();
        //sequenceToStamp = dataStore.<Integer, byte[], Stamp>getStore(
        //    "stampProviderSequenceToStamp",
        //    (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
        //    (toDeserialize) -> {
        //        try {
        //            return toDeserialize == null ? null : new Stamp(DataToBytesUtils.getDataInput(toDeserialize));
        //        }
        //    });

        //sequenceToUncommittedStamp = dataStore.<Integer, byte[], UncommittedStamp>getStore(
        //    "stampProviderSequenceToUncommittedStamp",
        //    (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
        //    (toDeserialize) -> {
        //        try {
        //            return toDeserialize == null ? null : new UncommittedStamp(DataToBytesUtils.getDataInput(toDeserialize));
        //        }
        //    });
    }

    protected byte[] convertStampToBytes(Stamp stamp) {
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

    protected byte[] convertUncommittedStampToBytes(UncommittedStamp ustamp) {
        ByteArrayDataBuffer srcData = new ByteArrayDataBuffer();

        srcData.putInt(ustamp.authorNid);
        srcData.putInt(ustamp.moduleNid);
        srcData.putInt(ustamp.pathNid);

        int length = srcData.getPosition();
        byte[] destBytes = new byte[length];
        System.arraycopy(srcData.getData(), 0, destBytes, 0, length);
        return destBytes;
    }

    protected Stamp convertBytesToStamp(byte[] bytesIn) {
        ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(bytesIn);
        String statusName = byteBuffer.getUTF();
        long time = byteBuffer.getLong();
        int authorNid = byteBuffer.getInt();
        int moduleNid = byteBuffer.getInt();
        int pathNid = byteBuffer.getInt();

        Status status = Status.valueOf(statusName);

        Stamp stamp = new Stamp(status, time, authorNid, moduleNid, pathNid);

        return stamp;
    }

    protected UncommittedStamp convertBytesToUncommittedStamp(byte[] bytesIn) {
        ByteArrayDataBuffer byteBuffer = new ByteArrayDataBuffer(bytesIn);
        int authorNid = byteBuffer.getInt();
        int moduleNid = byteBuffer.getInt();
        int pathNid = byteBuffer.getInt();

        UncommittedStamp ustamp = new UncommittedStamp(null, authorNid, moduleNid, pathNid);

        return ustamp;
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Set pending stamps for commit.
     *
     * @param pendingStamps the pending stamps
     */
    @Override // :TODO:
    synchronized public void addPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps) {
        for (Map.Entry<UncommittedStamp, Integer> entry : pendingStamps.entrySet()) {
            cacheUncommittedStampToStampSequenceMap.get().remove(entry.getKey());
        }
    }

    /**
     * Adds the stamp.
     *
     * @param stamp the stamp
     * @param stampSequence the stamp sequence
     */
    @Override // :TODO:
    public void addStamp(Stamp stamp, int stampSequence) {
        this.cacheStampObjectToStampSequenceMap.put(stamp, stampSequence);
        this.cacheStampSequenceToStampObjectMap.put(stampSequence, stamp);
        storeStampCreate(stampSequence, stamp);
        LOG.trace("Added stamp {}", stamp);
    }

    /**
     * Cancel.
     *
     * @param authorNid the author nid
     * @return the task
     */
    @Override // :TODO:
    public synchronized Task<Void> cancel(int authorNid) {
        Map<UncommittedStamp, Integer> map = cacheUncommittedStampToStampSequenceMap.get();

        map.forEach(
            (uncommittedStamp, stampSequence) -> {
                // for each uncommitted stamp matching the author, remove the uncommitted stamp
                // and replace with a canceled stamp.
                if (uncommittedStamp.authorNid == authorNid) {
                    final Stamp stamp = new Stamp(
                        uncommittedStamp.status,
                        Long.MIN_VALUE,
                        uncommittedStamp.authorNid,
                        uncommittedStamp.moduleNid,
                        uncommittedStamp.pathNid);

                    addStamp(stamp, stampSequence);
                    map.remove(uncommittedStamp);
                }
            });

        // TODO make asynchronous with a actual task.
        final Task<Void> task = new TimedTask<Void>() {
            @Override // :TODO:
            protected Void call()
                throws Exception {
                Get.activeTasks()
                    .remove(this);
                return null;
            }
        };

        Get.activeTasks()
            .add(task);
        Get.workExecutors()
            .getExecutor()
            .execute(task);
        return task;
    }

    /**
     * Describe stamp sequence.
     *
     * @param stampSequence the stamp sequence
     * @return the string
     */
    @Override // :TODO:
    public String describeStampSequence(int stampSequence) {
        if (stampSequence == -1) {
            return "{Stamp≤CANCELED≥}";
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("{Stamp≤");
        sb.append(stampSequence);
        sb.append("::");

        try {
            final Status status = getStatusForStamp(stampSequence);

            sb.append(status);
            sb.append(" ");

            final long time = getTimeForStamp(stampSequence);

            if (time == Long.MAX_VALUE) {
                sb.append("UNCOMMITTED:");
            } else if (time == Long.MIN_VALUE) {
                sb.append("CANCELED:  ");
            } else {
                sb.append(Instant.ofEpochMilli(time));
            }

            sb.append(" a:");
            sb.append(Get.conceptDescriptionText(getAuthorNidForStamp(stampSequence)));
            sb.append(" m:");
            sb.append(Get.conceptDescriptionText(getModuleNidForStamp(stampSequence)));
            sb.append(" p: ");
            sb.append(Get.conceptDescriptionText(getPathNidForStamp(stampSequence)));
        } catch (Exception e) {
            sb.append(e.getMessage());
        }

        sb.append("≥}");
        return sb.toString();
    }

    @Override // :TODO:
    public String describeStampSequenceForTooltip(int stampSequence, ManifoldCoordinate manifoldCoordinate) {
        if (stampSequence == -1) {
            return "CANCELED";
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("S: ");

        final Status status = getStatusForStamp(stampSequence);

        sb.append(status)
            .append("\nT: ");

        final long time = getTimeForStamp(stampSequence);

        // Cannot change to case statement, since case supports int not long...
        if (time == Long.MAX_VALUE) {
            sb.append("UNCOMMITTED");
        } else if (time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            ZonedDateTime stampTime = Instant.ofEpochMilli(time)
                .atZone(ZoneOffset.UTC);

            sb.append(stampTime.format(FORMATTER));
        }

        LatestVersion<DescriptionVersion> authorDescription = manifoldCoordinate.getPreferredDescription(
            getAuthorNidForStamp(stampSequence));

        if (authorDescription.isPresent()) {
            sb.append("\nA: ")
                .append(authorDescription.get()
                    .getText());
        } else {
            sb.append("\nA: unretrievable");
        }

        LatestVersion<DescriptionVersion> moduleDescription = manifoldCoordinate.getPreferredDescription(
            getModuleNidForStamp(stampSequence));

        if (moduleDescription.isPresent()) {
            sb.append("\nM: ")
                .append(moduleDescription.get()
                    .getText());
        } else {
            sb.append("\nM: unretrievable");
        }

        LatestVersion<DescriptionVersion> pathDescription = manifoldCoordinate.getPreferredDescription(
            getPathNidForStamp(stampSequence));

        if (pathDescription.isPresent()) {
            sb.append("\nP: ")
                .append(pathDescription.get()
                    .getText());
        } else {
            sb.append("\nP: unretrievable");
        }

        Optional<String> optionalComment = Get.commitService()
            .getComment(stampSequence);

        if (optionalComment.isPresent()) {
            sb.append("\n\ncomment: ");
            sb.append(optionalComment.get());
        }

        return sb.toString();
    }

    /**
     * Stamp sequences equal except author and time.
     *
     * @param stampSequence1 the stamp sequence 1
     * @param stampSequence2 the stamp sequence 2
     * @return true, if successful
     */
    @Override // :TODO:
    public boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2) {
        if (getModuleNidForStamp(stampSequence1) != getModuleNidForStamp(stampSequence2)) {
            return false;
        }

        if (getPathNidForStamp(stampSequence1) != getPathNidForStamp(stampSequence2)) {
            return false;
        }

        return getStatusForStamp(stampSequence1) == getStatusForStamp(stampSequence2);
    }

    @Override // :TODO:
    public Future<?> sync() {
        return Get.executor().submit(() -> {
            writeData();
            return null;
        });
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LOG.info("Starting PostgresStampProvider startMe() PostConstruct");

        String isaacDbUrl = System.getProperty("ISAAC_PSQL_URL", "jdbc:postgresql://localhost/isaac_db");
        String isaacUsername = System.getProperty("ISAAC_PSQL_UNAME", "isaac_user");
        String isaacUserpwd = System.getProperty("ISAAC_PSQL_UPWD", "isaac_pwd");

        HikariConfig config = new HikariConfig();
        // ::NYI: pass in setJdbcUrl as parameter instead of being hardcoded.
        config.setJdbcUrl(isaacDbUrl);
        config.setUsername(isaacUsername);
        config.setPassword(isaacUserpwd);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String sqlCreate
                    = "CREATE TABLE IF NOT EXISTS stamp_committed_table ("
                    + "stamp_committed_sequence INTEGER, "
                    + "stamp_committed_data     bytea, "
                    + "UNIQUE (stamp_committed_data), "
                    + "PRIMARY KEY (stamp_committed_sequence) ); ";
                logSqlString(sqlCreate);
                stmt.execute(sqlCreate);
            }

            try (Statement stmt = conn.createStatement()) {
                String sqlCreate
                    = "CREATE TABLE IF NOT EXISTS stamp_uncommitted_table ("
                    + "stamp_uncommitted_sequence INTEGER, "
                    + "stamp_uncommitted_data     bytea, "
                    + "UNIQUE (stamp_uncommitted_data), "
                    + "PRIMARY KEY (stamp_uncommitted_sequence) ); ";
                logSqlString(sqlCreate);
                stmt.execute(sqlCreate);
            }

            try (Statement stmt = conn.createStatement()) {
                String sqlCreate = "CREATE SEQUENCE IF NOT EXISTS stamp_next_sequence "
                    + "AS INTEGER  MINVALUE 1 START WITH 1; ";
                logSqlString(sqlCreate);
                boolean result = stmt.execute(sqlCreate);
                if (result) {
                    String sqlSetVal = "SELECT setval('stamp_next_sequence', 1, false); ";
                    logSqlString(sqlSetVal);
                    stmt.execute(sqlSetVal);
                }

            }

        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        } finally {
            LOG.info("Connection closed.");
        }

        // :REVIEWED_TO_HERE:
        LOG.debug("Looking for data store based stamp data");
        // :CACHE:
        this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
        cacheUncommittedStampToStampSequenceMap.get().clear();
        this.cacheStampObjectToStampSequenceMap.clear();
        this.cacheStampSequenceToPathNidMap.clear();
        this.cacheStampSequenceToStampObjectMap.clear();

        //We put the nextStampSequence here in the MAX_VALUE slot.
        OptionalInt oi = getStampNextSequenceCurrval();
        if (oi.isPresent()) {
            this.nextStampSequence.set(oi.getAsInt());

            // load cacheStampObjectToStampSequenceMap
            // load cacheStampSequenceToStampObjectMap
            storeStampReadAll();

            //load cacheUncommittedStampToStampSequenceMap 
            storeUncommittedStampReadAll();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping PostgresStampProvider pre-destroy. ");
        if (this.ds != null) {
            this.ds.close();
        }

        // :REVIEWED_TO_HERE:
        writeData();

        this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);

        // :CACHE:
        cacheUncommittedStampToStampSequenceMap.get().clear();
        this.cacheStampObjectToStampSequenceMap.clear();
        this.cacheStampSequenceToPathNidMap.clear();
        this.cacheStampSequenceToStampObjectMap.clear();

        // :NYI: close management of not-yet 
    }

    private void writeData() throws RuntimeException {
        //Just write the unwritten bits to the data store
        //Should be there already, but make sure we have the latest
        putStampNextSequenceSetval(nextStampSequence.get());
        //stamps are written as they are created

        //Write the uncommitted data
        storeUncommittedStampDeleteAll();
        for (Entry<UncommittedStamp, Integer> uc : cacheUncommittedStampToStampSequenceMap.get().entrySet()) {
            storeUncommittedStampCreate(uc.getValue(), uc.getKey());
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the activated stamp sequence.
     *
     * @param stampSequence the stamp sequence
     * @return the activated stamp sequence
     */
    @Override // :TODO:
    public int getActivatedStampSequence(int stampSequence) {
        return getStampSequence(
            Status.ACTIVE,
            getTimeForStamp(stampSequence),
            getAuthorNidForStamp(stampSequence),
            getModuleNidForStamp(stampSequence),
            getPathNidForStamp(stampSequence));
    }

    /**
     * Gets the author nid for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the author nid for stamp
     */
    @Override // :TODO:
    public int getAuthorNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.USER.getNid();
        }

        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToStampObjectMap.get(stampSequence)
                .getAuthorNid();
        }

        for (Map.Entry<UncommittedStamp, Integer> entry : cacheUncommittedStampToStampSequenceMap.get()
            .entrySet()) {
            if (entry.getValue() == stampSequence) {
                return entry.getKey().authorNid;
            }
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    @Override // :TODO:
    public Optional<UUID> getDataStoreId() {
        return dataStore.getDataStoreId();
    }

    /**
     * Gets the database folder.
     *
     * @return the database folder
     */
    @Override // :TODO:
    public Path getDataStorePath() {
        return dataStore.getDataStorePath();
    }

    /**
     * Gets the database validity status.
     *
     * @return the database validity status
     */
    @Override // :TODO:
    public DataStoreStartState getDataStoreStartState() {
        return dataStore.getDataStoreStartState();
    }

    /**
     * Gets the module nid for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the module nid for stamp
     */
    @Override // :TODO:
    public int getModuleNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.UNSPECIFIED_MODULE.getNid();
        }

        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToStampObjectMap.get(stampSequence)
                .getModuleNid();
        }

        for (Map.Entry<UncommittedStamp, Integer> entry : cacheUncommittedStampToStampSequenceMap.get()
            .entrySet()) {
            if (entry.getValue() == stampSequence) {
                return entry.getKey().moduleNid;
            }
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    /**
     * Checks if not canceled.
     *
     * @param stamp the stamp
     * @return true, if not canceled
     */
    @Override // :TODO:
    public boolean isNotCanceled(int stamp) {
        if (stamp < 0) {
            return false;
        }

        return getTimeForStamp(stamp) != Long.MIN_VALUE;
    }

    /**
     * Gets the path nid for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the path nid for stamp
     */
    @Override // :TODO:
    public int getPathNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.DEVELOPMENT_PATH.getNid();
        }

        if (this.cacheStampSequenceToPathNidMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToPathNidMap.get(stampSequence);
        }

        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            this.cacheStampSequenceToPathNidMap.put(stampSequence, this.cacheStampSequenceToStampObjectMap.get(stampSequence)
                .getPathNid());
            return this.cacheStampSequenceToPathNidMap.get(stampSequence);
        }

        for (Map.Entry<UncommittedStamp, Integer> entry : cacheUncommittedStampToStampSequenceMap.get()
            .entrySet()) {
            if (entry.getValue() == stampSequence) {
                return entry.getKey().pathNid;
            }
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    /**
     * Gets the pending stamps for commit.
     *
     * @return the pending stamps for commit
     */
    @Override // :TODO:
    public ConcurrentHashMap<UncommittedStamp, Integer> getPendingStampsForCommit() {
        ConcurrentHashMap<UncommittedStamp, Integer> pendingStampsForCommit
            = cacheUncommittedStampToStampSequenceMap.get();

        while (!cacheUncommittedStampToStampSequenceMap.compareAndSet(
            pendingStampsForCommit,
            new ConcurrentHashMap<>())) {
            pendingStampsForCommit = cacheUncommittedStampToStampSequenceMap.get();
        }

        return pendingStampsForCommit;
    }

    /**
     * Gets the retired stamp sequence.
     *
     * @param stampSequence the stamp sequence
     * @return the retired stamp sequence
     */
    @Override // :TODO:
    public int getRetiredStampSequence(int stampSequence) {
        return getStampSequence(
            Status.INACTIVE,
            getTimeForStamp(stampSequence),
            getAuthorNidForStamp(stampSequence),
            getModuleNidForStamp(stampSequence),
            getPathNidForStamp(stampSequence));
    }

    /**
     * Gets the stamp sequence.
     *
     * @param status the status
     * @param time the time
     * @param authorSequence the author nid
     * @param moduleSequence the module nid
     * @param pathSequence the path nid
     * @return the stamp sequence
     */
    @Override // :TODO:
    public int getStampSequence(Status status, long time, int authorSequence, int moduleSequence, int pathSequence) {
        if (status == Status.PRIMORDIAL) {
            throw new UnsupportedOperationException(status + " is not an assignable value.");
        }
        final Stamp stampKey = new Stamp(status, time, authorSequence, moduleSequence, pathSequence);

        if (time == Long.MAX_VALUE) {
            final UncommittedStamp uncommittedStamp = new UncommittedStamp(status, authorSequence, moduleSequence, pathSequence);
            final Integer temp = cacheUncommittedStampToStampSequenceMap.get()
                .get(uncommittedStamp);

            if (temp != null) {
                return temp;
            } else {
                this.stampLock.lock();

                try {
                    if (cacheUncommittedStampToStampSequenceMap.get()
                        .containsKey(uncommittedStamp)) {
                        return cacheUncommittedStampToStampSequenceMap.get()
                            .get(uncommittedStamp);
                    }

                    final int stampSequence = this.nextStampSequence.getAndIncrement();

                    LOG.trace("Putting {}, {} into uncommitted stamp to sequence map", uncommittedStamp, stampSequence);
                    cacheUncommittedStampToStampSequenceMap.get()
                        .put(uncommittedStamp, stampSequence);
                    this.cacheStampSequenceToStampObjectMap.put(stampSequence, stampKey);
                    putStampNextSequenceSetval(nextStampSequence.get());
                    return stampSequence;
                } finally {
                    this.stampLock.unlock();
                }
            }
        }

        if (!this.cacheStampObjectToStampSequenceMap.containsKey(stampKey)) {
            // maybe have a few available in an atomic queue, and put back
            // if not used? Maybe in a thread-local?
            // Have different sequences, and have the increments be equal to the
            // number of sequences?
            this.stampLock.lock();

            try {
                if (!this.cacheStampObjectToStampSequenceMap.containsKey(stampKey)) {
                    OptionalInt stampValue = OptionalInt.of(this.nextStampSequence.getAndIncrement());

                    this.cacheStampSequenceToStampObjectMap.put(stampValue.getAsInt(), stampKey);
                    this.cacheStampObjectToStampSequenceMap.put(stampKey, stampValue.getAsInt());
                    putStampNextSequenceSetval(nextStampSequence.get());
                    storeStampCreate(stampValue.getAsInt(), stampKey);
                }
            } finally {
                this.stampLock.unlock();
            }
        }

        return this.cacheStampObjectToStampSequenceMap.get(stampKey);
    }

    /**
     * Gets the stamp sequences.
     *
     * @return the stamp sequences
     */
    @Override // :TODO:
    public IntStream getStampSequences() {
        return IntStream.rangeClosed(FIRST_STAMP_SEQUENCE, this.nextStampSequence.get())
            .filter((stampSequence) -> this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence));
    }

    /**
     * Gets the status for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the status for stamp
     */
    @Override // :TODO:
    public Status getStatusForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return Status.CANCELED;
        }

        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToStampObjectMap.get(stampSequence)
                .getStatus();
        }

        for (Map.Entry<UncommittedStamp, Integer> entry : cacheUncommittedStampToStampSequenceMap.get()
            .entrySet()) {
            if (entry.getValue() == stampSequence) {
                return entry.getKey().status;
            }
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    String sqlReadStampSequenceCommitted() {
        return "SELECT stamp_committed_sequence FROM stamp_committed_table "
            + "WHERE stamp_committed_data = ?;";
    }

    String sqlReadStampSequenceUncommitted() {
        return "SELECT stamp_uncommitted_sequence FROM stamp_uncommitted_table "
            + "WHERE stamp_uncommitted_data = ?;";
    }

    /**
     * Gets the time for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the time for stamp
     */
    @Override // :TODO:!!!:
    public long getTimeForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return Long.MIN_VALUE;
        }
        // :REVIEWED_TO_HERE:

        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToStampObjectMap.get(stampSequence)
                .getTime();
        }

        if (cacheUncommittedStampToStampSequenceMap.get()
            .containsValue(stampSequence)) {
            return Long.MAX_VALUE;
        }

        throw new NoSuchElementException(
            "No stampSequence found: " + stampSequence + " map size: " + this.cacheStampObjectToStampSequenceMap.size() + " inverse map size: "
            + this.cacheStampSequenceToStampObjectMap.size());
    }

    String sqlReadStampDataCommitted() {
        return "SELECT stamp_committed_data FROM stamp_committed_table "
            + "WHERE stamp_committed_sequence = ?;";
    }

    String sqlReadStampDataUncommitted() {
        return "SELECT stamp_uncommitted_data FROM stamp_uncommitted_table "
            + "WHERE stamp_uncommitted_sequence = ?;";
    }

    private OptionalInt getStampNextSequenceNextval() {
        String sql = "SELECT nextval('stamp_next_sequence'); ";
        logSqlString(sql);
        try (Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                int nextval = resultSet.getInt(1);
                return OptionalInt.of(nextval);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return OptionalInt.empty();
    }

    private OptionalInt getStampNextSequenceCurrval() {
        String sql = "SELECT currval('stamp_next_sequence'); ";
        logSqlString(sql);
        try (Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                int currval = resultSet.getInt(1);
                return OptionalInt.of(currval);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
        return OptionalInt.empty();
    }

    private void putStampNextSequenceSetval(int nextValue) {
        // is_called false: currval will return "nextValue" before advancing
        String sql = "SELECT setval('stamp_next_sequence', ?, false); ";
        try (Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nextValue);
            logSqlStmt(stmt);
            stmt.execute();
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }

    }

    @Override // :TODO::!!!:
    public Stamp getStamp(int stampSequence) {
        if (stampSequence < 0) {
            return new Stamp(
                Status.CANCELED, // status
                Long.MIN_VALUE, // time
                TermAux.USER.getNid(), // authorNid
                TermAux.UNSPECIFIED_MODULE.getNid(), // moduleNid 
                TermAux.DEVELOPMENT_PATH.getNid());  // pathNid
        }

        // :REVIEWED_TO_HERE:
        // stampSequence int --> Stamp
        if (this.cacheStampSequenceToStampObjectMap.containsKey(stampSequence)) {
            return this.cacheStampSequenceToStampObjectMap.get(stampSequence);
        }

        for (Map.Entry<UncommittedStamp, Integer> entry : cacheUncommittedStampToStampSequenceMap.get()
            .entrySet()) {
            if (entry.getValue() == stampSequence) {
                UncommittedStamp us = entry.getKey();
                return new Stamp(us.status, Long.MAX_VALUE, us.authorNid, us.moduleNid, us.pathNid);
            }
        }

        throw new NoSuchElementException(
            "No stampSequence found: " + stampSequence + " map size: " + this.cacheStampObjectToStampSequenceMap.size() + " inverse map size: "
            + this.cacheStampSequenceToStampObjectMap.size());
    }

    /**
     * Checks if uncommitted.
     *
     * @param stampSequence the stamp sequence
     * @return true, if uncommitted
     */
    @Override
    public boolean isUncommitted(int stampSequence) {
        return getTimeForStamp(stampSequence) == Long.MAX_VALUE;
    }

    //~--- persistant datastore ------------------------------------------------
    private void storeStampReadAll() {
        String sql = "SELECT (stamp_committed_sequence, stamp_committed_data) "
            + "FROM stamp_committed_table; ";

        try (
            Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {

            boolean originalAutoCommit = conn.getAutoCommit();
            if (originalAutoCommit) {
                conn.setAutoCommit(false); // start temp transaction
            }

            stmt.setFetchSize(512);

            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    int sequence = resultSet.getInt(1);
                    byte[] bytea = resultSet.getBytes(2);
                    Stamp stamp = convertBytesToStamp(bytea);

                    this.cacheStampObjectToStampSequenceMap.put(stamp, sequence);
                    this.cacheStampSequenceToStampObjectMap.put(sequence, stamp);
                }
            }

            if (originalAutoCommit) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void storeStampCreate(int stampSequence, Stamp stampObject) {
        String sql = "INSERT INTO stamp_committed_table "
            + "(stamp_committed_sequence, stamp_committed_data) "
            + "VALUES (?,?) "
            + "ON CONFLICT DO NOTHING; ";

        try (
            Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stampSequence); // stamp_uncommitted_sequence
            byte[] bytes = convertStampToBytes(stampObject);
            stmt.setBytes(2, bytes);   // stamp_uncommitted_data
            logSqlBytea(stmt, bytes);

            stmt.execute();

        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void storeUncommittedStampDeleteAll() {
        String sql = "DELETE FROM stamp_uncommitted_table; ";

        try (
            Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void storeUncommittedStampReadAll() {
        String sql = "SELECT (stamp_uncommitted_sequence, stamp_uncommitted_data) "
            + "FROM stamp_uncommitted_table; ";

        try (
            Connection conn = this.ds.getConnection();
            Statement stmt = conn.createStatement()) {

            boolean originalAutoCommit = conn.getAutoCommit();
            if (originalAutoCommit) {
                conn.setAutoCommit(false); // start temp transaction
            }

            stmt.setFetchSize(512);

            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    int sequence = resultSet.getInt(1);
                    byte[] bytea = resultSet.getBytes(2);
                    UncommittedStamp ustamp = convertBytesToUncommittedStamp(bytea);
                    cacheUncommittedStampToStampSequenceMap.get().put(ustamp, sequence);
                }
            }

            if (originalAutoCommit) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }

    }

    private void storeUncommittedStampCreate(int stampSequence, UncommittedStamp stampObject) {
        String sql = "INSERT INTO stamp_uncommitted_table "
            + "(stamp_uncommitted_sequence, stamp_uncommitted_data) "
            + "VALUES (?,?) "
            + "ON CONFLICT DO NOTHING; ";

        try (
            Connection conn = this.ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stampSequence); // stamp_uncommitted_sequence
            byte[] bytes = convertUncommittedStampToBytes(stampObject);
            stmt.setBytes(2, bytes);   // stamp_uncommitted_data
            logSqlBytea(stmt, bytes);

            stmt.execute();

        } catch (SQLException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

}
