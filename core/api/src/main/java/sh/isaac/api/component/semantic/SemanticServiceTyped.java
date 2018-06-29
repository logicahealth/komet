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



package sh.isaac.api.component.semantic;

//~--- JDK imports ------------------------------------------------------------

import java.util.stream.Stream;
import sh.isaac.api.collections.IntSet;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.StampPosition;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface SemanticServiceTyped.
 *
 * @author kec
 */
public interface SemanticServiceTyped {
   /**
    * Write semantic.
    *
    * @param semanticChronicle the semantic chronicle
    */
   void writeSemanticChronology(SemanticChronology semanticChronicle);

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the parallel semantic stream.
    *
    * @param <V>
    * @return the parallel semantic stream
    */
   <V extends SemanticChronology> Stream<V> getParallelSemanticChronologyStream();

   /**
    * Gets the semantic.
    *
    * @param semanticNid the semantic nid
    * @return the semantic
    */
   SemanticChronology getSemanticChronology(int semanticNid);

   /**
    * Gets the semantic nids for component.
    *
    * @param componentNid the component nid
    * @return the semantic nids for component
    */
   IntSet getSemanticNidsForComponent(int componentNid);

   /**
    * Gets the semantic nids for component from assemblage.
    *
    * @param componentNid the component nid
    * @param assemblageConceptNid the assemblage nid
    * @return the semantic nids for component from assemblage
    */
   IntSet getSemanticNidsForComponentFromAssemblage(int componentNid, int assemblageConceptNid);

   /**
    * Gets the semantic nids for components from assemblage.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptNid the assemblage concept nid
    * @return the semantic nids for components from assemblage
    */
   IntSet getSemanticNidsForComponentsFromAssemblage(NidSet componentNidSet, int assemblageConceptNid);

   /**
    * Gets the semantic nids for components from assemblage modified after position.
    *
    * @param componentNidSet the component nid set
    * @param assemblageConceptNid the assemblage nid
    * @param position the position
    * @return the semantic nids for components from assemblage modified after position
    */
   IntSet getSemanticNidsForComponentsFromAssemblageModifiedAfterPosition(NidSet componentNidSet,
         int assemblageConceptNid,
         StampPosition position);

   /**
    * Gets the semantic nids from assemblage.
    *
    * @param assemblageConceptNid the assemblage concept nid
    * @return the semantic nids from assemblage
    */
   IntSet getSemanticNidsFromAssemblage(int assemblageConceptNid);

   /**
    * Gets the semantic nids from assemblage modified after position.
    *
    * @param assemblageConceptNid the assemblage nid
    * @param position the position
    * @return the semantic nids from assemblage modified after position
    */
   IntSet getSemanticNidsFromAssemblageModifiedAfterPosition(int assemblageConceptNid,
         StampPosition position);

   /**
    * Gets the semantic stream.
    *
    * @param <V>
    * @return the semantic stream
    */
   <V extends SemanticChronology> Stream<V> getSemanticChronologyStream();

   /**
    * Gets the SemanticChronologies for component.
    *
    * @param <V>
    * @param componentNid the component nid
    * @return the SemanticChronologies for component
    */
   <V extends SemanticChronology> Stream<V> getSemanticChronologiesForComponent(int componentNid);

   /**
    * Gets the SemanticChronologies for component from assemblage.
    *
    * @param <V>
    * @param componentNid the component nid
    * @param assemblageConceptNid the assemblage concept nid
    * @return the SemanticChronologies for component from assemblage
    */
   <V extends SemanticChronology> Stream<V> getSemanticChronologiesForComponentFromAssemblage(int componentNid, int assemblageConceptNid);

   /**
    * Gets the SemanticChronologies from assemblage.
    *
    * @param <V>
    * @param assemblageConceptNid the assemblage sequence
    * @return the SemanticChronologies from assemblage
    */
   <V extends SemanticChronology> Stream<V> getSemanticChronologiesFromAssemblage(int assemblageConceptNid);
}

