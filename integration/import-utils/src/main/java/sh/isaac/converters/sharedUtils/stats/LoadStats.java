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



package sh.isaac.converters.sharedUtils.stats;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

//~--- classes ----------------------------------------------------------------

/**
 * Keep counts on all of the types of things that are converted.
 *
 * @author Daniel Armbrust
 */
public class LoadStats {
   private AtomicInteger                             concepts_                    = new AtomicInteger();
   private AtomicInteger                             graphs_                      = new AtomicInteger();
   private AtomicInteger                             clonedConcepts_              = new AtomicInteger();
   private AtomicInteger                             skippedPropertiesCounter_    = new AtomicInteger();
   private AtomicInteger                             generatedPreferredTermCount_ = new AtomicInteger();
   private TreeMap<String, Integer>                  descriptions_                = new TreeMap<String, Integer>();
   private TreeMap<String, Integer>                  refsetMembers_               = new TreeMap<String, Integer>();
   private TreeMap<String, Integer>                  relationships_               = new TreeMap<String, Integer>();
   private TreeMap<String, Integer>                  associations_                = new TreeMap<String, Integer>();
   private TreeMap<String, TreeMap<String, Integer>> annotations_ = new TreeMap<String, TreeMap<String, Integer>>();
   private Object                                    syncLock                     = new Object();

   //~--- methods -------------------------------------------------------------

   public void addAnnotation(String annotatedItem, String annotationName) {
      increment(annotations_, annotatedItem, annotationName);
   }

   public void addAssociation(String assnName) {
      increment(associations_, assnName);
   }

   public void addConcept() {
      concepts_.incrementAndGet();
   }

   public void addConceptClone() {
      clonedConcepts_.incrementAndGet();
   }

   public void addDescription(String descName) {
      increment(descriptions_, descName);
   }

   public void addGraph() {
      graphs_.incrementAndGet();
   }

   public void addRefsetMember(String refsetName) {
      increment(refsetMembers_, refsetName);
   }

   public void addRelationship(String relName) {
      increment(relationships_, relName);
   }

   public void addSkippedProperty() {
      skippedPropertiesCounter_.incrementAndGet();
   }

   public void incDescriptionCopiedFromFSNCount() {
      generatedPreferredTermCount_.incrementAndGet();
   }

   private void increment(TreeMap<String, Integer> dataHolder, String type) {
      synchronized (syncLock) {
         Integer i = dataHolder.get(type);

         if (i == null) {
            i = new Integer(1);
         } else {
            i++;
         }

         dataHolder.put(type, i);
      }
   }

   private void increment(TreeMap<String, TreeMap<String, Integer>> dataHolder, String annotatedType, String type) {
      synchronized (syncLock) {
         TreeMap<String, Integer> map = dataHolder.get(annotatedType);

         if (map == null) {
            map = new TreeMap<String, Integer>();
         }

         Integer i = map.get(type);

         if (i == null) {
            i = new Integer(1);
         } else {
            i++;
         }

         map.put(type, i);
         dataHolder.put(annotatedType, map);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getClonedConceptCount() {
      return clonedConcepts_.get();
   }

   public int getConceptCount() {
      return concepts_.get();
   }

   public int getSkippedPropertyCount() {
      return skippedPropertiesCounter_.get();
   }

   public ArrayList<String> getSummary() {
      ArrayList<String> result = new ArrayList<String>();

      result.add("Concepts: " + concepts_.get());
      result.add("Graphs: " + graphs_.get());

      if (clonedConcepts_.get() > 0) {
         result.add("Cloned Concepts: " + clonedConcepts_.get());
      }

      int sum = 0;

      for (Map.Entry<String, Integer> value: relationships_.entrySet()) {
         sum += value.getValue();
         result.add("Relationship '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Relationships Total: " + sum);
      sum = 0;

      for (Map.Entry<String, Integer> value: associations_.entrySet()) {
         sum += value.getValue();
         result.add("Association '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Associations Total: " + sum);
      sum = 0;

      for (Map.Entry<String, Integer> value: descriptions_.entrySet()) {
         sum += value.getValue();
         result.add("Description '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Descriptions Total: " + sum);

      if (generatedPreferredTermCount_.get() > 0) {
         result.add("Descriptions duplicated from FSN: " + generatedPreferredTermCount_.get());
      }

      sum = 0;

      int nestedSum = 0;

      for (Map.Entry<String, TreeMap<String, Integer>> value: annotations_.entrySet()) {
         nestedSum = 0;

         for (Map.Entry<String, Integer> nestedValue: value.getValue()
               .entrySet()) {
            result.add("Annotation '" + value.getKey() + ":" + nestedValue.getKey() + "': " + nestedValue.getValue());
            nestedSum += nestedValue.getValue();
         }

         sum += nestedSum;

         if (value.getValue()
                  .size() > 1) {
            result.add("Annotation '" + value.getKey() + "' Total: " + nestedSum);
         }
      }

      result.add("Annotations Total: " + sum);

      if (skippedPropertiesCounter_.get() > 0) {
         result.add("Skipped Properties: " + skippedPropertiesCounter_.get());
      }

      sum = 0;

      for (Map.Entry<String, Integer> value: refsetMembers_.entrySet()) {
         sum += value.getValue();
         result.add("Refset Members '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Refset Members Total: " + sum);
      return result;
   }
}

