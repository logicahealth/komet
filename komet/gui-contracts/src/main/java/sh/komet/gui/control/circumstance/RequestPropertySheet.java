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
import sh.isaac.model.statement.CircumstanceImpl;
import sh.isaac.model.statement.RequestCircumstanceImpl;
import sh.komet.gui.control.measure.PropertySheetMeasureWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class RequestPropertySheet extends CircumstancePropertySheet {

    public RequestPropertySheet(Manifold manifold) {
        super(manifold);
    }

    @Override
    protected void getSubclassProperties(CircumstanceImpl circumstance, List<PropertySheet.Item> itemList) {
        RequestCircumstanceImpl request = (RequestCircumstanceImpl) circumstance;
        
        // TODO SimpleListProperty<StatementAssociation> conditionalTriggersProperty()
        
        // TODO SimpleListProperty<? extends Participant> requestedParticipantsProperty()
        
        // TODO SimpleObjectProperty<LogicalExpression> priorityProperty()

        // TODO SimpleListProperty<Repetition> repetitionsProperty()

        itemList.add(new PropertySheetMeasureWrapper(manifold, request.requestedMeasureProperty()));
    }
    
}
