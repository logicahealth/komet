package org.ihtsdo.otf.tcc.ddo.concept.component.attribute;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeChronicleBI;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;



public class ConceptAttributesChronicleDdo
        extends ComponentChronicleDdo<ConceptAttributesVersionDdo, ConceptAttributeVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ConceptAttributesChronicleDdo() {
      super();
   }

   public ConceptAttributesChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept,
           ConceptAttributeChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected ConceptAttributesVersionDdo makeVersion(TerminologySnapshotDI ss, ConceptAttributeVersionBI version)
           throws IOException, ContradictionException {
      return new ConceptAttributesVersionDdo(this, ss, version);
   }
}
