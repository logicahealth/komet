/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sh.komet.fx.stage;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 *
 * @author kec
 */
public class Cube extends Box {

   final Rotate rx = new Rotate(0, Rotate.X_AXIS);
   final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
   final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

   public Cube(double size, Color color) {
      super(size, size, size);
      setMaterial(new PhongMaterial(color));
      getTransforms().addAll(rz, ry, rx);
   }

   public static Cube green() {
      Cube c = new Cube(1, Color.GREEN);
      c.rx.setAngle(45);
      c.ry.setAngle(45);
      return c;
   }

   public static Cube red() {
      Cube c = new Cube(1, Color.RED);
      c.rx.setAngle(45);
      c.ry.setAngle(45);
      return c;
   }

   public static Cube orange() {
      Cube c = new Cube(1, Color.ORANGE);
      c.rx.setAngle(45);
      c.ry.setAngle(45);
      return c;
   }
   
   public static Parent orangeContent() {
      return createContent(orange(), Rotate.X_AXIS);
   }

   public static Parent redContent() {
      return createContent(red(), Rotate.Y_AXIS);
   }

   public static Parent greenContent() {
      return createContent(green(), Rotate.Z_AXIS);
   }

   public static Parent createContent(Cube c, Point3D axis) {

      Timeline animation = new Timeline();
      if (axis.equals(Rotate.X_AXIS)) {
         animation.getKeyFrames().addAll(
                 new KeyFrame(Duration.ZERO,
                         new KeyValue(c.rx.angleProperty(), 0d)),
                 new KeyFrame(Duration.seconds(1),
                         new KeyValue(c.rx.angleProperty(), 360d)));
      } else if (axis.equals(Rotate.Y_AXIS)) {
         animation.getKeyFrames().addAll(
                 new KeyFrame(Duration.ZERO,
                         new KeyValue(c.ry.angleProperty(), 0d)),
                 new KeyFrame(Duration.seconds(1),
                         new KeyValue(c.ry.angleProperty(), 360d)));
      } else if (axis.equals(Rotate.Z_AXIS)) {
         animation.getKeyFrames().addAll(
                 new KeyFrame(Duration.ZERO,
                         new KeyValue(c.rz.angleProperty(), 0d)),
                 new KeyFrame(Duration.seconds(1),
                         new KeyValue(c.rz.angleProperty(), 360d)));
      } else {
         throw new UnsupportedOperationException("ai Can't handle: " + axis);
      }

      animation.setCycleCount(Timeline.INDEFINITE);

      PerspectiveCamera camera = new PerspectiveCamera(true);
      camera.getTransforms().add(new Translate(0, 0, -10));

      Group root = new Group();
      root.getChildren().addAll(c);

      SubScene subScene = new SubScene(root, 200, 480, true, SceneAntialiasing.BALANCED);
      subScene.setCamera(camera);

      animation.play();
      return new Group(subScene);
   }
}
