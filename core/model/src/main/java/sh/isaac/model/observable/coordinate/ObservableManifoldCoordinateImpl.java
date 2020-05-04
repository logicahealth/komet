/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.model.observable.coordinate;

//~--- JDK imports ------------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.coordinate.*;
import sh.isaac.api.observable.coordinate.*;
import sh.isaac.model.observable.ObservableFields;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableManifoldCoordinateImpl.
 *
 * @author kec
 */
public class ObservableManifoldCoordinateImpl
        extends ObservableCoordinateImpl<ManifoldCoordinateImmutable>
         implements ObservableManifoldCoordinate {

    /**
     *
     * The vertexSort property.
     */
    private final SimpleObjectProperty<VertexSort> vertexSortProperty;

    /**
     *
     * The digraph coordinate property.
     */
    private final SimpleObjectProperty<DigraphCoordinateImmutable> digraphCoordinateImmutableProperty;

    private final ObservableDigraphCoordinateImpl observableDigraphCoordinate;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable taxonomy coordinate impl.
    *
    * @param manifoldCoordinate the taxonomy coordinate
    */
   public ObservableManifoldCoordinateImpl(ManifoldCoordinateImmutable manifoldCoordinate) {
       super(manifoldCoordinate);
       this.vertexSortProperty = new SimpleObjectProperty<>(this,
               ObservableFields.VERTEX_SORT_PROPERTY.toExternalString(),
               manifoldCoordinate.getVertexSort());

       this.observableDigraphCoordinate = new ObservableDigraphCoordinateImpl(manifoldCoordinate.toDigraphImmutable());
       this.digraphCoordinateImmutableProperty = observableDigraphCoordinate.baseCoordinateProperty();
        addListeners();
   }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends ManifoldCoordinateImmutable> observable, ManifoldCoordinateImmutable oldValue, ManifoldCoordinateImmutable newValue) {
        this.vertexSortProperty.setValue(newValue.getVertexSort());
        this.digraphCoordinateImmutableProperty.setValue(newValue.toDigraphImmutable());
    }

    @Override
    protected void addListeners() {
        this.vertexSortProperty.addListener(this::vertexSortChanged);
        this.digraphCoordinateImmutableProperty.addListener(this::digraphChanged);
    }

    @Override
    protected void removeListeners() {
        this.vertexSortProperty.removeListener(this::vertexSortChanged);
        this.digraphCoordinateImmutableProperty.removeListener(this::digraphChanged);
    }

    //~--- methods -------------------------------------------------------------
   private void digraphChanged(ObservableValue<? extends DigraphCoordinateImmutable> observable,
                               DigraphCoordinateImmutable oldValue,
                               DigraphCoordinateImmutable newValue) {
       this.setValue(ManifoldCoordinateImmutable.make(getVertexSort(), newValue, getStampFilter()));
       this.digraphCoordinateImmutableProperty.set(newValue);
   }

    private void vertexSortChanged(ObservableValue<? extends VertexSort> observable,
                                   VertexSort oldValue,
                                   VertexSort newValue) {
        this.setValue(ManifoldCoordinateImmutable.make(newValue, getDigraph(), getStampFilter()));
    }


    @Override
    public ObjectProperty<VertexSort> vertexSortProperty() {
        return vertexSortProperty;
    }

    @Override
    public ObjectProperty<DigraphCoordinateImmutable> digraphCoordinateImmutableProperty() {
        return digraphCoordinateImmutableProperty;
    }

    @Override
    public ObservableDigraphCoordinate getDigraph() {
        return this.observableDigraphCoordinate;
    }

    @Override
    public ObservableLogicCoordinate getLogicCoordinate() {
        return this.observableDigraphCoordinate.getLogicCoordinate();
    }

    @Override
    public ObservableLanguageCoordinate getLanguageCoordinate() {
        return this.observableDigraphCoordinate.getLanguageCoordinate();
    }

    @Override
    public VertexSort getVertexSort() {
        return vertexSortProperty.get();
    }

    @Override
    public ObservableStampFilter getStampFilter() {
        return this.observableDigraphCoordinate.getEdgeStampFilter();
    }

    @Override
    public ObservableStampFilter getLanguageStampFilter() {
        return this.observableDigraphCoordinate.getVertexStampFilter();
    }

    @Override
    public ObservableStampFilter getVertexStampFilter() {
        return this.observableDigraphCoordinate.getVertexStampFilter();
    }

    @Override
    public ManifoldCoordinateImmutable toManifoldCoordinateImmutable() {
        return this.getValue();
    }

    @Override
    public ObservableStampFilter getEdgeStampFilter() {
        return this.observableDigraphCoordinate.getEdgeStampFilter();
    }
}

