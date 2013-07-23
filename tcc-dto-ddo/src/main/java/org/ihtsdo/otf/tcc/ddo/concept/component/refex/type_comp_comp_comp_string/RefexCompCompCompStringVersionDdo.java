package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_string;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp.RefexCompCompCompVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringVersionBI;

/**
 * Class description
 *
 *
 * @param <T>
 * @param <V>
 *
 * @version        Enter version here..., 13/04/24
 * @author         Enter your name here...    
 */
public class RefexCompCompCompStringVersionDdo<T extends RefexChronicleDdo,
    V extends RefexCompCompCompStringVersionDdo> extends RefexCompCompCompVersionDdo<T, V> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   private SimpleStringProperty string1Property = new SimpleStringProperty(this, "string1");

   /**
    * Constructs ...
    *
    */
   public RefexCompCompCompStringVersionDdo() {
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
   public RefexCompCompCompStringVersionDdo(T chronicle, TerminologySnapshotDI ss,
       RefexNidNidNidStringVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.string1Property.set(another.getString1());
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidCidVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidCidVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (RefexCompCompCompStringVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompCompCompStringVersionDdo another = (RefexCompCompCompStringVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         if (this.string1Property.get().equals(another.string1Property.get())) {
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
      buff.append(" string1:");
      buff.append(this.string1Property.get());
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
   public String getString1() {
      return string1Property.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public SimpleStringProperty getString1Property() {
      return string1Property;
   }

   /**
    * Method description
    *
    *
    * @param float1
    */
   public void setString1(String string1) {
      this.string1Property.set(string1);
   }
}
