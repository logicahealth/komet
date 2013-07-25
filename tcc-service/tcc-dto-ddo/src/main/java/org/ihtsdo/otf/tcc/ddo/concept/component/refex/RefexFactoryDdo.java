/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_array_of_bytearray.RefexArrayOfByteArrayChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_boolean.RefexBooleanChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp.RefexCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_boolean.RefexCompBooleanChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp.RefexCompCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp.RefexCompCompCompChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_float.RefexCompCompCompFloatChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_int.RefexCompCompCompIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_long.RefexCompCompCompLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_comp_string
   .RefexCompCompCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_comp_string.RefexCompCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_float.RefexCompFloatChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_int.RefexCompIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_long.RefexCompLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_comp_string.RefexCompStringChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_int.RefexIntChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_long.RefexLongChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_member.RefexMembershipChronicleDdo;
import org.ihtsdo.otf.tcc.ddo.concept.component.refex.type_string.RefexStringChronicleDdo;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 *
 * @author kec
 */
public class RefexFactoryDdo {

   /**
    * Method description
    *
    *
    * @param ss
    * @param concept
    * @param another
    *
    * @return
    *
    * @throws ContradictionException
    * @throws IOException
    */
   public static RefexChronicleDdo make(TerminologySnapshotDI ss, ConceptChronicleDdo concept, RefexChronicleBI another)
           throws IOException, ContradictionException {
      switch (another.getRefexType()) {
      case ARRAY_BYTEARRAY :
         return new RefexArrayOfByteArrayChronicleDdo(ss, concept, another);

      case BOOLEAN :
         return new RefexBooleanChronicleDdo(ss, concept, another);

      case CID :
         return new RefexCompChronicleDdo(ss, concept, another);

      case CID_BOOLEAN :
         return new RefexCompBooleanChronicleDdo(ss, concept, another);

      case CID_CID :
         return new RefexCompCompChronicleDdo(ss, concept, another);

      case CID_CID_CID :
         return new RefexCompCompCompChronicleDdo(ss, concept, another);

      case CID_CID_CID_FLOAT :
         return new RefexCompCompCompFloatChronicleDdo(ss, concept, another);

      case CID_CID_CID_INT :
         return new RefexCompCompCompIntChronicleDdo(ss, concept, another);

      case CID_CID_CID_LONG :
         return new RefexCompCompCompLongChronicleDdo(ss, concept, another);

      case CID_CID_CID_STRING :
         return new RefexCompCompCompStringChronicleDdo(ss, concept, another);

      case CID_CID_STR :
         return new RefexCompCompStringChronicleDdo(ss, concept, another);

      case CID_FLOAT :
         return new RefexCompFloatChronicleDdo();

      case CID_INT :
         return new RefexCompIntChronicleDdo(ss, concept, another);

      case CID_LONG :
         return new RefexCompLongChronicleDdo(ss, concept, another);

      case CID_STR :
         return new RefexCompStringChronicleDdo(ss, concept, another);

      case INT :
         return new RefexIntChronicleDdo(ss, concept, another);

      case LONG :
         return new RefexLongChronicleDdo(ss, concept, another);

      case MEMBER :
         return new RefexMembershipChronicleDdo(ss, concept, another);

      case STR :
         return new RefexStringChronicleDdo(ss, concept, another);

      default :
         throw new UnsupportedOperationException("Can't handle: " + another.getRefexType());
      }
   }
}
