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
package gov.vha.isaac.ochre.observable.model;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.observable.concept.ObservableConceptChronology;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;
import gov.vha.isaac.ochre.api.observable.sememe.version.ObservableDescriptionSememe;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.observable.model.version.ObservableConceptVersionImpl;
import gov.vha.isaac.ochre.observable.model.version.ObservableDescriptionImpl;
import java.util.Optional;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author kec
 */
public class ObservableConceptChronologyImpl extends
        ObservableChronologyImpl<
            ObservableConceptVersionImpl, ConceptChronology<ConceptVersionImpl>>
        implements ObservableConceptChronology<ObservableConceptVersionImpl> {

    private IntegerProperty conceptSequenceProperty;

    private ListProperty<ObservableSememeChronology<ObservableDescriptionSememe>> descriptionListProperty;

    public ObservableConceptChronologyImpl(ConceptChronology chronicledObjectLocal) {
        super(chronicledObjectLocal);
    }

    @Override
    protected ObservableList<ObservableConceptVersionImpl> getObservableVersionList() {
        ObservableList<ObservableConceptVersionImpl> observableList = FXCollections.observableArrayList();
        chronicledObjectLocal.getVersionList().stream().forEach((conceptVersion) -> {
            observableList.add(new ObservableConceptVersionImpl(conceptVersion, this));
        });
        return observableList;
    }

    @Override
    public Optional<LatestVersion<ObservableDescriptionSememe>> getFullySpecifiedDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        Optional<LatestVersion<DescriptionSememe>> optionalFsn = chronicledObjectLocal.getFullySpecifiedDescription(languageCoordinate, stampCoordinate);
        return getSpecifiedDescription(optionalFsn);
    }

    private Optional<LatestVersion<ObservableDescriptionSememe>> getSpecifiedDescription(Optional<LatestVersion<DescriptionSememe>> optionalSpecifiedDescription) {
        if (optionalSpecifiedDescription.isPresent()) {
            LatestVersion<DescriptionSememe> latestPreferred = optionalSpecifiedDescription.get();
            int latestStampSequence = latestPreferred.value().getStampSequence();
            
            ObservableSememeChronologyImpl<ObservableDescriptionImpl, SememeChronology<DescriptionSememe>> observableSpecified = 
                    new ObservableSememeChronologyImpl<>(latestPreferred.value().getChronology());
                   
            LatestVersion<ObservableDescriptionSememe> latest = new LatestVersion<>(ObservableDescriptionSememe.class);
            
            observableSpecified.getVersionList().forEach((version) -> {
                if (version.getStampSequence() == latestStampSequence) {
                    latest.addLatest(version);
                }
            });
            
            return Optional.of(latest);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LatestVersion<ObservableDescriptionSememe>> getPreferredDescription(LanguageCoordinate languageCoordinate, StampCoordinate stampCoordinate) {
        Optional<LatestVersion<DescriptionSememe>> optionalPreferred = chronicledObjectLocal.getPreferredDescription(languageCoordinate, stampCoordinate);
        return getSpecifiedDescription(optionalPreferred);

    }

    @Override
    public ObservableList<ObservableSememeChronology<ObservableDescriptionSememe>>
            getConceptDescriptionList() {
        return conceptDescriptionListProperty().get();
    }

    @Override
    public ListProperty<ObservableSememeChronology<ObservableDescriptionSememe>>
            conceptDescriptionListProperty() {
        if (descriptionListProperty == null) {
            ObservableList<ObservableSememeChronology<ObservableDescriptionSememe>> observableList = FXCollections.observableArrayList();
            descriptionListProperty = new SimpleListProperty<>(this,
                    ObservableFields.DESCRIPTION_LIST_FOR_CONCEPT.toExternalString(),
                    observableList);

            chronicledObjectLocal.getConceptDescriptionList().stream().forEach((conceptDescriptionChronicle) -> {
                ObservableSememeChronologyImpl observableConceptDescriptionChronicle = new ObservableSememeChronologyImpl(conceptDescriptionChronicle);
                observableList.add(observableConceptDescriptionChronicle);
            });
        }
        return descriptionListProperty;
    }

    @Override
    public int getConceptSequence() {
        if (conceptSequenceProperty != null) {
            return conceptSequenceProperty.get();
        }
        return chronicledObjectLocal.getConceptSequence();
    }

    @Override
    public IntegerProperty conceptSequenceProperty() {
        if (conceptSequenceProperty == null) {
            conceptSequenceProperty = new CommitAwareIntegerProperty(this,
                    ObservableFields.CONCEPT_SEQUENCE_FOR_CHRONICLE.toExternalString(),
                    getConceptSequence());
        }
        return conceptSequenceProperty;
    }

    @Override
    public ObservableConceptVersionImpl createMutableVersion(State state, EditCoordinate ec) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObservableConceptVersionImpl createMutableVersion(int stampSequence) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsDescription(String descriptionText) {
        return chronicledObjectLocal.containsDescription(descriptionText);
    }

    @Override
    public boolean containsActiveDescription(String descriptionText, StampCoordinate stampCoordinate) {
        return chronicledObjectLocal.containsDescription(descriptionText, stampCoordinate);
    }

}
