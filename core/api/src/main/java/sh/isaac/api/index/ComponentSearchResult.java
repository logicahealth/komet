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

/**
 * Class to termstore the nid of a component that matches a search, and the
 * sore of that component's match.
 * @author kec
 */
public class ComponentSearchResult
         implements SearchResult {
   /**
    * The native id of the component that matches the search.
    */
   public int nid;

   /**
    * The score of the components match relative to other matches.
    */
   public float score;

   //~--- constructors --------------------------------------------------------

   public ComponentSearchResult(int nid, float score) {
      this.nid   = nid;
      this.score = score;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getNid() {
      return nid;
   }

   //~--- set methods ---------------------------------------------------------

   public void setNid(int nid) {
      this.nid = nid;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public float getScore() {
      return score;
   }

   //~--- set methods ---------------------------------------------------------

   public void setScore(float score) {
      this.score = score;
   }
}

