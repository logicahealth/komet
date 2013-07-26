package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleIntegerProperty;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_int.RefexCompIntVersionDdo;

@XmlSeeAlso( {
    RefexCompIntVersionDdo.class, 
})

public class RefexIntVersionDdo extends RefexVersionDdo<RefexIntChronicleDdo, RefexIntVersionDdo> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private SimpleIntegerProperty int1Property = new SimpleIntegerProperty(this, "int1");

   //~--- constructors --------------------------------------------------------

   public RefexIntVersionDdo() {
      super();
   }

   public RefexIntVersionDdo(RefexIntChronicleDdo chronicle, TerminologySnapshotDI ss, RefexIntVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.int1Property.set(another.getInt1());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetIntVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetIntVersion</tt>.
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

      if (RefexIntVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexIntVersionDdo another = (RefexIntVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare int1Property
         if (this.int1Property.get() != another.int1Property.get()) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   public SimpleIntegerProperty int1Property() {
      return int1Property;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" int: ");
      buff.append(this.int1Property.get());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public int getInt1() {
      return int1Property.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setInt1(int intValue) {
      this.int1Property.set(intValue);
   }
}
