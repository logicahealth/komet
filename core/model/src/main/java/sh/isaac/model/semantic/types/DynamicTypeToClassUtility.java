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



package sh.isaac.model.semantic.types;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicTypeToClassUtility}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicTypeToClassUtility {
   /**
    * Type to class.
    *
    * @param type the type
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    * @return the dynamic data impl
    */
   public static DynamicDataImpl typeToClass(DynamicDataType type,
         byte[] data,
         int assemblageSequence,
         int columnNumber) {
      switch (type) {
      case ARRAY:
         return new DynamicArrayImpl<>(data, assemblageSequence, columnNumber);

      case BOOLEAN:
         return new DynamicBooleanImpl(data, assemblageSequence, columnNumber);

      case BYTEARRAY:
         return new DynamicByteArrayImpl(data, assemblageSequence, columnNumber);

      case DOUBLE:
         return new DynamicDoubleImpl(data, assemblageSequence, columnNumber);

      case FLOAT:
         return new DynamicFloatImpl(data, assemblageSequence, columnNumber);

      case INTEGER:
         return new DynamicIntegerImpl(data, assemblageSequence, columnNumber);

      case LONG:
         return new DynamicLongImpl(data, assemblageSequence, columnNumber);

      case NID:
         return new DynamicNidImpl(data, assemblageSequence, columnNumber);

      case STRING:
         return new DynamicStringImpl(data, assemblageSequence, columnNumber);

      case UUID:
         return new DynamicUUIDImpl(data, assemblageSequence, columnNumber);

      case SEQUENCE:
         return new DynamicSequenceImpl(data, assemblageSequence, columnNumber);

      case POLYMORPHIC:
      case UNKNOWN:
         throw new RuntimeException("No implementation exists for type unknown");

      default:
         throw new RuntimeException("Implementation error");
      }
   }

   /**
    * Impl class for type.
    *
    * @param type the type
    * @return the class
    */
   protected static Class<? extends DynamicData> implClassForType(DynamicDataType type) {
      switch (type) {
      case ARRAY:
         return DynamicArrayImpl.class;

      case BOOLEAN:
         return DynamicBooleanImpl.class;

      case BYTEARRAY:
         return DynamicByteArrayImpl.class;

      case DOUBLE:
         return DynamicDoubleImpl.class;

      case FLOAT:
         return DynamicFloatImpl.class;

      case INTEGER:
         return DynamicIntegerImpl.class;

      case LONG:
         return DynamicLongImpl.class;

      case NID:
         return DynamicNidImpl.class;

      case STRING:
         return DynamicStringImpl.class;

      case UUID:
         return DynamicUUIDImpl.class;

      case SEQUENCE:
         return DynamicSequenceImpl.class;

      case UNKNOWN:
      case POLYMORPHIC:
         throw new RuntimeException("Should be impossible");

      default:
         throw new RuntimeException("Design failure");
      }
   }

   /**
    * Type to class.
    *
    * @param type the type
    * @param data the data
    * @return the dynamic data
    */
   protected static DynamicData typeToClass(DynamicDataType type, byte[] data) {
      switch (type) {
      case ARRAY:
         return new DynamicArrayImpl<>(data);

      case BOOLEAN:
         return new DynamicBooleanImpl(data);

      case BYTEARRAY:
         return new DynamicByteArrayImpl(data);

      case DOUBLE:
         return new DynamicDoubleImpl(data);

      case FLOAT:
         return new DynamicFloatImpl(data);

      case INTEGER:
         return new DynamicIntegerImpl(data);

      case LONG:
         return new DynamicLongImpl(data);

      case NID:
         return new DynamicNidImpl(data);

      case STRING:
         return new DynamicStringImpl(data);

      case UUID:
         return new DynamicUUIDImpl(data);

      case SEQUENCE:
         return new DynamicSequenceImpl(data);

      case UNKNOWN:
      case POLYMORPHIC:
         throw new RuntimeException("Should be impossible");

      default:
         throw new RuntimeException("Design failure");
      }
   }
}

