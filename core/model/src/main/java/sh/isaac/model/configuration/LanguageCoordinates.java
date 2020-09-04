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



package sh.isaac.model.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.ChronologyChangeListener;
import sh.isaac.api.commit.CommitRecord;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;

/**
 * The Class LanguageCoordinates.
 *
 * @author kec
 */
//Even though this class is static, needs to be a service, so that the reset() gets fired at appropriate times.
@Service
@Singleton
public class LanguageCoordinates implements StaticIsaacCache {
   
   private static final Logger LOG = LogManager.getLogger();
   
   private static final Cache<Integer, ConceptSpecification[]> LANG_EXPAND_CACHE = Caffeine.newBuilder().maximumSize(100).build();
   private static ChronologyChangeListener ccl = null;
   
   /**
    * Case significance to concept nid.
    *
    * @param initialCaseSignificant the initial case significant
    * @return the int
    */
   public static int caseSignificanceToConceptSequence(boolean initialCaseSignificant) {
      return TermAux.caseSignificanceToConceptNid(initialCaseSignificant);
   }
   
   /**
    * Concept id to case significance.
    *
    * @param id the id
    * @return true, if successful
    */
   public static boolean conceptIdToCaseSignificance(int id) {
      return TermAux.conceptIdToCaseSignificance(id);
   }

   /**
    * Take in a list of the description type prefs, such as {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}
    * and include any non-core description types that are linked to these core types, in the right order, so that the LanguageCoordinates can include the 
    * non-core description types in the appropriate places when looking for descriptions.
    * @param descriptionTypePreferenceList the starting list - should only consist of core description types - 
    * {@link TermAux#FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}, {@link TermAux#REGULAR_NAME_DESCRIPTION_TYPE}, {@link TermAux#DEFINITION_DESCRIPTION_TYPE} 
    * @param stampFilter - optional - if not provided, uses {@link Coordinates.Filter.getDevelopmentLatestActiveOnly()}
    * @return the initial list, plus any equivalent non-core types in the appropriate order.  See {@link DynamicConstants#DYNAMIC_DESCRIPTION_CORE_TYPE}
    */
   public static ConceptSpecification[] expandDescriptionTypePreferenceList(ConceptSpecification[] descriptionTypePreferenceList, StampFilter stampFilter) {
      LOG.trace("Expand desription types requested");
      StampFilter filter = stampFilter == null ? Coordinates.Filter.DevelopmentLatestActiveOnly() : stampFilter;
      int requestKey = filter.hashCode();
      for (ConceptSpecification cs : descriptionTypePreferenceList) {
         requestKey = 97 * requestKey + cs.hashCode();
      }
      
      return LANG_EXPAND_CACHE.get(requestKey, keyAgain -> 
      {
         long time = System.currentTimeMillis();
        
         if (ccl == null) {
            ccl = new ChronologyChangeListener()
            {
               UUID me = UUID.randomUUID();
               {
                  Get.commitService().addChangeListener(this);
               }
               
               @Override
               public void handleCommit(CommitRecord commitRecord) {
                  // ignore
               }
               
               @Override
               public void handleChange(SemanticChronology sc) {
                  LANG_EXPAND_CACHE.invalidateAll();
               }
               
               @Override
               public void handleChange(ConceptChronology cc) {
                  LANG_EXPAND_CACHE.invalidateAll();
               }
               
               @Override
               public UUID getListenerUuid() {
                  return me;
               }
            };
         }
         HashMap<ConceptSpecification, HashSet<ConceptSpecification>> equivalentTypes = new HashMap<>();
         
         //Collect the mappings from core types -> non core types
         IntStream nids = Get.identifierService().getNidsForAssemblage(DynamicConstants.get().DYNAMIC_DESCRIPTION_CORE_TYPE.getNid());
         nids.forEach(nid -> {
             SemanticChronology sc = Get.assemblageService().getSemanticChronology(nid);
            DynamicVersion dv = (DynamicVersion)sc.getLatestVersion(filter).get();
            ConceptProxy coreType = new ConceptProxy(Get.identifierService().getNidForUuids(((DynamicUUID)dv.getData(0)).getDataUUID()));
            HashSet<ConceptSpecification> mapped = equivalentTypes.get(coreType);
            if (mapped == null) {
               mapped = new HashSet<>();
               equivalentTypes.put(coreType, mapped);
            }
            mapped.add(new ConceptProxy(sc.getReferencedComponentNid()));
         });
         
         if (equivalentTypes.isEmpty()) {
            //this method is a noop
            LOG.trace("Expanded description types call is a noop in {}ms", System.currentTimeMillis() - time);
            return descriptionTypePreferenceList;
         }
         
         ArrayList<ConceptSpecification> result = new ArrayList<>();
         ArrayList<Integer> startNids = new ArrayList<>();
         ArrayList<Integer> endNids = new ArrayList<>();
         for (ConceptSpecification coreType : descriptionTypePreferenceList) {
            startNids.add(coreType.getNid());
            if (!result.contains(coreType)) {
               result.add(coreType);
            }
            HashSet<ConceptSpecification> nonCoreTypes = equivalentTypes.get(coreType);
            if (nonCoreTypes != null) {
               for (ConceptSpecification type: nonCoreTypes) {
                  if (!result.contains(type)) {
                     result.add(type);
                     endNids.add(type.getNid());
                  }
               }
            }
         }
         LOG.info("Expanded language type list from {} to {} in {}ms", startNids, endNids, System.currentTimeMillis() - time);
         return result.toArray(new ConceptSpecification[result.size()]);
     });
   }

   @Override
   public void reset() {
      LANG_EXPAND_CACHE.invalidateAll();
   }
}
