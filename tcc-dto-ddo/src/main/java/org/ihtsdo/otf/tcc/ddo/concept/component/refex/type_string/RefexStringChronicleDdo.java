package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RefexStringChronicleDdo extends RefexChronicleDdo<RefexStringVersionDdo, StringSememe> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexStringChronicleDdo() {
      super();
   }

   public RefexStringChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept, SememeChronology<StringSememe> another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getVersionList().get(0));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexStringVersionDdo makeVersion(TaxonomyCoordinate ss, StringSememe version)
           throws IOException, ContradictionException {
      return new RefexStringVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.STR;
   }
}
