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



package sh.isaac.model.coordinate;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LogicCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ManifoldCoordinateImpl.
 *
 * @author kec
 */
public class ManifoldCoordinateImpl
         implements ManifoldCoordinate {

   /** The taxonomy type. */
   PremiseType taxonomyPremiseType;

   /** The stamp coordinate. */
   StampCoordinate stampCoordinate;

   /** The language coordinate. */
   LanguageCoordinate languageCoordinate;

   /** The logic coordinate. */
   LogicCoordinate logicCoordinate;

   /** The uuid. */
   UUID uuid = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new taxonomy coordinate impl.
    */
   private ManifoldCoordinateImpl() {
      // for jaxb
   }

   /**
    * Instantiates a new taxonomy coordinate impl.
    *
    * @param taxonomyType the taxonomy type
    * @param stampCoordinate the stamp coordinate
    * @param languageCoordinate the language coordinate
    * @param logicCoordinate the logic coordinate
    */
   public ManifoldCoordinateImpl(PremiseType taxonomyType,
                                 StampCoordinate stampCoordinate,
                                 LanguageCoordinate languageCoordinate,
                                 LogicCoordinate logicCoordinate) {
      this.taxonomyPremiseType       = taxonomyType;
      this.stampCoordinate    = stampCoordinate;
      this.languageCoordinate = languageCoordinate;
      this.logicCoordinate    = logicCoordinate;
      //this.uuid               //lazy load
   }
   
   /**
    * Instantiates a new taxonomy coordinate impl.  Calls {@link #ManifoldCoordinateImpl(PremiseType, StampCoordinate, LanguageCoordinate, LogicCoordinate)}
    * with a {@link PremiseType#STATED} and the default Logic Coordinate from {@link ConfigurationService}
    *
    * @param stampCoordinate the stamp coordinate
    * @param languageCoordinate - optional - uses default if not provided.  the language coordinate
    */
   public ManifoldCoordinateImpl(StampCoordinate stampCoordinate,
                                 LanguageCoordinate languageCoordinate) {
      this(PremiseType.STATED, stampCoordinate, 
          languageCoordinate == null ? Get.configurationService().getUserConfiguration(Optional.empty()).getLanguageCoordinate() : languageCoordinate,
          Get.configurationService().getUserConfiguration(Optional.empty()).getLogicCoordinate());
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

      if (getClass() != obj.getClass()) {
         return false;
      }

      final ManifoldCoordinateImpl other = (ManifoldCoordinateImpl) obj;

      if (this.taxonomyPremiseType != other.taxonomyPremiseType) {
         return false;
      }

      if (!Objects.equals(this.stampCoordinate, other.stampCoordinate)) {
         return false;
      }

      if (!Objects.equals(this.logicCoordinate, other.logicCoordinate)) {
         return false;
      }

      return Objects.equals(this.languageCoordinate, other.languageCoordinate);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 53 * hash + Objects.hashCode(this.taxonomyPremiseType);
      hash = 53 * hash + Objects.hashCode(this.stampCoordinate);
      hash = 53 * hash + Objects.hashCode(this.languageCoordinate);
      return hash;
   }

   /**
    * Make analog.
    *
    * @param stampPositionTime the stamp position time
    * @return the taxonomy coordinate impl
    */
   @Override
   public ManifoldCoordinateImpl makeCoordinateAnalog(long stampPositionTime) {
      return new ManifoldCoordinateImpl(this.taxonomyPremiseType,
                                        this.stampCoordinate.makeCoordinateAnalog(stampPositionTime),
                                        this.languageCoordinate,
                                        this.logicCoordinate);
   }

   /**
    * Make analog.
    *
    * @param taxonomyType the taxonomy type
    * @return the taxonomy coordinate impl
    */
   @Override
   public ManifoldCoordinateImpl makeCoordinateAnalog(PremiseType taxonomyType) {
      return new ManifoldCoordinateImpl(taxonomyType,
                                        this.stampCoordinate,
                                        this.languageCoordinate,
                                        this.logicCoordinate);
   }

   /**
    * Make analog.
    *
    * @param state the state
    * @return the taxonomy coordinate impl
    */
   @Override
   public ManifoldCoordinateImpl makeCoordinateAnalog(Status... state) {
      return new ManifoldCoordinateImpl(this.taxonomyPremiseType,
                                        this.stampCoordinate.makeCoordinateAnalog(state),
                                        this.languageCoordinate,
                                        this.logicCoordinate);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ManifoldCoordinateImpl{" + this.taxonomyPremiseType + ",\n" + this.stampCoordinate + ", \n" +
             this.languageCoordinate + ", \n" + this.logicCoordinate + ", uuid=" + getCoordinateUuid() + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the language coordinate.
    *
    * @return the language coordinate
    */
   @Override
   public LanguageCoordinate getLanguageCoordinate() {
      return this.languageCoordinate;
   }

   /**
    * Gets the logic coordinate.
    *
    * @return the logic coordinate
    */
   @Override
   public LogicCoordinate getLogicCoordinate() {
      return this.logicCoordinate;
   }

   /**
    * Gets the stamp coordinate.
    *
    * @return the stamp coordinate
    */
   @Override
   public StampCoordinate getStampCoordinate() {
      return this.stampCoordinate;
   }

   /**
    * Gets the taxonomy type.
    *
    * @return the taxonomy type
    */
   @Override
   public PremiseType getTaxonomyPremiseType() {
      return this.taxonomyPremiseType;
   }
   public void setTaxonomyPremiseType(PremiseType taxonomyPremiseType) {
       this.taxonomyPremiseType = taxonomyPremiseType;
   }
   /**
    * Gets the uuid.
    *
    * @return the uuid
    */
   @Override
   public UUID getCoordinateUuid() {
      if (this.uuid == null) {
         uuid = UUID.randomUUID();
      }
      return this.uuid;
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class AnyTypeAdapter.
    */
   private static class AnyTypeAdapter
           extends XmlAdapter<Object, Object> {
      /**
       * Marshal.
       *
       * @param v the v
       * @return the object
       */
      @Override
      public Object marshal(Object v) {
         return v;
      }

      /**
       * Unmarshal.
       *
       * @param v the v
       * @return the object
       */
      @Override
      public Object unmarshal(Object v) {
         return v;
      }
   }
   
   @Override
   public ManifoldCoordinateImpl deepClone() {
      ManifoldCoordinateImpl newCoordinate = new ManifoldCoordinateImpl(taxonomyPremiseType,
                                 stampCoordinate.deepClone(),
                                 languageCoordinate.deepClone(),
                                 logicCoordinate.deepClone());
      return newCoordinate;
   }

    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return languageCoordinate.getNextProrityLanguageCoordinate();
    }

    @Override
    public LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
        return languageCoordinate.getDefinitionDescription(descriptionList, stampCoordinate);
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return languageCoordinate.getModulePreferenceListForLanguage();
    }

    @Override
    public List<ConceptSpecification> getModulePreferenceOrderForVersions() {
        return stampCoordinate.getModulePreferenceOrderForVersions();
    }

    @Override
    public Set<ConceptSpecification> getModuleSpecifications() {
        return stampCoordinate.getModuleSpecifications();
    }
    
    
}

