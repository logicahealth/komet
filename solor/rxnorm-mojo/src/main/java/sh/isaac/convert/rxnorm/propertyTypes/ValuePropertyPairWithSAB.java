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



package sh.isaac.convert.rxnorm.propertyTypes;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.rxnorm.rrf.RXNSAT;

import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.converters.sharedUtils.umlsUtils.ValuePropertyPairWithAttributes;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ValuePropertyPairWithSAB.
 */
public class ValuePropertyPairWithSAB
        extends ValuePropertyPairWithAttributes {
   /** The sab. */
   private final String sab;

   /** The sat data. */
   private final ArrayList<RXNSAT> satData;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new value property pair with SAB.
    *
    * @param value the value
    * @param property the property
    * @param sab the sab
    * @param satData the sat data
    */
   public ValuePropertyPairWithSAB(String value, Property property, String sab, ArrayList<RXNSAT> satData) {
      super(value, property);
      this.sab     = sab;
      this.satData = satData;
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
      // Boosting descriptions that come from RXNORM up to the very top.
      if (this.sab.equals("RXNORM") &&!((ValuePropertyPairWithSAB) o).sab.equals("RXNORM")) {
         return -1;
      } else if (!this.sab.equals("RXNORM") && ((ValuePropertyPairWithSAB) o).sab.equals("RXNORM")) {
         return 1;
      }

      return super.compareTo(o);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sab.
    *
    * @return the sab
    */
   public String getSab() {
      return this.sab;
   }

   /**
    * Gets the sat data.
    *
    * @return the sat data
    */
   public ArrayList<RXNSAT> getSatData() {
      return this.satData;
   }
}

