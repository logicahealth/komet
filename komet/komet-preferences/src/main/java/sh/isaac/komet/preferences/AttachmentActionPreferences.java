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
package sh.isaac.komet.preferences;

import java.util.prefs.BackingStoreException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.control.concept.PropertySheetItemConceptWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 * Attachment actions are provided by rules
 * 
 * Rules are stored in assemblages
 * 
 * One string semantic == 1 rule?
 * One membership semantic = 1 rule?
 * 
 * Attachments action rules may need to know: 
 *      Version type to show within
 * 
 *      Assemblage concept
 * 
 *      Does versioned component already have member in assemblage?
 *      Within assemblage semantic referencing component exists, and if so it's value
 * 
 *      Properties to edit (active, text, ...) (not for membership and string)
 * 
 *      if a concept property, provide a list with a default? A search? A create?
 * 
 * @author kec
 */
public class AttachmentActionPreferences extends AbstractPreferences {
    public enum Keys {
        ACTION_NAME,
        ASSEMBLAGE,
        MODULE,
        PATH
    };

    private final SimpleStringProperty actionName
            = new SimpleStringProperty(this, MetaData.ASSEMBLAGE_MEMBERSHIP_TYPE____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> assemblage
            = new SimpleObjectProperty(this, MetaData.ASSEMBLAGE_MEMBERSHIP_TYPE____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> module
            = new SimpleObjectProperty(this, MetaData.MODULE____SOLOR.toExternalString());

    private final SimpleObjectProperty<ConceptSpecification> path
            = new SimpleObjectProperty(this, MetaData.PATH____SOLOR.toExternalString());

    public AttachmentActionPreferences(IsaacPreferences preferencesNode, Manifold manifold) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "Custom actions"), manifold);
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, actionName));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, assemblage));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, module));
        getItemList().add(new PropertySheetItemConceptWrapper(manifold, path));
    }
    
    @Override
    void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.ACTION_NAME, actionName.get());
        getPreferencesNode().putConceptSpecification(Keys.ASSEMBLAGE, assemblage.get());
        getPreferencesNode().putConceptSpecification(Keys.MODULE, module.get());
        getPreferencesNode().putConceptSpecification(Keys.PATH, path.get());
    }

    @Override
    final void revertFields() {
        actionName.set(getPreferencesNode().get(Keys.ACTION_NAME, "Action name"));
        assemblage.set(getPreferencesNode().getConceptSpecification(Keys.ASSEMBLAGE, TermAux.ASSEMBLAGE));
        module.set(getPreferencesNode().getConceptSpecification(Keys.MODULE, TermAux.SOLOR_OVERLAY_MODULE));
        path.set(getPreferencesNode().getConceptSpecification(Keys.PATH, TermAux.DEVELOPMENT_PATH));
    }
    
}
