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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.control.toggle.OnOffToggleSwitch;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

import static sh.komet.gui.control.property.ViewProperties.UNLINKED;

/**
 *
 * @author kec
 */
public class ConceptLabelToolbar {

   protected static final Logger LOG = LogManager.getLogger();
   final ViewProperties viewProperties;
   final MenuButton activityLinkMenu = new MenuButton();
   final ConceptLabelWithDragAndDrop conceptLabel;
   final Label rightInfoLabel = new Label("");
   final GridPane toolBarGrid = new GridPane();
   final OnOffToggleSwitch focusOnChange = new OnOffToggleSwitch();
   final SimpleObjectProperty<ActivityFeed> activityFeedProperty;


   public void activityFeedEventHandler(Event event) {
      try {
         MenuItem menuItem = (MenuItem) event.getSource();
           String activityFeedSpec = (String) menuItem.getUserData();
           ActivityFeed activityFeed = this.viewProperties.getActivityFeed(activityFeedSpec);
           this.activityFeedProperty.set(activityFeed);
       }
      catch (Exception e) {
         LOG.warn("Failure handling manifold event!", e);
      }
   }

   public final Node getGraphicForActivity(String activityFeedName) {
      // TODO make the activity menu it's own object to be used in many places
      HBox combinedGraphic = new HBox(1);
      combinedGraphic.setMinWidth(45);
      combinedGraphic.setPrefWidth(45);
      combinedGraphic.setMaxWidth(45);
      if (activityFeedName.equals(UNLINKED)) {
         Node linkBroken = Iconography.LINK_BROKEN.getIconographic();
         Rectangle rect = new Rectangle(16, 16, Color.TRANSPARENT);
         combinedGraphic.getChildren().addAll(linkBroken,rect);
      } else {
         Optional<Node> optionalIcon = ViewProperties.getOptionalGraphicForActivityFeed(activityFeedName);
         if (optionalIcon.isPresent()) {
            combinedGraphic.getChildren().addAll(
                    Iconography.LINK.getIconographic(),
                    optionalIcon.get());
         } else {
            combinedGraphic.getChildren().addAll(
                    Iconography.LINK.getIconographic());
         }
      }
      return combinedGraphic;
   }

   private ConceptLabelToolbar(ViewProperties viewProperties,
                               SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                               Consumer<ConceptLabelWithDragAndDrop> descriptionTextUpdater,
                               SimpleIntegerProperty selectionIndexProperty,
                               Runnable unlink,
                               SimpleObjectProperty<ActivityFeed> activityFeedProperty,
                               Optional<Boolean> focusTabOnConceptChange) {
      this.viewProperties = viewProperties;
      this.conceptLabel = new ConceptLabelWithDragAndDrop(viewProperties,
              conceptFocusProperty,
              descriptionTextUpdater,
              selectionIndexProperty,
              unlink);

      if (focusTabOnConceptChange.isPresent())
      {
         this.focusOnChange.selectedProperty().set(focusTabOnConceptChange.get());
      } else {
         this.focusOnChange.setManaged(false);
         this.focusOnChange.setVisible(false);
      }

      this.viewProperties.getActivityFeeds().forEach(feed -> {
         MenuItem activityItem = new MenuItemWithText(feed.getFeedName(), getGraphicForActivity(feed.getFeedName()));
         activityItem.setUserData(feed.getFullyQualifiedActivityFeedName());
         activityItem.addEventHandler(ActionEvent.ACTION, this::activityFeedEventHandler);
         activityLinkMenu.getItems().add(activityItem);
      });
      activityLinkMenu.getItems().sort(Comparator.comparing(MenuItem::getText));

      ArrayList<Menu> otherViewFeeds = new ArrayList<>();
      ViewProperties.getAll().forEach(anotherView -> {
         if (anotherView != this.viewProperties) {
            otherViewFeeds.add(new Menu(anotherView.getViewName()));
            anotherView.getActivityFeeds().forEach(feed -> {
               MenuItem activityItem = new MenuItemWithText(feed.getFeedName(), getGraphicForActivity(feed.getFeedName()));
               activityItem.setUserData(feed.getFullyQualifiedActivityFeedName());
               activityItem.addEventHandler(ActionEvent.ACTION, this::activityFeedEventHandler);
               otherViewFeeds.get(otherViewFeeds.size() - 1).getItems().add(activityItem);
            });
            otherViewFeeds.get(otherViewFeeds.size() - 1).getItems().sort(Comparator.comparing(MenuItem::getText));
         }
      });
      otherViewFeeds.sort(Comparator.comparing(Menu::getText));

      activityLinkMenu.getItems().addAll(otherViewFeeds);

      activityLinkMenu.setGraphic(getGraphicForActivity(activityFeedProperty.get().getFeedName()));
      this.activityFeedProperty = activityFeedProperty;
      activityFeedProperty.addListener((observable, oldValue, newValue) -> {
         activityLinkMenu.setGraphic(getGraphicForActivity(newValue.getFeedName()));
      });
   }

      public static ConceptLabelToolbar make(ViewProperties viewProperties,
                                             SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                                             Consumer<ConceptLabelWithDragAndDrop> descriptionTextUpdater,
                                             SimpleIntegerProperty selectionIndexProperty,
                                             Runnable unlink,
                                             SimpleObjectProperty<ActivityFeed> activityFeedProperty,
                                             Optional<Boolean> focusTabOnConceptChange) {
      ConceptLabelToolbar gctb = new ConceptLabelToolbar(viewProperties, conceptFocusProperty,
              descriptionTextUpdater, selectionIndexProperty, unlink, activityFeedProperty, focusTabOnConceptChange);
      GridPane.setConstraints(gctb.activityLinkMenu, 0, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
      gctb.toolBarGrid.getChildren().add(gctb.activityLinkMenu);
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

    public Label getRightInfoLabel() {
        return rightInfoLabel;
    }
    
    public ReadOnlyBooleanProperty getFocusTabOnConceptChange() {
       return BooleanProperty.readOnlyBooleanProperty(focusOnChange.selectedProperty());
    }
}
