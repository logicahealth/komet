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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sh.isaac.provider.commit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.MetadataService;
import sh.isaac.api.SystemStatusService;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.alert.SuccessAlert;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.commit.ChangeChecker;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.CheckAndWriteTask;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.CommitTask;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.datastore.ExtendedStore;
import sh.isaac.api.datastore.ExtendedStoreData;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.DataToBytesUtils;
import sh.isaac.model.ChronologyImpl;
import sh.isaac.model.DataStoreSubService;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 * The Class CommitProvider.
 *
 * @author kec
 */
@Service(name = "Commit Provider")
@RunLevel(value = LookupService.SL_L2)
public class CommitProvider
        implements CommitService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();

    private static boolean singleTransactionOnly = false;

    private static boolean logTransactionCreation = false;

    /**
     * The Constant DEFAULT_COMMIT_MANAGER_FOLDER.
     */
    public static final String DEFAULT_COMMIT_MANAGER_FOLDER = "commit-manager";

    /**
     * The Constant COMMIT_MANAGER_DATA_FILENAME.
     */
    private static final String COMMIT_MANAGER_DATA_FILENAME = "commit-manager.data";

    /**
     * The Constant STAMP_ALIAS_MAP_FILENAME.
     */
    private static final String STAMP_ALIAS_MAP_FILENAME = "stamp-alias.map";

    /**
     * The Constant STAMP_COMMENT_MAP_FILENAME.
     */
    private static final String STAMP_COMMENT_MAP_FILENAME = "stamp-comment.map";

    /**
     * The Constant WRITE_POOL_SIZE.
     */
    private static final int WRITE_POOL_SIZE = 40;

    private Optional<UUID> dataStoreId = Optional.empty();

    private AtomicReference<Instant> lastCommitTime = new AtomicReference<>(Instant.MIN);

    /**
     * The uncommitted nid lock.
     */
    private final ReentrantLock uncommittedSequenceLock = new ReentrantLock();

    /**
     * The write permit reference.
     */
    private final AtomicReference<Semaphore> writePermitReference
            = new AtomicReference<>(new Semaphore(WRITE_POOL_SIZE));

    /**
     * The write completion service.
     */
    private final WriteCompletionService writeCompletionService = new WriteCompletionService();

    /**
     * The change listeners.
     */
    ConcurrentSkipListSet<WeakReference<ChronologyChangeListener>> changeListeners = new ConcurrentSkipListSet<>();

    ConcurrentSkipListSet<WeakReference<CommitListener>> commitListeners = new ConcurrentSkipListSet<>();


    /**
     * The checkers.
     */
    private final ConcurrentSkipListSet<ChangeChecker> checkers = new ConcurrentSkipListSet<>();

    /**
     * The last commit.
     */
    private long lastCommit = Long.MIN_VALUE;

    /**
     * The deferred import no check nids.
     */
    private AtomicReference<Set<Integer>> deferredImportNoCheckNids = new AtomicReference<>(new ConcurrentSkipListSet<>());

    /**
     * Persistent map of a stamp aliases to a nid.
     */
    private StampAliasMap stampAliasMap;

    /**
     * Persistent map of comments to a stamp.
     */
    private StampCommentMap stampCommentMap;

    /**
     * Persistent nid of database commit actions.
     */
    private final AtomicLong databaseSequence = new AtomicLong();

    /**
     * Persistent stamp nid.
     * TODO: move field to transaction for multi-user environment.
     */
    private final ConcurrentSkipListSet<Integer>  uncommittedConceptsWithChecksNidSet = new ConcurrentSkipListSet<>();

    /**
     * The uncommitted concepts no checks nid set.
     * TODO: move field to transaction for multi-user environment.
     */
    private final ConcurrentSkipListSet<Integer>  uncommittedConceptsNoChecksNidSet = new ConcurrentSkipListSet<>();

    /**
     * The uncommitted semantics with checks nid set.
     * TODO: move field to transaction for multi-user environment.
     */
    private final ConcurrentSkipListSet<Integer>  uncommittedSemanticsWithChecksNidSet = new ConcurrentSkipListSet<>();

    /**
     * The uncommitted semantics no checks nid set.
     * TODO: move field to transaction for multi-user environment.
     */
    private final ConcurrentSkipListSet<Integer>  uncommittedSemanticsNoChecksNidSet = new ConcurrentSkipListSet<>();

    private Set<Task<?>> pendingCommitTasks = ConcurrentHashMap.newKeySet();

    /**
     * The database validity.
     */
    private DataStoreStartState databaseValidity = DataStoreStartState.NOT_YET_CHECKED;

    /**
     * The db folder path.
     */
    private Path dbFolderPath;

    /**
     * The commit manager folder.
     */
    private Path commitManagerFolder;

    private ExtendedStore dataStore = null;
    private ExtendedStoreData<Integer, NidSet> nidStore;
    private static final String NID_STORE_NAME = "CommitProviderNidSetStore";
    private static final int uncommittedConceptsWithChecksNidSetId = 0;
    private static final int uncommittedConceptsNoChecksNidSetId = 1;
    private static final int uncommittedSemanticsWithChecksNidSetId = 2;
    private static final int uncommittedSemanticsNoChecksNidSetId = 3;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new commit provider.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private CommitProvider() {
        //For HK2 construction only
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Adds the alias.
     *
     * @param stampSequence      the stamp nid
     * @param stampAlias         the stamp alias
     * @param aliasCommitComment the alias commit comment
     */
    @Override
    public void addAlias(int stampSequence, int stampAlias, String aliasCommitComment) {
        this.stampAliasMap.addAlias(stampSequence, stampAlias);

        if (aliasCommitComment != null) {
            this.stampCommentMap.addComment(stampAlias, aliasCommitComment);
        }
    }

    /**
     * Adds the change checker.
     *
     * @param checker the checker
     */
    @Override
    public void addChangeChecker(ChangeChecker checker) {
        this.checkers.add(checker);
    }

    /**
     * Due to the use of Weak References in the implementation, you MUST
     * maintain a reference to the change listener that is passed in here,
     * otherwise, it will be rapidly garbage collected, and you will randomly
     * stop getting change notifications!.
     *
     * @param changeListener the change listener
     * @see sh.isaac.api.commit.CommitService#addChangeListener(sh.isaac.api.commit.ChronologyChangeListener)
     */
    @Override
    public void addChangeListener(ChronologyChangeListener changeListener) {
        this.changeListeners.add(new ChangeListenerReference(changeListener));
    }


    public Task<Void> addUncommitted(Transaction transaction, Chronology chronology) {
        checkComponentInTransaction((TransactionImpl) transaction, chronology);
        if (chronology instanceof ConceptChronology) {
            return addUncommitted(transaction, (ConceptChronology) chronology);
        }
        return addUncommitted(transaction, (SemanticChronology) chronology);
    }

    private void checkComponentInTransaction(TransactionImpl transaction, Chronology chronology) {
        if (!transaction.containsComponent(chronology.getNid())) {
            throw new IllegalStateException("Chronology is unknown to transaction: " + chronology);
        }
    }

    /**
     * Adds the uncommitted.
     *
     * @param cc the cc
     * @return the task
     */
    private CheckAndWriteTask addUncommitted(Transaction transaction, ConceptChronology cc) {
        if (cc instanceof ObservableChronologyImpl) {
            cc = (ConceptChronology) ((ObservableChronologyImpl) cc).getWrappedChronology();
        }
        return checkAndWrite(transaction, cc, this.writePermitReference.get());
    }

    /**
     * Adds the uncommitted.
     *
     * @param sc the sc
     * @return the task
     */
     private CheckAndWriteTask addUncommitted(Transaction transaction, SemanticChronology sc) {
        if (sc instanceof ObservableChronologyImpl) {
            sc = (SemanticChronology) ((ObservableChronologyImpl) sc).getWrappedChronology();
        }
        return checkAndWrite(transaction, sc, this.writePermitReference.get());
    }

    /**
     * Adds the uncommitted no checks.
     *
     * @param cc the cc
     * @return the task
     */
    public Task<Void> addUncommittedNoChecks(Transaction transaction, ConceptChronology cc) {
        if (cc instanceof ObservableChronologyImpl) {
            cc = (ConceptChronology) ((ObservableChronologyImpl) cc).getWrappedChronology();
        }
        return write(transaction, cc, this.writePermitReference.get());
    }

    /**
     * Adds the uncommitted no checks.
     *
     * @param sc the sc
     * @return the task
     */
    public Task<Void> addUncommittedNoChecks(Transaction transaction, SemanticChronology sc) {
        if (sc instanceof ObservableChronologyImpl) {
            sc = (SemanticChronology) ((ObservableChronologyImpl) sc).getWrappedChronology();
        }
        return write(transaction, sc, this.writePermitReference.get());
    }

    /**
     * Cancel.
     *
     * @param transaction the transaction
     * @return the task
     * TODO: Consider removal
     */
    protected TimedTask<Void> cancel(TransactionImpl transaction) {
        PendingTransactions.removeTransaction(transaction);
        return Get.stampService()
                .cancel(transaction);
    }

    /**
     * Commit.
     *
     * @param transaction   the transaction for the commit.
     * @param commitComment the commit comment
     * @return the task
     * TODO: Consider removal
     */
    public CommitTask commit(Transaction transaction, String commitComment, ConcurrentSkipListSet<AlertObject> alertCollection) {
        return this.commit(transaction, commitComment, alertCollection, getTimeForCommit());
    }

    public CommitTask commit(Transaction transaction, String commitComment,
                      ConcurrentSkipListSet<AlertObject> alertCollection, Instant commitTime) {
        LOG.debug("Start commit with transaction:\n{}, \n  comment: {}, alert count {}, commitTime {}", transaction, commitComment, alertCollection.size(), commitTime);
        Semaphore pendingWrites = writePermitReference.getAndSet(new Semaphore(WRITE_POOL_SIZE));
        pendingWrites.acquireUninterruptibly(WRITE_POOL_SIZE);

        TransactionCommitTask task = null;
        try {
            this.uncommittedSequenceLock.lock();
            lastCommit = incrementAndGetSequence();

            task = TransactionCommitTask.get(commitComment,
                    this,
                    this.checkers,
                    alertCollection,
                    (TransactionImpl) transaction, commitTime);
            this.pendingCommitTasks.add(task);
            return task;
        } finally {
            this.uncommittedSequenceLock.unlock();
            if (task != null) {
                this.pendingCommitTasks.remove(task);
            }
        }
    }

    /**
     *
     * TODO: Consider removal
     */
    public CommitTask commitObservableVersions(
            Transaction transaction,
            String commitComment, ObservableVersion... versionsToCommit) {

        SingleCommitTask commitTask = new SingleCommitTask(
                transaction,
                commitComment,
                this.checkers,
                this.changeListeners,
                versionsToCommit);
        Get.executor().execute(commitTask);
        return commitTask;
    }

    /**
     * Import no checks.
     *
     * @param isaacExternalizable the isaac externalizable
     */
    @Override
    public void importNoChecks(IsaacExternalizable isaacExternalizable) {
        switch (isaacExternalizable.getIsaacObjectType()) {
            case CONCEPT:
                final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) isaacExternalizable;
                if (conceptChronology.removeUncommittedVersions()) {
                    LOG.warn("Removed uncommitted versions on import from: " + conceptChronology);
                }
                if (!conceptChronology.getVersionList().isEmpty()) {
                    Get.conceptService()
                            .writeConcept(conceptChronology);
                }
                break;

            case SEMANTIC:
                final SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) isaacExternalizable;
                if (semanticChronology.removeUncommittedVersions()) {
                    LOG.warn("Removed uncommitted versions on import from: " + semanticChronology);
                }
                if (!semanticChronology.getVersionList().isEmpty()) {
                    Get.assemblageService()
                            .writeSemanticChronology(semanticChronology);
                    deferNidAction(semanticChronology.getNid());
                }
                break;

            case STAMP_ALIAS:
                final StampAlias stampAlias = (StampAlias) isaacExternalizable;

                this.stampAliasMap.addAlias(stampAlias.getStampSequence(), stampAlias.getStampAlias());
                //TODO [DAN 3] with Filter Alias, I'm not sure on the implications this may have for the index.  There
                //may be a required index update, with a stamp alias....
                break;

            case STAMP_COMMENT:
                final StampComment stampComment = (StampComment) isaacExternalizable;

                this.stampCommentMap.addComment(stampComment.getStampSequence(), stampComment.getComment());
                break;

            default:
                throw new UnsupportedOperationException("ap Can't handle: " + isaacExternalizable.getClass().getName()
                        + ": " + isaacExternalizable);
        }
    }

    @Override
    public Optional<Chronology> importIfContentChanged(IsaacExternalizable isaacExternalizable) {
        switch (isaacExternalizable.getIsaacObjectType()) {
            case CONCEPT: {
                final ConceptChronologyImpl conceptChronology = (ConceptChronologyImpl) isaacExternalizable;
                if (conceptChronology.removeUncommittedVersions()) {
                    LOG.warn("Removed uncommitted versions on import from: " + conceptChronology);
                }
                final Optional<? extends ConceptChronology> optionalExistingChronology = Get.conceptService().getOptionalConcept(conceptChronology.getNid());
                if (optionalExistingChronology.isEmpty()) {
                    Get.conceptService().writeConcept(conceptChronology);
                    return Optional.of(conceptChronology);
                } else {
                    removeDuplicates(optionalExistingChronology, conceptChronology);
                    if (!conceptChronology.getVersionList().isEmpty()) {
                        Get.conceptService().writeConcept(conceptChronology);
                        return Optional.of(conceptChronology);
                    }
                 }
            }
            return Optional.empty();

            case SEMANTIC:
                final SemanticChronologyImpl semanticChronology = (SemanticChronologyImpl) isaacExternalizable;
                if (semanticChronology.removeUncommittedVersions()) {
                    LOG.warn("Removed uncommitted versions on import from: " + semanticChronology);
                }
                final Optional<? extends SemanticChronology> optionalExistingSemantic = Get.assemblageService().getOptionalSemanticChronology(semanticChronology.getNid());
                if (optionalExistingSemantic.isEmpty()) {
                    Get.assemblageService().writeSemanticChronology(semanticChronology);
                    deferNidAction(semanticChronology.getNid());
                    return Optional.of(semanticChronology);
                } else {
                    removeDuplicates(optionalExistingSemantic, semanticChronology);
                    if (!semanticChronology.getVersionList().isEmpty()) {
                        Get.assemblageService().writeSemanticChronology(semanticChronology);
                        deferNidAction(semanticChronology.getNid());
                        return Optional.of(semanticChronology);
                    }
                }
                return Optional.empty();
            case STAMP_ALIAS:
                final StampAlias stampAlias = (StampAlias) isaacExternalizable;

                this.stampAliasMap.addAlias(stampAlias.getStampSequence(), stampAlias.getStampAlias());
                //TODO [DAN 3] with Filter Alias, I'm not sure on the implications this may have for the index.  There
                //may be a required index update, with a stamp alias....
                return Optional.empty();

            case STAMP_COMMENT:
                final StampComment stampComment = (StampComment) isaacExternalizable;

                this.stampCommentMap.addComment(stampComment.getStampSequence(), stampComment.getComment());
                return Optional.empty();

            default:
                throw new UnsupportedOperationException("ap Can't handle: " + isaacExternalizable.getClass().getName()
                        + ": " + isaacExternalizable);
        }

    }

    private void removeDuplicates(Optional<? extends Chronology> optionalExistingChronology, ChronologyImpl newChronology) {
        HashSet<String> existingSamps = new HashSet<>();
        for (Version v : optionalExistingChronology.get().getVersionList()) {
            existingSamps.add(v.getSampKey());
        }
        CopyOnWriteArrayList<Version> versions = newChronology.getCommittedVersionList();
        Iterator<Version> versionIterator = versions.iterator();
        while (versionIterator.hasNext()) {
            Version v = versionIterator.next();
            if (existingSamps.contains(v.getSampKey())) {
                versions.remove(v);
            }
        }
    }

    /**
     * Increment and get commit sequence.
     *
     * @return the commit sequence
     */
    @Override
    public long incrementAndGetSequence() {
        long seq = this.databaseSequence.incrementAndGet();
        if (dataStore != null) {
            dataStore.putSharedStoreLong(DEFAULT_COMMIT_MANAGER_FOLDER + "databaseSequence", this.databaseSequence.get());
        }
        return seq;
    }

    /**
     * Post process import no checks.
     */
    @Override
    public void postProcessImportNoChecks() {
        final Set<Integer> nids = this.deferredImportNoCheckNids.getAndSet(new ConcurrentSkipListSet<>());
        ArrayList<Exception> exceptions = new ArrayList<>();
        if (nids != null) {
            LOG.info("Post processing import. Deferred set size: " + nids.size());


            // update the taxonomy first, in case the description indexer wants to know the taxonomy of a description.
            for (final int nid : nids) {
                IsaacObjectType objectType = Get.identifierService()
                        .getObjectTypeForComponent(nid);
                if (IsaacObjectType.SEMANTIC == objectType) {
                    final SemanticChronology sc = Get.assemblageService()
                            .getSemanticChronology(nid);

                    if (sc.getVersionType() == VersionType.LOGIC_GRAPH) {
                        try {
                            Get.taxonomyService().updateTaxonomy(sc);
                        } catch (RuntimeException e) {
                            LOG.error("While processing: " + sc, e);
                            exceptions.add(e);
                        }
                    }
                } else {
                    Get.identifierService()
                            .getObjectTypeForComponent(nid);
                    LOG.error("Unexpected nid of type: " + objectType +
                            " in deferred set: " + nid + " UUIDs: " + Arrays.toString(Get.identifierService().getUuidArrayForNid(nid)));
                }
            }
            if (Get.configurationService().getGlobalDatastoreConfiguration().enableLuceneIndexes()) {
                ArrayList<Future<Long>> futures = new ArrayList<>();
                List<IndexBuilderService> indexers = Get.services(IndexBuilderService.class);
                for (final int nid : nids) {
                    IsaacObjectType objectType = Get.identifierService()
                            .getObjectTypeForComponent(nid);
                    if (IsaacObjectType.SEMANTIC == objectType) {
                        final SemanticChronology sc = Get.assemblageService()
                                .getSemanticChronology(nid);

                        for (IndexBuilderService ibs : indexers) {
                            futures.add(ibs.index(sc));
                        }

                    } else {
                        Get.identifierService()
                                .getObjectTypeForComponent(nid);
                        LOG.error("Unexpected nid of type: " + objectType +
                                " in deferred set: " + nid + " UUIDs: " + Arrays.toString(Get.identifierService().getUuidArrayForNid(nid)));
                    }
                }
                // wait for all indexing operations to complete
                for (Future<Long> f : futures) {
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error("Unexpected error waiting for index update", e);
                        exceptions.add(e);
                    }
                }

                for (IndexBuilderService ibs : indexers) {
                    try {
                        ibs.sync().get();
                    } catch (Exception e) {
                        LOG.error("Error rebuilding indexes!", e);
                        exceptions.add(e);
                    }
                }
            }
            LOG.info("Post processing import complete");
        }
        if (exceptions.size() > 0) {
            LOG.error("Encountered {} errors during postProcessImportNoChecks", exceptions.size());
            throw new RuntimeException("Errors during import!", exceptions.get(0));
        }
    }

    /**
     * Removes the change checker.
     *
     * @param checker the checker
     */
    @Override
    public void removeChangeChecker(ChangeChecker checker) {
        this.checkers.remove(checker);
    }

    /**
     * Removes the change listener.
     *
     * @param changeListener the change listener
     */
    @Override
    public void removeChangeListener(ChronologyChangeListener changeListener) {
        this.changeListeners.remove(new ChangeListenerReference(changeListener));
    }

    @Override
    public Future<?> sync() {
        return Get.executor().submit(() -> {
            for (Task<?> updateTask : pendingCommitTasks) {
                try {
                    Platform.runLater(() -> LOG.info("Waiting for completion of: " + updateTask.getTitle()));
                    updateTask.get();
                    Platform.runLater(() -> LOG.info("Completed: " + updateTask.getTitle()));
                } catch (Throwable ex) {
                    LOG.error("Error syncing CommitProvider", ex);
                }
            }
            writeData();
            return null;
        });
    }


    /**
     * Associate a comment with a stamp.
     *
     * @param stamp
     * @param commitComment
     */
    public void addComment(int stamp, String commitComment) {
        this.stampCommentMap.addComment(stamp, commitComment);
        LOG.trace("stamp {} comment: {}", stamp, commitComment);
    }

    /**
     * Handle commit notification.
     *
     * @param commitRecord the commit record
     */
    public void handleCommitNotification(CommitRecord commitRecord) {
        this.changeListeners.forEach((listenerRef) -> {
            final ChronologyChangeListener listener = listenerRef.get();

            if (listener == null) {
                this.changeListeners.remove(listenerRef);
            } else {
                listener.handleCommit(commitRecord);
            }
        });
        this.commitListeners.forEach((listenerRef) -> {
            final CommitListener listener = listenerRef.get();

            if (listener == null) {
                this.commitListeners.remove(listenerRef);
            } else {
                listener.handleCommit(commitRecord);
            }
        });
    }

    /**
     * Handle change notification.
     *
     * @param chronology
     */
    public void handleChangeNotification(Chronology chronology) {
        this.changeListeners.forEach((listenerRef) -> {
            final ChronologyChangeListener listener = listenerRef.get();

            if (listener == null) {
                this.changeListeners.remove(listenerRef);
            } else {
                if (chronology instanceof ConceptChronology) {
                    listener.handleChange((ConceptChronology) chronology);
                } else {
                    listener.handleChange((SemanticChronology) chronology);
                }

            }
        });
    }


    /**
     * Check and write.
     *
     * @param cc             the cc
     * @param writeSemaphore the write semaphore
     * @return the task
     */
    private CheckAndWriteTask checkAndWrite(Transaction transaction, ConceptChronology cc,
                                            Semaphore writeSemaphore) {
        return getCheckAndWriteTask(transaction, cc, writeSemaphore);
    }

    private CheckAndWriteTask getCheckAndWriteTask(Transaction transaction, Chronology chronology, Semaphore writeSemaphore) {
        checkComponentInTransaction((TransactionImpl) transaction, chronology);
        writeSemaphore.acquireUninterruptibly();

        try {
            final WriteAndCheckChronicle task = new WriteAndCheckChronicle(
                    transaction,
                    chronology,
                    this.checkers,
                    writeSemaphore,
                    this.changeListeners,
                    (semanticOrConceptChronicle,
                     changeCheckerActive) -> handleUncommittedNidSet(
                            semanticOrConceptChronicle,
                            changeCheckerActive));

            this.writeCompletionService.submit(task);
            return task;
        } catch (Exception e) {
            //release semaphore, if we didn't successfully submit the task
            writeSemaphore.release();
            throw e;
        }
    }

    /**
     * Check and write.
     *
     * @param sc             the sc
     * @param writeSemaphore the write semaphore
     * @return the task
     */
    private CheckAndWriteTask checkAndWrite(Transaction transaction, SemanticChronology sc,
                                            Semaphore writeSemaphore) {
        return getCheckAndWriteTask(transaction, sc, writeSemaphore);
    }

    /**
     * Defer nid action.
     *
     * @param nid the nid
     */
    private void deferNidAction(int nid) {
        Set<Integer> nids = this.deferredImportNoCheckNids.get();
        nids.add(nid);
    }

    /**
     * Handle uncommitted nid set.
     *
     * @param chronicle           the semantic or concept chronicle
     * @param changeCheckerActive the change checker active
     */
    private void handleUncommittedNidSet(Chronology chronicle, boolean changeCheckerActive) {
        if (chronicle instanceof ObservableChronologyImpl) {
            chronicle = ((ObservableChronologyImpl) chronicle).getWrappedChronology();
        }
        try {
            this.uncommittedSequenceLock.lock();

            switch (chronicle.getIsaacObjectType()) {
                case CONCEPT: {
                    final int nid = chronicle.getNid();
                    final ConcurrentSkipListSet<Integer> set = changeCheckerActive ? this.uncommittedConceptsWithChecksNidSet
                            : this.uncommittedConceptsNoChecksNidSet;

                    if (chronicle.isUncommitted()) {
                        set.add(nid);
                    } else {
                        set.remove(nid);
                    }

                    break;
                }

                case SEMANTIC: {
                    final int nid = chronicle.getNid();
                    final ConcurrentSkipListSet<Integer> set = changeCheckerActive ? this.uncommittedSemanticsWithChecksNidSet
                            : this.uncommittedSemanticsNoChecksNidSet;

                    if (chronicle.isUncommitted()) {
                        set.add(nid);
                    } else {
                        set.remove(nid);
                    }

                    break;
                }

                default:
                    throw new RuntimeException("Only Concepts or Semantics should be passed");
            }
        } finally {
            this.uncommittedSequenceLock.unlock();
        }
    }

    /**
     * Start me.
     */
    @PostConstruct
    private void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting chronology provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting CommitProvider post-construct for change to runlevel: " + LookupService.getProceedingToRunLevel());

            ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
            Path dataStorePath = configurationService.getDataStoreFolderPath();

            //Continue using our own local storage in the case of FileSystem data even though the file system store
            //now supports the extended APIs for performance reasons
            if (Get.dataStore().implementsExtendedStoreAPI() && Get.dataStore().getDataStoreType() != DatabaseImplementation.FILESYSTEM) {
                dataStore = (ExtendedStore) Get.dataStore();
                nidStore = dataStore.<Integer, byte[], NidSet>getStore(NID_STORE_NAME,
                        (valueToSerialize) -> valueToSerialize == null ? null : DataToBytesUtils.getBytes(valueToSerialize::write),
                        (valueToDeserialize)
                                -> {
                            try {
                                NidSet ns = new NidSet();
                                if (valueToDeserialize != null) {
                                    ns.read(DataToBytesUtils.getDataInput(valueToDeserialize));
                                }
                                return ns;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                LOG.info("DataStore implements extended API, will be used for Commit provider");
            } else {
                this.dbFolderPath = dataStorePath.resolve("commit-provider");

                Files.createDirectories(this.dbFolderPath);
                this.commitManagerFolder = this.dbFolderPath.resolve(DEFAULT_COMMIT_MANAGER_FOLDER);

                if (!Files.isDirectory(this.commitManagerFolder) || !Files.isRegularFile(this.commitManagerFolder.resolve(COMMIT_MANAGER_DATA_FILENAME))) {
                    this.databaseValidity = DataStoreStartState.NO_DATASTORE;
                } else {
                    this.databaseValidity = DataStoreStartState.EXISTING_DATASTORE;
                    if (this.commitManagerFolder.resolve(DATASTORE_ID_FILE).toFile().isFile()) {
                        dataStoreId = Optional.of(UUID.fromString(new String(Files.readAllBytes(this.commitManagerFolder.resolve(DATASTORE_ID_FILE)))));
                    } else {
                        LOG.warn("No datastore ID in the pre-existing commit service {}", this.commitManagerFolder);
                    }
                }

                Files.createDirectories(this.commitManagerFolder);

                LOG.info("Commit provider starting in {} using local storage", this.commitManagerFolder.toFile().getCanonicalFile().toString());

                if (!this.dataStoreId.isPresent()) {
                    this.dataStoreId = LookupService.get().getService(MetadataService.class).getDataStoreId();
                    Files.write(this.commitManagerFolder.resolve(DATASTORE_ID_FILE), this.dataStoreId.get().toString().getBytes());
                }
            }

            this.lastCommitTime.set(Instant.MIN);
            this.changeListeners.clear();
            this.checkers.clear();
            this.lastCommit = Long.MIN_VALUE;
            this.deferredImportNoCheckNids.get().clear();
            this.databaseSequence.set(0);
            this.uncommittedConceptsWithChecksNidSet.clear();
            this.uncommittedConceptsNoChecksNidSet.clear();
            this.uncommittedSemanticsWithChecksNidSet.clear();
            this.uncommittedSemanticsNoChecksNidSet.clear();
            this.pendingCommitTasks.clear();

            this.writeCompletionService.start();

            if (dataStore == null) {
                stampCommentMap = new StampCommentMap();
                stampAliasMap = new StampAliasMap();

                if (getDataStoreStartState() == DataStoreStartState.EXISTING_DATASTORE) {
                    LOG.info("Reading existing commit manager data from {}", this.commitManagerFolder);
                    LOG.info("Reading " + COMMIT_MANAGER_DATA_FILENAME);

                    try (DataInputStream in
                                 = new DataInputStream(new FileInputStream(new File(this.commitManagerFolder.toFile(),
                            COMMIT_MANAGER_DATA_FILENAME)))) {
                        this.databaseSequence.set(in.readLong());
                        this.uncommittedConceptsWithChecksNidSet.addAll(NidSet.of(in).box());
                        this.uncommittedConceptsNoChecksNidSet.addAll(NidSet.of(in).box());
                        this.uncommittedSemanticsWithChecksNidSet.addAll(NidSet.of(in).box());
                        this.uncommittedSemanticsNoChecksNidSet.addAll(NidSet.of(in).box());
                    }

                    LOG.info("Reading: " + STAMP_ALIAS_MAP_FILENAME);
                    this.stampAliasMap.read(new File(this.commitManagerFolder.toFile(), STAMP_ALIAS_MAP_FILENAME));
                    LOG.info("Reading: " + STAMP_COMMENT_MAP_FILENAME);
                    this.stampCommentMap.read(new File(this.commitManagerFolder.toFile(), STAMP_COMMENT_MAP_FILENAME));
                }
            } else {
                if (getDataStoreStartState() == DataStoreStartState.EXISTING_DATASTORE) {
                    // If a DB is just built by a loader, its possible that the databaseSequence was never incremented, and then, never stored to the data store.
                    // It needs to stay the default value, startup value, in this case.  
                    this.databaseSequence.set(dataStore.getSharedStoreLong(DEFAULT_COMMIT_MANAGER_FOLDER + "databaseSequence").orElse(this.databaseSequence.get()));
                    this.uncommittedConceptsWithChecksNidSet.addAll(nidStore.get(uncommittedConceptsWithChecksNidSetId).box());
                    this.uncommittedConceptsNoChecksNidSet.addAll(nidStore.get(uncommittedConceptsNoChecksNidSetId).box());
                    this.uncommittedSemanticsWithChecksNidSet.addAll(nidStore.get(uncommittedSemanticsWithChecksNidSetId).box());
                    this.uncommittedSemanticsNoChecksNidSet.addAll(nidStore.get(uncommittedSemanticsNoChecksNidSetId).box());
                }
                stampAliasMap = new StampAliasMap(dataStore);  //doen't need to read, it reads live as necessary
                stampCommentMap = new StampCommentMap(dataStore); //doen't need to read, it reads live as necessary

            }

            //This change checker prevents developers from making a mutable, not committing it, then making another mutable
            //of the same object, then committing, then not being able to figure out where their previous changes went.
            checkers.add(new ChangeChecker() {
                @Override
                public Optional<AlertObject> check(Version version, Transaction transaction) {
                    // Accumulate uncommitted versions in passed chronology
                    final List<Version> uncommittedVersions = new ArrayList<>();
                    if (version.getChronology() != null) {
                        for (Version v : version.getChronology().getVersionList()) {
                            if (v.isUncommitted()) {
                                uncommittedVersions.add(v);
                            }
                        }

                    }
                    // Warn or fail if multiple uncommitted versions in passed chronology
                    if (uncommittedVersions.size() > 1) {
                        return Optional.of(new AlertObject("Data loss warning",
                                "Found " + uncommittedVersions.size() + " uncommitted versions in chronology " + version.getPrimordialUuid()
                                + "\n" + version.getChronology(), AlertType.WARNING,
                                AlertCategory.ADD_UNCOMMITTED));
                    }
                    return Optional.empty();
                }

                @Override
                public String getDescription() {
                    return "Warn about multiple uncommitted versions, which, in all use cases to date, has indicated a programming error";
                }
            });

            // This change checker prevents developers from creating a mutable of a description, and then forgetting to set 1 or more of the
            // required fields.
            checkers.add(new ChangeChecker() {
                @Override
                public Optional<AlertObject> check(Version version, Transaction transaction) {
                    if (version.getSemanticType() == VersionType.DESCRIPTION) {
                        if (((DescriptionVersion) version).getCaseSignificanceConceptNid() == 0 || ((DescriptionVersion) version).getLanguageConceptNid() == 0
                                || ((DescriptionVersion) version).getDescriptionTypeConceptNid() == 0 || ((DescriptionVersion) version).getText() == null) {
                            return Optional.of(new AlertObject("Invalid Description", "Failed to set all required fields on a description!", AlertType.ERROR,
                                    AlertCategory.ADD_UNCOMMITTED));
                        }
                    }
                    return Optional.of(new SuccessAlert("Passed change checker", "Passed change checker", AlertCategory.ADD_UNCOMMITTED));
                }

                @Override
                public String getDescription() {
                    return "Raise an error if the description is not structured correctly.";
                }
            });

        } catch (final Exception e) {
            LookupService.getService(SystemStatusService.class)
                    .notifyServiceConfigurationFailure("Commit Provider", e);
            LOG.error("CommitProvider Startup Failure!", e);
            throw new RuntimeException(e);
        } finally {
            progressTask.finished();
        }
    }

    /**
     * Stop me.
     */
    @PreDestroy
    private void stopMe() {
        LOG.info("Stopping CommitProvider pre-destroy for change to runlevel: " + LookupService.getProceedingToRunLevel());

        try {
            StopMeTask stopMeTask = new StopMeTask();
            Get.workExecutors().getExecutor().submit(stopMeTask);
            stopMeTask.get();
        } catch (Exception ex) {
            LOG.error("error stopping commit provider", ex);
            throw new RuntimeException(ex);
        }
        LOG.info("Stopped CommitProvider pre-destroy");
    }

    private class StopMeTask extends TimedTaskWithProgressTracker {
        public StopMeTask() {
            updateTitle("Stopping commit provider");
            addToTotalWork(16);
            Get.activeTasks().add(this);
        }

        @Override
        protected Object call() throws Exception {
            try {
                this.updateMessage("CommitProvider sync");
                sync().get();
                if (nidStore != null) {
                    dataStore.closeStore(NID_STORE_NAME);
                    nidStore = null;
                }
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider write completion service stop");
                CommitProvider.this.writeCompletionService.stop();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider datastore id reset");
                CommitProvider.this.dataStoreId = Optional.empty();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider last commit time reset");
                CommitProvider.this.lastCommitTime.set(Instant.MIN);
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider change listeners reset");
                CommitProvider.this.changeListeners.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider checkers reset");
                CommitProvider.this.checkers.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider last commit reset");
                CommitProvider.this.lastCommit = Long.MIN_VALUE;
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider deferred import no checks reset");
                CommitProvider.this.deferredImportNoCheckNids.get().clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider stamp alias map reset");
                CommitProvider.this.stampAliasMap.shutdown();
                CommitProvider.this.stampAliasMap = null;
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider stamp commit map reset");
                CommitProvider.this.stampCommentMap.shutdown();
                CommitProvider.this.stampCommentMap = null;
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider database sequence reset");
                CommitProvider.this.databaseSequence.set(0);
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider uncommitted concepts with checks");
                CommitProvider.this.uncommittedConceptsWithChecksNidSet.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider uncommitted concepts no checks");
                CommitProvider.this.uncommittedConceptsNoChecksNidSet.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider uncommitted semantics with checks");
                CommitProvider.this.uncommittedSemanticsWithChecksNidSet.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider uncommitted semantics no checks");
                CommitProvider.this.uncommittedSemanticsNoChecksNidSet.clear();
                this.completedUnitOfWork();
                this.updateMessage("CommitProvider pending commit tasks");
                CommitProvider.this.pendingCommitTasks.clear();
                this.completedUnitOfWork();
                CommitProvider.this.dataStore = null;
                return null;
            } finally {
                Get.activeTasks().remove(this);
            }
        }
    }

    private void writeData() throws IOException {
        if (dataStore == null) {
            this.stampAliasMap.write(new File(this.commitManagerFolder.toFile(), STAMP_ALIAS_MAP_FILENAME));
            this.stampCommentMap.write(new File(this.commitManagerFolder.toFile(), STAMP_COMMENT_MAP_FILENAME));

            try (DataOutputStream out = new DataOutputStream(
                    new FileOutputStream(
                            new File(
                                    this.commitManagerFolder.toFile(),
                                    COMMIT_MANAGER_DATA_FILENAME)))) {
                out.writeLong(this.databaseSequence.get());
                NidSet.of(this.uncommittedConceptsWithChecksNidSet.stream()).write(out);
                NidSet.of(this.uncommittedConceptsNoChecksNidSet.stream()).write(out);
                NidSet.of(this.uncommittedSemanticsWithChecksNidSet.stream()).write(out);
                NidSet.of(this.uncommittedSemanticsNoChecksNidSet.stream()).write(out);
            }
        } else {
            //Just need to write the uncommitted nidset data.   Everything else  is written on change in the MV store, 
            nidStore.put(uncommittedConceptsWithChecksNidSetId, NidSet.of(this.uncommittedConceptsWithChecksNidSet.stream()));
            nidStore.put(uncommittedConceptsNoChecksNidSetId, NidSet.of(this.uncommittedConceptsNoChecksNidSet.stream()));
            nidStore.put(uncommittedSemanticsWithChecksNidSetId, NidSet.of(this.uncommittedSemanticsWithChecksNidSet.stream()));
            nidStore.put(uncommittedSemanticsNoChecksNidSetId, NidSet.of(this.uncommittedSemanticsNoChecksNidSet.stream()));
        }
    }

    /**
     * Write.
     *
     * @param cc             the cc
     * @param writeSemaphore the write semaphore
     * @return the task
     */
    private Task<Void> write(Transaction transaction, ConceptChronology cc,
                             Semaphore writeSemaphore) {
        writeSemaphore.acquireUninterruptibly();

        try {
            final WriteConceptChronicle task = new WriteConceptChronicle(cc,
                    writeSemaphore,
                    this.changeListeners,
                    (semanticOrConceptChronicle,
                     changeCheckerActive) -> handleUncommittedNidSet(
                            semanticOrConceptChronicle,
                            changeCheckerActive));

            this.writeCompletionService.submit(task);
            return task;
        } catch (Exception e) {
            //release semaphore, if we didn't successfully submit the task
            writeSemaphore.release();
            throw e;
        }
    }

    /**
     * Write.
     *
     * @param sc             the sc
     * @param writeSemaphore the write semaphore
     * @return the task
     */
    private Task<Void> write(Transaction transaction, SemanticChronology sc,
                             Semaphore writeSemaphore) {
        writeSemaphore.acquireUninterruptibly();
        /*
         * The Class WriteSemanticChronology.
         * TODO: unnecessary overhead in this task. reimplement without task...
         */

        try {
            final WriteSemanticChronology task = new WriteSemanticChronology(sc,
                    writeSemaphore,
                    this.changeListeners,
                    (semanticOrConceptChronicle,
                     changeCheckerActive) -> handleUncommittedNidSet(
                            semanticOrConceptChronicle,
                            changeCheckerActive));

            this.writeCompletionService.submit(task);
            return task;
        } catch (Exception e) {
            //release semaphore, if we didn't successfully submit the task
            writeSemaphore.release();
            throw e;
        }
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the aliases.
     *
     * @param stampSequence the stamp nid
     * @return the aliases
     */
    @Override
    public int[] getAliases(int stampSequence) {
        return this.stampAliasMap.getAliases(stampSequence);
    }

    /**
     * Gets the comment.
     *
     * @param stampSequence the stamp nid
     * @return the comment
     */
    @Override
    public Optional<String> getComment(int stampSequence) {
        return this.stampCommentMap.getComment(stampSequence);
    }

    //~--- set methods ---------------------------------------------------------

    /**
     * Set comment.
     *
     * @param stampSequence the stamp nid
     * @param comment       the comment
     */
    @Override
    public void setComment(int stampSequence, String comment) {
        this.stampCommentMap.addComment(stampSequence, comment);
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the commit manager nid.
     *
     * @return the commit manager nid
     */
    @Override
    public long getCommitManagerSequence() {
        return this.databaseSequence.get();
    }

    /**
     * Gets the database folder.
     *
     * @return the database folder
     */
    @Override
    public Path getDataStorePath() {
        return dataStore == null ? this.commitManagerFolder : dataStore.getDataStorePath();
    }

    @Override
    public Optional<UUID> getDataStoreId() {
        return dataStore == null ? this.dataStoreId : dataStore.getDataStoreId();
    }

    /**
     * Gets the database validity status.
     *
     * @return the database validity status
     */
    @Override
    public DataStoreStartState getDataStoreStartState() {
        return dataStore == null ? this.databaseValidity : dataStore.getDataStoreStartState();
    }

    @Override
    public Stream<StampAlias> getStampAliasStream(boolean parallel) {
        return this.stampAliasMap.getStampAliasStream(parallel);
    }

    public Set<Task<?>> getPendingCommitTasks() {
        return pendingCommitTasks;
    }

    @Override
    public Stream<StampComment> getStampCommentStream(boolean parallel) {
        return this.stampCommentMap.getStampCommentStream(parallel);
    }

    @Override
    public Instant getTimeForCommit() {
        Instant commitTime = Instant.now();

        //Make it impossible to have two commits happen in the same MS - because this messes up the RelativePositionCalculator
        //This could probably be done without a sync block, if I wanted to be clever enough, but I'm sure it won't matter...
        // TODO: sort this out on another day... There is a use case for supporting multiple commits at the same instants.
        // For example, releasing multiple concurrent editions.
        synchronized (lastCommitTime) {
            if (commitTime.isAfter(lastCommitTime.get())) {
                lastCommitTime.set(commitTime);
            } else {
                lastCommitTime.set(lastCommitTime.get().plusMillis(1));
                commitTime = lastCommitTime.get();
            }
        }
        return commitTime;
    }

    /**
     * Gets the uncommitted component text summary.
     *
     * @return the uncommitted component text summary
     */
    @Override
    public String getUncommittedComponentTextSummary() {
        final StringBuilder builder = new StringBuilder("CommitProvider summary: ");

        builder.append("\nuncommitted concepts with checks: ")
                .append(this.uncommittedConceptsWithChecksNidSet);
        builder.append("\nuncommitted concepts no checks: ")
                .append(this.uncommittedConceptsNoChecksNidSet);
        builder.append("\nuncommitted semantics with checks: ")
                .append(this.uncommittedSemanticsWithChecksNidSet);
        builder.append("\nuncommitted semantics no checks: ")
                .append(this.uncommittedSemanticsNoChecksNidSet);
        return builder.toString();
    }

    /**
     * Gets the uncommitted concept nids.
     *
     * @return the uncommitted concept nids
     */
    @Override
    public ObservableList<Integer> getUncommittedConceptNids() {
        // need to create a list that can be backed with a set...
        throw new UnsupportedOperationException(
                "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
    }

    //~--- inner classes -------------------------------------------------------

    private static class CommitListenerReference <T extends CommitListener> extends WeakReference<T>
            implements Comparable<CommitListenerReference> {

        /**
         * The listener uuid.
         */
        UUID listenerUuid;

        public CommitListenerReference(T referent) {
            super(referent);
            this.listenerUuid = referent.getListenerUuid();
        }

        /**
         * Compare to.
         *
         * @param o the o
         * @return the int
         */
        @Override
        public int compareTo(CommitListenerReference o) {
            return this.listenerUuid.compareTo(o.listenerUuid);
        }

        /**
         * Equals.
         *
         * @param obj the obj
         * @return true, if successful
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            final UUID otherUUID = ((CommitListenerReference) obj).listenerUuid;

            return this.listenerUuid.equals(otherUUID);
        }

        /**
         * Hash code.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            int hash = 3;

            hash = 67 * hash + Objects.hashCode(this.listenerUuid);
            return hash;
        }
    }
    /**
     * The Class ChangeListenerReference.
     */
    private static class ChangeListenerReference
            extends CommitListenerReference<ChronologyChangeListener> {

        //~--- constructors -----------------------------------------------------

        /**
         * Instantiates a new change listener reference.
         *
         * @param referent the referent
         */
        public ChangeListenerReference(ChronologyChangeListener referent) {
            super(referent);
        }

        //~--- methods ----------------------------------------------------------

    }

    public ConcurrentSkipListSet<ChangeChecker> getCheckers() {
        return checkers;
    }

    @Override
    public ObservableSet<Transaction> getPendingTransactionList() {
        return PendingTransactions.getPendingTransactionList();
    }

    @Override
    public Transaction newTransaction(Optional<String> transactionName, ChangeCheckerMode changeCheckerMode, boolean indexOnCommit) {
        if (singleTransactionOnly && PendingTransactions.getPendingTransactionCount() > 0) {
            for (Transaction transaction: PendingTransactions.getConcurrentPendingTransactionList()) {
                LOG.info("Pending transaction: " + transaction.getTransactionId());
            }
            throw new IllegalStateException("Second transaction.");
        }
        TransactionImpl transaction = new TransactionImpl(transactionName, changeCheckerMode, indexOnCommit);
        if (logTransactionCreation) {
            logStackTrace(transaction);
        }
        PendingTransactions.addTransaction(transaction);
        return transaction;
    }

    private void logStackTrace(TransactionImpl transaction) {
        StringBuilder sb = new StringBuilder();
        sb.append("New transaction:").append(transaction.getTransactionId()).append("\n");
        for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
            if (element.toString().startsWith("java.base/java.lang.Thread.getStackTrace")) {
                continue;
            }
            if (element.toString().startsWith("org.testng")) {
                break;
            }
            sb.append("   ").append(element).append("\n");
        }
        LOG.info(sb);
    }

    @Override
    public Task<Void> addUncommitted(Transaction transaction, Version version) {
        if (!version.isUncommitted()) {
            throw new RuntimeException("Invalid API pattern!  Cannot call addUncommitted with a stamp that was not created with a transaction");
        }
        transaction.addVersionToTransaction(version);
        if (version.getChronology().getIsaacObjectType() == IsaacObjectType.CONCEPT) {
            return addUncommitted(transaction, (ConceptChronology) version.getChronology());
        }
        return addUncommitted(transaction, (SemanticChronology) version.getChronology());
    }

    @Override
    public void addCommitListener(CommitListener commitListener) {
        this.commitListeners.add(new CommitListenerReference<>(commitListener));
    }

    @Override
    public void removeCommitListener(CommitListener commitListener) {
        this.commitListeners.remove(new CommitListenerReference<>(commitListener));
    }

    @Override
    public void notifyListeners(CommitRecord commitRecord) {
        handleCommitNotification(commitRecord);
    }
}
