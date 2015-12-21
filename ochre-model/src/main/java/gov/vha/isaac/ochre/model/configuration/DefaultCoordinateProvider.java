/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.model.configuration;


import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableEditCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLanguageCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLogicCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableTaxonomyCoordinate;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableEditCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableLanguageCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableLogicCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableStampCoordinateImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableStampPositionImpl;
import gov.vha.isaac.ochre.model.observable.coordinate.ObservableTaxonomyCoordinateImpl;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.collections.ObservableIntegerArray;

/**
 *
 * @author kec
 */
public class DefaultCoordinateProvider {

    AtomicBoolean defaultsSetup = new AtomicBoolean();
    CountDownLatch defaultsSetupLatch = new CountDownLatch(1);
    ObservableEditCoordinate observableEditCoordinate;
    ObservableLanguageCoordinate observableLanguageCoordinate;
    ObservableLogicCoordinate observableLogicCoordinate;
    ObservableStampCoordinate observableStampCoordinate;
    ObservableStampPositionImpl observableStampPosition;
    ObservableTaxonomyCoordinate observableTaxonomyCoordinate;

    private void setupDefaults() {
        try {
            if (defaultsSetup.compareAndSet(false, true)) {
                observableEditCoordinate = new ObservableEditCoordinateImpl(EditCoordinates.getDefaultUserSolorOverlay());
                observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate());
                observableLogicCoordinate = new ObservableLogicCoordinateImpl(LogicCoordinates.getStandardElProfile());
                observableStampCoordinate = new ObservableStampCoordinateImpl(StampCoordinates.getDevelopmentLatestActiveOnly());
                observableStampPosition = new ObservableStampPositionImpl(StampCoordinates.getDevelopmentLatestActiveOnly().getStampPosition());
                observableTaxonomyCoordinate = new ObservableTaxonomyCoordinateImpl(TaxonomyCoordinates.getInferredTaxonomyCoordinate(observableStampCoordinate, observableLanguageCoordinate, observableLogicCoordinate));
                observableStampCoordinate.stampPositionProperty().setValue(observableStampPosition);
                defaultsSetupLatch.countDown();
            }
            defaultsSetupLatch.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setDefaultUser(int conceptId) {
        setupDefaults();
        observableEditCoordinate.authorSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultModule(int conceptId) {
        setupDefaults();
        observableEditCoordinate.moduleSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultTime(long timeInMs) {
        setupDefaults();
        observableStampPosition.timeProperty().set(timeInMs);
    }

    public void setDefaultPath(int conceptId) {
        setupDefaults();
        observableStampPosition.stampPathSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
        observableEditCoordinate.pathSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultLanguage(int conceptId) {
        setupDefaults();
        observableLanguageCoordinate.languageConceptSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
        setupDefaults();
        for (int i = 0; i < dialectAssemblagePreferenceList.length; i++) {
            dialectAssemblagePreferenceList[i] = Get.identifierService().getConceptSequence(dialectAssemblagePreferenceList[i]);
        }
        ObservableIntegerArray dialectAssemblageIntegerArray
                = observableLanguageCoordinate.dialectAssemblagePreferenceListProperty().get();
        dialectAssemblageIntegerArray.clear();
        dialectAssemblageIntegerArray.addAll(dialectAssemblagePreferenceList);
    }

    public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        setupDefaults();
        for (int i = 0; i < descriptionTypePreferenceList.length; i++) {
            descriptionTypePreferenceList[i] = Get.identifierService().getConceptSequence(descriptionTypePreferenceList[i]);
        }
        ObservableIntegerArray descriptionTypeIntegerArray
                = observableLanguageCoordinate.descriptionTypePreferenceListProperty().get();
        descriptionTypeIntegerArray.clear();
        descriptionTypeIntegerArray.addAll(descriptionTypePreferenceList);
        observableLanguageCoordinate.descriptionTypePreferenceListProperty().set(descriptionTypeIntegerArray);
    }

    public void setDefaultStatedAssemblage(int conceptId) {
        setupDefaults();
        observableLogicCoordinate.statedAssemblageSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultInferredAssemblage(int conceptId) {
        setupDefaults();
        observableLogicCoordinate.inferredAssemblageSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultDescriptionLogicProfile(int conceptId) {
        setupDefaults();
        observableLogicCoordinate.descriptionLogicProfileSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));
    }

    public void setDefaultClassifier(int conceptId) {
        setupDefaults();
        observableLogicCoordinate.classifierSequenceProperty().set(Get.identifierService().getConceptSequence(conceptId));

    }

    public ObservableEditCoordinate getDefaultEditCoordinate() {
        setupDefaults();
        return observableEditCoordinate;
    }

    public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
        setupDefaults();
        return observableLanguageCoordinate;
    }

    public ObservableLogicCoordinate getDefaultLogicCoordinate() {
        setupDefaults();
        return observableLogicCoordinate;
    }

    public ObservableStampCoordinate getDefaultStampCoordinate() {
        setupDefaults();
        return observableStampCoordinate;
    }

    public ObservableTaxonomyCoordinate getDefaultTaxonomyCoordinate() {
        setupDefaults();
        return observableTaxonomyCoordinate;
    }
    
}
