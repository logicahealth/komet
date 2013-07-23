package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp.RefexCompCompVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_float.RefexCompCompCompFloatVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_int.RefexCompCompCompIntVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_long.RefexCompCompCompLongVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_string.RefexCompCompCompStringVersionDdo;

@XmlSeeAlso( {
    RefexCompCompCompFloatVersionDdo.class,
    RefexCompCompCompIntVersionDdo.class,
    RefexCompCompCompLongVersionDdo.class,
    RefexCompCompCompStringVersionDdo.class, 
})
public class RefexCompCompCompVersionDdo<T extends RefexChronicleDdo, V extends RefexCompCompCompVersionDdo>
        extends RefexCompCompVersionDdo<T, V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   private ComponentReference comp3Ref;

   //~--- constructors --------------------------------------------------------

   public RefexCompCompCompVersionDdo() {
      super();
   }

   public RefexCompCompCompVersionDdo(T chronicle, TerminologySnapshotDI ss, RefexNidNidNidVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.comp3Ref = new ComponentReference(ss, another.getNid3());
   }

   //~--- methods -------------------------------------------------------------

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

      if (RefexCompCompCompVersionDdo.class.isAssignableFrom(obj.getClass())) {
         RefexCompCompCompVersionDdo another = (RefexCompCompCompVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         if (!this.comp3Ref.equals(another.comp3Ref)) {
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
      buff.append(" c3:");
      buff.append(this.comp3Ref);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   public ComponentReference getComp3Ref() {
      return comp3Ref;
   }

   //~--- set methods ---------------------------------------------------------

   public void setComp3Ref(ComponentReference comp3Ref) {
      this.comp3Ref = comp3Ref;
   }
}
