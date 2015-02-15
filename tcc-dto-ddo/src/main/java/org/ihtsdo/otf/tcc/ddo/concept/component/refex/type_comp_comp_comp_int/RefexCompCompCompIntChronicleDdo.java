package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_int;

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
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntVersionBI;

@XmlRootElement()
public class RefexCompCompCompIntChronicleDdo
        extends RefexChronicleDdo<RefexCompCompCompIntVersionDdo, RefexNidNidNidIntVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexCompCompCompIntChronicleDdo() {
      super();
   }

   public RefexCompCompCompIntChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexCompCompCompIntVersionDdo makeVersion(TerminologySnapshotDI ss, RefexNidNidNidIntVersionBI version)
           throws IOException, ContradictionException {
      return new RefexCompCompCompIntVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP_COMP_COMP_FLOAT;
   }
}
