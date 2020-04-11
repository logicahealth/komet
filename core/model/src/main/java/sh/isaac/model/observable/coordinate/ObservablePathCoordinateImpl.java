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
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.PathCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservablePathCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservablePathCoordinateImpl.
 *
 * @author kec
 */

public class ObservablePathCoordinateImpl
        extends ObservableCoordinateImpl<PathCoordinateImmutable>
         implements ObservablePathCoordinate {

   /** The path concept property. */
   final ObjectProperty<ConceptSpecification> pathConceptProperty;

   /** The module specifications property. */
   final SimpleEqualityBasedSetProperty<ConceptSpecification> moduleSpecificationsProperty;



   //~--- constructors --------------------------------------------------------

    private ObservablePathCoordinateImpl(int pathConceptNid,
                                        ImmutableIntSet moduleNids) {
        this(PathCoordinateImmutable.make(pathConceptNid, moduleNids));
    }

    private ObservablePathCoordinateImpl(PathCoordinateImmutable pathCoordinateImmutable) {
        super(pathCoordinateImmutable);
        this.pathConceptProperty = new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                pathCoordinateImmutable.getPathForCoordinate());

        this.moduleSpecificationsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.MODULE_SPECIFICATION_SET_FOR_STAMP_COORDINATE.toExternalString(),
                FXCollections.observableSet(pathCoordinateImmutable.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet()));

        addListeners();
    }

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this::pathConceptChanged);
        this.moduleSpecificationsProperty.addListener(this::moduleSetChanged);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this::pathConceptChanged);
        this.moduleSpecificationsProperty.removeListener(this::moduleSetChanged);
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends PathCoordinateImmutable> observable, PathCoordinateImmutable oldValue, PathCoordinateImmutable newValue) {
        pathConceptProperty.setValue(Get.conceptSpecification(newValue.getPathNidForCoordinate()));
        moduleSpecificationsProperty.setValue(FXCollections.observableSet(newValue.getModuleNids().collect(nid -> Get.conceptSpecification(nid)).castToSet()));
    }

    public static ObservablePathCoordinateImpl make(PathCoordinateImmutable pathCoordinateImmutable) {
        return new ObservablePathCoordinateImpl(pathCoordinateImmutable);
    }


    private void moduleSetChanged(SetChangeListener.Change<? extends ConceptSpecification> c) {
        this.setValue(PathCoordinateImmutable.make(getPathConceptForCoordinate().getNid(),
                IntSets.immutable.of(c.getSet().stream().mapToInt(value -> value.getNid()).toArray())));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(PathCoordinateImmutable.make(newPathConcept.getNid(), getModuleNids()));
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathConceptProperty() {
        return this.pathConceptProperty;
    }

    @Override
    public PathCoordinateImmutable getPathCoordinate() {
        return getValue();
    }

    @Override
    public PathCoordinateImmutable toPathCoordinateImmutable() {
        return getValue();
    }

    @Override
    public String toUserString() {
        return getValue().toUserString();
    }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservablePathCoordinateImpl{" + this.getValue().toString() + '}';
   }

   @Override
   public int hashCode() {
      return this.getValue().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return this.getValue().equals(obj);
   }

    @Override
    public SetProperty<ConceptSpecification> moduleSpecificationsProperty() {
        return this.moduleSpecificationsProperty;
    }
}

