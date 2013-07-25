package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_string;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringVersionBI;

@XmlRootElement()
public class RefexCompCompCompStringChronicleDdo
        extends RefexChronicleDdo<RefexCompCompCompStringVersionDdo, RefexNidNidNidStringVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexCompCompCompStringChronicleDdo() {
      super();
   }

   public RefexCompCompCompStringChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexCompCompCompStringVersionDdo makeVersion(TerminologySnapshotDI ss, RefexNidNidNidStringVersionBI version)
           throws IOException, ContradictionException {
      return new RefexCompCompCompStringVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP_COMP_COMP_STRING;
   }
}
