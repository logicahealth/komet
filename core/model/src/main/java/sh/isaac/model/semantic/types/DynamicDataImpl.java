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

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.LookupService;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicData}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class DynamicDataImpl
         implements DynamicData {
   /** The name provider. */
   private transient Supplier<String> nameProvider = null;

   /** The data. */
   protected byte[] data;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic data impl.
    */
   protected DynamicDataImpl() {}

   /**
    * Instantiates a new dynamic data impl.
    *
    * @param data the data
    */
   protected DynamicDataImpl(byte[] data) {
      this.data = data;
   }

   /**
    * Instantiates a new dynamic data impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicDataImpl(byte[] data, int assemblageSequence, int columnNumber) {
      this.data = data;
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
   public final void configureNameProvider(int assemblageSequence, int columnNumber) {
      if (this.nameProvider == null) {
         this.nameProvider = new Supplier<String>() {
            private String nameCache = null;
            @Override
            public String get() {
               if (this.nameCache == null) {
                  final DynamicUtility ls = LookupService.get()
                                                               .getService(DynamicUtility.class);

                  if (ls == null) {
                     throw new RuntimeException(
                         "An implementation of DynamicSemanticUtility is not available on the classpath");
                  } else {
                     this.nameCache = ls.readDynamicUsageDescription(assemblageSequence)
                                         .getColumnInfo()[columnNumber]
                                         .getColumnName();
                  }
               }

               return this.nameCache;
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
      switch (this.getDynamicDataType()) {
      case BOOLEAN:
      case DOUBLE:
      case FLOAT:
      case INTEGER:
      case LONG:
      case NID:
      case STRING:
      case UUID:
         return getDataObject().toString();

      case ARRAY:
         String temp = "[";

         for (final DynamicData dsdNest: ((DynamicArray<?>) this).getDataArray()) {
            temp += dsdNest.dataToString() + ", ";
         }

         if (temp.length() > 1) {
            temp = temp.substring(0, temp.length() - 2);
         }

         temp += "]";
         return temp;

      case BYTEARRAY:
         return "[-byte array size " + this.data.length + "]";

      case POLYMORPHIC:
      case UNKNOWN:
      default:
         LogManager.getLogger().error("Unexpected case!");
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

      final DynamicDataImpl other = (DynamicDataImpl) obj;

      return Arrays.equals(this.data, other.data);
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

      result = prime * result + Arrays.hashCode(this.data);
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
      return "(" + getDynamicDataType().name() + " - " + getName() + " - " + getDataObject() + ")";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   @Override
   public byte[] getData() {
      return this.data;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DynamicDataType getDynamicDataType() {
      return DynamicDataType.classToType(this.getClass());
   }

   /**
    * Gets the name.
    *
    * @return the name
    */
   protected String getName() {
      return ((this.nameProvider == null) ? "???"
            : this.nameProvider.get());
   }
}

