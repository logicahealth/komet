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

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPath;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.SimpleEqualityBasedSetProperty;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPathImpl.
 *
 * @author kec
 */

public class ObservableStampPathImpl
        extends ObservableCoordinateImpl<StampPathImmutable>
         implements ObservableStampPath {

   /** The path concept property. */
   final ObjectProperty<ConceptSpecification> pathConceptProperty;

    final SimpleEqualityBasedSetProperty<StampPositionImmutable> pathOriginsProperty;

    final SimpleEqualityBasedListProperty<StampPositionImmutable> pathOriginsAsList;


   //~--- constructors --------------------------------------------------------

    private ObservableStampPathImpl(int pathConceptNid,
                                    ImmutableSet<StampPositionImmutable> origins) {
        this(StampPathImmutable.make(pathConceptNid, origins));
    }

    private ObservableStampPathImpl(StampPathImmutable stampPathImmutable) {
        super(stampPathImmutable);
        this.pathConceptProperty = new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampPathImmutable.getPathConcept());

        this.pathOriginsProperty = new SimpleEqualityBasedSetProperty<>(this,
                ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
                FXCollections.observableSet(stampPathImmutable.getPathOrigins().toSet()));

        this.pathOriginsAsList = new SimpleEqualityBasedListProperty<>(this,
                ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
                FXCollections.observableList(stampPathImmutable.getPathOrigins().toList()));

        addListeners();
    }

    @Override
    protected void addListeners() {
        this.pathConceptProperty.addListener(this::pathConceptChanged);
        this.pathOriginsProperty.addListener(this::pathOriginsSetChanged);
        this.pathOriginsAsList.addListener(this::pathOriginsListChanged);
    }

    @Override
    protected void removeListeners() {
        this.pathConceptProperty.removeListener(this::pathConceptChanged);
        this.pathOriginsProperty.removeListener(this::pathOriginsSetChanged);
    }

    @Override
    protected void baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPathImmutable> observable, StampPathImmutable oldValue, StampPathImmutable newValue) {
        this.pathConceptProperty.setValue(Get.conceptSpecification(newValue.getPathConceptNid()));
        this.pathOriginsProperty.setValue(FXCollections.observableSet(newValue.getPathOrigins().toSet()));
        this.pathOriginsAsList.setValue(FXCollections.observableList(newValue.getPathOrigins().toList()));
    }

    public static ObservableStampPathImpl make(StampPathImmutable stampPathImmutable) {
        return new ObservableStampPathImpl(stampPathImmutable);
    }

    private void pathOriginsSetChanged(SetChangeListener.Change<? extends StampPositionImmutable> c) {
        this.setValue(StampPathImmutable.make(getPathConcept().getNid(),
                Sets.immutable.withAll(c.getSet())));
    }

    private void pathOriginsListChanged(ListChangeListener.Change<? extends StampPositionImmutable> c) {
        this.setValue(StampPathImmutable.make(getPathConcept().getNid(),
                Sets.immutable.withAll(c.getList())));
    }

    private void pathConceptChanged(ObservableValue<? extends ConceptSpecification> observablePathConcept,
                                    ConceptSpecification oldPathConcept,
                                    ConceptSpecification newPathConcept) {
        this.setValue(StampPathImmutable.make(newPathConcept.getNid(),
                getPathOrigins()));
    }

    @Override
    public ObjectProperty<ConceptSpecification> pathConceptProperty() {
        return this.pathConceptProperty;
    }


   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "ObservableStampPathImpl{" + this.getValue().toString() + '}';
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
    public SetProperty<StampPositionImmutable> pathOriginsProperty() {
        return this.pathOriginsProperty;
    }

    @Override
    public ListProperty<StampPositionImmutable> pathOriginsAsListProperty() {
        return this.pathOriginsAsList;
    }

    @Override
    public StampPath getStampPath() {
        return this.getValue();
    }

    @Override
    public int getPathConceptNid() {
        return this.getValue().getPathConceptNid();
    }

    @Override
    public ImmutableSet<StampPositionImmutable> getPathOrigins() {
        return this.getValue().getPathOrigins();
    }

    @Override
    public StampPathImmutable toStampPathImmutable() {
        return getValue();
    }
}

