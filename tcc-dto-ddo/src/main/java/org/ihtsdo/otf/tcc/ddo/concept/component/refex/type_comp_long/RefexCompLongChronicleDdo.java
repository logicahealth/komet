package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_long;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RefexCompLongChronicleDdo
        extends RefexChronicleDdo<RefexCompLongVersionDdo, RefexNidLongVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexCompLongChronicleDdo() {
      super();
   }

   public RefexCompLongChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexCompLongVersionDdo makeVersion(TerminologySnapshotDI ss, RefexNidLongVersionBI version)
           throws IOException, ContradictionException {
      return new RefexCompLongVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP_LONG;
   }
}
