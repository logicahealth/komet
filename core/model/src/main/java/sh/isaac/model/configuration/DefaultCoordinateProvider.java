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



package sh.isaac.model.configuration;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

//~--- non-JDK imports --------------------------------------------------------

import javafx.collections.ObservableIntegerArray;

import sh.isaac.api.Get;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableTaxonomyCoordinate;
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampPositionImpl;
import sh.isaac.model.observable.coordinate.ObservableTaxonomyCoordinateImpl;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class DefaultCoordinateProvider {
   AtomicBoolean                defaultsSetup      = new AtomicBoolean();
   CountDownLatch               defaultsSetupLatch = new CountDownLatch(1);
   ObservableEditCoordinate     observableEditCoordinate;
   ObservableLanguageCoordinate observableLanguageCoordinate;
   ObservableLogicCoordinate    observableLogicCoordinate;
   ObservableStampCoordinate    observableStampCoordinate;
   ObservableStampPositionImpl  observableStampPosition;
   ObservableTaxonomyCoordinate observableTaxonomyCoordinate;

   //~--- methods -------------------------------------------------------------

   private void setupDefaults() {
      try {
         if (defaultsSetup.compareAndSet(false, true)) {
            observableEditCoordinate = new ObservableEditCoordinateImpl(EditCoordinates.getDefaultUserSolorOverlay());
            observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
            observableLogicCoordinate = new ObservableLogicCoordinateImpl(LogicCoordinates.getStandardElProfile());
            observableStampCoordinate =
               new ObservableStampCoordinateImpl(StampCoordinates.getDevelopmentLatestActiveOnly());
            observableStampPosition =
               new ObservableStampPositionImpl(StampCoordinates.getDevelopmentLatestActiveOnly().getStampPosition());
            observableTaxonomyCoordinate = new ObservableTaxonomyCoordinateImpl(
                TaxonomyCoordinates.getInferredTaxonomyCoordinate(observableStampCoordinate,
                      observableLanguageCoordinate,
                      observableLogicCoordinate));
            observableStampCoordinate.stampPositionProperty()
                                     .setValue(observableStampPosition);
            defaultsSetupLatch.countDown();
         }

         defaultsSetupLatch.await();
      } catch (InterruptedException ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefaultClassifier(int conceptId) {
      setupDefaults();
      observableLogicCoordinate.classifierSequenceProperty()
                               .set(Get.identifierService()
                                       .getConceptSequence(conceptId));
   }

   public void setDefaultDescriptionLogicProfile(int conceptId) {
      setupDefaults();
      observableLogicCoordinate.descriptionLogicProfileSequenceProperty()
                               .set(Get.identifierService()
                                       .getConceptSequence(conceptId));
   }

   public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      setupDefaults();

      for (int i = 0; i < descriptionTypePreferenceList.length; i++) {
         descriptionTypePreferenceList[i] = Get.identifierService()
               .getConceptSequence(descriptionTypePreferenceList[i]);
      }

      ObservableIntegerArray descriptionTypeIntegerArray =
         observableLanguageCoordinate.descriptionTypePreferenceListProperty()
                                     .get();

      descriptionTypeIntegerArray.clear();
      descriptionTypeIntegerArray.addAll(descriptionTypePreferenceList);
      observableLanguageCoordinate.descriptionTypePreferenceListProperty()
                                  .set(descriptionTypeIntegerArray);
   }

   public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      setupDefaults();

      for (int i = 0; i < dialectAssemblagePreferenceList.length; i++) {
         dialectAssemblagePreferenceList[i] = Get.identifierService()
               .getConceptSequence(dialectAssemblagePreferenceList[i]);
      }

      ObservableIntegerArray dialectAssemblageIntegerArray =
         observableLanguageCoordinate.dialectAssemblagePreferenceListProperty()
                                     .get();

      dialectAssemblageIntegerArray.clear();
      dialectAssemblageIntegerArray.addAll(dialectAssemblagePreferenceList);
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableEditCoordinate getDefaultEditCoordinate() {
      setupDefaults();
      return observableEditCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefaultInferredAssemblage(int conceptId) {
      setupDefaults();
      observableLogicCoordinate.inferredAssemblageSequenceProperty()
                               .set(Get.identifierService()
                                       .getConceptSequence(conceptId));
   }

   public void setDefaultLanguage(int conceptId) {
      setupDefaults();
      observableLanguageCoordinate.languageConceptSequenceProperty()
                                  .set(Get.identifierService()
                                        .getConceptSequence(conceptId));
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
      setupDefaults();
      return observableLanguageCoordinate;
   }

   public ObservableLogicCoordinate getDefaultLogicCoordinate() {
      setupDefaults();
      return observableLogicCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefaultModule(int conceptId) {
      setupDefaults();
      observableEditCoordinate.moduleSequenceProperty()
                              .set(Get.identifierService()
                                      .getConceptSequence(conceptId));
   }

   public void setDefaultPath(int conceptId) {
      setupDefaults();
      observableStampPosition.stampPathSequenceProperty()
                             .set(Get.identifierService()
                                     .getConceptSequence(conceptId));
      observableEditCoordinate.pathSequenceProperty()
                              .set(Get.identifierService()
                                      .getConceptSequence(conceptId));
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableStampCoordinate getDefaultStampCoordinate() {
      setupDefaults();
      return observableStampCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefaultStatedAssemblage(int conceptId) {
      setupDefaults();
      observableLogicCoordinate.statedAssemblageSequenceProperty()
                               .set(Get.identifierService()
                                       .getConceptSequence(conceptId));
   }

   //~--- get methods ---------------------------------------------------------

   public ObservableTaxonomyCoordinate getDefaultTaxonomyCoordinate() {
      setupDefaults();
      return observableTaxonomyCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefaultTime(long timeInMs) {
      setupDefaults();
      observableStampPosition.timeProperty()
                             .set(timeInMs);
   }

   public void setDefaultUser(int conceptId) {
      setupDefaults();
      observableEditCoordinate.authorSequenceProperty()
                              .set(Get.identifierService()
                                      .getConceptSequence(conceptId));
   }
}

