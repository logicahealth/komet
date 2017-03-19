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



package sh.isaac.model.sememe.dataTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.LoggerFactory;

import sh.isaac.api.LookupService;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class DynamicSememeDataImpl
         implements DynamicSememeData {
   private transient Supplier<String> nameProvider_ = null;
   protected byte[]                   data_;

   //~--- constructors --------------------------------------------------------

   protected DynamicSememeDataImpl() {}

   protected DynamicSememeDataImpl(byte[] data) {
      data_ = data;
   }

   protected DynamicSememeDataImpl(byte[] data, int assemblageSequence, int columnNumber) {
      data_ = data;
      configureNameProvider(assemblageSequence, columnNumber);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void configureNameProvider(int assemblageSequence, int columnNumber) {
      if (nameProvider_ == null) {
         nameProvider_ = new Supplier<String>() {
            private String nameCache_ = null;
            @Override
            public String get() {
               if (nameCache_ == null) {
                  DynamicSememeUtility ls = LookupService.get()
                                                         .getService(DynamicSememeUtility.class);

                  if (ls == null) {
                     throw new RuntimeException(
                         "An implementation of DynamicSememeUtility is not available on the classpath");
                  } else {
                     nameCache_ = ls.readDynamicSememeUsageDescription(assemblageSequence)
                                    .getColumnInfo()[columnNumber]
                                    .getColumnName();
                  }
               }

               return nameCache_;
            }
         };
      }
   }

   @Override
   public String dataToString() {
      switch (this.getDynamicSememeDataType()) {
      case BOOLEAN:
      case DOUBLE:
      case FLOAT:
      case INTEGER:
      case LONG:
      case NID:
      case SEQUENCE:
      case STRING:
      case UUID:
         return getDataObject().toString();

      case ARRAY:
         String temp = "[";

         for (DynamicSememeData dsdNest: ((DynamicSememeArray<?>) this).getDataArray()) {
            temp += dsdNest.dataToString() + ", ";
         }

         if (temp.length() > 1) {
            temp = temp.substring(0, temp.length() - 2);
         }

         temp += "]";
         return temp;

      case BYTEARRAY:
         return "[-byte array size " + data_.length + "]";

      case POLYMORPHIC:
      case UNKNOWN:
      default:
         LoggerFactory.getLogger(DynamicSememeDataImpl.class)
                      .error("Unexpected case!");
         return "--internal error--";
      }
   }

   /**
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

      DynamicSememeDataImpl other = (DynamicSememeDataImpl) obj;

      if (!Arrays.equals(data_, other.data_)) {
         return false;
      }

      return true;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + Arrays.hashCode(data_);
      return result;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "(" + getDynamicSememeDataType().name() + " - " + getName() + " - " + getDataObject() + ")";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    *
    * @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData#getData()
    */
   @Override
   public byte[] getData() {
      return data_;
   }

   /**
    * @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData#getDynamicSememeDataType()
    */
   @Override
   public DynamicSememeDataType getDynamicSememeDataType() {
      return DynamicSememeDataType.classToType(this.getClass());
   }

   protected String getName() {
      return ((nameProvider_ == null) ? "???"
                                      : nameProvider_.get());
   }
}

