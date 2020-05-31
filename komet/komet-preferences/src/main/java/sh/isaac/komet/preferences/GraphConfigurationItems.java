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
import sh.isaac.api.coordinate.*;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.model.observable.coordinate.ObservableManifoldCoordinateImpl;
import sh.komet.gui.contract.preferences.KometPreferencesController;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


    public GraphConfigurationItems(IsaacPreferences preferencesNode, ViewProperties viewProperties,
                                   KometPreferencesController kpc) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "View configurations"),
                viewProperties, kpc);
        if (!initialized()) {
            // Add four defaults: inferred Preferred, inferred FQN, stated FQN, stated Preferred...

            // inferred Preferred
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(INFERRED_PREFERRED.getUuid().toString());
                childPreferences.put(GROUP_NAME, INFERRED_PREFERRED.getString());
                childPreferences.put(ITEM_NAME, INFERRED_PREFERRED.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, INFERRED_PREFERRED.toStringArray());
                childPreferences = addChild(INFERRED_PREFERRED.getUuid().toString(), GraphConfigurationItemPanel.class);
                FxGet.manifoldCoordinates().put(INFERRED_PREFERRED,
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(
                                VertexSortPreferredName.SINGLETON,
                                Coordinates.Digraph.DevelopmentInferred(),
                                Coordinates.Filter.DevelopmentLatest()
                        ))
                );
                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            // inferred FQN,
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(INFERRED_FQN.getUuid().toString());
                childPreferences.put(GROUP_NAME, INFERRED_FQN.getString());
                childPreferences.put(ITEM_NAME, INFERRED_FQN.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, INFERRED_FQN.toStringArray());
                childPreferences = addChild(INFERRED_FQN.getUuid().toString(), GraphConfigurationItemPanel.class);
                FxGet.manifoldCoordinates().put(INFERRED_FQN,
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(
                                VertexSortPreferredName.SINGLETON,
                                Coordinates.Digraph.DevelopmentInferred(),
                                Coordinates.Filter.DevelopmentLatest()
                        ))
                );
                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            // Stated FQN...
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(STATED_FQN.getUuid().toString());
                childPreferences.put(GROUP_NAME, STATED_FQN.getString());
                childPreferences.put(ITEM_NAME, STATED_FQN.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, STATED_FQN.toStringArray());
                childPreferences = addChild(STATED_FQN.getUuid().toString(), GraphConfigurationItemPanel.class);
                FxGet.manifoldCoordinates().put(STATED_FQN,
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(
                                VertexSortFullyQualifiedName.SINGLETON,
                                Coordinates.Digraph.DevelopmentStated(),
                                Coordinates.Filter.DevelopmentLatest()
                        ))
                );


                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            // Stated, Preferred
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(STATED_PREFERRED.getUuid().toString());
                childPreferences.put(GROUP_NAME, STATED_PREFERRED.getString());
                childPreferences.put(ITEM_NAME, STATED_PREFERRED.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.SOLOR_ROOT));
                childPreferences.putConceptList(TREES, new ArrayList<>());
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, true);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, STATED_PREFERRED.toStringArray());
                childPreferences = addChild(STATED_PREFERRED.getUuid().toString(), GraphConfigurationItemPanel.class);
                FxGet.manifoldCoordinates().put(STATED_PREFERRED,
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(
                                VertexSortPreferredName.SINGLETON,
                                Coordinates.Digraph.DevelopmentStated(),
                                Coordinates.Filter.DevelopmentLatest()
                        ))
                );
                new GraphConfigurationItemPanel(childPreferences, viewProperties, kpc);
            }

            // Path tree
            {
                IsaacPreferences childPreferences = getPreferencesNode().node(PATH_TREE.getUuid().toString());
                childPreferences.put(GROUP_NAME, PATH_TREE.getString());
                childPreferences.put(ITEM_NAME, PATH_TREE.getString());
                childPreferences.putConceptList(ROOTS, List.of(TermAux.PRIMORDIAL_PATH));
                childPreferences.putConceptList(TREES, Arrays.asList(TermAux.PATH_ORIGIN_ASSEMBLAGE));
                childPreferences.putConceptList(INVERSE_TREES, new ArrayList<>());
                childPreferences.putBoolean(INCLUDE_DEFINING_TAXONOMY, false);
                childPreferences.putArray(MANIFOLD_COORDINATE_KEY, PATH_TREE.toStringArray());
                childPreferences = addChild(PATH_TREE.getUuid().toString(), GraphConfigurationItemPanel.class);
                FxGet.manifoldCoordinates().put(PATH_TREE,
                        new ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable.make(
                                VertexSortPreferredName.SINGLETON,
                                Coordinates.Digraph.DevelopmentStated(),
                                Coordinates.Filter.DevelopmentLatest()
                        ))
                );
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
