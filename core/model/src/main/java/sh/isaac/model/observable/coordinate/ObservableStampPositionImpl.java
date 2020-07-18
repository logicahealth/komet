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

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampPosition;
import sh.isaac.api.coordinate.StampPositionImmutable;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.equalitybased.SimpleEqualityBasedObjectProperty;

import java.util.Objects;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 * The Class ObservableStampPositionImpl.
 *
 * @author kec
 */
public class ObservableStampPositionImpl
        extends ObservableStampPositionBase {

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable stamp position impl.
    *
    * @param stampPosition the stamp position
    */
   public ObservableStampPositionImpl(StampPositionImmutable stampPosition, String coordinateName) {
      super(stampPosition, coordinateName);
   }
   public ObservableStampPositionImpl(StampPositionImmutable stampPosition) {
      super(stampPosition, "Stamp position");
   }

   @Override
   public void setExceptOverrides(StampPositionImmutable updatedCoordinate) {
      setValue(updatedCoordinate);
   }

   protected ObjectProperty<ConceptSpecification> makePathConceptProperty(StampPosition stampPosition) {
      return new SimpleEqualityBasedObjectProperty(this,
              ObservableFields.PATH_FOR_PATH_COORDINATE.toExternalString(),
              stampPosition.getPathConcept());
   }

   protected LongProperty makeTimeProperty(StampPosition stampPosition) {
      return new SimpleLongProperty(this,
              ObservableFields.TIME_FOR_STAMP_POSITION.toExternalString(),
              stampPosition.getTime());
   }

}

