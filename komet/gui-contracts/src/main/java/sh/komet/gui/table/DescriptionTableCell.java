package sh.komet.gui.table;

import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.transform.NonInvertibleTransformException;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.drag.drop.DragDetectedCellEventHandler;
import sh.komet.gui.drag.drop.DragDoneEventHandler;
import sh.komet.gui.interfaces.DraggableWithImage;

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

/**
 *
 * @author kec
 * @param <T> The type for a row in the table
 */
public class DescriptionTableCell<T extends IdentifiedObject> extends TableCell<T, String> 
        implements DraggableWithImage {

   private double   dragOffset = 0;

   public DescriptionTableCell() {
      this.setOnDragDetected(new DragDetectedCellEventHandler());
      this.setOnDragDone(new DragDoneEventHandler());
   }

   @Override
   protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty); 
      setText(item);
   }

   @Override
   public Image getDragImage() {
      SnapshotParameters snapshotParameters = new SnapshotParameters();

      dragOffset = 0;

      double width  = this.getWidth();
      double height = this.getHeight();

      try {
         snapshotParameters.setTransform(this.getLocalToParentTransform().createInverse());
      } catch (NonInvertibleTransformException ex) {
         throw new RuntimeException(ex);
      }
      snapshotParameters.setViewport(new Rectangle2D(dragOffset -2, 0, width, height));
      return snapshot(snapshotParameters, null);
   }

   @Override
   public double getDragViewOffsetX() {
      return dragOffset;
   }
   
}
