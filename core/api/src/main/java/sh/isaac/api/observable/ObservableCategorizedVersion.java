/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.api.observable;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyProperty;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.CategorizedVersion;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.CommitStates;
import sh.isaac.api.coordinate.EditCoordinate;

/**
 *
 * @author kec
 */
public class ObservableCategorizedVersion extends CategorizedVersion implements ObservableVersion {

   public ObservableCategorizedVersion(ObservableVersion delegate, CategorizedVersions categorizedVersions) {
      super(delegate, categorizedVersions);
   }
      
   public ObservableVersion getObservableVersion() {
      return unwrap();
   }

   @Override
   public IntegerProperty authorNidProperty() {
      return getObservableVersion().authorNidProperty();
   }

   @Override
   public ObjectProperty<CommitStates> commitStateProperty() {
      return getObservableVersion().commitStateProperty();
   }

   @Override
   public IntegerProperty moduleNidProperty() {
      return getObservableVersion().moduleNidProperty();
   }

   @Override
   public IntegerProperty pathNidProperty() {
      return getObservableVersion().pathNidProperty();
   }

   @Override
   public ReadOnlyIntegerProperty stampSequenceProperty() {
      return getObservableVersion().stampSequenceProperty();
   }

   @Override
   public ObjectProperty<Status> stateProperty() {
      return getObservableVersion().stateProperty();
   }

   @Override
   public LongProperty timeProperty() {
      return getObservableVersion().timeProperty();
   }

   @Override
   public ObservableChronology getChronology() {
      return getObservableVersion().getChronology();
   }

   @Override
   public List<ReadOnlyProperty<?>> getProperties() {
      return getObservableVersion().getProperties();
   }

   @Override
   public <T> Optional<T> getUserObject(String objectKey) {
      return getObservableVersion().getUserObject(objectKey);
   }

   @Override
   public void putUserObject(String objectKey, Object object) {
      getObservableVersion().putUserObject(objectKey, object);
   }

   @Override
   public <T> Optional<T> removeUserObject(String objectKey) {
      return getObservableVersion().removeUserObject(objectKey);
   }

    @Override
    public List<Property<?>> getEditableProperties() {
       return getObservableVersion().getEditableProperties();
    }

    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        return getObservableVersion().makeAutonomousAnalog(ec);
    }

    @Override
    public Chronology createIndependentChronicle() {
        return getObservableVersion().createIndependentChronicle();
    }

    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        return getObservableVersion().createIndependentChronicle();
    }
}
