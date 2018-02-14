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

import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.statement.StatementAssociation;

import java.util.UUID;

/**
 *
 * @author kec
 */
public class StatementAssociationImpl implements StatementAssociation {
    private final SimpleObjectProperty<LogicalExpression> associationSemantic = new SimpleObjectProperty();
    private final SimpleObjectProperty<UUID> associatedStatementId = new SimpleObjectProperty();

    @Override
    public LogicalExpression getAssociationSemantic() {
        return associationSemantic.get();
    }

    public SimpleObjectProperty<LogicalExpression> associationSemanticProperty() {
        return associationSemantic;
    }

    public void setAssociationSemantic(LogicalExpression associationSemantic) {
        this.associationSemantic.set(associationSemantic);
    }

    @Override
    public UUID getAssociatedStatementId() {
        return associatedStatementId.get();
    }

    public SimpleObjectProperty<UUID> associatedStatementIdProperty() {
        return associatedStatementId;
    }

    public void setAssociatedStatementId(UUID associatedStatementId) {
        this.associatedStatementId.set(associatedStatementId);
    }
}
