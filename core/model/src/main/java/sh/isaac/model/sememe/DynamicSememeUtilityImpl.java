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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import javax.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.ObjectChronologyType;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBoolean;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArray;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDouble;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequence;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import sh.isaac.model.sememe.dataTypes.DynamicSememeArrayImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeBooleanImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeStringImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;

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
 * {@link DynamicSememeUtility}
 *
 * Convenience methods related to DynamicSememes.  Implemented as an interface and a singleton to provide
 * lower level code with access to these methods at runtime via HK2.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DynamicSememeUtilityImpl
         implements DynamicSememeUtility {
   /**
    * Configure column index info.
    *
    * @param columns the columns
    * @return the dynamic sememe array
    */
   @Override
   public DynamicSememeArray<DynamicSememeData> configureColumnIndexInfo(DynamicSememeColumnInfo[] columns) {
      final ArrayList<DynamicSememeIntegerImpl> temp = new ArrayList<>();

      if (columns != null) {
         Arrays.sort(columns);

         for (final DynamicSememeColumnInfo ci: columns) {
            // byte arrays are not currently indexable withing lucene
            if ((ci.getColumnDataType() != DynamicSememeDataType.BYTEARRAY) && ci.getIndexConfig()) {
               temp.add(new DynamicSememeIntegerImpl(ci.getColumnOrder()));
            }
         }

         if (temp.size() > 0) {
            return new DynamicSememeArrayImpl<DynamicSememeData>(temp.toArray(new DynamicSememeData[temp.size()]));
         }
      }

      return null;
   }

   /**
    * Configure dynamic sememe definition data for column.
    *
    * @param ci the ci
    * @return the dynamic sememe data[]
    */
   @Override
   public DynamicSememeData[] configureDynamicSememeDefinitionDataForColumn(DynamicSememeColumnInfo ci) {
      final DynamicSememeData[] data = new DynamicSememeData[7];

      data[0] = new DynamicSememeIntegerImpl(ci.getColumnOrder());
      data[1] = new DynamicSememeUUIDImpl(ci.getColumnDescriptionConcept());

      if (DynamicSememeDataType.UNKNOWN == ci.getColumnDataType()) {
         throw new RuntimeException("Error in column - if default value is provided, the type cannot be polymorphic");
      }

      data[2] = new DynamicSememeStringImpl(ci.getColumnDataType().name());
      data[3] = convertPolymorphicDataColumn(ci.getDefaultColumnValue(), ci.getColumnDataType());
      data[4] = new DynamicSememeBooleanImpl(ci.isColumnRequired());

      if (ci.getValidator() != null) {
         final DynamicSememeString[] validators = new DynamicSememeString[ci.getValidator().length];

         for (int i = 0; i < validators.length; i++) {
            validators[i] = new DynamicSememeStringImpl(ci.getValidator()[i].name());
         }

         data[5] = new DynamicSememeArrayImpl<DynamicSememeString>(validators);
      } else {
         data[5] = null;
      }

      if (ci.getValidatorData() != null) {
         final DynamicSememeData[] validatorData = new DynamicSememeData[ci.getValidatorData().length];

         for (int i = 0; i < validatorData.length; i++) {
            validatorData[i] = convertPolymorphicDataColumn(ci.getValidatorData()[i],
                  ci.getValidatorData()[i]
                    .getDynamicSememeDataType());
         }

         data[6] = new DynamicSememeArrayImpl<DynamicSememeData>(validatorData);
      } else {
         data[6] = null;
      }

      return data;
   }

   /**
    * Configure dynamic sememe restriction data.
    *
    * @param referencedComponentRestriction the referenced component restriction
    * @param referencedComponentSubRestriction the referenced component sub restriction
    * @return the dynamic sememe data[]
    */
   @Override
   public DynamicSememeData[] configureDynamicSememeRestrictionData(ObjectChronologyType referencedComponentRestriction,
         SememeType referencedComponentSubRestriction) {
      if ((referencedComponentRestriction != null) &&
            (ObjectChronologyType.UNKNOWN_NID != referencedComponentRestriction)) {
         int size = 1;

         if ((referencedComponentSubRestriction != null) && (SememeType.UNKNOWN != referencedComponentSubRestriction)) {
            size = 2;
         }

         final DynamicSememeData[] data = new DynamicSememeData[size];

         data[0] = new DynamicSememeStringImpl(referencedComponentRestriction.name());

         if (size == 2) {
            data[1] = new DynamicSememeStringImpl(referencedComponentSubRestriction.name());
         }

         return data;
      }

      return null;
   }

   /**
    * Creates the dynamic string data.
    *
    * @param value the value
    * @return the dynamic sememe string
    */
   @Override
   public DynamicSememeString createDynamicStringData(String value) {
      return new DynamicSememeStringImpl(value);
   }

   /**
    * Creates the dynamic UUID data.
    *
    * @param value the value
    * @return the dynamic sememe UUID
    */
   @Override
   public DynamicSememeUUID createDynamicUUIDData(UUID value) {
      return new DynamicSememeUUIDImpl(value);
   }

   /**
    * Read the {@link DynamicSememeUsageDescription} for the specified assemblage concept.
    *
    * @param assemblageNidOrSequence the assemblage nid or sequence
    * @return the dynamic sememe usage description
    */
   @Override
   public DynamicSememeUsageDescription readDynamicSememeUsageDescription(int assemblageNidOrSequence) {
      return DynamicSememeUsageDescriptionImpl.read(assemblageNidOrSequence);
   }

   /**
    * To string.
    *
    * @param data DynamicSememeData[]
    * @return the string
    */
   public static String toString(DynamicSememeData[] data) {
      final StringBuilder sb = new StringBuilder();

      sb.append("[");

      if (data != null) {
         for (final DynamicSememeData dsd: data) {
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
    * @return the dynamic sememe data
    */
   private static DynamicSememeData convertPolymorphicDataColumn(DynamicSememeData defaultValue,
         DynamicSememeDataType columnType) {
      DynamicSememeData result;

      if (defaultValue != null) {
         try {
            if (DynamicSememeDataType.BOOLEAN == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.BYTEARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.DOUBLE == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.FLOAT == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.INTEGER == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.LONG == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.NID == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.STRING == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.UUID == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.ARRAY == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.SEQUENCE == columnType) {
               result = defaultValue;
            } else if (DynamicSememeDataType.POLYMORPHIC == columnType) {
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

