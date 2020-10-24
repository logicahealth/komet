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

import java.time.Instant;
import java.util.UUID;
import java.util.stream.IntStream;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.DatastoreServices;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.collections.StampSequenceSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.snapshot.calculator.RelativePosition;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.transaction.Transaction;

/**
 * Created by kec on 1/2/16.
 */
@Contract
public interface StampService
        extends DatastoreServices {
   static final UUID UNKNOWN_TRANSACTION_ID = new UUID(0,0);
   /**
    * STAMP sequences start at 1, in part to ensure that uninitialized values
    * (a zero by default) are not treated as valid stamp sequences.
    */
   int FIRST_STAMP_SEQUENCE = 1;

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
    * transaction. Should only be used by developers creating their own commit
    * service.
    *
    * @param transaction the author nid
    * @return the task
    */
   TimedTask<Void> cancel(Transaction transaction);

   /**
    * Used by the commit manger to commit a
    * transaction. Should only be used by developers creating their own commit
    * service.
    *
    * @param transaction the author nid
    * @param commitTime the commit time to associate with this transaction.
    * @return the task
    */
   TimedTask<Void> commit(Transaction transaction, long commitTime);

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
    * An idempotent operation to return a sequence that uniquely identified by
    * this combination of status, time, author, module, and path (STAMP). If an
    * existing sequence has this combination, that existing sequence will be
    * returned. If no sequence has this combination, a new sequence will be
    * created and returned.
    *
    *
    *
    * @param status the status
    * @param time the time
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    * @return the stampSequence
    * @throws IllegalStateException if the time is either Long.MAX_VALUE or Long.MIN_VALUE. Uncommitted versions
    * must use transactions.
    */
   int getStampSequence(Status status, long time, int authorNid, int moduleNid, int pathNid);

   /**
    * An idempotent operation to return a sequence that uniquely identified by
    * this combination of status, time, author, module, and path (STAMP) for a
    * particular transaction. If an existing sequence associated with the
    * transaction has this combination, that existing sequence will be
    * returned. If no sequence for a particular transaction has this combination,
    * a new sequence will be created and returned.
    *
    *
    * @param transaction the transaction
    * @param status the status
    * @param time the time
    * @param authorNid the author nid
    * @param moduleNid the module nid
    * @param pathNid the path nid
    * @return the stampSequence
    */
    int getStampSequence(Transaction transaction, Status status, long time, int authorNid, int moduleNid, int pathNid);

  /**
   * Gets the stamp sequences.
   *
   * @return an IntStream of all stamp sequences known to the stamp service.
   */
   IntStream getStampSequences();

   /**
    * Return the set of stamps that are between the two stamp coordinates, where
    * the returned values are exclusive of the start coordinate, and inclusive of the
    * end coordinate. IF authors are specified on the endCoordinate, only stamps from those
    * authors are included in the results.
    *
    * @param startFilter
    * @param endFilter
    * @return all stamps between the provided coordinates.
    */
   default StampSequenceSet getStampsBetweenCoordinates(StampFilter startFilter, StampFilter endFilter) {
      StampSequenceSet matchingStamps = new StampSequenceSet();

      VersionManagmentPathService pathService = Get.versionManagmentPathService();
      getStampSequences().forEach(stamp -> {
         if (pathService.getRelativePosition(stamp, startFilter.getStampPosition()) == RelativePosition.AFTER) {
            RelativePosition relativeToEnd = pathService.getRelativePosition(stamp, endFilter.getStampPosition());
            if (relativeToEnd == RelativePosition.EQUAL || relativeToEnd == RelativePosition.BEFORE) {
                  matchingStamps.add(stamp);
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

   default boolean isStampActive(int stampSequence) {
      return getStatusForStamp(stampSequence) == Status.ACTIVE;
   }

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
    * @param stampSequence
    * @return
    */
   Stamp getStamp(int stampSequence);

   /**
    *
    * @param stampSequence
    * @return the transaction id for an uncommitted stamp, or UNKNOWN_TRANSACTION_ID if the stamp is committed,
    * or is not associated with a transaction.
    */
   UUID getTransactionIdForStamp(int stampSequence);

   default StampPositionImmutable getStampPosition(int stampSequence) {
      return StampPositionImmutable.make(getTimeForStamp(stampSequence), getPathNidForStamp(stampSequence));
   }

   ImmutableIntSet getPathsInUse();

   default ImmutableSet<ConceptSpecification> getPathConceptsInUse() {
      MutableSet<ConceptSpecification> paths = Sets.mutable.empty();
      for (int pathNid: Get.stampService().getPathsInUse().toArray()) {
         paths.add(Get.concept(pathNid));
      }
      return paths.toImmutable();
   }

   ImmutableIntSet getModulesInUse();

   default ImmutableSet<ConceptSpecification> getModuleConceptsInUse() {
      MutableSet<ConceptSpecification> modules = Sets.mutable.empty();
      for (int moduleNid: Get.stampService().getModulesInUse().toArray()) {
         modules.add(Get.concept(moduleNid));
      }
      return modules.toImmutable();
   }

   ImmutableIntSet getAuthorsInUse();

   default ImmutableSet<ConceptSpecification> getAuthorConceptsInUse() {
      MutableSet<ConceptSpecification> authors = Sets.mutable.empty();
      for (int authorNid: Get.stampService().getAuthorsInUse().toArray()) {
         authors.add(Get.concept(authorNid));
      }
      return authors.toImmutable();
   }

   ImmutableLongList getTimesInUse();
}

