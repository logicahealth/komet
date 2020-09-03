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
package sh.isaac.model.observable.coordinate;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

/**
 * The Class ObservableLanguageCoordinateImpl.
 *
 * @author kec
 */
public final class ObservableLanguageCoordinateImpl
        extends ObservableLanguageCoordinateBase {

     /**
     * Instantiates a new observable language coordinate impl.
     *
     * @param languageCoordinate the language coordinate
     */
     public ObservableLanguageCoordinateImpl(LanguageCoordinate languageCoordinate, String coordinateName) {
         super(languageCoordinate, coordinateName);
     }

    public ObservableLanguageCoordinateImpl(LanguageCoordinate languageCoordinate) {
        super(languageCoordinate, "Language coordinate");
    }

    @Override
    public void setExceptOverrides(LanguageCoordinateImmutable updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeLanguageProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.LANGUAGE_FOR_LANGUAGE_COORDINATE.toExternalString(),
                languageCoordinate.getLanguageConcept());
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeDialectAssemblagePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinate.getDialectAssemblageSpecPreferenceList()));
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeDescriptionTypePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinate.getDescriptionTypeSpecPreferenceList()));
    }

    @Override
    protected SimpleEqualityBasedListProperty<ConceptSpecification> makeModulePreferenceListProperty(LanguageCoordinate languageCoordinate) {
        return new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.MODULE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinate.getModuleSpecPreferenceListForLanguage()));
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ObservableLanguageCoordinate> makeNextPriorityLanguageCoordinateProperty(LanguageCoordinate languageCoordinate) {
        if (languageCoordinate.getNextPriorityLanguageCoordinate().isPresent()) {
            return new SimpleEqualityBasedObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    new ObservableLanguageCoordinateImpl(languageCoordinate.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable(),
                            ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString()));
        }
        return new SimpleEqualityBasedObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    null);
    }

    @Override
    public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
        this.dialectAssemblagePreferenceListProperty().get().clear();
        for (int nid: dialectAssemblagePreferenceList) {
            this.dialectAssemblagePreferenceListProperty().get().add(Get.conceptSpecification(nid));
        }
    }

    
    @Override
    public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        this.descriptionTypePreferenceListProperty().get().clear();
        for (int nid: descriptionTypePreferenceList) {
            this.descriptionTypePreferenceListProperty().get().add(Get.conceptSpecification(nid));
        }
   }

    /**
     * 
     * @param languageConceptNid 
     * @deprecated for backward compatibility only
     */
    public void setLanguageConceptNid(int languageConceptNid) {
        this.languageConceptProperty().set(Get.conceptSpecification(languageConceptNid));
    }

    @Override
    public LanguageCoordinateImmutable getOriginalValue() {
        return getValue();
    }


    @Override
    protected LanguageCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateImmutable> observable, LanguageCoordinateImmutable oldValue, LanguageCoordinateImmutable newValue) {
        this.languageConceptProperty().setValue(newValue.getLanguageConcept());
        this.dialectAssemblagePreferenceListProperty().setAll(newValue.getDialectAssemblageSpecPreferenceList());
        this.descriptionTypePreferenceListProperty().setAll(newValue.getDescriptionTypeSpecPreferenceList());
        this.modulePreferenceListForLanguageProperty().setAll(newValue.getModuleSpecPreferenceListForLanguage());
        if (newValue.getNextPriorityLanguageCoordinate().isPresent()) {
            if (this.nextPriorityLanguageCoordinateProperty().get() != null) {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                this.nextPriorityLanguageCoordinateProperty().get().setValue(languageCoordinateImmutable);
            } else {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                ObservableLanguageCoordinateImpl observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(languageCoordinateImmutable);
                this.nextPriorityLanguageCoordinateProperty().setValue(observableLanguageCoordinate);
            }
        } else {
            this.nextPriorityLanguageCoordinateProperty().setValue(null);
        }
        return newValue;
    }

}
