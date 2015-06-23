package org.ihtsdo.otf.tcc.ddo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------


import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class DescriptionChronicleDdo extends ComponentChronicleDdo<DescriptionVersionDdo, DescriptionSememe> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public DescriptionChronicleDdo() {
      super();
   }

   public DescriptionChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept, SememeChronology<? extends DescriptionSememe> another)
           throws IOException, ContradictionException {
      super(ss, concept, another);
   }

    @Override
    protected DescriptionVersionDdo makeVersion(TaxonomyCoordinate ss, DescriptionSememe version) throws IOException, ContradictionException {
        return new DescriptionVersionDdo(this, ss, version);
    }
}
