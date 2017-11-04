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
import sh.isaac.model.observable.coordinate.ObservableEditCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampPositionImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DefaultCoordinateProvider.
 *
 * @author kec
 */
public class DefaultCoordinateProvider {
   /** The defaults setup. */
   AtomicBoolean defaultsSetup = new AtomicBoolean();

   /** The defaults setup latch. */
   CountDownLatch defaultsSetupLatch = new CountDownLatch(1);

   /** The observable edit coordinate. */
   ObservableEditCoordinate observableEditCoordinate;

   /** The observable language coordinate. */
   ObservableLanguageCoordinate observableLanguageCoordinate;

   /** The observable logic coordinate. */
   ObservableLogicCoordinate observableLogicCoordinate;

   /** The observable stamp coordinate. */
   ObservableStampCoordinate observableStampCoordinate;

   /** The observable stamp position. */
   ObservableStampPositionImpl observableStampPosition;

   /** The observable taxonomy coordinate. */
   ObservableManifoldCoordinate observableManifoldCoordinate;

   //~--- methods -------------------------------------------------------------

   /**
    * Setup defaults.
    */
   private void setupDefaults() {
      try {
         if (this.defaultsSetup.compareAndSet(false, true)) {
            this.observableEditCoordinate =
               new ObservableEditCoordinateImpl(EditCoordinates.getDefaultUserSolorOverlay());
            this.observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
            this.observableLogicCoordinate = new ObservableLogicCoordinateImpl(LogicCoordinates.getStandardElProfile());
            this.observableStampCoordinate =
               new ObservableStampCoordinateImpl(StampCoordinates.getDevelopmentLatestActiveOnly());
            this.observableStampPosition =
               new ObservableStampPositionImpl(StampCoordinates.getDevelopmentLatestActiveOnly().getStampPosition());
            this.observableManifoldCoordinate = new ObservableManifoldCoordinateImpl(
                ManifoldCoordinates.getInferredManifoldCoordinate(this.observableStampCoordinate,
                      this.observableLanguageCoordinate,
                      this.observableLogicCoordinate));
            this.observableStampCoordinate.stampPositionProperty()
                                          .setValue(this.observableStampPosition);
            this.defaultsSetupLatch.countDown();
         }

         this.defaultsSetupLatch.await();
      } catch (final InterruptedException ex) {
         throw new RuntimeException(ex);
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default classifier.
    *
    * @param conceptId the new default classifier
    */
   public void setDefaultClassifier(int conceptId) {
      setupDefaults();
      this.observableLogicCoordinate.classifierNidProperty()
                                    .set(conceptId);
   }

   /**
    * Sets the default description logic profile.
    *
    * @param conceptId the new default description logic profile
    */
   public void setDefaultDescriptionLogicProfile(int conceptId) {
      setupDefaults();
      this.observableLogicCoordinate.descriptionLogicProfileNidProperty()
                                    .set(conceptId);
   }

   /**
    * Sets the default description type preference list.
    *
    * @param descriptionTypePreferenceList the new default description type preference list
    */
   public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      setupDefaults();

      final ObservableIntegerArray descriptionTypeIntegerArray =
         this.observableLanguageCoordinate.descriptionTypePreferenceListProperty()
                                          .get();

      descriptionTypeIntegerArray.clear();
      descriptionTypeIntegerArray.addAll(descriptionTypePreferenceList);
      this.observableLanguageCoordinate.descriptionTypePreferenceListProperty()
                                       .set(descriptionTypeIntegerArray);
   }

   /**
    * Sets the default dialect assemblage preference list.
    *
    * @param dialectAssemblagePreferenceList the new default dialect assemblage preference list
    */
   public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      setupDefaults();

      final ObservableIntegerArray dialectAssemblageIntegerArray =
         this.observableLanguageCoordinate.dialectAssemblagePreferenceListProperty()
                                          .get();

      dialectAssemblageIntegerArray.clear();
      dialectAssemblageIntegerArray.addAll(dialectAssemblagePreferenceList);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default edit coordinate.
    *
    * @return the default edit coordinate
    */
   public ObservableEditCoordinate getDefaultEditCoordinate() {
      setupDefaults();
      return this.observableEditCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default inferred assemblage.
    *
    * @param conceptId the new default inferred assemblage
    */
   public void setDefaultInferredAssemblage(int conceptId) {
      setupDefaults();
      this.observableLogicCoordinate.inferredAssemblageNidProperty()
                                    .set(conceptId);
   }

   /**
    * Sets the default language.
    *
    * @param conceptId the new default language
    */
   public void setDefaultLanguage(int conceptId) {
      setupDefaults();
      this.observableLanguageCoordinate.languageConceptNidProperty()
                                       .set(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default language coordinate.
    *
    * @return the default language coordinate
    */
   public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
      setupDefaults();
      return this.observableLanguageCoordinate;
   }

   /**
    * Gets the default logic coordinate.
    *
    * @return the default logic coordinate
    */
   public ObservableLogicCoordinate getDefaultLogicCoordinate() {
      setupDefaults();
      return this.observableLogicCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default module.
    *
    * @param conceptId the new default module
    */
   public void setDefaultModule(int conceptId) {
      setupDefaults();
      this.observableEditCoordinate.moduleSequenceProperty()
                                   .set(conceptId);
   }

   /**
    * Sets the default path.
    *
    * @param conceptId the new default path
    */
   public void setDefaultPath(int conceptId) {
      setupDefaults();
      this.observableStampPosition.stampPathNidProperty()
                                  .set(conceptId);
      this.observableEditCoordinate.pathSequenceProperty()
                                   .set(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default stamp coordinate.
    *
    * @return the default stamp coordinate
    */
   public ObservableStampCoordinate getDefaultStampCoordinate() {
      setupDefaults();
      return this.observableStampCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default stated assemblage.
    *
    * @param conceptId the new default stated assemblage
    */
   public void setDefaultStatedAssemblage(int conceptId) {
      setupDefaults();
      this.observableLogicCoordinate.statedAssemblageNidProperty()
                                    .set(conceptId);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the default taxonomy coordinate.
    *
    * @return the default taxonomy coordinate
    */
   public ObservableManifoldCoordinate getDefaultManifoldCoordinate() {
      setupDefaults();
      return this.observableManifoldCoordinate;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the default time.
    *
    * @param timeInMs the new default time
    */
   public void setDefaultTime(long timeInMs) {
      setupDefaults();
      this.observableStampPosition.timeProperty()
                                  .set(timeInMs);
   }

   /**
    * Sets the default user.
    *
    * @param conceptId the new default user
    */
   public void setDefaultUser(int conceptId) {
      setupDefaults();
      this.observableEditCoordinate.authorSequenceProperty()
                                   .set(conceptId);
   }
}

