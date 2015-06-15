package org.ihtsdo.otf.tcc.dto.component.media;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Collection;
import java.util.UUID;

public class TtkMediaRevision extends TtkRevision implements TtkMediaVersion {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public String textDescription;
   public UUID   typeUuid;

   //~--- constructors --------------------------------------------------------

   public TtkMediaRevision() {
      super();
   }

   public TtkMediaRevision(MediaVersionBI another) throws IOException {
      super(another);
      this.textDescription = another.getTextDescription();
      this.typeUuid        = Ts.get().getUuidPrimordialForNid(another.getTypeNid());
   }

   public TtkMediaRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

    @Override
    protected void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
        references.add(this.typeUuid);
    }


   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code EImageVersion} object, and contains the same values, field by field,
    * as this {@code EImageVersion}.
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

      if (TtkMediaRevision.class.isAssignableFrom(obj.getClass())) {
         TtkMediaRevision another = (TtkMediaRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare textDescription
         if (!this.textDescription.equals(another.textDescription)) {
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
      textDescription = in.readUTF();
      typeUuid        = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" desc:");
      buff.append("'").append(this.textDescription).append("'");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeUTF(textDescription);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

    @Override
   public String getTextDescription() {
      return textDescription;
   }

    @Override
   public UUID getTypeUuid() {
      return typeUuid;
   }

   //~--- set methods ---------------------------------------------------------

   public void setTextDescription(String textDescription) {
      this.textDescription = textDescription;
   }

   public void setTypeUuid(UUID typeUuid) {
      this.typeUuid = typeUuid;
   }
}
