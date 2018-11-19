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

import java.util.Collection;
import sh.isaac.api.query.AttributeFunction;
import sh.isaac.api.query.LetItemKey;
import sh.isaac.api.query.AttributeSpecification;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.MenuItem;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.query.QueryFieldSpecification;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ControllerForReturnSpecification extends ControllerForSpecification {


    final ObservableList<AttributeSpecification> returnSpecificationRows
            = FXCollections.observableArrayList(returnSpecificationRow
                    -> new Observable[]{
                returnSpecificationRow.columnNameProperty()});

    public ControllerForReturnSpecification(SimpleListProperty<ConceptSpecification> forAssemblagesProperty,
            ObservableMap<LetItemKey, Object> letItemObjectMap,
            ObservableList<AttributeFunction> cellFunctions,
            ObservableList<ConceptSpecification> joinProperties,
            ObservableList<MenuItem> addFieldItems,
            Manifold manifold) {
        super(forAssemblagesProperty, manifold, addFieldItems, joinProperties, letItemObjectMap, cellFunctions);
    }

    public ObservableList<MenuItem> getAddFieldItems() {
        return addFieldItems;
    }

    @Override
    protected void clearForChange() {
       returnSpecificationRows.clear();
    }
    
    public ObservableList<AttributeSpecification> getReturnSpecificationRows() {
        return returnSpecificationRows;
    }

    public ObservableList<ConceptSpecification> getJoinProperties() {
        return joinProperties;
    }


    public void addReturnSpecificationListener(ListChangeListener<? super AttributeSpecification> listener) {
        returnSpecificationRows.addListener(listener);
    }

    public void removeReturnSpecificationListener(ListChangeListener<? super AttributeSpecification> listener) {
        returnSpecificationRows.removeListener(listener);
    }

    void reset() {
        this.returnSpecificationRows.clear();
        this.addFieldItems.clear();
        this.forAssemblagesProperty.clear();
        this.joinProperties.clear();
        this.lastStampCoordinateKey = null;
        this.letItemObjectMap.clear();
    }

    @Override
    protected Collection<? extends QueryFieldSpecification> getSpecificationRows() {
         return returnSpecificationRows;
    }

    @Override
    protected MenuItem makeMenuItem(String specificationName, QueryFieldSpecification queryFieldSpecification) {
        return new MenuItemForReturnSpecification("Add " + specificationName, (AttributeSpecification) queryFieldSpecification, returnSpecificationRows);
    }

    @Override
    protected QueryFieldSpecification makeQueryFieldSpecification(AttributeFunction attributeFunction, String specificationName, int assemblageNid, ConceptSpecification propertySpecification, int propertyIndex) {
        return new AttributeSpecification(new AttributeFunction(""), specificationName, assemblageNid, propertySpecification, lastStampCoordinateKey, propertyIndex);
    }

}
