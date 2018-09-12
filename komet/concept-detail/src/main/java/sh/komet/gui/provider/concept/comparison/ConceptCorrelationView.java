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
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class ConceptCorrelationView {


    final Stage stage;
    final Manifold manifold;
    final ConceptCorrelationController controller;
    
    protected ConceptCorrelationView(Manifold manifold, String title) {
        try {
            this.manifold = manifold;
            this.stage = new Stage();
            //stage.initModality(Modality.NONE);
            //stage.setAlwaysOnTop(false);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sh/komet/gui/provider/concept/comparison/ConceptComparison.fxml"));
            AnchorPane root = loader.load();
            this.controller = loader.getController();
            this.controller.setManifold(manifold);
            
            
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


    public static ConceptCorrelationController show(Manifold manifold, String title, 
            EventHandler<WindowEvent> closeRequestHandler) {
        ConceptCorrelationView correlationView = new ConceptCorrelationView(manifold, title);
        
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
