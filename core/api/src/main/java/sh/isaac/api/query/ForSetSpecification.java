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



package sh.isaac.api.query;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.collections.ConceptSequenceSet;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SememeSequenceSet;

//~--- classes ----------------------------------------------------------------

/**
 * Created by kec on 11/2/14.
 */
@XmlRootElement(name = "for-set")
@XmlAccessorType(value = XmlAccessType.NONE)
public class ForSetSpecification {
   /** The for collection types. */
   @XmlElementWrapper(name = "for")
   @XmlElement(name = "component")
   private List<ComponentCollectionTypes> forCollectionTypes = new ArrayList<>();

   /** The custom collection. */
   @XmlElementWrapper(name = "custom-for")
   @XmlElement(name = "uuid")
   private Set<UUID> customCollection = new HashSet<>();

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new for set specification.
    */
   public ForSetSpecification() {}

   /**
    * Instantiates a new for set specification.
    *
    * @param forCollectionTypes the for collection types
    */
   public ForSetSpecification(ComponentCollectionTypes... forCollectionTypes) {
      this.forCollectionTypes.addAll(Arrays.asList(forCollectionTypes));
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the collection.
    *
    * @return the collection
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public NidSet getCollection()
            throws IOException {
      final NidSet forSet = NidSet.of();

      for (final ComponentCollectionTypes collection: this.forCollectionTypes) {
         switch (collection) {
         case ALL_COMPONENTS:
            forSet.or(NidSet.ofAllComponentNids());
            break;

         case ALL_CONCEPTS:
            forSet.or(NidSet.of(ConceptSequenceSet.of(Get.identifierService()
                  .getConceptSequenceStream())));
            break;

         case ALL_SEMEMES:
            forSet.or(NidSet.of(SememeSequenceSet.of(Get.identifierService()
                  .getSememeSequenceStream())));
            break;

         case CUSTOM_SET:
            for (final UUID uuid: this.customCollection) {
               forSet.add(Get.identifierService()
                             .getNidForUuids(uuid));
            }

            break;

         default:
            throw new UnsupportedOperationException();
         }
      }

      return forSet;
   }

   /**
    * Gets the custom collection.
    *
    * @return the custom collection
    */
   public Set<UUID> getCustomCollection() {
      return this.customCollection;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the custom collection.
    *
    * @param customCollection the new custom collection
    */
   public void setCustomCollection(Set<UUID> customCollection) {
      this.customCollection = customCollection;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the for collection types.
    *
    * @return the for collection types
    */
   public List<ComponentCollectionTypes> getForCollectionTypes() {
      return this.forCollectionTypes;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the for collection types.
    *
    * @param forCollectionTypes the new for collection types
    */
   public void setForCollectionTypes(List<ComponentCollectionTypes> forCollectionTypes) {
      this.forCollectionTypes = forCollectionTypes;
   }
}

