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
package sh.komet.gui.importation;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class ImportView {

    final Stage stage;
    ImportViewController controller;
    
    private ImportView(ViewProperties manifold) {
        try {
            this.stage = new Stage();
            //stage.initModality(Modality.NONE);
            //stage.setAlwaysOnTop(false);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImportView.fxml"));
            Parent root = loader.load();
            this.controller = loader.getController();
            this.controller.setImportStage(stage);
            this.controller.setManifold(manifold);
            
            //create scene with set width, height and color
            Scene scene = new Scene(root, 900, 600, Color.WHITESMOKE);
            //Disabled for now, due to bugs in the CSS interacting with the treeTable view.
            //scene.getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
            
            //set scene to stage
            stage.setScene(scene);
            
            //set title to stage
            stage.setTitle("Data import");
            
            stage.sizeToScene();
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void show(ViewProperties manifold) {
        ImportView importView = new ImportView(manifold);
        //show the stage
        //center stage on screen
        importView.stage.centerOnScreen();
        importView.stage.show();
    }
}


