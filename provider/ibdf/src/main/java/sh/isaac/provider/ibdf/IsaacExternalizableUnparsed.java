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



package sh.isaac.provider.ibdf;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampUniversal;

//~--- classes ----------------------------------------------------------------

/**
 * {@link IsaacExternalizableUnparsed}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsaacExternalizableUnparsed {
   /** The data for the unparsed object. */
   private final ByteArrayDataBuffer data;

   /** The type of the object. */
   IsaacObjectType type;

   //~--- constructors --------------------------------------------------------
   public IsaacExternalizableUnparsed(IsaacExternalizable externalizable) {
      this.type = externalizable.getIsaacObjectType();
      this.data = new ByteArrayDataBuffer();
      this.type.writeTypeVersionHeader(data);
      externalizable.putExternal(this.data);
      this.data.trimToSize();
      this.data.setPosition(2);
   }

   public IsaacExternalizableUnparsed(ByteArrayDataBuffer data) {
      if (data.getObjectDataFormatVersion() > 0) {
         throw new IllegalStateException("Improperly initialized. object data format versions should not be set yet: " + data.getObjectDataFormatVersion());
      }
      this.data = data;
      this.type = IsaacObjectType.fromByteArrayDataBuffer(data);
      this.data.setObjectDataFormatVersion(this.data.getByte());
      
   }
   //~--- methods -------------------------------------------------------------

   /**
    * Parses the IsaacExternalizable.
    * 
    * @return the Isaac externalizable object
    */
   public IsaacExternalizable parse() {
      switch (this.type) {
      case CONCEPT:
         ConceptChronologyImpl temp = ConceptChronologyImpl.make(this.data);
         return temp;

      case SEMANTIC:
         return SemanticChronologyImpl.make(this.data);

      case STAMP_ALIAS:
         return new StampAlias(this.data);

      case STAMP_COMMENT:
         return new StampComment(this.data);
         
      case STAMP:
         return new StampUniversal(data);

      default:
         throw new UnsupportedOperationException("aq Can't handle: " + this.type);
      }
   }
   
   public byte[] getBytes() {
      return this.data.getData();
   }
}

