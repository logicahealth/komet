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



package sh.isaac.api.component.semantic.version.dynamic;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;

//~--- non-JDK imports --------------------------------------------------------

/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link DynamicUtility}
 *
 * This class exists as an interface primarily to allow classes in ochre-api and ochre-impl to have access to these methods
 * that need to be implemented further down the dependency tree (with access to metadata, etc)
 *
 *  Code in ochre-util and ochre-api will access the impl via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DynamicUtility {
   /**
    * This will return the column index configuration that will mark each supplied column that is indexable, for indexing.
    * Returns null, if no columns need indexing.
    *
    * @param columns the columns
    * @return the dynamic array
    */
   public DynamicArray<DynamicData> configureColumnIndexInfo(DynamicColumnInfo[] columns);

   /**
    * Configure dynamic definition data for column.
    *
    * @param ci the ci
    * @return the dynamic data[]
    */
   public DynamicData[] configureDynamicDefinitionDataForColumn(DynamicColumnInfo ci);

   /**
    * Configure dynamic restriction data.
    *
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @return the dynamic data[]
    */
   public DynamicData[] configureDynamicRestrictionData(IsaacObjectType referencedComponentRestriction,
         VersionType referencedComponentSubRestriction);

   /**
    * Creates the dynamic string data.
    *
    * @param value the value
    * @return the dynamic string
    */
   public DynamicString createDynamicStringData(String value);

   /**
    * Creates the dynamic UUID data.
    *
    * @param value the value
    * @return the dynamic UUID
    */
   public DynamicUUID createDynamicUUIDData(UUID value);
   
   /**
    * Creates the dynamic Array String data.
    *
    * @param value the value
    * @return the dynamic Array
    */
   public DynamicArray<DynamicString> createDynamicStringArrayData(String ... values);

   /**
    * Convenience method to read all of the extended details of a DynamicAssemblage.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic usage description
    */
   public DynamicUsageDescription readDynamicUsageDescription(int assemblageNidOrSequence);
   
   /**
    * Add all of the necessary metadata semantics onto the specified concept to make it a concept that defines a dynamic semantic assemblage
    * See {@link DynamicUsageDescription} class for more details on this format.
    * @param conceptNid - The concept that will define a dynamic semantic
    * @param semanticDescription - The description that describes the purpose of this dynamic semantic
    * @param columns - optional - the columns of data that this dynamic semantic needs to be able to store.
    * @param referencedComponentTypeRestriction - optional - any component type restriction info for the columns
    * @param referencedComponentTypeSubRestriction - optional - any compont sub-type restrictions for the columns
    * @param editCoord - optional - the edit coordinate to construct this on - if null, uses the system default coordinate
    * @return all of the created (but uncommitted) SemanticChronologies
    */
   public SemanticChronology[] configureConceptAsDynamicSemantic(int conceptNid, String semanticDescription, DynamicColumnInfo[] columns,
         IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, EditCoordinate editCoord);
   
   /**
    * Add all of the necessary metadata semantics onto the specified concept to make it a concept that defines a dynamic semantic assemblage
    * See {@link DynamicUsageDescription} class for more details on this format.
    * @param conceptNid - The concept that will define a dynamic semantic
    * @param semanticDescription - The description that describes the purpose of this dynamic semantic
    * @param columns - optional - the columns of data that this dynamic semantic needs to be able to store.
    * @param referencedComponentTypeRestriction - optional - any component type restriction info for the columns
    * @param referencedComponentTypeSubRestriction - optional - any compont sub-type restrictions for the columns
    * @param stampSequence - the stamp to construct this on
    * @return all of the created (but unwritten) SemanticChronologies.  It is up to the caller to write the chronologies to the appropriate store.
    */
   public List<Chronology> configureConceptAsDynamicSemantic(int conceptNid, String semanticDescription, DynamicColumnInfo[] columns,
         IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, int stampSequence);
   
   /**
    * Create a new concept to be used in a column of a dynamic semantic definition
    * @param columnName - the FSN and regular name of the concept
    * @param columnDescription - the optional but highly recommended description of the column
    * @param editCoordinate - optional - uses default if not provided
    * @param extraParents - optional - by default, listed under {@link DynamicConstants#DYNAMIC_COLUMNS}
    * @return the list of chronology objects created but not committed
    */
   public ArrayList<Chronology> buildUncommittedNewDynamicSemanticColumnInfoConcept(String columnName, String columnDescription, 
            EditCoordinate editCoordinate, UUID[] extraParents);

   /**
    * validate that the proposed dynamicData aligns with the definition.  This also fills in default values,
    * as necessary, if the data[] contains 'nulls' and the column is specified with a default value.
    *
    * @param dsud the dsud
    * @param data the data
    * @param referencedComponentNid the referenced component nid
    * @param referencedComponentVersionType - optional - there are some build sequences where we can't look up the version type here, it must be
    * passed in
    * @param stampSequence the stamp sequence of this data
    * @throws IllegalArgumentException the illegal argument exception
    * @throws InvalidParameterException - if anything fails validation
    */
   public default void validate(DynamicUsageDescription dsud,
                                DynamicData[] data,
                                int referencedComponentNid,
                                VersionType referencedComponentVersionType,
                                int stampSequence)
            throws IllegalArgumentException {
      // Make sure the referenced component meets the ref component restrictions, if any are present.
      if ((dsud.getReferencedComponentTypeRestriction() != null) &&
            (dsud.getReferencedComponentTypeRestriction() != IsaacObjectType.UNKNOWN)) {
         final IsaacObjectType requiredType = dsud.getReferencedComponentTypeRestriction();
         final IsaacObjectType foundType = Get.identifierService().getObjectTypeForComponent(referencedComponentNid);

         if (requiredType != foundType) {
            throw new IllegalArgumentException("The referenced component must be of type " + requiredType +
                                               ", but a " + foundType + " was passed");
         }

         if ((requiredType == IsaacObjectType.SEMANTIC) &&
               (dsud.getReferencedComponentTypeSubRestriction() != null) &&
               (dsud.getReferencedComponentTypeSubRestriction() != VersionType.UNKNOWN)) {
            final VersionType requiredSemanticType = dsud.getReferencedComponentTypeSubRestriction();
            final VersionType foundSemanticType    = referencedComponentVersionType == null ? Get.assemblageService()
                                                     .getSemanticChronology(referencedComponentNid)
                                                     .getVersionType() : referencedComponentVersionType;

            if (requiredSemanticType != foundSemanticType) {
               throw new IllegalArgumentException("The referenced component must be of type " +
                                                  requiredSemanticType + ", but a " + foundSemanticType + " was passed");
            }
         }
      }

      if (data == null) {
         data = new DynamicData[] {};
      }

      // specifically allow < - we don't need the trailing columns, if they were defined as optional.
      if (data.length > dsud.getColumnInfo().length) {
         throw new IllegalArgumentException(
             "The Assemblage concept: " + dsud.getDynamicName() + " specifies " + dsud.getColumnInfo().length +
             " columns of data, while the provided data contains " + data.length +
             " columns.  The data size array must not exeed the definition." +
             " (the data column count may be less, if the missing columns are defined as optional)");
      }

      int lastColumnWithDefaultValue = 0;

      for (int i = 0; i < dsud.getColumnInfo().length; i++) {
         if (dsud.getColumnInfo()[i]
                 .getDefaultColumnValue() != null) {
            lastColumnWithDefaultValue = i;
         }
      }

      if (lastColumnWithDefaultValue + 1 > data.length) {
         // We need to lengthen the data array, to make room to add the default value
         data = Arrays.copyOf(data, lastColumnWithDefaultValue);
      }

      for (int i = 0; i < dsud.getColumnInfo().length; i++) {
         final DynamicData defaultValue = dsud.getColumnInfo()[i]
                                                    .getDefaultColumnValue();

         if ((defaultValue != null) && (data[i] == null)) {
            data[i] = defaultValue;
         }
      }

      // If they provided less columns, make sure the remaining columns are all optional
      for (int i = data.length; i < dsud.getColumnInfo().length; i++) {
         if (dsud.getColumnInfo()[i]
                 .isColumnRequired()) {
            throw new IllegalArgumentException("No data was supplied for column '" +
                                               dsud.getColumnInfo()[i].getColumnName() + "' [" + (i + 1) + "(index " +
                                               i + ")] but the column is specified as a required column");
         }
      }

      for (int dataColumn = 0; dataColumn < data.length; dataColumn++) {
         final DynamicColumnInfo dsci = dsud.getColumnInfo()[dataColumn];

         if (data[dataColumn] == null) {
            if (dsci.isColumnRequired()) {
               throw new IllegalArgumentException("No data was supplied for column " + (dataColumn + 1) +
                                                  " but the column is specified as a required column");
            }
         } else {
            final DynamicDataType allowedDT = dsci.getColumnDataType();

            if ((data[dataColumn] != null) &&
                  (allowedDT != DynamicDataType.POLYMORPHIC) &&
                  (data[dataColumn].getDynamicDataType() != allowedDT)) {
               throw new IllegalArgumentException("The supplied data for column " + dataColumn + " is of type " +
                                                  data[dataColumn].getDynamicDataType() +
                                                  " but the assemblage concept declares that it must be " + allowedDT);
            }

            if ((dsci.getValidator() != null) && (dsci.getValidator().length > 0)) {
               try {
                  for (int i = 0; i < dsci.getValidator().length; i++) {
                     boolean rethrow = false;

                     try {
                        if (!dsci.getValidator()[i]
                                 .passesValidator(data[dataColumn],
                                                  dsci.getValidatorData()[i],
                                                  stampSequence)) {
                           rethrow = true;
                           throw new IllegalArgumentException(
                               "The supplied data for column " + dataColumn +
                               " does not pass the assigned validator(s) for this dynamic field.  Data: " +
                               data[dataColumn].dataToString() + " Validator: " + dsci.getValidator()[i].name() +
                               " Validator Data: " + dsci.getValidatorData()[i].dataToString() + " Semantic: " + dsud.getDynamicName()
                               + " Referenced Component " 
                               + Get.identifiedObjectService().getChronology(referencedComponentNid).get().toUserString());
                        }
                     } catch (final IllegalArgumentException e) {
                        if (rethrow) {
                           throw e;
                        } else {
                           LogManager.getLogger()
                                        .debug("Couldn't execute validator due to missing coordiantes");
                        }
                     }
                  }
               } catch (final IllegalArgumentException e) {
                  throw e;
               } catch (final RuntimeException e) {
                  throw new IllegalArgumentException("Validator Failure: ", e);
               }
            }
         }
      }
   }
}

