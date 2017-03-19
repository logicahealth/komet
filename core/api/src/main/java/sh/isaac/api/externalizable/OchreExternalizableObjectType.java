/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.externalizable;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

//~--- enums ------------------------------------------------------------------

/**
 *
 * @author kec
 */
public enum OchreExternalizableObjectType {
   /**
    * An external representation of a concept. An identifier with status. Descriptions and definitions of concepts
    * are provided as SEMEMEs.
    */
   CONCEPT((byte) 1),

   /**
    * An external representation of a semantic unit of meaning, associated with a concept or another SEMEME.
    */
   SEMEME((byte) 2),

   /**
    * An external representation of a stamp comment.
    */
   STAMP_COMMENT((byte) 4),

   /**
    * An external representation of a stamp alias.
    */
   STAMP_ALIAS((byte) 5);

   private final byte token;

   //~--- constructors --------------------------------------------------------

   private OchreExternalizableObjectType(byte token) {
      this.token = token;
   }

   //~--- methods -------------------------------------------------------------

   public static OchreExternalizableObjectType fromDataStream(DataInput input)
            throws IOException {
      byte token = input.readByte();

      switch (token) {
      case 1:
         return CONCEPT;

      case 2:
         return SEMEME;

      case 3:
         throw new UnsupportedOperationException("Commit record deprecated: " + token);

      case 4:
         return STAMP_COMMENT;

      case 5:
         return STAMP_ALIAS;

      default:
         throw new UnsupportedOperationException("Can't handle: " + token);
      }
   }

   public void toDataStream(DataOutput out)
            throws IOException {
      out.writeByte(token);
   }

   //~--- get methods ---------------------------------------------------------

   public byte getToken() {
      return token;
   }
}

