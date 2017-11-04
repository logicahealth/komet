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
import sh.isaac.api.commit.StampService;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

/**
 * The Class StampUniversal.
 *
 * @author kec
 */
public class StampUniversal implements IsaacExternalizable {
   /** The status. */
   @XmlAttribute
   public State status;

   /** The time. */
   @XmlAttribute
   public long time;

   /** The author uuid. */
   @XmlAttribute
   public UUID authorUuid;

   /** The module uuid. */
   @XmlAttribute
   public UUID moduleUuid;

   /** The path uuid. */
   @XmlAttribute
   public UUID pathUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp universal.
    *
    * @param in the in
    */
   public StampUniversal(ByteArrayDataBuffer in) {
      if (IsaacObjectType.STAMP.getDataFormatVersion() != in.getObjectDataFormatVersion()) {
         throw new UnsupportedOperationException("Data format version not supported: " + in.getObjectDataFormatVersion());
      }
      this.status     = State.getFromBoolean(in.getBoolean());
      this.time       = in.getLong();
      this.authorUuid = new UUID(in.getLong(), in.getLong());
      this.moduleUuid = new UUID(in.getLong(), in.getLong());
      this.pathUuid   = new UUID(in.getLong(), in.getLong());
   }
   
   /**
    * Instantiates a new stamp universal.
    *
    * @param version the version to create a stamp for
    */
   public StampUniversal(StampedVersion version) {
      this(version.getStampSequence());
   }

   /**
    * Instantiates a new stamp universal.
    *
    * @param stamp the stamp
    */
   public StampUniversal(int stamp) {
      final StampService      stampService = Get.stampService();
      final IdentifierService idService    = Get.identifierService();

      this.status     = stampService.getStatusForStamp(stamp);
      this.time       = stampService.getTimeForStamp(stamp);
      this.authorUuid = idService.getUuidPrimordialForNid(stampService.getAuthorNidForStamp(stamp))
                                 .get();
      this.moduleUuid = idService.getUuidPrimordialForNid(stampService.getModuleNidForStamp(stamp))
                                 .get();
      this.pathUuid   = idService.getUuidPrimordialForNid(stampService.getPathNidForStamp(stamp))
                                 .get();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Write external.
    *
    * @param out the out
    */
   public void writeExternal(ByteArrayDataBuffer out) {
      out.putBoolean(this.status.getBoolean());
      out.putLong(this.time);
      out.putLong(this.authorUuid.getMostSignificantBits());
      out.putLong(this.authorUuid.getLeastSignificantBits());
      out.putLong(this.moduleUuid.getMostSignificantBits());
      out.putLong(this.moduleUuid.getLeastSignificantBits());
      out.putLong(this.pathUuid.getMostSignificantBits());
      out.putLong(this.pathUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the author uuid.
    *
    * @return the author uuid
    */
   public UUID getAuthorUuid() {
      return this.authorUuid;
   }

   /**
    * Gets the.
    *
    * @param in the in
    * @return the stamp universal
    */
   public static StampUniversal get(ByteArrayDataBuffer in) {
      return new StampUniversal(in);
   }

   /**
    * Gets the.
    *
    * @param stampSequence the stamp sequence
    * @return the stamp universal
    */
   public static StampUniversal get(int stampSequence) {
      return new StampUniversal(stampSequence);
   }

   /**
    * Gets the module uuid.
    *
    * @return the module uuid
    */
   public UUID getModuleUuid() {
      return this.moduleUuid;
   }

   /**
    * Gets the path uuid.
    *
    * @return the path uuid
    */
   public UUID getPathUuid() {
      return this.pathUuid;
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   public int getStampSequence() {
      final IdentifierService idService = Get.identifierService();

      return Get.stampService()
                .getStampSequence(this.status,
                                  this.time,
                                  idService.getNidForUuids(this.authorUuid),
                                  idService.getNidForUuids(this.moduleUuid),
                                  idService.getNidForUuids(this.pathUuid));
   }

   /**
    * Gets the status.
    *
    * @return the status
    */
   public State getStatus() {
      return this.status;
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   public long getTime() {
      return this.time;
   }

   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      IsaacObjectType.STAMP.writeTypeVersionHeader(out);
      this.writeExternal(out);
   }

   @Override
   public IsaacObjectType getIsaacObjectType() {
      return IsaacObjectType.STAMP;
   }
}

