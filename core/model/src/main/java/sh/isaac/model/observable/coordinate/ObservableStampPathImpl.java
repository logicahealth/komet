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

import java.util.List;
import java.util.stream.Collectors;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.collections.FXCollections;

import sh.isaac.api.coordinate.StampPath;
import sh.isaac.api.observable.coordinate.ObservableStampPath;
import sh.isaac.api.observable.coordinate.ObservableStampPosition;
import sh.isaac.model.coordinate.StampPathImpl;
import sh.isaac.model.observable.ObservableFields;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class ObservableStampPathImpl
        extends ObservableCoordinateImpl
         implements ObservableStampPath {
   StampPathImpl                                stampPath;
   ReadOnlyIntegerProperty                      pathConceptSequenceProperty;
   ReadOnlyListWrapper<ObservableStampPosition> pathOriginsProperty;

   //~--- constructors --------------------------------------------------------

   public ObservableStampPathImpl(StampPath stampPath) {
      this.stampPath = (StampPathImpl) stampPath;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(StampPath o) {
      return stampPath.compareTo(o);
   }

   @Override
   public ReadOnlyIntegerProperty pathConceptSequenceProperty() {
      if (pathConceptSequenceProperty == null) {
         pathConceptSequenceProperty = new SimpleIntegerProperty(this,
               ObservableFields.PATH_SEQUENCE_FOR_STAMP_PATH.toExternalString(),
               getPathConceptSequence());
      }

      return pathConceptSequenceProperty;
   }

   @Override
   public ReadOnlyListProperty<ObservableStampPosition> pathOriginsProperty() {
      if (pathOriginsProperty == null) {
         pathOriginsProperty = new ReadOnlyListWrapper<>(this,
               ObservableFields.PATH_ORIGIN_LIST_FOR_STAMP_PATH.toExternalString(),
               FXCollections.<ObservableStampPosition>observableList(getPathOrigins()));
      }

      return pathOriginsProperty;
   }

   @Override
   public String toString() {
      return "ObservableStampPathImpl{" + stampPath + '}';
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getPathConceptSequence() {
      if (pathConceptSequenceProperty != null) {
         return pathConceptSequenceProperty.get();
      }

      return stampPath.getPathConceptSequence();
   }

   @Override
   public List<ObservableStampPosition> getPathOrigins() {
      if (pathOriginsProperty != null) {
         return pathOriginsProperty.get();
      }

      return stampPath.getPathOrigins()
                      .stream()
                      .map((origin) -> new ObservableStampPositionImpl(origin))
                      .collect(Collectors.toList());
   }
}

