package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_boolean;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javafx.beans.property.SimpleBooleanProperty;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanVersionBI;


public class RefexCompBooleanVersionDdo<T extends RefexChronicleDdo, V extends RefexCompBooleanVersionDdo>
        extends RefexCompVersionDdo<T, V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private SimpleBooleanProperty boolean1Property = new SimpleBooleanProperty(this, "boolean1");

   //~--- constructors --------------------------------------------------------

   public RefexCompBooleanVersionDdo() {
      super();
   }

   public RefexCompBooleanVersionDdo(T chronicle, TerminologySnapshotDI ss, RefexNidBooleanVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.boolean1Property.set(another.getBoolean1());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidStrVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidStrVersion</tt>.
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

      if (RefexCompBooleanVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompBooleanVersionDdo another = (RefexCompBooleanVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare 
         if (this.getBoolean1() != another.getBoolean1()) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" boolean1: ");
      buff.append("'").append(this.boolean1Property.get()).append("'");
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   public boolean getBoolean1() {
      return boolean1Property.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setBoolean1(boolean booleanValue) {
      this.boolean1Property.set(booleanValue);
   }
   
     public SimpleBooleanProperty boolean1Property() {
      return boolean1Property;
   }
}
