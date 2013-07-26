package org.ihtsdo.otf.tcc.ddo.concept.component.attribute;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleBooleanProperty;

import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentVersionDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;


public class ConceptAttributesVersionDdo
        extends ComponentVersionDdo<ConceptAttributesChronicleDdo, ConceptAttributesVersionDdo> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   protected SimpleBooleanProperty definedProperty = new SimpleBooleanProperty(this, "defined");

   //~--- constructors --------------------------------------------------------

   public ConceptAttributesVersionDdo() {
      super();
   }

   public ConceptAttributesVersionDdo(ConceptAttributesChronicleDdo chronicle, TerminologySnapshotDI ss,
                                     ConceptAttributeVersionBI another)
           throws IOException, ContradictionException {
      super(chronicle, ss, another);
      this.definedProperty.set(another.isDefined());
   }

   //~--- methods -------------------------------------------------------------

   public SimpleBooleanProperty definedProperty() {
      return definedProperty;
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EConceptAttributesVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EConceptAttributesVersion</tt>.
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

      if (ConceptAttributesVersionDdo.class.isAssignableFrom(obj.getClass())) {
         ConceptAttributesVersionDdo another = (ConceptAttributesVersionDdo) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare definedProperty
         if (this.definedProperty != another.definedProperty) {
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
      buff.append(" defined:");
      buff.append(this.definedProperty.get());
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_ConceptualizeExternally#isDefined()
    */
   public boolean isDefined() {
      return definedProperty.get();
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefined(boolean defined) {
      this.definedProperty.set(defined);
   }
}
