package org.ihtsdo.otf.tcc.ddo.concept.component.media;

//~--- non-JDK imports --------------------------------------------------------


import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.ComponentChronicleDdo;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.media.MediaChronicleBI;
import org.ihtsdo.otf.tcc.api.media.MediaVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class MediaChronicleDdo extends ComponentChronicleDdo<MediaVersionDdo, MediaVersionBI> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public byte[] dataBytes;
   public String format;

   //~--- constructors --------------------------------------------------------

   public MediaChronicleDdo() {
      super();
   }

   public MediaChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept, MediaChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, another.getPrimordialVersion());

      this.dataBytes = another.getPrimordialVersion().getMedia();
      this.format    = another.getPrimordialVersion().getFormat();
   }

   //~--- get methods ---------------------------------------------------------

   public byte[] getDataBytes() {
      return dataBytes;
   }

   public String getFormat() {
      return format;
   }

    @Override
    protected MediaVersionDdo makeVersion(TerminologySnapshotDI ss, MediaVersionBI version) throws IOException, ContradictionException {
        return new MediaVersionDdo(this, ss, version);
    }
}
