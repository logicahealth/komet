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
   private final AtomicInteger                             concepts_                    = new AtomicInteger();
   private final AtomicInteger                             graphs_                      = new AtomicInteger();
   private final AtomicInteger                             clonedConcepts_              = new AtomicInteger();
   private final AtomicInteger                             skippedPropertiesCounter_    = new AtomicInteger();
   private final AtomicInteger                             generatedPreferredTermCount_ = new AtomicInteger();
   private final TreeMap<String, Integer>                  descriptions_                = new TreeMap<String, Integer>();
   private final TreeMap<String, Integer>                  refsetMembers_               = new TreeMap<String, Integer>();
   private final TreeMap<String, Integer>                  relationships_               = new TreeMap<String, Integer>();
   private final TreeMap<String, Integer>                  associations_                = new TreeMap<String, Integer>();
   private final TreeMap<String, TreeMap<String, Integer>> annotations_ = new TreeMap<String, TreeMap<String, Integer>>();
   private final Object                                    syncLock                     = new Object();

   //~--- methods -------------------------------------------------------------

   public void addAnnotation(String annotatedItem, String annotationName) {
      increment(this.annotations_, annotatedItem, annotationName);
   }

   public void addAssociation(String assnName) {
      increment(this.associations_, assnName);
   }

   public void addConcept() {
      this.concepts_.incrementAndGet();
   }

   public void addConceptClone() {
      this.clonedConcepts_.incrementAndGet();
   }

   public void addDescription(String descName) {
      increment(this.descriptions_, descName);
   }

   public void addGraph() {
      this.graphs_.incrementAndGet();
   }

   public void addRefsetMember(String refsetName) {
      increment(this.refsetMembers_, refsetName);
   }

   public void addRelationship(String relName) {
      increment(this.relationships_, relName);
   }

   public void addSkippedProperty() {
      this.skippedPropertiesCounter_.incrementAndGet();
   }

   public void incDescriptionCopiedFromFSNCount() {
      this.generatedPreferredTermCount_.incrementAndGet();
   }

   private void increment(TreeMap<String, Integer> dataHolder, String type) {
      synchronized (this.syncLock) {
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
      synchronized (this.syncLock) {
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
      return this.clonedConcepts_.get();
   }

   public int getConceptCount() {
      return this.concepts_.get();
   }

   public int getSkippedPropertyCount() {
      return this.skippedPropertiesCounter_.get();
   }

   public ArrayList<String> getSummary() {
      final ArrayList<String> result = new ArrayList<String>();

      result.add("Concepts: " + this.concepts_.get());
      result.add("Graphs: " + this.graphs_.get());

      if (this.clonedConcepts_.get() > 0) {
         result.add("Cloned Concepts: " + this.clonedConcepts_.get());
      }

      int sum = 0;

      for (final Map.Entry<String, Integer> value: this.relationships_.entrySet()) {
         sum += value.getValue();
         result.add("Relationship '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Relationships Total: " + sum);
      sum = 0;

      for (final Map.Entry<String, Integer> value: this.associations_.entrySet()) {
         sum += value.getValue();
         result.add("Association '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Associations Total: " + sum);
      sum = 0;

      for (final Map.Entry<String, Integer> value: this.descriptions_.entrySet()) {
         sum += value.getValue();
         result.add("Description '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Descriptions Total: " + sum);

      if (this.generatedPreferredTermCount_.get() > 0) {
         result.add("Descriptions duplicated from FSN: " + this.generatedPreferredTermCount_.get());
      }

      sum = 0;

      int nestedSum = 0;

      for (final Map.Entry<String, TreeMap<String, Integer>> value: this.annotations_.entrySet()) {
         nestedSum = 0;

         for (final Map.Entry<String, Integer> nestedValue: value.getValue()
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

      if (this.skippedPropertiesCounter_.get() > 0) {
         result.add("Skipped Properties: " + this.skippedPropertiesCounter_.get());
      }

      sum = 0;

      for (final Map.Entry<String, Integer> value: this.refsetMembers_.entrySet()) {
         sum += value.getValue();
         result.add("Refset Members '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Refset Members Total: " + sum);
      return result;
   }
}

