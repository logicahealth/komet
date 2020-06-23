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

import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.*;
import static sh.komet.gui.contract.preferences.GraphConfigurationItem.*;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 *
 * @author kec
 */
public class GraphConfigurationItems extends ParentPanel  {


    public GraphConfigurationItems(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                   KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "View configurations"),
                viewProperties, kpc);
        if (!initialized()) {
            // Add two defaults: path tree, logic tree...

            // inferred Preferred
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(PREMISE_DIGRAPH.getUuid().toString());
                childPreferences.put(GROUP_NAME, PREMISE_DIGRAPH.getString());
                childPreferences.put(ITEM_NAME, PREMISE_DIGRAPH.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences = addChild(PREMISE_DIGRAPH.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            // Path tree
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(PATH_DIGRAPH.getUuid().toString());
                childPreferences.put(GROUP_NAME, PATH_DIGRAPH.getString());
                childPreferences.put(ITEM_NAME, PATH_DIGRAPH.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.PRIMORDIAL_PATH));
                childPreferences.putConceptList(TREES, Arrays.asList(TermAux.PATH_ORIGIN_ASSEMBLAGE));
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, false);
                childPreferences = addChild(PATH_DIGRAPH.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            save();
        }
    }
    @Override
    protected void saveFields() throws BackingStoreException {
        // nothing to save
    }

    @Override
    final protected void revertFields() {
        // nothing to revert
    }

    @Override
    protected Class getChildClass() {
        return GraphConfigurationItemPanel.class;
    }    

}
