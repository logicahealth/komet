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



package sh.komet.gui.alert;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.Resolver;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.style.PseudoClasses;
import sh.komet.gui.style.StyleClasses;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class AlertPanel
        extends GridPane {
   private final ToolBar     resolverBar       = new ToolBar();
   private boolean           showDetails       = false;
   protected final Text      alertTitle        = new Text();
   protected final Text      alertDescription  = new Text();
   protected final Button    moreDetailsButton = new Button("    More details");
   private final AlertObject alert;
   protected final Node      alertIcon;

   //~--- constructors --------------------------------------------------------

   public AlertPanel(AlertObject alert) {
      this.alert = alert;

      switch (alert.getAlertType()) {
      case CONFIRMATION:
         alertIcon = Iconography.ALERT_CONFIRM2.getIconographic();
         pseudoClassStateChanged(PseudoClasses.ALERT_CONFIRM_PSEUDO_CLASS, true);
         break;

      case ERROR:
         alertIcon = Iconography.ALERT_ERROR2.getIconographic();
         pseudoClassStateChanged(PseudoClasses.ALERT_ERROR_PSEUDO_CLASS, true);
         break;

      case INFORMATION:
         alertIcon = Iconography.ALERT_INFORM2.getIconographic();
         pseudoClassStateChanged(PseudoClasses.ALERT_INFO_PSEUDO_CLASS, true);
         break;

      case WARNING:
         alertIcon = Iconography.ALERT_WARN2.getIconographic();
         pseudoClassStateChanged(PseudoClasses.ALERT_WARN_PSEUDO_CLASS, true);
         break;
         
      case SUCCESS:
         alertIcon = Iconography.CHECK.getIconographic();
         pseudoClassStateChanged(PseudoClasses.ALERT_SUCCESS_PSEUDO_CLASS, true);
         break;
         

      default:
         throw new UnsupportedOperationException("Can't handle: " + alert.getAlertType());
      }

      alertIcon.getStyleClass()
               .add(StyleClasses.ALERT_ICON.toString());

      if ((alert.getAlertDescription() != null) &&!alert.getAlertDescription().isEmpty()) {
         this.alertTitle.setText(this.alert.getAlertTitle() + "...");
      } else {
         this.alertTitle.setText(this.alert.getAlertTitle());
      }

      this.alertDescription.setText(this.alert.getAlertDescription());
      this.alertTitle.getStyleClass()
                     .setAll(StyleClasses.ALERT_TITLE.toString());
      this.alertDescription.getStyleClass()
                           .setAll(StyleClasses.ALERT_DESCRIPTION.toString());
      this.moreDetailsButton.getStyleClass()
                            .setAll(StyleClasses.MORE_ALERT_DETAILS.toString());
      this.moreDetailsButton.setOnAction(
          (event) -> {
             showDetails = !showDetails;
             layoutAlert();
          });
      this.getStyleClass()
          .setAll(StyleClasses.ALERT_PANE.toString());
      layoutAlert();
   }

   //~--- methods -------------------------------------------------------------

   public final void layoutAlert() {
      getChildren()
               .clear();

      int row    = 0;
      int column = 0;

      GridPane.setConstraints(
          alertIcon,
          column,
          row,
          1,
          1,
          HPos.LEFT,
          VPos.CENTER,
          Priority.NEVER,
          Priority.NEVER,
          new Insets(2, 2, 2, 10));
      getChildren()
               .add(alertIcon);
      column++;
      GridPane.setConstraints(
          alertTitle,
          column,
          row,
          1,
          1,
          HPos.LEFT,
          VPos.CENTER,
          Priority.NEVER,
          Priority.NEVER,
          new Insets(2, 2, 2, 10));
      getChildren()
               .add(alertTitle);
      column++;

      Label fillerPane = new Label();

      GridPane.setConstraints(
          fillerPane,
          column,
          row,
          1,
          1,
          HPos.LEFT,
          VPos.CENTER,
          Priority.ALWAYS,
          Priority.NEVER,
          new Insets(2, 2, 2, 10));
      getChildren()
               .add(fillerPane);
      column++;
      GridPane.setConstraints(
          moreDetailsButton,
          column,
          row,
          1,
          1,
          HPos.RIGHT,
          VPos.BOTTOM,
          Priority.NEVER,
          Priority.NEVER,
          new Insets(2, 2, 2, 10));
      getChildren()
               .add(moreDetailsButton);
      column = 0;
      row++;

      if (showDetails) {
         GridPane.setConstraints(
             alertDescription,
             column,
             row,
             3,
             1,
             HPos.LEFT,
             VPos.TOP,
             Priority.ALWAYS,
             Priority.NEVER,
             new Insets(2, 2, 2, 15));
         getChildren()
                  .add(alertDescription);
         row++;

         for (Resolver resolver: alert.getResolvers()) {
            
            Node graphic = null;
            switch (resolver.getPersistence()) {
               case TEMPORARY:
               graphic = Iconography.TEMPORARY_FIX.getIconographic();
               break;
               case PERMANENT:
                  break;
                  default:
                     throw new UnsupportedOperationException("Can't handle: " + resolver.getPersistence());
            }
                    
                    
            Button resolverButton = new Button(resolver.getTitle(), graphic);

            resolverButton.setTooltip(new Tooltip(resolver.getDescription()));
            resolverButton.setOnAction(
                (event) -> {
               // not blocking since not doing a get() on the task.
                   resolver.resolve();
                });
            GridPane.setConstraints(
                resolverButton,
                column,
                row,
                1,
                2,
                HPos.LEFT,
                VPos.TOP,
                Priority.NEVER,
                Priority.NEVER,
                new Insets(2, 2, 10, 15));
            getChildren()
                     .add(resolverButton);
            row++;
         }
      }
   }
}

