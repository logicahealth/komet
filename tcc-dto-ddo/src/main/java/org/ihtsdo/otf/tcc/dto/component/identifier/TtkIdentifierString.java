package org.ihtsdo.otf.tcc.dto.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.id.StringIdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAttribute;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentFields;
import org.ihtsdo.otf.tcc.dto.component.transformer.ComponentTransformerBI;

public class TtkIdentifierString extends TtkIdentifier {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   @XmlAttribute
   public String denotation;

   //~--- constructors --------------------------------------------------------

   public TtkIdentifierString() {
      super();
   }

   public TtkIdentifierString(StringIdBI id) throws IOException {
      super(id);
      denotation = id.getDenotation();
   }

   public TtkIdentifierString(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readUTF();
   }

   public TtkIdentifierString(TtkIdentifierString another, ComponentTransformerBI transformer) {
      super(another, transformer);
      this.denotation = transformer.transform(another.denotation, another, ComponentFields.ID_STRING_DENOTATION);
   }


   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersionString</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersionString</tt>.
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

      if (TtkIdentifierString.class.isAssignableFrom(obj.getClass())) {
         TtkIdentifierString another = (TtkIdentifierString) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare denotation
         if (!this.denotation.equals(another.denotation)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersionString</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersionString</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { denotation.hashCode(), status.hashCode(), pathUuid.hashCode(),
                                         (int) time, (int) (time >>> 32) });
   }

   @Override
   public TtkIdentifierString makeTransform(ComponentTransformerBI transformer) {
      return new TtkIdentifierString(this, transformer);
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" denotation:");
      buff.append("'").append(this.denotation).append("'");
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeDenotation(DataOutput out) throws IOException {
      out.writeUTF(denotation);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getDenotation() {
      return denotation;
   }

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.STRING;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object denotation) {
      this.denotation = (String) denotation;
   }
}
