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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import sh.isaac.api.ConceptProxyLazy;
import sh.isaac.api.Get;
import sh.isaac.api.LanguageCoordinateService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.configuration.LanguageCoordinates;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;

/**
 * The Class LanguageCoordinateImpl.
 *
 * @author kec
 */
public class LanguageCoordinateImpl
         implements LanguageCoordinate {
   /** The language concept nid. */
   ConceptSpecification languageConcept;

   /** The dialect assemblage preference list. */
   int[] dialectAssemblagePreferenceList;

   /** The description type preference list. */
   int[] descriptionTypePreferenceList;
   
   int[] modulePreferenceList;
   
   private HashMap<Integer, LanguageCoordinate> altDescriptionTypeListCache = new HashMap<>();
   
   LanguageCoordinateImpl nextProrityLanguageCoordinate;

   /**
    * Instantiates a new language coordinate impl.
    *
    * @param languageConcept the language concept id
    * @param dialectAssemblagePreferenceList the dialect assemblage preference list
    * @param descriptionTypePreferenceList the description type preference list
    * @param modulePreferenceList the module preference list.  See {@link LanguageCoordinate#getModulePreferenceListForLanguage()}
    */
   public LanguageCoordinateImpl(ConceptSpecification languageConcept,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList,
                                 int[] modulePreferenceList) {
      this.languageConcept         = languageConcept;
      this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;
      this.descriptionTypePreferenceList = descriptionTypePreferenceList;
      this.modulePreferenceList = modulePreferenceList;
   }
   
   /**
    * Instantiates a new language coordinate impl, with an unspecified set of modulePreferences.
    *
    * @param languageConcept the language concept
    * @param dialectAssemblagePreferenceList the dialect assemblage preference list
    * @param descriptionTypePreferenceList the description type preference list
    */
   public LanguageCoordinateImpl(ConceptSpecification languageConcept,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList) {
      this(languageConcept, dialectAssemblagePreferenceList, descriptionTypePreferenceList, new int[] {});
   }
   
   /**
    * Instantiates a new language coordinate impl, with an unspecified set of modulePreferences.
    *
    * @param languageConcept the language concept
    * @param dialectAssemblagePreferenceList the dialect assemblage preference list
    * @param descriptionTypePreferenceList the description type preference list
    */
   public LanguageCoordinateImpl(int languageConcept,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList) {
      this(new ConceptProxyLazy(languageConcept), dialectAssemblagePreferenceList, descriptionTypePreferenceList, new int[] {});
   }

    @Override
    public ConceptSpecification getLanguageConcept() {
        return languageConcept;
    }

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

      final LanguageCoordinateImpl other = (LanguageCoordinateImpl) obj;

      if (this.languageConcept.getNid() != other.languageConcept.getNid()) {
         return false;
      }

      if (!Arrays.equals(this.dialectAssemblagePreferenceList, other.dialectAssemblagePreferenceList)) {
         return false;
      }
      
      if ((modulePreferenceList == null && other.modulePreferenceList != null) 
            || (modulePreferenceList != null && other.modulePreferenceList == null)
            || modulePreferenceList != null && !Arrays.equals(this.modulePreferenceList, other.modulePreferenceList)) {
         return false;
      }

      return Arrays.equals(this.descriptionTypePreferenceList, other.descriptionTypePreferenceList);
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int hash = 3;

      hash = 79 * hash + this.languageConcept.getNid();
      hash = 79 * hash + Arrays.hashCode(this.dialectAssemblagePreferenceList);
      hash = 79 * hash + Arrays.hashCode(this.descriptionTypePreferenceList);
      hash = 79 * hash + (this.modulePreferenceList == null ? 0 : Arrays.hashCode(this.modulePreferenceList));
      return hash;
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "Language Coordinate{" + Get.conceptDescriptionText(this.languageConcept.getNid()) +
             ", dialect preference: " + Get.conceptDescriptionTextList(this.dialectAssemblagePreferenceList) +
             ", type preference: " + Get.conceptDescriptionTextList(this.descriptionTypePreferenceList) +
             ", module preference: " + Get.conceptDescriptionTextList(this.modulePreferenceList)+ '}';
   }

   /**
    * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescription(java.util.List, sh.isaac.api.coordinate.StampCoordinate)
    * Implemented via {@link LanguageCoordinateService#getSpecifiedDescription(StampCoordinate, List, LanguageCoordinate)}
    */
   @Override
   public LatestVersion<DescriptionVersion> getDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate, descriptionList, this);
   }
   
   
   /**
    * 
    * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescription(int, int[], sh.isaac.api.coordinate.StampCoordinate)
    */
   @Override
   public LatestVersion<DescriptionVersion> getDescription(int conceptNid, int[] descriptionTypePreference, StampCoordinate stampCoordinate) {
      Integer key = Arrays.hashCode(descriptionTypePreference);
      LanguageCoordinate lc = altDescriptionTypeListCache.get(key);
      if (lc == null) {
         lc = this.cloneAndChangeDescriptionType(descriptionTypePreference);
         altDescriptionTypeListCache.put(key, lc);
      }
      return lc.getDescription(conceptNid, stampCoordinate);
   }

   /**
    * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescription(java.util.List, int[], sh.isaac.api.coordinate.StampCoordinate)
    */
    @Override
   public LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, int[] descriptionTypePreference,
         StampCoordinate stampCoordinate) {
      Integer key = Arrays.hashCode(descriptionTypePreference);
      LanguageCoordinate lc = altDescriptionTypeListCache.get(key);
      if (lc == null) {
         lc = this.cloneAndChangeDescriptionType(descriptionTypePreference);
         altDescriptionTypeListCache.put(key, lc);
      }
      return lc.getDescription(descriptionList, stampCoordinate);
   }

   /**
    * Gets the description type preference list.
    *
    * @return the description type preference list
    */
   @Override
   public int[] getDescriptionTypePreferenceList() {
      return this.descriptionTypePreferenceList;
   }

   public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      this.descriptionTypePreferenceList = descriptionTypePreferenceList;
      //Don't need to clear altDescriptionTypeListCache here, because its ignored anyway
   }
   
   /**
    * Same as {@link #setDescriptionTypePreferenceList(int[])}, except it also makes the same 
    * call recursively on the preference list in {@link #getNextProrityLanguageCoordinate()}, if any.
    * @param descriptionTypePreferenceList
    */
   public void setDescriptionTypePreferenceListRecursive(int[] descriptionTypePreferenceList) {
      this.descriptionTypePreferenceList = descriptionTypePreferenceList;
      if (getNextProrityLanguageCoordinate().isPresent()) {
         ((LanguageCoordinateImpl)getNextProrityLanguageCoordinate().get()).setDescriptionTypePreferenceListRecursive(descriptionTypePreferenceList);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the dialect assemblage preference list.
    *
    * @return the dialect assemblage preference list
    */
   @Override
   public int[] getDialectAssemblagePreferenceList() {
      return this.dialectAssemblagePreferenceList;
   }

   public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;
      altDescriptionTypeListCache.clear();
   }

    public void setLanguageConceptNid(int languageConceptNid) {
        this.languageConcept = Get.conceptSpecification(languageConceptNid);
    }
    public void setLanguageConcept(ConceptSpecification languageConcept) {
        this.languageConcept = languageConcept;
    }

   @Override
   public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return getDescription(descriptionList, new int[] {TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
   }

   /**
    * Gets the language concept nid.
    *
    * @return the language concept nid
    */
   @Override
   public int getLanguageConceptNid() {
      return this.languageConcept.getNid();
   }

   public ChangeListener<ObservableLanguageCoordinate> setNextProrityLanguageCoordinateProperty(
           ObjectProperty<ObservableLanguageCoordinate> nextProrityLanguageCoordinateProperty) {
       
      final ChangeListener<ObservableLanguageCoordinate> listener = (ObservableValue<? extends ObservableLanguageCoordinate> observable,
                                               ObservableLanguageCoordinate oldValue,
                                               ObservableLanguageCoordinate newValue) -> {
               this.nextProrityLanguageCoordinate = ((ObservableLanguageCoordinateImpl)newValue).unwrap();
            };

      nextProrityLanguageCoordinateProperty.addListener(new WeakChangeListener<>(listener));
      altDescriptionTypeListCache.clear();
      return listener;
   }

   @Override
   public LatestVersion<DescriptionVersion> getPreferredDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return getDescription(descriptionList, new int[] {TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
   }

   @Override
   public LatestVersion<DescriptionVersion> getDefinitionDescription(
           List<SemanticChronology> descriptionList,
           StampCoordinate stampCoordinate) {
      return getDescription(descriptionList, new int[] {TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()}, stampCoordinate);
   }

   @Override
   public LanguageCoordinateImpl deepClone() {
      LanguageCoordinateImpl newCoordinate = new LanguageCoordinateImpl(languageConcept,
                                 dialectAssemblagePreferenceList.clone(),
                                 descriptionTypePreferenceList.clone(),
                                 modulePreferenceList == null ? null : modulePreferenceList.clone());
      if (this.nextProrityLanguageCoordinate != null) {
          newCoordinate.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) this.nextProrityLanguageCoordinate.deepClone();
      }
      return newCoordinate;
   }

    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return Optional.ofNullable(this.nextProrityLanguageCoordinate);
    }

    public void setNextProrityLanguageCoordinate(LanguageCoordinate languageCoordinate) {
        this.nextProrityLanguageCoordinate = (LanguageCoordinateImpl) languageCoordinate;
        altDescriptionTypeListCache.clear();
    }

    @Override
    public int[] getModulePreferenceListForLanguage() {
        return modulePreferenceList;
    }
    
    /**
     * Clone this coordinate, and change the description types list to the new list, recursively.
     * Also expands description types
     * @param descriptionTypes
     * @return
     */
    private LanguageCoordinate cloneAndChangeDescriptionType(int[] descriptionTypes) {
        LanguageCoordinateImpl lci = deepClone();
        lci.setDescriptionTypePreferenceListRecursive(LanguageCoordinates.expandDescriptionTypePreferenceList(descriptionTypes, null));
        return lci;
    }
}
