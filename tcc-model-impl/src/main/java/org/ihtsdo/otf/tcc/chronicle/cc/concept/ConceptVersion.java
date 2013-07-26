package org.ihtsdo.otf.tcc.chronicle.cc.concept;

//~--- non-JDK imports --------------------------------------------------------

import java.io.DataOutput;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraintIncoming;
import org.ihtsdo.otf.tcc.api.constraint.ConstraintBI;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraint;
import org.ihtsdo.otf.tcc.api.constraint.ConstraintCheckType;
import org.ihtsdo.otf.tcc.api.constraint.RelConstraintOutgoing;
import org.ihtsdo.otf.tcc.api.constraint.DescriptionConstraint;
import org.ihtsdo.otf.tcc.api.chronicle.ProcessComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.nid.NidListBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidList;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionBI;
import org.ihtsdo.otf.tcc.chronicle.cc.LanguageSortPrefs.LANGUAGE_SORT_PREF;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.chronicle.cc.relationship.group.RelGroupVersion;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptCB;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.otf.tcc.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetPolicy;
import org.ihtsdo.otf.tcc.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.HistoricalRelType;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.mahout.math.map.OpenIntIntHashMap;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;

public class ConceptVersion implements ConceptVersionBI, Comparable<ConceptVersion> {
   private static NidSetBI classifierCharacteristics;

   //~--- fields --------------------------------------------------------------

   private ConceptChronicle        concept;
   NidListBI              fsnOrder;
   NidListBI              preferredOrder;
   NidListBI              synonymOrder;
   private ViewCoordinate vc;

   //~--- constructors --------------------------------------------------------

   public ConceptVersion(ConceptChronicle concept, ViewCoordinate coordinate) {
      super();

      if (concept == null) {
         throw new IllegalArgumentException();
      }

      this.concept = concept;
      this.vc      = new ViewCoordinate(UUID.randomUUID(), coordinate.getName() + " clone", coordinate);
   }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        concept.writeExternal(out);
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) throws IOException {
      return concept.addAnnotation(annotation);
   }

   @Override
   public void cancel() throws IOException {
      concept.cancel();
   }

   private boolean checkConceptVersionConstraint(int cNid, ConceptSpec constraint,
           ConstraintCheckType checkType)
           throws IOException, ContradictionException {
      switch (checkType) {
      case EQUALS :
         return P.s.getConceptVersion(vc, cNid).getNid() == constraint.getStrict(vc).getNid();

      case IGNORE :
         return true;

      case KIND_OF :
         return P.s.getConceptVersion(vc, cNid).isKindOf(constraint.getStrict(vc));

      default :
         throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
      }
   }

   private boolean checkTextConstraint(String text, String constraint, ConstraintCheckType checkType) {
      switch (checkType) {
      case EQUALS :
         return text.equals(constraint);

      case IGNORE :
         return true;

      case REGEX :
         Pattern pattern = Pattern.compile(constraint);
         Matcher matcher = pattern.matcher(text);

         return matcher.find();

      default :
         throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
      }
   }

   @Override
   public boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                         ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
           throws IOException {
      return concept.commit(changeSetPolicy, changeSetWriterThreading);
   }

   public void commit(ChangeSetPolicy changeSetPolicy, ChangeSetWriterThreading changeSetWriterThreading)
           throws IOException {
      concept.commit(changeSetPolicy, changeSetWriterThreading);
   }

   @Override
   public int compareTo(ConceptVersion o) {
      return getNid() - o.getNid();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ConceptVersion) {
         ConceptVersion another = (ConceptVersion) obj;

         if (concept.nid != another.concept.nid) {
            return false;
         }

         if (vc == another.vc) {
            return true;
         }

         return vc.equals(another.vc);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return concept.hashCode;
   }

   @Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws Exception {
      return concept.makeAdjudicationAnalogs(ec, vc);
   }

   @Override
   public ConceptCB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
      return concept.makeBlueprint(vc, idDirective, refexDirective);
   }

   @Override
   public void processComponentChronicles(ProcessComponentChronicleBI processor) throws Exception {
      concept.processComponentChronicles(processor);
   }

   @Override
   public boolean satisfies(ConstraintBI constraint, ConstraintCheckType subjectCheck,
                            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck)
           throws IOException, ContradictionException {
      if (RelConstraintOutgoing.class.isAssignableFrom(constraint.getClass())) {
         return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelationshipsOutgoingActive());
      } else if (RelConstraintIncoming.class.isAssignableFrom(constraint.getClass())) {
         return testRels(constraint, subjectCheck, propertyCheck, valueCheck, getRelationshipsIncomingActive());
      } else if (DescriptionConstraint.class.isAssignableFrom(constraint.getClass())) {
         DescriptionConstraint dc = (DescriptionConstraint) constraint;

         for (DescriptionVersionBI desc : getDescriptionsActive()) {
            if (checkConceptVersionConstraint(desc.getConceptNid(), dc.getConceptSpec(), subjectCheck)
                    && checkConceptVersionConstraint(desc.getTypeNid(), dc.getDescTypeSpec(), propertyCheck)
                    && checkTextConstraint(desc.getText(), dc.getText(), valueCheck)) {
               return true;
            }
         }

         return false;
      }

      throw new UnsupportedOperationException("Can't handle constraint of type: " + constraint);
   }

   private static void setupClassifierCharacteristics() {
      if (classifierCharacteristics == null) {
         NidSetBI temp = new NidSet();

         try {
            temp.add(P.s.getNidForUuids(SnomedMetadataRf1.DEFINED_RF1.getUuids()));
            temp.add(P.s.getNidForUuids(SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids()));
            temp.add(
                P.s.getNidForUuids(SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids()));
            temp.add(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getConceptNid());
         } catch (ValidationException e) {
            throw new RuntimeException(e);
         } catch (IOException e) {
            throw new RuntimeException(e);
         }

         classifierCharacteristics = temp;
      }
   }

   private void setupFsnOrder() {
      if (fsnOrder == null) {
         NidListBI newList = new NidList();

         newList.add(ReferenceConcepts.FULLY_SPECIFIED_RF1.getNid());
         newList.add(ReferenceConcepts.FULLY_SPECIFIED_RF2.getNid());
         fsnOrder = newList;
      }
   }

   private void setupPreferredOrder() {
      if (preferredOrder == null) {
         NidListBI newList = new NidList();

         newList.add(ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF1.getNid());
         newList.add(ReferenceConcepts.PREFERRED_RF1.getNid());
         newList.add(ReferenceConcepts.PREFERRED_ACCEPTABILITY_RF2.getNid());
         newList.add(ReferenceConcepts.SYNONYM_RF1.getNid());
         newList.add(ReferenceConcepts.SYNONYM_RF2.getNid());
         preferredOrder = newList;
      }
   }

   @Override
   public boolean stampIsInRange(int min, int max) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   private boolean testRels(ConstraintBI constraint, ConstraintCheckType subjectCheck,
                            ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck,
                            Collection<? extends RelationshipVersionBI> rels)
           throws IOException, ContradictionException {
      RelConstraint rc = (RelConstraint) constraint;

      for (RelationshipVersionBI rel : rels) {
         if (checkConceptVersionConstraint(rel.getOriginNid(), rc.getOriginSpec(), subjectCheck)
                 && checkConceptVersionConstraint(rel.getTypeNid(), rc.getRelTypeSpec(), propertyCheck)
                 && checkConceptVersionConstraint(rel.getDestinationNid(), rc.getDestinationSpec(),
                    valueCheck)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toLongString() {
      return concept.toLongString();
   }

   @Override
   public String toString() {
      return concept.toString() + "\n\nviewCoordinate:\n" + vc;
   }

   @Override
   public String toUserString() {
      return concept.toString();
   }

   @Override
   public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
      if (getPreferredDescription() != null) {
         return getPreferredDescription().getText();
      }

      return concept.getText();
   }

   @Override
   public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() throws IOException {
      return concept.getAdditionalIds();
   }

   @Override
   public Collection<? extends IdBI> getAllIds() throws IOException {
      return concept.getAllIds();
   }

   @Override
   public Set<Integer> getAllNidsForVersion() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<Integer> getAllStamps() throws IOException {
      return concept.getAllStamps();
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() throws IOException {
      return concept.getAnnotations();
   }

   @Override
   public int getAuthorNid() {
      try {
         return getConceptAttributes().getAuthorNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public ConceptChronicleBI getChronicle() {
      return concept;
   }

   @Override
   public ComponentChronicleBI<?> getComponent(int nid) throws IOException {
      return (ComponentChronicleBI<?>) concept.getComponent(nid);
   }

   @Override
   public ConceptAttributeVersionBI getConceptAttributes() throws IOException {
      return concept.getConceptAttributes();
   }

   @Override
   public ConceptAttributeVersionBI getConceptAttributesActive() throws IOException, ContradictionException {
      return concept.getConceptAttributes().getVersion(vc);
   }

   @Override
   public int getConceptNid() {
      return concept.getConceptNid();
   }

   public Collection<Integer> getConceptNidsAffectedByCommit() throws IOException {
      return concept.getConceptNidsAffectedByCommit();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      return concept.getAnnotationsActive(xyz);
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           Class<T> cls)
           throws IOException {
      return concept.getAnnotationsActive(xyz, cls);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      return concept.getAnnotationsActive(xyz, refexNid);
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid, Class<T> cls)
           throws IOException {
      return concept.getAnnotationsActive(xyz, refexNid, cls);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(int refsetNid) throws IOException {
      return concept.getRefexMembersActive(vc, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      return concept.getRefexMembersActive(xyz, refsetNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
      return concept.getRefexMembersActive(xyz);
   }

   @Override
   public RefexChronicleBI<?> getCurrentRefsetMemberForComponent(int componentNid) throws IOException {
      return concept.getCurrentRefsetMemberForComponent(vc, componentNid);
   }

   @Override
   public RefexVersionBI<?> getCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid)
           throws IOException {
      return concept.getCurrentRefsetMemberForComponent(vc, componentNid);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc)
           throws IOException {
      return concept.getCurrentRefsetMembers(vc);
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc, Long cutoffTime)
           throws IOException {
      return concept.getCurrentRefsetMembers(vc, cutoffTime);
   }

   @Override
   public Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException {
      return concept.getDescriptions();
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescriptionsActive() throws IOException {
      Collection<DescriptionVersionBI> returnValues = new ArrayList<>();

      for (DescriptionChronicleBI desc : getDescriptions()) {
         returnValues.addAll(desc.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescriptionsActive(int typeNid) throws IOException {
      return getDescriptionsFullySpecifiedActive(new NidSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive(NidSetBI typeNids) throws IOException {
      Collection<DescriptionVersionBI> results = new ArrayList<>();

      for (DescriptionVersionBI d : getDescriptionsActive()) {
         if (typeNids.contains(d.getTypeNid())) {
            results.add(d);
         }
      }

      return results;
   }

   @Override
   public ConceptChronicleBI getEnclosingConcept() {
      return concept;
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescriptionsFullySpecifiedActive() throws IOException {
      setupFsnOrder();

      return getDescriptionsFullySpecifiedActive(new NidSet(fsnOrder.getListArray()));
   }

   @Override
   public DescriptionVersionBI getFullySpecifiedDescription() throws IOException, ContradictionException {
      setupFsnOrder();

      return concept.getDesc(fsnOrder, vc.getLangPrefList(), vc.getAllowedStatus(), vc.getPositionSet(),
                             LANGUAGE_SORT_PREF.getPref(vc.getLangSort()), vc.getPrecedence(),
                             vc.getContradictionManager());
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
      return concept.getRefexMembersInactive(xyz);
   }

   @Override
   public long getLastModificationSequence() {
      return concept.getLastModificationSequence();
   }

   @Override
   public Collection<? extends MediaChronicleBI> getMedia() throws IOException {
      return concept.getImages();
   }

   @Override
   public Collection<? extends MediaVersionBI> getMediaActive() throws IOException, ContradictionException {
      Collection<MediaVersionBI> returnValues = new ArrayList<>();

      for (MediaChronicleBI media : getMedia()) {
         returnValues.addAll(media.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public int getModuleNid() {
      try {
         return getConceptAttributes().getModuleNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public int getNid() {
      return concept.getNid();
   }

   @Override
   public Collection<List<Integer>> getNidPathsToRoot() throws IOException {
      return getNidPathsToRootNoAdd(new ArrayList<Integer>());
   }

   private Collection<List<Integer>> getNidPathsToRoot(List<Integer> nidPath) throws IOException {
      nidPath.add(this.getNid());

      return getNidPathsToRootNoAdd(nidPath);
   }

   private Collection<List<Integer>> getNidPathsToRootNoAdd(List<Integer> nidPath) throws IOException {
      TreeSet<List<Integer>> pathList = new TreeSet<>(new Comparator<List<Integer>>() {
         @Override
         public int compare(List<Integer> o1, List<Integer> o2) {
            if (o1.size() != o2.size()) {
               return o1.size() - o2.size();
            }

            int size = o1.size();

            for (int i = 0; i < size; i++) {
               if (o1.get(i) != o2.get(i)) {
                  return o1.get(i) - o2.get(i);
               }
            }

            return 0;
         }
      });

      try {
         Collection<? extends ConceptVersionBI> parents = getRelationshipsOutgoingDestinationsActiveIsa();

         if (parents.isEmpty()) {
            pathList.add(nidPath);
         } else {
            for (ConceptVersionBI parent : parents) {
               pathList.addAll(((ConceptVersion) parent).getNidPathsToRoot(new ArrayList(nidPath)));
            }
         }
      } catch (ContradictionException ex) {
         ConceptChronicle.logger.log(Level.SEVERE, "Contradiction exception.", ex);
      }

      return pathList;
   }

   @Override
   public int getPathNid() {
      try {
         return getConceptAttributes().getPathNid();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public PositionBI getPosition() throws IOException {
      throw new UnsupportedOperationException();
   }

   @Override
   public Set<PositionBI> getPositions() throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getDescriptionsPreferredActive() throws IOException {
      setupPreferredOrder();

      return getDescriptionsFullySpecifiedActive(new NidSet(preferredOrder.getListArray()));
   }

   @Override
   public DescriptionVersionBI getPreferredDescription() throws IOException, ContradictionException {
      setupPreferredOrder();

      return concept.getDesc(preferredOrder, vc.getLangPrefList(), vc.getAllowedStatus(),
                             vc.getPositionSet(), LANGUAGE_SORT_PREF.getPref(vc.getLangSort()),
                             vc.getPrecedence(), vc.getContradictionManager());
   }

   @Override
   public UUID getPrimordialUuid() {
      return concept.getPrimordialUuid();
   }

   @Override
   public ConceptVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      return concept.getRefexMembers(refsetNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      return concept.getRefexes();
   }

   @Override
   public RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException {
      return concept.getRefsetMemberForComponent(componentNid);
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
      return concept.getRefsetMembers();
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive() throws IOException {
      return concept.getCurrentRefsetMembers(vc);
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getRelationshipGroupsActive() throws IOException, ContradictionException {
      ArrayList<RelGroupVersionBI> results = new ArrayList<>();

      for (RelGroupChronicleBI rgc : concept.getRelationshipGroupsActive(vc)) {
         RelGroupVersionBI rgv = new RelGroupVersion(rgc, vc);

         if (rgv.getRels().size() > 0) {
            results.add(rgv);
         }
      }

      return results;
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getRelationshipGroupsActive(ViewCoordinate vc)
           throws IOException, ContradictionException {
      return concept.getRelationshipGroupsActive(vc);
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelationshipsIncoming() throws IOException {
      return concept.getRelationshipsIncoming();
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActive()
           throws IOException, ContradictionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         returnValues.addAll(rel.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelationshipsIncomingActiveIsa()
           throws IOException, ContradictionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI rv : rel.getVersions(vc)) {
            if (vc.getIsaNid() == rv.getTypeNid()) {
               returnValues.add(rv);
            }
         }
      }

      return returnValues;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOrigins() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOrigins(int typeNid) throws IOException {
      return getRelationshipsIncomingOrigins(new NidSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOrigins(NidSetBI typeNids)
           throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOriginsActive()
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOriginsActive(int typeNid)
           throws IOException, ContradictionException {
      return getRelationshipsIncomingOriginsActive(new NidSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOriginsActive(NidSetBI typeNids)
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOriginsActiveIsa()
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaNid() == relv.getTypeNid()) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsIncomingOriginsIsa() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsIncoming()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (vc.getIsaNid() == relv.getTypeNid()) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getOriginNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRelationshipsOutgoing() throws IOException {
      setupClassifierCharacteristics();

      Collection<? extends RelationshipChronicleBI> allRels = concept.getRelationshipsOutgoing();
      Collection<RelationshipChronicleBI>           results = new ArrayList<>(allRels.size());

      switch (vc.getRelationshipAssertionType()) {
      case INFERRED :
         for (RelationshipChronicleBI rc : allRels) {
            for (RelationshipVersionBI<?> rv : rc.getVersions()) {
               if (classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                  results.add(rc);

                  break;
               }
            }
         }

         return results;

      case INFERRED_THEN_STATED :
         return allRels;

      case STATED :
         for (RelationshipChronicleBI rc : allRels) {
            for (RelationshipVersionBI<?> rv : rc.getVersions()) {
               if (!classifierCharacteristics.contains(rv.getCharacteristicNid())) {
                  results.add(rc);

                  break;
               }
            }
         }

         return results;

      default :
         throw new RuntimeException("Can't handle: " + vc.getRelationshipAssertionType());
      }
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActive()
           throws IOException, ContradictionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         returnValues.addAll(rel.getVersions(vc));
      }

      return returnValues;
   }

   @Override
   public Collection<? extends RelationshipVersionBI> getRelationshipsOutgoingActiveIsa()
           throws IOException, ContradictionException {
      Collection<RelationshipVersionBI> returnValues = new ArrayList<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI rv : rel.getVersions(vc)) {
            if (vc.getIsaNid() == rv.getTypeNid()) {
               returnValues.add(rv);
            }
         }
      }

      return returnValues;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinations() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinations(int typeNid) throws IOException {
      return getRelationshipsOutgoingDestinations(new NidSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinations(NidSetBI typeNids)
           throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinationsActive()
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

            conceptSet.add(cv);
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinationsActive(int typeNid)
           throws IOException, ContradictionException {
      return getRelationshipsOutgoingDestinationsActive(new NidSet(new int[] { typeNid }));
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinationsActive(NidSetBI typeNids)
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (typeNids.contains(relv.getTypeNid())) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinationsActiveIsa()
           throws IOException, ContradictionException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaNid() == relv.getTypeNid()) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public Collection<? extends ConceptVersionBI> getRelationshipsOutgoingDestinationsIsa() throws IOException {
      HashSet<ConceptVersionBI> conceptSet = new HashSet<>();

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions()) {
            if (vc.getIsaNid() == relv.getTypeNid()) {
               ConceptVersionBI cv = P.s.getConceptVersion(vc, relv.getDestinationNid());

               conceptSet.add(cv);
            }
         }
      }

      return conceptSet;
   }

   @Override
   public int[] getRelationshipsOutgoingDestinationsNidsActiveIsa() throws IOException {
      OpenIntIntHashMap nidList = new OpenIntIntHashMap(10);

      for (RelationshipChronicleBI rel : getRelationshipsOutgoing()) {
         for (RelationshipVersionBI relv : rel.getVersions(vc)) {
            if (vc.getIsaNid() == relv.getTypeNid()) {
               nidList.put(relv.getDestinationNid(), relv.getDestinationNid());
            }
         }
      }

      return nidList.keys().elements();
   }

   @Override
   public int getStamp() {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public Status getStatus() {
      try {
         return getConceptAttributes().getStatus();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Collection<? extends DescriptionVersionBI> getSynonyms() throws IOException {
      if (synonymOrder == null) {
         synonymOrder = new NidList();
         synonymOrder.add(ReferenceConcepts.ACCEPTABLE_ACCEPTABILITY.getNid());
         synonymOrder.add(ReferenceConcepts.SYNONYM_RF1.getNid());
         synonymOrder.add(ReferenceConcepts.SYNONYM_RF2.getNid());
      }

      throw new UnsupportedOperationException();
   }

   @Override
   public long getTime() {
      try {
         return getConceptAttributes().getTime();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<UUID> getUUIDs() {
      return concept.getUUIDs();
   }

   @Override
   public ConceptVersionBI getVersion(ViewCoordinate c) {
      return concept.getVersion(c);
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends ConceptVersionBI> getVersions(ViewCoordinate c) {
      return concept.getVersions();
   }

   @Override
   public ViewCoordinate getViewCoordinate() {
      return vc;
   }

   @Override
   public boolean hasAnnotationMemberActive(int refsetNid) throws IOException {
      return concept.hasCurrentAnnotationMember(vc, refsetNid);
   }

   @Override
   public boolean hasChildren() throws IOException, ContradictionException {
      Collection<? extends RelationshipVersionBI> children = this.getRelationshipsIncomingActive();

      if (children.isEmpty()) {
         return false;
      }

      return true;
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return concept.hasCurrentAnnotationMember(xyz, refsetNid);
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      return concept.hasCurrentRefexMember(xyz, refsetNid);
   }

   @Override
   public boolean hasCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid) throws IOException {
      return concept.hasCurrentRefsetMemberForComponent(vc, componentNid);
   }

   @Override
   public boolean hasHistoricalRels() throws IOException, ContradictionException {
      boolean                                       history = false;
      Collection<? extends RelationshipChronicleBI> outRels = getRelationshipsOutgoing();

      if (outRels != null) {
         NidSet historicalTypeNids = new NidSet();

         for (ConceptSpec spec : HistoricalRelType.getHistoricalTypes()) {
            historicalTypeNids.add(spec.getStrict(vc).getNid());
         }

         for (RelationshipChronicleBI outRel : outRels) {
            RelationshipVersionBI<?> vOutRel = outRel.getVersion(vc);

            if (vOutRel != null) {
               if (historicalTypeNids.contains(vOutRel.getTypeNid())) {
                  history = true;

                  break;
               }
            }
         }
      }

      return history;
   }

   @Override
   public boolean hasRefexMemberActive(int refsetNid) throws IOException {
      return concept.hasCurrentRefexMember(vc, refsetNid);
   }

   @Override
   public boolean hasRefsetMemberForComponentActive(int componentNid) throws IOException {
      return concept.hasCurrentRefsetMemberForComponent(vc, componentNid);
   }

   @Override
   public boolean isActive() throws IOException {
      try {
         if (getConceptAttributesActive() == null) {
            return false;
         }

         return true;
      } catch (ContradictionException ex) {
         for (ConceptAttributeVersionBI version : concept.getConceptAttributes().getVersions(vc)) {
            if (vc.getAllowedStatus().contains(version.getStatus())) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean isAnnotationStyleRefex() throws IOException {
      return concept.isAnnotationStyleRefex();
   }

   @Override
   public boolean isBaselineGeneration() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isChildOf(ConceptVersionBI possibleParent) throws IOException {
      for (int nid : getRelationshipsOutgoingDestinationsNidsActiveIsa()) {
         if (nid == possibleParent.getNid()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isKindOf(ConceptVersionBI possibleKind) throws IOException, ContradictionException {
      return Ts.get().isKindOf(getNid(), possibleKind.getNid(), vc);
   }

   @Override
   public boolean isLeaf() throws IOException {
      return P.s.getPossibleChildren(concept.nid, vc).length == 0;
   }

   // TODO
   @Override
   public boolean isMember(int collectionNid) throws IOException {
      boolean isMember = false;

      try {
         Collection<? extends RefexChronicleBI<?>> refexes =
            concept.getConceptAttributes().getRefexMembersActive(vc);

         if (refexes != null) {
            for (RefexChronicleBI<?> refex : refexes) {
               if (refex.getRefexExtensionNid() == collectionNid) {
                  return true;
               }
            }
         }

         return isMember;
      } catch (Exception e) {
         throw new IOException(e);    // AceLog.getAppLog().alertAndLogException(e);
      }
   }

   @Override
   public boolean isUncommitted() {
      return concept.isUncommitted();
   }

    @Override
    public boolean isAnnotationIndex() throws IOException {
        return concept.isAnnotationIndex();
    }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAnnotationStyleRefex(boolean annotationStyleRefset) {
      concept.setAnnotationStyleRefex(annotationStyleRefset);
   }
}
