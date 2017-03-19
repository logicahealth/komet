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

/**
 * The Class ValuePropertyPair.
 */
public class ValuePropertyPair
         implements Comparable<ValuePropertyPair> {
   
   /** The value disabled. */
   private Boolean  valueDisabled_ = null;  // used for overriding the property default with instance data
   
   /** The time. */
   protected Long   time_          = null;
   
   /** The property. */
   private final Property property_;
   
   /** The value. */
   private final String   value_;
   
   /** The description UUI D. */
   private UUID     descriptionUUID_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new value property pair.
    *
    * @param value the value
    * @param property the property
    */
   public ValuePropertyPair(String value, Property property) {
      this.value_           = value;
      this.property_        = property;
      this.descriptionUUID_ = null;
   }

   /**
    * Instantiates a new value property pair.
    *
    * @param value the value
    * @param descriptionUUID the description UUID
    * @param property the property
    */
   public ValuePropertyPair(String value, UUID descriptionUUID, Property property) {
      this.value_           = value;
      this.property_        = property;
      this.descriptionUUID_ = descriptionUUID;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compare to.
    *
    * @param o the o
    * @return the int
    */
   @Override
   public int compareTo(ValuePropertyPair o) {
      int result = this.property_.getPropertyType()
                            .getClass()
                            .getName()
                            .compareTo(o.property_.getPropertyType()
                                  .getClass()
                                  .getName());

      if (result == 0) {
         result = this.property_.getPropertySubType() - o.property_.getPropertySubType();

         if (result == 0) {
            result = this.property_.getSourcePropertyNameFSN()
                              .compareTo(o.property_.getSourcePropertyNameFSN());

            if (result == 0) {
               result = this.value_.compareTo(o.value_);
            }
         }
      }

      return result;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Should this description instance be disabled, taking into account local override (if set) and falling back to property default.
    *
    * @return true, if disabled
    */
   public boolean isDisabled() {
      if (this.valueDisabled_ != null) {
         return this.valueDisabled_;
      } else {
         return this.property_.isDisabled();
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the disabled.
    *
    * @param disabled the new disabled
    */
   public void setDisabled(boolean disabled) {
      this.valueDisabled_ = disabled;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the property.
    *
    * @return the property
    */
   public Property getProperty() {
      return this.property_;
   }

   /**
    * Gets the time.
    *
    * @return the time
    */
   public Long getTime() {
      return this.time_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the time.
    *
    * @param time the new time
    */
   public void setTime(long time) {
      this.time_ = time;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   public UUID getUUID() {
      return this.descriptionUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the uuid.
    *
    * @param uuid the new uuid
    */
   public void setUUID(UUID uuid) {
      this.descriptionUUID_ = uuid;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the value.
    *
    * @return the value
    */
   public String getValue() {
      return this.value_;
   }
}

