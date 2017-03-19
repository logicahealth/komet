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
import sh.isaac.api.coordinate.EditCoordinate;

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
    * @param stamp
    * @param stampSequence
    */
   void addStamp(Stamp stamp, int stampSequence);

   /**
    * Used by the commit manger to cancel pending stamps for a particular
    * author. Should only be used by developers creating their own commit
    * service.
    *
    * @param authorSequence
    * @return
    */
   Task<Void> cancel(int authorSequence);

   /**
    *
    * @param stampSequence
    * @return a textual representation of the stamp sequence.
    */
   String describeStampSequence(int stampSequence);

   /**
    * Use to compare if versions may be unnecessary duplicates. If their
    * content is equal, see if their stampSequences indicate a semantic
    * difference (change in status, module, or path).
    *
    * @param stampSequence1
    * @param stampSequence2
    * @return true if stampSequences are equal without considering the author
    * and time.
    */
   boolean stampSequencesEqualExceptAuthorAndTime(int stampSequence1, int stampSequence2);

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @param stampSequence a stamp sequence to create an analog of
    * @return a stampSequence with a State of {@link State#ACTIVE}, but the
    * same time, author, module, and path as the provided stamp sequence.
    */
   int getActivatedStampSequence(int stampSequence);

   int getAuthorSequenceForStamp(int stampSequence);

   /**
    *
    * @param stampSequence
    * @return the Instant represented by this stampSequence
    */
   default Instant getInstantForStamp(int stampSequence) {
      return Instant.ofEpochMilli(getTimeForStamp(stampSequence));
   }

   int getModuleSequenceForStamp(int stampSequence);

   boolean isNotCanceled(int stampSequence);

   int getPathSequenceForStamp(int stampSequence);

   /**
    * Used by the commit manager to get the pending stamps, so that there is a
    * definitive list if items in the commit. Should only be used by developers
    * creating their own commit service.
    *
    * @return
    */
   Map<UncommittedStamp, Integer> getPendingStampsForCommit();

   //~--- set methods ---------------------------------------------------------

   /**
    * Used to revert a commit in progress, i.e. a commit that failed because of
    * a data check error, or some other intervening circumstance. Not for use
    * (will not work) to undo a successful commit. Should only be used by
    * developers creating their own commit service.
    *
    * @param pendingStamps
    */
   void setPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps);

   //~--- get methods ---------------------------------------------------------

   /**
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
    * @param status
    * @param time
    * @param authorSequence
    * @param moduleSequence
    * @param pathSequence
    * @return the stampSequence
    */
   int getStampSequence(State status, long time, int authorSequence, int moduleSequence, int pathSequence);

   /**
    * @return an IntStream of all stamp sequences known to the commit service.
    */
   IntStream getStampSequences();

   State getStatusForStamp(int stampSequence);

   long getTimeForStamp(int stampSequence);

   boolean isUncommitted(int stampSequence);
}

