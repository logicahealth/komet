package org.ihtsdo.otf.tcc.ddo.concept.component.attribute;

//~--- non-JDK imports --------------------------------------------------------


import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;



public class ConceptAttributesChronicleDdo
        extends ComponentChronicleDdo<ConceptAttributesVersionDdo, ConceptVersion> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public ConceptAttributesChronicleDdo() {
      super();
   }

   public ConceptAttributesChronicleDdo(TaxonomyCoordinate taxonomyCoordinate, ConceptChronicleDdo concept,
           ObjectChronology<? extends ConceptVersion> another)
           throws IOException, ContradictionException {
      super(taxonomyCoordinate, concept, another);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected ConceptAttributesVersionDdo makeVersion(TaxonomyCoordinate taxonomyCoordinate, ConceptVersion version)
           throws IOException, ContradictionException {
      return new ConceptAttributesVersionDdo(this, taxonomyCoordinate, version);
   }
}
