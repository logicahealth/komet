package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_long;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongVersionBI;

@XmlRootElement()
public class RefexCompCompCompLongChronicleDdo
        extends RefexChronicleDdo<RefexCompCompCompLongVersionDdo, RefexNidNidNidLongVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexCompCompCompLongChronicleDdo() {
      super();
   }

   public RefexCompCompCompLongChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexCompCompCompLongVersionDdo makeVersion(TerminologySnapshotDI ss, RefexNidNidNidLongVersionBI version)
           throws IOException, ContradictionException {
      return new RefexCompCompCompLongVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP_COMP_COMP_LONG;
   }
}
