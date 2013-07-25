package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_float;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleFloatProperty;

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatVersionBI;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp.RefexCompCompCompVersionDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

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
public class RefexCompCompCompFloatVersionDdo<T extends RefexChronicleDdo,
    V extends RefexCompCompCompFloatVersionDdo> extends RefexCompCompCompVersionDdo<T, V> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   private SimpleFloatProperty float1Property = new SimpleFloatProperty(this, "float1");

   /**
    * Constructs ...
    *
    */
   public RefexCompCompCompFloatVersionDdo() {
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
   public RefexCompCompCompFloatVersionDdo(T chronicle, TerminologySnapshotDI ss,
       RefexNidNidNidFloatVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.float1Property.set(another.getFloat1());
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

      if (RefexCompCompCompFloatVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompCompCompFloatVersionDdo another = (RefexCompCompCompFloatVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         if (this.float1Property.get() != another.float1Property.get()) {
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
      buff.append(this.float1Property.get());
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
   public float getFloat1() {
      return float1Property.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public SimpleFloatProperty getFloat1Property() {
      return float1Property;
   }

   /**
    * Method description
    *
    *
    * @param float1
    */
   public void setFloat1(float float1) {
      this.float1Property.set(float1);
   }
}
