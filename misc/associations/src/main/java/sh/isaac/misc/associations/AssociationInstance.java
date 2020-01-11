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

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.coordinate.StampCoordinate;


/**
 * {@link AssociationInstance}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AssociationInstance
{
   private DynamicVersion semantic;
   private StampCoordinate stampCoord;
   
   private transient AssociationType assnType_;

   //TODO [DAN 3] Write the code that checks the index states on startup
   
   private AssociationInstance(DynamicVersion data, StampCoordinate stampCoordinate)
   {
      this.semantic = data;
      this.stampCoord = stampCoordinate;
   }
   
   /**
    * Read the dynamic semantic instance (that represents an association) and turn it into an association object.
    * @param data - the semantic to read
    * @param stampCoordinate - optional - only used during readback of the association type - will only be utilized
    * if one calls {@link AssociationInstance#getAssociationType()} - see {@link AssociationType#read(int, StampCoordinate)}
    * @return
    */
   public static AssociationInstance read(DynamicVersion data, StampCoordinate stampCoordinate)
   {
      return new AssociationInstance(data, stampCoordinate);
   }
   
   public AssociationType getAssociationType()
   {
      if (assnType_ == null)
      {
         assnType_ = AssociationType.read(this.semantic.getAssemblageNid(), stampCoord, Get.languageCoordinateService().getUsEnglishLanguagePreferredTermCoordinate());
      }
      return assnType_;
   }

   /**
    * @return the source component of the association.
    */
   public Chronology getSourceComponent()
   {
      return Get.identifiedObjectService().getChronology(this.semantic.getReferencedComponentNid()).get();
   }
   
   /**
    * @return the nid of the source component of the association
    */
   public int getSourceComponentData()
   {
      return this.semantic.getReferencedComponentNid();
   }

   /**
    * @return - the target component (if any) linked by this association instance
    * This may return an empty if there was no target linked, or, if the target linked
    * was a UUID that isn't resolveable in this DB (in which case, see the {@link #getTargetComponentData()} method)
    */
   public Optional<? extends Chronology> getTargetComponent()
   {
      int targetColIndex = AssociationUtilities.findTargetColumnIndex(this.semantic.getAssemblageNid());
      if (targetColIndex >= 0)
      {
         DynamicData[] data = this.semantic.getData();
         if (data != null && data.length > targetColIndex && data[targetColIndex] != null)
         {
            int nid = 0;
            if (data[targetColIndex].getDynamicDataType() == DynamicDataType.UUID 
                  && Get.identifierService().hasUuid(((DynamicUUID) data[targetColIndex]).getDataUUID()))
            {
               nid = Get.identifierService().getNidForUuids(((DynamicUUID) data[targetColIndex]).getDataUUID());
            }
            else if (data[targetColIndex].getDynamicDataType() == DynamicDataType.NID)
            {
               nid = ((DynamicNid) data[targetColIndex]).getDataNid();
            }
            if (nid != 0)
            {   
               return Get.identifiedObjectService().getChronology(nid);
            }
         }
      }
      else
      {
         throw new RuntimeException("unexpected");
      }
      
      return Optional.empty();
   }
   
   /**
    * @return the raw target component data - which will be of type {@link DynamicNidBI} or {@link DynamicUUID}
    * or, it may be empty, if there was not target.
    */
   public Optional<DynamicData> getTargetComponentData()
   {
      int targetColIndex = AssociationUtilities.findTargetColumnIndex(this.semantic.getAssemblageNid());
      if (targetColIndex >= 0)
      {
         DynamicData[] data = this.semantic.getData();
         if (data != null && data.length > targetColIndex && data[targetColIndex] != null)
         {
            if (data[targetColIndex].getDynamicDataType() == DynamicDataType.UUID)
            {
               return Optional.of(((DynamicUUID) data[targetColIndex]));
            }
            else if (data[targetColIndex].getDynamicDataType() == DynamicDataType.NID)
            {
               return Optional.of((DynamicNid) data[targetColIndex]);
            }
         }
      }
      else
      {
         throw new RuntimeException("unexpected");
      }
      
      return Optional.empty();
   }

   /**
    * @return the concept nid of the association type concept (without incurring the overhead of reading the AssoicationType object)
    */
   public int getAssociationTypeNid() 
   {
      return this.semantic.getAssemblageNid();
   }

   public DynamicVersion getData()
   {
      return this.semantic;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      try
      {
         return "Association [Name: " + getAssociationType().getAssociationName() + " Inverse Name: " + getAssociationType().getAssociationInverseName() 
               + " Source: " + getSourceComponent().getPrimordialUuid() 
               + " Type: " + getAssociationType().getAssociationTypeConcept().getPrimordialUuid() + " Target: " + getTargetComponentData().toString() + "]";
      }
      catch (Exception e)
      {
         LogManager.getLogger().error("Error formatting association instance", e);
         return this.semantic.toString();
      }
   }
}
