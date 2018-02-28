/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.model.statement;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import sh.isaac.api.statement.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.SimpleIntegerProperty;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class ClinicalStatementImpl implements ClinicalStatement {

    private final SimpleStringProperty narrative =
            new SimpleStringProperty(this, ObservableFields.STATEMENT_NARRATIVE.toExternalString());

    private final SimpleObjectProperty<Measure> statementTime = 
            new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_TIME.toExternalString());
    private final SimpleObjectProperty<UUID> statementId = 
            new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_ID.toExternalString());
    private final SimpleObjectProperty<UUID> subjectOfRecordId = 
            new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_SOR.toExternalString());
    private final SimpleListProperty<Participant> statementAuthors = 
            new SimpleListProperty(this, ObservableFields.STATEMENT_AUTHORS.toExternalString());
    private final SimpleIntegerProperty mode = 
            new SimpleIntegerProperty(this, ObservableFields.STATEMENT_MODE.toExternalString());
    private final SimpleIntegerProperty subjectOfInformation = 
            new SimpleIntegerProperty(this, ObservableFields.STATEMENT_SOI.toExternalString());
    private final SimpleIntegerProperty statementType = 
            new SimpleIntegerProperty(this, ObservableFields.STATEMENT_TYPE.toExternalString());
    private final SimpleIntegerProperty topic = 
            new SimpleIntegerProperty(this, ObservableFields.STATEMENT_TOPIC.toExternalString());
    private final SimpleObjectProperty<Circumstance> circumstance = 
            new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_CIRCUMSTANCE.toExternalString());
    private final SimpleListProperty<StatementAssociation> statementAssociations = 
            new SimpleListProperty(this, ObservableFields.STATEMENT_ASSOCIATIONS.toExternalString());

    private final SimpleObjectProperty<ManifoldCoordinate> manifold = 
            new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_STAMP_COORDINATE.toExternalString());

    public ClinicalStatementImpl(ManifoldCoordinate manifoldCoordinate) {
        statementId.set(UUID.randomUUID());
        this.manifold.set(manifoldCoordinate);
        statementTime.set(new MeasureImpl(manifoldCoordinate));
    }
    
    @Override
    public StampCoordinate getStampCoordinate() {
        return manifold.get();
    }

    public SimpleObjectProperty<ManifoldCoordinate> stampCoordinateProperty() {
        return manifold;
    }

    public void setManifold(ManifoldCoordinate coordinate) {
        this.manifold.set(coordinate);
    }
    
    @Override
    public Optional<String> getNarrative() {
        return Optional.ofNullable(narrative.get());
    }

    public SimpleStringProperty narrativeProperty() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative.set(narrative);
    }

    @Override
    public Measure getStatementTime() {
        return statementTime.get();
    }

    public SimpleObjectProperty<Measure> statementTimeProperty() {
        return statementTime;
    }

    public void setStatementTime(Measure statementTime) {
        this.statementTime.set(statementTime);
    }

    @Override
    public UUID getStatementId() {
        return statementId.get();
    }

    public SimpleObjectProperty<UUID> statementIdProperty() {
        return statementId;
    }

    public void setStatementId(UUID statementId) {
        this.statementId.set(statementId);
    }

    @Override
    public UUID getSubjectOfRecordId() {
        return subjectOfRecordId.get();
    }

    public SimpleObjectProperty<UUID> subjectOfRecordIdProperty() {
        return subjectOfRecordId;
    }

    public void setSubjectOfRecordId(UUID subjectOfRecordId) {
        this.subjectOfRecordId.set(subjectOfRecordId);
    }

    @Override
    public ObservableList<? extends Participant> getStatementAuthors() {
        return statementAuthors.get();
    }

    public SimpleListProperty<? extends Participant> statementAuthorsProperty() {
        return statementAuthors;
    }

    public void setStatementAuthors(List<? extends Participant> statementAuthors) {
        this.statementAuthors.get().setAll(statementAuthors);
    }

    @Override
    public ConceptChronology getMode() {
        return Get.concept(mode.get());
    }

    public SimpleIntegerProperty modeProperty() {
        return mode;
    }

    public void setMode(ConceptChronology mode) {
        this.mode.set(mode.getNid());
    }


    @Override
    public ConceptChronology getSubjectOfInformation() {
        return Get.concept(subjectOfInformation.get());
    }

    public SimpleIntegerProperty subjectOfInformationProperty() {
        return subjectOfInformation;
    }

    public void setSubjectOfInformation(ConceptChronology subjectOfInformation) {
        this.subjectOfInformation.set(subjectOfInformation.getNid());
    }

    @Override
    public ConceptChronology getStatementType() {
        return Get.concept(statementType.get());
    }

    public SimpleIntegerProperty statementTypeProperty() {
        return statementType;
    }

    public void setStatementType(ConceptChronology statementType) {
        this.statementType.set(statementType.getNid());
    }

    @Override
    public ConceptChronology getTopic() {
        return Get.concept(topic.get());
    }

    public SimpleIntegerProperty topicProperty() {
        return topic;
    }

    public void setTopic(ConceptChronology topic) {
        this.topic.set(topic.getNid());
    }

    @Override
    public Circumstance getCircumstance() {
        return circumstance.get();
    }

    public SimpleObjectProperty<Circumstance> circumstanceProperty() {
        return circumstance;
    }

    public void setCircumstance(Circumstance circumstance) {
        this.circumstance.set(circumstance);
    }

    @Override
    public ObservableList<StatementAssociation> getStatementAssociations() {
        return statementAssociations.get();
    }

    public SimpleListProperty<StatementAssociation> statementAssociationsProperty() {
        return statementAssociations;
    }

    public void setStatementAssociations(ObservableList<StatementAssociation> statementAssociations) {
        this.statementAssociations.set(statementAssociations);
    }
}
