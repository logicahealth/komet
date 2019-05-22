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

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import sh.isaac.api.query.SortSpecification;

/**
 *
 * @author kec
 */
public class MenuItemForSortSpecification extends MenuItem {
    final SortSpecification rowToAdd;
    final ObservableList<SortSpecification> sortSpecificationRows;

    public MenuItemForSortSpecification(String menuText, SortSpecification rowToAdd, ObservableList<SortSpecification> sortSpecificationRows) {
        super(menuText);
        this.rowToAdd = rowToAdd;
        this.sortSpecificationRows = sortSpecificationRows;
        this.setOnAction(this::addRow);
    }
    
    private void addRow(ActionEvent action) {
        this.sortSpecificationRows.add(new SortSpecification(rowToAdd));
    }

    @Override
    public String toString() {
        return "MenuItemForSortSpecification{" +
                getText() +
                '}';
    }
}
