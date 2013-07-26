package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_int;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp.RefexCompCompCompVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javafx.beans.property.SimpleIntegerProperty;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntVersionBI;

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
public class RefexCompCompCompIntVersionDdo<T extends RefexChronicleDdo,
    V extends RefexCompCompCompIntVersionDdo> extends RefexCompCompCompVersionDdo<T, V> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   private SimpleIntegerProperty int1Property = new SimpleIntegerProperty(this, "int1");

   /**
    * Constructs ...
    *
    */
   public RefexCompCompCompIntVersionDdo() {
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
   public RefexCompCompCompIntVersionDdo(T chronicle, TerminologySnapshotDI ss,
       RefexNidNidNidIntVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.int1Property.set(another.getInt1());
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

      if (RefexCompCompCompIntVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompCompCompIntVersionDdo another = (RefexCompCompCompIntVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         if (this.int1Property.get() != another.int1Property.get()) {
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
      buff.append(" float1:");
      buff.append(this.int1Property.get());
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
   public float getInt1() {
      return int1Property.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public SimpleIntegerProperty getInt1Property() {
      return int1Property;
   }

   /**
    * Method description
    *
    *
    * @param float1
    */
   public void setInt1(int int1) {
      this.int1Property.set(int1);
   }
}
