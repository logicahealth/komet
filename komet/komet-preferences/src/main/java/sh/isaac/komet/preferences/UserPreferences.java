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
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class UserPreferences extends AbstractPreferences {

    //PropertySheetItem moduleProperty = createPropertyItem(MetaData.MODULE____SOLOR.);
    //PropertySheetItem pathProperty = createPropertyItem(concept.pathNidProperty());

    public UserPreferences(IsaacPreferences preferencesNode, Manifold manifold) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "User"), manifold);
        //getItemList().add(moduleProperty);
        //getItemList().add(pathProperty);
        save();
    }

    @Override
    void saveFields() throws BackingStoreException {
        // Nothing to do. 
    }

    @Override
    void revertFields() {
        // Nothing to do. 
    }

}
    

