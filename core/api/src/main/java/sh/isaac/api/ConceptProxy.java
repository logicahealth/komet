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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

//~--- non-JDK imports --------------------------------------------------------
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.api.util.StringUtils;
import sh.isaac.api.util.UUIDUtil;

//~--- classes ----------------------------------------------------------------
/**
 * Created by kec on 2/16/15.
 */
@XmlRootElement(name = "Concept")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"fullyQualfiedName", "uuids"})
public class ConceptProxy
        implements ConceptSpecification { 

   public static final String METADATA_SEMANTIC_TAG = "SOLOR";
   
   /**
    * Universal identifiers for the concept proxied by the is object.
    */
   @XmlAttribute(name = "uuids", required = true)
   private UUID[] uuids;

   /**
    * The fully qualified name for this object.
    */
   @XmlAttribute(name = "fqn", required = true)
   private String fullyQualfiedName;

   /**
    * The regular name for this object.
    */
    @XmlTransient
   private Optional<String> regularName = null;  //leave null, so we know if we have done a lookup or not
   
     @XmlTransient
   private int cachedNid = 0;

   //~--- constructors --------------------------------------------------------
   /**
    * Instantiates a new concept proxy.
    */
   public ConceptProxy() {
   }
   public ConceptProxy(ConceptSpecification spec) {
       this(spec.toExternalString());
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param conceptNid the concept nid
    */
   public ConceptProxy(int conceptNid) {
       this.cachedNid = conceptNid;
   }

   /**
    * Instantiates a new concept proxy.
    *
    * @param externalString the external string
    */
   public ConceptProxy(String externalString) {

      final String[] parts = StringUtils.split(externalString, FIELD_SEPARATOR);

      int partIndex = 0;

      this.fullyQualfiedName = parts[partIndex++];

      if (!UUIDUtil.isUUID(parts[partIndex])) {
         this.regularName = Optional.of(parts[partIndex++]);
      }

      final List<UUID> uuidList = new ArrayList<>(parts.length - partIndex);

      for (int i = partIndex; i < parts.length; i++) {
         uuidList.add(UUID.fromString(parts[i]));
      }

      if (uuidList.size() < 1) {
         throw new IllegalStateException("No uuids specified in: " + externalString);
      }

      this.uuids = uuidList.toArray(new UUID[uuidList.size()]);
   }

   /**
    * Instantiates a new concept proxy.  Semantic tags and the regular name are set 
    * via the rules of {@link #setFullyQualfiedName(String)}
    *
    * @param fullySpecifiedDescriptionText the fullySpecifiedDescriptionText
    * @param uuids the uuids
    */
   public ConceptProxy(String fullySpecifiedDescriptionText, UUID... uuids) {
      this.uuids = uuids;
      setFullyQualfiedName(fullySpecifiedDescriptionText);
   }

   /**
    * Instantiates a new concept proxy.  fullyQualifiedName and regularName are treated per the rules 
    * of their setters, when it comes to semantic tags (adding or removing tags as necessary)
    *
    * @param fullyQualifiedName the regularName
    * @param regularName
    * @param uuids the uuids
    */
   public ConceptProxy(String fullyQualifiedName, String regularName, UUID... uuids) {
      this.uuids = uuids;
      if (UUIDUtil.isUUID(regularName)) {
         throw new IllegalStateException("perferredDescription cannot be a uuid for: " + regularName);
      }
      setFullyQualfiedName(fullyQualifiedName);
      setRegularName(regularName);
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
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }

      if (obj instanceof ConceptSpecification) {

         final ConceptSpecification other = (ConceptSpecification) obj;

         if (obj instanceof ConceptProxy) {
            ConceptProxy proxy = (ConceptProxy) obj;
            return Arrays.stream(this.getUuids()).anyMatch((UUID objUuid) -> {
               return Arrays.stream(proxy.getUuids()).anyMatch((otherUuid) -> {
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
      if (Get.identifierService() != null) {
         return Long.hashCode(this.getNid());
      }
      int hash = 5;

      hash = 79 * hash + Arrays.deepHashCode(this.getUuids());
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      if (this.getUuids() != null) {
          StringBuilder builder = new StringBuilder();
          builder.append("ConceptProxy(\"");
          builder.append(this.fullyQualfiedName);
          builder.append("\", ");
          int count = 0;
          for (UUID uuid: this.uuids) {
              builder.append("UUID.fromString(\"");
              builder.append(uuid.toString());
              builder.append("\")");
              count++;
              if (count < this.uuids.length) {
                  builder.append(", ");
              }
          }
          builder.append(")");
          return  builder.toString();
      }

      return "ConceptProxy(\"" + this.fullyQualfiedName + ", null UUIDs)";
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the concept fullySpecifiedDescriptionText text.
    *
    * @return the concept fullySpecifiedDescriptionText text
    */
   @Override
   public String getFullyQualifiedName() {
      if (this.fullyQualfiedName == null) {
         this.fullyQualfiedName = Get.defaultCoordinate().getFullySpecifiedDescriptionText(this);
      }
      return this.fullyQualfiedName;
   }

   //~--- get methods ---------------------------------------------------------
   /**
    * Gets the nid.
    *
    * @return the nid
    */
   @Override
   public int getNid() throws NoSuchElementException {
      if (cachedNid == 0) {
         try {
          cachedNid = Get.identifierService().getNidForUuids(getPrimordialUuid());
         }
         catch (NoSuchElementException e) {
            //This it to help me bootstrap the system... normally, all metadata will be pre-assigned by the IdentifierProvider upon startup.
            cachedNid = Get.identifierService().assignNid(getUuids());
         }
      }
      return cachedNid;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void clearCache() {
      cachedNid = 0;
      ConceptSpecification.super.clearCache();
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
      if ((this.getUuids() == null) || (this.uuids.length < 1)) {
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
      return Arrays.asList(this.getUuids());
   }

   /**
    * Gets the universal identifiers for the concept proxied by the is object.
    *
    * @return the universal identifiers for the concept proxied by the is object
    */
   @Override
   @XmlTransient
   public UUID[] getUuids() {
       if (this.uuids == null) {
           this.uuids = Get.identifierService().getUuidArrayForNid(cachedNid);
       }
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
      final String[] returnVal = new String[this.getUuids().length];
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
   
   /**
    * Set the fully qualified name, adding the {@link #METADATA_SEMANTIC_TAG} if no semantic tag is present.
    * If {@link #setRegularName(String)} has not yet been set (via this call, or the constructor) then the regular
    * name will be set to the fully qualified name, minus the semantic tag.
    * @param fullyQualfiedName the fullyQualfiedName to set
    */
   public final void setFullyQualfiedName(String fullyQualfiedName) {
      this.fullyQualfiedName = SemanticTags.addSemanticTagIfAbsent(fullyQualfiedName, METADATA_SEMANTIC_TAG);
      if (this.regularName == null) {
         this.regularName = Optional.of(SemanticTags.stripSemanticTagIfPresent(this.fullyQualfiedName));
      }
   }

   /**
    * @param regularName the regularName to set
    * If the passed regular name contains a semantic tag, it will be stripped.
    */
   public final void setRegularName(String regularName) {
      this.regularName = Optional.ofNullable(SemanticTags.stripSemanticTagIfPresent(regularName));
   }

   @Override
   public Optional<String> getRegularName() {
      if (this.regularName == null) {
         this.regularName = Get.defaultCoordinate().getRegularName(this);
      }
      return this.regularName;
   }

   public Optional<String> getRegularNameNoLookup() {
      return this.regularName == null ? Optional.empty() : this.regularName;
   }
}
