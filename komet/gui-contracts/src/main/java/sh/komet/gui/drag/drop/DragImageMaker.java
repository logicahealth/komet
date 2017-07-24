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
package sh.komet.gui.drag.drop;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.transform.NonInvertibleTransformException;
import sh.komet.gui.interfaces.DraggableWithImage;

/**
 *
 * @author kec
 */
public class DragImageMaker implements DraggableWithImage {

   private double dragOffset = 0;

   Node node;

   public DragImageMaker(Node node) {
      this.node = node;
   }

   @Override
   public Image getDragImage() {
      SnapshotParameters snapshotParameters = new SnapshotParameters();

      dragOffset = 0;

      // The height difference and widh difference are to account for possible 
      // changes in size of an object secondary to a hover (which might cause a 
      // -fx-effect:  dropshadow... or similar, whicn will create a diference in the 
      // tile pane height, but not cause a change in getLayoutBounds()...
      // I don't know if this is a workaround for a bug, or if this is expected
      // behaviour for some reason...
      double layoutWidth = node.getLayoutBounds()
              .getWidth();
      double widthDifference = node.getBoundsInParent()
              .getWidth() - layoutWidth;
      double widthAdjustment = 0;
      if (widthDifference > 0) {
         widthDifference = Math.rint(widthDifference);
         widthAdjustment = widthDifference / 2;
      }

      dragOffset = node.getBoundsInParent()
              .getMinX() + widthAdjustment;
      double width = node.getLayoutBounds().getWidth() - dragOffset;
      double height = node.getLayoutBounds().getHeight();

      try {
         snapshotParameters.setTransform(node.getLocalToParentTransform().createInverse());
      } catch (NonInvertibleTransformException ex) {
         throw new RuntimeException(ex);
      }
      snapshotParameters.setViewport(new Rectangle2D(dragOffset - 2, 0, width, height));
      return node.snapshot(snapshotParameters, null);
   }

   @Override
   public double getDragViewOffsetX() {
      return dragOffset;
   }

}
