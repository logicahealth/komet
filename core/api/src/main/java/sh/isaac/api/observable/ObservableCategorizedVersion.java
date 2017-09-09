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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import sh.isaac.api.State;
import sh.isaac.api.chronicle.CategorizedVersion;
import sh.isaac.api.chronicle.CategorizedVersions;
import sh.isaac.api.commit.CommitStates;

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
   public IntegerProperty authorSequenceProperty() {
      return getObservableVersion().authorSequenceProperty();
   }

   @Override
   public ObjectProperty<CommitStates> commitStateProperty() {
      return getObservableVersion().commitStateProperty();
   }

   @Override
   public IntegerProperty moduleSequenceProperty() {
      return getObservableVersion().moduleSequenceProperty();
   }

   @Override
   public IntegerProperty pathSequenceProperty() {
      return getObservableVersion().pathSequenceProperty();
   }

   @Override
   public IntegerProperty stampSequenceProperty() {
      return getObservableVersion().stampSequenceProperty();
   }

   @Override
   public ObjectProperty<State> stateProperty() {
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
   public List<Property<?>> getProperties() {
      return getObservableVersion().getProperties();
   }
   
   
}
