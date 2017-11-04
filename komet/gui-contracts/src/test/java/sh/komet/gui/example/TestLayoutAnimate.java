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
import java.util.Random;
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import sh.komet.gui.layout.LayoutAnimator;

/**
 * Creates a FlowPane and adds some rectangles inside.
 * A LayoutAnimator is set to observe the contents of the FlowPane for layout
 * changes.
 * Sourced from: https://gist.github.com/jewelsea/5683558
 */
public class TestLayoutAnimate extends Application {
  public static void main(String[] args) {
    Application.launch(TestLayoutAnimate.class);
  }

  @Override
  public void start(Stage primaryStage) {
    final Pane root = new FlowPane();

    // Clicking on button adds more rectangles
    Button btn = new Button();
    btn.setText("Add Rectangles");
    final TestLayoutAnimate self = this;
    btn.setOnAction((ActionEvent event) -> {
       self.addRectangle(root);
    });
    root.getChildren().add(btn);

    // add 5 rectangles to start with
    for (int i = 0; i < 5; i++) {
      addRectangle(root);
    }
    root.layout();
    LayoutAnimator ly = new LayoutAnimator();
    ly.observe(root.getChildren());

    Scene scene = new Scene(root, 300, 250);

    primaryStage.setTitle("Flow Layout Test");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  protected void addRectangle(Pane root) {
    Random rnd = new Random();
    Rectangle nodeNew = new Rectangle(50 + rnd.nextInt(20), 40 + rnd.nextInt(20));

// for testing pre-translated nodes
//    nodeNew.setTranslateX(rnd.nextInt(20));
//    nodeNew.setTranslateY(rnd.nextInt(15));

    nodeNew.setStyle("-fx-margin: 10;");
    String rndColor = String.format("%02X", rnd.nextInt(), rnd.nextInt(), rnd.nextInt());
    try {
      Paint rndPaint = Paint.valueOf(rndColor);
      nodeNew.setFill(rndPaint);
    } catch (Exception e) {
      nodeNew.setFill(Paint.valueOf("#336699"));
    }

    nodeNew.setStroke(Paint.valueOf("black"));
    root.getChildren().add(0, nodeNew);
  }
}