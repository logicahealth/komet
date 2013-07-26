package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.ddo.ComponentReference;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentVersionDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_array_of_bytearray.RefexArrayOfByteArrayChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_boolean.RefexBooleanChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_boolean.RefexCompBooleanChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp.RefexCompCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp.RefexCompCompCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_float.RefexCompCompCompFloatChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_int.RefexCompCompCompIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_long.RefexCompCompCompLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_string
   .RefexCompCompCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_string.RefexCompCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_float.RefexCompFloatChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_int.RefexCompIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_long.RefexCompLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_string.RefexCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_int.RefexIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member.RefexMembershipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringChronicleDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;

//J-
@XmlSeeAlso( {
   RefexArrayOfByteArrayChronicleDdo.class, 
   RefexBooleanChronicleDdo.class, 
   RefexCompChronicleDdo.class, 
   RefexCompBooleanChronicleDdo.class, 
   RefexCompCompChronicleDdo.class,
   RefexCompCompCompChronicleDdo.class, 
   RefexCompCompCompFloatChronicleDdo.class,
   RefexCompCompCompIntChronicleDdo.class,
   RefexCompCompCompLongChronicleDdo.class,
   RefexCompCompCompStringChronicleDdo.class,
   RefexCompCompStringChronicleDdo.class, 
   RefexCompFloatChronicleDdo.class,
   RefexCompIntChronicleDdo.class,
   RefexCompLongChronicleDdo.class,
   RefexCompStringChronicleDdo.class, 
   RefexIntChronicleDdo.class, 
   RefexLongChronicleDdo.class, 
   RefexMembershipChronicleDdo.class,
   RefexStringChronicleDdo.class, 
})
//J+
public abstract class RefexChronicleDdo<V extends ComponentVersionDdo, T extends RefexVersionBI>
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
   public RefexChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexVersionBI another)
           throws IOException, ContradictionException {
      super(ss, concept, another);
      this.referencedComponentReference      = new ComponentReference(ss,
          another.getReferencedComponentNid());
      this.refexExtensionIdentifierReference =
         new ComponentReference(ss.getConceptVersion(another.getRefexExtensionNid()));
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
