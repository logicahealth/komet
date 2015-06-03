package org.ihtsdo.otf.tcc.model.cc.relationship;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;

import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RelationshipCAB;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipType;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipChronicle;
import org.ihtsdo.otf.tcc.dto.component.relationship.TtkRelationshipRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.ReferenceConcepts;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;

public class Relationship extends ConceptComponent<RelationshipRevision, Relationship>
        implements RelationshipAnalogBI<RelationshipRevision> {
   private static int                                   classifierAuthorNid = Integer.MIN_VALUE;
   private static VersionComputer<RelationshipVersion> computer            = new VersionComputer<>();

   //~--- fields --------------------------------------------------------------

   protected int   c2Nid;
   protected int   characteristicNid;
   protected int   group;
   protected int   refinabilityNid;
   protected int   typeNid;
   List<RelationshipVersion> versions;

   //~--- constructors --------------------------------------------------------

   public Relationship() {
      super();
   }

   public Relationship(TtkRelationshipChronicle eRel, ConceptChronicleBI enclosingConcept) throws IOException {
      super(eRel, enclosingConcept.getNid());
      c2Nid = PersistentStore.get().getNidForUuids(eRel.getC2Uuid());
      characteristicNid = PersistentStore.get().getNidForUuids(eRel.getCharacteristicUuid());
      group = eRel.getRelGroup();
      refinabilityNid = PersistentStore.get().getNidForUuids(eRel.getRefinabilityUuid());
      typeNid = PersistentStore.get().getNidForUuids(eRel.getTypeUuid());
      primordialStamp = PersistentStore.get().getStamp(eRel);

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

      if (getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
         throw new InvalidCAB("Inferred relationships can not be used to make blueprints");
      } else if (getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
         relType = RelationshipType.STATED_HIERARCHY;
	  } else if (getCharacteristicNid() == SnomedMetadataRf2.QUALIFYING_RELATIONSSHIP_RF2.getLenient().getNid()) {
	         relType = RelationshipType.QUALIFIER;
	  } else if (getCharacteristicNid() == SnomedMetadataRf2.HISTORICAL_RELATIONSSHIP_RF2.getLenient().getNid()) {
         relType = RelationshipType.HISTORIC;
	  }

      RelationshipCAB relBp = new RelationshipCAB(getOriginNid(), getTypeNid(), getDestinationNid(), getGroup(), relType,
                                getVersion(vc), Optional.of(vc), idDirective, refexDirective);

      return relBp;
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
      ConceptComponent.addNidToBuffer(buf, getConceptNid());
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
   
   public String toSimpleString(){
        StringBuilder buf = new StringBuilder();
        buf.append("-nid: ").append(nid);
        buf.append("-enclosing concept nid: ").append(enclosingConceptNid);
        buf.append("-dest: ").append(c2Nid);
        buf.append("-char: ").append(characteristicNid);
        buf.append("-type: ").append(typeNid);
        buf.append("-group: ").append(group);
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
   public Optional<RelationshipVersion> getVersion(ViewCoordinate c) throws ContradictionException {
      List<RelationshipVersion> vForC = getVersions(c);

      if (vForC.isEmpty()) {
         return Optional.empty();
      }

      if (vForC.size() > 1) {
         vForC = c.getContradictionManager().resolveVersions(vForC);
      }

      if (vForC.size() > 1) {
         throw new ContradictionException(vForC.toString());
      }

      if (!vForC.isEmpty()) {
         return Optional.of(vForC.get(0));
      }

      return Optional.empty();
   }

    @Override
    public List<RelationshipVersion> getVersionList() {
        return getVersions();
    }

    @Override
    public List<RelationshipVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<RelationshipVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new RelationshipVersion(this, this, primordialStamp));
                for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                    list.add(new RelationshipVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (RelationshipRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new RelationshipVersion(r, this, r.stamp));
                        for (int stampAlias : getCommitManager().getAliases(r.stamp)) {
                            list.add(new RelationshipVersion(r, this, stampAlias));
                        }
                    }
                }
            }

            versions = list;
        }

        return versions;
    }

   @Override
   public List<RelationshipVersion> getVersions(ViewCoordinate c) {
      List<RelationshipVersion> returnValues = new ArrayList<>(2);

      computer.addSpecifiedRelVersions(returnValues, getVersions(), c);

      return returnValues;
   }

   public Collection<RelationshipVersion> getVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
           Position viewPosition, Precedence precedence, ContradictionManagerBI contradictionMgr) {
      List<RelationshipVersion> returnTuples = new ArrayList<>(2);

      computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPosition, returnTuples, getVersions(),
                                    precedence, contradictionMgr);

      return returnTuples;
   }

    protected static int inferredNid = Integer.MAX_VALUE;
    protected static int statedNid = Integer.MAX_VALUE;
    @Override
    public boolean isInferred()  {
        if (inferredNid == Integer.MAX_VALUE) {
            inferredNid = PersistentStore.get().getNidForUuids(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
        }
        return characteristicNid == inferredNid;
    }

    @Override
    public boolean isStated() throws IOException {
        if (statedNid == Integer.MAX_VALUE) {
            statedNid = PersistentStore.get().getNidForUuids(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
        }
        return characteristicNid == statedNid;
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

}
