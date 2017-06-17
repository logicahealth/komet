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



package sh.isaac.utility.export;

//~--- JDK imports ------------------------------------------------------------

import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;

//~--- classes ----------------------------------------------------------------

/**
 * {@link AssociationInstance}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AssociationInstance {
   /** The sememe. */
   private final DynamicSememe<?> sememe;

   /** The stamp coordinate. */
   private final StampCoordinate stampCoordinate;

   /** The assn type. */
   private transient AssociationType associationType;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new association instance.
    *
    * @param data the data
    * @param stampCoordinate the stamp coordinate
    */

   // TODO Write the code that checks the index states on startup
   private AssociationInstance(DynamicSememe<?> data, StampCoordinate stampCoordinate) {
      this.sememe     = data;
      this.stampCoordinate = stampCoordinate;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Read the dynamic sememe instance (that represents an association) and turn it into an association object.
    *
    * @param data - the sememe to read
    * @param stampCoordinate - optional - only used during readback of the association type - will only be utilized
    * if one calls {@link AssociationInstance#getAssociationType()} - see {@link AssociationType#read(int, StampCoordinate)}
    * @return the association instance
    */
   public static AssociationInstance read(DynamicSememe<?> data, StampCoordinate stampCoordinate) {
      return new AssociationInstance(data, stampCoordinate);
   }

   /**
    * To string.
    *
    * @return the string
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      try {
         return "Association [Name: " + getAssociationType().getAssociationName() + " Inverse Name: " +
                getAssociationType().getAssociationInverseName() + " Source: " +
                getSourceComponent().getPrimordialUuid() + " Type: " +
                getAssociationType().getAssociationTypeConcept().getPrimordialUuid() + " Target: " +
                getTargetComponentData().toString() + "]";
      } catch (final Exception e) {
         LogManager.getLogger()
                   .error("Error formatting association instance", e);
         return this.sememe.toString();
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the association type.
    *
    * @return the association type
    */
   public AssociationType getAssociationType() {
      if (this.associationType == null) {
         this.associationType = AssociationType.read(this.sememe.getAssemblageSequence(),
               this.stampCoordinate,
               Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate());
      }

      return this.associationType;
   }

   /**
    * Gets the association type sequence.
    *
    * @return the concept sequence of the association type concept (without incurring the overhead of reading the AssoicationType object)
    */
   public int getAssociationTypeSequenece() {
      return this.sememe.getAssemblageSequence();
   }

   /**
    * Gets the data.
    *
    * @return the data
    */
   public DynamicSememe<?> getData() {
      return this.sememe;
   }

   /**
    * Gets the source component.
    *
    * @return the source component of the association.
    */
   public ObjectChronology<? extends StampedVersion> getSourceComponent() {
      return Get.identifiedObjectService()
                .getIdentifiedObjectChronology(this.sememe.getReferencedComponentNid())
                .get();
   }

   /**
    * Gets the source component data.
    *
    * @return the nid of the source component of the association
    */
   public int getSourceComponentData() {
      return this.sememe.getReferencedComponentNid();
   }

   /**
    * Gets the target component.
    *
    * @return - the target component (if any) linked by this association instance
    * This may return an empty if there was no target linked, or, if the target linked
    * was a UUID that isn't resolveable in this DB (in which case, see the {@link #getTargetComponentData()} method)
    */
   public Optional<? extends ObjectChronology<? extends StampedVersion>> getTargetComponent() {
      final int targetColIndex = AssociationUtilities.findTargetColumnIndex(this.sememe.getAssemblageSequence());

      if (targetColIndex >= 0) {
         final DynamicSememeData[] data = this.sememe.getData();

         if ((data != null) && (data.length > targetColIndex) && (data[targetColIndex] != null)) {
            int nid = 0;

            if ((data[targetColIndex].getDynamicSememeDataType() == DynamicSememeDataType.UUID) &&
                  Get.identifierService().hasUuid(((DynamicSememeUUID) data[targetColIndex]).getDataUUID())) {
               nid = Get.identifierService()
                        .getNidForUuids(((DynamicSememeUUID) data[targetColIndex]).getDataUUID());
            } else if (data[targetColIndex].getDynamicSememeDataType() == DynamicSememeDataType.NID) {
               nid = ((DynamicSememeNid) data[targetColIndex]).getDataNid();
            }

            if (nid != 0) {
               return Get.identifiedObjectService()
                         .getIdentifiedObjectChronology(nid);
            }
         }
      } else {
         throw new RuntimeException("unexpected");
      }

      return Optional.empty();
   }

   /**
    * Gets the target component data.
    *
    * @return the raw target component data - which will be of type {@link DynamicSememeNidBI} or {@link DynamicSememeUUID}
    * or, it may be empty, if there was not target.
    */
   public Optional<DynamicSememeData> getTargetComponentData() {
      final int targetColIndex = AssociationUtilities.findTargetColumnIndex(this.sememe.getAssemblageSequence());

      if (targetColIndex >= 0) {
         final DynamicSememeData[] data = this.sememe.getData();

         if ((data != null) && (data.length > targetColIndex) && (data[targetColIndex] != null)) {
            if (data[targetColIndex].getDynamicSememeDataType() == DynamicSememeDataType.UUID) {
               return Optional.of(((DynamicSememeUUID) data[targetColIndex]));
            } else if (data[targetColIndex].getDynamicSememeDataType() == DynamicSememeDataType.NID) {
               return Optional.of((DynamicSememeNid) data[targetColIndex]);
            }
         }
      } else {
         throw new RuntimeException("unexpected");
      }

      return Optional.empty();
   }
}

