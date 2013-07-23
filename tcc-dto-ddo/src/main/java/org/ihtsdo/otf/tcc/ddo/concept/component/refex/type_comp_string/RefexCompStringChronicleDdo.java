package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_string;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.FXCollections;

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class RefexCompStringChronicleDdo
        extends RefexChronicleDdo<RefexCompStringVersionDdo, RefexNidStringVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public RefexCompStringChronicleDdo() {
      super();
   }

   public RefexCompStringChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected RefexCompStringVersionDdo makeVersion(TerminologySnapshotDI ss, RefexNidStringVersionBI version)
           throws IOException, ContradictionException {
      return new RefexCompStringVersionDdo(this, ss, version);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP_STR;
   }
}
