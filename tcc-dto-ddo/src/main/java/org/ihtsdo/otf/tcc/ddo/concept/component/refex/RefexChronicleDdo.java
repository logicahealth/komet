package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member.RefexMembershipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringChronicleDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;

//J-
@XmlSeeAlso( {
   RefexCompChronicleDdo.class, 
   RefexLongChronicleDdo.class, 
   RefexMembershipChronicleDdo.class,
   RefexStringChronicleDdo.class, 
})
//J+
public abstract class RefexChronicleDdo<V extends ComponentVersionDdo, T extends SememeVersion>
        extends ComponentChronicleDdo<V, T> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /** Field description */
   protected ComponentReference referencedComponentReference;

   /** Field description */
   protected ComponentReference refexExtensionIdentifierReference;

   /**
    * Constructs ...
    *
    */
   public RefexChronicleDdo() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param ss
    * @param concept
    * @param another
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public RefexChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept, T another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getChronology());
      this.referencedComponentReference      = new ComponentReference(
          another.getReferencedComponentNid(), 
              ss.getStampCoordinate(), ss.getLanguageCoordinate());
      this.refexExtensionIdentifierReference =
         new ComponentReference(Get.identifierService().getConceptNid(another.getAssemblageSequence()), 
              ss.getStampCoordinate(), ss.getLanguageCoordinate());
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public ComponentReference getReferencedComponentReference() {
      return referencedComponentReference;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public ComponentReference getRefexExtensionIdentifierReference() {
      return refexExtensionIdentifierReference;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public abstract REFEX_TYPE_DDO getType();

   /**
    * Method description
    *
    *
    * @param componentReference
    */
   public void setReferencedComponentReference(ComponentReference componentReference) {
      this.referencedComponentReference = componentReference;
   }

   /**
    * Method description
    *
    *
    * @param refexRef
    */
   public void setRefexExtensionIdentifierReference(ComponentReference refexRef) {
      this.refexExtensionIdentifierReference = refexRef;
   }
}
