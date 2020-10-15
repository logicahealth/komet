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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.Activity;
import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.coordinate.WriteCoordinateImpl;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampPath;
import sh.isaac.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableLogicCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.isaac.model.observable.coordinate.ObservableStampPathImpl;
import sh.isaac.model.observable.coordinate.ObservableStampPositionImpl;

/**
 * The Class DefaultCoordinateProvider.
 *
 * @author kec
 */
public class DefaultCoordinateProvider {
   private final SimpleObjectProperty<WriteCoordinate> writeCoordinate;

   /** The observable language coordinate. */
   private final ObservableLanguageCoordinate observableLanguageCoordinate;

   /** The observable logic coordinate. */
   private final ObservableLogicCoordinate observableLogicCoordinate;

   /** The observable stamp coordinate. */
   private final ObservableStampPath observableStampPath;

   /** The observable stamp position. */
   private final ObservableStampPositionImpl observableStampPosition;

   /** The observable taxonomy coordinate. */
   private final ObservableManifoldCoordinate observableManifoldCoordinate;

   public DefaultCoordinateProvider() {
      this.writeCoordinate = new SimpleObjectProperty<WriteCoordinate>(new WriteCoordinateImpl(EditCoordinates.getDefaultUserSolorOverlay().getAuthorNidForChanges(), 
            EditCoordinates.getDefaultUserSolorOverlay().getDefaultModuleNid(), EditCoordinates.getDefaultUserSolorOverlay().getPromotionPathNid()));
      this.observableStampPath = ObservableStampPathImpl.make(Coordinates.Path.Development());
      this.observableLogicCoordinate = new ObservableLogicCoordinateImpl(Coordinates.Logic.ElPlusPlus());
      this.observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(
              Coordinates.Language.UsEnglishFullyQualifiedName());
      this.observableStampPosition = new ObservableStampPositionImpl(Coordinates.Position.LatestOnDevelopment());
      this.observableManifoldCoordinate = new ObservableManifoldCoordinateImpl(
              ManifoldCoordinateImmutable.makeInferred(this.observableStampPath.getStampFilter(),
                      this.observableLanguageCoordinate,
                      this.observableLogicCoordinate, Activity.DEVELOPING, Coordinates.Edit.Default()));
   }

   /**
    * Sets the default classifier.
    *
    * @param conceptId the new default classifier
    */
   public void setDefaultClassifier(int conceptId) {
      this.observableLogicCoordinate.classifierProperty()
                                    .set(new ConceptProxy(conceptId));
   }

   /**
    * Sets the default description logic profile.
    *
    * @param conceptId the new default description logic profile
    */
   public void setDefaultDescriptionLogicProfile(int conceptId) {
      this.observableLogicCoordinate.descriptionLogicProfileProperty()
                                    .set(new ConceptProxy(conceptId));
   }

   /**
    * Sets the default description type preference list.
    *
    * @param descriptionTypePreferenceList the new default description type preference list
    */
   public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
      final ObservableList<ConceptSpecification> descriptionTypeList =
         this.observableLanguageCoordinate.descriptionTypePreferenceListProperty()
                                          .get();

      descriptionTypeList.clear();
      for (int nid: descriptionTypePreferenceList) {
          descriptionTypeList.add(Get.conceptSpecification(nid));
      }
   }

   /**
    * Sets the default dialect assemblage preference list.
    *
    * @param dialectAssemblagePreferenceList the new default dialect assemblage preference list
    */
   public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
      final ObservableList<ConceptSpecification> dialectAssemblageList =
         this.observableLanguageCoordinate.dialectAssemblagePreferenceListProperty()
                                          .get();

      dialectAssemblageList.clear();
      for (int nid: dialectAssemblagePreferenceList) {
          dialectAssemblageList.add(Get.conceptSpecification(nid));
      }
   }

   /**
    * Gets the default edit coordinate.
    *
    * @return the default edit coordinate
    */
   public ReadOnlyObjectProperty<WriteCoordinate> getDefaultWriteCoordinate() {
      return this.writeCoordinate;
   }

   /**
    * Sets the default inferred assemblage.
    *
    * @param conceptId the new default inferred assemblage
    */
   public void setDefaultInferredAssemblage(int conceptId) {
      this.observableLogicCoordinate.inferredAssemblageProperty()
                                    .set(new ConceptProxy(conceptId));
   }

   /**
    * Sets the default language.
    *
    * @param conceptId the new default language
    */
   public void setDefaultLanguage(int conceptId) {
      this.observableLanguageCoordinate.languageConceptProperty()
                                       .set(Get.conceptSpecification(conceptId));
   }

   /**
    * Gets the default language coordinate.
    *
    * @return the default language coordinate
    */
   public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
      return this.observableLanguageCoordinate;
   }

   /**
    * Gets the default logic coordinate.
    *
    * @return the default logic coordinate
    */
   public ObservableLogicCoordinate getDefaultLogicCoordinate() {
      return this.observableLogicCoordinate;
   }

   /**
    * Sets the default write module.
    *
    * @param moduleNid the new default module
    */
   public void setDefaultModule(int moduleNid) {
      this.writeCoordinate.set(new WriteCoordinateImpl(this.writeCoordinate.get().getAuthorNid(), moduleNid, this.writeCoordinate.get().getPathNid()));
   }

   /**
    * Sets the default path.
    *
    * @param pathSpecification
    */
   public void setDefaultPath(ConceptSpecification pathSpecification) {
      this.observableStampPosition.pathConceptProperty().set(pathSpecification);
      this.writeCoordinate.set(new WriteCoordinateImpl(this.writeCoordinate.get().getAuthorNid(), this.writeCoordinate.get().getModuleNid(), pathSpecification.getNid()));
   }

   /**
    * Gets the default stamp coordinate.
    *
    * @return the default stamp coordinate
    */
   public ObservableStampPath getDefaultStampCoordinate() {
      return this.observableStampPath;
   }

   /**
    * Sets the default stated assemblage.
    *
    * @param conceptId the new default stated assemblage
    */
   public void setDefaultStatedAssemblage(int conceptId) {
      this.observableLogicCoordinate.statedAssemblageProperty()
                                    .set(new ConceptProxy(conceptId));
   }

   /**
    * Gets the default taxonomy coordinate.
    *
    * @return the default taxonomy coordinate
    */
   public ObservableManifoldCoordinate getDefaultManifoldCoordinate() {
      return this.observableManifoldCoordinate;
   }

   /**
    * Sets the default time.
    *
    * @param timeInMs the new default time
    */
   public void setDefaultTime(long timeInMs) {
      this.observableStampPosition.timeProperty()
                                  .set(timeInMs);
   }
   
   /**
    * Sets the default premise type.
    *
    * @param premiseType the new default premise type
    */
   public void setDefaultPremiseType(PremiseType premiseType) {
      switch (premiseType) {
         case STATED:
            this.observableManifoldCoordinate.getNavigationCoordinate().navigatorIdentifierConceptsProperty().clear();
            this.observableManifoldCoordinate.getNavigationCoordinate().navigatorIdentifierConceptsProperty().add(TermAux.EL_PLUS_PLUS_STATED_ASSEMBLAGE);
            break;
         case INFERRED:
            this.observableManifoldCoordinate.getNavigationCoordinate().navigatorIdentifierConceptsProperty().clear();
            this.observableManifoldCoordinate.getNavigationCoordinate().navigatorIdentifierConceptsProperty().add(TermAux.EL_PLUS_PLUS_INFERRED_ASSEMBLAGE);
            break;
        default :
            throw new RuntimeException("oops");
      }
   }

   /**
    * Sets the default user.
    *
    * @param authorNid the new default user
    */
   public void setDefaultUser(int authorNid) {
      this.writeCoordinate.set(new WriteCoordinateImpl(authorNid, this.writeCoordinate.get().getModuleNid(), this.writeCoordinate.get().getPathNid()));
   }
}

