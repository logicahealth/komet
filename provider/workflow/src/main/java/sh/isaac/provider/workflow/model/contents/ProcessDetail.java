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



package sh.isaac.provider.workflow.model.contents;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.commit.Stamp;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.provider.stamp.StampSerializer;
import sh.isaac.provider.workflow.BPMNInfo;

//~--- classes ----------------------------------------------------------------

/**
 * The metadata defining a given process (or workflow instance). This doesn't
 * include its Detail which is available via {@link ProcessHistory}
 *
 * {@link AbstractStorableWorkflowContents}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class ProcessDetail
        extends AbstractStorableWorkflowContents {
   /**
    * A map of all component nids modified within the workflow process to the time of initial modification.
    * Therefore, if a component has been modified multiple times within a
    * single process, only the first of those times are required on the map.
    */
   private final Map<Integer, Stamp> componentToIntitialEditMap = new HashMap<>();

   /** The time the workflow process was launched. */
   private long timeLaunched = -1L;

   /**
    * The time the workflow process was finished (either via canceled Or
    * Concluded).
    */
   private long timeCanceledOrConcluded = -1L;

   /** The workflow process's current "owner". */
   private UUID ownerId = BPMNInfo.UNOWNED_PROCESS;

   /** The stamp serializer. */
   private final StampSerializer stampSerializer = new StampSerializer();

   /** The workflow definition key for which the Process Detail is relevant. */
   private UUID definitionId;

   /** The user who originally defined (created) the workflow process. */
   private UUID creatorId;

   /** The time the workflow process was created. */
   private long timeCreated;

   /** The workflow process's status. */
   private ProcessStatus status;

   /** The workflow process's name. */
   private String name;

   /** The workflow process's description. */
   private String description;

   /** Definition uuid most significant bits. */
   private long definitionIdMsb;

   /** Definition uuid least significant bits. */
   private long definitionIdLsb;

   /** Creator uuid most significant bits. */
   private long creatorIdMsb;

   /** Creator uuid least significant bits. */
   private long creatorIdLsb;

   /** Owner uuid most significant bits. */
   private long ownerIdMsb;

   /** Owner uuid least significant bits. */
   private long ownerIdLsb;

   //~--- constant enums ------------------------------------------------------

   /**
    * The exhaustive list of possible ways an instantiated process may be ended.
    */
   public enum EndWorkflowType {
      /** Process is stopped without reaching a completed state. */
      CANCELED,

      /** Process has been finished by reaching a completed state. */
      CONCLUDED
   }

   /**
    * The exhaustive list of possible process statuses.
    */
   public enum ProcessStatus {
      /** Process is being defined and has yet to be launched. */
      DEFINED,

      /** Process has been launched. */
      LAUNCHED,

      /** A previously launched or defined process that has been canceled. */
      CANCELED,

      /** A previously launched process that is completed. */
      CONCLUDED
   }

   ;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructor for a new process based on serialized content.
    *
    * @param data
    * The data to deserialize into its components
    */
   public ProcessDetail(byte[] data) {
      readData(new ByteArrayDataBuffer(data));
   }

   /**
    * Constructor for a new process based on specified entry fields.
    *
    * @param definitionId the definition id
    * @param creatorId the creator id
    * @param timeCreated the time created
    * @param status the status
    * @param name the name
    * @param description the description
    */
   public ProcessDetail(UUID definitionId,
                        UUID creatorId,
                        long timeCreated,
                        ProcessStatus status,
                        String name,
                        String description) {
      this.definitionId    = definitionId;
      this.creatorId       = creatorId;
      this.timeCreated     = timeCreated;
      this.status          = status;
      this.name            = name;
      this.description     = description;
      this.ownerId         = creatorId;
      this.definitionIdMsb = definitionId.getMostSignificantBits();
      this.definitionIdLsb = definitionId.getLeastSignificantBits();
      this.creatorIdMsb    = creatorId.getMostSignificantBits();
      this.creatorIdLsb    = creatorId.getLeastSignificantBits();
      this.ownerIdMsb      = this.ownerId.getMostSignificantBits();
      this.ownerIdLsb      = this.ownerId.getLeastSignificantBits();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      final ProcessDetail other = (ProcessDetail) obj;

      return this.definitionId.equals(other.definitionId) &&
             this.componentToIntitialEditMap.equals(other.componentToIntitialEditMap) &&
             this.creatorId.equals(other.creatorId) &&
             (this.timeCreated == other.timeCreated) &&
             (this.timeLaunched == other.timeLaunched) &&
             (this.timeCanceledOrConcluded == other.timeCanceledOrConcluded) &&
             (this.status == other.status) &&
             this.name.equals(other.name) &&
             this.description.equals(other.description) &&
             this.ownerId.equals(other.ownerId);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.definitionId.hashCode() + this.componentToIntitialEditMap.hashCode() + this.creatorId.hashCode() +
             new Long(this.timeCreated).hashCode() + new Long(this.timeLaunched).hashCode() +
             new Long(this.timeCanceledOrConcluded).hashCode() + this.status.hashCode() + this.name.hashCode() +
             this.description.hashCode() + this.ownerId.hashCode();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuffer buf = new StringBuffer();

      for (final Integer compNid: this.componentToIntitialEditMap.keySet()) {
         buf.append("\n\t\t\tFor Component Nid: " + compNid + " first edited in workflow at Stamp: " +
                    this.componentToIntitialEditMap.get(compNid));
      }

      LocalDateTime date = LocalDateTime.from(Instant.ofEpochMilli(this.timeCreated)
                                                     .atZone(ZoneId.systemDefault()));
      final String  timeCreatedString = BPMNInfo.workflowDateFormatter.format(date);

      date = LocalDateTime.from(Instant.ofEpochMilli(this.timeLaunched)
                                       .atZone(ZoneId.systemDefault()));

      final String timeLaunchedString = BPMNInfo.workflowDateFormatter.format(date);

      date = LocalDateTime.from(Instant.ofEpochMilli(this.timeCanceledOrConcluded)
                                       .atZone(ZoneId.systemDefault()));

      final String timeCanceledOrConcludedString = BPMNInfo.workflowDateFormatter.format(date);

      return "\n\t\tId: " + this.id + "\n\t\tDefinition Id: " + this.definitionId.toString() +
             "\n\t\tComponents to Sequences Map: " + buf.toString() + "\n\t\tCreator Id: " +
             this.creatorId.toString() + "\n\t\tTime Created: " + timeCreatedString + "\n\t\tTime Launched: " +
             timeLaunchedString + "\n\t\tTime Canceled or Concluded: " + timeCanceledOrConcludedString +
             "\n\t\tStatus: " + this.status + "\n\t\tName: " + this.name + "\n\t\tDescription: " + this.description +
             "\n\t\tOwner Id: " + this.ownerId.toString();
   }

   /**
    * Put additional workflow fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putLong(this.definitionIdMsb);
      out.putLong(this.definitionIdLsb);
      out.putInt(this.componentToIntitialEditMap.size());

      for (final Integer componentNid: this.componentToIntitialEditMap.keySet()) {
         out.putNid(componentNid);

         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            this.stampSerializer.serialize(new DataOutputStream(baos),
                                           this.componentToIntitialEditMap.get(componentNid));
            out.putByteArrayField(baos.toByteArray());
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }

      out.putLong(this.creatorIdMsb);
      out.putLong(this.creatorIdLsb);
      out.putLong(this.timeCreated);
      out.putLong(this.timeLaunched);
      out.putLong(this.timeCanceledOrConcluded);
      out.putByteArrayField(this.status.name()
                                       .getBytes());
      out.putByteArrayField(this.name.getBytes());
      out.putByteArrayField(this.description.getBytes());
      out.putLong(this.ownerIdMsb);
      out.putLong(this.ownerIdLsb);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if active.
    *
    * @return true, if active
    */
   public boolean isActive() {
      return (this.status == ProcessStatus.LAUNCHED) || (this.status == ProcessStatus.DEFINED);
   }

   /**
    * Gets the additional workflow fields.
    *
    * @param in the in
    * @return the additional workflow fields
    */
   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      this.definitionIdMsb = in.getLong();
      this.definitionIdLsb = in.getLong();
      this.definitionId    = new UUID(this.definitionIdMsb, this.definitionIdLsb);

      final int collectionCount = in.getInt();

      for (int i = 0; i < collectionCount; i++) {
         final int compNid = in.getNid();

         try (ByteArrayInputStream baos = new ByteArrayInputStream(in.getByteArrayField())) {
            this.componentToIntitialEditMap.put(compNid, this.stampSerializer.deserialize(new DataInputStream(baos)));
         } catch (final IOException e) {
            throw new RuntimeException(e);
         }
      }

      this.creatorIdMsb            = in.getLong();
      this.creatorIdLsb            = in.getLong();
      this.creatorId               = new UUID(this.creatorIdMsb, this.creatorIdLsb);
      this.timeCreated             = in.getLong();
      this.timeLaunched            = in.getLong();
      this.timeCanceledOrConcluded = in.getLong();
      this.status                  = ProcessStatus.valueOf(new String(in.getByteArrayField()));
      this.name                    = new String(in.getByteArrayField());
      this.description             = new String(in.getByteArrayField());
      this.ownerIdMsb              = in.getLong();
      this.ownerIdLsb              = in.getLong();
      this.ownerId                 = new UUID(this.ownerIdMsb, this.ownerIdLsb);
   }

   /**
    * Checks if canceled.
    *
    * @return true, if canceled
    */
   public boolean isCanceled() {
      return this.status == ProcessStatus.CANCELED;
   }

   /**
    * Gets the process's component nids.
    *
    * @return map of component nids to ordered stamp sequences
    */
   public Map<Integer, Stamp> getComponentToInitialEditMap() {
      return this.componentToIntitialEditMap;
   }

   /**
    * Checks if concluded.
    *
    * @return true, if concluded
    */
   public boolean isConcluded() {
      return this.status == ProcessStatus.CONCLUDED;
   }

   /**
    * Gets the process creator.
    *
    * @return the process creator's id
    */
   public UUID getCreatorId() {
      return this.creatorId;
   }

   /**
    * Gets the definition Id associated with the process.
    *
    * @return the key of the definition from which the process is created
    */
   public UUID getDefinitionId() {
      return this.definitionId;
   }

   /**
    * The description of the process.
    *
    * @return process description
    */
   public String getDescription() {
      return this.description;
   }

   /**
    * The name of the process.
    *
    * @return process name
    */
   public String getName() {
      return this.name;
   }

   /**
    * Retrieves the current owner of the process.
    *
    * @return 0-based UUID: Process is not owned.
    * Otherwise, return the process's current owner id
    */
   public UUID getOwnerId() {
      return this.ownerId;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the current owner of the process.
    *
    * @param nid The userId obtaining a lock on the instance. Note '0' means no owner.
    */
   public void setOwnerId(UUID nid) {
      this.ownerId    = nid;
      this.ownerIdMsb = this.ownerId.getMostSignificantBits();
      this.ownerIdLsb = this.ownerId.getLeastSignificantBits();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the process's current status.
    *
    * @return the process Status
    */
   public ProcessStatus getStatus() {
      return this.status;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the process's current status as this is updated over the course of
    * the process.
    *
    * @param status
    * The process's current status
    */
   public void setStatus(ProcessStatus status) {
      this.status = status;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the time the process ended either via cancelation or conclusion as
    * long primitive type.
    *
    * @return the time the process was canceled or concluded
    */
   public long getTimeCanceledOrConcluded() {
      return this.timeCanceledOrConcluded;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the time the process ended either via cancelation or conclusion as
    * this isn't available during the object's construction.
    *
    * @param time
    * The time the process was canceled/concluded as long primitive
    * type
    */
   public void setTimeCanceledOrConcluded(long time) {
      this.timeCanceledOrConcluded = time;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the time the process was created as long primitive type.
    *
    * @return the time the process was created
    */
   public long getTimeCreated() {
      return this.timeCreated;
   }

   /**
    * Get the time the process was launched as long primitive type.
    *
    * @return the time launched
    */
   public long getTimeLaunched() {
      return this.timeLaunched;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the time the process as launched as this isn't available during the
    * object's construction.
    *
    * @param time
    * The time the process was launched as long primitive type
    */
   public void setTimeLaunched(long time) {
      this.timeLaunched = time;
   }
}

