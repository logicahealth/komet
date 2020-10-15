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

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PopOver;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.MenuSupplierForFocusConcept;
import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.style.StyleClasses;

/**
 *
 * @author kec
 */
public class ConceptNode extends Label {

    private static final Logger LOG = LogManager.getLogger();
    private final int conceptNid;
    private final Button openConceptButton = new Button("", Iconography.LINK_EXTERNAL.getIconographic());
    private final ManifoldCoordinate manifoldCoordinate;
    private PremiseType premiseType = PremiseType.INFERRED;

    public ConceptNode(int conceptNid, ManifoldCoordinate manifoldCoordinate) {
        this.conceptNid = conceptNid;
        this.manifoldCoordinate = manifoldCoordinate;
        this.setText(manifoldCoordinate.getPreferredDescriptionText(conceptNid));

        HBox controlBox;
        LatestVersion<Version> latest = manifoldCoordinate.getVertexStampFilter().latestConceptVersion(conceptNid);
        if (latest.isPresent()) {
            controlBox = new HBox(openConceptButton, AxiomView.computeGraphic(conceptNid, false,
                    latest.get().getStatus(), manifoldCoordinate, premiseType));
        } else {
            controlBox = new HBox(openConceptButton, AxiomView.computeGraphic(conceptNid, false,
                    Status.PRIMORDIAL, manifoldCoordinate, premiseType));
        }

        this.setGraphic(controlBox);
        setOnDragDetected(this::handleDragDetected);
        setOnDragDone(this::handleDragDone);
        openConceptButton.getStyleClass().setAll(StyleClasses.OPEN_CONCEPT_BUTTON.toString());
        openConceptButton.setOnMouseClicked(this::handleShowConceptNodeClick);
        ContextMenu contextMenu = new ContextMenu();
        this.setContextMenu(contextMenu);
        Menu copyMenu = MenuSupplierForFocusConcept.makeCopyMenuItem(Optional.of(Get.concept(this.conceptNid)), this.manifoldCoordinate);
        contextMenu.getItems().addAll(copyMenu.getItems());
    }

    private void handleShowConceptNodeClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
            showPopup(conceptNid, mouseEvent);
        }
    }

    private void showPopup(int conceptNid, MouseEvent mouseEvent) {

        Optional<LogicalExpression> expression = manifoldCoordinate.getLogicalExpression(conceptNid, premiseType);
        if (!expression.isPresent()) {
            premiseType = PremiseType.STATED;
            expression = manifoldCoordinate.getLogicalExpression(conceptNid, premiseType);
        }
        if (expression.isPresent()) {
            PopOver popover = new PopOver();
            popover.setContentNode(AxiomView.createWithCommitPanel(expression.get(),
                    premiseType,
                    manifoldCoordinate));
            popover.setCloseButtonEnabled(true);
            popover.setHeaderAlwaysVisible(false);
            popover.setTitle("");
            popover.show(openConceptButton, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            mouseEvent.consume();
        }
    }

    private void handleDragDetected(MouseEvent event) {
        LOG.debug("Drag detected: " + event);

        DragImageMaker dragImageMaker = new DragImageMaker(this);
        Dragboard db = this.startDragAndDrop(TransferMode.COPY);

        db.setDragView(dragImageMaker.getDragImage());

        IsaacClipboard content = new IsaacClipboard(Get.concept(conceptNid));
        db.setContent(content);
        event.consume();
    }

    private void handleDragDone(DragEvent event) {
        LOG.debug("Dragging done: " + event);
    }
}
