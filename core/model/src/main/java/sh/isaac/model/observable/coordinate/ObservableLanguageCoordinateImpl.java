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

import java.util.List;
import java.util.Optional;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.coordinate.LanguageCoordinateImpl;
import sh.isaac.model.observable.ObservableFields;

/**
 * The Class ObservableLanguageCoordinateImpl.
 *
 * @author kec
 */
public final class ObservableLanguageCoordinateImpl
        extends ObservableCoordinateImpl
        implements ObservableLanguageCoordinate {

    /**
     * The language concept nid property.
     */
    private final ObjectProperty<ConceptSpecification> languageProperty;

    /**
     * The dialect assemblage preference list property.
     */
    private final ListProperty<ConceptSpecification> dialectAssemblagePreferenceListProperty;

    /**
     * The description type preference list property.
     */
    private final ListProperty<ConceptSpecification> descriptionTypePreferenceListProperty;

    private ObjectProperty<ObservableLanguageCoordinate> nextProrityLanguageCoordinateProperty = null;

    /**
     * The language coordinate.
     */
    private final LanguageCoordinateImpl languageCoordinate;

    /**
     * Instantiates a new observable language coordinate impl.
     *
     * @param languageCoordinate the language coordinate
     */
    public ObservableLanguageCoordinateImpl(LanguageCoordinate languageCoordinate) {
        if (languageCoordinate instanceof ObservableLanguageCoordinateImpl) {
            throw new IllegalStateException("Trying to wrap an observable coordinate in an observable coordinate...");
        }
        this.languageCoordinate = (LanguageCoordinateImpl) languageCoordinate;
        
        this.languageProperty = new SimpleObjectProperty<>(this, 
                ObservableFields.LANGUAGE_FOR_LANGUAGE_COORDINATE.toExternalString(), 
                    languageCoordinate.getLanguageConcept());
        this.languageProperty.addListener((ObservableValue<? extends ConceptSpecification> observable, ConceptSpecification oldValue, ConceptSpecification newValue) -> {
            ObservableLanguageCoordinateImpl.this.languageCoordinate.setLanguageConceptNid(newValue.getNid());
        });
        
        ObservableList<ConceptSpecification> dialectAssemblagePreferenceList = FXCollections.observableArrayList();
        for (ConceptSpecification spec: languageCoordinate.getDialectAssemblageSpecPreferenceList()) {
            dialectAssemblagePreferenceList.add(spec);
        }
        
        this.dialectAssemblagePreferenceListProperty = new SimpleListProperty(this, 
                ObservableFields.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(), dialectAssemblagePreferenceList);
        
        this.dialectAssemblagePreferenceListProperty.addListener((ListChangeListener.Change<? extends ConceptSpecification> c) -> {
            int[] newList = new int[c.getList().size()];
            int i = 0;
            for (ConceptSpecification spec: c.getList()) {
                newList[i++] = spec.getNid();
            }
            ObservableLanguageCoordinateImpl.this.languageCoordinate.setDialectAssemblagePreferenceList(newList);           
        });
        
        ObservableList<ConceptSpecification> descriptionTypePreferenceList = FXCollections.observableArrayList();
        for (ConceptSpecification spec: languageCoordinate.getDescriptionTypeSpecPreferenceList()) {
            descriptionTypePreferenceList.add(spec);
        }
        
        
        this.descriptionTypePreferenceListProperty = new SimpleListProperty<>(this,
                    ObservableFields.DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                    descriptionTypePreferenceList);
       
        this.descriptionTypePreferenceListProperty.addListener((ListChangeListener.Change<? extends ConceptSpecification> c) -> {
            int[] newList = new int[c.getList().size()];
            int i = 0;
            for (ConceptSpecification spec: c.getList()) {
                newList[i++] = spec.getNid();
            }
            ObservableLanguageCoordinateImpl.this.languageCoordinate.setDescriptionTypePreferenceList(newList);           
        });
        
    }

    @Override
    public LanguageCoordinateImpl getLanguageCoordinate() {
        return languageCoordinate;
    }
    
    @Override
    public void setDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
        this.dialectAssemblagePreferenceListProperty.get().clear();
        for (int nid: dialectAssemblagePreferenceList) {
            this.dialectAssemblagePreferenceListProperty.get().add(Get.conceptSpecification(nid));
        }
    }

    
    @Override
    public void setDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        this.descriptionTypePreferenceListProperty.get().clear();
        for (int nid: descriptionTypePreferenceList) {
            this.descriptionTypePreferenceListProperty.get().add(Get.conceptSpecification(nid));
        }
   }

    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#descriptionTypePreferenceListProperty()
     */
    @Override
    public ListProperty<ConceptSpecification> descriptionTypePreferenceListProperty() {
         return this.descriptionTypePreferenceListProperty;
    }
    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#dialectAssemblagePreferenceListProperty()
     */
    @Override
    public ListProperty<ConceptSpecification>  dialectAssemblagePreferenceListProperty() {
        return this.dialectAssemblagePreferenceListProperty;
    }
    

    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#nextProrityLanguageCoordinateProperty()
     */
    @Override
    public ObjectProperty<ObservableLanguageCoordinate> nextProrityLanguageCoordinateProperty() {
        if (this.nextProrityLanguageCoordinateProperty == null) {
            ObservableLanguageCoordinate nextPriorityLanguageCoordinate = null;
            Optional<LanguageCoordinate> nextPriorityOption = languageCoordinate.getNextProrityLanguageCoordinate();
            if (nextPriorityOption.isPresent()) {
                nextPriorityLanguageCoordinate = new ObservableLanguageCoordinateImpl(nextPriorityOption.get());
            }
            this.nextProrityLanguageCoordinateProperty = new SimpleObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    nextPriorityLanguageCoordinate);
            this.nextProrityLanguageCoordinateProperty.addListener((invalidation) -> fireValueChangedEvent());

            addListenerReference(this.languageCoordinate
                    .setNextProrityLanguageCoordinateProperty(nextProrityLanguageCoordinateProperty));
        }

        return this.nextProrityLanguageCoordinateProperty;
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
    public ConceptSpecification getLanguageConcept() {
        return this.languageCoordinate.getLanguageConcept();
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getNextProrityLanguageCoordinate()
     */
    @Override
    public Optional<LanguageCoordinate> getNextProrityLanguageCoordinate() {
        return Optional.ofNullable(nextProrityLanguageCoordinateProperty().get());
    }

    /**
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#languageConceptNidProperty()
     */
    @Override
    public ObjectProperty<ConceptSpecification> languageConceptProperty() {
         return this.languageProperty;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ObservableLanguageCoordinateImpl{" + this.languageCoordinate + '}';
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescription(java.util.List, sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getDescription(descriptionList, stampCoordinate);
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getDescriptionTypePreferenceList()
     */
    @Override
    public int[] getDescriptionTypePreferenceList() {
        return this.languageCoordinate.getDescriptionTypePreferenceList();
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getDialectAssemblagePreferenceList()
     */
    @Override
    public int[] getDialectAssemblagePreferenceList() {
        return this.languageCoordinate.getDialectAssemblagePreferenceList();
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getFullySpecifiedDescription(java.util.List, sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getFullySpecifiedDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getFullySpecifiedDescription(descriptionList, stampCoordinate);
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getLanguageConceptNid()
     */
    @Override
    public int getLanguageConceptNid() {
        return this.languageCoordinate.getLanguageConceptNid();
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getPreferredDescription(java.util.List, sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getPreferredDescription(
            List<SemanticChronology> descriptionList,
            StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getPreferredDescription(descriptionList, stampCoordinate);
    }

    /**
     * @return 
     * @see sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate#deepClone()
     */
    @Override
    public ObservableLanguageCoordinateImpl deepClone() {
        return new ObservableLanguageCoordinateImpl(languageCoordinate.deepClone());
    }

    public LanguageCoordinateImpl unwrap() {
        return languageCoordinate;
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getDefinitionDescription(java.util.List, sh.isaac.api.coordinate.StampCoordinate)
     */
    @Override
    public LatestVersion<DescriptionVersion> getDefinitionDescription(List<SemanticChronology> descriptionList, StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getDefinitionDescription(descriptionList, stampCoordinate);
    }

    /**
     * @see sh.isaac.api.coordinate.LanguageCoordinate#getModulePreferenceListForLanguage()
     */
    @Override
    public int[] getModulePreferenceListForLanguage() {
        return this.languageCoordinate.getModulePreferenceListForLanguage();
    }

    @Override
    public LatestVersion<DescriptionVersion> getDescription(int conceptNid, int[] descriptionTypePreference, StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getDescription(conceptNid, descriptionTypePreference, stampCoordinate);
    }

    @Override
    public LatestVersion<DescriptionVersion> getDescription(List<SemanticChronology> descriptionList, int[] descriptionTypePreference,
            StampCoordinate stampCoordinate) {
        return this.languageCoordinate.getDescription(descriptionList, descriptionTypePreference, stampCoordinate);
    }

    @Override
    public ConceptSpecification[] getDialectAssemblageSpecPreferenceList() {
        return languageCoordinate.getDialectAssemblageSpecPreferenceList();
    }

    @Override
    public ConceptSpecification[] getDescriptionTypeSpecPreferenceList() {
        return languageCoordinate.getDescriptionTypeSpecPreferenceList();
    }

    @Override
    public ConceptSpecification[] getModuleSpecPreferenceListForLanguage() {
        return languageCoordinate.getModuleSpecPreferenceListForLanguage();
    }
    
    
}
