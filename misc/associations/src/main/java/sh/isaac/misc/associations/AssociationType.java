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

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.*;
import sh.isaac.utility.Frills;



public class AssociationType
{
   private int associationNid;
   private String associationName_;
   private Optional<String> associationInverseName_;
   private String description_;
   
   private static final Logger log = LogManager.getLogger();
   
   private AssociationType(int nid)
   {
      this.associationNid = nid;
   }

   /**
    * Read all details that define an Association.  
    * @param conceptNid The concept that represents the association assemblage
    * @param manifoldCoordinate optional - uses system default if not provided.
    * @return the AssociationType information
    */
   public static AssociationType read(int conceptNid, ManifoldCoordinate manifoldCoordinate)
   {
      AssociationType at = new AssociationType(conceptNid);

      ManifoldCoordinate localManifoldCoordinate = (manifoldCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getManifoldCoordinate() : manifoldCoordinate);

      at.associationName_ = Get.conceptService().getSnapshot(manifoldCoordinate).conceptDescriptionText(conceptNid);
      
      //Find the inverse name
      for (DescriptionVersion desc : Frills.getDescriptionsOfType(conceptNid, MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR, localManifoldCoordinate.getViewStampFilter().makeCoordinateAnalog(Status.ACTIVE)))
      {
         
         if (Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(desc.getNid(), 
               DynamicConstants.get().DYNAMIC_ASSOCIATION_INVERSE_NAME.getNid()).anyMatch(nestedSemantic ->
         {
            if (nestedSemantic.getVersionType() == VersionType.DYNAMIC)
            {
               return nestedSemantic.getLatestVersion(localManifoldCoordinate.getViewStampFilter()).isPresent();
            }
            return false;
         }))
         {
            at.associationInverseName_ = Optional.of(desc.getText());
         }
      }
      
      //find the description
      for (DescriptionVersion desc : Frills.getDescriptionsOfType(at.getAssociationType(),
            MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR, localManifoldCoordinate.getViewStampFilter().makeCoordinateAnalog(Status.ACTIVE)))
      {
         if (Frills.isDescriptionPreferred(desc.getNid(), localManifoldCoordinate.getViewStampFilter()) &&
               Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(desc.getNid(), 
                     DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION.getNid()).anyMatch(nestedSemantic ->
         {
            if (nestedSemantic.getVersionType() == VersionType.DYNAMIC)
            {
               return nestedSemantic.getLatestVersion(localManifoldCoordinate.getViewStampFilter()).isPresent();
            }
            return false;
         }))
         {
            at.description_ = desc.getText();
         }
      }
      
      if (at.associationInverseName_ == null)
      {
         at.associationInverseName_ = Optional.empty();
      }
      if (at.description_ == null)
      {
         at.description_ = "-No description on path!-";
      }
      
      return at;
   }
   
   /**
    * @return the association type concept
    */
   public ConceptChronology getAssociationTypeConcept() 
   {
      return Get.conceptService().getConceptChronology(associationNid);
   }
   
   /**
    * @return the concept nid of the association type concept (assemblage concept)
    */
   public int getAssociationType() 
   {
      return associationNid;
   }
   

   public String getAssociationName()
   {
      return associationName_;
   }
   
   /**
    * @return the inverse name of the association (if present) (Read from the association type concept)
    */
   public Optional<String> getAssociationInverseName()
   {
      return associationInverseName_;
   }
   
   public String getDescription()
   {
      return description_;
   }

}
