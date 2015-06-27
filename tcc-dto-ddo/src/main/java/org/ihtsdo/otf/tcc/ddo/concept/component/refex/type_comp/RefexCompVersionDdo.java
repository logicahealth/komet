package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;


public class RefexCompVersionDdo<T extends RefexChronicleDdo, V extends RefexCompVersionDdo>
        extends RefexVersionDdo<T, V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private ComponentReference comp1Ref;

   //~--- constructors --------------------------------------------------------

   public RefexCompVersionDdo() {
      super();
   }

   public RefexCompVersionDdo(T chronicle, TaxonomyCoordinate ss,
                             ComponentNidSememe another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.comp1Ref = new ComponentReference(another.getComponentNid(), 
              ss.getStampCoordinate(), ss.getLanguageCoordinate());
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is {@code true}
    * if and only if the argument is not {@code null}, is a
    * {@code ERefsetCidVersion} object, and contains the same values, field by field,
    * as this {@code ERefsetCidVersion}.
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

      if (RefexCompVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompVersionDdo another = (RefexCompVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.comp1Ref.equals(another.comp1Ref)) {
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
      buff.append(" c1:");
      buff.append(this.comp1Ref);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentReference getComp1Ref() {
      return comp1Ref;
   }

   //~--- set methods ---------------------------------------------------------

   public void setComp1Ref(ComponentReference comp1Ref) {
      this.comp1Ref = comp1Ref;
   }
}
