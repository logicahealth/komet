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

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.control.Label;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshotService;
import sh.isaac.api.coordinate.LanguageCoordinateProxy;
import sh.isaac.api.coordinate.LogicCoordinateProxy;
import sh.isaac.api.coordinate.StampCoordinateProxy;
import sh.isaac.api.observable.coordinate.ObservableEditCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLanguageCoordinate;
import sh.isaac.api.observable.coordinate.ObservableLogicCoordinate;
import sh.isaac.api.observable.coordinate.ObservableStampCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateProxy;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.komet.iconography.Iconography;

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
public class Manifold implements StampCoordinateProxy, LanguageCoordinateProxy, LogicCoordinateProxy, ManifoldCoordinateProxy {

   private static final ObservableMap<String, Manifold> manifolds = FXCollections.observableHashMap();
   public static final Manifold UNLINKED = newManifold(
           "unlinked",
           UUID.randomUUID(),
           Get.configurationService().getDefaultManifoldCoordinate(),
           Get.configurationService().getDefaultEditCoordinate(),
           () -> new Label());

   public static final Manifold TAXONOMY = newManifold(
           "taxonomy",
           UUID.randomUUID(),
           Get.configurationService().getDefaultManifoldCoordinate(),
           Get.configurationService().getDefaultEditCoordinate(),
           () -> Iconography.TAXONOMY_ICON.getIconographic());

   public static final Manifold SIMPLE_SEARCH = newManifold(
           "search",
           UUID.randomUUID(),
           Get.configurationService().getDefaultManifoldCoordinate(),
           Get.configurationService().getDefaultEditCoordinate(),
           () -> Iconography.SIMPLE_SEARCH.getIconographic());

   public static final Manifold FLOWR_QUERY = newManifold(
           "flowr",
           UUID.randomUUID(),
           Get.configurationService().getDefaultManifoldCoordinate(),
           Get.configurationService().getDefaultEditCoordinate(),
           () -> Iconography.FL0WR_SEARCH.getIconographic());

   public static Manifold newManifold(String name, UUID manifoldUuid, ObservableManifoldCoordinate observableManifoldCoordinate, ObservableEditCoordinate editCoordinate, Supplier<Node> iconSupplier) {
      Manifold manifold = new Manifold(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, iconSupplier);
      manifolds.put(name, manifold);
      return manifold;
   }

   public static Manifold newManifold(String name, UUID manifoldUuid, ObservableManifoldCoordinate observableManifoldCoordinate, ObservableEditCoordinate editCoordinate, Supplier<Node> iconSupplier, ConceptChronology focusedObject) {
      Manifold manifold = new Manifold(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, iconSupplier, focusedObject);
      manifolds.put(name, manifold);
      return manifold;
   }

   public static Manifold get(String name) {
      return manifolds.get(name);
   }

   public static Collection<Manifold> getValues() {
      return manifolds.values();
   }

   final SimpleStringProperty nameProperty;
   final SimpleObjectProperty<UUID> manifoldUuidProperty;
   final ObservableManifoldCoordinate observableManifoldCoordinate;
   final ObservableEditCoordinate editCoordinate;
   final SimpleObjectProperty<ConceptChronology> focusedConceptChronologyProperty;
   final Supplier<Node> iconSupplier;

   private Manifold(String name, UUID manifoldUuid, ObservableManifoldCoordinate observableManifoldCoordinate, ObservableEditCoordinate editCoordinate, Supplier<Node> iconSupplier) {
      this(name, manifoldUuid, observableManifoldCoordinate, editCoordinate, iconSupplier, null);
   }

   private Manifold(String name, UUID manifoldUuid, ObservableManifoldCoordinate observableManifoldCoordinate, ObservableEditCoordinate editCoordinate, Supplier<Node> iconSupplier, ConceptChronology focusedObject) {
      this.nameProperty = new SimpleStringProperty(name);
      this.manifoldUuidProperty = new SimpleObjectProperty<>(manifoldUuid);
      this.observableManifoldCoordinate = observableManifoldCoordinate;
      this.editCoordinate = editCoordinate;
      this.focusedConceptChronologyProperty = new SimpleObjectProperty<>(focusedObject);
      this.iconSupplier = iconSupplier;
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
   public ObservableManifoldCoordinate getManifoldCoordinate() {
      return observableManifoldCoordinate;
   }

   public ObservableEditCoordinate getEditCoordinate() {
      return editCoordinate;
   }

   public SimpleObjectProperty<ConceptChronology> focusedConceptChronologyProperty() {
      return focusedConceptChronologyProperty;
   }

   public void setFocusedConceptChronology(ConceptChronology focusedObject) {
      this.focusedConceptChronologyProperty.set(focusedObject);
   }

   public ConceptSnapshotService getConceptSnapshotService() {
      return Get.conceptService().getSnapshot(observableManifoldCoordinate);
   }

   @Override
   public ObservableStampCoordinate getStampCoordinate() {
      return this.observableManifoldCoordinate.getStampCoordinate();
   }

   @Override
   public ObservableLanguageCoordinate getLanguageCoordinate() {
      return this.observableManifoldCoordinate.getLanguageCoordinate();
   }

   @Override
   public ObservableLogicCoordinate getLogicCoordinate() {
      return this.observableManifoldCoordinate.getLogicCoordinate();
   }

   public Node getIconographic() {
      return iconSupplier.get();
   }

}
