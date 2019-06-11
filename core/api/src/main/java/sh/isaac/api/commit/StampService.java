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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.DatastoreServices;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.collections.IntSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.snapshot.calculator.RelativePosition;

//~--- interfaces -------------------------------------------------------------

/**
 * Created by kec on 1/2/16.
 */
@Contract
public interface StampService
        extends DatastoreServices {
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
    * @param authorNid the author nid
    * @return the task
    */
   Task<Void> cancel(int authorNid);

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
    * @return a stampSequence with a Status of {@link Status#ACTIVE}, but the
    * same time, author, module, and path as the provided stamp sequence.
    */
   int getActivatedStampSequence(int stampSequence);

   /**
    * Gets the author nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the author nid for stamp
    */
   int getAuthorNidForStamp(int stampSequence);

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
    * Gets the module nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the module nid for stamp
    */
   int getModuleNidForStamp(int stampSequence);

   /**
    * Checks if not canceled.
    *
    * @param stampSequence the stamp sequence
    * @return true, if not canceled
    */
   boolean isNotCanceled(int stampSequence);

   /**
    * Gets the path nid for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the path nid for stamp
    */
   int getPathNidForStamp(int stampSequence);

   /**
    * Used by the commit manager to get the pending stamps, so that there is a
    * definitive list if items in the commit. Should only be used by developers
    * creating their own commit service.
    *
    * @return the pending stamps for commit
    */
   ConcurrentHashMap<UncommittedStamp, Integer> getPendingStampsForCommit();

   //~--- set methods ---------------------------------------------------------

   /**
    * Used to revert a commit in progress, i.e. a commit that failed because of
    * a data check error, or some other intervening circumstance. Not for use
    * (will not work) to undo a successful commit. Should only be used by
    * developers creating their own commit service.
    *
    * @param pendingStamps the pending stamps
    */
   void addPendingStampsForCommit(Map<UncommittedStamp, Integer> pendingStamps);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the retired stamp sequence.
    *
    * @param stampSequence a stamp sequence to create an analog of
    * @return a stampSequence with a Status of {@link Status#INACTIVE}, but the
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
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    * @return the stampSequence
    */
   int getStampSequence(Status status, long time, int authorNid, int moduleNid, int pathNid);

   /**
    * Gets the stamp sequences.
    *
    * @return an IntStream of all stamp sequences known to the commit service.
    */
   IntStream getStampSequences();
   /**
    * Return the set of stamps that are between the two stamp coordinates, where
    * the returned values are exclusive of the start coordinate, and inclusive of the
    * end coordinate. IF authors are specified on the endCoordinate, only stamps from those
    * authors are included in the results.
    *
    * @param startCoordinate
    * @param endCoordinate
    * @return all stamps between the provided coordinates.
    */
   default StampSequenceSet getStampsBetweenCoordinates(StampCoordinate startCoordinate, StampCoordinate endCoordinate) {
      StampSequenceSet matchingStamps = new StampSequenceSet();

      VersionManagmentPathService positionCalc = Get.versionManagmentPathService();
      NidSet authorNids = endCoordinate.getAuthorNids();
      StampService stampService = Get.stampService();
      getStampSequences().forEach(stamp -> {
         if (positionCalc.getRelativePosition(stamp, startCoordinate) == RelativePosition.AFTER) {
            RelativePosition relativeToEnd = positionCalc.getRelativePosition(stamp, endCoordinate);
            if (relativeToEnd == RelativePosition.EQUAL || relativeToEnd == RelativePosition.BEFORE) {
               if (authorNids.isEmpty()) {
                  matchingStamps.add(stamp);
               } else if (authorNids.contains(stampService.getAuthorNidForStamp(stamp))) {
                   matchingStamps.add(stamp);
               }
            }
         }
      });
      return matchingStamps;
   }


   /**
    * Gets the status for stamp.
    *
    * @param stampSequence the stamp sequence
    * @return the status for stamp
    */
   Status getStatusForStamp(int stampSequence);

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
   
   /**
    * Get the stamp object from an int stamp
    * If the provided stamp is invalid / less than 0, this returns a default stamp, with most fields set to unspecified.
    * @param stamp
    * @return
    */
   Stamp getStamp(int stamp);
}

