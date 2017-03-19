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



package sh.isaac.api.externalizable;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.OchreExternalizable;
import sh.isaac.api.externalizable.OchreExternalizableObjectType;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampComment
         implements OchreExternalizable {
   private String comment;
   private int    stampSequence;

   //~--- constructors --------------------------------------------------------

   public StampComment(ByteArrayDataBuffer in) {
      final byte version = in.getByte();

      if (version == getDataFormatVersion()) {
         this.stampSequence = StampUniversal.get(in)
                                       .getStampSequence();
         this.comment       = in.readUTF();
      } else {
         throw new UnsupportedOperationException("Can't handle version: " + version);
      }
   }

   public StampComment(String comment, int stampSequence) {
      this.comment       = comment;
      this.stampSequence = stampSequence;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      out.putByte(getDataFormatVersion());
      StampUniversal.get(this.stampSequence)
                    .writeExternal(out);
      out.putUTF(this.comment);
   }

   @Override
   public String toString() {
      return "StampComment{" + "comment='" + this.comment + '\'' + ", stampSequence=" + this.stampSequence + '}';
   }

   //~--- get methods ---------------------------------------------------------

   public String getComment() {
      return this.comment;
   }

   @Override
   public byte getDataFormatVersion() {
      return 0;
   }

   @Override
   public OchreExternalizableObjectType getOchreObjectType() {
      return OchreExternalizableObjectType.STAMP_COMMENT;
   }

   public int getStampSequence() {
      return this.stampSequence;
   }
}

