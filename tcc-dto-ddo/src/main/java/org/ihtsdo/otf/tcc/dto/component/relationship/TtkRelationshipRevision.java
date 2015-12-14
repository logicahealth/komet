  package org.ihtsdo.otf.tcc.dto.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.relationship.RelationshipVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;

@XmlRootElement(name="relationship-revision")
public class TtkRelationshipRevision extends TtkRevision implements TtkRelationshipVersion {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public UUID characteristicUuid;
   @XmlAttribute
   public int  group;
   @XmlAttribute
   public UUID refinabilityUuid;
   @XmlAttribute
   public UUID typeUuid;

   //~--- constructors --------------------------------------------------------

   public TtkRelationshipRevision() {
      super();
   }

   public TtkRelationshipRevision(RelationshipVersionBI rv) throws IOException {
      super(rv);
      TerminologyStoreDI ts = Ts.get();

      characteristicUuid = ts.getUuidPrimordialForNid(rv.getCharacteristicNid());
      refinabilityUuid   = ts.getUuidPrimordialForNid(rv.getRefinabilityNid());
      group              = rv.getGroup();
      typeUuid           = ts.getUuidPrimordialForNid(rv.getTypeNid());
   }

   public TtkRelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }
   //~--- methods -------------------------------------------------------------
   @Override
   protected void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
       references.add(this.typeUuid);
       references.add(this.characteristicUuid);
       references.add(this.refinabilityUuid);
   }
   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERelationshipVersion} object, and contains the same values,
    * field by field, as this {@code ERelationshipVersion}.
    *
    * @param obj the object to compare with.
    * @return {@code true} if the objects are the same;
    *         {@code false} otherwise.
    */
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TtkRelationshipRevision.class.isAssignableFrom(obj.getClass())) {
         TtkRelationshipRevision another = (TtkRelationshipRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare characteristicUuid
         if (!this.characteristicUuid.equals(another.characteristicUuid)) {
            return false;
         }

         // Compare refinabilityUuid
         if (!this.refinabilityUuid.equals(another.refinabilityUuid)) {
            return false;
         }

         // Compare group
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

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      characteristicUuid = new UUID(in.readLong(), in.readLong());
      refinabilityUuid   = new UUID(in.readLong(), in.readLong());
      group              = in.readInt();
      typeUuid           = new UUID(in.readLong(), in.readLong());
      if (typeUuid.equals(TermAux.IS_A.getUuids()[0])) {
          typeUuid = Snomed.IS_A.getUuids()[0];
      }
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" grp:");
      buff.append(this.group);
      buff.append(" char:");
      buff.append(this.characteristicUuid);
      buff.append(" ref:");
      buff.append(this.refinabilityUuid);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(characteristicUuid.getMostSignificantBits());
      out.writeLong(characteristicUuid.getLeastSignificantBits());
      out.writeLong(refinabilityUuid.getMostSignificantBits());
      out.writeLong(refinabilityUuid.getLeastSignificantBits());
      out.writeInt(group);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

    @Override
   public UUID getCharacteristicUuid() {
      return characteristicUuid;
   }

    @Override
   public int getGroup() {
      return group;
   }

    @Override
   public int getRelGroup() {
      return group;
   }

    @Override
   public UUID getRefinabilityUuid() {
      return refinabilityUuid;
   }

    @Override
   public UUID getTypeUuid() {
      return typeUuid;
   }

   //~--- set methods ---------------------------------------------------------

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
