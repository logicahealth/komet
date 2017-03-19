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



package sh.isaac.api.index;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ConceptSearchResult}
 * Class to support merging search results based on the concepts that are associated with.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConceptSearchResult
         implements SearchResult {
   /**
    * The native id of the component(s) that matches the search.
    */
   public Set<Integer> nids = new HashSet<>();

   /**
    * The sequence ID of the concept most closely related to the search result (the concept referenced by a description, for example)
    */
   public int conceptSequence;

   /**
    * The score of the component with the best score, relative to the other matches.
    */
   public float bestScore;

   //~--- constructors --------------------------------------------------------

   public ConceptSearchResult(int conceptSequence, int componentNid, float score) {
      this.conceptSequence = conceptSequence;
      this.nids.add(componentNid);
      this.bestScore = score;
   }

   //~--- methods -------------------------------------------------------------

   public void merge(ConceptSearchResult other) {
      if (this.conceptSequence != other.conceptSequence) {
         throw new RuntimeException("Unmergeable!");
      }

      if (other.bestScore > this.bestScore) {
         this.bestScore = other.bestScore;
      }

      this.nids.addAll(other.getMatchingComponents());
   }

   public void merge(SearchResult other) {
      if (other.getScore() > this.bestScore) {
         this.bestScore = other.getScore();
      }

      this.nids.add(other.getNid());
   }

   //~--- get methods ---------------------------------------------------------

   public float getBestScore() {
      return this.bestScore;
   }

   public int getConceptSequence() {
      return this.conceptSequence;
   }

   public Collection<? extends Integer> getMatchingComponents() {
      return this.nids;
   }

   /**
    * Returns (an arbitrary) match nid from the set of component match nids
    */
   @Override
   public int getNid() {
      return this.nids.iterator()
                 .next();
   }

   /**
    * returns {@link #getBestScore()}
    */
   @Override
   public float getScore() {
      return getBestScore();
   }
}

