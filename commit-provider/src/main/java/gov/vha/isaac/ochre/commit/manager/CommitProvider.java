/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.commit.manager;


import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.SystemStatusService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.commit.*;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
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
@Service(name = "Commit Provider")
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
     * Persistent map of a stamp aliases to a sequence
     */
    private final StampAliasMap stampAliasMap = new StampAliasMap();
    /**
     * Persistent map of comments to a stamp.
     */
    private final StampCommentMap stampCommentMap = new StampCommentMap();

    /**
     * Persistent sequence of database commit actions
     */
    private final AtomicLong databaseSequence = new AtomicLong();
    /**
     * Persistent stamp sequence
     */
    private final ConceptSequenceSet uncommittedConceptsWithChecksSequenceSet = ConceptSequenceSet.concurrent();
    private final ConceptSequenceSet uncommittedConceptsNoChecksSequenceSet = ConceptSequenceSet.concurrent();
    private final SememeSequenceSet uncommittedSememesWithChecksSequenceSet = SememeSequenceSet.concurrent();
    private final SememeSequenceSet uncommittedSememesNoChecksSequenceSet = SememeSequenceSet.concurrent();

    private CommitProvider() throws IOException {
        try {
            dbFolderPath = LookupService.getService(ConfigurationService.class).getChronicleFolderPath().resolve("commit-provider");
            loadRequired.set(Files.exists(dbFolderPath));
            Files.createDirectories(dbFolderPath);
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
            LOG.info("Starting CommitProvider post-construct");
            writeConceptCompletionServicePool.submit(writeConceptCompletionService);
            writeSememeCompletionServicePool.submit(writeSememeCompletionService);
            if (loadRequired.get()) {
                LOG.info("Reading existing commit manager data. ");
                LOG.info("Reading " + COMMIT_MANAGER_DATA_FILENAME);
                try (DataInputStream in = new DataInputStream(new FileInputStream(new File(commitManagerFolder.toFile(), COMMIT_MANAGER_DATA_FILENAME)))) {
                    databaseSequence.set(in.readLong());
                    UuidIntMapMap.getNextNidProvider().set(in.readInt());
                    uncommittedConceptsWithChecksSequenceSet.read(in);
                    uncommittedConceptsNoChecksSequenceSet.read(in);
                    uncommittedSememesWithChecksSequenceSet.read(in);
                    uncommittedSememesNoChecksSequenceSet.read(in);
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
        LOG.info("Stopping CommitProvider pre-destroy. ");
        writeConceptCompletionService.cancel();
        writeSememeCompletionService.cancel();
        stampAliasMap.write(new File(commitManagerFolder.toFile(), STAMP_ALIAS_MAP_FILENAME));
        stampCommentMap.write(new File(commitManagerFolder.toFile(), STAMP_COMMENT_MAP_FILENAME));

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(commitManagerFolder.toFile(), COMMIT_MANAGER_DATA_FILENAME)))) {
            out.writeLong(databaseSequence.get());
            out.writeInt(UuidIntMapMap.getNextNidProvider().get());

            uncommittedConceptsWithChecksSequenceSet.write(out);
            uncommittedConceptsNoChecksSequenceSet.write(out);
            uncommittedSememesWithChecksSequenceSet.write(out);
            uncommittedSememesNoChecksSequenceSet.write(out);

        }

    }

    @Override
    public String getUncommittedComponentTextSummary() {
        StringBuilder builder = new StringBuilder("CommitProvider summary: ");
        builder.append("\nuncommitted concepts with checks: ").append(uncommittedConceptsWithChecksSequenceSet);
        builder.append("\nuncommitted concepts no checks: ").append(uncommittedConceptsNoChecksSequenceSet);
        builder.append("\nuncommitted sememes with checks: ").append(uncommittedSememesWithChecksSequenceSet);
        builder.append("\nuncommitted sememes no checks: ").append(uncommittedSememesNoChecksSequenceSet);
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

    /**
     * Perform a global commit. The caller may chose to block on the returned
     * task if synchronous operation is desired.
     *
     *
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


        Map<UncommittedStamp, Integer> pendingStampsForCommit = Get.stampService().getPendingStampsForCommit();

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

        Get.stampService().setPendingStampsForCommit(pendingStampsForCommit);
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


    protected void addComment(int stamp, String commitComment) {
        stampCommentMap.addComment(stamp, commitComment);
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

    public Task<Void> cancel(EditCoordinate editCoordinate) {
        return Get.stampService().cancel(editCoordinate.getAuthorSequence());
    }

    @Override
    public Stream<StampAlias> getStampAliasStream() {
        return stampAliasMap.getStampAliasStream();
    }

    @Override
    public Stream<StampComment> getStampCommentStream() {
        return stampCommentMap.getStampCommentStream();
    }

    public void importNoChecks(OchreExternalizable ochreExternalizable) {
        switch (ochreExternalizable.getOchreObjectType()) {
            case CONCEPT:
                ConceptChronology conceptChronology = (ConceptChronology) ochreExternalizable;
                Get.conceptService().writeConcept(conceptChronology);
                break;
            case SEMEME:
                SememeChronology sememeChronology = (SememeChronology) ochreExternalizable;
                Get.sememeService().writeSememe(sememeChronology);
                break;
            case STAMP_ALIAS:
                StampAlias stampAlias = (StampAlias) ochreExternalizable;
                stampAliasMap.addAlias(stampAlias.getStampSequence(), stampAlias.getStampAlias());
                break;
            case STAMP_COMMENT:
                StampComment stampComment = (StampComment) ochreExternalizable;
                stampCommentMap.addComment(stampComment.getStampSequence(), stampComment.getComment());
                break;
            default: throw new UnsupportedOperationException("Can't handle: " + ochreExternalizable);
        }
    }

}
