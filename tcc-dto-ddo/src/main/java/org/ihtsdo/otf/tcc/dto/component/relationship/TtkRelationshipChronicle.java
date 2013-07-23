package org.ihtsdo.otf.tcc.dto.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

@XmlRootElement(name="relationship")
public class TtkRelationshipChronicle extends TtkComponentChronicle<TtkRelationshipRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public UUID c1Uuid;
   @XmlAttribute
   public UUID c2Uuid;
   @XmlAttribute
   public UUID characteristicUuid;
   @XmlAttribute
   public UUID refinabilityUuid;
   @XmlAttribute
   public int  group;
   @XmlAttribute
   public UUID typeUuid;

   //~--- constructors --------------------------------------------------------

   public TtkRelationshipChronicle() {
      super();
   }

   public TtkRelationshipChronicle(RelationshipChronicleBI rel) throws IOException {
      super(rel.getPrimordialVersion());

      Collection<? extends RelationshipVersionBI> rels      = rel.getVersions();
      int                                         partCount = rels.size();
      Iterator<? extends RelationshipVersionBI>   relItr    = rels.iterator();
      TerminologyStoreDI                          ts        = Ts.get();
      RelationshipVersionBI                       rv        = relItr.next();

      c1Uuid             = ts.getUuidPrimordialForNid(rv.getConceptNid());
      c2Uuid             = ts.getUuidPrimordialForNid(rv.getDestinationNid());
      characteristicUuid = ts.getUuidPrimordialForNid(rv.getCharacteristicNid());
      refinabilityUuid   = ts.getUuidPrimordialForNid(rv.getRefinabilityNid());
      group           = rv.getGroup();
      typeUuid           = ts.getUuidPrimordialForNid(rv.getTypeNid());
      pathUuid           = ts.getUuidPrimordialForNid(rv.getPathNid());
      status         = rv.getStatus();
      time               = rv.getTime();

      if (partCount > 1) {
         revisions = new ArrayList<>(partCount - 1);

         while (relItr.hasNext()) {
            rv = relItr.next();
            revisions.add(new TtkRelationshipRevision(rv));
         }
      }
   }

   public TtkRelationshipChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkRelationshipChronicle(TtkRelationshipChronicle another, ComponentTransformerBI transformer) {
      super(another, transformer); 

         this.c1Uuid             = transformer.transform(another.c1Uuid, another, ComponentFields.RELATIONSHIP_ORIGIN_UUID);
         this.c2Uuid             = transformer.transform(another.c2Uuid, another, ComponentFields.RELATIONSHIP_DESTINATION_UUID);
         this.characteristicUuid = transformer.transform(another.characteristicUuid, another, ComponentFields.RELATIONSHIP_CHARACTERISTIC_UUID);
         this.refinabilityUuid   = transformer.transform(another.refinabilityUuid, another, ComponentFields.RELATIONSHIP_REFINABILITY_UUID);
         this.group           = transformer.transform(another.group, another, ComponentFields.RELATIONSHIP_GROUP);
         this.typeUuid           = transformer.transform(another.typeUuid, another, ComponentFields.RELATIONSHIP_TYPE_UUID);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERelationship</tt> object, and contains the same values,
    * field by field, as this <tt>ERelationship</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TtkRelationshipChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkRelationshipChronicle another = (TtkRelationshipChronicle) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare c2Uuid
         if (!this.c2Uuid.equals(another.c2Uuid)) {
            return false;
         }

         // Compare characteristicUuid
         if (!this.characteristicUuid.equals(another.characteristicUuid)) {
            return false;
         }

         // Compare refinabilityUuid
         if (!this.refinabilityUuid.equals(another.refinabilityUuid)) {
            return false;
         }

         // Compare relGroup
         if (this.group != another.group) {
            return false;
         }

         // Compare typeUuid
         if (!this.typeUuid.equals(another.typeUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERelationship</code>.
    *
    * @return a hash code value for this <tt>ERelationship</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TtkRelationshipChronicle makeTransform(ComponentTransformerBI transformer) {
      return new TtkRelationshipChronicle(this, transformer);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid             = new UUID(in.readLong(), in.readLong());
      c2Uuid             = new UUID(in.readLong(), in.readLong());
      characteristicUuid = new UUID(in.readLong(), in.readLong());
      refinabilityUuid   = new UUID(in.readLong(), in.readLong());
      group           = in.readInt();
      typeUuid           = new UUID(in.readLong(), in.readLong());
      if (typeUuid.equals(TermAux.IS_A.getUuids()[0])) {
          typeUuid = Snomed.IS_A.getUuids()[0];
      }

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TtkRelationshipRevision(in, dataVersion));
         }
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" c1: ");
      buff.append(informAboutUuid(this.c1Uuid));
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" c2: ");
      buff.append(informAboutUuid(this.c2Uuid));
      buff.append(" grp:");
      buff.append(this.group);
      buff.append(" char: ");
      buff.append(informAboutUuid(this.characteristicUuid));
      buff.append(" ref: ");
      buff.append(informAboutUuid(this.refinabilityUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
      out.writeLong(c2Uuid.getMostSignificantBits());
      out.writeLong(c2Uuid.getLeastSignificantBits());
      out.writeLong(characteristicUuid.getMostSignificantBits());
      out.writeLong(characteristicUuid.getLeastSignificantBits());
      out.writeLong(refinabilityUuid.getMostSignificantBits());
      out.writeLong(refinabilityUuid.getLeastSignificantBits());
      out.writeInt(group);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TtkRelationshipRevision erv : revisions) {
            erv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getC1Uuid()
    */
   public UUID getC1Uuid() {
      return c1Uuid;
   }

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getC2Uuid()
    */
   public UUID getC2Uuid() {
      return c2Uuid;
   }

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getCharacteristicUuid()
    */
   public UUID getCharacteristicUuid() {
      return characteristicUuid;
   }

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRefinabilityUuid()
    */
   public UUID getRefinabilityUuid() {
      return refinabilityUuid;
   }

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRelGroup()
    */
   public int getRelGroup() {
      return group;
   }

   @Override
   public List<TtkRelationshipRevision> getRevisionList() {
      return revisions;
   }

   /*
    *  (non-Javadoc)
    * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getTypeUuid()
    */
   public UUID getTypeUuid() {
      return typeUuid;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setC2Uuid(UUID c2Uuid) {
      this.c2Uuid = c2Uuid;
   }

   public void setCharacteristicUuid(UUID characteristicUuid) {
      this.characteristicUuid = characteristicUuid;
   }

   public void setRefinabilityUuid(UUID refinabilityUuid) {
      this.refinabilityUuid = refinabilityUuid;
   }

   public void setRelGroup(int relGroup) {
      this.group = relGroup;
   }

   public void setTypeUuid(UUID typeUuid) {
      this.typeUuid = typeUuid;
   }
}
