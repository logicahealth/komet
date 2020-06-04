/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.provider.concept.comparison;

import java.io.IOException;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class ConceptCorrelationView {


    final Stage stage;
    final ViewProperties viewProperties;
    final ConceptCorrelationController controller;
    
    protected ConceptCorrelationView(ViewProperties viewProperties,
                                     ActivityFeed activityFeed,
                                     IsaacPreferences preferences, String title) {
        try {
            this.viewProperties = viewProperties;
            this.stage = new Stage();
            //stage.initModality(Modality.NONE);
            //stage.setAlwaysOnTop(false);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/komet/gui/provider/concept/comparison/ConceptComparison.fxml"));
            AnchorPane root = loader.load();
            this.controller = loader.getController();
            this.controller.setViewProperties(viewProperties, preferences);
            
            
            //create scene with set width, height and color
            Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight(), Color.WHITESMOKE);
            
            //set scene to stage
            stage.setScene(scene);
            
            //set title to stage
            stage.setTitle(title);
            
            stage.sizeToScene();
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static ConceptCorrelationController show(ViewProperties viewProperties,
                                                    ActivityFeed activityFeed,
                                                    IsaacPreferences preferences,
                                                    EventHandler<WindowEvent> closeRequestHandler) {
        ConceptCorrelationView correlationView = new ConceptCorrelationView(viewProperties, activityFeed, preferences, "temp");
        Stage stage = correlationView.stage;
        stage.getProperties().put(ViewProperties.Keys.NAME_PREFIX, "");
        stage.getProperties().put(ViewProperties.Keys.NAME_SUFFIX, " correlation");
        stage.setTitle(stage.getProperties().get(ViewProperties.Keys.NAME_PREFIX) +
                        FxGet.configurationName() +
                        stage.getProperties().get(ViewProperties.Keys.NAME_SUFFIX));
        FxGet.configurationNameProperty().addListener((observable, oldValue, newValue) -> {
        stage.setTitle(stage.getProperties().get(ViewProperties.Keys.NAME_PREFIX) +
                        newValue +
                        stage.getProperties().get(ViewProperties.Keys.NAME_SUFFIX));
             
        });
        
        correlationView.controller.setComparisonExpression(CorrelationProblem5.getComparisonExpression());
        correlationView.controller.setReferenceExpression(CorrelationProblem5.getReferenceExpression());
        
        //show the stage
        //center stage on screen
        correlationView.stage.centerOnScreen();
        correlationView.stage.show();
        correlationView.stage.setOnCloseRequest(closeRequestHandler);
        return correlationView.controller;
    }
}
