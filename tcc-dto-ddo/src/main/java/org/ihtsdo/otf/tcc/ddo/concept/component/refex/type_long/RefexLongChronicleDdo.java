package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RefexLongChronicleDdo extends RefexChronicleDdo<RefexLongVersionDdo, LongSememe> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexLongChronicleDdo() {
      super();
   }

   public RefexLongChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept, SememeChronology<LongSememe> another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getVersionList().get(0));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexLongVersionDdo makeVersion(TaxonomyCoordinate ss, LongSememe version)
           throws IOException, ContradictionException {
      return new RefexLongVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.LONG;
   }
}
