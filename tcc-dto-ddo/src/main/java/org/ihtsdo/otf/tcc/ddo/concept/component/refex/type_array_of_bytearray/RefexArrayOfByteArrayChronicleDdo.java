package org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_array_of_bytearray;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.REFEX_TYPE_DDO;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.RefexChronicleDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class description
 *
 *
 * @version        Enter version here..., 13/04/25
 * @author         Enter your name here...    
 */
@XmlRootElement()
public class RefexArrayOfByteArrayChronicleDdo
        extends RefexChronicleDdo<RefexArrayOfByteArrayVersionDdo, RefexArrayOfBytearrayVersionBI> {

   /** Field description */
   public static final long serialVersionUID = 1;

   /**
    * Constructs ...
    *
    */
   public RefexArrayOfByteArrayChronicleDdo() {
      super();
   }

   /**
    * Constructs ...
    *
    *
    * @param ss
    * @param concept
    * @param another
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public RefexArrayOfByteArrayChronicleDdo(TerminologySnapshotDI ss, ConceptChronicleDdo concept,
       RefexChronicleBI another)
           throws IOException, ContradictionException {
      super(ss, concept, (RefexVersionBI) another.getPrimordialVersion());
   }

   /**
    * Method description
    *
    *
    * @param ss
    * @param version
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   @Override
   protected RefexArrayOfByteArrayVersionDdo makeVersion(TerminologySnapshotDI ss,
       RefexArrayOfBytearrayVersionBI version)
           throws IOException, ContradictionException {
      return new RefexArrayOfByteArrayVersionDdo(this, ss, version);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public REFEX_TYPE_DDO getType() {
      return REFEX_TYPE_DDO.COMP;
   }
}
