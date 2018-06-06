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
package sh.komet.gui.control.participant;

import java.util.ArrayList;
import java.util.List;
import org.controlsfx.control.PropertySheet;
import sh.isaac.model.statement.ParticipantImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ParticipantPropertySheet {
    
    protected final Manifold manifold;
    
    
    private final PropertySheet propertySheet = new PropertySheet();
    {
        this.propertySheet.setMode(PropertySheet.Mode.NAME);
        this.propertySheet.setSearchBoxVisible(false);
        this.propertySheet.setModeSwitcherVisible(false);
        
    }

    public ParticipantPropertySheet(Manifold manifold) {
        this.manifold = manifold;
        this.propertySheet.setPropertyEditorFactory(new PropertyEditorFactory(this.manifold));
    }
    
    public PropertySheet getPropertySheet() {
        return propertySheet;
    }
    
    public void setParticipant(ParticipantImpl participant) {
        
        propertySheet.getItems().clear();
        if (participant != null) {
            propertySheet.getItems().addAll(getProperties(participant));
        }
    }

    private List<PropertySheet.Item> getProperties(ParticipantImpl participant) {
       ArrayList<PropertySheet.Item> itemList = new ArrayList<>();
       
       itemList.add(new PropertySheetItemConceptWrapper(manifold, participant.participantRoleProperty()));
       // TODO add UUID editor...

       return itemList;
    }
}