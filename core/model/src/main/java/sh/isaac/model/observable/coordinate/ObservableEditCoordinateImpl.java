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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableEditCoordinateImpl.
 *
 * @author kec
 */
public class ObservableEditCoordinateImpl
        extends ObservableEditCoordinateBase {

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable edit coordinate impl.
    *
    * @param editCoordinate the edit coordinate
    */
   public ObservableEditCoordinateImpl(EditCoordinateImmutable editCoordinate, String coordinateName) {
       super(editCoordinate, coordinateName);
   }

    public ObservableEditCoordinateImpl(EditCoordinateImmutable editCoordinate) {
        super(editCoordinate, "Edit coordinate");
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makePathProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.PATH_NID_FOR_EDIT_CORDINATE.toExternalString(),
                editCoordinate.getPath());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeModuleProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
                editCoordinate.getModule());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE.toExternalString(),
                editCoordinate.getAuthor());
    }
}

