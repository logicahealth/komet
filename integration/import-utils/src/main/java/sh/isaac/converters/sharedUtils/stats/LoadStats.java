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
   /** The concepts. */
   private final AtomicInteger concepts = new AtomicInteger();

   /** The graphs. */
   private final AtomicInteger graphs = new AtomicInteger();

   /** The cloned concepts. */
   private final AtomicInteger clonedConcepts = new AtomicInteger();

   /** The skipped properties counter. */
   private final AtomicInteger skippedPropertiesCounter = new AtomicInteger();

   /** The generated preferred term count. */
   private final AtomicInteger generatedPreferredTermCount = new AtomicInteger();

   /** The descriptions. */
   private final TreeMap<String, Integer> descriptions = new TreeMap<>();

   /** The refset members. */
   private final TreeMap<String, Integer> refsetMembers = new TreeMap<>();

   /** The relationships. */
   private final TreeMap<String, Integer> relationships = new TreeMap<>();

   /** The associations. */
   private final TreeMap<String, Integer> associations = new TreeMap<>();

   /** The annotations. */
   private final TreeMap<String, TreeMap<String, Integer>> annotations = new TreeMap<>();

   /** The sync lock. */
   private final Object syncLock = new Object();

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the annotation.
    *
    * @param annotatedItem the annotated item
    * @param annotationName the annotation name
    */
   public void addAnnotation(String annotatedItem, String annotationName) {
      increment(this.annotations, annotatedItem, annotationName);
   }

   /**
    * Adds the association.
    *
    * @param assnName the assn name
    */
   public void addAssociation(String assnName) {
      increment(this.associations, assnName);
   }

   /**
    * Adds the concept.
    */
   public void addConcept() {
      this.concepts.incrementAndGet();
   }

   /**
    * Adds the concept clone.
    */
   public void addConceptClone() {
      this.clonedConcepts.incrementAndGet();
   }

   /**
    * Adds the description.
    *
    * @param descName the desc name
    */
   public void addDescription(String descName) {
      increment(this.descriptions, descName);
   }

   /**
    * Adds the graph.
    */
   public void addGraph() {
      this.graphs.incrementAndGet();
   }

   /**
    * Adds the refset member.
    *
    * @param refsetName the refset name
    */
   public void addRefsetMember(String refsetName) {
      increment(this.refsetMembers, refsetName);
   }

   /**
    * Adds the relationship.
    *
    * @param relName the rel name
    */
   public void addRelationship(String relName) {
      increment(this.relationships, relName);
   }

   /**
    * Adds the skipped property.
    */
   public void addSkippedProperty() {
      this.skippedPropertiesCounter.incrementAndGet();
   }

   /**
    * Inc description copied from FQN count.
    */
   public void incDescriptionCopiedFromFQNCount() {
      this.generatedPreferredTermCount.incrementAndGet();
   }

   /**
    * Increment.
    *
    * @param dataHolder the data holder
    * @param type the type
    */
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

   /**
    * Increment.
    *
    * @param dataHolder the data holder
    * @param annotatedType the annotated type
    * @param type the type
    */
   private void increment(TreeMap<String, TreeMap<String, Integer>> dataHolder, String annotatedType, String type) {
      synchronized (this.syncLock) {
         TreeMap<String, Integer> map = dataHolder.get(annotatedType);

         if (map == null) {
            map = new TreeMap<>();
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

   /**
    * Gets the cloned concept count.
    *
    * @return the cloned concept count
    */
   public int getClonedConceptCount() {
      return this.clonedConcepts.get();
   }

   /**
    * Gets the concept count.
    *
    * @return the concept count
    */
   public int getConceptCount() {
      return this.concepts.get();
   }

   /**
    * Gets the skipped property count.
    *
    * @return the skipped property count
    */
   public int getSkippedPropertyCount() {
      return this.skippedPropertiesCounter.get();
   }

   /**
    * Gets the summary.
    *
    * @return the summary
    */
   public ArrayList<String> getSummary() {
      final ArrayList<String> result = new ArrayList<>();

      result.add("Concepts: " + this.concepts.get());
      result.add("Graphs: " + this.graphs.get());

      if (this.clonedConcepts.get() > 0) {
         result.add("Cloned Concepts: " + this.clonedConcepts.get());
      }

      int sum = 0;

      for (final Map.Entry<String, Integer> value: this.relationships.entrySet()) {
         sum += value.getValue();
         result.add("Relationship '" + value.getKey() + "': " + value.getValue());
      }

      if (sum > 0) {
         result.add("Relationships Total: " + sum);
         sum = 0;
      }

      for (final Map.Entry<String, Integer> value: this.associations.entrySet()) {
         sum += value.getValue();
         result.add("Association '" + value.getKey() + "': " + value.getValue());
      }

      if (sum > 0) {
         result.add("Associations Total: " + sum);
         sum = 0;
      }

      for (final Map.Entry<String, Integer> value: this.descriptions.entrySet()) {
         sum += value.getValue();
         result.add("Description '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Descriptions Total: " + sum);

      if (this.generatedPreferredTermCount.get() > 0) {
         result.add("Descriptions duplicated from FQN: " + this.generatedPreferredTermCount.get());
      }

      sum = 0;

      int nestedSum = 0;

      for (final Map.Entry<String, TreeMap<String, Integer>> value: this.annotations.entrySet()) {
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

      if (this.skippedPropertiesCounter.get() > 0) {
         result.add("Skipped Properties: " + this.skippedPropertiesCounter.get());
      }

      sum = 0;

      for (final Map.Entry<String, Integer> value: this.refsetMembers.entrySet()) {
         sum += value.getValue();
         result.add("Refset Members '" + value.getKey() + "': " + value.getValue());
      }

      result.add("Refset Members Total: " + sum);
      return result;
   }
}

