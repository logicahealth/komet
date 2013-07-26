package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_array_of_bytearray;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp.RefexCompCompVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_float.RefexCompFloatVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_int.RefexCompIntVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_long.RefexCompLongVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_string.RefexCompStringVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Class description
 *
 *
 * @param <T>
 * @param <V>
 *
 * @version        Enter version here..., 13/04/25
 * @author         Enter your name here...    
 */
@XmlSeeAlso( {
   RefexCompCompVersionDdo.class, RefexCompFloatVersionDdo.class, RefexCompLongVersionDdo.class,
   RefexCompStringVersionDdo.class, RefexCompIntVersionDdo.class
})
public class RefexArrayOfByteArrayVersionDdo<T extends RefexChronicleDdo,
    V extends RefexArrayOfByteArrayVersionDdo> extends RefexVersionDdo<T, V> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   private SimpleObjectProperty<byte[][]> arrayOfByteArrayProperty;

   /**
    * Constructs ...
    *
    */
   public RefexArrayOfByteArrayVersionDdo() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param chronicle
    * @param ss
    * @param another
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public RefexArrayOfByteArrayVersionDdo(T chronicle, TerminologySnapshotDI ss,
       RefexArrayOfBytearrayVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.arrayOfByteArrayProperty = new SimpleObjectProperty(another.getArrayOfByteArray());
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidVersion</tt>.
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

      if (RefexArrayOfByteArrayVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexArrayOfByteArrayVersionDdo another = (RefexArrayOfByteArrayVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.arrayOfByteArrayProperty.equals(another.arrayOfByteArrayProperty)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a string representation of the object.
    *
    * @return
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" c1:");
      buff.append(this.arrayOfByteArrayProperty);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public byte[][] getArrayOfByteArray() {
      return arrayOfByteArrayProperty.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public SimpleObjectProperty<byte[][]> getArrayOfByteArrayProperty() {
      return arrayOfByteArrayProperty;
   }

   /**
    * Method description
    *
    *
    * @param comp1Ref
    */
   public void setArrayOfByteArray(byte[][] comp1Ref) {
      this.arrayOfByteArrayProperty.setValue(comp1Ref);
   }
}
