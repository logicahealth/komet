package org.ihtsdo.otf.tcc.dto.component.refex.type_uuid_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public class TtkRefexUuidUuidUuidMemberChronicle extends TtkRefexAbstractMemberChronicle<TtkRefexUuidUuidUuidRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public UUID uuid1;
   @XmlAttribute
   public UUID uuid2;
   @XmlAttribute
   public UUID uuid3;

   //~--- constructors --------------------------------------------------------

   public TtkRefexUuidUuidUuidMemberChronicle() {
      super();
   }

   public TtkRefexUuidUuidUuidMemberChronicle(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                               ts        = Ts.get();
      Collection<? extends RefexNidNidNidVersionBI> refexes   = another.getVersions();
      int                                              partCount = refexes.size();
      Iterator<? extends RefexNidNidNidVersionBI>   itr       = refexes.iterator();
      RefexNidNidNidVersionBI                       rv        = itr.next();

      this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
      this.uuid2 = ts.getUuidPrimordialForNid(rv.getNid2());
      this.uuid3 = ts.getUuidPrimordialForNid(rv.getNid3());

      if (partCount > 1) {
         revisions = new ArrayList<>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TtkRefexUuidUuidUuidRevision(rv));
         }
      }
   }

   public TtkRefexUuidUuidUuidMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkRefexUuidUuidUuidMemberChronicle(TtkRefexUuidUuidUuidMemberChronicle another, ComponentTransformerBI transformer) {
      super(another, transformer);
      this.uuid1 = transformer.transform(another.uuid1, another, ComponentFields.REFEX_COMPONENT_1_UUID);
      this.uuid2 = transformer.transform(another.uuid2, another, ComponentFields.REFEX_COMPONENT_2_UUID);
      this.uuid3 = transformer.transform(another.uuid3, another, ComponentFields.REFEX_COMPONENT_3_UUID);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidCidMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidCidMember</tt>.
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

      if (TtkRefexUuidUuidUuidMemberChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkRefexUuidUuidUuidMemberChronicle another = (TtkRefexUuidUuidUuidMemberChronicle) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.uuid1.equals(another.uuid1)) {
            return false;
         }

         // Compare c2Uuid
         if (!this.uuid2.equals(another.uuid2)) {
            return false;
         }

         // Compare c3Uuid
         if (!this.uuid3.equals(another.uuid3)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetCidCidCidMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidCidCidMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TtkRefexUuidUuidUuidMemberChronicle makeTransform(ComponentTransformerBI transformer) {
      return new TtkRefexUuidUuidUuidMemberChronicle(this, transformer);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      uuid1 = new UUID(in.readLong(), in.readLong());
      uuid2 = new UUID(in.readLong(), in.readLong());
      uuid3 = new UUID(in.readLong(), in.readLong());

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TtkRefexUuidUuidUuidRevision(in, dataVersion));
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
      buff.append(" c1:");
      buff.append(informAboutUuid(this.uuid1));
      buff.append(" c2:");
      buff.append(informAboutUuid(this.uuid2));
      buff.append(" c3:");
      buff.append(informAboutUuid(this.uuid3));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(uuid1.getMostSignificantBits());
      out.writeLong(uuid1.getLeastSignificantBits());
      out.writeLong(uuid2.getMostSignificantBits());
      out.writeLong(uuid2.getLeastSignificantBits());
      out.writeLong(uuid3.getMostSignificantBits());
      out.writeLong(uuid3.getLeastSignificantBits());

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TtkRefexUuidUuidUuidRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getUuid1() {
      return uuid1;
   }

   public UUID getUuid2() {
      return uuid2;
   }

   public UUID getUuid3() {
      return uuid3;
   }

   @Override
   public List<TtkRefexUuidUuidUuidRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public RefexType getType() {
      return RefexType.CID_CID_CID;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUuid1(UUID uuid1) {
      this.uuid1 = uuid1;
   }

   public void setUuid2(UUID uuid2) {
      this.uuid2 = uuid2;
   }

   public void setUuid3(UUID uuid3) {
      this.uuid3 = uuid3;
   }
}
