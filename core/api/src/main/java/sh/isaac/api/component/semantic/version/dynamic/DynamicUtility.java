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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 * {@link DynamicUtility}
 *
 * This class exists as an interface primarily to allow classes in api and impl to have access to these methods
 * that need to be implemented further down the dependency tree (with access to metadata, etc)
 *
 *  Code in util and api will access the impl via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DynamicUtility {

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
    * @param values the values
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
    * NOTE!!! If this concept lives outside of the metadata tree, in order to get the proper markers into the lucene indes on ALL of the descriptions 
    * for this concept, once should reindex all of the descriptions attached to the passed in conceptNid, after writing the results of this call to the 
    * system.
    * 
    * Add all of the necessary metadata semantics onto the specified concept to make it a concept that defines a dynamic semantic assemblage
    * See {@link DynamicUsageDescription} class for more details on this format.
    * @param wc - optional - the edit coordinate to construct this on - if null, uses the system default coordinate
    * @param conceptNid - The concept that will define a dynamic semantic
    * @param semanticDescription - The description that describes the purpose of this dynamic semantic
    * @param columns - optional - the columns of data that this dynamic semantic needs to be able to store.
    * @param referencedComponentTypeRestriction - optional - any component type restriction info for the columns
    * @param referencedComponentTypeSubRestriction - optional - any component sub-type restrictions for the columns
    * @param write - if true, write immediately.  If false, caller is responsible for writing the chronologies to the data store.
    * @return all of the created (but uncommitted) SemanticChronologies
    */
   public SemanticChronology[] configureConceptAsDynamicSemantic(WriteCoordinate wc, int conceptNid, String semanticDescription, DynamicColumnInfo[] columns,
                                                                 IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, 
                                                                 boolean write);
   
   /**
    * Create a new concept to be used in a column of a dynamic semantic definition
    * @param wc - optional write coordinate to create on.  Uses system default if not provided
    * @param columnName - the FSN and regular name of the concept
    * @param columnDescription - the optional but highly recommended description of the column
    * @param extraParents - optional - by default, listed under {@link DynamicConstants#DYNAMIC_COLUMNS}
    * @return the list of chronology objects created but not committed
    */
   public ArrayList<Chronology> buildUncommittedNewDynamicSemanticColumnInfoConcept(WriteCoordinate wc, String columnName, String columnDescription, UUID[] extraParents);

   /**
    * validate that the proposed dynamicData aligns with the definition.  This also fills in default values,
    * as necessary, if the data[] contains 'nulls' and the column is specified with a default value.
    *
    * @param dynamicUsageDescriptionSupplier a function to supply the appropriate DynamicUsageDescription
    * @param userData the data
    * @param referencedComponentNid the referenced component nid
    * @param referencedComponentVersionType - optional - there are some build sequences where we can't look up the version type here, it must be
    * passed in
    * @param stampSequence the stamp sequence of this data
    * @param delayValidation return functions of certain validations that may not be able to execute now, due to out-of-order data loading.
    * @return If any validations could not be executed at this time, due to a a DB Build mode, for example, return those validations wrapped
    *     in functions.  The caller should execute those functions when their load process is complete. 
    * @throws IllegalArgumentException the illegal argument exception
    * @throws InvalidParameterException - if anything fails validation
    */
   public default List<BooleanSupplier> validate(Supplier<DynamicUsageDescription> dynamicUsageDescriptionSupplier,
                                DynamicData[] userData,
                                int referencedComponentNid,
                                VersionType referencedComponentVersionType,
                                int stampSequence,
                                boolean delayValidation)
            throws IllegalArgumentException {
       /*
        * Make sure the referenced component meets the ref component restrictions, if any are present.
        * If we are loading things out of order, this validation could fail due to the referenced component not yet being loaded.
        */
       ArrayList<BooleanSupplier> delayedValidators = new ArrayList<>();
         final DynamicUsageDescription dsud = dynamicUsageDescriptionSupplier.get();
         if ((dsud.getReferencedComponentTypeRestriction() != null) &&
               (dsud.getReferencedComponentTypeRestriction() != IsaacObjectType.UNKNOWN)) {

         BooleanSupplier refComponentValidator = () -> {
            final IsaacObjectType requiredType = dsud.getReferencedComponentTypeRestriction();
            final IsaacObjectType foundType = Get.identifierService().getObjectTypeForComponent(referencedComponentNid);
   
            if (requiredType != foundType)
            {
               throw new IllegalArgumentException("The referenced component must be of type " + requiredType + ", but a " + foundType + " was passed");
            }
   
            if ((requiredType == IsaacObjectType.SEMANTIC) && (dsud.getReferencedComponentTypeSubRestriction() != null)
                  && (dsud.getReferencedComponentTypeSubRestriction() != VersionType.UNKNOWN))
            {
               final VersionType requiredSemanticType = dsud.getReferencedComponentTypeSubRestriction();
               final VersionType foundSemanticType = referencedComponentVersionType == null
                     ? Get.assemblageService().getSemanticChronology(referencedComponentNid).getVersionType()
                     : referencedComponentVersionType;
   
               if (requiredSemanticType != foundSemanticType)
               {
                  throw new IllegalArgumentException(
                        "The referenced component must be of type " + requiredSemanticType + ", but a " + foundSemanticType + " was passed");
               }
            }
            return true;
         };
         if (delayValidation) {
            delayedValidators.add(refComponentValidator);
         }
         else
         {
            refComponentValidator.getAsBoolean();
         }
      }
         
      int lastColumnWithDefaultValue = 0;

      for (int i = 0; i < dsud.getColumnInfo().length; i++) {
         if (dsud.getColumnInfo()[i].getDefaultColumnValue() != null) {
            lastColumnWithDefaultValue = i;
         }
      }

      DynamicData[] tempData = userData == null ? new DynamicData[0] : userData;
      
      // We may need to lengthen the data array, to make room to add the default value
      
      final DynamicData[] data = (lastColumnWithDefaultValue + 1 > tempData.length) ? 
            Arrays.copyOf(tempData, lastColumnWithDefaultValue) : tempData;
      
      tempData = null;

      // specifically allow < - we don't need the trailing columns, if they were defined as optional.
      if (data.length > dsud.getColumnInfo().length) {
         throw new IllegalArgumentException(
             "The Assemblage concept: " + dsud.getDynamicName() + " specifies " + dsud.getColumnInfo().length +
             " columns of data, while the provided data contains " + data.length +
             " " + Arrays.toString(data) +
             " columns.  The data size array must not exeed the definition." +
             " (the data column count may be less, if the missing columns are defined as optional)");
      }

      if (lastColumnWithDefaultValue + 1 > data.length) {
         
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
         final int dataColumnFinal = dataColumn;

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
               BiFunction<DynamicValidatorType, DynamicData, Boolean> colInfoValidator = (dynamicValidatorType, dynamicData) -> {
                  if (!dynamicValidatorType.passesValidator(data[dataColumnFinal], dynamicData, stampSequence)) {
                      throw new IllegalArgumentException(
                          "The supplied data for column " + dataColumnFinal +
                          " does not pass the assigned validator(s) for this dynamic field.  Data: " +
                          data[dataColumnFinal].dataToString() + " Validator: " + dynamicValidatorType.name() +
                          " Validator Data: " + dynamicData.dataToString() + " Semantic: " + dsud.getDynamicName()
                          + " Referenced Component " 
                          + Get.identifiedObjectService().getChronology(referencedComponentNid).get().toUserString());
                  }
                  return true;
               };
               
               try {
                  for (int i = 0; i < dsci.getValidator().length; i++) {
                     final int valFinal = i;
                     if (delayValidation) {
                        delayedValidators.add(() -> colInfoValidator.apply(dsci.getValidator()[valFinal], dsci.getValidatorData()[valFinal]));
                     }
                     else {
                        colInfoValidator.apply(dsci.getValidator()[valFinal], dsci.getValidatorData()[valFinal]);
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
      return delayedValidators;
   }
}