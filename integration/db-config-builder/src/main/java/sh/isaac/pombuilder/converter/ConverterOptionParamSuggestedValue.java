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



package sh.isaac.pombuilder.converter;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link ConverterOptionParamSuggestedValue}
 * Just a simple Pair object.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConverterOptionParamSuggestedValue {
   private String value;
   private String description;

   //~--- constructors --------------------------------------------------------

   @SuppressWarnings("unused")
   private ConverterOptionParamSuggestedValue() {
      // For serialization
   }

   public ConverterOptionParamSuggestedValue(String value) {
      this.value = value;
   }

   public ConverterOptionParamSuggestedValue(String value, String description) {
      this.value       = value;
      this.description = description;
   }

   //~--- methods -------------------------------------------------------------

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

      ConverterOptionParamSuggestedValue other = (ConverterOptionParamSuggestedValue) obj;

      if (value == null) {
         if (other.value != null) {
            return false;
         }
      } else if (!value.equals(other.value)) {
         return false;
      }

      if (getDescription() == null) {
         if (other.getDescription() != null) {
            return false;
         }
      } else if (!getDescription().equals(other.getDescription())) {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      final int prime  = 31;
      int       result = 1;

      result = prime * result + getDescription().hashCode();
      result = prime * result + ((value == null) ? 0
            : value.hashCode());
      return result;
   }

   @Override
   public String toString() {
      return "ConverterOptionParamSuggestedValue [value=" + value + ", description=" + description + "]";
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * A user friendly description of this value for use in user-facing GUIs.  If a description wasn't provided
    * upon construction, this will return the same thing as {@link #getValue()}
    */
   public String getDescription() {
      return StringUtils.isNotBlank(description) ? description
            : value;
   }

   /**
    * The value to be passed in to the {@link ConverterOptionValue} class
    */
   public String getValue() {
      return value;
   }
}

