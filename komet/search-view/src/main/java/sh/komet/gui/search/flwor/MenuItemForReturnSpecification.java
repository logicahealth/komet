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

import sh.isaac.api.query.AttributeSpecification;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

/**
 *
 * @author kec
 */
public class MenuItemForReturnSpecification extends MenuItem {
    final AttributeSpecification rowToAdd;
    final ObservableList<AttributeSpecification> returnSpecificationRows;

    public MenuItemForReturnSpecification(String menuText, AttributeSpecification rowToAdd, ObservableList<AttributeSpecification> returnSpecificationRows) {
        super(menuText);
        this.rowToAdd = rowToAdd;
        this.returnSpecificationRows = returnSpecificationRows;
        this.setOnAction(this::addRow);
    }
    
    private void addRow(ActionEvent action) {
        this.returnSpecificationRows.add(new AttributeSpecification(rowToAdd));
    }
    
}
