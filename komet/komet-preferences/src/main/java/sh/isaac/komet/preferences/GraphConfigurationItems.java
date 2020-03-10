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
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.UuidStringKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.BackingStoreException;

import static sh.isaac.komet.preferences.GraphConfigurationItemPanel.Keys.*;
import static sh.isaac.komet.preferences.coordinate.ManifoldCoordinateGroupPanel.*;
import static sh.komet.gui.contract.preferences.GraphConfigurationItem.*;
import static sh.komet.gui.contract.preferences.PreferenceGroup.Keys.GROUP_NAME;

/**
 *
 * @author kec
 */
public class GraphConfigurationItems extends ParentPanel  {


    public GraphConfigurationItems(IsaacPreferences preferencesNode, Manifold manifold,
                                   KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "View configurations"),
                manifold, kpc);
        if (!initialized()) {
            // Add four defaults: defining all, defining active only, defining active only FQN, stated all...

            // Defining all
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(DEFINING_ALL.getUuid().toString());
                childPreferences.put(GROUP_NAME, DEFINING_ALL.getString());
                childPreferences.put(ITEM_NAME, DEFINING_ALL.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, INFERRED_GRAPH_NAVIGATION_ANY_NODE_MANIFOLD_KEY.toStringArray());
                childPreferences = addChild(DEFINING_ALL.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, manifold, kpc);
            }

            // Defining active only
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(DEFINING_ACTIVE.getUuid().toString());
                childPreferences.put(GROUP_NAME, DEFINING_ACTIVE.getString());
                childPreferences.put(ITEM_NAME, DEFINING_ACTIVE.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, INFERRED_GRAPH_NAVIGATION_ACTIVE_NODES_MANIFOLD_KEY.toStringArray());
                childPreferences = addChild(DEFINING_ACTIVE.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, manifold, kpc);
            }

            // Defining active only FQN...
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(DEFINING_ACTIVE_FQN.getUuid().toString());
                childPreferences.put(GROUP_NAME, DEFINING_ACTIVE_FQN.getString());
                childPreferences.put(ITEM_NAME, DEFINING_ACTIVE_FQN.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, INFERRED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES_MANIFOLD_KEY.toStringArray());
                childPreferences = addChild(DEFINING_ACTIVE_FQN.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, manifold, kpc);
            }

            // Stated, nodes of all status
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(STATED_ALL.getUuid().toString());
                childPreferences.put(GROUP_NAME, STATED_ALL.getString());
                childPreferences.put(ITEM_NAME, STATED_ALL.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, STATED_GRAPH_NAVIGATION_ACTIVE_FQN_NODES_MANIFOLD_KEY.toStringArray());
                childPreferences = addChild(STATED_ALL.getUuid().toString(), GraphConfigurationItemPanel.class);
                new GraphConfigurationItemPanel(childPreferences, manifold, kpc);
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
