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



package sh.isaac.provider.stamp;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.DataSerializer;
import sh.isaac.api.Status;
import sh.isaac.api.commit.Stamp;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampSerializer.
 *
 * @author kec
 */
public class StampSerializer
         implements DataSerializer<Stamp>, Serializable {
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   //~--- methods -------------------------------------------------------------

   /**
    * Deserialize.
    *
    * @param in the in
    * @return the stamp
    */
   @Override
   public Stamp deserialize(DataInput in) {
      try {
         return new Stamp(Status.valueOf(in.readUTF()),
                          in.readLong(),
                          in.readInt(),
                          in.readInt(),
                          in.readInt());
      } catch (final IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   /**
    * Serialize.
    *
    * @param out the out
    * @param stamp the stamp
    */
   @Override
   public void serialize(DataOutput out, Stamp stamp) {
      try {
         out.writeUTF(stamp.getStatus().name());
         out.writeLong(stamp.getTime());
         out.writeInt(stamp.getAuthorNid());
         out.writeInt(stamp.getModuleNid());
         out.writeInt(stamp.getPathNid());
      } catch (final IOException ex) {
         throw new RuntimeException(ex);
      }
   }
}

