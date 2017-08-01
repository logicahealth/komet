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
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Demonstrates a drag-and-drop feature.
 */
public class HelloDragAndDrop extends Application {

    @Override public void start(Stage stage) {
        stage.setTitle("Hello Drag And Drop");

        Group root = new Group();
        Scene scene = new Scene(root, 400, 200);
        scene.setFill(Color.LIGHTGREEN);

        final Text source = new Text(50, 100, "DRAG ME");
        source.setScaleX(2.0);
        source.setScaleY(2.0);

        final Text target = new Text(250, 100, "DROP HERE");
        target.setScaleX(2.0);
        target.setScaleY(2.0);

        source.setOnDragDetected(new EventHandler <MouseEvent>() {
            public void handle(MouseEvent event) {
                /* drag was detected, start drag-and-drop gesture*/
                System.out.println("onDragDetected");
                
                /* allow any transfer mode */
                Dragboard db = source.startDragAndDrop(TransferMode.ANY);
                
                /* put a string on dragboard */
                ClipboardContent content = new ClipboardContent();
                content.putString(source.getText());
                db.setContent(content);
                
                event.consume();
            }
        });

        target.setOnDragOver(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                System.out.println("onDragOver");
                
                /* accept it only if it is  not dragged from the same node 
                 * and if it has a string data */
                if (event.getGestureSource() != target &&
                        event.getDragboard().hasString()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                
                event.consume();
            }
        });

        target.setOnDragEntered(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture entered the target */
                System.out.println("onDragEntered");
                /* show to the user that it is an actual gesture target */
                if (event.getGestureSource() != target &&
                        event.getDragboard().hasString()) {
                    target.setFill(Color.GREEN);
                }
                
                event.consume();
            }
        });

        target.setOnDragExited(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* mouse moved away, remove the graphical cues */
                target.setFill(Color.BLACK);
                
                event.consume();
            }
        });
        
        target.setOnDragDropped(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                System.out.println("onDragDropped");
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    target.setText(db.getString());
                    success = true;
                }
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);
                
                event.consume();
            }
        });

        source.setOnDragDone(new EventHandler <DragEvent>() {
            public void handle(DragEvent event) {
                /* the drag-and-drop gesture ended */
                System.out.println("onDragDone");
                /* if the data was successfully moved, clear it */
                if (event.getTransferMode() == TransferMode.MOVE) {
                    source.setText("");
                }
                
                event.consume();
            }
        });

        root.getChildren().add(source);
        root.getChildren().add(target);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}