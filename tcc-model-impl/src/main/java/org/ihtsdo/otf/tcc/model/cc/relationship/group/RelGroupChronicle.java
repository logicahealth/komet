package org.ihtsdo.otf.tcc.model.cc.relationship.group;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.snapshot.calculator.RelativePositionCalculator;
import gov.vha.isaac.ochre.collections.SequenceSet;
import gov.vha.isaac.ochre.collections.StampSequenceSet;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.EditCoordinate;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.id.IdBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.group.RelGroupVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.util.Optional;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.concept.ConceptChronicle;

public class RelGroupChronicle implements RelGroupChronicleBI {
   private int                                 conceptNid;
   private int                                 nid;
   private int                                 relGroup;
   private Collection<RelationshipChronicleBI> rels;
   private UUID                                uuid;

   //~--- constructors --------------------------------------------------------

   public RelGroupChronicle(ConceptChronicle c, int relGroup, Collection<RelationshipChronicleBI> rels)
           throws IOException {
      super();
      this.relGroup   = relGroup;
      this.conceptNid = c.getNid();

      uuid = UuidT5Generator.get(UuidT5Generator.REL_GROUP_NAMESPACE,
              c.getPrimordialUuid().toString() + relGroup);

      nid = PersistentStore.get().getNidForUuids(uuid);
      PersistentStore.get().setConceptNidForNid(conceptNid, nid);
      this.rels = rels;
   }

   //~--- methods -------------------------------------------------------------
    @Override
    public boolean isLatestVersionActive(StampCoordinate coordinate) {
        RelativePositionCalculator calc = RelativePositionCalculator.getCalculator(coordinate);
        StampSequenceSet latestStampSequences = calc.getLatestStampSequencesAsSet(this.getVersionStampSequences());
        return !latestStampSequences.isEmpty();
    }

   @Override
   public boolean addAnnotation(RefexChronicleBI<?> annotation) {
      throw new UnsupportedOperationException("Not supported.");
   }

    @Override
    public IntStream getVersionStampSequences() {
        SequenceSet sequences = new SequenceSet();
        for (RelationshipChronicleBI rel: rels) {
            sequences.addAll(rel.getVersionStampSequences());
        }
        return sequences.stream();
    }
   
   

   /**
    * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#addDynamicAnnotation(org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI)
    */
   @Override
   public boolean addDynamicAnnotation(RefexDynamicChronicleBI<?> annotation) throws IOException
   {
       throw new UnsupportedOperationException("Not supported.");
   }

@Override
   public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public String toUserString() {
      StringBuilder buff = new StringBuilder();

      buff.append("Group: ");

      for (RelationshipChronicleBI rc : rels) {
         buff.append(rc.toUserString());
         buff.append(";");
      }

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<? extends IdBI> getAdditionalIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends IdBI> getAllIds() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Set<Integer> getAllStamps() throws IOException {
      HashSet<Integer> sapNids = new HashSet<>();

      for (RelationshipChronicleBI r : rels) {
         sapNids.addAll(r.getAllStamps());
      }

      return sapNids;
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
      throw new UnsupportedOperationException("Not supported.");
   }

   public int getConceptNid() {
      return conceptNid;
   }

    @Override
    public int getEnclosingConceptNid() {
        return conceptNid;
    }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz)
           throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           Class<T> cls)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public <T extends RefexVersionBI<?>> Collection<T> getAnnotationsActive(ViewCoordinate xyz,
           int refexNid, Class<T> cls)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz, int refsetNid)
           throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersActive(ViewCoordinate xyz) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }


   @Override
   public Collection<? extends RefexVersionBI<?>> getRefexMembersInactive(ViewCoordinate xyz) throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public int getNid() {
      return nid;
   }

   @Override
   public Set<Position> getPositions() throws IOException {
      Set<Position> positions = new HashSet<>();

      for (RelationshipChronicleBI rc : rels) {
         positions.addAll(rc.getPositions());
      }

      return positions;
   }

   @Override
   public UUID getPrimordialUuid() {
      if (uuid == null) {
         return UUID.fromString("00000000-0000-0000-C000-000000000046");
      }

      return uuid;
   }

   @Override
   public RelGroupVersionBI getPrimordialVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
      throw new UnsupportedOperationException("Not supported.");
   }
   
   /**
    * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamic()
    */
   @Override
   public Collection<? extends RefexDynamicChronicleBI<?>> getRefexesDynamic() throws IOException
   {
      throw new UnsupportedOperationException("Not supported.");
   }
   
   

   /**
    * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicAnnotations()
    */
   @Override
   public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicAnnotations() throws IOException
   {
      throw new UnsupportedOperationException("Not supported.");
   }
   
   /**
    * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexDynamicMembers()
    */
   @Override
   public Collection<? extends RefexDynamicChronicleBI<?>> getRefexDynamicMembers() throws IOException
   {
      throw new UnsupportedOperationException("Not supported.");
   }

   /**
    * @see org.ihtsdo.otf.tcc.api.chronicle.ComponentBI#getRefexesDynamicActive(org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate)
    */
   @Override
   public Collection<? extends RefexDynamicVersionBI<?>> getRefexesDynamicActive(ViewCoordinate viewCoordinate) throws IOException
   {
      throw new UnsupportedOperationException("Not supported.");
   }

   @Override
   public int getRelGroup() {
      return relGroup;
   }

   @Override
   public Collection<? extends RelationshipChronicleBI> getRels() {
      return rels;
   }
   @Override
   public List<UUID> getUuidList() {
      return Arrays.asList(new UUID[] { uuid });
   }

   @Override
   public Optional<RelGroupVersionBI> getVersion(ViewCoordinate c) throws ContradictionException {
      return Optional.of(new RelGroupVersion(this, c));
   }

   @Override
   public List<? extends RelGroupVersionBI> getVersions() {
      return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, null) });
   }

   @Override
   public List<? extends RelGroupVersionBI> getVersionList() {
      return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, null) });
   }

   @Override
   public Collection<? extends RelGroupVersionBI> getVersions(ViewCoordinate c) {
      return Arrays.asList(new RelGroupVersionBI[] { new RelGroupVersion(this, c) });
   }

   @Override
   public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public boolean isUncommitted() {
      return false;
   }
   
    @Override
    public CommitStates getCommitState() {
        if (isUncommitted()) {
            return CommitStates.UNCOMMITTED;
        }
        return CommitStates.COMMITTED;
    }

    @Override
    public List<SememeChronology<? extends SememeVersion<?>>> getSememeList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   @Override
    public List<SememeChronology<? extends SememeVersion<?>>> getSememeListFromAssemblage(int assemblageSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   @Override
    public <SV extends SememeVersion> List<SememeChronology<SV>> getSememeListFromAssemblageOfType(int assemblageSequence, Class<SV> type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Optional<LatestVersion<RelGroupVersionBI>> getLatestVersion(Class<RelGroupVersionBI> type, StampCoordinate coordinate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
