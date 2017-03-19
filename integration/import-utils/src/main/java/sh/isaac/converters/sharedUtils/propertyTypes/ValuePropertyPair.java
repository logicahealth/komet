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



package sh.isaac.converters.sharedUtils.propertyTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- classes ----------------------------------------------------------------

public class ValuePropertyPair
         implements Comparable<ValuePropertyPair> {
   private Boolean  valueDisabled_ = null;  // used for overriding the property default with instance data
   protected Long   time_          = null;
   private Property property_;
   private String   value_;
   private UUID     descriptionUUID_;

   //~--- constructors --------------------------------------------------------

   public ValuePropertyPair(String value, Property property) {
      value_           = value;
      property_        = property;
      descriptionUUID_ = null;
   }

   public ValuePropertyPair(String value, UUID descriptionUUID, Property property) {
      value_           = value;
      property_        = property;
      descriptionUUID_ = descriptionUUID;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(ValuePropertyPair o) {
      int result = property_.getPropertyType()
                            .getClass()
                            .getName()
                            .compareTo(o.property_.getPropertyType()
                                  .getClass()
                                  .getName());

      if (result == 0) {
         result = property_.getPropertySubType() - o.property_.getPropertySubType();

         if (result == 0) {
            result = property_.getSourcePropertyNameFSN()
                              .compareTo(o.property_.getSourcePropertyNameFSN());

            if (result == 0) {
               result = value_.compareTo(o.value_);
            }
         }
      }

      return result;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Should this description instance be disabled, taking into account local override (if set) and falling back to property default.
    * @return
    */
   public boolean isDisabled() {
      if (valueDisabled_ != null) {
         return valueDisabled_;
      } else {
         return property_.isDisabled();
      }
   }

   //~--- set methods ---------------------------------------------------------

   public void setDisabled(boolean disabled) {
      valueDisabled_ = disabled;
   }

   //~--- get methods ---------------------------------------------------------

   public Property getProperty() {
      return property_;
   }

   public Long getTime() {
      return time_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setTime(long time) {
      time_ = time;
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getUUID() {
      return descriptionUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUUID(UUID uuid) {
      descriptionUUID_ = uuid;
   }

   //~--- get methods ---------------------------------------------------------

   public String getValue() {
      return value_;
   }
}

