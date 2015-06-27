package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import javafx.beans.property.SimpleLongProperty;

import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

public class RefexLongVersionDdo extends RefexVersionDdo<RefexLongChronicleDdo, RefexLongVersionDdo> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private SimpleLongProperty long1Property = new SimpleLongProperty(this, "long1");

   //~--- constructors --------------------------------------------------------

   public RefexLongVersionDdo() {
      super();
   }

   public RefexLongVersionDdo(RefexLongChronicleDdo chronicle, TaxonomyCoordinate ss,
                             LongSememe another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.long1Property.set(another.getLongValue());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERefsetLongVersion} object, and contains the same values, field by field,
    * as this {@code ERefsetLongVersion}.
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

      if (RefexLongVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexLongVersionDdo another = (RefexLongVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare long1Property
         if (this.long1Property.get() != another.long1Property.get()) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   public SimpleLongProperty long1Property() {
      return long1Property;
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" long1: ");
      buff.append(this.long1Property.get());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public long getLong1() {
      return long1Property.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setLong1(long longValue) {
      this.long1Property.set(longValue);
   }
}
