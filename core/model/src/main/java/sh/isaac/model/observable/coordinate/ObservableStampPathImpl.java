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
import org.eclipse.collections.api.set.ImmutableSet;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.coordinate.StampPathImmutable;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedListProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedSetProperty;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPathImpl.
 *
 * @author kec
 */

public class ObservableStampPathImpl
        extends ObservableStampPathBase {


   //~--- constructors --------------------------------------------------------

    private ObservableStampPathImpl(int pathConceptNid,
                                    ImmutableSet<StampPositionImmutable> origins) {
        this(StampPathImmutable.make(pathConceptNid, origins));
    }

    private ObservableStampPathImpl(StampPathImmutable stampPathImmutable, String coordinateName) {
        super(stampPathImmutable, coordinateName);
    }
    private ObservableStampPathImpl(StampPathImmutable stampPathImmutable) {
        super(stampPathImmutable, "Stamp path");
    }

    @Override
    public void setExceptOverrides(StampPathImmutable updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    public static ObservableStampPathImpl make(StampPathImmutable stampPathImmutable) {
        return new ObservableStampPathImpl(stampPathImmutable);
    }

    public static ObservableStampPathImpl make(StampPathImmutable stampPathImmutable, String coordinateName) {
        return new ObservableStampPathImpl(stampPathImmutable, coordinateName);
    }

    @Override
    protected ListProperty<StampPositionImmutable> makePathOriginsAsListProperty(StampPath stampPath) {
        return new SimpleEqualityBasedListProperty<StampPositionImmutable>(this,
                ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
                FXCollections.observableList(stampPath.getPathOrigins().toList()));
    }

    @Override
    protected SetProperty<StampPositionImmutable> makePathOriginsProperty(StampPath stampPath) {
        return new SimpleEqualityBasedSetProperty<StampPositionImmutable>(this,
                ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
                FXCollections.observableSet(stampPath.getPathOrigins().toSet()));
    }

    @Override
    protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampPath stampPath) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
                stampPath.getPathConcept());
    }

    @Override
    public StampPathImmutable getOriginalValue() {
        return getValue();
    }


    @Override
    protected final StampPathImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends StampPathImmutable> observable, StampPathImmutable oldValue, StampPathImmutable newValue) {
        this.pathConceptProperty().setValue(Get.conceptSpecification(newValue.getPathConceptNid()));
        this.pathOriginsProperty().setValue(FXCollections.observableSet(newValue.getPathOrigins().toSet()));
        this.pathOriginsAsListProperty().setValue(FXCollections.observableList(newValue.getPathOrigins().toList()));
        return newValue;
    }

}

