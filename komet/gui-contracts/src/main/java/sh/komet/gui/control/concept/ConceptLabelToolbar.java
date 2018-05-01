/*
 * Copyright 2017 Your Organisation.
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
package sh.komet.gui.control.concept;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.OnOffToggleSwitch;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConceptLabelToolbar implements ChangeListener<String> {

   protected static final Logger LOG = LogManager.getLogger();
   final MenuButton manifoldLinkMenu = new MenuButton();
   final ManifoldLinkedConceptLabel conceptLabel;
   final Label rightInfoLabel = new Label("");
   final Manifold manifold;
   final Supplier<List<MenuItem>> menuSupplier;
   final GridPane toolBarGrid = new GridPane();
   final OnOffToggleSwitch focusOnChange = new OnOffToggleSwitch();


   public void manifoldEventHandler(Event event) {
      try {
         MenuItem menuItem = (MenuItem) event.getSource();
           String manifoldGroup = (String) menuItem.getUserData();
           Optional<ConceptSpecification> spec = manifold.getConceptForGroup(manifoldGroup);
           manifold.setGroupName(manifoldGroup);
           if (spec.isPresent()) {
              ConceptChronology focusedConcept = Get.concept(spec.get());
              manifold.setFocusedConceptChronology(focusedConcept);
           } else {
              if (!manifold.getGroupName().equals(ManifoldGroup.UNLINKED.getGroupName())) {
                 manifold.setFocusedConceptChronology(null);
              }
           }
           manifoldLinkMenu.setGraphic(getNodeForManifold(manifoldGroup));
      }
      catch (Exception e) {
         LOG.warn("Failure handling manifold event!", e);
      }
   }

   public final Node getNodeForManifold(Manifold manifold) {
      return getNodeForManifold(manifold.getGroupName());
   }
   // TODO make the manifold menu it's own object to be used in many places
   public final Node getNodeForManifold(String manifoldGroup) {
      HBox combinedGraphic = new HBox(1);
      combinedGraphic.setMinWidth(45);
      combinedGraphic.setPrefWidth(45);
      combinedGraphic.setMaxWidth(45);
      if (manifoldGroup.equals(ManifoldGroup.UNLINKED.getGroupName())) {
         Node linkBroken = Iconography.LINK_BROKEN.getIconographic();
         Rectangle rect = new Rectangle(16, 16, Color.TRANSPARENT);
         combinedGraphic.getChildren().addAll(linkBroken,rect);
      } else {
         combinedGraphic.getChildren().addAll(
              Iconography.LINK.getIconographic(), 
         Manifold.getIconographic(manifoldGroup));
      }
      return combinedGraphic;
   }

   private ConceptLabelToolbar(Manifold manifold, Supplier<List<MenuItem>> menuSupplier, Optional<Boolean> focusTabOnConceptChange) {
      this.manifold = manifold;
      this.menuSupplier = menuSupplier;
      this.conceptLabel = new ManifoldLinkedConceptLabel(manifold, ManifoldLinkedConceptLabel::setFullySpecifiedText, menuSupplier);
      if (focusTabOnConceptChange.isPresent())
      {
         this.focusOnChange.selectedProperty().set(focusTabOnConceptChange.get());
      }
      else
      {
         this.focusOnChange.setManaged(false);
         this.focusOnChange.setVisible(false);
      }

      // Manifold
      Manifold.getGroupNames().stream().filter((groupString) -> {
          if (FxGet.fxConfiguration().isShowBetaFeaturesEnabled()) {
              return true;
          } 
          return !groupString.toLowerCase().startsWith("flwor");
      }).map((m) -> {
         MenuItem manifoldItem = new MenuItem(m, getNodeForManifold(m));
         manifoldItem.setUserData(m);
         manifoldItem.addEventHandler(ActionEvent.ACTION, this::manifoldEventHandler);
         return manifoldItem;
      }).forEachOrdered((manifoldItem) -> {
         manifoldLinkMenu.getItems().add(manifoldItem);
      });

      manifoldLinkMenu.setGraphic(getNodeForManifold(manifold));
      manifold.groupNameProperty().addListener(new WeakChangeListener<>(this));
// History
// 
   }

   /**
    * 
    * @param manifold
    * @param menuSupplier
    * @param focusTabOnConceptChange - Optional.empty, if you don't want this feature, other wise true or false to set the initial state
    * @return
    */
   public static ConceptLabelToolbar make(Manifold manifold, Supplier<List<MenuItem>> menuSupplier, Optional<Boolean> focusTabOnConceptChange) {

      ConceptLabelToolbar gctb = new ConceptLabelToolbar(manifold, menuSupplier, focusTabOnConceptChange);
      GridPane.setConstraints(gctb.manifoldLinkMenu, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      gctb.toolBarGrid.getChildren().add(gctb.manifoldLinkMenu);
      GridPane.setConstraints(gctb.conceptLabel, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);
      gctb.conceptLabel.setMaxWidth(2000);
      gctb.conceptLabel.setMinWidth(100);
      gctb.toolBarGrid.getChildren().add(gctb.conceptLabel);
      
      GridPane.setConstraints(gctb.rightInfoLabel, 2, 0, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      gctb.toolBarGrid.getChildren().add(gctb.rightInfoLabel);
      
      if (focusTabOnConceptChange.isPresent())
      {
         Label focusChangeWrapper = new Label("Focus", gctb.focusOnChange);
         gctb.focusOnChange.setSelected(focusTabOnConceptChange.get());
         focusChangeWrapper.setContentDisplay(ContentDisplay.RIGHT);
         focusChangeWrapper.setStyle("-fx-font-size:12;");
         Tooltip.install(focusChangeWrapper, new Tooltip("Focus tab on selection change"));
         GridPane.setConstraints(focusChangeWrapper, 3, 0, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
         gctb.toolBarGrid.getChildren().add(focusChangeWrapper);
      }
      
      gctb.toolBarGrid.getStyleClass().add("concept-label-toolbar");
      return gctb;
   }

   public Node getToolbarNode() {
       return this.toolBarGrid;
   }
   @Override
   public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
      manifoldLinkMenu.setGraphic(getNodeForManifold(manifold));      
   }

    public Label getRightInfoLabel() {
        return rightInfoLabel;
    }
    
    public ReadOnlyBooleanProperty getFocusTabOnConceptChange() {
       return BooleanProperty.readOnlyBooleanProperty(focusOnChange.selectedProperty());
    }
}
