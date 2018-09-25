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
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.MetaData;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.PropertySheetTextWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class TaxonomyItemPanel extends AbstractPreferences {

    public enum Keys {
        ITEM_NAME,
    };

    private final SimpleStringProperty nameProperty
            = new SimpleStringProperty(this, MetaData.TAXONOMY_CONFIGURATION_NAME____SOLOR.toExternalString());

    public TaxonomyItemPanel(IsaacPreferences preferencesNode, Manifold manifold,
            KometPreferencesController kpc) {
        super(preferencesNode,
                getGroupName(preferencesNode),
                manifold, kpc);
        nameProperty.set(groupNameProperty().get());
        nameProperty.addListener((observable, oldValue, newValue) -> {
            groupNameProperty().set(newValue);
        });
        revertFields();
        save();
        getItemList().add(new PropertySheetTextWrapper(manifold, nameProperty));

    }
    
    private static String getGroupName(IsaacPreferences preferencesNode) {
        return preferencesNode.get(Keys.ITEM_NAME, "Taxonomy configuration");
    }

    @Override
    final void saveFields() throws BackingStoreException {
        getPreferencesNode().put(Keys.ITEM_NAME, nameProperty.get());
    }

    @Override
    final void revertFields() {
        this.nameProperty.set(getPreferencesNode().get(Keys.ITEM_NAME, getGroupName()));
    }
    
    @Override
    public boolean showDelete() {
        return true;
    }
     
    
}
