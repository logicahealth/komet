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

import sh.isaac.api.Status;
import sh.isaac.converters.sharedUtils.ComponentReference;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;
import sh.isaac.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.SemanticChronology;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ValuePropertyPairWithAttributes.
 */
public class ValuePropertyPairWithAttributes
        extends ValuePropertyPair {
   /** The string attributes. */
   protected HashMap<UUID, ArrayList<String>> stringAttributes = new HashMap<>();

   /** The uuid attributes. */
   protected HashMap<UUID, ArrayList<UUID>> uuidAttributes = new HashMap<>();
   
   protected HashMap<UUID, ArrayList<String>> identifierAttributes = new HashMap<>();

   /** The refset membership. */
   protected ArrayList<UUID> refsetMembership = new ArrayList<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new value property pair with attributes.
    *
    * @param value the value
    * @param property the property
    */
   public ValuePropertyPairWithAttributes(String value, Property property) {
      super(value, property);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the refset membership.
    *
    * @param refsetConcept the refset concept
    */
   public void addRefsetMembership(UUID refsetConcept) {
      this.refsetMembership.add(refsetConcept);
   }
   
   
   public void addIdentifierAttribute(UUID type, String value)
   {
      ArrayList<String> values = identifierAttributes.get(type);
      if (values == null)
      {
         values = new ArrayList<>();
         identifierAttributes.put(type, values);
      }
      values.add(value);
   }

   /**
    * Adds the string attribute.
    *
    * @param type the type
    * @param value the value
    */
   public void addStringAttribute(UUID type, String value) {
      ArrayList<String> values = this.stringAttributes.get(type);

      if (values == null) {
         values = new ArrayList<>();
         this.stringAttributes.put(type, values);
      }

      values.add(value);
   }

   /**
    * Adds the UUID attribute.
    *
    * @param type the type
    * @param value the value
    */
   public void addUUIDAttribute(UUID type, UUID value) {
      ArrayList<UUID> values = this.uuidAttributes.get(type);

      if (values == null) {
         values = new ArrayList<>();
         this.uuidAttributes.put(type, values);
      }

      values.add(value);
   }

   /**
    * Process attributes.
    *
    * @param ibdfCreationUtility the ibdf creation utility
    * @param descriptionSource the description source
    * @param descriptions the descriptions
    */
   public static void processAttributes(IBDFCreationUtility ibdfCreationUtility,
         List<? extends ValuePropertyPairWithAttributes> descriptionSource,
         List<SemanticChronology> descriptions) {
      for (int i = 0; i < descriptionSource.size(); i++) {
         for (final Entry<UUID, ArrayList<String>> attributes: descriptionSource.get(i).stringAttributes
               .entrySet()) {
            for (final String value: attributes.getValue()) {
               ibdfCreationUtility.addStringAnnotation(ComponentReference.fromChronology(descriptions.get(i)),
                     value,
                     attributes.getKey(),
                     Status.ACTIVE);
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
            ibdfCreationUtility.addAssemblageMembership(ComponentReference.fromChronology(descriptions.get(i)),
                  refsetConcept,
                  Status.ACTIVE,
                  null);
         }
         
       for (Entry<UUID, ArrayList<String>> identifierAttributes : descriptionSource.get(i).identifierAttributes
            .entrySet()) {
         for (String value : identifierAttributes.getValue()) {
            // TODO confirm parameters appropriate
            ibdfCreationUtility.addStaticStringAnnotation(ComponentReference.fromChronology(descriptions.get(i)), value, identifierAttributes.getKey(), Status.ACTIVE);
             
         }
       }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the string attribute.
    *
    * @param type the type
    * @return the string attribute
    */
   public ArrayList<String> getStringAttribute(UUID type) {
      return this.stringAttributes.get(type);
   }
   
   public ArrayList<String> getIdentifierAttribute(UUID type)
   {
      return identifierAttributes.get(type);
   }
}

