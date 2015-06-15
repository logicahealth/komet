package org.ihtsdo.otf.tcc.dto.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.id.LongIdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.ddo.concept.component.identifier.IDENTIFIER_PART_TYPES;

public class TtkIdentifierLong extends TtkIdentifier {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public long denotation;

   //~--- constructors --------------------------------------------------------

   public TtkIdentifierLong() {
      super();
   }

   public TtkIdentifierLong(LongIdBI id) throws IOException {
      super(id);
      denotation = id.getDenotation();
   }

   public TtkIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readLong();
   }

   //~--- methods -------------------------------------------------------------
   @Override
   protected void addUuidReferencesForRevisionComponent(Collection<UUID> references) {
       references.add(this.authorityUuid);
   }
   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code EIdentifierVersionLong} object, and contains the same values, field by field,
    * as this {@code EIdentifierVersionLong}.
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

      if (TtkIdentifierLong.class.isAssignableFrom(obj.getClass())) {
         TtkIdentifierLong another = (TtkIdentifierLong) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare denotation
         if (this.denotation != another.denotation) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this {@code EIdentifierVersionLong}.
    *
    * @return a hash code value for this {@code EIdentifierVersionLong}.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] {
         (int) denotation, (int) (denotation >>> 32), status.hashCode(), pathUuid.hashCode(), (int) time,
         (int) (time >>> 32)
      });
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" denotation:");
      buff.append(this.denotation);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeDenotation(DataOutput out) throws IOException {
      out.writeLong(denotation);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Long getDenotation() {
      return denotation;
   }

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.LONG;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object denotation) {
      this.denotation = (Long) denotation;
   }
}
