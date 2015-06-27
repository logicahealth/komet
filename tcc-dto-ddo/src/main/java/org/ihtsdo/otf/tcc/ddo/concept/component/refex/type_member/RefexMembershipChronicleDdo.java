package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RefexMembershipChronicleDdo
        extends RefexChronicleDdo<RefexMembershipVersionDdo, SememeVersion> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexMembershipChronicleDdo() {
      super();
   }

   public RefexMembershipChronicleDdo(TaxonomyCoordinate ss, ConceptChronicleDdo concept, SememeChronology<SememeVersion> another)
           throws IOException, ContradictionException {
      super(ss, concept, (SememeVersion) another.getVersionList().get(0));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexMembershipVersionDdo makeVersion(TaxonomyCoordinate ss, SememeVersion version)
           throws IOException, ContradictionException {
      return new RefexMembershipVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.MEMBER;
   }
}
