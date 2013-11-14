package org.ihtsdo.otf.tcc.datastore;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ExternalStampBI;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.db.DbDependency;
import org.ihtsdo.otf.tcc.api.db.EccsDependency;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.nid.ConcurrentBitSet;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetItrBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.store.TerminologyDI.CONCEPT_EVENT;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.thread.NamedThreadFactory;
import org.ihtsdo.otf.tcc.datastore.id.MemoryCacheBdb;
import org.ihtsdo.otf.tcc.datastore.temp.AceLog;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.change.LastChange;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptDataFetcherI;
import org.ihtsdo.otf.tcc.model.cc.relationship.Relationship;
import org.ihtsdo.otf.tcc.model.cc.termstore.TerminologySnapshot;
import org.ihtsdo.otf.tcc.model.cc.termstore.Termstore;
import org.ihtsdo.otf.tcc.model.cs.CsProperty;

import org.jvnet.hk2.annotations.Service;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import java.io.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service(name = "Bdb Terminology Service")
public class BdbTerminologyStore extends Termstore {
    private static final Logger   LOG                   = Logger.getLogger(BdbTerminologyStore.class.getName());
    public static final String    BDB_LOCATION_PROPERTY = "org.ihtsdo.otf.tcc.datastore.bdb-location";
    public static final String    DEFAULT_BDB_LOCATION  = "berkeley-db";
    private static ViewCoordinate metadataVC            = null;
    private static AtomicBoolean  databaseSetup         = new AtomicBoolean(false);
    private static CountDownLatch setupComplete         = new CountDownLatch(1);
    String                        bdbLocation;

    public BdbTerminologyStore() {
        if (databaseSetup.compareAndSet(false, true)) {
            try {
                bdbLocation = System.getProperty(BDB_LOCATION_PROPERTY);

                if (bdbLocation == null) {
                    bdbLocation = "berkeley-db";
                    LOG.info(BDB_LOCATION_PROPERTY + " not set. Using default location of: " + DEFAULT_BDB_LOCATION);
                } else {
                    LOG.log(Level.INFO, BDB_LOCATION_PROPERTY + " set. Starting from location: {0}", bdbLocation);
                }
                Bdb.selectJeProperties(new File(bdbLocation), new File(bdbLocation));
                Bdb.setup(bdbLocation, this);
                LOG.info("Database setup complete");
                setupComplete.countDown();
            } catch (IOException ex) {
                Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            LOG.info("Database setup already initialized");
        }

        try {
            setupComplete.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void waitForSetup() {
        try {
            setupComplete.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shutdown() {
        try {
            Bdb.close();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addOrigins(Set<Path> paths, Collection<? extends Position> origins) {
        if (origins == null) {
            return;
        }

        for (Position o : origins) {
            paths.add(o.getPath());
            addOrigins(paths, o.getPath().getOrigins());
        }
    }

    @Override
    public void addPropertyChangeListener(CONCEPT_EVENT pce, PropertyChangeListener l) {
        GlobalPropertyChange.addPropertyChangeListener(pce, l);
    }

    @Override
    public void addRelOrigin(int destinationCNid, int originCNid) throws IOException {
        Bdb.addRelOrigin(destinationCNid, originCNid);
    }

    @Override
    public void addUncommitted(ConceptChronicleBI concept) throws IOException {
        BdbCommitManager.addUncommitted(concept);
    }

    @Override
    public void addUncommittedNoChecks(ConceptChronicleBI cc) throws IOException {
        BdbCommitManager.addUncommittedNoChecks(cc);
    }

    @Override
    public void addVetoablePropertyChangeListener(CONCEPT_EVENT pce, VetoableChangeListener l) {
        GlobalPropertyChange.addVetoableChangeListener(pce, l);
    }

    @Override
    public void addXrefPair(int nid, NidPairForRefex pair) throws IOException {
        Bdb.addXrefPair(nid, pair);
    }

    @Override
    public void cancel() {
        BdbCommitManager.cancel();
    }

    @Override
    public void cancel(ConceptChronicleBI concept) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancel(ConceptVersionBI concept) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelAfterCommit(NidSetBI commitSapNids) throws IOException {
        Bdb.getStampDb().cancelAfterCommit(commitSapNids);
    }

    @Override
    public void commit() throws IOException {
        BdbCommitManager.commit(ChangeSetPolicy.MUTABLE_ONLY, ChangeSetWriterThreading.SINGLE_THREAD);
    }

    @Override
    public void commit(ConceptChronicleBI cc) throws IOException {
        BdbCommitManager.commit((ConceptChronicle) cc, ChangeSetPolicy.MUTABLE_ONLY,
                                ChangeSetWriterThreading.SINGLE_THREAD);
    }

    @Override
    public void commit(ConceptVersionBI concept) throws IOException {
        this.commit(concept.getChronicle());
    }

    @Override
    public boolean commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy,
                          ChangeSetWriterThreading changeSetWriterThreading)
            throws IOException {
        return BdbCommitManager.commit((ConceptChronicle) cc, changeSetPolicy, changeSetWriterThreading);
    }

    @Override
    public boolean forget(ConceptAttributeVersionBI attr) throws IOException {
        boolean forgotten = BdbCommitManager.forget(attr);

        if (forgotten) {
            Bdb.getConceptDb().forget((ConceptChronicle) attr.getEnclosingConcept());
        }

        return forgotten;
    }

    @Override
    public void forget(ConceptChronicleBI concept) throws IOException {
        BdbCommitManager.forget(concept);
    }

    @Override
    public void forget(DescriptionVersionBI desc) throws IOException {
        BdbCommitManager.forget(desc);
    }

    @Override
    public void forget(RefexChronicleBI extension) throws IOException {
        BdbCommitManager.forget(extension);
    }

    @Override
    public void forget(RelationshipVersionBI rel) throws IOException {
        BdbCommitManager.forget(rel);
    }

    @Override
    public void forgetXrefPair(int nid, NidPairForRefex pair) {
        Bdb.forgetXrefPair(nid, pair);
    }

    @Override
    public long incrementAndGetSequence() {
        return Bdb.gVersion.incrementAndGet();
    }

    @Override
    public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInParallel(processor);
    }

    @Override
    public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
        Bdb.getConceptDb().iterateConceptDataInSequence(processor);
    }

    @Override
    public void loadEconFiles(java.nio.file.Path[] econFiles) throws Exception {
        File[] files = new File[econFiles.length];

        for (int i = 0; i < files.length; i++) {
            files[i] = econFiles[i].toFile();
        }

        loadEconFiles(files);
    }

    @Override
    public void loadEconFiles(File[] econFiles) throws Exception {
        boolean     consoleFeedback           = true;
        ThreadGroup loadBdbMultiDbThreadGroup = new ThreadGroup(this.getClass().getSimpleName()
                                                    + ".loadEconFiles threads");
        ExecutorService executors = Executors.newCachedThreadPool(new NamedThreadFactory(loadBdbMultiDbThreadGroup,
                                        "converter "));

        try {
            LinkedBlockingQueue<ConceptConverter>     converters                = new LinkedBlockingQueue<>();
            int                                       runtimeConverterSize      =
                Runtime.getRuntime().availableProcessors() * 2;
            int                                       converterSize             = runtimeConverterSize; 
            AtomicInteger                             conceptsRead              = new AtomicInteger();
            AtomicInteger                             conceptsProcessed         = new AtomicInteger();

            for (File conceptsFile : econFiles) {
                System.out.println("Starting load from: " + conceptsFile.getAbsolutePath());
                converters.clear();

                for (int i = 0; i < converterSize; i++) {
                    converters.add(new ConceptConverter(converters, conceptsProcessed));
                }

                FileInputStream     fis = new FileInputStream(conceptsFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                DataInputStream     in  = new DataInputStream(bis);

                try {
                    System.out.print(conceptsRead + "-");

                    while (true) {
                        TtkConceptChronicle eConcept = new TtkConceptChronicle(in);
                        int                 read     = conceptsRead.incrementAndGet();

                        if (consoleFeedback && (read % 100 == 0)) {
                            if (read % 8000 == 0) {
                                System.out.println('.');
                                System.out.print(read + "-");
                            } else {
                                System.out.print('.');
                            }
                        }

                        ConceptConverter conceptConverter = converters.take();

                        try {
                            conceptConverter.setEConcept(eConcept);
                        } catch (Throwable ex) {
                            throw new IOException(ex);
                        }

                        executors.execute(conceptConverter);
                    }
                } catch (EOFException e) {
                    in.close();
                }

                // See if any exceptions in the last converters;
                while (converters.isEmpty() == false) {
                    ConceptConverter conceptConverter = converters.take();

                    try {
                        conceptConverter.setEConcept(null);
                    } catch (Throwable ex) {
                        Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                while (conceptsProcessed.get() < conceptsRead.get()) {
                    Thread.sleep(1000);
                }

                System.out.println("\nFinished load of " + conceptsRead + " concepts from: "
                                   + conceptsFile.getAbsolutePath());
            }
        } finally {
            executors.shutdown();
        }

//      ViewCoordinate vc = StandardViewCoordinates.getSnomedInferredLatest();
//
//      vc.setRelAssertionType(RelAssertionType.STATED);
//
//      EditCoordinate ec = new EditCoordinate(TermAux.USER.getNid(), DescriptionLogicBinding.DL_MODULE.getNid(),
//                             Snomed.SNOMED_RELEASE_PATH.getNid());
//
//      // Convert to new form.
//      SnomedToLogicTree converter = new SnomedToLogicTree(vc, ec);
//      long             time      = System.currentTimeMillis();
//
//      System.out.println(TimeHelper.formatDate(time));
//      Ts.get().iterateConceptDataInParallel(converter);
//      Ts.get().commit();
//      System.out.println("Conversion time: "
//                         + TimeHelper.getElapsedTimeString(System.currentTimeMillis() - time));

        System.out.println("Starting db sync.");
        Bdb.sync();
        System.out.println("Finished db sync.");
        Bdb.commit();
    }

    @Override
    public void put(UUID uuid, int nid) {
        Bdb.getUuidsToNidMap().put(uuid, nid);
    }

    @Override
    public void putViewCoordinate(ViewCoordinate vc) throws IOException {
        Bdb.putViewCoordinate(vc);
    }

    @Override
    public void resetConceptNidForNid(int cNid, int nid) throws IOException {
        Bdb.getMemoryCache().resetCNidForNid(cNid, nid);
    }

    @Override
    public void resumeChangeNotifications() {
        LastChange.resumeChangeNotifications();
    }

    @Override
    public boolean satisfiesDependencies(Collection<DbDependency> dependencies) {
        if (dependencies != null) {
            try {
                for (DbDependency d : dependencies) {
                    String value = P.s.getProperty(d.getKey());

                    if (d.satisfactoryValue(value) == false) {
                        return false;
                    }
                }
            } catch (Throwable e) {
                AceLog.getAppLog().alertAndLogException(e);

                return false;
            }
        }

        return true;
    }

    @Override
    public void suspendChangeNotifications() {
        LastChange.suspendChangeNotifications();
    }

    public int uuidsToNid(Collection<UUID> uuids) throws IOException {
        return Bdb.uuidsToNid(uuids);
    }

    public int uuidsToNid(UUID... uuids) throws IOException {
        return Bdb.uuidToNid(uuids);
    }

    @Override
    public void waitTillWritesFinished() {
        BdbCommitManager.waitTillWritesFinished();
    }

    @Override
    public NativeIdSetBI getAllConceptNids() throws IOException {
        return Bdb.getConceptDb().getConceptNidSet();
    }

    @Override
    public NativeIdSetBI getAllConceptNidsFromCache() throws IOException {
        return Bdb.getMemoryCache().getAllConceptNidsFromCache();
    }

    @Override
    public int getAuthorNidForStamp(int sapNid) {
        return Bdb.getAuthorNidForSapNid(sapNid);
    }

    @Override
    public int getConceptCount() throws IOException {
        return Bdb.getConceptDb().getCount();
    }

    @Override
    public ConceptDataFetcherI getConceptDataFetcher(int cNid) throws IOException {
        return new NidDataFromBdb(cNid);
    }

    @Override
    public int getConceptNidForNid(int nid) {
        return Bdb.getConceptNid(nid);
    }

    @Override
    public int[] getDestRelOriginNids(int cNid) throws IOException {
        return Bdb.getMemoryCache().getDestRelNids(cNid);
    }

    @Override
    public int[] getDestRelOriginNids(int cNid, NidSetBI relTypes) throws IOException {
        return Bdb.getMemoryCache().getDestRelNids(cNid, relTypes);
    }

    @Override
    public Collection<Relationship> getDestRels(int cNid) throws IOException {
        return Bdb.getMemoryCache().getDestRels(cNid);
    }

    @Override
    public NativeIdSetBI getEmptyNidSet() throws IOException {
        return Bdb.getConceptDb().getEmptyIdSet();
    }

    @Override
    public ConceptChronicleDdo getFxConcept(UUID conceptUUID, ViewCoordinate vc)
            throws IOException, ContradictionException {
        TerminologySnapshotDI ts = getSnapshot(vc);
        ConceptVersionBI      c  = ts.getConceptVersion(conceptUUID);

        return new ConceptChronicleDdo(ts, c, VersionPolicy.ALL_VERSIONS, RefexPolicy.REFEX_MEMBERS,
                                       RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
    }

    @Override
    public ConceptChronicleDdo getFxConcept(ComponentReference ref, UUID viewCoordinateUuid,
            VersionPolicy versionPolicy, RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy)
            throws IOException, ContradictionException {
        TerminologySnapshotDI ts = getSnapshot(getViewCoordinate(viewCoordinateUuid));
        ConceptVersionBI      c;

        if (ref.getNid() != Integer.MAX_VALUE) {
            c = ts.getConceptVersion(ref.getNid());
        } else {
            c = ts.getConceptVersion(ref.getUuid());
        }

        return new ConceptChronicleDdo(ts, c, versionPolicy, refexPolicy, relationshipPolicy);
    }

    @Override
    public ConceptChronicleDdo getFxConcept(ComponentReference ref, ViewCoordinate vc, VersionPolicy versionPolicy,
            RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy)
            throws IOException, ContradictionException {
        TerminologySnapshotDI ts = getSnapshot(vc);
        ConceptVersionBI      c;

        if (ref.getNid() != Integer.MAX_VALUE) {
            c = ts.getConceptVersion(ref.getNid());
        } else {
            c = ts.getConceptVersion(ref.getUuid());
        }

        return new ConceptChronicleDdo(ts, c, versionPolicy, refexPolicy, relationshipPolicy);
    }

    @Override
    public ConceptChronicleDdo getFxConcept(UUID conceptUUID, UUID viewCoordinateUuid, VersionPolicy versionPolicy,
            RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy)
            throws IOException, ContradictionException {
        TerminologySnapshotDI ts = getSnapshot(getViewCoordinate(viewCoordinateUuid));
        ConceptVersionBI      c  = ts.getConceptVersion(conceptUUID);

        return new ConceptChronicleDdo(ts, c, versionPolicy, refexPolicy, relationshipPolicy);
    }

    @Override
    public ConceptChronicleDdo getFxConcept(UUID conceptUUID, ViewCoordinate vc, VersionPolicy versionPolicy,
            RefexPolicy refexPolicy, RelationshipPolicy relationshipPolicy)
            throws IOException, ContradictionException {
        TerminologySnapshotDI ts = getSnapshot(vc);
        ConceptVersionBI      c  = ts.getConceptVersion(conceptUUID);

        return new ConceptChronicleDdo(ts, c, versionPolicy, refexPolicy, relationshipPolicy);
    }

    @Override
    public long getLastCancel() {
        return BdbCommitManager.getLastCancel();
    }

    @Override
    public long getLastCommit() {
        return BdbCommitManager.getLastCommit();
    }

    @Override
    public Collection<DbDependency> getLatestChangeSetDependencies() throws IOException {
        CsProperty[] keysToCheck = new CsProperty[] { CsProperty.LAST_CHANGE_SET_WRITTEN,
                CsProperty.LAST_CHANGE_SET_READ };
        List<DbDependency> latestDependencies = new ArrayList<>(2);

        for (CsProperty prop : keysToCheck) {
            String value = Bdb.getProperty(prop.toString());

            if (value != null) {
                String changeSetName = value;
                String changeSetSize = Bdb.getProperty(changeSetName);

                latestDependencies.add(new EccsDependency(changeSetName, changeSetSize));
            }
        }

        return latestDependencies;
    }

    @Override
    public int getMaxReadOnlyStamp() {
        return Bdb.getStampDb().getReadOnlyMax();
    }

    @Override
    public ViewCoordinate getMetadataVC() throws IOException {
        if (metadataVC == null) {
            metadataVC = makeMetaVc();
            Bdb.putViewCoordinate(metadataVC);
        }

        return metadataVC;
    }

    @Override
    public int getModuleNidForStamp(int sapNid) {
        return Bdb.getModuleNidForSapNid(sapNid);
    }

    @Override
    public int getNidForUuids(Collection<UUID> uuids) throws IOException {
        return Bdb.uuidsToNid(uuids);
    }

    @Override
    public int getNidForUuids(UUID... uuids) throws IOException {
        return Bdb.uuidToNid(uuids);
    }

    @Override
    public Path getPath(int pathNid) throws IOException {
        return BdbPathManager.get().get(pathNid);
    }

    @Override
    public List<? extends Path> getPathChildren(int nid) {
        return BdbPathManager.get().getPathChildren(nid);
    }

    @Override
    public int getPathNidForStamp(int sapNid) {
        return Bdb.getPathNidForSapNid(sapNid);
    }

    @Override
    public Set<Path> getPathSetFromPositionSet(Set<Position> positions) throws IOException {
        HashSet<Path> paths = new HashSet<>(positions.size());

        for (Position position : positions) {
            paths.add(position.getPath());

            // addOrigins(paths, position.getPath().getInheritedOrigins());
        }

        return paths;
    }

    @Override
    public Set<Path> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException {
        HashSet<Path> paths = new HashSet<>(sapNids.size());

        for (int sap : sapNids) {
            Path path = Bdb.getStampDb().getPosition(sap).getPath();

            paths.add(path);
            addOrigins(paths, path.getOrigins());
        }

        return paths;
    }

    @Override
    public Set<Position> getPositionSet(Set<Integer> sapNids) throws IOException {
        HashSet<Position> positions = new HashSet<>(sapNids.size());

        for (int sap : sapNids) {
            if (sap >= 0) {
                positions.add(Bdb.getStampDb().getPosition(sap));
            }
        }

        return positions;
    }

    @Override
    public int[] getPossibleChildren(int parentNid, ViewCoordinate vc) throws IOException {
        throw new UnsupportedOperationException("needs to get concept nids, not rel nids");

        // return Bdb.getMemoryCache().getDestRelNids(parentNid, vc);
    }

    @Override
    public Map<String, String> getProperties() throws IOException {
        return Bdb.getProperties();
    }

    @Override
    public String getProperty(String key) throws IOException {
        return Bdb.getProperty(key);
    }

    @Override
    public List<NidPairForRefex> getRefexPairs(int nid) {
        return Bdb.getRefsetPairs(nid);
    }

    @Override
    public long getSequence() {
        return Bdb.gVersion.incrementAndGet();
    }

    @Override
    public TerminologySnapshotDI getSnapshot(ViewCoordinate c) {
        assert c != null;

        return new TerminologySnapshot(this, c);
    }

    @Override
    public int getStamp(ExternalStampBI version) {
        return Bdb.getStamp(version);
    }

    @Override
    public int getStamp(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        return Bdb.getStampDb().getStamp(status, time, authorNid, moduleNid, pathNid);
    }

    @Override
    public Status getStatusForStamp(int stamp) {
        return Bdb.getStatusForStamp(stamp);
    }

    @Override
    public TerminologyBuilderBI getTerminologyBuilder(EditCoordinate ec, ViewCoordinate vc) {
        return new BdbTermBuilder(ec, vc);
    }

    @Override
    public long getTimeForStamp(int sapNid) {
        return Bdb.getTimeForSapNid(sapNid);
    }

    @Override
    public Collection<? extends ConceptChronicleBI> getUncommittedConcepts() {
        return BdbCommitManager.getUncommitted();
    }

    @Override
    public UUID getUuidPrimordialForNid(int nid) throws IOException {
        ComponentChronicleBI<?> c = getComponent(nid);

        if (c != null) {
            return c.getPrimordialUuid();
        }

        return UUID.fromString("00000000-0000-0000-C000-000000000046");
    }

    @Override
    public List<UUID> getUuidsForNid(int nid) throws IOException {
        return Bdb.getUuidsToNidMap().getUuidsForNid(nid);
    }

    @Override
    public ViewCoordinate getViewCoordinate(UUID vcUuid) throws IOException {
        return Bdb.getViewCoordinate(vcUuid);
    }

    @Override
    public Collection<ViewCoordinate> getViewCoordinates() throws IOException {
        return Bdb.getViewCoordinates();
    }

    @Override
    public boolean hasConcept(int cNid) throws IOException {
        return Bdb.isConcept(cNid);
    }

    @Override
    public boolean hasPath(int nid) throws IOException {
        return BdbPathManager.get().hasPath(nid);
    }

    @Override
    public boolean hasUncommittedChanges() {
        if (BdbCommitManager.getUncommitted().isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasUuid(List<UUID> memberUUIDs) {
        assert memberUUIDs != null;

        for (UUID uuid : memberUUIDs) {
            if (Bdb.hasUuid(uuid)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasUuid(UUID memberUUID) {
        assert memberUUID != null;

        return Bdb.hasUuid(memberUUID);
    }

    @Override
    public boolean isKindOf(int childNid, int parentNid, ViewCoordinate vc) throws IOException, ContradictionException {
        return Bdb.getMemoryCache().isKindOf(childNid, parentNid, vc);
    }

    @Override
    public NativeIdSetBI isChildOfSet(int parentNid, ViewCoordinate vc) {
        try {
            return Bdb.getMemoryCache().isChildOfSet(parentNid, vc);
        } catch (IOException | ContradictionException ex) {
            Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public NativeIdSetBI isKindOfSet(int parentNid, ViewCoordinate vc) {
        try {
            return Bdb.getMemoryCache().isKindOfSet(parentNid, vc);
        } catch (IOException | ContradictionException ex) {
            Logger.getLogger(BdbTerminologyStore.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void setConceptNidForNid(int cNid, int nid) throws IOException {
        Bdb.getMemoryCache().setCNidForNid(cNid, nid);
    }

    @Override
    public void setProperty(String key, String value) throws IOException {
        Bdb.setProperty(key, value);
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid, ViewCoordinate vc)
            throws IOException, ContradictionException {
        return Bdb.getMemoryCache().isChildOf(childNid, parentNid, vc);
    }

    @Override
    public NativeIdSetBI getConceptNidsForComponentNids(NativeIdSetBI componentNativeIds) throws IOException {
        NativeIdSetItrBI iter    = componentNativeIds.getSetBitIterator();
        NativeIdSetBI    cNidSet = new ConcurrentBitSet();

        while (iter.next()) {
            int nid = Bdb.getMemoryCache().getCNid(iter.nid());
            if(nid > 0){
                nid = -1 * nid;
            }
            cNidSet.add(nid);
        }

        return cNidSet;
    }

    @Override
    public NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNativeIds) throws IOException {
        return Bdb.getMemoryCache().getComponentNidsForConceptNids(conceptNativeIds);
    }

    @Override
    public NativeIdSetBI getOrphanNids(NativeIdSetBI conceptNativeIds) throws IOException {
        return Bdb.getMemoryCache().getOrphanNids(conceptNativeIds);
    }

    @Override
    public NativeIdSetBI relationshipSet(int parentNid, ViewCoordinate viewCoordinate) {

        // Bdb.getMemoryCache().getDestRels(parentNid);
        // åBdb.getMemoryCache().getDestRelNids(parentNid, null)
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public NativeIdSetBI getAllComponentNids() throws IOException {
        return Bdb.getConceptDb().getAllComponentNids();
    }

    @Override
    public void setIndexed(int nid, boolean indexed) {
        Bdb.setIndexed(nid, indexed);
    }

    @Override
    public boolean isIndexed(int nid) {
        return Bdb.isIndexed(nid);
    }

    /*
     * @Override
     * public Collection<Integer> searchLuceneRefset(String query, SearchType searchType) throws IOException, ParseException {
     *   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     * }
     */
    private static class ConceptConverter implements Runnable {
        TtkConceptChronicle                       eConcept   = null;
        Throwable                                 exception  = null;
        ConceptChronicle                          newConcept = null;
        AtomicInteger                             conceptsProcessed;
        LinkedBlockingQueue<ConceptConverter>     converters;
        MemoryCacheBdb                            nidCnidMap;

        public ConceptConverter(LinkedBlockingQueue<ConceptConverter> converters, AtomicInteger conceptsRead) {
            this.converters                = converters;
            this.conceptsProcessed         = conceptsRead;
        }

        @Override
        public void run() {
            if (nidCnidMap == null) {
                nidCnidMap = Bdb.getMemoryCache();
            }

            try {
                newConcept = ConceptChronicle.get(eConcept);

                if (newConcept != null) {
                    assert newConcept.readyToWrite();
                    Bdb.getConceptDb().writeConcept(newConcept);

                    Collection<Integer> nids = newConcept.getAllNids();

                    assert nidCnidMap.getCNid(newConcept.getNid()) == newConcept.getNid();

                    for (int nid : nids) {
                        assert nidCnidMap.getCNid(nid) == newConcept.getNid();
                    }
                }

                conceptsProcessed.incrementAndGet();
            } catch (Throwable e) {
                exception = e;
            }

            converters.add(this);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.ihtsdo.db.bdb.I_ProcessEConcept#setEConcept(org.ihtsdo.etypes .EConcept)
         */
        public void setEConcept(TtkConceptChronicle eConcept) throws Throwable {
            if (exception != null) {
                throw exception;
            }

            this.eConcept = eConcept;
        }
    }
}
