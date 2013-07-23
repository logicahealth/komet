package org.ihtsdo.otf.tcc.chronicle.cc.relationship;

import org.ihtsdo.otf.tcc.api.coordinate.PositionSetBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.chronicle.TypedComponentVersionBI;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.chronicle.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.chronicle.cc.concept.ConceptChronicle;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship>
        implements RelationshipAnalogBI<RelationshipRevision> {
   private static int                                   classifierAuthorNid = Integer.MIN_VALUE;
   private static VersionComputer<Relationship.Version> computer            = new VersionComputer<>();
   public static final int                              INFERRED_NID_RF1;
   public static final int                              INFERRED_NID_RF2;
   public static final int                              STATED_NID_RF1;
   public static final int                              STATED_NID_RF2;

   //~--- static initializers -------------------------------------------------

   static {
      try {
         INFERRED_NID_RF1 =
            Ts.get().getNidForUuids(SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
         STATED_NID_RF1 =
            Ts.get().getNidForUuids(SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getUuids());
         INFERRED_NID_RF2 = Ts.get().getNidForUuids(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
         STATED_NID_RF2   = Ts.get().getNidForUuids(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- fields --------------------------------------------------------------

   private int   c2Nid;
   private int   characteristicNid;
   private int   group;
   private int   refinabilityNid;
   private int   typeNid;
   List<Version> versions;

   //~--- constructors --------------------------------------------------------

   public Relationship() {
      super();
   }

   public Relationship(ConceptChronicleBI enclosingConcept, TupleInput input) throws IOException {
      super(enclosingConcept.getNid(), input);
   }

   public Relationship(TtkRelationshipChronicle eRel, ConceptChronicleBI enclosingConcept) throws IOException {
      super(eRel, enclosingConcept.getNid());
      c2Nid = P.s.getNidForUuids(eRel.getC2Uuid());
      characteristicNid = P.s.getNidForUuids(eRel.getCharacteristicUuid());
      group = eRel.getRelGroup();
      refinabilityNid = P.s.getNidForUuids(eRel.getRefinabilityUuid());
      typeNid = P.s.getNidForUuids(eRel.getTypeUuid());
      primordialStamp = P.s.getStamp(eRel);

      if (eRel.getRevisionList() != null) {
         revisions = new RevisionSet<>(primordialStamp);

         for (TtkRelationshipRevision erv : eRel.getRevisionList()) {
            revisions.add(new RelationshipRevision(erv, this));
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addComponentNids(Set<Integer> allNids) {
      allNids.add(c2Nid);
      allNids.add(characteristicNid);
      allNids.add(refinabilityNid);
      allNids.add(typeNid);
   }

   public boolean addPart(RelationshipRevision part) {
      return revisions.add(part);
   }

   @Override
   public void clearVersions() {
      versions = null;
      clearAnnotationVersions();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (Relationship.class.isAssignableFrom(obj.getClass())) {
         Relationship another = (Relationship) obj;

         return nid == another.nid;
      }

      return false;
   }

   public boolean everWasType(int typeNid) {
      if (this.typeNid == typeNid) {
         return true;
      }

      if (revisions != null) {
         for (RelationshipRevision rv : revisions) {
            if (rv.getTypeNid() == typeNid) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship> obj) {
      if (Relationship.class.isAssignableFrom(obj.getClass())) {
         Relationship another = (Relationship) obj;

         if (this.c2Nid != another.c2Nid) {
            return false;
         }

         if (this.getCharacteristicNid() != another.getCharacteristicNid()) {
            return false;
         }

         if (this.group != another.group) {
            return false;
         }

         if (this.getRefinabilityNid() != another.getRefinabilityNid()) {
            return false;
         }

         if (this.getTypeNid() != another.getTypeNid()) {
            return false;
         }

         return conceptComponentFieldsEqual(another);
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { nid, c2Nid, enclosingConceptNid });
   }

   @Override
   public RelationshipRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
      RelationshipRevision newR = new RelationshipRevision(this, status, time, authorNid, moduleNid,
                                     pathNid, this);

      addRevision(newR);

      return newR;
   }

   @Override
   public RelationshipCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
      RelationshipType relType = null;

      if ((getCharacteristicNid()
              == SnomedMetadataRf1.INFERRED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient()
                 .getNid()) || (getCharacteristicNid()
                                == SnomedMetadataRf1.DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient()
                                   .getNid()) || (getCharacteristicNid()
                                      == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid())) {
         throw new InvalidCAB("Inferred relationships can not be used to make blueprints");
      } else if ((getCharacteristicNid()
                  == SnomedMetadataRf1.STATED_DEFINING_CHARACTERISTIC_TYPE_RF1.getLenient()
                     .getNid()) || (getCharacteristicNid()
                                    == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid())) {
         relType = RelationshipType.STATED_HIERARCHY;
      }

      RelationshipCAB relBp = new RelationshipCAB(getOriginNid(), getTypeNid(), getDestinationNid(), getGroup(), relType,
                                getVersion(vc), vc, idDirective, refexDirective);

      return relBp;
   }

   @Override
   public void readFromBdb(TupleInput input) {

      // nid, list size, and conceptNid are read already by the binder...
      c2Nid             = input.readInt();
      characteristicNid = input.readInt();
      group             = input.readSortedPackedInt();
      refinabilityNid   = input.readInt();
      typeNid           = input.readInt();

      int additionalVersionCount = input.readSortedPackedInt();

      if (additionalVersionCount > 0) {
         revisions = new RevisionSet<>(primordialStamp);

         for (int i = 0; i < additionalVersionCount; i++) {
            revisions.add(new RelationshipRevision(input, this));
         }
      }
   }

   @Override
   public boolean readyToWriteComponent() {
      assert c2Nid != Integer.MAX_VALUE : assertionString();
      assert characteristicNid != Integer.MAX_VALUE : assertionString();
      assert group != Integer.MAX_VALUE : assertionString();
      assert refinabilityNid != Integer.MAX_VALUE : assertionString();
      assert typeNid != Integer.MAX_VALUE : assertionString();

      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuffer buf = new StringBuffer();

      buf.append(this.getClass().getSimpleName()).append(":{");
      buf.append("src:");
      ConceptComponent.addNidToBuffer(buf, getEnclosingConcept().getNid());
      buf.append(" t:");
      ConceptComponent.addNidToBuffer(buf, getTypeNid());
      buf.append(" dest:");
      ConceptComponent.addNidToBuffer(buf, c2Nid);
      buf.append(" c:");
      ConceptComponent.addNidToBuffer(buf, getCharacteristicNid());
      buf.append(" g:").append(group);
      buf.append(" r:");
      ConceptComponent.addNidToBuffer(buf, getRefinabilityNid());
      buf.append(" ");
      buf.append(super.toString());

      return buf.toString();
   }

   @Override
   public String toUserString() {
      StringBuffer buf = new StringBuffer();

      ConceptComponent.addTextToBuffer(buf, typeNid);
      buf.append(": ");
      ConceptComponent.addTextToBuffer(buf, c2Nid);

      return buf.toString();
   }

   /**
    * Test method to check to see if two objects are equal in all respects.
    * @param another
    * @return either a zero length String, or a String containing a description of the
    * validation failures.
    * @throws IOException
    */
   public String validate(Relationship another) throws IOException {
      assert another != null;

      StringBuilder buf = new StringBuilder();

      if (this.c2Nid != another.c2Nid) {
         buf.append("\tRelationship.initialCaseSignificant not equal: \n"
                    + "\t\tthis.c2Nid = ").append(this.c2Nid).append("\n"
                       + "\t\tanother.c2Nid = ").append(another.c2Nid).append("\n");
      }

      if (this.getCharacteristicNid() != another.getCharacteristicNid()) {
         buf.append(
             "\tRelationship.characteristicNid not equal: \n" + "\t\tthis.characteristicNid = ").append(
             this.getCharacteristicNid()).append("\n" + "\t\tanother.characteristicNid = ").append(
             another.getCharacteristicNid()).append("\n");
      }

      if (this.group != another.group) {
         buf.append("\tRelationship.group not equal: \n"
                    + "\t\tthis.group = ").append(this.group).append("\n"
                       + "\t\tanother.group = ").append(another.group).append("\n");
      }

      if (this.getRefinabilityNid() != another.getRefinabilityNid()) {
         buf.append("\tRelationship.refinabilityNid not equal: \n"
                    + "\t\tthis.refinabilityNid = ").append(this.getRefinabilityNid()).append("\n"
                       + "\t\tanother.refinabilityNid = ").append(another.getRefinabilityNid()).append("\n");
      }

      if (this.getTypeNid() != another.getTypeNid()) {
         buf.append("\tRelationship.typeNid not equal: \n"
                    + "\t\tthis.typeNid = ").append(this.getTypeNid()).append("\n"
                       + "\t\tanother.typeNid = ").append(another.getTypeNid()).append("\n");
      }

      // Compare the parents
      buf.append(super.validate(another));

      return buf.toString();
   }

   @Override
   public void writeToBdb(TupleOutput output, int maxReadOnlyStamp) {

      //
      List<RelationshipRevision> revisionsToWrite = new ArrayList<>();

      if (revisions != null) {
         for (RelationshipRevision p : revisions) {
            if ((p.getStamp() > maxReadOnlyStamp) && (p.getTime() != Long.MIN_VALUE)) {
               revisionsToWrite.add(p);
            }
         }
      }

      // Start writing
      // c1Nid is the enclosing concept, does not need to be written.
      output.writeInt(c2Nid);
      output.writeInt(getCharacteristicNid());
      output.writeSortedPackedInt(group);
      output.writeInt(getRefinabilityNid());
      output.writeInt(getTypeNid());
      output.writeSortedPackedInt(revisionsToWrite.size());

      for (RelationshipRevision p : revisionsToWrite) {
         p.writeRevisionBdb(output);
      }
      
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getCharacteristicNid() {
      return characteristicNid;
   }

   public static int getClassifierAuthorNid() {
      if (classifierAuthorNid == Integer.MIN_VALUE) {
         classifierAuthorNid = ReferenceConcepts.SNOROCKET.getNid();
      }

      return classifierAuthorNid;
   }

   @Override
   public int getDestinationNid() {
      return c2Nid;
   }

   @Override
   public int getGroup() {
      return group;
   }

   @Override
   public int getOriginNid() {
      return enclosingConceptNid;
   }

   @Override
   public Relationship getPrimordialVersion() {
      return Relationship.this;
   }

   @Override
   public int getRefinabilityNid() {
      return refinabilityNid;
   }

   @Override
   public int getTypeNid() {
      return typeNid;
   }

   @Override
   public IntArrayList getVariableVersionNids() {
      IntArrayList nidList = new IntArrayList(7);

      nidList.add(enclosingConceptNid);
      nidList.add(c2Nid);
      nidList.add(getCharacteristicNid());
      nidList.add(getRefinabilityNid());
      nidList.add(getTypeNid());

      return nidList;
   }

   @Override
   public Relationship.Version getVersion(ViewCoordinate c) throws ContradictionException {
      List<Relationship.Version> vForC = getVersions(c);

      if (vForC.isEmpty()) {
         return null;
      }

      if (vForC.size() > 1) {
         vForC = c.getContradictionManager().resolveVersions(vForC);
      }

      if (vForC.size() > 1) {
         throw new ContradictionException(vForC.toString());
      }

      if (!vForC.isEmpty()) {
         return vForC.get(0);
      }

      return null;
   }

   @Override
   public List<Version> getVersions() {
      if (versions == null) {
         int count = 1;

         if (revisions != null) {
            count = count + revisions.size();
         }

         ArrayList<Version> list = new ArrayList<>(count);

         if (getTime() != Long.MIN_VALUE) {
            list.add(new Version(this));
         }

         if (revisions != null) {
            for (RelationshipRevision r : revisions) {
               if (r.getTime() != Long.MIN_VALUE) {
                  list.add(new Version(r));
               }
            }
         }

         versions = list;
      }

      return versions;
   }

   @Override
   public List<Relationship.Version> getVersions(ViewCoordinate c) {
      List<Version> returnValues = new ArrayList<>(2);

      computer.addSpecifiedRelVersions(returnValues, getVersions(), c);

      return returnValues;
   }

   public Collection<Relationship.Version> getVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
           PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr) {
      List<Version> returnTuples = new ArrayList<>(2);

      computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions, returnTuples, getVersions(),
                                    precedence, contradictionMgr);

      return returnTuples;
   }

   @Override
   public boolean isInferred() {
      return (getCharacteristicNid() == INFERRED_NID_RF2) || (getCharacteristicNid() == INFERRED_NID_RF1);
   }

   @Override
   public boolean isStated() {
      return (getCharacteristicNid() == STATED_NID_RF2) || (getCharacteristicNid() == STATED_NID_RF1);
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public final void setCharacteristicNid(int characteristicNid) {
      if (this.characteristicNid != characteristicNid) {
         this.characteristicNid = characteristicNid;
         modified();
      }
   }

   @Override
   public void setDestinationNid(int dNid) throws PropertyVetoException {
      if (this.c2Nid != dNid) {

         // new xref is added on the dbWrite.
         this.c2Nid = dNid;
         modified();
      }
   }

   @Override
   public void setGroup(int group) {
      this.group = group;
      modified();
   }

   @Override
   public final void setRefinabilityNid(int refinabilityNid) {
      if (this.refinabilityNid != refinabilityNid) {
         this.refinabilityNid = refinabilityNid;
         modified();
      }
   }

   @Override
   public final void setTypeNid(int typeNid) {
      if (this.typeNid != typeNid) {
         this.typeNid = typeNid;
         modified();
      }
   }

   //~--- inner classes -------------------------------------------------------

   public class Version extends ConceptComponent<RelationshipRevision, Relationship>.Version
           implements RelationshipAnalogBI<RelationshipRevision>, TypedComponentVersionBI {
      public Version(RelationshipAnalogBI cv) {
         super(cv);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean fieldsEqual(ConceptComponent<RelationshipRevision, Relationship>.Version another) {
         Relationship.Version anotherVersion = (Relationship.Version) another;

         if (this.getC2Nid() != anotherVersion.getC2Nid()) {
            return false;
         }

         if (this.getCharacteristicNid() != anotherVersion.getCharacteristicNid()) {
            return false;
         }

         if (this.getGroup() != anotherVersion.getGroup()) {
            return false;
         }

         if (this.getRefinabilityNid() != anotherVersion.getRefinabilityNid()) {
            return false;
         }

         if (this.getTypeNid() != anotherVersion.getTypeNid()) {
            return false;
         }

         return true;
      }

      public RelationshipRevision makeAnalog() {
         if (Relationship.this != getCv()) {
            RelationshipRevision rev = (RelationshipRevision) getCv();

            return new RelationshipRevision(rev, Relationship.this);
         }

         return new RelationshipRevision(Relationship.this);
      }

      @Override
      public RelationshipRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
         return (RelationshipRevision) getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
      }

      @Override
      public RelationshipCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
         return getCv().makeBlueprint(vc, idDirective, refexDirective);
      }

      //~--- get methods ------------------------------------------------------

      public int getC1Nid() {
         return getEnclosingConcept().getNid();
      }

      public int getC2Nid() {
         return c2Nid;
      }

      @Override
      public int getCharacteristicNid() {
         return getCv().getCharacteristicNid();
      }

      RelationshipAnalogBI getCv() {
         return (RelationshipAnalogBI) cv;
      }

      @Override
      public int getDestinationNid() {
         return Relationship.this.c2Nid;
      }

      @Override
      public int getGroup() {
         return getCv().getGroup();
      }

      @Override
      public int getOriginNid() {
         return Relationship.this.enclosingConceptNid;
      }

      @Override
      public Relationship getPrimordialVersion() {
         return Relationship.this;
      }

      @Override
      public int getRefinabilityNid() {
         return getCv().getRefinabilityNid();
      }

      public ConceptChronicle getType() throws IOException {
         return (ConceptChronicle) P.s.getConcept(getTypeNid());
      }

      @Override
      public int getTypeNid() {
         return getCv().getTypeNid();
      }

      @Override
      public IntArrayList getVariableVersionNids() {
         if (Relationship.this != getCv()) {
            IntArrayList resultList = new IntArrayList(7);

            resultList.add(getCharacteristicNid());
            resultList.add(getRefinabilityNid());
            resultList.add(getTypeNid());
            resultList.add(getC1Nid());
            resultList.add(getC2Nid());

            return resultList;
         }

         return Relationship.this.getVariableVersionNids();
      }

      @Override
      public Relationship.Version getVersion(ViewCoordinate c) throws ContradictionException {
         return Relationship.this.getVersion(c);
      }

      @Override
      public List<? extends Version> getVersions() {
         return Relationship.this.getVersions();
      }

      @Override
      public Collection<Relationship.Version> getVersions(ViewCoordinate c) {
         return Relationship.this.getVersions(c);
      }

      @Override
      public boolean isInferred() {
         return getCv().isInferred();
      }

      @Override
      public boolean isStated() {
         return getCv().isStated();
      }

      //~--- set methods ------------------------------------------------------

      @Override
      public void setCharacteristicNid(int characteristicNid) throws PropertyVetoException {
         getCv().setCharacteristicNid(characteristicNid);
      }

      @Override
      public void setDestinationNid(int destNid) throws PropertyVetoException {
         getCv().setDestinationNid(destNid);
      }

      @Override
      public void setGroup(int group) throws PropertyVetoException {
         getCv().setGroup(group);
      }

      @Override
      public void setRefinabilityNid(int refinabilityNid) throws PropertyVetoException {
         getCv().setRefinabilityNid(refinabilityNid);
      }

      @Override
      public void setTypeNid(int typeNid) throws PropertyVetoException {
         getCv().setTypeNid(typeNid);
      }
   }
}
