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



package sh.isaac.api.classifier;

import sh.isaac.api.Get;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.util.time.DateTimeUtil;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static sh.isaac.api.util.time.DateTimeUtil.TEXT_FORMAT_WITH_ZONE;

/**
 * The Class ClassifierResults.
 *
 * @author kec
 */
public interface ClassifierResults {


   /**
    * Get the set of concepts sent to the classifier for evaluation.
    *
    * @return the concepts included in the classification.
    */
   Set<Integer> getClassificationConceptSet();

   /**
    * Get the set of concepts that had inferred form changes as a result
    * of classification.
    *
    * @return the concepts with inferred changes.
    */
   Set<Integer> getConceptsWithInferredChanges();

   /**
    * Gets the commit record.
    *
    * @return the commit record
    */
   Optional<CommitRecord> getCommitRecord();

   /**
    * Gets the equivalent sets.
    *
    * @return the equivalent sets
    */
   Set<int[]> getEquivalentSets();

   /**
    * If this Optional is present, then the classification was not performed due to these cycles that were found.
    *
    * When a cycle was detected, the rest of the classification is aborted, so no other details in this class are populated.
    *
    * @return A map of concept nids to sets of nid arrays, each set represents a cycle that the concept nid is
    * involved in, and the nid[] is the cycle path.  Returns an empty object, if no cycles were detected.
    */
   Optional<Map<Integer, Set<int[]>>> getCycles();

   /**
    * Add concept nids that were detected as orphans
    * @param orphans
    */
   void addOrphans(Set<Integer> orphans);

   /**
    * @return The list of orphaned concept nids that were detected during classification
    */
   Set<Integer> getOrphans();

   StampFilter getStampFilter();

   LogicCoordinate getLogicCoordinate();

   EditCoordinate getEditCoordinate();

   Instant getCommitTime();

   default String getDefaultText() {
      StringBuilder sb = new StringBuilder();
      sb.append(TEXT_FORMAT_WITH_ZONE.format(getCommitTime().atZone(ZoneOffset.systemDefault())));
      sb.append(" written to the ");
      sb.append(Get.conceptDescriptionText(getEditCoordinate().getModuleNid()));
      return sb.toString();
   }
}