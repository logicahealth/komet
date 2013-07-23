package org.ihtsdo.otf.tcc.dto.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkComponentChronicle;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public abstract class TtkRefexAbstractMemberChronicle<V extends TtkRevision> extends TtkComponentChronicle<V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public UUID componentUuid;
   @XmlAttribute
   public UUID refexExtensionUuid;

   //~--- constructors --------------------------------------------------------

   public TtkRefexAbstractMemberChronicle() {
      super();
   }

   public TtkRefexAbstractMemberChronicle(RefexVersionBI another) throws IOException {
      super(another);
      this.componentUuid = Ts.get().getComponent(another.getReferencedComponentNid()).getPrimordialUuid();
      this.refexExtensionUuid    = Ts.get().getComponent(another.getRefexExtensionNid()).getPrimordialUuid();
   }

   public TtkRefexAbstractMemberChronicle(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TtkRefexAbstractMemberChronicle(TtkRefexAbstractMemberChronicle another, ComponentTransformerBI transformer) {
      super(another, transformer);

         this.componentUuid = transformer.transform(another.componentUuid, another, ComponentFields.REFEX_REFERENCED_COMPONENT_UUID);
         this.refexExtensionUuid    = transformer.transform(another.refexExtensionUuid, another, ComponentFields.REFEX_COLLECTION_UUID);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefset</tt> object, and contains the same values, field by field,
    * as this <tt>ERefset</tt>.
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

      if (TtkRefexAbstractMemberChronicle.class.isAssignableFrom(obj.getClass())) {
         TtkRefexAbstractMemberChronicle<?> another = (TtkRefexAbstractMemberChronicle<?>) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare refsetUuid
         if (!this.refexExtensionUuid.equals(another.refexExtensionUuid)) {
            return false;
         }

         // Compare componentUuid
         if (!this.componentUuid.equals(another.componentUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefset</code>.
    *
    * @return a hash code value for this <tt>ERefset</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      refexExtensionUuid    = new UUID(in.readLong(), in.readLong());
      componentUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" refex:");
      buff.append(informAboutUuid(this.refexExtensionUuid));
      buff.append(" component:");
      buff.append(informAboutUuid(this.componentUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(refexExtensionUuid.getMostSignificantBits());
      out.writeLong(refexExtensionUuid.getLeastSignificantBits());
      out.writeLong(componentUuid.getMostSignificantBits());
      out.writeLong(componentUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getComponentUuid() {
      return componentUuid;
   }

   public UUID getRefexExtensionUuid() {
      return refexExtensionUuid;
   }

   public abstract RefexType getType();

   //~--- set methods ---------------------------------------------------------

   public void setComponentUuid(UUID componentUuid) {
      this.componentUuid = componentUuid;
   }

   public void setRefexExtensionUuid(UUID refexExtensionUuid) {
      this.refexExtensionUuid = refexExtensionUuid;
   }
}
