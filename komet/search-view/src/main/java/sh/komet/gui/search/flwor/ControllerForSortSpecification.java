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

import java.util.List;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.AttributeFunction;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.QueryFieldSpecification;
import sh.isaac.api.query.SortSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ControllerForSortSpecification extends ControllerForSpecification {
    final ObservableList<SortSpecification> sortSpecificationRows
            = FXCollections.observableArrayList();
    
    public ControllerForSortSpecification(SimpleListProperty<ConceptSpecification> forAssemblagesProperty,
            ObservableList<LetItemKey> letItemKeys,
            ObservableMap<LetItemKey, Object> letItemObjectMap,
            ObservableList<AttributeFunction> cellFunctions,
            ObservableList<ConceptSpecification> joinProperties,
            ObservableList<MenuItem> addFieldItems,
            TableView<List<String>> resultTable,
            Manifold manifold) {
        super(forAssemblagesProperty, manifold, letItemKeys, addFieldItems, joinProperties, letItemObjectMap, cellFunctions, resultTable);
        this.setupAttributeFunctions();
    }

    @Override
    protected void clearForChange() {
        sortSpecificationRows.clear();
    }

    @Override
    protected ObservableList<SortSpecification> getSpecificationRows() {
        return sortSpecificationRows;
    }

    @Override
    protected MenuItem makeMenuItem(String specificationName, QueryFieldSpecification queryFieldSpecification) {
        return new MenuItemForSortSpecification("Add " + specificationName, (SortSpecification) queryFieldSpecification, getSpecificationRows());
    }

    @Override
    protected QueryFieldSpecification makeQueryFieldSpecification(AttributeFunction attributeFunction, String specificationName, int assemblageNid, ConceptSpecification propertySpecification, int propertyIndex) {
        return new SortSpecification(attributeFunction, specificationName, assemblageNid, propertySpecification, TableColumn.SortType.ASCENDING, lastStampCoordinateKey, propertyIndex);
    }

}
