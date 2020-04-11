/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.control.axiom;

import java.util.NoSuchElementException;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PopOver;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class ConceptNode extends Label {

    private final int conceptNid;
    private final Button openConceptButton = new Button("", Iconography.LINK_EXTERNAL.getIconographic());
    private final Manifold manifold;
    private PremiseType premiseType = PremiseType.INFERRED;

    public ConceptNode(int conceptNid, Manifold manifold) {
        this.conceptNid = conceptNid;
        this.manifold = manifold;
        this.setText(manifold.getPreferredDescriptionText(conceptNid));

        HBox controlBox;
        LatestVersion<Version> latest = manifold.getStampFilter().latestConceptVersion(conceptNid);
        if (latest.isPresent()) {
            controlBox = new HBox(openConceptButton, AxiomView.computeGraphic(conceptNid, false,
                    latest.get().getStatus(), manifold, premiseType));
        } else {
            controlBox = new HBox(openConceptButton, AxiomView.computeGraphic(conceptNid, false,
                    Status.PRIMORDIAL, manifold, premiseType));
        }
        ;

        this.setGraphic(controlBox);
        setOnDragDetected(this::handleDragDetected);
        setOnDragDone(this::handleDragDone);
        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
        openConceptButton.setOnMouseClicked(this::handleShowConceptNodeClick);
    }

    private void handleShowConceptNodeClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            showPopup(conceptNid, mouseEvent);
        }
    }

    private void showPopup(int conceptNid, MouseEvent mouseEvent) {

        Optional<LogicalExpression> expression = manifold.getLogicalExpression(conceptNid, premiseType);
        if (!expression.isPresent()) {
            premiseType = PremiseType.STATED;
            expression = manifold.getLogicalExpression(conceptNid, premiseType);
        }
        if (expression.isPresent()) {
            PopOver popover = new PopOver();
            popover.setContentNode(AxiomView.createWithCommitPanel(expression.get(),
                    premiseType,
                    manifold));
            popover.setCloseButtonEnabled(true);
            popover.setHeaderAlwaysVisible(false);
            popover.setTitle("");
            popover.show(openConceptButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            mouseEvent.consume();
        }
    }

    private void handleDragDetected(MouseEvent event) {
        System.out.println("Drag detected: " + event);

        DragImageMaker dragImageMaker = new DragImageMaker(this);
        Dragboard db = this.startDragAndDrop(TransferMode.COPY);

        db.setDragView(dragImageMaker.getDragImage());

        IsaacClipboard content = new IsaacClipboard(Get.concept(conceptNid));
        db.setContent(content);
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        System.out.println("Dragging done: " + event);
    }
}
