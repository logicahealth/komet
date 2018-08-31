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

import java.util.Stack;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public abstract class ActionPreferences extends AbstractPreferences {

    public enum Keys {
        ACTION_ID_LIST
    };
    ObservableList<String> actionUuidList = FXCollections.observableArrayList();
    Stack<PreferencesTreeItem> childrenToAdd = new Stack<>();
    
    public ActionPreferences(IsaacPreferences preferencesNode, String groupName, Manifold manifold, KometPreferencesController kpc) {
        super(preferencesNode, groupName, manifold, kpc);
    }

    @Override
    protected final void addChildren() {
        while (!childrenToAdd.empty()) {
            getTreeItem().getChildren().add(childrenToAdd.pop());
            getTreeItem().setExpanded(true);
        }
    }

    protected final void newAction(ActionEvent action) {
        UUID newUuid = UUID.randomUUID();
        actionUuidList.add(newUuid.toString());
        addActionPanel(newUuid);
    }
    abstract protected void addActionPanel(UUID actionUuid);
    @Override
    public final Node getTopPanel(Manifold manifold) {
        Button addButton = new Button("Add");
        addButton.setOnAction(this::newAction);
        return  new ToolBar(addButton);
    }
    
}
