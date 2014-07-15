package org.ihtsdo.otf.tcc.model.cc.termstore;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.chronicle.ComponentContainerBI;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptContainerBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Path;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.concept.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.otf.tcc.api.store.TermChangeListener;
import org.ihtsdo.otf.tcc.api.blueprint.TerminologyBuilderBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.db.DbDependency;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptVersion;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RefexPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.RelationshipPolicy;
import org.ihtsdo.otf.tcc.ddo.fetchpolicy.VersionPolicy;
import org.ihtsdo.otf.tcc.ddo.store.FxTerminologySnapshotDI;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import java.io.File;
import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.nid.NativeIdSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.uuid.UuidFactory;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/03/29
 * @author         Enter your name here...    
 */
public class TerminologySnapshot implements TerminologySnapshotDI, FxTerminologySnapshotDI {

   /** Field description */
   private PersistentStoreI store;

   /** Field description */
   private ViewCoordinate vc;

   /**
    * Constructs ...
    *
    *
    * @param store
    * @param coordinate
    */
   public TerminologySnapshot(PersistentStoreI store, ViewCoordinate coordinate) {
      super();
      this.store = store;
      this.vc    = coordinate;
   }

   /**
    * Method description
    *
    *
    * @param key
    * @param writer
    */
   @Override
   public void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer) {
      store.addChangeSetGenerator(key, writer);
   }

   /**
    * Method description
    *
    *
    * @param pce
    * @param l
    */
   @Override
   public void addPropertyChangeListener(CONCEPT_EVENT pce, PropertyChangeListener l) {
      store.addPropertyChangeListener(pce, l);
   }

   /**
    * Method description
    *
    *
    * @param cl
    */
   @Override
   public void addTermChangeListener(TermChangeListener cl) {
      store.addTermChangeListener(cl);
   }

   /**
    * Method description
    *
    *
    * @param concept
    *
    * @throws IOException
    */
   @Override
   public void addUncommitted(ConceptChronicleBI concept) throws IOException {
      store.addUncommitted(concept);
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws IOException
    */
   @Override
   public void addUncommitted(ConceptVersionBI cv) throws IOException {
      store.addUncommitted(cv);
   }

   /**
    * Method description
    *
    *
    * @param cc
    *
    * @throws IOException
    */
   @Override
   public void addUncommittedNoChecks(ConceptChronicleBI cc) throws IOException {
      store.addUncommittedNoChecks(cc);
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws IOException
    */
   @Override
   public void addUncommittedNoChecks(ConceptVersionBI cv) throws IOException {
      store.addUncommittedNoChecks(cv);
   }

   /**
    * Method description
    *
    *
    * @param pce
    * @param l
    */
   @Override
   public void addVetoablePropertyChangeListener(CONCEPT_EVENT pce, VetoableChangeListener l) {
      store.addVetoablePropertyChangeListener(pce, l);
   }

   /**
    * Method description
    *
    *
    * @throws IOException
    */
   @Override
   public void cancel() throws IOException {
      store.cancel();
   }

   /**
    * Method description
    *
    *
    * @param cc
    *
    * @throws IOException
    */
   @Override
   public void cancel(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException();
   }

   /**
    * Method description
    *
    *
    * @param concept
    *
    * @throws IOException
    */
   @Override
   public void cancel(ConceptVersionBI concept) throws IOException {
      throw new UnsupportedOperationException();
   }

   /**
    * Method description
    *
    *
    * @throws IOException
    */
   @Override
   public void commit() throws IOException {
      store.commit();
   }

   /**
    * Method description
    *
    *
    * @param cc
    *
    * @throws IOException
    */
   @Override
   public void commit(ConceptChronicleBI cc) throws IOException {
      throw new UnsupportedOperationException();
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws IOException
    */
   @Override
   public void commit(ConceptVersionBI cv) throws IOException {
      commit(cv);
   }

   /**
    * Method description
    *
    *
    * @param changeSetFileName
    * @param changeSetTempFileName
    * @param policy
    *
    * @return
    */
   @Override
   public ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
       File changeSetTempFileName, ChangeSetGenerationPolicy policy) {
      return store.createDtoChangeSetGenerator(changeSetFileName, changeSetTempFileName, policy);
   }

   /**
    * Method description
    *
    *
    * @param attr
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public boolean forget(ConceptAttributeVersionBI attr) throws IOException {
      return store.forget(attr);
   }

   /**
    * Method description
    *
    *
    * @param concept
    *
    * @throws IOException
    */
   @Override
   public void forget(ConceptChronicleBI concept) throws IOException {
      store.forget(concept);
   }

   /**
    * Method description
    *
    *
    * @param desc
    *
    * @throws IOException
    */
   @Override
   public void forget(DescriptionVersionBI desc) throws IOException {
      store.forget(desc);
   }

   /**
    * Method description
    *
    *
    * @param extension
    *
    * @throws IOException
    */
   @Override
   public void forget(RefexChronicleBI extension) throws IOException {
      store.forget(extension);
   }

   /**
    * Method description
    *
    *
    * @param rel
    *
    * @throws IOException
    */
   @Override
   public void forget(RelationshipVersionBI rel) throws IOException {
      store.forget(rel);
   }

   /**
    * Method description
    *
    *
    * @param nid
    *
    * @return
    */
   @Override
   public CharSequence informAboutNid(int nid) {
      return store.informAboutNid(nid);
   }

   /**
    * Method description
    *
    *
    * @param uuid
    *
    * @return
    */
   @Override
   public CharSequence informAboutUuid(UUID uuid) {
      return store.informAboutUuid(uuid);
   }

   /**
    * Method description
    *
    *
    * @param processor
    *
    * @throws Exception
    */
   @Override
   public void iterateConceptDataInParallel(ProcessUnfetchedConceptDataBI processor) throws Exception {
      store.iterateConceptDataInParallel(processor);
   }

   /**
    * Method description
    *
    *
    * @param processor
    *
    * @throws Exception
    */
   @Override
   public void iterateConceptDataInSequence(ProcessUnfetchedConceptDataBI processor) throws Exception {
      store.iterateConceptDataInSequence(processor);
   }

   /**
    * Method description
    *
    *
    * @param econFiles
    *
    * @throws Exception
    */
   @Override
   public void loadEconFiles(File[] econFiles) throws Exception {
      store.loadEconFiles(econFiles);
   }

   @Override
   public void loadEconFiles(java.nio.file.Path[] econFiles) throws Exception {
      store.loadEconFiles(econFiles);
   }

   /**
    * Method description
    *
    *
    * @param econFileStrings
    *
    * @throws Exception
    */
   @Override
   public void loadEconFiles(String[] econFileStrings) throws Exception {
      store.loadEconFiles(econFileStrings);
   }

   /**
    * Method description
    *
    *
    * @param path
    * @param time
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Position newPosition(Path path, long time) throws IOException {
      return store.newPosition(path, time);
   }

   /**
    * Method description
    *
    *
    * @param key
    */
   @Override
   public void removeChangeSetGenerator(String key) {
      store.removeChangeSetGenerator(key);
   }

   /**
    * Method description
    *
    *
    * @param cl
    */
   @Override
   public void removeTermChangeListener(TermChangeListener cl) {
      store.removeTermChangeListener(cl);
   }

   /**
    * Method description
    *
    */
   @Override
   public void resumeChangeNotifications() {
      store.resumeChangeNotifications();
   }

   /**
    * Method description
    *
    *
    * @param dependencies
    *
    * @return
    */
   @Override
   public boolean satisfiesDependencies(Collection<DbDependency> dependencies) {
      return store.satisfiesDependencies(dependencies);
   }

   /**
    * Method description
    *
    */
   @Override
   public void suspendChangeNotifications() {
      store.suspendChangeNotifications();
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public NativeIdSetBI getAllConceptNids() throws IOException {
      return store.getAllConceptNids();
   }

   /**
    * Method description
    *
    *
    * @param sapNid
    *
    * @return
    */
   @Override
   public int getAuthorNidForStamp(int sapNid) {
      return store.getAuthorNidForStamp(sapNid);
   }

   /**
    * Method description
    *
    *
    * @param ec
    *
    * @return
    */
   @Override
   public TerminologyBuilderBI getBuilder(EditCoordinate ec) {
      return store.getTerminologyBuilder(ec, vc);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ComponentVersionBI getComponentVersion(Collection<UUID> uuids)
           throws IOException, ContradictionException {
      return store.getComponentVersion(vc, uuids);
   }

   /**
    * Method description
    *
    *
    * @param cc
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ComponentVersionBI getComponentVersion(ComponentContainerBI cc)
           throws IOException, ContradictionException {
      return getComponentVersion(cc.getNid());
   }

   /**
    * Method description
    *
    *
    * @param nid
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException {
      return store.getComponentVersion(vc, nid);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException {
      return store.getComponentVersion(vc, uuids);
   }

   /**
    * Method description
    *
    *
    * @param nid
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ConceptVersionBI getConceptForNid(int nid) throws IOException {
      return new ConceptVersion((ConceptChronicle) store.getConceptForNid(nid), vc);
   }

   /**
    * Method description
    *
    *
    * @param nid
    *
    * @return
    */
   @Override
   public int getConceptNidForNid(int nid) {
      return PersistentStore.get().getConceptNidForNid(nid);
   }

   /**
    * Method description
    *
    *
    * @param nid
    *
    * @return
    */
   @Override
   public int getConceptNidForNid(Integer nid) {
      return store.getConceptNidForNid(nid);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException {
      return new ConceptVersion((ConceptChronicle) store.getConcept(store.getNidForUuids(uuids)), vc);
   }

   /**
    * Method description
    *
    *
    * @param cc
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ConceptVersionBI getConceptVersion(ConceptContainerBI cc) throws IOException {
      return getConceptVersion(cc.getCnid());
   }

   /**
    * Method description
    *
    *
    * @param cNid
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ConceptVersionBI getConceptVersion(int cNid) throws IOException {
      return new ConceptVersion((ConceptChronicle) store.getConcept(cNid), vc);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException {
      return new ConceptVersion((ConceptChronicle) store.getConcept(store.getNidForUuids(uuids)), vc);
   }

   /**
    * Method description
    *
    *
    * @param cNids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Map<Integer, ConceptVersionBI> getConceptVersions(NativeIdSetBI cNids) throws IOException {
      return store.getConceptVersions(vc, cNids);
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public NativeIdSetBI getEmptyNidSet() throws IOException {
      return store.getEmptyNidSet();
   }

   /**
    * Method description
    *
    *
    * @param conceptUUID
    * @param vc
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ConceptChronicleDdo getFxConcept(UUID conceptUUID, ViewCoordinate vc)
           throws IOException, ContradictionException {
      ConceptVersionBI c = getConceptVersion(conceptUUID);

      return new ConceptChronicleDdo(this, c, VersionPolicy.ACTIVE_VERSIONS, RefexPolicy.REFEX_MEMBERS,
                           RelationshipPolicy.ORIGINATING_RELATIONSHIPS);
   }

   /**
    * Method description
    *
    *
    * @param ref
    * @param refexPolicy
    * @param relationshipPolicy
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ConceptChronicleDdo getFxConcept(ComponentReference ref, RefexPolicy refexPolicy,
                                 RelationshipPolicy relationshipPolicy)
           throws IOException, ContradictionException {
      ConceptVersionBI c;

      if (ref.getNid() != Integer.MAX_VALUE) {
         c = getConceptVersion(ref.getNid());
      } else {
         c = getConceptVersion(ref.getUuid());
      }

      return new ConceptChronicleDdo(this, c, VersionPolicy.ACTIVE_VERSIONS, refexPolicy, relationshipPolicy);
   }

   /**
    * Method description
    *
    *
    * @param conceptUUID
    * @param refexPolicy
    * @param relationshipPolicy
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public ConceptChronicleDdo getFxConcept(UUID conceptUUID, RefexPolicy refexPolicy,
                                 RelationshipPolicy relationshipPolicy)
           throws IOException, ContradictionException {
      ConceptVersionBI c = getConceptVersion(conceptUUID);

      return new ConceptChronicleDdo(this, c, VersionPolicy.ACTIVE_VERSIONS, refexPolicy, relationshipPolicy);
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public ViewCoordinate getMetadataVC() throws IOException {
      return store.getMetadataVC();
   }

   /**
    * Method description
    *
    *
    * @param sapNid
    *
    * @return
    */
   @Override
   public int getModuleNidForStamp(int sapNid) {
      return store.getModuleNidForStamp(sapNid);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Collection<Integer> getNidCollection(Collection<UUID> uuids) throws IOException {
      return PersistentStore.get().getNidCollection(uuids);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public int getNidForUuids(Collection<UUID> uuids) throws IOException {
      return PersistentStore.get().getNidForUuids(uuids);
   }

   /**
    * Method description
    *
    *
    * @param uuids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public int getNidForUuids(UUID... uuids) throws IOException {
      return PersistentStore.get().getNidForUuids(uuids);
   }

   /**
    * Method description
    *
    *
    * @param authorityUuid
    * @param altId
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException {
      return PersistentStore.get().getNidForUuids(UuidFactory.getUuidFromAlternateId(authorityUuid, altId));
   }

   /**
    * Method description
    *
    *
    * @param pathNid
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Path getPath(int pathNid) throws IOException {
      return store.getPath(pathNid);
   }

   /**
    * Method description
    *
    *
    * @param sapNid
    *
    * @return
    */
   @Override
   public int getPathNidForStamp(int sapNid) {
      return store.getPathNidForStamp(sapNid);
   }

   /**
    * Method description
    *
    *
    * @param positions
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Set<Path> getPathSetFromPositionSet(Set<Position> positions) throws IOException {
      return store.getPathSetFromPositionSet(positions);
   }

   /**
    * Method description
    *
    *
    * @param sapNids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Set<Path> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException {
      return store.getPathSetFromSapSet(sapNids);
   }

   /**
    * Method description
    *
    *
    * @param sapNids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Set<Position> getPositionSet(Set<Integer> sapNids) throws IOException {
      return store.getPositionSet(sapNids);
   }

   /**
    * Method description
    *
    *
    * @param cNid
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public int[] getPossibleChildren(int cNid) throws IOException {
      return store.getPossibleChildren(cNid, vc);
   }

   /**
    * Method description
    *
    *
    * @param sapNid
    *
    * @return
    */
   @Override
   public Status getStatusForStamp(int stamp) {
      return store.getStatusForStamp(stamp);
   }

   /**
    * Method description
    *
    *
    * @param sapNid
    *
    * @return
    */
   @Override
   public long getTimeForStamp(int sapNid) {
      return store.getTimeForStamp(sapNid);
   }

   /**
    * Method description
    *
    *
    * @param nids
    *
    * @return
    *
    * @throws IOException
    */
   @Override
   public Collection<UUID> getUuidCollection(Collection<Integer> nids) throws IOException {
      return PersistentStore.get().getUuidCollection(nids);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public ViewCoordinate getViewCoordinate() {
      return vc;
   }

   /**
    * Method description
    *
    *
    * @param childNid
    * @param parentNid
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   public boolean isKindOf(int childNid, int parentNid) throws IOException, ContradictionException {
      return store.isKindOf(childNid, parentNid, vc);
   }

    @Override
    public CharSequence informAboutId(Object id) {
        if (id instanceof UUID) {
            return informAboutUuid((UUID) id);
        } else if (id instanceof Number) {
            informAboutNid((Integer) id);
        }
        return id.toString();
    }

    @Override
    public NativeIdSetBI getConceptNidsForComponentNids(NativeIdSetBI componentNativeIds) throws IOException {
        return store.getConceptNidsForComponentNids(componentNativeIds);
    }

    @Override
    public NativeIdSetBI getComponentNidsForConceptNids(NativeIdSetBI conceptNativeIds) throws IOException {
        return store.getComponentNidsForConceptNids(conceptNativeIds);
    }

    @Override
    public NativeIdSetBI getAllComponentNids() throws IOException {
        return store.getAllComponentNids();
    }

    @Override
    public void index() throws IOException {
        store.index();
    }

    @Override
    public NativeIdSetBI getOrphanNids(NativeIdSetBI conceptNativeIds) throws IOException {
        return store.getOrphanNids(conceptNativeIds);
    }

    @Override
    public NativeIdSetBI getAllConceptNidsFromCache() throws IOException {
        return store.getAllConceptNidsFromCache();
    }
}
