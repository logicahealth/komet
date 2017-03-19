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



package sh.isaac.converters.sharedUtils.umlsUtils;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.State;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;

//~--- classes ----------------------------------------------------------------

public class ValuePropertyPairWithAttributes
        extends ValuePropertyPair {
   protected HashMap<UUID, ArrayList<String>> stringAttributes = new HashMap<>();
   protected HashMap<UUID, ArrayList<UUID>>   uuidAttributes   = new HashMap<>();
   protected ArrayList<UUID>                  refsetMembership = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   public ValuePropertyPairWithAttributes(String value, Property property) {
      super(value, property);
   }

   //~--- methods -------------------------------------------------------------

   public void addRefsetMembership(UUID refsetConcept) {
      this.refsetMembership.add(refsetConcept);
   }

   public void addStringAttribute(UUID type, String value) {
      ArrayList<String> values = this.stringAttributes.get(type);

      if (values == null) {
         values = new ArrayList<>();
         this.stringAttributes.put(type, values);
      }

      values.add(value);
   }

   public void addUUIDAttribute(UUID type, UUID value) {
      ArrayList<UUID> values = this.uuidAttributes.get(type);

      if (values == null) {
         values = new ArrayList<>();
         this.uuidAttributes.put(type, values);
      }

      values.add(value);
   }

   public static void processAttributes(IBDFCreationUtility ibdfCreationUtility,
         List<? extends ValuePropertyPairWithAttributes> descriptionSource,
         List<SememeChronology<DescriptionSememe<?>>> descriptions) {
      for (int i = 0; i < descriptionSource.size(); i++) {
         for (final Entry<UUID, ArrayList<String>> attributes: descriptionSource.get(i).stringAttributes
               .entrySet()) {
            for (final String value: attributes.getValue()) {
               ibdfCreationUtility.addStringAnnotation(ComponentReference.fromChronology(descriptions.get(i)),
                     value,
                     attributes.getKey(),
                     State.ACTIVE);
            }
         }

         for (final Entry<UUID, ArrayList<UUID>> attributes: descriptionSource.get(i).uuidAttributes
               .entrySet()) {
            for (final UUID value: attributes.getValue()) {
               ibdfCreationUtility.addUUIDAnnotation(ComponentReference.fromChronology(descriptions.get(i)),
                     value,
                     attributes.getKey());
            }
         }

         for (final UUID refsetConcept: descriptionSource.get(i).refsetMembership) {
            ibdfCreationUtility.addRefsetMembership(ComponentReference.fromChronology(descriptions.get(i)),
                  refsetConcept,
                  State.ACTIVE,
                  null);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public ArrayList<String> getStringAttribute(UUID type) {
      return this.stringAttributes.get(type);
   }
}

