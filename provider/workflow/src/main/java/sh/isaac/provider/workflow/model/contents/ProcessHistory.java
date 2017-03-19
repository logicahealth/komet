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

   /**
    * Process uuid most significant bits
    */
   private long processIdMsb;

   /**
    * Process uuid least significant bits
    */
   private long processIdLsb;

   /**
    * User uuid most significant bits
    */
   private long userIdMsb;

   /**
    * User uuid least significant bits
    */
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
    * @param processId
    * @param userId
    * @param timeAdvanced
    * @param initialState
    * @param action
    * @param outcomeState
    * @param comment
    * @param historySequence
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

   @Override
   public boolean equals(Object obj) {
      ProcessHistory other = (ProcessHistory) obj;

      return this.processId.equals(other.processId) &&
             this.userId.equals(other.userId) &&
             (this.timeAdvanced == other.timeAdvanced) &&
             this.initialState.equals(other.initialState) &&
             this.action.equals(other.action) &&
             this.outcomeState.equals(other.outcomeState) &&
             this.comment.equals(other.comment) &&
             (this.historySequence == other.historySequence);
   }

   @Override
   public int hashCode() {
      return processId.hashCode() + userId.hashCode() + new Long(timeAdvanced).hashCode() + initialState.hashCode() +
             action.hashCode() + outcomeState.hashCode() + comment.hashCode() + historySequence;
   }

   @Override
   public String toString() {
      LocalDateTime date = LocalDateTime.from(Instant.ofEpochMilli(timeAdvanced)
                                                     .atZone(ZoneId.systemDefault()));
      String        timeAdvancedString = BPMNInfo.workflowDateFormatter.format(date);

      return "\n\t\tId: " + id + "\n\t\tProcess Id: " + processId + "\n\t\tWorkflowUser Id: " + userId +
             "\n\t\tTime Advanced as Long: " + timeAdvanced + "\n\t\tTime Advanced: " + timeAdvancedString +
             "\n\t\tInitial State: " + initialState + "\n\t\tAction: " + action + "\n\t\tOutcome State: " +
             outcomeState + "\n\t\tComment: " + comment + "\n\t\tHistory Sequence: " + historySequence;
   }

   @Override
   protected void putAdditionalWorkflowFields(ByteArrayDataBuffer out) {
      out.putLong(processIdMsb);
      out.putLong(processIdLsb);
      out.putLong(userIdMsb);
      out.putLong(userIdLsb);
      out.putLong(timeAdvanced);
      out.putByteArrayField(initialState.getBytes());
      out.putByteArrayField(action.getBytes());
      out.putByteArrayField(outcomeState.getBytes());
      out.putByteArrayField(comment.getBytes());
      out.putInt(historySequence);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the action performed upon the workflow process.
    *
    * @return the action taken
    */
   public String getAction() {
      return action;
   }

   @Override
   protected void getAdditionalWorkflowFields(ByteArrayDataBuffer in) {
      processIdMsb    = in.getLong();
      processIdLsb    = in.getLong();
      userIdMsb       = in.getLong();
      userIdLsb       = in.getLong();
      timeAdvanced    = in.getLong();
      initialState    = new String(in.getByteArrayField());
      action          = new String(in.getByteArrayField());
      outcomeState    = new String(in.getByteArrayField());
      comment         = new String(in.getByteArrayField());
      historySequence = in.getInt();
      processId       = new UUID(processIdMsb, processIdLsb);
      userId          = new UUID(userIdMsb, userIdLsb);
   }

   /**
    * Gets the comment provided by the user when advancing the process.
    *
    * @return the comment
    */
   public String getComment() {
      return comment;
   }

   /**
    * Gets the sequence within the process which the history represents.
    *
    * @return the history sequence
    */
   public int getHistorySequence() {
      return historySequence;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the sequence within the process which the history represents.
    *
    * @return the history sequence
    */
   public void setHistorySequence(int seq) {
      historySequence = seq;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the state of the process prior to it being advanced.
    *
    * @return the initial state of the process
    */
   public String getInitialState() {
      return initialState;
   }

   /**
    * Gets the state of the process following it being advanced.
    *
    * @return the outcome state
    */
   public String getOutcomeState() {
      return outcomeState;
   }

   /**
    * Gets the key of the process entry.
    *
    * @return process key
    */
   public UUID getProcessId() {
      return processId;
   }

   /**
    * Gets the time which the workflow process was advanced.
    *
    * @return the time the process was advanced
    */
   public long getTimeAdvanced() {
      return timeAdvanced;
   }

   /**
    * Gets the user's id that advanced the workflow process.
    *
    * @return the user nid
    */
   public UUID getUserId() {
      return userId;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * A custom comparator to assist in ordering process history information.
    * Based on advancement time.
    *
    */
   public static class ProcessHistoryComparator
            implements Comparator<ProcessHistory> {
      public ProcessHistoryComparator() {}

      //~--- methods ----------------------------------------------------------

      @Override
      public int compare(ProcessHistory o1, ProcessHistory o2) {
         if (o1.getProcessId()
               .equals(o2.getProcessId())) {
            long seq1 = o1.getHistorySequence();
            long seq2 = o2.getHistorySequence();

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

