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
 * {@link DynamicSememeData}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class DynamicSememeDataImpl
         implements DynamicSememeData {
   
   /** The name provider. */
   private transient Supplier<String> nameProvider_ = null;
   
   /** The data. */
   protected byte[]                   data_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic sememe data impl.
    */
   protected DynamicSememeDataImpl() {}

   /**
    * Instantiates a new dynamic sememe data impl.
    *
    * @param data the data
    */
   protected DynamicSememeDataImpl(byte[] data) {
      this.data_ = data;
   }

   /**
    * Instantiates a new dynamic sememe data impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicSememeDataImpl(byte[] data, int assemblageSequence, int columnNumber) {
      this.data_ = data;
      configureNameProvider(assemblageSequence, columnNumber);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Configure name provider.
    *
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   @Override
   public void configureNameProvider(int assemblageSequence, int columnNumber) {
      if (this.nameProvider_ == null) {
         this.nameProvider_ = new Supplier<String>() {
            private String nameCache_ = null;
            @Override
            public String get() {
               if (this.nameCache_ == null) {
                  final DynamicSememeUtility ls = LookupService.get()
                                                         .getService(DynamicSememeUtility.class);

                  if (ls == null) {
                     throw new RuntimeException(
                         "An implementation of DynamicSememeUtility is not available on the classpath");
                  } else {
                     this.nameCache_ = ls.readDynamicSememeUsageDescription(assemblageSequence)
                                    .getColumnInfo()[columnNumber]
                                    .getColumnName();
                  }
               }

               return this.nameCache_;
            }
         };
      }
   }

   /**
    * Data to string.
    *
    * @return the string
    */
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

         for (final DynamicSememeData dsdNest: ((DynamicSememeArray<?>) this).getDataArray()) {
            temp += dsdNest.dataToString() + ", ";
         }

         if (temp.length() > 1) {
            temp = temp.substring(0, temp.length() - 2);
         }

         temp += "]";
         return temp;

      case BYTEARRAY:
         return "[-byte array size " + this.data_.length + "]";

      case POLYMORPHIC:
      case UNKNOWN:
      default:
         LoggerFactory.getLogger(DynamicSememeDataImpl.class)
                      .error("Unexpected case!");
         return "--internal error--";
      }
   }

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

      final DynamicSememeDataImpl other = (DynamicSememeDataImpl) obj;

      if (!Arrays.equals(this.data_, other.data_)) {
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

      result = prime * result + Arrays.hashCode(this.data_);
      return result;
   }

   /**
    * To string.
    *
    * @return the string
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "(" + getDynamicSememeDataType().name() + " - " + getName() + " - " + getDataObject() + ")";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data.
    *
    * @return the data
    * @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData#getData()
    */
   @Override
   public byte[] getData() {
      return this.data_;
   }

   /**
    * Gets the dynamic sememe data type.
    *
    * @return the dynamic sememe data type
    * @see sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData#getDynamicSememeDataType()
    */
   @Override
   public DynamicSememeDataType getDynamicSememeDataType() {
      return DynamicSememeDataType.classToType(this.getClass());
   }

   /**
    * Gets the name.
    *
    * @return the name
    */
   protected String getName() {
      return ((this.nameProvider_ == null) ? "???"
                                      : this.nameProvider_.get());
   }
}

