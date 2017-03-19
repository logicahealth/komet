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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Comparator;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.provider.workflow.BPMNInfo;

//~--- classes ----------------------------------------------------------------

/**
 * A single advancement (history) of a given workflow process. A new entry is
 * added for every workflow action a user takes.
 *
 * {@link AbstractStorableWorkflowContents}
 *
 * @author <a href="mailto:jefron@westcoastinformatics.com">Jesse Efron</a>
 */
public class ProcessHistory
        extends AbstractStorableWorkflowContents {
   /** The workflow process key for which the Process History is relevant. */
   private UUID processId;

   /** The user who advanced the process. */
   private UUID userId;

   /** The time the workflow was advanced. */
   private long timeAdvanced;

   /** The workflow process state before an action was taken. */
   private String initialState;

   /** The workflow action taken by the user. */
   private String action;

   /** The workflow process state that exists after the action was taken. */
   private String outcomeState;

   /** The comment added by the user when performing the workflow action. */
   private String comment;

   /** The sequence in the process which the history represents. */
   private int historySequence;

   /** Process uuid most significant bits. */
   private long processIdMsb;

   /** Process uuid least significant bits. */
   private long processIdLsb;

   /** User uuid most significant bits. */
   private long userIdMsb;

   /** User uuid least significant bits. */
   private long userIdLsb;

   //~--- constructors --------------------------------------------------------

   /**
    * Constructor for a new process history based on serialized content.
    *
    * @param data
    * The data to deserialize into its components
    */
   public ProcessHistory(byte[] data) {
      readData(new ByteArrayDataBuffer(data));
   }

   /**
    * Constructor for a new process history based on specified entry fields.
    *
    * @param processId the process id
    * @param userId the user id
    * @param timeAdvanced the time advanced
    * @param initialState the initial state
    * @param action the action
    * @param outcomeState the outcome state
    * @param comment the comment
    * @param historySequence the history sequence
    */
   public ProcessHistory(UUID processId,
                         UUID userId,
                         long timeAdvanced,
                         String initialState,
                         String action,
                         String outcomeState,
                         String comment,
                         int historySequence) {
      this.processId       = processId;
      this.userId          = userId;
      this.timeAdvanced    = timeAdvanced;
      this.initialState    = initialState;
      this.action          = action;
      this.outcomeState    = outcomeState;
      this.comment         = comment;
      this.historySequence = historySequence;
      this.processIdMsb    = processId.getMostSignificantBits();
      this.processIdLsb    = processId.getLeastSignificantBits();
      this.userIdMsb       = userId.getMostSignificantBits();
      this.userIdLsb       = userId.getLeastSignificantBits();
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
      final ProcessHistory other = (ProcessHistory) obj;

      return this.processId.equals(other.processId) &&
             this.userId.equals(other.userId) &&
             (this.timeAdvanced == other.timeAdvanced) &&
             this.initialState.equals(other.initialState) &&
             this.action.equals(other.action) &&
             this.outcomeState.equals(other.outcomeState) &&
             this.comment.equals(other.comment) &&
             (this.historySequence == other.historySequence);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      return this.processId.hashCode() + this.userId.hashCode() + new Long(this.timeAdvanced).hashCode() + this.initialState.hashCode() +
             this.action.hashCode() + this.outcomeState.hashCode() + this.comment.hashCode() + this.historySequence;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final LocalDateTime date = LocalDateTime.from(Instant.ofEpochMilli(this.timeAdvanced)
                                                     .atZone(ZoneId.systemDefault()));
      final String        timeAdvancedString = BPMNInfo.workflowDateFormatter.format(date);

      return "\n\t\tId: " + this.id + "\n\t\tProcess Id: " + this.processId + "\n\t\tWorkflowUser Id: " + this.userId +
             "\n\t\tTime Advanced as Long: " + this.timeAdvanced + "\n\t\tTime Advanced: " + timeAdvancedString +
             "\n\t\tInitial State: " + this.initialState + "\n\t\tAction: " + this.action + "\n\t\tOutcome State: " +
             this.outcomeState + "\n\t\tComment: " + this.comment + "\n\t\tHistory Sequence: " + this.historySequence;
   }

   /**
    * Put additional workflow fields.
    *
    * @param out the out
    */
   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putLong(this.processIdMsb);
      out.putLong(this.processIdLsb);
      out.putLong(this.userIdMsb);
      out.putLong(this.userIdLsb);
      out.putLong(this.timeAdvanced);
      out.putByteArrayField(this.initialState.getBytes());
      out.putByteArrayField(this.action.getBytes());
      out.putByteArrayField(this.outcomeState.getBytes());
      out.putByteArrayField(this.comment.getBytes());
      out.putInt(this.historySequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the action performed upon the workflow process.
    *
    * @return the action taken
    */
   public String getAction() {
      return this.action;
   }

   /**
    * Gets the additional workflow fields.
    *
    * @param in the in
    * @return the additional workflow fields
    */
   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      this.processIdMsb    = in.getLong();
      this.processIdLsb    = in.getLong();
      this.userIdMsb       = in.getLong();
      this.userIdLsb       = in.getLong();
      this.timeAdvanced    = in.getLong();
      this.initialState    = new String(in.getByteArrayField());
      this.action          = new String(in.getByteArrayField());
      this.outcomeState    = new String(in.getByteArrayField());
      this.comment         = new String(in.getByteArrayField());
      this.historySequence = in.getInt();
      this.processId       = new UUID(this.processIdMsb, this.processIdLsb);
      this.userId          = new UUID(this.userIdMsb, this.userIdLsb);
   }

   /**
    * Gets the comment provided by the user when advancing the process.
    *
    * @return the comment
    */
   public String getComment() {
      return this.comment;
   }

   /**
    * Gets the sequence within the process which the history represents.
    *
    * @return the history sequence
    */
   public int getHistorySequence() {
      return this.historySequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the sequence within the process which the history represents.
    *
    * @param seq the new sequence in the process which the history represents
    * @return the history sequence
    */
   public void setHistorySequence(int seq) {
      this.historySequence = seq;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the state of the process prior to it being advanced.
    *
    * @return the initial state of the process
    */
   public String getInitialState() {
      return this.initialState;
   }

   /**
    * Gets the state of the process following it being advanced.
    *
    * @return the outcome state
    */
   public String getOutcomeState() {
      return this.outcomeState;
   }

   /**
    * Gets the key of the process entry.
    *
    * @return process key
    */
   public UUID getProcessId() {
      return this.processId;
   }

   /**
    * Gets the time which the workflow process was advanced.
    *
    * @return the time the process was advanced
    */
   public long getTimeAdvanced() {
      return this.timeAdvanced;
   }

   /**
    * Gets the user's id that advanced the workflow process.
    *
    * @return the user nid
    */
   public UUID getUserId() {
      return this.userId;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * A custom comparator to assist in ordering process history information.
    * Based on advancement time.
    *
    */
   public static class ProcessHistoryComparator
            implements Comparator<ProcessHistory> {
      
      /**
       * Instantiates a new process history comparator.
       */
      public ProcessHistoryComparator() {}

      //~--- methods ----------------------------------------------------------

      /**
       * Compare.
       *
       * @param o1 the o 1
       * @param o2 the o 2
       * @return the int
       */
      @Override
      public int compare(ProcessHistory o1, ProcessHistory o2) {
         if (o1.getProcessId()
               .equals(o2.getProcessId())) {
            final long seq1 = o1.getHistorySequence();
            final long seq2 = o2.getHistorySequence();

            if (seq1 > seq2) {
               return 1;
            } else if (seq1 < seq2) {
               return -1;
            }
         }

         return 0;
      }
   }
}

