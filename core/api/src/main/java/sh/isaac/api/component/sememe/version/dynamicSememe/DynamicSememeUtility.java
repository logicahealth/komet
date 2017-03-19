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



package sh.isaac.api.component.sememe.version.dynamicSememe;

//~--- JDK imports ------------------------------------------------------------

import java.security.InvalidParameterException;

import java.util.Arrays;
import java.util.UUID;

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

import org.slf4j.LoggerFactory;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.TaxonomyCoordinate;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link DynamicSememeUtility}
 *
 * This class exists as an interface primarily to allow classes in ochre-api and ochre-impl to have access to these methods
 * that need to be implemented further down the dependency tree (with access to metadata, etc)
 *
 *  Code in ochre-util and ochre-api will access the impl via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DynamicSememeUtility {
   
   /**
    * This will return the column index configuration that will mark each supplied column that is indexable, for indexing.
    * Returns null, if no columns need indexing.
    *
    * @param columns the columns
    * @return the dynamic sememe array
    */
   public DynamicSememeArray<DynamicSememeData> configureColumnIndexInfo(DynamicSememeColumnInfo[] columns);

   /**
    * Configure dynamic sememe definition data for column.
    *
    * @param ci the ci
    * @return the dynamic sememe data[]
    */
   public DynamicSememeData[] configureDynamicSememeDefinitionDataForColumn(DynamicSememeColumnInfo ci);

   /**
    * Configure dynamic sememe restriction data.
    *
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @return the dynamic sememe data[]
    */
   public DynamicSememeData[] configureDynamicSememeRestrictionData(ObjectChronologyType referencedComponentRestriction,
         SememeType referencedComponentSubRestriction);

   /**
    * Creates the dynamic string data.
    *
    * @param value the value
    * @return the dynamic sememe string
    */
   public DynamicSememeString createDynamicStringData(String value);

   /**
    * Creates the dynamic UUID data.
    *
    * @param value the value
    * @return the dynamic sememe UUID
    */
   public DynamicSememeUUID createDynamicUUIDData(UUID value);

   /**
    * Convenience method to read all of the extended details of a DynamicSememeAssemblage.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic sememe usage description
    */
   public DynamicSememeUsageDescription readDynamicSememeUsageDescription(int assemblageNidOrSequence);

   /**
    * validate that the proposed dynamicSememeData aligns with the definition.  This also fills in default values,
    * as necessary, if the data[] contains 'nulls' and the column is specified with a default value.
    *
    * @param dsud the dsud
    * @param data the data
    * @param referencedComponentNid the referenced component nid
    * @param stampCoordinate - optional - column specific validators may be skipped if this is not provided
    * @param taxonomyCoordinate - optional - column specific validators may be skipped if this is not provided
    * @throws IllegalArgumentException the illegal argument exception
    * @throws InvalidParameterException - if anything fails validation
    */
   public default void validate(DynamicSememeUsageDescription dsud,
                                DynamicSememeData[] data,
                                int referencedComponentNid,
                                StampCoordinate stampCoordinate,
                                TaxonomyCoordinate taxonomyCoordinate)
            throws IllegalArgumentException {
      // Make sure the referenced component meets the ref component restrictions, if any are present.
      if ((dsud.getReferencedComponentTypeRestriction() != null) &&
            (dsud.getReferencedComponentTypeRestriction() != ObjectChronologyType.UNKNOWN_NID)) {
         final ObjectChronologyType requiredType = dsud.getReferencedComponentTypeRestriction();
         final ObjectChronologyType foundType    = Get.identifierService()
                                                .getChronologyTypeForNid(referencedComponentNid);

         if (requiredType != foundType) {
            throw new IllegalArgumentException("The referenced component must be of type " + requiredType +
                                               ", but a " + foundType + " was passed");
         }

         if ((requiredType == ObjectChronologyType.SEMEME) &&
               (dsud.getReferencedComponentTypeSubRestriction() != null) &&
               (dsud.getReferencedComponentTypeSubRestriction() != SememeType.UNKNOWN)) {
            final SememeType requiredSememeType = dsud.getReferencedComponentTypeSubRestriction();
            final SememeType foundSememeType    = Get.sememeService()
                                               .getSememe(referencedComponentNid)
                                               .getSememeType();

            if (requiredSememeType != foundSememeType) {
               throw new IllegalArgumentException("The referenced component must be a sememe of type " +
                                                  requiredSememeType + ", but a " + foundSememeType + " was passed");
            }
         }
      }

      if (data == null) {
         data = new DynamicSememeData[] {};
      }

      // specifically allow < - we don't need the trailing columns, if they were defined as optional.
      if (data.length > dsud.getColumnInfo().length) {
         throw new IllegalArgumentException(
             "The Assemblage concept: " + dsud.getDynamicSememeName() + " specifies " + dsud.getColumnInfo().length +
             " columns of data, while the provided data contains " + data.length +
             " columns.  The data size array must not exeed the sememe definition." +
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
         final DynamicSememeData defaultValue = dsud.getColumnInfo()[i]
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
         final DynamicSememeColumnInfo dsci = dsud.getColumnInfo()[dataColumn];

         if (data[dataColumn] == null) {
            if (dsci.isColumnRequired()) {
               throw new IllegalArgumentException("No data was supplied for column " + (dataColumn + 1) +
                                                  " but the column is specified as a required column");
            }
         } else {
            final DynamicSememeDataType allowedDT = dsci.getColumnDataType();

            if ((data[dataColumn] != null) &&
                  (allowedDT != DynamicSememeDataType.POLYMORPHIC) &&
                  (data[dataColumn].getDynamicSememeDataType() != allowedDT)) {
               throw new IllegalArgumentException("The supplied data for column " + dataColumn + " is of type " +
                                                  data[dataColumn].getDynamicSememeDataType() +
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
                                                  stampCoordinate,
                                                  taxonomyCoordinate)) {
                           rethrow = true;
                           throw new IllegalArgumentException(
                               "The supplied data for column " + dataColumn +
                               " does not pass the assigned validator(s) for this dynamic sememe.  Data: " +
                               data[dataColumn].dataToString() + " Validator: " + dsci.getValidator()[i].name() +
                               " Validator Data: " + dsci.getValidatorData()[i].dataToString());
                        }
                     } catch (final IllegalArgumentException e) {
                        if (rethrow) {
                           throw e;
                        } else {
                           LoggerFactory.getLogger(DynamicSememeUtility.class)
                                        .debug("Couldn't execute validator due to missing coordiantes");
                        }
                     }
                  }
               } catch (final IllegalArgumentException e) {
                  throw e;
               } catch (final RuntimeException e) {
                  throw new IllegalArgumentException(e.getMessage());
               }
            }
         }
      }
   }
}

