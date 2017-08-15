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
package sh.komet.gui.example;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sh.komet.gui.control.ComponentPanel;

/**
 *
 * @author kec
 */
public class ComponentPanelTester extends Application {

   @Override
   public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Component Panel Tester");
        FlowPane flowPane = new FlowPane(Orientation.VERTICAL);
        Scene scene = new Scene(flowPane, 400, 200);
        scene.setFill(Color.LIGHTGREEN);
        flowPane.getChildren().add(new ComponentPanel(null, null));
        flowPane.getChildren().add(new ComponentPanel(null, null));
        
        primaryStage.setScene(scene);
        primaryStage.show();
   }
   
   
    public static void main(String[] args) {
        Application.launch(args);
    }
   
}
