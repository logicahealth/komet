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


package sh.isaac.provider.stamp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.collections.impl.factory.primitive.LongSets;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.concurrent.Task;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.commit.Stamp;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.commit.UncommittedStamp;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.datastore.DataStore;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.datastore.extendedStore.ExtendedStoreStandAlone;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.DataToBytesUtils;
import sh.isaac.provider.commit.CancelUncommittedStamps;
import sh.isaac.provider.commit.TransactionImpl;

/**
 * Created by kec on 1/2/16.
 */
@Service(name = "Stamp Provider")
@RunLevel(value = LookupService.SL_L2)
public class StampProvider
        implements StampService, CancelUncommittedStamps {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG);

    private static final Logger LOG = LogManager.getLogger();

    private final ReentrantLock stampLock = new ReentrantLock();

    private static final String DEFAULT_STAMP_MANAGER_NAME = "stamp-manager";

    /**
     * The next stamp sequence.
     */
    private final AtomicInteger nextStampSequence = new AtomicInteger(FIRST_STAMP_SEQUENCE);

    /**
     * map of stamp sequences to a STAMP object. When a STAMP is cancelled, the time is
     * set to Long.MIN_VALUE, therefore there can be more than one stamp sequence for canceled
     * stamps. The stamp map supports an int[] so that when more than one canceled stamp with a
     * particular module, author, & path will be properly represented.
     * 
     * Not stored in this form
     */
    private transient final ConcurrentHashMap<Stamp, int[]> stampMap = new ConcurrentHashMap<>();

    /**
     * The stamp sequence path nid map - performance cache only
     */
    private transient final ConcurrentHashMap<Integer, Integer> stampSequence_PathNid_Map = new ConcurrentHashMap<>();
    
    /**
     * Inverse of sequenceToUncommittedStamp - does not need to be serialized
     */
    private transient final ConcurrentHashMap<UncommittedStamp, Integer> uncommittedStampIntegerConcurrentHashMap = new ConcurrentHashMap<>();

    private DataStore dataStore = null;
    private ExtendedStoreData<Integer, Stamp> inverseStampMap = null;
    private ExtendedStoreData<Integer, UncommittedStamp> sequenceToUncommittedStamp = null;
    
    private static final String INVERSE_STAMP_MAP_STORAGE_NAME = "inverseStampMap";
    private static final String SEQUENCE_TO_UNCOMITTED_STAMP_STORAGE_NAME = "sequenceToUncommittedStamp";

    /*
     * Instantiates a new stamp provider.  For HK2 only
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private StampProvider() throws IOException {
    }

    /**
     * Adds the stamp.
     *
     * @param stamp         the stamp
     * @param stampSequence the stamp sequence
     */
    @Override
    public void addStamp(Stamp stamp, int stampSequence) {
        this.stampMap.merge(stamp, new int[]{stampSequence}, this::mergeSequences);
        this.inverseStampMap.put(stampSequence, stamp);
         LOG.trace("Added stamp {}", stamp);
    }

    private int[] mergeSequences(int[] ints, int[] ints2) {
        SequenceSet<?> sequences = SequenceSet.of(ints);
        for (int sequence: ints2) {
            sequences.add(sequence);
        }
        return sequences.asArray();
    }

    /**
     * Cancel.
     *
     * @param transaction the transaction to cancel.
     * @return the task
     */
    @Override
    public synchronized TimedTask<Void> cancel(Transaction transaction) {
        CancelTask task = new CancelTask(transaction);
        Get.workExecutors().getExecutor().execute(task);
        return task;
    }

    @Override
    public synchronized TimedTask<Void> commit(Transaction transaction, long commitTime) {
        CommitTask task = new CommitTask(transaction, commitTime);
        Get.workExecutors().getForkJoinPoolExecutor().execute(task);
        return task;
    }

    private class CancelTask extends TimedTaskWithProgressTracker<Void> {
        final Transaction transaction;

        public CancelTask(Transaction transaction) {
            updateTitle(getTitleString() + transaction.getTransactionId());
            this.transaction = transaction;
            Get.activeTasks().add(this);
            addToTotalWork(uncommittedStampIntegerConcurrentHashMap.size());
        }

        protected long getTime() {
            return Long.MIN_VALUE;
        }

        protected Status getStatus(Status uncommittedStatus) {
            return Status.CANCELED;
        }

        protected String getTitleString() {
            return "Canceling transaction: ";
        }

        @Override
        protected Void call() throws Exception {
            try {
                uncommittedStampIntegerConcurrentHashMap.forEach(
                        (uncommittedStamp, stampSequence) -> {
                            try {
                                processTransaction(uncommittedStamp, stampSequence, (TransactionImpl) transaction);
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                                throw e;
                            }
                        });
                return null;
            } catch (Throwable t) {
                LOG.error(t.getLocalizedMessage(), t);
                throw t;
            } finally {
                Get.activeTasks().remove(this);
            }
        }

        private void processTransaction(UncommittedStamp uncommittedStamp, Integer stampSequence, TransactionImpl transaction) {
            // for each uncommitted stamp matching the transaction, remove the uncommitted stamp
            // and replace with a stamp with a proper time indicating canceled...
            if (transaction.getTransactionId().equals(uncommittedStamp.getTransactionId())) {
                long time = getTime();
                if (time != Long.MIN_VALUE) { // Long.MIN_VALUE ==  canceled...
                    if (uncommittedStamp.getTime() != Long.MAX_VALUE) {
                        time = uncommittedStamp.getTime();
                    }
                }
                final Stamp stamp = new Stamp(
                        getStatus(uncommittedStamp.getStatus()),
                        time,
                        uncommittedStamp.getAuthorNid(),
                        uncommittedStamp.getModuleNid(),
                        uncommittedStamp.getPathNid());
                if (stamp.getStatus() == Status.CANCELED) {
                    LOG.info("Canceling Filter <" + stampSequence + ">: " + stamp + " " + transaction);
                }

                addStamp(stamp, stampSequence);
                uncommittedStampIntegerConcurrentHashMap.remove(uncommittedStamp);
                sequenceToUncommittedStamp.remove(stampSequence);
            }
            completedUnitOfWork();
            for (TransactionImpl childTransaction : transaction.getChildren()) {
                processTransaction(uncommittedStamp, stampSequence, childTransaction);
            }
        }
    }


    private class CommitTask extends CancelTask {
        final long commitTime;

        public CommitTask(Transaction transaction, long commitTime) {
            super(transaction);
            this.commitTime = commitTime;
        }

        protected long getTime() {
            return this.commitTime;
        }
        protected Status getStatus(Status uncommittedStatus) {
            return uncommittedStatus;
        }

        protected String getTitleString() {
            return "Committing transaction: ";
        }
    }

    /**
     * Describe stamp sequence.
     *
     * @param stampSequence the stamp sequence
     * @return the string
     */
    @Override
    public String describeStampSequence(int stampSequence) {
        if (stampSequence == -1) {
            return "{Stamp≤CANCELED≥}";
        }

        if (stampSequence == Integer.MAX_VALUE) {
            return "{Stamp≤UNCOMMITTED Observable Version≥}";
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

            if (sequenceToUncommittedStamp.containsKey(stampSequence)) {
                sb.append("UNCOMMITTED-");
                if (time != Long.MAX_VALUE) {
                   sb.append(Instant.ofEpochMilli(time));
                }
                sb.append('_');
                sb.append(sequenceToUncommittedStamp.get(stampSequence).getTransactionId());
                sb.append(":");
            } else if (time == Long.MIN_VALUE) {
                sb.append("CANCELED:");
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

    @Override
    public String describeStampSequenceForTooltip(int stampSequence, ManifoldCoordinate manifoldCoordinate) {
        if (stampSequence == -1) {
            return "CANCELED";
        }
        if (stampSequence == Integer.MAX_VALUE) {
            return "Uncommitted from observable with no stamped version";
        }

        final StringBuilder sb = new StringBuilder();

        sb.append("S: ");

        final Status status = getStatusForStamp(stampSequence);

        sb.append(status)
                .append("\nT: ");

        final long time = getTimeForStamp(stampSequence);

        // Cannot change to case statement, since case supports int not long...
        if (sequenceToUncommittedStamp.containsKey(stampSequence)) {
            sb.append("UNCOMMITTED-");
            if (time != Long.MAX_VALUE) {
                ZonedDateTime stampTime = Instant.ofEpochMilli(time)
                        .atZone(ZoneOffset.UTC);
                sb.append(stampTime.format(FORMATTER));
             }
            sb.append(sequenceToUncommittedStamp.get(stampSequence).getTransactionId().toString());
            sb.append("");
        } else if (time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            ZonedDateTime stampTime = Instant.ofEpochMilli(time)
                    .atZone(ZoneOffset.systemDefault());
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
    @Override
    public boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2) {
        if (getModuleNidForStamp(stampSequence1) != getModuleNidForStamp(stampSequence2)) {
            return false;
        }

        if (getPathNidForStamp(stampSequence1) != getPathNidForStamp(stampSequence2)) {
            return false;
        }

        return getStatusForStamp(stampSequence1) == getStatusForStamp(stampSequence2);
    }

    @Override
    public Future<?> sync() {
        return Get.executor().submit(() -> {
            writeData();
            return null;
        });
    }

    /**
     * Start me.
     * NOTE: cannot get descriptions of concepts at this run level, the stamp provider must start before the
     * concept service and assemblage service can start.
     */
    @PostConstruct
    private void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Stamp provider");
        Get.executor().execute(progressTask);
        LOG.info("Starting StampProvider post-construct");
        try {
            this.dataStore = Get.dataStore();
            if (Get.dataStore().implementsExtendedStoreAPI()) {
                LOG.info("DataStore implements extended API, will be used for Stamp Provider");
                this.inverseStampMap = ((ExtendedStore)dataStore).<Integer, byte[], Stamp>getStore(INVERSE_STAMP_MAP_STORAGE_NAME,
                        (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
                        (toDeserialize) -> {
                            try {
                                return toDeserialize == null ? null : new Stamp(DataToBytesUtils.getDataInput(toDeserialize));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                this.sequenceToUncommittedStamp = ((ExtendedStore)dataStore).<Integer, byte[], UncommittedStamp>getStore(SEQUENCE_TO_UNCOMITTED_STAMP_STORAGE_NAME,
                        (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
                        (toDeserialize) -> {
                            try {
                                return toDeserialize == null ? null : new UncommittedStamp(DataToBytesUtils.getDataInput(toDeserialize));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } else {
                LOG.info("DataStore does not implement extended API, local file store will be used for Stamp Provider");
                Path dbFolderPath = Get.configurationService().getDataStoreFolderPath().resolve(DEFAULT_STAMP_MANAGER_NAME);
                Files.createDirectories(dbFolderPath);

                this.inverseStampMap = new ExtendedStoreStandAlone<Integer, byte[], Stamp>(new File(dbFolderPath.toFile(), INVERSE_STAMP_MAP_STORAGE_NAME),
                        (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
                        (toDeserialize) -> {
                            try {
                                return toDeserialize == null ? null : new Stamp(DataToBytesUtils.getDataInput(toDeserialize));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                this.sequenceToUncommittedStamp = new ExtendedStoreStandAlone<Integer, byte[], UncommittedStamp>(
                        new File(dbFolderPath.toFile(), SEQUENCE_TO_UNCOMITTED_STAMP_STORAGE_NAME),
                        (toSerialize) -> toSerialize == null ? null : DataToBytesUtils.getBytes(toSerialize::write),
                        (toDeserialize) -> {
                            try {
                                return toDeserialize == null ? null : new UncommittedStamp(DataToBytesUtils.getDataInput(toDeserialize));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            LOG.debug("Looking for data store based stamp data");
            this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
            this.stampMap.clear();
            this.stampSequence_PathNid_Map.clear();
            this.uncommittedStampIntegerConcurrentHashMap.clear();
            
            inverseStampMap.getStream(false).forEach(stampPair ->
            {
                this.stampMap.merge(stampPair.getValue(), new int[]{stampPair.getKey()}, this::mergeSequences);
                if (stampPair.getKey() >= nextStampSequence.get()) {
                    nextStampSequence.set(stampPair.getKey() + 1);
                }
            });

            sequenceToUncommittedStamp.getStream(true).forEach(stampPair ->
            {
                this.uncommittedStampIntegerConcurrentHashMap.put(stampPair.getValue(), stampPair.getKey());
            });

        }
        catch (IOException e1) {
            throw new RuntimeException(e1);
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping StampProvider pre-destroy. ");

        writeData();
        if (dataStore.implementsExtendedStoreAPI()) {
            ((ExtendedStore)dataStore).closeStore(INVERSE_STAMP_MAP_STORAGE_NAME);
            ((ExtendedStore)dataStore).closeStore(SEQUENCE_TO_UNCOMITTED_STAMP_STORAGE_NAME);
        }
        uncommittedStampIntegerConcurrentHashMap.clear();
        this.nextStampSequence.set(FIRST_STAMP_SEQUENCE);
        this.stampMap.clear();
        this.stampSequence_PathNid_Map.clear();
        this.sequenceToUncommittedStamp = null;
        this.inverseStampMap = null;
        this.dataStore = null;
        LOG.info("Stopped StampProvider pre-destroy. ");
    }

    @SuppressWarnings("rawtypes")
    private void writeData() throws RuntimeException {
        //We will NOT call sync here, it is the dataStores job to sync itself when requested, or on shutdown.
        try {
            if (inverseStampMap instanceof ExtendedStoreStandAlone) {
                ((ExtendedStoreStandAlone)inverseStampMap).sync().get();
                ((ExtendedStoreStandAlone)sequenceToUncommittedStamp).sync().get();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the author nid for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the author nid for stamp
     */
    @Override
    public int getAuthorNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.USER.getNid();
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            return this.inverseStampMap.get(stampSequence)
                    .getAuthorNid();
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us.getAuthorNid();
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return dataStore.getDataStoreId();
    }

    /**
     * Gets the database folder.
     *
     * @return the database folder
     */
    @Override
    public Path getDataStorePath() {
        return dataStore.getDataStorePath();
    }

    /**
     * Gets the database validity status.
     *
     * @return the database validity status
     */
    @Override
    public DataStoreStartState getDataStoreStartState() {
        return dataStore.getDataStoreStartState();
    }

    /**
     * Gets the module nid for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the module nid for stamp
     */
    @Override
    public int getModuleNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.UNSPECIFIED_MODULE.getNid();
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            return this.inverseStampMap.get(stampSequence)
                    .getModuleNid();
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us.getModuleNid();
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    /**
     * Checks if not canceled.
     *
     * @param stamp the stamp
     * @return true, if not canceled
     */
    @Override
    public boolean isNotCanceled(int stamp) {
        if (stamp < 0) {
            return false;
        }

        if (getStatusForStamp(stamp) == Status.CANCELED) {
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
    @Override
    public int getPathNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.DEVELOPMENT_PATH.getNid();
        }

        if (this.stampSequence_PathNid_Map.containsKey(stampSequence)) {
            return this.stampSequence_PathNid_Map.get(stampSequence);
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            this.stampSequence_PathNid_Map.put(stampSequence, this.inverseStampMap.get(stampSequence)
                    .getPathNid());
            return this.stampSequence_PathNid_Map.get(stampSequence);
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us.getPathNid();
        }


        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    boolean cancelUncommittedStamps = false;

    @Override
    //TODO Keith [1] - Dan Notes - I have no idea what this is for, or why it is done during changeset processing.  Completeyl unsafe for threading, 
    //No idea on the actual intention of the code.
    public void setCancelUncommittedStamps(boolean cancelUncommittedStamps) {
        this.cancelUncommittedStamps = cancelUncommittedStamps;
    }

    @Override
    public int getStampSequence(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        if (authorNid == 0) throw new IllegalStateException("Author cannot be zero...");
        if (moduleNid == 0) throw new IllegalStateException("module cannot be zero...");
        if (pathNid == 0) throw new IllegalStateException("path cannot be zero...");

        if (cancelUncommittedStamps && time == Long.MAX_VALUE) {
            time = Long.MIN_VALUE;
            status = Status.CANCELED;
            LOG.warn("Canceling uncommitted stamp: " + Get.conceptDescriptionText(authorNid) + " " +
                    Get.conceptDescriptionText(moduleNid) + " " +
                    Get.conceptDescriptionText(pathNid));
        } else {
            if (time == Long.MAX_VALUE) {
                throw new IllegalStateException("Uncommitted stamps must be accompanied by a transaction");
            }
            if (time == Long.MIN_VALUE) {
                throw new IllegalStateException("Canceled stamps cannot be created directly. They must be created using a transaction");
            }
        }
        return getStampSequence(null, status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public int getStampSequence(Transaction transaction, Status status, long time, int authorNid, int moduleNid, int pathNid) {
        if (authorNid == 0) throw new IllegalStateException("Author cannot be zero...");
        if (moduleNid == 0) throw new IllegalStateException("module cannot be zero...");
        if (pathNid == 0) throw new IllegalStateException("path cannot be zero...");
        if (transaction != null) {
            transaction = ((TransactionImpl) transaction).getTransactionForPath(pathNid);
        }

        if (status == Status.PRIMORDIAL) {
            throw new UnsupportedOperationException(status + " is not an assignable value.");
        }
        final Stamp stampKey = new Stamp(status, time, authorNid, moduleNid, pathNid);

        if (time == Long.MAX_VALUE || transaction != null) {
            final UncommittedStamp usp = new UncommittedStamp(transaction, status, time, authorNid, moduleNid, pathNid);
            final Integer temp = uncommittedStampIntegerConcurrentHashMap.get(usp);

            if (temp != null) {
                //LOG.info("Created stamp (1): " + temp);
                return temp;
            } else {
                this.stampLock.lock();

                try {
                    if (uncommittedStampIntegerConcurrentHashMap.containsKey(usp)) {
                        //LOG.info("Created stamp (2): " + uncommittedStampIntegerConcurrentHashMap.get(usp));
                        return uncommittedStampIntegerConcurrentHashMap.get(usp);
                    }

                    final int stampSequence = this.nextStampSequence.getAndIncrement();
                    if (transaction != null) {
                        Transaction transactionForPath = ((TransactionImpl) transaction).addStampToTransaction(stampSequence);
                    }
                    LOG.trace("Putting {}, {} into uncommitted stamp to sequence map", () -> usp.toString(), () -> stampSequence);
                    this.uncommittedStampIntegerConcurrentHashMap.put(usp, stampSequence);
                    this.sequenceToUncommittedStamp.put(stampSequence, usp);
                    this.inverseStampMap.put(stampSequence, stampKey);
                    return stampSequence;
                } finally {
                    this.stampLock.unlock();
                }
            }
        }

        if (!this.stampMap.containsKey(stampKey)) {
            // maybe have a few available in an atomic queue, and put back
            // if not used? Maybe in a thread-local?
            // Have different sequences, and have the increments be equal to the
            // number of sequences?
            this.stampLock.lock();

            try {
                if (!this.stampMap.containsKey(stampKey)) {
                    OptionalInt stampValue = OptionalInt.of(this.nextStampSequence.getAndIncrement());

                    this.inverseStampMap.put(stampValue.getAsInt(), stampKey);
                    this.stampMap.merge(stampKey, new int[]{stampValue.getAsInt()}, this::mergeSequences);
                }
            } finally {
                this.stampLock.unlock();
            }
            //LOG.info("Created stamp (4b): " + Arrays.toString(this.stampMap.get(stampKey)));
        }
        return this.stampMap.get(stampKey)[0];
    }

    /**
     * Gets the stamp sequences.
     *
     * @return the stamp sequences
     */
    @Override
    public IntStream getStampSequences() {
        return IntStream.rangeClosed(FIRST_STAMP_SEQUENCE, this.nextStampSequence.get())
                .filter((stampSequence) -> this.inverseStampMap.containsKey(stampSequence));
    }

    /**
     * Gets the status for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the status for stamp
     */
    @Override
    public Status getStatusForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return Status.CANCELED;
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            return this.inverseStampMap.get(stampSequence)
                    .getStatus();
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us.getStatus();
        }

        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    /**
     * Gets the time for stamp.
     *
     * @param stampSequence the stamp sequence
     * @return the time for stamp
     */
    @Override
    public long getTimeForStamp(int stampSequence) {
        if (stampSequence == Integer.MAX_VALUE) {
            return Long.MAX_VALUE; // uncommitted...
        }
        if (stampSequence < 0) {
            return Long.MIN_VALUE;
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            return this.inverseStampMap.get(stampSequence)
                    .getTime();
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us.getTime();
        }

        throw new NoSuchElementException(
                "No stampSequence found: " + stampSequence + " map size: " + this.stampMap.size() + " inverse map size: " +
                        this.inverseStampMap.size());
    }

    @Override
    public UUID getTransactionIdForStamp(int stampSequence) {
        if (sequenceToUncommittedStamp != null) {
            UncommittedStamp uncommittedStamp = sequenceToUncommittedStamp.get(stampSequence);
            if (uncommittedStamp != null) {
                return uncommittedStamp.getTransactionId();
            }
        }
        for (Entry<UncommittedStamp, Integer> entry: uncommittedStampIntegerConcurrentHashMap.entrySet()) {
            if (entry.getValue() == stampSequence) {
                return entry.getKey().getTransactionId();
            }
        }
        return UNKNOWN_TRANSACTION_ID;
    }

    @Override
    public Stamp getStamp(int stampSequence) {
        if (stampSequence < 0) {
            return new Stamp(Status.CANCELED, Long.MIN_VALUE, TermAux.USER.getNid(), TermAux.UNSPECIFIED_MODULE.getNid(), TermAux.DEVELOPMENT_PATH.getNid());
        }

        if (this.inverseStampMap.containsKey(stampSequence)) {
            return this.inverseStampMap.get(stampSequence);
        }
        UncommittedStamp us = sequenceToUncommittedStamp.get(stampSequence);
        if (us != null) {
            return us;
        }

        throw new NoSuchElementException(
                "No stampSequence found: " + stampSequence + " map size: " + this.stampMap.size() + " inverse map size: " +
                        this.inverseStampMap.size());
    }

    /**
     * Checks if uncommitted.
     *
     * @param stampSequence the stamp sequence
     * @return true, if uncommitted
     */
    @Override
    public boolean isUncommitted(int stampSequence) {
        return sequenceToUncommittedStamp.containsKey(stampSequence);
    }

    @Override
    public ImmutableIntSet getPathsInUse() {
        MutableIntSet pathsInUse = IntSets.mutable.empty();
        stampSequence_PathNid_Map.values().forEach(pathNid -> pathsInUse.add(pathNid));
        return pathsInUse.toImmutable();
    }

    @Override
    public ImmutableIntSet getModulesInUse() {
        MutableIntSet modulesInUse = IntSets.mutable.empty();
        for (Entry<Integer, Stamp> stamp: inverseStampMap.getEntrySet()) {
            modulesInUse.add(stamp.getValue().getModuleNid());
        }
        return modulesInUse.toImmutable();
    }

    @Override
    public ImmutableIntSet getAuthorsInUse() {
        MutableIntSet authorsInUse = IntSets.mutable.empty();
        for (Entry<Integer, Stamp> stamp: inverseStampMap.getEntrySet()) {
            authorsInUse.add(stamp.getValue().getAuthorNid());
        }
        return authorsInUse.toImmutable();
    }

    @Override
    public ImmutableLongList getTimesInUse() {
        MutableLongSet timesInUse = LongSets.mutable.empty();
        for (Entry<Integer, Stamp> stamp: inverseStampMap.getEntrySet()) {
            timesInUse.add(stamp.getValue().getTime());
        }
        return timesInUse.toSortedList().toImmutable();
    }
}

