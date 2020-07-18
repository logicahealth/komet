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
package sh.komet.gui.control.circumstance;

import java.util.List;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.statement.Repetition;
import sh.isaac.model.statement.CircumstanceImpl;
import sh.isaac.model.statement.RepetitionImpl;
import sh.isaac.model.statement.RequestCircumstanceImpl;
import sh.komet.gui.control.list.PropertySheetListWrapper;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.control.repetition.RepetitionEditor;

/**
 *
 * @author kec
 */
public class RequestPropertySheet extends CircumstancePropertySheet {

    public RequestPropertySheet(ManifoldCoordinate manifoldCoordinate) {
        super(manifoldCoordinate);
    }

    @Override
    protected void getSubclassProperties(CircumstanceImpl circumstance, List<PropertySheet.Item> itemList) {
        RequestCircumstanceImpl request = (RequestCircumstanceImpl) circumstance;
        
        // TODO SimpleListProperty<StatementAssociation> conditionalTriggersProperty()
        
        // TODO SimpleListProperty<? extends Participant> requestedParticipantsProperty()
        
        // TODO SimpleObjectProperty<LogicalExpression> priorityProperty()

        itemList.add(new PropertySheetListWrapper<>(manifoldCoordinate, request.repetitionsProperty(),
            this::newRepetition, 
            this::newPropertyEditor));

        itemList.add(new PropertySheetMeasureWrapper(manifoldCoordinate, request.requestedMeasureProperty()));
    }
    
    Repetition newRepetition() {
        return new RepetitionImpl();
    }
    
    PropertyEditor<Repetition> newPropertyEditor(ManifoldCoordinate manifoldCoordinate) {
        return new RepetitionEditor(manifoldCoordinate);
    }
}
