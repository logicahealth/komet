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
package sh.isaac.api;

//~--- JDK imports ------------------------------------------------------------
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.annotation.XmlTransient;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.UUIDUtil;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 2/16/15.
 */
public class ConceptProxy
        implements ConceptSpecification {

   /**
    * Universal identifiers for the concept proxied by the is object.
    */
   protected UUID[] uuids;

   /**
    * A fullySpecifiedDescriptionText of the concept proxied by this object.
    */
   protected String fullySpecifiedDescriptionText;

   protected String preferredDescriptionText;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new concept proxy.
    */
   public ConceptProxy() {
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param conceptSequenceOrNid the concept sequence or nid
    */
   public ConceptProxy(int conceptSequenceOrNid) {
      final ConceptChronology cc = Get.conceptService()
              .getConcept(conceptSequenceOrNid);

      this.uuids = cc.getUuidList()
              .toArray(new UUID[0]);
      this.fullySpecifiedDescriptionText = Get.defaultCoordinate().getFullySpecifiedDescriptionText(conceptSequenceOrNid);
      this.preferredDescriptionText = Get.defaultCoordinate().getPreferredDescriptionText(conceptSequenceOrNid);
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param externalString the external string
    */
   public ConceptProxy(String externalString) {

      final String[] parts = StringUtils.split(externalString, FIELD_SEPARATOR);

      int partIndex = 0;

      this.fullySpecifiedDescriptionText = parts[partIndex++];

      if (UUIDUtil.isUUID(parts[partIndex])) {
         this.preferredDescriptionText = parts[partIndex++];
      }

      final List<UUID> uuidList = new ArrayList<>(parts.length - partIndex);

      for (int i = 1; i < parts.length; i++) {
         uuidList.add(UUID.fromString(parts[i]));
      }

      if (uuidList.size() < 1) {
         throw new IllegalStateException("No uuids specified in: " + externalString);
      }

      this.uuids = uuidList.toArray(new UUID[uuidList.size()]);
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param fullySpecifiedDescriptionText the fullySpecifiedDescriptionText
    * @param uuidString the uuid string.
    */
   public ConceptProxy(String fullySpecifiedDescriptionText, String uuidString) {
      this(fullySpecifiedDescriptionText, UUID.fromString(uuidString));
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param fullySpecifiedDescriptionText the fullySpecifiedDescriptionText
    * @param uuids the uuids
    */
   public ConceptProxy(String fullySpecifiedDescriptionText, UUID... uuids) {
      this.uuids = uuids;
      this.fullySpecifiedDescriptionText = fullySpecifiedDescriptionText;
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param fullySpecifiedDescriptionText the fullySpecifiedDescriptionText
    * @param preferredDescriptionText
    * @param uuidStrings the uuid strings
    */
   public ConceptProxy(String fullySpecifiedDescriptionText, String preferredDescriptionText, String... uuidStrings) {
      this.uuids = Arrays.stream(uuidStrings)
              .map(uuidString -> UUID.fromString(uuidString))
              .toArray(UUID[]::new);
      this.fullySpecifiedDescriptionText = fullySpecifiedDescriptionText;
      this.preferredDescriptionText = preferredDescriptionText;
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param fullySpecifiedDescriptionText the fullySpecifiedDescriptionText
    * @param preferredDescriptionText
    * @param uuids the uuids
    */
   public ConceptProxy(String fullySpecifiedDescriptionText, String preferredDescriptionText, UUID... uuids) {
      this.uuids = uuids;
      this.fullySpecifiedDescriptionText = fullySpecifiedDescriptionText;
      this.preferredDescriptionText = preferredDescriptionText;
   }

   //~--- methods -------------------------------------------------------------
   /**
    * Equals.
    *
    * @param obj the obj
    * @return true, if successful
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj instanceof ConceptSpecification) {

         final ConceptSpecification other = (ConceptSpecification) obj;

         if (obj instanceof ConceptProxy) {
            ConceptProxy proxy = (ConceptProxy) obj;
            return Arrays.stream(this.uuids).anyMatch((UUID objUuid) -> {
               return Arrays.stream(proxy.uuids).anyMatch((otherUuid) -> {
                  return objUuid.equals(otherUuid);
               });
            });
         }
         return this.getNid() == other.getNid();
      }
      return false;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 5;

      hash = 79 * hash + Arrays.deepHashCode(this.uuids);
      hash = 79 * hash + Objects.hashCode(this.fullySpecifiedDescriptionText);
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      if (this.uuids != null) {
         return "ConceptProxy{" + this.fullySpecifiedDescriptionText + "; " + Arrays.asList(this.uuids) + "}";
      }

      return "ConceptProxy{" + this.fullySpecifiedDescriptionText + "; null UUIDs}";
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the concept fullySpecifiedDescriptionText text.
    *
    * @return the concept fullySpecifiedDescriptionText text
    */
   @Override
   public String getFullySpecifiedConceptDescriptionText() {
      return this.fullySpecifiedDescriptionText;
   }

   /**
    * Gets the concept sequence.
    *
    * @param nid the nid
    * @return the concept sequence
    */
   protected static int getConceptSequence(int nid) {
      return Get.identifierService()
              .getConceptSequence(nid);
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set a fullySpecifiedDescriptionText of the concept proxied by this object.
    *
    * @param fullySpecifiedDescriptionText the new a fullySpecifiedDescriptionText of the concept proxied by this object
    */
   public void setFullySpecifiedDescriptionText(String fullySpecifiedDescriptionText) {
      this.fullySpecifiedDescriptionText = fullySpecifiedDescriptionText;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() {
      return Get.identifierService()
              .getNidForUuids(this.uuids);
   }

   /**
    * Added as an alternative way to get the primary UUID - since most users of a concept spec only have one UUID, and
    * only care about one UUID.
    *
    * @return the first UUID in the UUID list, or null, if not present
    */
   @XmlTransient
   @Override
   public UUID getPrimordialUuid() {
      if ((this.uuids == null) || (this.uuids.length < 1)) {
         return null;
      } else {
         return this.uuids[0];
      }
   }

   /**
    * Gets the uuid list.
    *
    * @return the uuid list
    */
   @Override
   public List<UUID> getUuidList() {
      return Arrays.asList(this.uuids);
   }

   /**
    * Gets the universal identifiers for the concept proxied by the is object.
    *
    * @return the universal identifiers for the concept proxied by the is object
    */
   @Override
   @XmlTransient
   public UUID[] getUuids() {
      return this.uuids;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Set universal identifiers for the concept proxied by the is object.
    *
    * @param uuids the new universal identifiers for the concept proxied by the is object
    */
   public void setUuids(UUID[] uuids) {
      this.uuids = uuids;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * added as an alternative way to get the uuids as strings rather than UUID objects this was done to help with Maven
    * making use of this class.
    *
    * @return the uuids as string
    */
   public String[] getUuidsAsString() {
      final String[] returnVal = new String[this.uuids.length];
      int i = 0;

      for (final UUID uuid : this.uuids) {
         returnVal[i++] = uuid.toString();
      }

      return returnVal;
   }

   //~--- set methods ---------------------------------------------------------
   /**
    * Added primarily for Maven so that using a String type configuration in a POM file the UUIDs array could be set.
    * This allows the ConceptSpec class to be embedded into a object to be configured by Maven POM configuration.
    *
    * @param uuids the new uuids as string
    */
   public void setUuidsAsString(String[] uuids) {
      this.uuids = new UUID[uuids.length];

      int i = 0;

      for (final String uuid : uuids) {
         this.uuids[i++] = UUID.fromString(uuid);
      }
   }

   @Override
   public Optional<String> getPreferedConceptDescriptionText() {
      if (this.preferredDescriptionText == null) {
         this.preferredDescriptionText = Get.defaultCoordinate().getPreferredDescriptionText(this);
      }
      return Optional.ofNullable(this.preferredDescriptionText);
   }

   public Optional<String> getPreferedConceptDescriptionTextNoLookup() {
      return Optional.ofNullable(this.preferredDescriptionText);
   }
}
