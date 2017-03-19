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

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.State;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class StampUniversal {
   @XmlAttribute
   public State status;
   @XmlAttribute
   public long  time;
   @XmlAttribute
   public UUID  authorUuid;
   @XmlAttribute
   public UUID  moduleUuid;
   @XmlAttribute
   public UUID  pathUuid;

   //~--- constructors --------------------------------------------------------

   public StampUniversal(ByteArrayDataBuffer in) {
      this.status     = State.getFromBoolean(in.getBoolean());
      this.time       = in.getLong();
      this.authorUuid = new UUID(in.getLong(), in.getLong());
      this.moduleUuid = new UUID(in.getLong(), in.getLong());
      this.pathUuid   = new UUID(in.getLong(), in.getLong());
   }

   public StampUniversal(int stamp) {
      StampService      stampService = Get.stampService();
      IdentifierService idService    = Get.identifierService();

      this.status     = stampService.getStatusForStamp(stamp);
      this.time       = stampService.getTimeForStamp(stamp);
      this.authorUuid = idService.getUuidPrimordialFromConceptId(stampService.getAuthorSequenceForStamp(stamp))
                                 .get();
      this.moduleUuid = idService.getUuidPrimordialFromConceptId(stampService.getModuleSequenceForStamp(stamp))
                                 .get();
      this.pathUuid   = idService.getUuidPrimordialFromConceptId(stampService.getPathSequenceForStamp(stamp))
                                 .get();
   }

   //~--- methods -------------------------------------------------------------

   public void writeExternal(ByteArrayDataBuffer out) {
      out.putBoolean(this.status.getBoolean());
      out.putLong(time);
      out.putLong(this.authorUuid.getMostSignificantBits());
      out.putLong(this.authorUuid.getLeastSignificantBits());
      out.putLong(this.moduleUuid.getMostSignificantBits());
      out.putLong(this.moduleUuid.getLeastSignificantBits());
      out.putLong(this.pathUuid.getMostSignificantBits());
      out.putLong(this.pathUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getAuthorUuid() {
      return authorUuid;
   }

   public static StampUniversal get(ByteArrayDataBuffer in) {
      return new StampUniversal(in);
   }

   public static StampUniversal get(int stampSequence) {
      return new StampUniversal(stampSequence);
   }

   public UUID getModuleUuid() {
      return moduleUuid;
   }

   public UUID getPathUuid() {
      return pathUuid;
   }

   public int getStampSequence() {
      IdentifierService idService = Get.identifierService();

      return Get.stampService()
                .getStampSequence(status,
                                  time,
                                  idService.getConceptSequenceForUuids(this.authorUuid),
                                  idService.getConceptSequenceForUuids(this.moduleUuid),
                                  idService.getConceptSequenceForUuids(this.pathUuid));
   }

   public State getStatus() {
      return status;
   }

   public long getTime() {
      return time;
   }
}

