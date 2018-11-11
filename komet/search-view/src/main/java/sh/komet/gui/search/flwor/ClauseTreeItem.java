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
package sh.komet.gui.search.flwor;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import sh.isaac.api.query.Clause;

/**
 *
 * @author kec
 */
public class ClauseTreeItem extends TreeItem<QueryClause> {

    {
        getChildren().addListener(this::updateClauseAfterChildrenChanged);
    }

    public ClauseTreeItem() {
    }

    public ClauseTreeItem(QueryClause value) {
        super(value);
    }

    public ClauseTreeItem(QueryClause value, Node graphic) {
        super(value, graphic);
    }

    private void updateClauseAfterChildrenChanged(ListChangeListener.Change<? extends TreeItem<QueryClause>> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    //permutate
                }
            } else if (c.wasUpdated()) {
                //update item
            } else {
                for (TreeItem<QueryClause> remitem : c.getRemoved()) {
                    if (remitem.getParent() != null) {
                        Clause parentClause = remitem.getParent().getValue().getClause();
                        Clause removedClause = remitem.getValue().getClause();
                        removedClause.removeParent(parentClause);
                    }
                }
                for (TreeItem<QueryClause> additem : c.getAddedSubList()) {
                    if (additem.getParent() != null) {
                        Clause parentClause = additem.getParent().getValue().getClause();
                        Clause addedClause = additem.getValue().getClause();
                        addedClause.setParent(parentClause);
                    }
                }
            }
        }
    }
}
