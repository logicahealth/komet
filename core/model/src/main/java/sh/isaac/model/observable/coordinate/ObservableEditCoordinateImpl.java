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

import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
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
   public ObservableEditCoordinateImpl(EditCoordinate editCoordinate, String coordinateName) {
       super(editCoordinate.toEditCoordinateImmutable(), coordinateName);
   }

    @Override
    protected EditCoordinateImmutable baseCoordinateChangedListenersRemoved(ObservableValue<? extends EditCoordinateImmutable> observable, EditCoordinateImmutable oldValue, EditCoordinateImmutable newValue) {
        this.authorForChangesProperty().setValue(newValue.getAuthorForChanges());
        this.defaultModuleProperty().setValue(newValue.getDefaultModule());
        this.destinationModuleProperty().setValue(newValue.getDestinationModule());
        this.promotionPathProperty().setValue(newValue.getPromotionPath());
        return newValue;
    }

    @Override
    public void setExceptOverrides(EditCoordinateImmutable updatedCoordinate) {
        setValue(updatedCoordinate);
    }

    public ObservableEditCoordinateImpl(EditCoordinate editCoordinate) {
        super(editCoordinate.toEditCoordinateImmutable(), "Edit coordinate");
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makePromotionPathProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.PATH_NID_FOR_EDIT_CORDINATE.toExternalString(),
                editCoordinate.getPromotionPath());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDefaultModuleProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty(this,
                ObservableFields.MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
                editCoordinate.getDefaultModule());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeAuthorForChangesProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.AUTHOR_NID_FOR_EDIT_COORDINATE.toExternalString(),
                editCoordinate.getAuthorForChanges());
    }

    @Override
    protected SimpleEqualityBasedObjectProperty<ConceptSpecification> makeDestinationModuleProperty(EditCoordinate editCoordinate) {
        return new SimpleEqualityBasedObjectProperty<>(this,
                ObservableFields.DESTINATION_MODULE_NID_FOR_EDIT_COORDINATE.toExternalString(),
                editCoordinate.getDestinationModule());
    }

    @Override
    public EditCoordinateImmutable getOriginalValue() {
        return getValue();
    }
}

