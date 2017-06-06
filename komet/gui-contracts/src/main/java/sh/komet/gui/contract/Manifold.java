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
package sh.komet.gui.contract;

import java.util.UUID;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.coordinate.LanguageCoordinateProxy;
import sh.isaac.api.coordinate.LogicCoordinateProxy;
import sh.isaac.api.coordinate.StampCoordinateProxy;
import sh.isaac.api.coordinate.TaxonomyCoordinateProxy;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.observable.coordinate.ObservableTaxonomyCoordinate;

/**
 * Manifold: Uniting various features, in this case an object that contains a set of coordinates and selections that
 * enable coordinated activities across a set of processes or graphical panels. In geometry, a coordinate system is a
 * system which uses one or more numbers, or coordinates, to uniquely determine the position of a point or other
 * geometric element on a manifold such as Euclidean space. ISAAC uses a number of coordinates, language coordinates,
 * stamp coordinates, edit coordinates to specify how a function should behave, in addition to current state information
 * such as a current position (selection). We use Manifold here to prevent confusion with other types of context,
 * context menus, etc.
 *
 * TODO: Move to general API when better refined.
 *
 * @author kec
 */
public class Manifold implements StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy, TaxonomyCoordinateProxy {

   final SimpleStringProperty nameProperty;
   final SimpleObjectProperty<UUID> manifoldUuidProperty;
   final ObservableTaxonomyCoordinate taxonomyCoordinate;
   final ObservableEditCoordinate editCoordinate;
   final SimpleObjectProperty<IdentifiedObject> focusedObjectProperty;

   public Manifold(String name, UUID manifoldUuid, ObservableTaxonomyCoordinate taxonomyCoordinate, ObservableEditCoordinate editCoordinate) {
      this(name, manifoldUuid, taxonomyCoordinate, editCoordinate, null);
   }
   
  public Manifold(String name, UUID manifoldUuid, ObservableTaxonomyCoordinate taxonomyCoordinate, ObservableEditCoordinate editCoordinate, IdentifiedObject focusedObject) {
      this.nameProperty = new SimpleStringProperty(name);
      this.manifoldUuidProperty = new SimpleObjectProperty<>(manifoldUuid);
      this.taxonomyCoordinate = taxonomyCoordinate;
      this.editCoordinate = editCoordinate;
      this.focusedObjectProperty = new SimpleObjectProperty<>(focusedObject);
   }

   public SimpleStringProperty getNameProperty() {
      return nameProperty;
   }

   public String getName() {
      return nameProperty.getValue();
   }

   public void setName(String name) {
      this.nameProperty.setValue(name);
   }

   public SimpleObjectProperty<UUID> getManifoldUuidProperty() {
      return manifoldUuidProperty;
   }

   public UUID getManifoldUuid() {
      return manifoldUuidProperty.get();
   }

   public void setManifoldUuid(UUID manifoldUuid) {
      this.manifoldUuidProperty.set(manifoldUuid);
   }

   @Override
   public ObservableTaxonomyCoordinate getTaxonomyCoordinate() {
      return taxonomyCoordinate;
   }

   public ObservableEditCoordinate getEditCoordinate() {
      return editCoordinate;
   }

   public SimpleObjectProperty<IdentifiedObject> focusedObjectProperty() {
      return focusedObjectProperty;
   }

   public void setFocusedObject(IdentifiedObject focusedObject) {
      this.focusedObjectProperty.set(focusedObject);
   }

   public ConceptSnapshotService getConceptSnapshotService() {
      return Get.conceptService().getSnapshot(taxonomyCoordinate.getStampCoordinate(), taxonomyCoordinate.getLanguageCoordinate());
   }

   @Override
   public ObservableStampCoordinate getStampCoordinate() {
      return this.taxonomyCoordinate.getStampCoordinate();
   }

   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate() {
      return this.taxonomyCoordinate.getLanguageCoordinate();
   }

   @Override
   public ObservableLogicCoordinate getLogicCoordinate() {
      return this.taxonomyCoordinate.getLogicCoordinate();
   }
   
   
}
