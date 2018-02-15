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

import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.statement.Participant;
import sh.isaac.model.observable.ObservableFields;


/**
 *
 * @author kec
 */
public class ParticipantImpl implements Participant {

    private final SimpleObjectProperty<LogicalExpression> participantRole = 
            new SimpleObjectProperty<>(this, ObservableFields.PARTICIPANT_ROLE.toExternalString());

    private final SimpleObjectProperty<UUID> participantId = 
            new SimpleObjectProperty(this, ObservableFields.PARTICIPANT_ID.toExternalString());

    @Override
    public Optional<UUID> getParticipantId() {
        return Optional.ofNullable(participantId.get());
    }

    public SimpleObjectProperty<UUID> participantIdProperty() {
        return participantId;
    }

    public void setParticipantId(UUID participantId) {
        this.participantId.set(participantId);
    }
    @Override
    public LogicalExpression getParticipantRole() {
        return participantRole.get();
    }

    public SimpleObjectProperty<LogicalExpression> participantRoleProperty() {
        return participantRole;
    }

    public void setParticipantRole(LogicalExpression participantRole) {
        this.participantRole.set(participantRole);
    }
}
