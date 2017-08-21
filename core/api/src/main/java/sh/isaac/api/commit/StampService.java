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



package sh.isaac.api.commit;

//~--- JDK imports ------------------------------------------------------------

import java.time.Instant;

import java.util.Map;
import java.util.stream.IntStream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatabaseServices;
import sh.isaac.api.State;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@Contract
public interface StampService
        extends DatabaseServices {
   /**
    * STAMP sequences start at 1, in part to ensure that uninitialized values
    * (a zero by default) are not treated as valid stamp sequences.
    */
   int FIRST_STAMP_SEQUENCE = 1;

   //~--- methods -------------------------------------------------------------

   /**
    * Used by the commit manger when committing a pending stamp.
    * Should only be used by developers creating their own commit
    * service.
    *
    * @param stamp the stamp
    * @param stampSequence the stamp sequence
    */
   void addStamp(Stamp stamp, int stampSequence);

   /**
    * Used by the commit manger to cancel pending stamps for a particular
    * author. Should only be used by developers creating their own commit
    * service.
    *
    * @param authorSequence the author sequence
    * @return the task
    */
   Task<Void> cancel(int authorSequence);

   /**
    * Describe stamp sequence.
    *
    * @param stampSequence the stamp sequence
    * @return a textual representation of the stamp sequence.
    */
   String describeStampSequence(int stampSequence);

   /**
    * Describe stamp sequence for tooltip.
    * Will create a String with 5 lines, one for each of S, T, A, M, and P. 
    *
    * @param stampSequence the stamp sequence
    * @param manifoldCoordinate the manifold to get language, dialect, and current coordinate info from. 
    * @return a textual representation of the stamp sequence.
    */
   String describeStampSequenceForTooltip(int stampSequence, ManifoldCoordinate manifoldCoordinate);

   /**
    * Use to compare if versions may be unnecessary duplicates. If their
    * content is equal, see if their stampSequences indicate a semantic
    * difference (change in status, module, or path).
    *
    * @param stampSequence1 the stamp sequence 1
    * @param stampSequence2 the stamp sequence 2
    * @return true if stampSequences are equal without considering the author
    * and time.
    */
   boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the activated stamp sequence.
    *
    * @param stampSequence a stamp sequence to create an analog of
    * @return a stampSequence with a State of {@link State#ACTIVE}, but the
    * same time, author, module, and path as the provided stamp sequence.
    */
   int getActivatedStampSequence(int stampSequence);

   /**
    * Gets the author sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the author sequence for stamp
    */
   int getAuthorSequenceForStamp(int stampSequence);

   /**
    * Gets the instant for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the Instant represented by this stampSequence
    */
   default Instant getInstantForStamp(int stampSequence) {
      return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
   }

   /**
    * Gets the module sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the module sequence for stamp
    */
   int getModuleSequenceForStamp(int stampSequence);

   /**
    * Checks if not canceled.
    *
    * @param stampSequence the stamp sequence
    * @return true, if not canceled
    */
   boolean isNotCanceled(int stampSequence);

   /**
    * Gets the path sequence for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the path sequence for stamp
    */
   int getPathSequenceForStamp(int stampSequence);

   /**
    * Used by the commit manager to get the pending stamps, so that there is a
    * definitive list if items in the commit. Should only be used by developers
    * creating their own commit service.
    *
    * @return the pending stamps for commit
    */
   Map<UncommittedStamp, Integer> getPendingStampsForCommit();

   //~--- set methods ---------------------------------------------------------

   /**
    * Used to revert a commit in progress, i.e. a commit that failed because of
    * a data check error, or some other intervening circumstance. Not for use
    * (will not work) to undo a successful commit. Should only be used by
    * developers creating their own commit service.
    *
    * @param pendingStamps the pending stamps
    */
   void setPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the retired stamp sequence.
    *
    * @param stampSequence a stamp sequence to create an analog of
    * @return a stampSequence with a State of {@link State#INACTIVE}, but the
    * same time, author, module, and path as the provided stamp sequence.
    */
   int getRetiredStampSequence(int stampSequence);

   /**
    * An idempotent operation to return a sequence that uniquely identified by
    * this combination of status, time, author, module, and path (STAMP). If an
    * existing sequence has this combination, that existing sequence will be
    * returned. If no sequence has this combination, a new sequence will be
    * created and returned.
    *
    * @param status the status
    * @param time the time
    * @param authorSequence the author sequence
    * @param moduleSequence the module sequence
    * @param pathSequence the path sequence
    * @return the stampSequence
    */
   int getStampSequence(State status, long time, int authorSequence, int moduleSequence, int pathSequence);

   /**
    * Gets the stamp sequences.
    *
    * @return an IntStream of all stamp sequences known to the commit service.
    */
   IntStream getStampSequences();

   /**
    * Gets the status for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the status for stamp
    */
   State getStatusForStamp(int stampSequence);

   /**
    * Gets the time for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the time for stamp
    */
   long getTimeForStamp(int stampSequence);

   /**
    * Checks if uncommitted.
    *
    * @param stampSequence the stamp sequence
    * @return true, if uncommitted
    */
   boolean isUncommitted(int stampSequence);
}

