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
package sh.komet.gui.drag.drop;

import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.style.PseudoClasses;
import static sh.isaac.komet.openjdk.KometTabPaneSkin.DRAG_DROP_KEY;
import static sh.isaac.komet.openjdk.KometTabPaneSkin.DRAG_ENTERED_KEY;
import static sh.isaac.komet.openjdk.KometTabPaneSkin.DRAG_EXITED_KEY;
import static sh.isaac.komet.openjdk.KometTabPaneSkin.DRAG_DONE_KEY;
import static sh.isaac.komet.openjdk.KometTabPaneSkin.DRAG_OVER_KEY;

/**
 *
 * @author kec
 */
public class TabDragAndDropHandler {

    private final DetailNode detailNode;
    
    private static class FunctionalWrapper implements EventHandler<DragEvent> {
        private final EventHandler<? super DragEvent> handler;

        public FunctionalWrapper(EventHandler<? super DragEvent> handler) {
            this.handler = handler;
        }

        @Override
        public void handle(DragEvent event) {
            handler.handle(event);
        }
        
    }

    public TabDragAndDropHandler(DetailNode detailNode) {
        this.detailNode = detailNode;
    }
    
    
    public static void setupTab(Tab droppableTab, DetailNode detailNode) {
        TabDragAndDropHandler handler = new TabDragAndDropHandler(detailNode);
        droppableTab.getProperties().put(DRAG_DROP_KEY, new FunctionalWrapper(handler::handleDragDropped));
        droppableTab.getProperties().put(DRAG_ENTERED_KEY, new FunctionalWrapper(handler::handleDragEntered));
        droppableTab.getProperties().put(DRAG_EXITED_KEY, new FunctionalWrapper(handler::handleDragExited));
        droppableTab.getProperties().put(DRAG_DONE_KEY, new FunctionalWrapper(handler::handleDragDone));
        droppableTab.getProperties().put(DRAG_OVER_KEY, new FunctionalWrapper(handler::handleDragOver));
    }
    
    public void handleDragAccept(DragEvent event) {
        if (acceptDrag(event)) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    public void handleDragEntered(DragEvent event) {
        if (acceptDrag(event)) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
            Pane dropTarget = (Pane) event.getSource();
            dropTarget.pseudoClassStateChanged(PseudoClasses.DROP_READY, true);
            dropTarget.applyCss();
        }
    }

    public void handleDragExited(DragEvent event) {
        Object source = event.getSource();
        if (source != null && source instanceof Pane) {
            Pane dropTarget = (Pane) source;
            event.acceptTransferModes(TransferMode.COPY);
            event.consume();
            dropTarget.pseudoClassStateChanged(PseudoClasses.DROP_READY, false);
            dropTarget.applyCss();
        }
    }

    public void handleDragOver(DragEvent event) {
        if (acceptDrag(event)) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    public void handleDragDropped(DragEvent event) {
        System.out.println("Drag dropped");
        if (acceptDrop(event)) {
            event.consume();
        }
    }
    public void handleDragDone(DragEvent event) {
        System.out.println("Drag done");
    }

    private boolean acceptDrop(DragEvent event) {
        if (acceptDrag(event)) {
            event.acceptTransferModes(TransferMode.COPY);
            if (event.getDragboard().hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
                ConceptChronology conceptChronology = Get.serializer()
                        .toObject(event.getDragboard(), IsaacClipboard.ISAAC_CONCEPT);

                detailNode.getManifold().setFocusedConceptChronology(conceptChronology);
            } else if (event.getDragboard().hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
                ConceptVersion conceptVersion = Get.serializer()
                        .toObject(event.getDragboard(), IsaacClipboard.ISAAC_CONCEPT_VERSION);

                detailNode.getManifold().setFocusedConceptChronology(conceptVersion.getChronology());
            }
        }
        return false;
    }

    private boolean acceptDrag(DragEvent event) {
        return true;
    }
}
