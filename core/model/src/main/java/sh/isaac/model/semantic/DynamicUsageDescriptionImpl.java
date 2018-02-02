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



package sh.isaac.model.semantic;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.configuration.StampCoordinates;

//~--- classes ----------------------------------------------------------------

/**
 * See {@link DynamicUsageDescription}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicUsageDescriptionImpl
         implements DynamicUsageDescription {
   /** The Constant logger. */
   protected static final Logger logger = Logger.getLogger(DynamicUsageDescription.class.getName());

   /** The cache. */
   private static Cache<Integer, DynamicUsageDescriptionImpl> cache =
         Caffeine.newBuilder().maximumSize(25).build();

   //~--- fields --------------------------------------------------------------

   /** The refex usage descriptor sequence. */
   int refexUsageDescriptorNid;

   /** The sememe usage description. */
   String sememeUsageDescription;

   /** The name. */
   String name;

   /** The refex column info. */
   DynamicColumnInfo[] refexColumnInfo;

   /** The referenced component type restriction. */
   IsaacObjectType referencedComponentTypeRestriction;

   /** The referenced component type sub restriction. */
   VersionType referencedComponentTypeSubRestriction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic element usage description impl.
    */
   private DynamicUsageDescriptionImpl() {
      // For use by the mock static method
   }

   /**
    * Read the RefexUsageDescription data from the database for a given sequence or nid.
    *
    * Note that most users should call {@link #read(int)} instead, as that
    * utilizes a cache. This always reads directly from the DB.
    *
    * @param refexUsageDescriptorId sequence or NID of refexUsageDescriptor
    */
   @SuppressWarnings("unchecked")
   public DynamicUsageDescriptionImpl(int refexUsageDescriptorId) {
      final ConceptChronology assemblageConcept = Get.conceptService()
                                                        .getConceptChronology(refexUsageDescriptorId);

      this.refexUsageDescriptorNid = assemblageConcept.getNid();

      final TreeMap<Integer, DynamicColumnInfo> allowedColumnInfo = new TreeMap<>();

      for (final SemanticChronology descriptionSememe:
            assemblageConcept.getConceptDescriptionList()) {
         @SuppressWarnings("rawtypes")
         final LatestVersion descriptionVersion =
            ((SemanticChronology) descriptionSememe).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly());

         if (descriptionVersion.isPresent()) {
            @SuppressWarnings("rawtypes")
            final DescriptionVersion ds = (DescriptionVersion) descriptionVersion.get();

            if (ds.getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
               final Optional<SemanticChronology> nestesdSememe = Get.assemblageService()
                                                                                               .getSemanticChronologyStreamForComponentFromAssemblage(ds.getNid(),
                                                                                                        DynamicConstants.get().DYNAMIC_DEFINITION_DESCRIPTION
                                                                                                              .getNid())
                                                                                               .findAny();

               if (nestesdSememe.isPresent()) {
                  this.sememeUsageDescription = ds.getText();
               }
               ;
            }

            if (ds.getDescriptionTypeConceptNid() ==
                  TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
               this.name = ds.getText();
            }

            if ((this.sememeUsageDescription != null) && (this.name != null)) {
               break;
            }
         }
      }

      if (StringUtils.isEmpty(this.sememeUsageDescription)) {
         throw new RuntimeException(
             "The Assemblage concept: " + assemblageConcept +
             " is not correctly assembled for use as an Assemblage for " +
             "a DynamicSememeData Refex Type.  It must contain a description of type Definition with an annotation of type " +
             "DynamicSememe.DYNAMIC_SEMEME_DEFINITION_DESCRIPTION");
      }

      Get.assemblageService()
         .getSemanticChronologyStreamForComponent(assemblageConcept.getNid())
         .forEach(sememe -> {
                     if (sememe.getVersionType() == VersionType.DYNAMIC) {
                        @SuppressWarnings("rawtypes")
                        final LatestVersion<? extends DynamicVersion> sememeVersion =
                           ((SemanticChronology) sememe).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly());

                        if (sememeVersion.isPresent()) {
                           @SuppressWarnings("rawtypes")
                           final DynamicVersion       ds                  = sememeVersion.get();
                           final DynamicData[] refexDefinitionData = ds.getData();

                           if (sememe.getAssemblageNid()==
                               DynamicConstants.get().DYNAMIC_EXTENSION_DEFINITION.getNid()) {
                              if ((refexDefinitionData == null) ||
                                  (refexDefinitionData.length < 3) ||
                                  (refexDefinitionData.length > 7)) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSememeData Refex Type.  It must contain at least 3 columns in the DynamicSememeDataBI attachment, and no more than 7.");
                              }

                              // col 0 is the column number,
                              // col 1 is the concept with col name
                              // col 2 is the column data type, stored as a string.
                              // col 3 (if present) is the default column data, stored as a subtype of DynamicData
                              // col 4 (if present) is a boolean field noting whether the column is required (true) or optional (false or null)
                              // col 5 (if present) is the validator {@link DynamicValidatorType}, stored as a string array.
                              // col 6 (if present) is the validatorData for the validator in column 5, stored as a subtype of DynamicData
                              try {
                                 final int  column          = (Integer) refexDefinitionData[0].getDataObject();
                                 final UUID descriptionUUID = (UUID) refexDefinitionData[1].getDataObject();
                                 final DynamicDataType type =
                                    DynamicDataType.valueOf((String) refexDefinitionData[2].getDataObject());
                                 DynamicData defaultData = null;

                                 if (refexDefinitionData.length > 3) {
                                    defaultData = ((refexDefinitionData[3] == null) ? null
                              : refexDefinitionData[3]);
                                 }

                                 if ((defaultData != null) &&
                                     (type.getDynamicMemberClass() !=
                                     refexDefinitionData[3].getDynamicDataType().getDynamicMemberClass())) {
                                    throw new IOException("The Assemblage concept: " + assemblageConcept +
                                    " is not correctly assembled for use as an Assemblage for " +
                                       "a DynamicSememeData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
                                 }

                                 Boolean columnRequired = null;

                                 if (refexDefinitionData.length > 4) {
                                    columnRequired = ((refexDefinitionData[4] == null) ? null
                              : (Boolean) refexDefinitionData[4].getDataObject());
                                 }

                                 DynamicValidatorType[] validators     = null;
                                 DynamicData[]          validatorsData = null;

                                 if (refexDefinitionData.length > 5) {
                                    if ((refexDefinitionData[5] != null) &&
                                        ((DynamicArray<DynamicString>) refexDefinitionData[5]).getDataArray().length >
                                        0) {
                                       final DynamicArray<DynamicString> readValidators =
                                          (DynamicArray<DynamicString>) refexDefinitionData[5];

                                       validators =
                                          new DynamicValidatorType[readValidators.getDataArray().length];

                                       for (int i = 0; i < validators.length; i++) {
                                          validators[i] = DynamicValidatorType.valueOf(
                                              (String) readValidators.getDataArray()[i]
                                                    .getDataObject());
                                       }
                                    }

                                    if (refexDefinitionData.length > 6) {
                                       if ((refexDefinitionData[6] != null) &&
                                           ((DynamicArray<? extends DynamicData>) refexDefinitionData[6]).getDataArray().length >
                                           0) {
                                          final DynamicArray<? extends DynamicData> readValidatorsData =
                                             (DynamicArray<? extends DynamicData>) refexDefinitionData[6];

                                          validatorsData =
                                             new DynamicData[readValidatorsData.getDataArray().length];

                                          if (validators != null) {
                                             for (int i = 0; i < validators.length; i++) {
                                                if (readValidatorsData.getDataArray()[i] != null) {
                                                   validatorsData[i] = readValidatorsData.getDataArray()[i];
                                                } else {
                                                   validatorsData[i] = null;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }

                                 allowedColumnInfo.put(column,
                                                       new DynamicColumnInfo(
                                                       assemblageConcept.getPrimordialUuid(),
                                                       column,
                                                       descriptionUUID,
                                                       type,
                                                       defaultData,
                                                       columnRequired,
                                                       validators,
                                                       validatorsData,
                                                       null));
                              } catch (final Exception e) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSememeData Refex Type.  The first column must have a data type of integer, and the third column must be a string " +
                                 "that is parseable as a DynamicSememeDataType");
                              }
                           } else if (sememe.getAssemblageNid() ==
                           DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getNid()) {
                              if ((refexDefinitionData == null) || (refexDefinitionData.length < 1)) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSememeData Refex Type.  If it contains a " +
                                 DynamicConstants.get().DYNAMIC_REFERENCED_COMPONENT_RESTRICTION.getFullyQualifiedName() +
                                 " then it must contain a single column of data, of type string, parseable as a " +
                                 IsaacObjectType.class.getName());
                              }

                              // col 0 is Referenced component restriction information - as a string.
                              try {
                                 final IsaacObjectType type =
                                       IsaacObjectType.parse(refexDefinitionData[0].getDataObject()
                                                                                     .toString(),
                                                               false);

                                 if (type == IsaacObjectType.UNKNOWN) {
                                    // just ignore - it shouldn't have been saved that way anyway.
                                 } else {
                                    this.referencedComponentTypeRestriction = type;
                                 }
                              } catch (final Exception e) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSememeData Refex Type.  The component type restriction annotation has an invalid value");
                              }

                              // col 1 is an optional Referenced component sub-restriction information - as a string.
                              if ((refexDefinitionData.length > 1) && (refexDefinitionData[1] != null)) {
                                 try {
                                    final VersionType type = VersionType.parse(refexDefinitionData[1].getDataObject()
                                                                                                   .toString(),
                                                                             false);

                                    if (type == VersionType.UNKNOWN) {
                                       // just ignore - it shouldn't have been saved that way anyway.
                                    } else {
                                       this.referencedComponentTypeSubRestriction = type;
                                    }
                                 } catch (final Exception e) {
                                    throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                    " is not correctly assembled for use as an Assemblage for " +
                                    "a DynamicSememeData Refex Type.  The component type restriction annotation has an invalid value");
                                 }
                              } else {
                                 this.referencedComponentTypeSubRestriction = null;
                              }
                           }
                        }
                     }
                  });
      this.refexColumnInfo = new DynamicColumnInfo[allowedColumnInfo.size()];

      int i = 0;

      for (final int key: allowedColumnInfo.keySet()) {
         if (key != i) {
            throw new RuntimeException(
                "The Assemblage concept: " + assemblageConcept +
                " is not correctly assembled for use as an Assemblage for " +
                "a DynamicSememeData Refex Type.  It must contain sequential column numbers, with no gaps, which start at 0.");
         }

         this.refexColumnInfo[i++] = allowedColumnInfo.get(key);
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final DynamicUsageDescriptionImpl other = (DynamicUsageDescriptionImpl) obj;

      if (this.refexUsageDescriptorNid != other.refexUsageDescriptorNid) {
         return false;
      }

      return true;
   }

   /**
    * Hash code.
    *
    * @return the int
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + this.refexUsageDescriptorNid;
      return result;
   }

   /**
    * Invent DynamicUsageDescription info for other sememe types (that
 aren't dynamic), otherwise, calls {@link #read(int)} if it is a dynamic
    * sememe.
    *
    * @param sememe the sememe in question
    * @return the dynamic element usage description
    */
   public static DynamicUsageDescription mockOrRead(SemanticChronology sememe) {
      final DynamicUsageDescriptionImpl dsud = new DynamicUsageDescriptionImpl();

      dsud.name                                  = Get.conceptDescriptionText(sememe.getAssemblageNid());
      dsud.referencedComponentTypeRestriction    = null;
      dsud.referencedComponentTypeSubRestriction = null;
      dsud.refexUsageDescriptorNid          = sememe.getAssemblageNid();
      dsud.sememeUsageDescription                = "-";

      switch (sememe.getVersionType()) {
      case COMPONENT_NID:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(sememe.getAssemblageNid()),
                0,
                DynamicConstants.get().DYNAMIC_DT_NID.getPrimordialUuid(),
                DynamicDataType.NID,
                null,
                true,
                null,
                null,
                false) };
         break;

      case LONG:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(sememe.getAssemblageNid()),
                0,
                DynamicConstants.get().DYNAMIC_DT_LONG.getPrimordialUuid(),
                DynamicDataType.LONG,
                null,
                true,
                null,
                null,
                false) };
         break;

      case DESCRIPTION:
      case STRING:
      case LOGIC_GRAPH:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {
            new DynamicColumnInfo(
                Get.identifierService().getUuidPrimordialForNid(sememe.getAssemblageNid()),
                0,
                DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(),
                DynamicDataType.STRING,
                null,
                true,
                null,
                null,
                false) };
         break;

      case MEMBER:
         dsud.refexColumnInfo = new DynamicColumnInfo[] {};
         break;

      case DYNAMIC:
         return read(sememe.getAssemblageNid());

      case UNKNOWN:
      default:
         throw new RuntimeException("Use case not yet supported");
      }

      return dsud;
   }

   /**
    * Read.
    *
    * @param assemblageNid the assemblage nid or sequence
    * @return the dynamic element usage description
    */
   public static DynamicUsageDescription read(int assemblageNid) {
      // TODO (artf231860) [REFEX] maybe? implement a mechanism to allow the cache to be updated... for now
      // cache is uneditable, and may be wrong, if the user changes the definition of a dynamic element.  Perhaps
      // implement a callback to clear the cache when we know a change of  a certain type happened instead?
      DynamicUsageDescriptionImpl temp     = cache.getIfPresent(assemblageNid);

      if (temp == null) {
         logger.log(Level.FINEST, "Cache miss on DynamicSememeUsageDescription Cache");
         temp = new DynamicUsageDescriptionImpl(assemblageNid);
         cache.put(assemblageNid, temp);
      }

      return temp;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the column info.
    *
    * @return the column info
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getColumnInfo()
    */
   @Override
   public DynamicColumnInfo[] getColumnInfo() {
      if (this.refexColumnInfo == null) {
         this.refexColumnInfo = new DynamicColumnInfo[] {};
      }

      return this.refexColumnInfo;
   }

   /**
    * Test if dyn sememe.
    *
    * @param assemblageNid the assemblage nid 
    * @return true, if dynamic element
    */
   public static boolean isDynamicSemantic(int assemblageNid) {
         try {
            read(assemblageNid);
            return true;
         } catch (final Exception e) {
            return false;
         }
   }

   /**
    * Gets the dynamic element name.
    *
    * @return the dynamic element name
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getDyanmicSememeName()
    */
   @Override
   public String getDynamicName() {
      return this.name;
   }

   /**
    * Gets the dynamic element usage description.
    *
    * @return the dynamic element usage description
    */

   /*
    *     @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getDynamicUsageDescription()
    */
   @Override
   public String getDynamicUsageDescription() {
      return this.sememeUsageDescription;
   }

   /**
    * Gets the dynamic element usage descriptor sequence.
    *
    * @return the dynamic element usage descriptor sequence
    */

   /*
    *     @see gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getDynamicUsageDescriptorSequence()
    */
   @Override
   public int getDynamicUsageDescriptorNid() {
      return this.refexUsageDescriptorNid;
   }

   /**
    * Gets the referenced component type restriction.
    *
    * @return the referenced component type restriction
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getReferencedComponentTypeRestriction()
    */
   @Override
   public IsaacObjectType getReferencedComponentTypeRestriction() {
      return this.referencedComponentTypeRestriction;
   }

   /**
    * Gets the referenced component type sub restriction.
    *
    * @return the referenced component type sub restriction
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicUsageDescription#getReferencedComponentTypeSubRestriction()
    */
   @Override
   public VersionType getReferencedComponentTypeSubRestriction() {
      return this.referencedComponentTypeSubRestriction;
   }
   
   /**
    * Invent DynamicSememeUsageDescription info for static sememes that are marked as type {@link TermAux#IDENTIFIER_SOURCE}
    */
   public static DynamicUsageDescription mockIdentifierType(int identifierAssemblageConceptId) {
       DynamicUsageDescriptionImpl dsud = new DynamicUsageDescriptionImpl();
       dsud.name = Get.conceptDescriptionText(identifierAssemblageConceptId);
       dsud.referencedComponentTypeRestriction = null;
       dsud.referencedComponentTypeSubRestriction = null;
       dsud.refexUsageDescriptorNid = identifierAssemblageConceptId;
       dsud.sememeUsageDescription = "-";
       dsud.refexColumnInfo = new DynamicColumnInfo[]{new DynamicColumnInfo(
           Get.identifierService().getUuidPrimordialForNid(identifierAssemblageConceptId),
           0, DynamicConstants.get().DYNAMIC_DT_STRING.getPrimordialUuid(), DynamicDataType.STRING, null, true, null, null, false)};
        return dsud;
   }
}

