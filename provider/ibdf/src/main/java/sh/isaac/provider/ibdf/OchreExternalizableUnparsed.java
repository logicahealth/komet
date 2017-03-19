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
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;
import sh.isaac.api.externalizable.StampAlias;
import sh.isaac.api.externalizable.StampComment;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;

//~--- classes ----------------------------------------------------------------

/**
 * {@link OchreExternalizableUnparsed}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class OchreExternalizableUnparsed {
   private final ByteArrayDataBuffer   data_;
   OchreExternalizableObjectType type_;

   //~--- constructors --------------------------------------------------------

   public OchreExternalizableUnparsed(OchreExternalizableObjectType type, ByteArrayDataBuffer data) {
      this.data_ = data;
      this.type_ = type;
   }

   //~--- methods -------------------------------------------------------------

   public OchreExternalizable parse() {
      switch (this.type_) {
      case CONCEPT:
         return ConceptChronologyImpl.make(this.data_);

      case SEMEME:
         return SememeChronologyImpl.make(this.data_);

      case STAMP_ALIAS:
         return new StampAlias(this.data_);

      case STAMP_COMMENT:
         return new StampComment(this.data_);

      default:
         throw new UnsupportedOperationException("Can't handle: " + this.type_);
      }
   }
}

