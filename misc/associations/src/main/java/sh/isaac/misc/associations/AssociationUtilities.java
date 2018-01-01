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
package sh.isaac.misc.associations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.model.semantic.DynamicUtilityImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.utility.Frills;


/**
 * {@link AssociationUtilities}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AssociationUtilities
{
   private static int associationNid = Integer.MIN_VALUE;
   
   private static int getAssociationNid()
   {
      if (associationNid == Integer.MIN_VALUE)
      {
         associationNid = DynamicConstants.get().DYNAMIC_ASSOCIATION.getNid();
      }
      return associationNid;
   }
   
   /**
    * Get a particular associations 
    * @param associationNid
    * @param stamp - optional - if not provided, uses the default from the config service
    * @return the found associationInstance, if present on the provided stamp path
    */
   public static Optional<AssociationInstance> getAssociation(int associationNid, StampCoordinate stamp)
   {
      StampCoordinate localStamp = stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp;
      SemanticChronology sc = Get.assemblageService().getSemanticChronology(associationNid);
      LatestVersion<Version> latest = sc.getLatestVersion(localStamp);
      if (latest.isPresent())
      {
         return Optional.of(AssociationInstance.read((DynamicVersion<?>)latest.get(), stamp));
      }
      return Optional.empty();
   }

   /**
    * Get all associations that originate on the specified componentNid
    * @param componentNid
    * @param stamp - optional - if not provided, uses the default from the config service
    */
   public static List<AssociationInstance> getSourceAssociations(int componentNid, StampCoordinate stamp)
   {
      ArrayList<AssociationInstance> results = new ArrayList<>();
      StampCoordinate localStamp = stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp;
      Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblages(componentNid, getAssociationConceptSequences())
         .forEach(associationC -> 
            {
               LatestVersion<Version> latest = associationC.getLatestVersion(localStamp);
               if (latest.isPresent())
               {
                  results.add(AssociationInstance.read((DynamicVersion<?>)latest.get(), stamp));
               }
               
            });
      return results;
   }

   /**
    * Get all association instances that have a target of the specified componentNid
    * @param componentNid
    * @param stamp - optional - if not provided, uses the default from the config service
    */
   //TODO should probably have a method here that takes in a target UUID, since that seems to be how I stored them?
   public static List<AssociationInstance> getTargetAssociations(int componentNid, StampCoordinate stamp)
   {
      ArrayList<AssociationInstance> result = new ArrayList<>();

      IndexSemanticQueryService indexer = LookupService.getService(IndexSemanticQueryService.class);
      if (indexer == null)
      {
         throw new RuntimeException("Required index is not available");
      }
      
      UUID uuid;
      ArrayList<Integer> associationTypes = new ArrayList<>();
//      ArrayList<Integer> colIndex = new ArrayList<>();
      
      try
      {
         uuid = Get.identifierService().getUuidPrimordialForNid(componentNid).orElse(null);   

         for (Integer associationTypeSequenece : getAssociationConceptSequences())
         {
            associationTypes.add(associationTypeSequenece);
//            colIndex.add(findTargetColumnIndex(associationTypeSequenece));
         }
         
         //TODO when issue with colIndex restrictions is fixed, put it back.
         List<SearchResult> refexes = indexer.queryData(new DynamicStringImpl(componentNid + (uuid == null ? "" : " OR " + uuid)),
               false, associationTypes.toArray(new Integer[associationTypes.size()]), null, null, null, null, null);
         for (SearchResult sr : refexes)
         {
            @SuppressWarnings("rawtypes")
            LatestVersion<DynamicVersion> latest = Get.assemblageService().getSnapshot(DynamicVersion.class, 
                  stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp).getLatestSemanticVersion(sr.getNid());
            
            if (latest.isPresent())
            {
               result.add(AssociationInstance.read(latest.get(), stamp));
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      return result;
   }

   /**
    * 
    * @param associationTypeConceptNid
    * @param stamp - optional - if not provided, uses the default from the config service
    * @return
    */
   public static List<AssociationInstance> getAssociationsOfType(int associationTypeConceptNid, StampCoordinate stamp)
   {
      ArrayList<AssociationInstance> results = new ArrayList<>();
      StampCoordinate localStamp = stamp == null ? Get.configurationService().getDefaultStampCoordinate() : stamp;
      Get.assemblageService().getSemanticChronologyStreamFromAssemblage(associationTypeConceptNid)
         .forEach(associationC -> 
            {
               LatestVersion<Version> latest = associationC.getLatestVersion(localStamp);
               if (latest.isPresent())
               {
                  results.add(AssociationInstance.read((DynamicVersion<?>)latest.get(), stamp));
               }
               
            });
      return results;
   }

   /**
    * Get a list of all of the concepts that identify a type of association - returning their concept sequence identifier.
    * @return
    */
   public static Set<Integer> getAssociationConceptSequences()
   {
      HashSet<Integer> result = new HashSet<>();

      Get.assemblageService().getSemanticChronologyStreamFromAssemblage(getAssociationNid()).forEach(associationC ->
      {
         result.add(associationC.getReferencedComponentNid());
      });
      return result;
   }

   /**
    * @param assemblageNidOrSequence
    */
   protected static int findTargetColumnIndex(int assemblageNidOrSequence)
   {
      DynamicUsageDescription rdud = LookupService.get().getService(DynamicUtilityImpl.class).readDynamicUsageDescription(assemblageNidOrSequence);

      for (DynamicColumnInfo rdci : rdud.getColumnInfo())
      {
         if (rdci.getColumnDescriptionConcept().equals(DynamicConstants.get().DYNAMIC_COLUMN_ASSOCIATION_TARGET_COMPONENT.getUUID()))
         {
            return rdci.getColumnOrder();
         }
      }
      return Integer.MIN_VALUE;
   }
   
   public static boolean isAssociation(SemanticChronology sc)
   {
      return Frills.definesAssociation(sc);
   }
}
