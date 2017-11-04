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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.model.semantic.types.DynamicArrayImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;

//~--- classes ----------------------------------------------------------------

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
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * {@link DynamicUtility}
 *
 * Convenience methods related to DynamicSememes.  Implemented as an interface and a singleton to provide
 * lower level code with access to these methods at runtime via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DynamicUtilityImpl
         implements DynamicUtility {
   /**
    * Configure column index info.
    *
    * @param columns the columns
    * @return the dynamic element array
    */
   @Override
   public DynamicArray<DynamicData> configureColumnIndexInfo(DynamicColumnInfo[] columns) {
      final ArrayList<DynamicIntegerImpl> temp = new ArrayList<>();

      if (columns != null) {
         Arrays.sort(columns);

         for (final DynamicColumnInfo ci: columns) {
            // byte arrays are not currently indexable withing lucene
            if ((ci.getColumnDataType() != DynamicDataType.BYTEARRAY) && ci.getIndexConfig()) {
               temp.add(new DynamicIntegerImpl(ci.getColumnOrder()));
            }
         }

         if (temp.size() > 0) {
            return new DynamicArrayImpl<>(temp.toArray(new DynamicData[temp.size()]));
         }
      }

      return null;
   }

   /**
    * Configure dynamic element definition data for column.
    *
    * @param ci the ci
    * @return the dynamic element data[]
    */
   @Override
   public DynamicData[] configureDynamicDefinitionDataForColumn(DynamicColumnInfo ci) {
      final DynamicData[] data = new DynamicData[7];

      data[0] = new DynamicIntegerImpl(ci.getColumnOrder());
      data[1] = new DynamicUUIDImpl(ci.getColumnDescriptionConcept());

      if (DynamicDataType.UNKNOWN == ci.getColumnDataType()) {
         throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
      }

      data[2] = new DynamicStringImpl(ci.getColumnDataType().name());
      data[3] = convertPolymorphicDataColumn(ci.getDefaultColumnValue(), ci.getColumnDataType());
      data[4] = new DynamicBooleanImpl(ci.isColumnRequired());

      if (ci.getValidator() != null) {
         final DynamicString[] validators = new DynamicString[ci.getValidator().length];

         for (int i = 0; i < validators.length; i++) {
            validators[i] = new DynamicStringImpl(ci.getValidator()[i].name());
         }

         data[5] = new DynamicArrayImpl<>(validators);
      } else {
         data[5] = null;
      }

      if (ci.getValidatorData() != null) {
         final DynamicData[] validatorData = new DynamicData[ci.getValidatorData().length];

         for (int i = 0; i < validatorData.length; i++) {
            validatorData[i] = convertPolymorphicDataColumn(ci.getValidatorData()[i],
                  ci.getValidatorData()[i]
                    .getDynamicDataType());
         }

         data[6] = new DynamicArrayImpl<>(validatorData);
      } else {
         data[6] = null;
      }

      return data;
   }

   /**
    * Configure dynamic element restriction data.
    *
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @return the dynamic element data[]
    */
   @Override
   public DynamicData[] configureDynamicRestrictionData(ObjectChronologyType referencedComponentRestriction,
         VersionType referencedComponentSubRestriction) {
      if ((referencedComponentRestriction != null) &&
            (ObjectChronologyType.UNKNOWN_NID != referencedComponentRestriction)) {
         int size = 1;

         if ((referencedComponentSubRestriction != null) && (VersionType.UNKNOWN != referencedComponentSubRestriction)) {
            size = 2;
         }

         final DynamicData[] data = new DynamicData[size];

         data[0] = new DynamicStringImpl(referencedComponentRestriction.name());

         if (size == 2) {
            data[1] = new DynamicStringImpl(referencedComponentSubRestriction.name());
         }

         return data;
      }

      return null;
   }

   /**
    * Creates the dynamic string data.
    *
    * @param value the value
    * @return the dynamic element string
    */
   @Override
   public DynamicString createDynamicStringData(String value) {
      return new DynamicStringImpl(value);
   }

   /**
    * Creates the dynamic UUID data.
    *
    * @param value the value
    * @return the dynamic element UUID
    */
   @Override
   public DynamicUUID createDynamicUUIDData(UUID value) {
      return new DynamicUUIDImpl(value);
   }

   /**
    * Read the {@link DynamicUsageDescription} for the specified assemblage concept.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic element usage description
    */
   @Override
   public DynamicUsageDescription readDynamicUsageDescription(int assemblageNidOrSequence) {
      return DynamicUsageDescriptionImpl.read(assemblageNidOrSequence);
   }

   /**
    * To string.
    *
    * @param data DynamicData[]
    * @return the string
    */
   public static String toString(DynamicData[] data) {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");

      if (data != null) {
         for (final DynamicData dsd: data) {
            if (dsd != null) {
               sb.append(dsd.dataToString());
            }

            sb.append(", ");
         }

         if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
         }
      }

      sb.append("]");
      return sb.toString();
   }

   /**
    * Convert polymorphic data column.
    *
    * @param defaultValue the default value
    * @param columnType the column type
    * @return the dynamic element data
    */
   private static DynamicData convertPolymorphicDataColumn(DynamicData defaultValue,
         DynamicDataType columnType) {
      DynamicData result;

      if (defaultValue != null) {
         try {
            if (DynamicDataType.BOOLEAN == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.BYTEARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.DOUBLE == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.FLOAT == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.INTEGER == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.LONG == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.NID == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.STRING == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.UUID == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.ARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.SEQUENCE == columnType) {
               result = defaultValue;
            } else if (DynamicDataType.POLYMORPHIC == columnType) {
               throw new RuntimeException(
                   "Error in column - if default value is provided, the type cannot be polymorphic");
            } else {
               throw new RuntimeException("Actually, the implementation is broken.  Ooops.");
            }
         } catch (final ClassCastException e) {
            throw new RuntimeException(
                "Error in column - if default value is provided, the type must be compatible with the the column descriptor type");
         }
      } else {
         result = null;
      }

      return result;
   }
}

