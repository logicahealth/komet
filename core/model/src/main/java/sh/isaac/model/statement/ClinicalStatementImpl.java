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
import javafx.collections.ObservableList;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.statement.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class ClinicalStatementImpl implements ClinicalStatement {
    private final SimpleObjectProperty<Measure> statementTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<UUID> statementId = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<UUID> subjectOfRecordId = new SimpleObjectProperty<>();
    private final SimpleListProperty<Participant> statementAuthors = new SimpleListProperty();
    private final SimpleObjectProperty<LogicalExpression> subjectOfInformation = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LogicalExpression> statementType = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LogicalExpression> topic = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Circumstance> circumstance = new SimpleObjectProperty<>();
    private final SimpleListProperty<StatementAssociation> statementAssociations = new SimpleListProperty();

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
    public LogicalExpression getSubjectOfInformation() {
        return subjectOfInformation.get();
    }

    public SimpleObjectProperty<LogicalExpression> subjectOfInformationProperty() {
        return subjectOfInformation;
    }

    public void setSubjectOfInformation(LogicalExpression subjectOfInformation) {
        this.subjectOfInformation.set(subjectOfInformation);
    }

    @Override
    public LogicalExpression getStatementType() {
        return statementType.get();
    }

    public SimpleObjectProperty<LogicalExpression> statementTypeProperty() {
        return statementType;
    }

    public void setStatementType(LogicalExpression statementType) {
        this.statementType.set(statementType);
    }

    @Override
    public LogicalExpression getTopic() {
        return topic.get();
    }

    public SimpleObjectProperty<LogicalExpression> topicProperty() {
        return topic;
    }

    public void setTopic(LogicalExpression topic) {
        this.topic.set(topic);
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
