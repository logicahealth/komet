package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp.RefexCompCompVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_float.RefexCompFloatVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_int.RefexCompIntVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_long.RefexCompLongVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_string.RefexCompStringVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_boolean.RefexCompBooleanVersionDdo;

@XmlSeeAlso( {
    RefexCompBooleanVersionDdo.class,
    RefexCompCompVersionDdo.class, 
    RefexCompFloatVersionDdo.class, 
    RefexCompIntVersionDdo.class,
    RefexCompLongVersionDdo.class,
    RefexCompStringVersionDdo.class, 
})
public class RefexCompVersionDdo<T extends RefexChronicleDdo, V extends RefexCompVersionDdo>
        extends RefexVersionDdo<T, V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private ComponentReference comp1Ref;

   //~--- constructors --------------------------------------------------------

   public RefexCompVersionDdo() {
      super();
   }

   public RefexCompVersionDdo(T chronicle, TerminologySnapshotDI ss,
                             RefexNidVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.comp1Ref = new ComponentReference(ss, another.getNid1());
   }

   //~--- methods -------------------------------------------------------------

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
