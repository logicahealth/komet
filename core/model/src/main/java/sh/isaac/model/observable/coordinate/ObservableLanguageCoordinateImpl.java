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

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.LanguageCoordinate;
import sh.isaac.api.coordinate.LanguageCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;

import java.util.Optional;

/**
 * The Class ObservableLanguageCoordinateImpl.
 *
 * @author kec
 */
public final class ObservableLanguageCoordinateImpl
        extends ObservableCoordinateImpl<LanguageCoordinateImmutable>
        implements ObservableLanguageCoordinate {

    /**
     * The language concept nid property.
     */
    private final SimpleEqualityBasedObjectProperty<ConceptSpecification> languageProperty;

    /**
     * The dialect assemblage preference list property.
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> dialectAssemblagePreferenceListProperty;

    /**
     * The description type preference list property.
     */
    private final SimpleEqualityBasedListProperty<ConceptSpecification> descriptionTypePreferenceListProperty;

    private final SimpleEqualityBasedListProperty<ConceptSpecification> modulePreferenceListProperty;

    private final SimpleEqualityBasedObjectProperty<ObservableLanguageCoordinateImpl> nextPriorityLanguageCoordinateProperty;

    /**
     * Instantiates a new observable language coordinate impl.
     *
     * @param languageCoordinateImmutable the language coordinate
     */
    public ObservableLanguageCoordinateImpl(LanguageCoordinateImmutable languageCoordinateImmutable) {
        super(languageCoordinateImmutable);

        this.languageProperty = new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.LANGUAGE_FOR_LANGUAGE_COORDINATE.toExternalString(),
                languageCoordinateImmutable.getLanguageConcept());

        //
        this.dialectAssemblagePreferenceListProperty = new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.DIALECT_ASSEMBLAGE_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinateImmutable.getDialectAssemblageSpecPreferenceList()));

        //
        this.descriptionTypePreferenceListProperty = new SimpleEqualityBasedListProperty<>(this,
                    ObservableFields.DESCRIPTION_TYPE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinateImmutable.getDescriptionTypeSpecPreferenceList()));

        //
        this.modulePreferenceListProperty = new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.MODULE_NID_PREFERENCE_LIST_FOR_LANGUAGE_COORDINATE.toExternalString(),
                FXCollections.observableArrayList(languageCoordinateImmutable.getModuleSpecPreferenceListForLanguage()));

        if (languageCoordinateImmutable.getNextPriorityLanguageCoordinate().isPresent()) {
            this.nextPriorityLanguageCoordinateProperty = new SimpleEqualityBasedObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    new ObservableLanguageCoordinateImpl(languageCoordinateImmutable.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable()));
        } else {
            this.nextPriorityLanguageCoordinateProperty = new SimpleEqualityBasedObjectProperty<>(this,
                    ObservableFields.NEXT_PRIORITY_LANGUAGE_COORDINATE.toExternalString(),
                    null);
        }
        addListeners();
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends LanguageCoordinateImmutable> observable, LanguageCoordinateImmutable oldValue, LanguageCoordinateImmutable newValue) {
        this.languageProperty.setValue(newValue.getLanguageConcept());
        this.dialectAssemblagePreferenceListProperty.setAll(newValue.getDialectAssemblageSpecPreferenceList());
        this.descriptionTypePreferenceListProperty.setAll(newValue.getDescriptionTypeSpecPreferenceList());
        this.modulePreferenceListProperty.setAll(newValue.getModuleSpecPreferenceListForLanguage());
        if (newValue.getNextPriorityLanguageCoordinate().isPresent()) {
            if (this.nextPriorityLanguageCoordinateProperty.get() != null) {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                this.nextPriorityLanguageCoordinateProperty.get().setValue(languageCoordinateImmutable);
            } else {
                LanguageCoordinateImmutable languageCoordinateImmutable = newValue.getNextPriorityLanguageCoordinate().get().toLanguageCoordinateImmutable();
                ObservableLanguageCoordinateImpl observableLanguageCoordinate = new ObservableLanguageCoordinateImpl(languageCoordinateImmutable);
                this.nextPriorityLanguageCoordinateProperty.setValue(observableLanguageCoordinate);
            }
        } else {
            this.nextPriorityLanguageCoordinateProperty.setValue(null);
        }
    }

    @Override
    protected void addListeners() {
        this.languageProperty.addListener(this::languageConceptChanged);
        this.dialectAssemblagePreferenceListProperty.addListener(this::dialectAssemblagePreferenceListChanged);
        this.descriptionTypePreferenceListProperty.addListener(this::descriptionTypePreferenceListChanged);
        this.modulePreferenceListProperty.addListener(this::modulePreferenceListChanged);
        this.nextPriorityLanguageCoordinateProperty.addListener(this::nextProrityLanguageCoordinateChanged);
    }

    @Override
    protected void removeListeners() {
        this.languageProperty.removeListener(this::languageConceptChanged);
        this.dialectAssemblagePreferenceListProperty.removeListener(this::dialectAssemblagePreferenceListChanged);
        this.descriptionTypePreferenceListProperty.removeListener(this::descriptionTypePreferenceListChanged);
        this.modulePreferenceListProperty.removeListener(this::modulePreferenceListChanged);
        this.nextPriorityLanguageCoordinateProperty.removeListener(this::nextProrityLanguageCoordinateChanged);
    }

    private void nextProrityLanguageCoordinateChanged(ObservableValue<? extends ObservableLanguageCoordinateImpl> observable,
                                                      ObservableLanguageCoordinateImpl oldNextPriorityCoordinate,
                                                      ObservableLanguageCoordinateImpl newNextPriorityCoordinate) {
        if (newNextPriorityCoordinate == null) {
            this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                    IntLists.immutable.of(getDescriptionTypePreferenceList()),
                    IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                    IntLists.immutable.of(getModulePreferenceListForLanguage()),
                    Optional.empty()));
        } else {
            this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                    IntLists.immutable.of(getDescriptionTypePreferenceList()),
                    IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                    IntLists.immutable.of(getModulePreferenceListForLanguage()),
                    Optional.of(newNextPriorityCoordinate.getValue())));
        }
    }

    private void modulePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                getNextPriorityLanguageCoordinate()));
    }
    private void descriptionTypePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    private void dialectAssemblagePreferenceListChanged(ListChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(LanguageCoordinateImmutable.make(getLanguageConceptNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(c.getList().stream().mapToInt(value -> value.getNid()).toArray()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    private void languageConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
                                        ConceptSpecification oldLanguageConcept,
                                        ConceptSpecification newLanguageConcept) {
        this.setValue(LanguageCoordinateImmutable.make(newLanguageConcept.getNid(),
                IntLists.immutable.of(getDescriptionTypePreferenceList()),
                IntLists.immutable.of(getDialectAssemblagePreferenceList()),
                IntLists.immutable.of(getModulePreferenceListForLanguage()),
                getNextPriorityLanguageCoordinate()));
    }

    @Override
    public ListProperty<ConceptSpecification> modulePreferenceListForLanguage() {
        return this.modulePreferenceListProperty;
    }

    @Override
    public LanguageCoordinateImmutable getLanguageCoordinate() {
        return getValue();
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
    public ObjectProperty<ObservableLanguageCoordinateImpl> nextProrityLanguageCoordinateProperty() {
        return this.nextPriorityLanguageCoordinateProperty;
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
    public ObjectProperty<ConceptSpecification> languageConceptProperty() {
         return this.languageProperty;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ObservableLanguageCoordinateImpl{" + this.getValue().toString() + '}';
    }

    @Override
    public LanguageCoordinateImmutable toLanguageCoordinateImmutable() {
        return getValue();
    }

    @Override
    public Optional<? extends LanguageCoordinate> getNextPriorityLanguageCoordinate() {
        return Optional.ofNullable(this.nextPriorityLanguageCoordinateProperty.getValue());
    }

}
