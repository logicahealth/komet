/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.commit.manager;


import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.SystemStatusService;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.commit.Alert;
import gov.vha.isaac.ochre.api.commit.AlertType;
import gov.vha.isaac.ochre.api.commit.ChangeChecker;
import gov.vha.isaac.ochre.api.commit.CheckPhase;
import gov.vha.isaac.ochre.api.commit.ChronologyChangeListener;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.task.SequentialAggregateTask;
import gov.vha.isaac.ochre.api.task.TimedTask;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.collections.StampSequenceSet;
import gov.vha.isaac.ochre.api.collections.UuidIntMapMap;
import gov.vha.isaac.ochre.api.externalizable.StampAlias;
import gov.vha.isaac.ochre.api.externalizable.StampComment;
import gov.vha.isaac.ochre.model.ObjectChronologyImpl;
import gov.vha.isaac.ochre.model.ObjectVersionImpl;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service(name = "Cradle Commit Provider")
@RunLevel(value = 1)
public class CommitProvider implements CommitService {

    private static final Logger LOG = LogManager.getLogger();

    public static final String DEFAULT_CRADLE_COMMIT_MANAGER_FOLDER = "commit-manager";
    private static final String COMMIT_MANAGER_DATA_FILENAME = "commit-manager.data";
    private static final String STAMP_ALIAS_MAP_FILENAME = "stamp-alias.map";
    private static final String STAMP_COMMENT_MAP_FILENAME = "stamp-comment.map";

    private static final int WRITE_POOL_SIZE = 40;

    private final Path dbFolderPath;
    private final Path commitManagerFolder;
    private final ReentrantLock stampLock = new ReentrantLock();
    private final ReentrantLock uncommittedSequenceLock = new ReentrantLock();

    private final AtomicReference<Semaphore> writePermitReference
            = new AtomicReference<>(new Semaphore(WRITE_POOL_SIZE));

    private final WriteConceptCompletionService writeConceptCompletionService = new WriteConceptCompletionService();
    private final WriteSememeCompletionService writeSememeCompletionService = new WriteSememeCompletionService();
    private final ExecutorService writeConceptCompletionServicePool = Executors.newSingleThreadExecutor((Runnable r) -> {
        return new Thread(r, "writeConceptCompletionService");
    });
    private final ExecutorService writeSememeCompletionServicePool = Executors.newSingleThreadExecutor((Runnable r) -> {
        return new Thread(r, "writeSememeCompletionService");
    });

    ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners = new ConcurrentSkipListSet<>();
    private final ConcurrentSkipListSet<ChangeChecker> checkers = new ConcurrentSkipListSet<>();
    private long lastCommit = Long.MIN_VALUE;
    private AtomicBoolean loadRequired = new AtomicBoolean();
    /**
     * TODO recreate alert collection at restart for uncommitted components.
     */
    private final ConcurrentSkipListSet<Alert> alertCollection = new ConcurrentSkipListSet<>();

    /**
     * TODO: persist across restarts.
     */
    private static final Map<UncommittedStamp, Integer> UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP
            = new ConcurrentHashMap<>();

    /**
     * Persistent map of a stamp aliases to a sequence
     */
    private final StampAliasMap stampAliasMap = new StampAliasMap();
    /**
     * Persistent map of comments to a stamp.
     */
    private final StampCommentMap stampCommentMap = new StampCommentMap();
    /**
     * Persistent map of stamp sequences to a Stamp object.
     */
    private final ConcurrentObjectIntMap<Stamp> stampMap = new ConcurrentObjectIntMap<>();
    /**
     * Persistent sequence of database commit actions
     */
    private final AtomicLong databaseSequence = new AtomicLong();
    /**
     * Persistent stamp sequence
     */
    private final AtomicInteger nextStampSequence = new AtomicInteger(FIRST_STAMP_SEQUENCE);

    /**
     * Persistent as a result of reading and writing the stampMap.
     */
    private final ConcurrentSequenceSerializedObjectMap<Stamp> inverseStampMap;

    private final ConceptSequenceSet uncommittedConceptsWithChecksSequenceSet = ConceptSequenceSet.concurrent();
    private final ConceptSequenceSet uncommittedConceptsNoChecksSequenceSet = ConceptSequenceSet.concurrent();
    private final SememeSequenceSet uncommittedSememesWithChecksSequenceSet = SememeSequenceSet.concurrent();
    private final SememeSequenceSet uncommittedSememesNoChecksSequenceSet = SememeSequenceSet.concurrent();

    private CommitProvider() throws IOException {
        try {
            dbFolderPath = LookupService.getService(ConfigurationService.class).getChronicleFolderPath().resolve("commit-provider");
            loadRequired.set(Files.exists(dbFolderPath));
            Files.createDirectories(dbFolderPath);
            inverseStampMap = new ConcurrentSequenceSerializedObjectMap<>(new StampSerializer(),
                    dbFolderPath, null, null);
            commitManagerFolder = dbFolderPath.resolve(DEFAULT_CRADLE_COMMIT_MANAGER_FOLDER);
            Files.createDirectories(commitManagerFolder);
        } catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Cradle Commit Provider", e);
            throw e;
        }
    }

    @PostConstruct
    private void startMe() throws IOException {
        try {
            LOG.info("Starting CradleCommitManager post-construct");
            writeConceptCompletionServicePool.submit(writeConceptCompletionService);
            writeSememeCompletionServicePool.submit(writeSememeCompletionService);
            if (loadRequired.get()) {
                LOG.info("Reading existing commit manager data. ");
                LOG.info("Reading " + COMMIT_MANAGER_DATA_FILENAME);
                try (DataInputStream in = new DataInputStream(new FileInputStream(new File(commitManagerFolder.toFile(), COMMIT_MANAGER_DATA_FILENAME)))) {
                    nextStampSequence.set(in.readInt());
                    databaseSequence.set(in.readLong());
                    UuidIntMapMap.getNextNidProvider().set(in.readInt());
                    int stampMapSize = in.readInt();
                    for (int i = 0; i < stampMapSize; i++) {
                        int stampSequence = in.readInt();
                        Stamp stamp = new Stamp(in);
                        stampMap.put(stamp, stampSequence);
                        inverseStampMap.put(stampSequence, stamp);
                    }
                    uncommittedConceptsWithChecksSequenceSet.read(in);
                    uncommittedConceptsNoChecksSequenceSet.read(in);
                    uncommittedSememesWithChecksSequenceSet.read(in);
                    uncommittedSememesNoChecksSequenceSet.read(in);

                    int uncommittedSize = in.readInt();
                    for (int i = 0; i < uncommittedSize; i++) {
                        UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.put(new UncommittedStamp(in), in.readInt());
                    }
                }

                LOG.info("Reading: " + STAMP_ALIAS_MAP_FILENAME);
                stampAliasMap.read(new File(commitManagerFolder.toFile(), STAMP_ALIAS_MAP_FILENAME));
                LOG.info("Reading: " + STAMP_COMMENT_MAP_FILENAME);
                stampCommentMap.read(new File(commitManagerFolder.toFile(), STAMP_COMMENT_MAP_FILENAME));
            }

        } catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Cradle Commit Provider", e);
            throw e;
        }
    }

    @PreDestroy
    private void stopMe() throws IOException {
        LOG.info("Stopping CradleCommitManager pre-destroy. ");
        writeConceptCompletionService.cancel();
        writeSememeCompletionService.cancel();
        stampAliasMap.write(new File(commitManagerFolder.toFile(), STAMP_ALIAS_MAP_FILENAME));
        stampCommentMap.write(new File(commitManagerFolder.toFile(), STAMP_COMMENT_MAP_FILENAME));

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(commitManagerFolder.toFile(), COMMIT_MANAGER_DATA_FILENAME)))) {
            out.writeInt(nextStampSequence.get());
            out.writeLong(databaseSequence.get());
            out.writeInt(UuidIntMapMap.getNextNidProvider().get());
            out.writeInt(stampMap.size());
            stampMap.forEachPair((Stamp stamp, int stampSequence) -> {
                try {
                    out.writeInt(stampSequence);
                    stamp.write(out);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            uncommittedConceptsWithChecksSequenceSet.write(out);
            uncommittedConceptsNoChecksSequenceSet.write(out);
            uncommittedSememesWithChecksSequenceSet.write(out);
            uncommittedSememesNoChecksSequenceSet.write(out);

            int size = UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.size();
            out.writeInt(size);

            for (Map.Entry<UncommittedStamp, Integer> entry : UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.entrySet()) {
                entry.getKey().write(out);
                out.writeInt(entry.getValue());
            }
        }

    }

    @Override
    public String getTextSummary() {
        StringBuilder builder = new StringBuilder("CommitProvider summary: ");
        builder.append("\nnextStamp: ").append(nextStampSequence);
        builder.append("\nuncommitted concepts with checks: ").append(uncommittedConceptsWithChecksSequenceSet);
        builder.append("\nuncommitted concepts no checks: ").append(uncommittedConceptsNoChecksSequenceSet);
        builder.append("\nuncommitted sememes with checks: ").append(uncommittedSememesWithChecksSequenceSet);
        builder.append("\nuncommitted sememes no checks: ").append(uncommittedSememesNoChecksSequenceSet);
        builder.append("\nuncommitted stamps: ").append(UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP);
        return builder.toString();
    }

    @Override
    public void addAlias(int stampSequence, int stampAlias, String aliasCommitComment) {
        stampAliasMap.addAlias(stampSequence, stampAlias);
        if (aliasCommitComment != null) {
            stampCommentMap.addComment(stampAlias, aliasCommitComment);
        }
    }

    @Override
    public int[] getAliases(int stampSequence) {
        return stampAliasMap.getAliases(stampSequence);
    }

    @Override
    public void setComment(int stampSequence, String comment) {
        stampCommentMap.addComment(stampSequence, comment);
    }

    @Override
    public Optional<String> getComment(int stampSequence) {
        return stampCommentMap.getComment(stampSequence);
    }

    @Override
    public long getCommitManagerSequence() {
        return databaseSequence.get();
    }

    @Override
    public long incrementAndGetSequence() {
        return databaseSequence.incrementAndGet();
    }

    @Override
    public int getAuthorSequenceForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.USER.getConceptSequence();
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return Get.identifierService().getConceptSequence(
                    s.get().getAuthorSequence());
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    public int getAuthorNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.USER.getNid();
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return s.get().getAuthorSequence();
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    @Override
    public int getModuleSequenceForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.UNSPECIFIED_MODULE.getConceptSequence();
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return Get.identifierService().getConceptSequence(
                    s.get().getModuleSequence());
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    private int getModuleNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.UNSPECIFIED_MODULE.getNid();
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return s.get().getModuleSequence();
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    ConcurrentHashMap<Integer, Integer> stampSequencePathSequenceMap = new ConcurrentHashMap();

    @Override
    public int getPathSequenceForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.PATH.getConceptSequence();
        }
        if (stampSequencePathSequenceMap.containsKey(stampSequence)) {
            return stampSequencePathSequenceMap.get(stampSequence);
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            stampSequencePathSequenceMap.put(stampSequence, Get.identifierService().getConceptSequence(
                    s.get().getPathSequence()));
            return stampSequencePathSequenceMap.get(stampSequence);
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    private int getPathNidForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return TermAux.PATH.getNid();
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return s.get().getPathSequence();
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    @Override
    public State getStatusForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return State.CANCELED;
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return s.get().getStatus();
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence);
    }

    @Override
    public long getTimeForStamp(int stampSequence) {
        if (stampSequence < 0) {
            return Long.MIN_VALUE;
        }
        Optional<Stamp> s = inverseStampMap.get(stampSequence);
        if (s.isPresent()) {
            return s.get().getTime();
        }
        throw new NoSuchElementException("No stampSequence found: " + stampSequence
                + " map size: " + stampMap.size()
                + " inverse map size: " + inverseStampMap.getSize());
    }

    @Override
    public int getRetiredStampSequence(int stampSequence) {
        return getStampSequence(State.INACTIVE,
                getTimeForStamp(stampSequence),
                getAuthorSequenceForStamp(stampSequence),
                getModuleSequenceForStamp(stampSequence),
                getPathSequenceForStamp(stampSequence));
    }

    @Override
    public int getActivatedStampSequence(int stampSequence) {
        return getStampSequence(State.ACTIVE,
                getTimeForStamp(stampSequence),
                getAuthorSequenceForStamp(stampSequence),
                getModuleSequenceForStamp(stampSequence),
                getPathSequenceForStamp(stampSequence));
    }

    @Override
    public int getStampSequence(State status, long time, int authorSequence, int moduleSequence, int pathSequence) {
        Stamp stampKey = new Stamp(status, time,
                authorSequence,
                moduleSequence,
                pathSequence);

        if (time == Long.MAX_VALUE) {
            UncommittedStamp usp = new UncommittedStamp(status, authorSequence,
                    moduleSequence, pathSequence);
            if (UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.containsKey(usp)) {
                return UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get(usp);
            } else {
                stampLock.lock();

                try {
                    if (UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.containsKey(usp)) {
                        return UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.get(usp);
                    }

                    int stampSequence = nextStampSequence.getAndIncrement();
                    UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.put(usp, stampSequence);
                    inverseStampMap.put(stampSequence, stampKey);

                    return stampSequence;
                } finally {
                    stampLock.unlock();
                }
            }
        }

        OptionalInt stampValue = stampMap.get(stampKey);
        if (!stampValue.isPresent()) {
            // maybe have a few available in an atomic queue, and put back
            // if not used? Maybe in a thread-local?
            // Have different sequences, and have the increments be equal to the
            // number of sequences?
            stampLock.lock();
            try {
                stampValue = stampMap.get(stampKey);
                if (!stampValue.isPresent()) {
                    stampValue = OptionalInt.of(nextStampSequence.getAndIncrement());
                    inverseStampMap.put(stampValue.getAsInt(), stampKey);
                    stampMap.put(stampKey, stampValue.getAsInt());
                }
            } finally {
                stampLock.unlock();
            }
        }
        return stampValue.getAsInt();
    }

    @Override
    public Task<Void> cancel() {
        return cancel(Get.configurationService().getDefaultEditCoordinate());
    }

    @Override
    public Task<Void> cancel(ConceptChronology cc) {
        return cancel(cc, Get.configurationService().getDefaultEditCoordinate());
    }

    @Override
    public Task<Void> cancel(SememeChronology sememeChronicle) {
        return cancel(sememeChronicle, Get.configurationService().getDefaultEditCoordinate());
    }

    @Override
    public Task<Void> cancel(ObjectChronology<?> chronicle, EditCoordinate editCoordinate) {
        ObjectChronologyImpl chronicleImpl = (ObjectChronologyImpl) chronicle;
        List<ObjectVersionImpl> versionList = chronicleImpl.getVersionList();
        for (ObjectVersionImpl version : versionList) {
            if (version.isUncommitted()) {
                if (version.getAuthorSequence() == editCoordinate.getAuthorSequence()) {
                    version.cancel();
                }
            }
        }
        chronicleImpl.setVersions(versionList); // see if id is in uncommitted with checks, and without checks...
        Collection<Task<?>> subTasks = new ArrayList<>();
        if (chronicle instanceof ConceptChronology) {
            ConceptChronology conceptChronology = (ConceptChronology) chronicle;
            if (uncommittedConceptsNoChecksSequenceSet.contains(conceptChronology.getConceptSequence())) {
                subTasks.add(addUncommittedNoChecks(conceptChronology));
            }
            if (uncommittedConceptsWithChecksSequenceSet.contains(conceptChronology.getConceptSequence())) {
                subTasks.add(addUncommitted(conceptChronology));
            }
        } else if (chronicle instanceof SememeChronology) {
            SememeChronology sememeChronology = (SememeChronology) chronicle;
            if (uncommittedSememesNoChecksSequenceSet.contains(sememeChronology.getSememeSequence())) {
                subTasks.add(addUncommittedNoChecks(sememeChronology));
            }
            if (uncommittedSememesWithChecksSequenceSet.contains(sememeChronology.getSememeSequence())) {
                subTasks.add(addUncommitted(sememeChronology));
            }
        } else {
            throw new RuntimeException("Unsupported chronology type: " + chronicle);
        }
        return new SequentialAggregateTask<>("Canceling change", subTasks);
    }

    @Override
    public synchronized Task<Optional<CommitRecord>> commit(ObjectChronology<?> chronicle, EditCoordinate editCoordinate, String commitComment) {
        // TODO make asynchronous with a actual task. 
        CommitRecord commitRecord = null;
        Set<Alert> alerts = new HashSet<>();
        if (chronicle instanceof ConceptChronology) {
            ConceptChronology conceptChronology = (ConceptChronology) chronicle;
            if (uncommittedConceptsWithChecksSequenceSet.contains(conceptChronology.getConceptSequence())) {
                checkers.stream().forEach((check) -> {
                    check.check(conceptChronology, alerts, CheckPhase.COMMIT);
                });
            }
        } else if (chronicle instanceof SememeChronology) {
            SememeChronology sememeChronology = (SememeChronology) chronicle;
            if (uncommittedSememesWithChecksSequenceSet.contains(sememeChronology.getSememeSequence())) {
                checkers.stream().forEach((check) -> {
                    check.check(sememeChronology, alerts, CheckPhase.COMMIT);
                });
            }
        } else {
            throw new RuntimeException("Unsupported chronology type: " + chronicle);
        }
        if (alerts.stream().anyMatch((alert)
                -> (alert.getAlertType() == AlertType.ERROR))) {
            alertCollection.addAll(alerts);

        } else {
            long commitTime = System.currentTimeMillis();
            // TODO have it only commit the versions on the sememe consistent with the edit coordinate. 
            // successful check, commit and remove uncommitted sequences...
            StampSequenceSet stampsInCommit = new StampSequenceSet();
            OpenIntIntHashMap stampAliases = new OpenIntIntHashMap();
            ConceptSequenceSet conceptsInCommit = new ConceptSequenceSet();
            SememeSequenceSet sememesInCommit = new SememeSequenceSet();

            chronicle.getVersionList().forEach((version) -> {
                if (((ObjectVersionImpl) version).isUncommitted() && ((ObjectVersionImpl) version).getAuthorSequence() == editCoordinate.getAuthorSequence()) {
                    ((ObjectVersionImpl) version).setTime(commitTime);
                    stampsInCommit.add(((ObjectVersionImpl) version).getStampSequence());
                }
            });

            if (chronicle instanceof ConceptChronology) {
                ConceptChronology conceptChronology = (ConceptChronology) chronicle;
                conceptsInCommit.add(conceptChronology.getConceptSequence());
                uncommittedConceptsWithChecksSequenceSet.remove(conceptChronology.getConceptSequence());
                uncommittedConceptsNoChecksSequenceSet.remove(conceptChronology.getConceptSequence());
                Get.conceptService().writeConcept(conceptChronology);
            } else {
                SememeChronology sememeChronology = (SememeChronology) chronicle;
                sememesInCommit.add(sememeChronology.getSememeSequence());
                uncommittedSememesWithChecksSequenceSet.remove(sememeChronology.getSememeSequence());
                uncommittedSememesNoChecksSequenceSet.remove(sememeChronology.getSememeSequence());
                Get.sememeService().writeSememe(sememeChronology);
            }

            commitRecord = new CommitRecord(Instant.ofEpochMilli(commitTime),
                    stampsInCommit,
                    stampAliases, conceptsInCommit, sememesInCommit,
                    commitComment);
        }

        CommitProvider.this.handleCommitNotification(commitRecord);
        Optional<CommitRecord> optionalRecord = Optional.ofNullable(commitRecord);
        Task<Optional<CommitRecord>> task = new TimedTask() {

            @Override
            protected Optional<CommitRecord> call() throws Exception {
                Get.activeTasks().remove(this);
                return optionalRecord;
            }
        };

        Get.activeTasks().add(task);
        Get.workExecutors().getExecutor().execute(task);
        return task;
    }

    @Override
    public Task<Optional<CommitRecord>> commit(EditCoordinate editCoordinate, String commitComment) {
        // TODO, make this only commit those components with changes from the provided edit coordinate. 
        throw new UnsupportedOperationException("This implementation is broken");
        //TODO this needs repair... pendingStampsForCommit, for example, is never populated.  
//		Semaphore pendingWrites = writePermitReference.getAndSet(new Semaphore(WRITE_POOL_SIZE));
//		pendingWrites.acquireUninterruptibly(WRITE_POOL_SIZE);
//		alertCollection.clear();
//		lastCommit = databaseSequence.incrementAndGet();
//
//		Map<UncommittedStamp, Integer> pendingStampsForCommit = new HashMap<>();
//		UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.forEach((uncommittedStamp, stampSequence) -> {
//			if (uncommittedStamp.authorSequence == editCoordinate.getAuthorSequence()) {
//				Stamp stamp = new Stamp(Status.getStatusFromState(uncommittedStamp.status),
//						  Long.MIN_VALUE,
//						  Get.identifierService().getConceptNid(uncommittedStamp.authorSequence),
//						  Get.identifierService().getConceptNid(uncommittedStamp.moduleSequence),
//						  Get.identifierService().getConceptNid(uncommittedStamp.pathSequence));
//				addStamp(stamp, stampSequence);
//				UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.remove(uncommittedStamp);
//			}
//		});
//		UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.clear();
//
//		CommitTask task = CommitTask.get(commitComment,
//				  uncommittedConceptsWithChecksSequenceSet,
//				  uncommittedConceptsNoChecksSequenceSet,
//				  uncommittedSememesWithChecksSequenceSet,
//				  uncommittedSememesNoChecksSequenceSet,
//				  lastCommit,
//				  checkers,
//				  alertCollection,
//				  pendingStampsForCommit,
//				  this);
//		return task;
    }

    @Override
    public synchronized Task<Void> cancel(EditCoordinate editCoordinate) {
        UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.forEach((uncommittedStamp, stampSequence) -> {
            if (uncommittedStamp.authorSequence == editCoordinate.getAuthorSequence()) {
                Stamp stamp = new Stamp(uncommittedStamp.status,
                        Long.MIN_VALUE,
                        uncommittedStamp.authorSequence,
                        uncommittedStamp.moduleSequence,
                        uncommittedStamp.pathSequence);
                addStamp(stamp, stampSequence);
                UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.remove(uncommittedStamp);
            }
        });
        // TODO make asynchronous with a actual task. 
        Task<Void> task = new TimedTask() {

            @Override
            protected Object call() throws Exception {
                Get.activeTasks().remove(this);
                return null;
            }
        };

        Get.activeTasks().add(task);
        Get.workExecutors().getExecutor().execute(task);
        return task;
    }

    /**
     * Perform a global commit. The caller may chose to block on the returned
     * task if synchronous operation is desired.
     *
     * @param commitComment
     * @return a task that is already submitted to an executor.
     */
    @Override
    public synchronized Task<Optional<CommitRecord>> commit(String commitComment) {
//		return commit(Get.configurationService().getDefaultEditCoordinate(), commitComment);
        Semaphore pendingWrites = writePermitReference.getAndSet(new Semaphore(WRITE_POOL_SIZE));
        pendingWrites.acquireUninterruptibly(WRITE_POOL_SIZE);
        alertCollection.clear();
        lastCommit = databaseSequence.incrementAndGet();

        Map<UncommittedStamp, Integer> pendingStampsForCommit = new HashMap<>(UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP);
        UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.clear();

        CommitTask task = CommitTask.get(commitComment,
                uncommittedConceptsWithChecksSequenceSet,
                uncommittedConceptsNoChecksSequenceSet,
                uncommittedSememesWithChecksSequenceSet,
                uncommittedSememesNoChecksSequenceSet,
                lastCommit,
                checkers,
                alertCollection,
                pendingStampsForCommit,
                this);
        return task;
    }

    protected void handleCommitNotification(CommitRecord commitRecord) {
        changeListeners.forEach((listenerRef) -> {
            ChronologyChangeListener listener = listenerRef.get();
            if (listener == null) {
                changeListeners.remove(listenerRef);
            } else {
                listener.handleCommit(commitRecord);
            }
        });
    }

    protected void revertCommit(ConceptSequenceSet conceptsToCommit,
            ConceptSequenceSet conceptsToCheck,
            SememeSequenceSet sememesToCommit,
            SememeSequenceSet sememesToCheck,
            Map<UncommittedStamp, Integer> pendingStampsForCommit) {

        UNCOMMITTED_STAMP_TO_STAMP_SEQUENCE_MAP.putAll(pendingStampsForCommit);
        uncommittedSequenceLock.lock();
        try {
            uncommittedConceptsWithChecksSequenceSet.or(conceptsToCheck);
            uncommittedConceptsNoChecksSequenceSet.or(conceptsToCommit);
            uncommittedConceptsNoChecksSequenceSet.andNot(conceptsToCheck);

            uncommittedSememesWithChecksSequenceSet.or(sememesToCheck);
            uncommittedSememesNoChecksSequenceSet.or(sememesToCommit);
            uncommittedSememesNoChecksSequenceSet.andNot(sememesToCheck);
        } finally {
            uncommittedSequenceLock.unlock();
        }

    }

    @Override
    public Task<Optional<CommitRecord>> commit(ConceptChronology cc, String commitComment) {
        return commit(cc, Get.configurationService().getDefaultEditCoordinate(), commitComment);
    }

    @Override
    public Task<Optional<CommitRecord>> commit(SememeChronology cc, String commitComment) {
        return commit(cc, Get.configurationService().getDefaultEditCoordinate(), commitComment);
    }

    @Override
    public ObservableList<Integer> getUncommittedConceptNids() {
        // need to create a list that can be backed with a set...
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObservableList<Alert> getAlertList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addChangeChecker(ChangeChecker checker) {
        checkers.add(checker);
    }

    @Override
    public void removeChangeChecker(ChangeChecker checker) {
        checkers.remove(checker);
    }

    @Override
    public boolean isNotCanceled(int stamp) {
        if (stamp < 0) {
            return false;
        }
        return getTimeForStamp(stamp) != Long.MIN_VALUE;
    }

    @Override
    public String describeStampSequence(int stampSequence) {
        StringBuilder sb = new StringBuilder();
        sb.append("⦙");
        sb.append(stampSequence);
        sb.append("::");
        State status = getStatusForStamp(stampSequence);
        sb.append(status);
        if (status == State.ACTIVE) {
            sb.append("  ");
        }
        sb.append(" ");

        long time = getTimeForStamp(stampSequence);
        if (time == Long.MAX_VALUE) {
            sb.append("UNCOMMITTED:");
        } else if (time == Long.MIN_VALUE) {
            sb.append("CANCELED:  ");
        } else {
            sb.append(Instant.ofEpochMilli(time));
        }
        sb.append(" a:");
        sb.append(Get.conceptDescriptionText(getAuthorSequenceForStamp(stampSequence)));
        sb.append(" <");
        sb.append(getAuthorSequenceForStamp(stampSequence));
        sb.append(">");
        sb.append(" m:");
        sb.append(Get.conceptDescriptionText(getModuleSequenceForStamp(stampSequence)));
        sb.append(" <");
        sb.append(getModuleSequenceForStamp(stampSequence));
        sb.append(">");
        sb.append(" p: ");
        sb.append(Get.conceptDescriptionText(getPathSequenceForStamp(stampSequence)));
        sb.append(" <");
        sb.append(getPathSequenceForStamp(stampSequence));
        sb.append(">⦙");
        return sb.toString();
    }

    @Override
    public IntStream getStampSequences() {
        return IntStream.rangeClosed(1, nextStampSequence.get()).
                filter((stampSequence) -> inverseStampMap.containsKey(stampSequence));
    }

    @Override
    public Task<Void> addUncommitted(SememeChronology sc) {
        handleUncommittedSequenceSet(sc, uncommittedSememesWithChecksSequenceSet);
        return writeSememeCompletionService.checkAndWrite(sc, checkers, alertCollection,
                writePermitReference.get(), changeListeners);
    }

    @Override
    public Task<Void> addUncommittedNoChecks(SememeChronology sc) {
        handleUncommittedSequenceSet(sc, uncommittedSememesNoChecksSequenceSet);
        return writeSememeCompletionService.write(sc,
                writePermitReference.get(), changeListeners);
    }

    @Override
    public Task<Void> addUncommitted(ConceptChronology cc) {
        handleUncommittedSequenceSet(cc, uncommittedConceptsWithChecksSequenceSet);
        return writeConceptCompletionService.checkAndWrite(cc, checkers, alertCollection,
                writePermitReference.get(), changeListeners);
    }

    @Override
    public Task<Void> addUncommittedNoChecks(ConceptChronology cc) {
        handleUncommittedSequenceSet(cc, uncommittedConceptsNoChecksSequenceSet);
        return writeConceptCompletionService.write(cc,
                writePermitReference.get(), changeListeners);
    }

    private void handleUncommittedSequenceSet(SememeChronology sememeChronicle, SememeSequenceSet set) {
        if (sememeChronicle.getCommitState() == CommitStates.UNCOMMITTED) {
            uncommittedSequenceLock.lock();
            try {
                set.add(Get.identifierService().getSememeSequence(sememeChronicle.getNid()));
            } finally {
                uncommittedSequenceLock.unlock();
            }
        } else {
            uncommittedSequenceLock.lock();
            try {
                set.remove(Get.identifierService().getSememeSequence(sememeChronicle.getNid()));
            } finally {
                uncommittedSequenceLock.unlock();
            }
        }
    }

    private void handleUncommittedSequenceSet(ConceptChronology concept, ConceptSequenceSet set) {
        if (concept.isUncommitted()) {
            uncommittedSequenceLock.lock();
            try {
                set.add(Get.identifierService().getConceptSequence(concept.getNid()));
            } finally {
                uncommittedSequenceLock.unlock();
            }
        } else {
            uncommittedSequenceLock.lock();
            try {
                set.remove(Get.identifierService().getConceptSequence(concept.getNid()));
            } finally {
                uncommittedSequenceLock.unlock();
            }
        }
    }

    @Override
    public boolean isUncommitted(int stampSequence) {
        return getTimeForStamp(stampSequence) == Long.MAX_VALUE;
    }

    protected void addComment(int stamp, String commitComment) {
        stampCommentMap.addComment(stamp, commitComment);
    }

    protected void addStamp(Stamp stamp, int stampSequence) {
        stampMap.put(stamp, stampSequence);
        inverseStampMap.put(stampSequence, stamp);
    }

    @Override
    public void addChangeListener(ChronologyChangeListener changeListener) {
        changeListeners.add(new ChangeListenerReference(changeListener));
    }

    @Override
    public void removeChangeListener(ChronologyChangeListener changeListener) {
        changeListeners.remove(new ChangeListenerReference(changeListener));
    }

    private static class ChangeListenerReference extends WeakReference<ChronologyChangeListener> implements Comparable<ChangeListenerReference> {

        UUID listenerUuid;

        public ChangeListenerReference(ChronologyChangeListener referent) {
            super(referent);
            this.listenerUuid = referent.getListenerUuid();
        }

        public ChangeListenerReference(ChronologyChangeListener referent, ReferenceQueue<? super ChronologyChangeListener> q) {
            super(referent, q);
            this.listenerUuid = referent.getListenerUuid();
        }

        @Override
        public int compareTo(ChangeListenerReference o) {
            return this.listenerUuid.compareTo(o.listenerUuid);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.listenerUuid);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChangeListenerReference other = (ChangeListenerReference) obj;
            return Objects.equals(this.listenerUuid, other.listenerUuid);
        }

    }

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
    public Stream<StampAlias> getStampAliasStream() {
        return stampAliasMap.getStampAliasStream();
    }

    @Override
    public Stream<StampComment> getStampCommentStream() {
        return stampCommentMap.getStampCommentStream();
    }

}
