package org.ihtsdo.otf.tcc.ddo.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class DescriptionChronicleDdo extends ComponentChronicleDdo<DescriptionVersionDdo, DescriptionVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public DescriptionChronicleDdo() {
      super();
   }

   public DescriptionChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, DescriptionChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getPrimordialVersion());
   }

    @Override
    protected DescriptionVersionDdo makeVersion(TerminologySnapshotDI ss, DescriptionVersionBI version) throws IOException, ContradictionException {
        return new DescriptionVersionDdo(this, ss, version);
    }
}
