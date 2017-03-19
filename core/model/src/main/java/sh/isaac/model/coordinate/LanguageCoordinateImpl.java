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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableIntegerArray;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DescriptionSememe;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@XmlRootElement(name = "languageCoordinate")
@XmlAccessorType(XmlAccessType.FIELD)
public class LanguageCoordinateImpl
         implements LanguageCoordinate {
   int   languageConceptSequence;
   int[] dialectAssemblagePreferenceList;
   int[] descriptionTypePreferenceList;

   //~--- constructors --------------------------------------------------------

   private LanguageCoordinateImpl() {
      // for jaxb
   }

   public LanguageCoordinateImpl(int languageConceptId,
                                 int[] dialectAssemblagePreferenceList,
                                 int[] descriptionTypePreferenceList) {
      this.languageConceptSequence         = Get.identifierService()
            .getConceptSequence(languageConceptId);
      this.dialectAssemblagePreferenceList = dialectAssemblagePreferenceList;

      for (int i = 0; i < this.dialectAssemblagePreferenceList.length; i++) {
         this.dialectAssemblagePreferenceList[i] = Get.identifierService()
               .getConceptSequence(this.dialectAssemblagePreferenceList[i]);
      }

      this.descriptionTypePreferenceList = descriptionTypePreferenceList;

      for (int i = 0; i < this.descriptionTypePreferenceList.length; i++) {
         this.descriptionTypePreferenceList[i] = Get.identifierService()
               .getConceptSequence(this.descriptionTypePreferenceList[i]);
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      final LanguageCoordinateImpl other = (LanguageCoordinateImpl) obj;

      if (this.languageConceptSequence != other.languageConceptSequence) {
         return false;
      }

      if (!Arrays.equals(this.dialectAssemblagePreferenceList, other.dialectAssemblagePreferenceList)) {
         return false;
      }

      return Arrays.equals(this.descriptionTypePreferenceList, other.descriptionTypePreferenceList);
   }

   @Override
   public int hashCode() {
      int hash = 3;

      hash = 79 * hash + this.languageConceptSequence;
      hash = 79 * hash + Arrays.hashCode(this.dialectAssemblagePreferenceList);
      return hash;
   }

   @Override
   public String toString() {
      return "Language Coordinate{" + Get.conceptDescriptionText(languageConceptSequence) + ", dialect preference: " +
             Get.conceptDescriptionTextList(dialectAssemblagePreferenceList) + ", type preference: " +
             Get.conceptDescriptionTextList(descriptionTypePreferenceList) + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getDescription(
           List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate, descriptionList, this);
   }

   @Override
   public int[] getDescriptionTypePreferenceList() {
      return descriptionTypePreferenceList;
   }

   //~--- set methods ---------------------------------------------------------

   public ArrayChangeListener<ObservableIntegerArray> setDescriptionTypePreferenceListProperty(
           ObjectProperty<ObservableIntegerArray> descriptionTypePreferenceListProperty) {
      ArrayChangeListener<ObservableIntegerArray> listener = (ObservableIntegerArray observableArray,
                                                              boolean sizeChanged,
                                                              int from,
                                                              int to) -> {
               descriptionTypePreferenceList = observableArray.toArray(descriptionTypePreferenceList);
            };

      descriptionTypePreferenceListProperty.getValue()
            .addListener(new WeakArrayChangeListener(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int[] getDialectAssemblagePreferenceList() {
      return dialectAssemblagePreferenceList;
   }

   //~--- set methods ---------------------------------------------------------

   public ArrayChangeListener<ObservableIntegerArray> setDialectAssemblagePreferenceListProperty(
           ObjectProperty<ObservableIntegerArray> dialectAssemblagePreferenceListProperty) {
      ArrayChangeListener<ObservableIntegerArray> listener = (ObservableIntegerArray observableArray,
                                                              boolean sizeChanged,
                                                              int from,
                                                              int to) -> {
               dialectAssemblagePreferenceList = observableArray.toArray(dialectAssemblagePreferenceList);
            };

      dialectAssemblagePreferenceListProperty.getValue()
            .addListener(new WeakArrayChangeListener(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getFullySpecifiedDescription(
           List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate,
                                         descriptionList,
                                         Get.languageCoordinateService()
                                               .getFullySpecifiedConceptSequence(),
                                         this);
   }

   @Override
   public int getLanguageConceptSequence() {
      return languageConceptSequence;
   }

   //~--- set methods ---------------------------------------------------------

   public ChangeListener<Number> setLanguageConceptSequenceProperty(IntegerProperty languageConceptSequenceProperty) {
      ChangeListener<Number> listener = (ObservableValue<? extends Number> observable,
                                         Number oldValue,
                                         Number newValue) -> {
                                           languageConceptSequence = newValue.intValue();
                                        };

      languageConceptSequenceProperty.addListener(new WeakChangeListener<>(listener));
      return listener;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Optional<LatestVersion<DescriptionSememe<?>>> getPreferredDescription(
           List<SememeChronology<? extends DescriptionSememe<?>>> descriptionList,
           StampCoordinate stampCoordinate) {
      return Get.languageCoordinateService()
                .getSpecifiedDescription(stampCoordinate,
                                         descriptionList,
                                         Get.languageCoordinateService()
                                               .getSynonymConceptSequence(),
                                         this);
   }
}

