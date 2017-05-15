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
package sh.isaac.komet.gui.drag.drop;

import sh.isaac.komet.gui.KOMET;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.komet.gui.interfaces.DraggableWithImage;

/**
 * {@link DragDetectedEventHandler}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DragDetectedEventHandler implements EventHandler<MouseEvent> {

   private final Node node;
   private final IdentifiedObject idObject;

   public DragDetectedEventHandler(Node node, IdentifiedObject idObject) {
      this.node = node;
      this.idObject = idObject;
   }

   /**
    * @param event
    * @see javafx.event.EventHandler#handle(javafx.event.Event)
    */
   @Override
   public void handle(MouseEvent event) {
      /* drag was detected, start a drag-and-drop gesture */
 /* allow any transfer mode */
      if (node != null) {
         Dragboard db = node.startDragAndDrop(TransferMode.COPY);
         if (node instanceof DraggableWithImage) {
            DraggableWithImage draggableWithImageNode = (DraggableWithImage) node;
            Image dragImage = draggableWithImageNode.getDragImage();
            double xOffset = ((dragImage.getWidth()/2) + draggableWithImageNode.getDragViewOffsetX()) - event.getX();
            double yOffset = event.getY() - (dragImage.getHeight()/2);
            System.out.println("\n\nclick x,y: " + event.getX() + ", " + event.getY());
            System.out.println("xOffset: " + xOffset);
            System.out.println("yOffset: " + yOffset + "\n");
            db.setDragView(dragImage, xOffset, yOffset);
         }

         /* Put a string on a dragboard */
         String drag = idObject.getPrimordialUuid().toString();
         if (drag != null && drag.length() > 0) {
            ClipboardContent content = new ClipboardContent();
            content.putString(drag);
            db.setContent(content);
            KOMET.dragStart();
            event.consume();
         }
      }
   }
}
