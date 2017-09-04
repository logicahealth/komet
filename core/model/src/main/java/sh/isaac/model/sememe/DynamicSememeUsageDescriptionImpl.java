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



package sh.isaac.model.sememe;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.collections.LruCache;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.constants.DynamicSememeConstants;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.api.component.sememe.version.DescriptionVersion;

//~--- classes ----------------------------------------------------------------

/**
 * See {@link DynamicSememeUsageDescription}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeUsageDescriptionImpl
         implements DynamicSememeUsageDescription {
   /** The Constant logger. */
   protected static final Logger logger = Logger.getLogger(DynamicSememeUsageDescription.class.getName());

   /** The cache. */
   private static LruCache<Integer, DynamicSememeUsageDescriptionImpl> cache =
      new LruCache<Integer, DynamicSememeUsageDescriptionImpl>(25);

   //~--- fields --------------------------------------------------------------

   /** The refex usage descriptor sequence. */
   int refexUsageDescriptorSequence;

   /** The sememe usage description. */
   String sememeUsageDescription;

   /** The name. */
   String name;

   /** The refex column info. */
   DynamicSememeColumnInfo[] refexColumnInfo;

   /** The referenced component type restriction. */
   ObjectChronologyType referencedComponentTypeRestriction;

   /** The referenced component type sub restriction. */
   SememeType referencedComponentTypeSubRestriction;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic sememe usage description impl.
    */
   private DynamicSememeUsageDescriptionImpl() {
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
   public DynamicSememeUsageDescriptionImpl(int refexUsageDescriptorId) {
      final ConceptChronology assemblageConcept = Get.conceptService()
                                                        .getConcept(refexUsageDescriptorId);

      this.refexUsageDescriptorSequence = assemblageConcept.getConceptSequence();

      final TreeMap<Integer, DynamicSememeColumnInfo> allowedColumnInfo = new TreeMap<>();

      for (final SememeChronology descriptionSememe:
            assemblageConcept.getConceptDescriptionList()) {
         @SuppressWarnings("rawtypes")
         final LatestVersion descriptionVersion =
            ((SememeChronology) descriptionSememe).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly());

         if (descriptionVersion.isPresent()) {
            @SuppressWarnings("rawtypes")
            final DescriptionVersion ds = (DescriptionVersion) descriptionVersion.get();

            if (ds.getDescriptionTypeConceptSequence() == TermAux.DEFINITION_DESCRIPTION_TYPE.getConceptSequence()) {
               final Optional<SememeChronology> nestesdSememe = Get.assemblageService()
                                                                                               .getSememesForComponentFromAssemblage(
                                                                                                  ds.getNid(),
                                                                                                        DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION
                                                                                                              .getSequence())
                                                                                               .findAny();

               if (nestesdSememe.isPresent()) {
                  this.sememeUsageDescription = ds.getText();
               }
               ;
            }

            if (ds.getDescriptionTypeConceptSequence() ==
                  TermAux.FULLY_SPECIFIED_DESCRIPTION_TYPE.getConceptSequence()) {
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
         .getSememesForComponent(assemblageConcept.getNid())
         .forEach(sememe -> {
                     if (sememe.getSememeType() == SememeType.DYNAMIC) {
                        @SuppressWarnings("rawtypes")
                        final LatestVersion<? extends DynamicSememe> sememeVersion =
                           ((SememeChronology) sememe).getLatestVersion(StampCoordinates.getDevelopmentLatestActiveOnly());

                        if (sememeVersion.isPresent()) {
                           @SuppressWarnings("rawtypes")
                           final DynamicSememe       ds                  = sememeVersion.get();
                           final DynamicSememeData[] refexDefinitionData = ds.getData();

                           if (sememe.getAssemblageSequence() ==
                               DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getSequence()) {
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
                              // col 3 (if present) is the default column data, stored as a subtype of DynamicSememeData
                              // col 4 (if present) is a boolean field noting whether the column is required (true) or optional (false or null)
                              // col 5 (if present) is the validator {@link DynamicSememeValidatorType}, stored as a string array.
                              // col 6 (if present) is the validatorData for the validator in column 5, stored as a subtype of DynamicSememeData
                              try {
                                 final int  column          = (Integer) refexDefinitionData[0].getDataObject();
                                 final UUID descriptionUUID = (UUID) refexDefinitionData[1].getDataObject();
                                 final DynamicSememeDataType type =
                                    DynamicSememeDataType.valueOf((String) refexDefinitionData[2].getDataObject());
                                 DynamicSememeData defaultData = null;

                                 if (refexDefinitionData.length > 3) {
                                    defaultData = ((refexDefinitionData[3] == null) ? null
                              : refexDefinitionData[3]);
                                 }

                                 if ((defaultData != null) &&
                                     (type.getDynamicSememeMemberClass() !=
                                     refexDefinitionData[3].getDynamicSememeDataType().getDynamicSememeMemberClass())) {
                                    throw new IOException("The Assemblage concept: " + assemblageConcept +
                                    " is not correctly assembled for use as an Assemblage for " +
                                       "a DynamicSememeData Refex Type.  The type of the column (column 3) must match the type of the defaultData (column 4)");
                                 }

                                 Boolean columnRequired = null;

                                 if (refexDefinitionData.length > 4) {
                                    columnRequired = ((refexDefinitionData[4] == null) ? null
                              : (Boolean) refexDefinitionData[4].getDataObject());
                                 }

                                 DynamicSememeValidatorType[] validators     = null;
                                 DynamicSememeData[]          validatorsData = null;

                                 if (refexDefinitionData.length > 5) {
                                    if ((refexDefinitionData[5] != null) &&
                                        ((DynamicSememeArray<DynamicSememeString>) refexDefinitionData[5]).getDataArray().length >
                                        0) {
                                       final DynamicSememeArray<DynamicSememeString> readValidators =
                                          (DynamicSememeArray<DynamicSememeString>) refexDefinitionData[5];

                                       validators =
                                          new DynamicSememeValidatorType[readValidators.getDataArray().length];

                                       for (int i = 0; i < validators.length; i++) {
                                          validators[i] = DynamicSememeValidatorType.valueOf(
                                              (String) readValidators.getDataArray()[i]
                                                    .getDataObject());
                                       }
                                    }

                                    if (refexDefinitionData.length > 6) {
                                       if ((refexDefinitionData[6] != null) &&
                                           ((DynamicSememeArray<? extends DynamicSememeData>) refexDefinitionData[6]).getDataArray().length >
                                           0) {
                                          final DynamicSememeArray<? extends DynamicSememeData> readValidatorsData =
                                             (DynamicSememeArray<? extends DynamicSememeData>) refexDefinitionData[6];

                                          validatorsData =
                                             new DynamicSememeData[readValidatorsData.getDataArray().length];

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
                                                       new DynamicSememeColumnInfo(
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
                           } else if (sememe.getAssemblageSequence() ==
                           DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getSequence()) {
                              if ((refexDefinitionData == null) || (refexDefinitionData.length < 1)) {
                                 throw new RuntimeException("The Assemblage concept: " + assemblageConcept +
                                 " is not correctly assembled for use as an Assemblage for " +
                                 "a DynamicSememeData Refex Type.  If it contains a " +
                                 DynamicSememeConstants.get().DYNAMIC_SEMEME_REFERENCED_COMPONENT_RESTRICTION.getPrimaryName() +
                                 " then it must contain a single column of data, of type string, parseable as a " +
                                 ObjectChronologyType.class.getName());
                              }

                              // col 0 is Referenced component restriction information - as a string.
                              try {
                                 final ObjectChronologyType type =
                                    ObjectChronologyType.parse(refexDefinitionData[0].getDataObject()
                                                                                     .toString(),
                                                               false);

                                 if (type == ObjectChronologyType.UNKNOWN_NID) {
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
                                    final SememeType type = SememeType.parse(refexDefinitionData[1].getDataObject()
                                                                                                   .toString(),
                                                                             false);

                                    if (type == SememeType.UNKNOWN) {
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
      this.refexColumnInfo = new DynamicSememeColumnInfo[allowedColumnInfo.size()];

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

      final DynamicSememeUsageDescriptionImpl other = (DynamicSememeUsageDescriptionImpl) obj;

      if (this.refexUsageDescriptorSequence != other.refexUsageDescriptorSequence) {
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

      result = prime * result + this.refexUsageDescriptorSequence;
      return result;
   }

   /**
    * Invent DynamicSememeUsageDescription info for other sememe types (that
    * aren't dynamic), otherwise, calls {@link #read(int)} if it is a dynamic
    * sememe.
    *
    * @param sememe the sememe in question
    * @return the dynamic sememe usage description
    */
   public static DynamicSememeUsageDescription mockOrRead(SememeChronology sememe) {
      final DynamicSememeUsageDescriptionImpl dsud = new DynamicSememeUsageDescriptionImpl();

      dsud.name                                  = Get.conceptDescriptionText(sememe.getAssemblageSequence());
      dsud.referencedComponentTypeRestriction    = null;
      dsud.referencedComponentTypeSubRestriction = null;
      dsud.refexUsageDescriptorSequence          = sememe.getAssemblageSequence();
      dsud.sememeUsageDescription                = "-";

      switch (sememe.getSememeType()) {
      case COMPONENT_NID:
         dsud.refexColumnInfo = new DynamicSememeColumnInfo[] {
            new DynamicSememeColumnInfo(
                Get.identifierService().getUuidPrimordialFromConceptId(sememe.getAssemblageSequence()).get(),
                0,
                DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_NID.getPrimordialUuid(),
                DynamicSememeDataType.NID,
                null,
                true,
                null,
                null,
                false) };
         break;

      case LONG:
         dsud.refexColumnInfo = new DynamicSememeColumnInfo[] {
            new DynamicSememeColumnInfo(
                Get.identifierService().getUuidPrimordialFromConceptId(sememe.getAssemblageSequence()).get(),
                0,
                DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_LONG.getPrimordialUuid(),
                DynamicSememeDataType.LONG,
                null,
                true,
                null,
                null,
                false) };
         break;

      case DESCRIPTION:
      case STRING:
      case LOGIC_GRAPH:
         dsud.refexColumnInfo = new DynamicSememeColumnInfo[] {
            new DynamicSememeColumnInfo(
                Get.identifierService().getUuidPrimordialFromConceptId(sememe.getAssemblageSequence()).get(),
                0,
                DynamicSememeConstants.get().DYNAMIC_SEMEME_DT_STRING.getPrimordialUuid(),
                DynamicSememeDataType.STRING,
                null,
                true,
                null,
                null,
                false) };
         break;

      case MEMBER:
         dsud.refexColumnInfo = new DynamicSememeColumnInfo[] {};
         break;

      case DYNAMIC:
         return read(sememe.getAssemblageSequence());

      case UNKNOWN:
      default:
         throw new RuntimeException("Use case not yet supported");
      }

      return dsud;
   }

   /**
    * Read.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic sememe usage description
    */
   public static DynamicSememeUsageDescription read(int assemblageNidOrSequence) {
      // TODO (artf231860) [REFEX] maybe? implement a mechanism to allow the cache to be updated... for now
      // cache is uneditable, and may be wrong, if the user changes the definition of a dynamic sememe.  Perhaps
      // implement a callback to clear the cache when we know a change of  a certain type happened instead?
      final int                         sequence = Get.identifierService()
                                                      .getConceptSequence(assemblageNidOrSequence);
      DynamicSememeUsageDescriptionImpl temp     = cache.get(sequence);

      if (temp == null) {
         logger.log(Level.FINEST, "Cache miss on DynamicSememeUsageDescription Cache");
         temp = new DynamicSememeUsageDescriptionImpl(sequence);
         cache.put(sequence, temp);
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
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription#getColumnInfo()
    */
   @Override
   public DynamicSememeColumnInfo[] getColumnInfo() {
      if (this.refexColumnInfo == null) {
         this.refexColumnInfo = new DynamicSememeColumnInfo[] {};
      }

      return this.refexColumnInfo;
   }

   /**
    * Test if dyn sememe.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return true, if dynamic sememe
    */
   public static boolean isDynamicSememe(int assemblageNidOrSequence) {
      if ((assemblageNidOrSequence >= 0) ||
            (Get.identifierService().getChronologyTypeForNid(assemblageNidOrSequence) ==
             ObjectChronologyType.CONCEPT)) {
         try {
            read(assemblageNidOrSequence);
            return true;
         } catch (final Exception e) {
            return false;
         }
      } else {
         return false;
      }
   }

   /**
    * Gets the dynamic sememe name.
    *
    * @return the dynamic sememe name
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription#getDyanmicSememeName()
    */
   @Override
   public String getDynamicSememeName() {
      return this.name;
   }

   /**
    * Gets the dynamic sememe usage description.
    *
    * @return the dynamic sememe usage description
    */

   /*
    *     @see DynamicSememeUsageDescription#getDynamicSememeUsageDescription()
    */
   @Override
   public String getDynamicSememeUsageDescription() {
      return this.sememeUsageDescription;
   }

   /**
    * Gets the dynamic sememe usage descriptor sequence.
    *
    * @return the dynamic sememe usage descriptor sequence
    */

   /*
    *     @see DynamicSememeUsageDescription#getDynamicSememeUsageDescriptorSequence()
    */
   @Override
   public int getDynamicSememeUsageDescriptorSequence() {
      return this.refexUsageDescriptorSequence;
   }

   /**
    * Gets the referenced component type restriction.
    *
    * @return the referenced component type restriction
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription#getReferencedComponentTypeRestriction()
    */
   @Override
   public ObjectChronologyType getReferencedComponentTypeRestriction() {
      return this.referencedComponentTypeRestriction;
   }

   /**
    * Gets the referenced component type sub restriction.
    *
    * @return the referenced component type sub restriction
    */

   /*
    *     @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription#getReferencedComponentTypeSubRestriction()
    */
   @Override
   public SememeType getReferencedComponentTypeSubRestriction() {
      return this.referencedComponentTypeSubRestriction;
   }
}

