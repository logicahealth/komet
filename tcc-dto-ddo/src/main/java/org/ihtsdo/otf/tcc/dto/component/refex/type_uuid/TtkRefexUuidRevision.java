package org.ihtsdo.otf.tcc.dto.component.refex.type_uuid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public class TtkRefexUuidRevision extends TtkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public UUID uuid1;

   //~--- constructors --------------------------------------------------------

   public TtkRefexUuidRevision() {
      super();
   }

   public TtkRefexUuidRevision(RefexNidVersionBI another) throws IOException {
      super(another);

      TerminologyStoreDI ts = Ts.get();

      this.uuid1 = ts.getUuidPrimordialForNid(another.getNid1());
   }

   public TtkRefexUuidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkRefexUuidRevision(TtkRefexUuidRevision another, ComponentTransformerBI transformer) {
      super(another, transformer);

      this.uuid1 = transformer.transform(another.uuid1, another, ComponentFields.REFEX_COMPONENT_1_UUID);
   }

   //~--- methods -------------------------------------------------------------
   @Override
   protected final void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
       // nothing to add
   }
   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERefsetCidVersion} object, and contains the same values, field by field,
    * as this {@code ERefsetCidVersion}.
    *
    * @param obj the object to compare with.
    * @return {@code true} if the objects are the same;
    *         {@code false} otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TtkRefexUuidRevision.class.isAssignableFrom(obj.getClass())) {
         TtkRefexUuidRevision another = (TtkRefexUuidRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.uuid1.equals(another.uuid1)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TtkRefexUuidRevision makeTransform(ComponentTransformerBI transformer) {
      return new TtkRefexUuidRevision(this, transformer);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      uuid1 = new UUID(in.readLong(), in.readLong());
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
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(uuid1.getMostSignificantBits());
      out.writeLong(uuid1.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getUuid1() {
      return uuid1;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUuid1(UUID uuid1) {
      this.uuid1 = uuid1;
   }
}
