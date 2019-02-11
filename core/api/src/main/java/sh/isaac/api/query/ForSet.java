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



package sh.isaac.api.query;

//~--- JDK imports ------------------------------------------------------------



//~--- non-JDK imports --------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 11/2/14.
 */
public class ForSet {

   //~--- constructors --------------------------------------------------------
    private final List<ConceptSpecification> assemblageSpecificationsForSet;
   /**
    * Instantiates a new for set specification.
    */
   public ForSet() {
       assemblageSpecificationsForSet = new ArrayList<>();
   }

   /**
    * Instantiates a new for set specification.
    *
    * @param assemblageSpecificationsForSet the for collection assemblage
    */
   public ForSet(List<ConceptSpecification> assemblageSpecificationsForSet) {
      this.assemblageSpecificationsForSet = assemblageSpecificationsForSet;
   }

   //~--- get methods ---------------------------------------------------------

   public List<ConceptSpecification> getForSet() {
       return assemblageSpecificationsForSet;
   }

    /**
     * Gets the collection.
     *
     * @return the collection
     */
    public Map<ConceptSpecification, NidSet> getCollectionMap() {
        Map<ConceptSpecification, NidSet> collections = new HashMap<>();
        for (ConceptSpecification assemblage: assemblageSpecificationsForSet) {
            NidSet assemblageNids = NidSet.of(Get.identifierService().getNidsForAssemblage(assemblage));
            collections.put(assemblage, assemblageNids);
        }
        return collections;
    }
   
   public Map<ConceptSpecification, Integer> getAssembalgeToIndexMap() {
       Map<ConceptSpecification, Integer> returnValue = new HashMap();
       for (int i = 0; i < assemblageSpecificationsForSet.size(); i++) {
           ConceptSpecification spec = assemblageSpecificationsForSet.get(i);
           returnValue.put(spec, i);
       }
       return returnValue;
   }
   public Map<ConceptSpecification, NidSet> getPossibleComponents() {
        Map<ConceptSpecification, NidSet> possibleComponents = new HashMap<>(assemblageSpecificationsForSet.size());
        for (ConceptSpecification spec: assemblageSpecificationsForSet) {
            possibleComponents.put(spec, NidSet.of(Get.identifierService().getNidsForAssemblage(spec)));
        }
        return possibleComponents;
    }        
            
    public static Map<ConceptSpecification, NidSet> deepClone(Map<ConceptSpecification, NidSet> forSet) {
        Map<ConceptSpecification, NidSet> resultMap = new HashMap<>(forSet.size());
        for (Map.Entry<ConceptSpecification, NidSet> entry: forSet.entrySet()) {
            resultMap.put(entry.getKey(), NidSet.of(entry.getValue()));
        }
        return resultMap;
    }
    
    
}

