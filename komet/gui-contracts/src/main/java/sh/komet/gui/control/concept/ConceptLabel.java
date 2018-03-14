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
package sh.komet.gui.control.concept;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.manifold.Manifold;
import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;

/**
 *
 * @author kec
 */
public class ConceptLabel
        extends Label implements PropertyEditor<Object> {

    private static final String EMPTY_TEXT = "empty";
    TransferMode[] transferMode = null;
    Manifold manifold;
    Consumer<ConceptLabel> descriptionTextUpdater;
    Background originalBackground;
    final Function<ConceptLabel, List<MenuItem>> menuSupplier;
    final SimpleObjectProperty<ConceptSpecification> conceptInLabel = new SimpleObjectProperty<>();

   //~--- constructors --------------------------------------------------------
   public ConceptLabel(Manifold manifold,
            Consumer<ConceptLabel> descriptionTextUpdater,
            Function<ConceptLabel, List<MenuItem>> menuSupplier) {
        super(EMPTY_TEXT);
        if (menuSupplier == null) {
            throw new IllegalStateException("Supplier<List<MenuItem>> menuSupplier cannot be null");
        }
        this.manifold = manifold;
        this.descriptionTextUpdater = descriptionTextUpdater;
        this.menuSupplier = menuSupplier;
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.setOnDragOver(this::handleDragOver);
        this.setOnDragEntered(this::handleDragEntered);
        this.setOnDragDetected(this::handleDragDetected);
        this.setOnDragExited(this::handleDragExited);
        this.setOnDragDropped(this::handleDragDropped);
        this.setOnDragDone(this::handleDragDone);
        this.setMinWidth(100);
        
        ContextMenu contextMenu = new ContextMenu();
        setupContextMenu(contextMenu);
        this.setContextMenu(contextMenu);
        contextMenu.setOnShowing(this::handle);
    }

    //~--- methods -------------------------------------------------------------
   public SimpleObjectProperty<ConceptSpecification> conceptInLabelProperty() {
       return conceptInLabel;
   }
    private void handle(WindowEvent event) {
        ContextMenu contextMenu = (ContextMenu) event.getSource();
        
        if (this.menuSupplier != null) {
            setupContextMenu(contextMenu);
        } 
    }

    private void setupContextMenu(ContextMenu contextMenu) {
        contextMenu.getItems().clear();
        List<MenuItem> menuItems = this.menuSupplier.apply(this);
        if (!menuItems.isEmpty()) {
            for (MenuItem menu : menuItems) {
                contextMenu.getItems().add(menu);
            }
        }
    }
    
    private void handleDragDetected(MouseEvent event) {
        System.out.println("Drag detected: " + event);
        
        DragImageMaker dragImageMaker = new DragImageMaker(this);
        Dragboard db = this.startDragAndDrop(TransferMode.COPY);
        
        db.setDragView(dragImageMaker.getDragImage());

        /* put a string on dragboard */
        IsaacClipboard content = new IsaacClipboard(Get.concept(this.conceptInLabel.get()));
        db.setContent(content);
        event.consume();
    }
    
    private void handleDragDone(DragEvent event) {
        System.out.println("Dragging done: " + event);
        this.setBackground(originalBackground);
        this.transferMode = null;
    }
    
    public void setConceptChronology(ConceptChronology conceptChronology) {
        this.conceptInLabel.set(conceptChronology);
        descriptionTextUpdater.accept(this);
    }
    
    private void handleDragDropped(DragEvent event) {
        System.out.println("Dragging dropped: " + event);
        
        Dragboard db = event.getDragboard();
        
        if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
            ConceptChronology conceptChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT);
            
            this.setValue(conceptChronology);
        } else if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
            ConceptVersion conceptVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT_VERSION);
            
            this.setValue(conceptVersion.getChronology());
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION)) {
            SemanticChronology semanticChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION);
            
            this.setValue(Get.conceptService()
                    .getConceptChronology(semanticChronology.getReferencedComponentNid()));
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION_VERSION)) {
            DescriptionVersion descriptionVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION_VERSION);
            
            this.setValue(
                    Get.conceptService()
                            .getConceptChronology(descriptionVersion.getReferencedComponentNid()));
        }
        
        this.setBackground(originalBackground);
    }
    
    private void handleDragEntered(DragEvent event) {
        System.out.println("Dragging entered: " + event);
        this.originalBackground = this.getBackground();
        
        Color backgroundColor;
        Set<DataFormat> contentTypes = event.getDragboard()
                .getContentTypes();
        
        if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.CONCEPT_TYPES)) {
            backgroundColor = Color.AQUA;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else if (IsaacClipboard.containsAny(contentTypes, IsaacClipboard.DESCRIPTION_TYPES)) {
            backgroundColor = Color.OLIVEDRAB;
            this.transferMode = TransferMode.COPY_OR_MOVE;
        } else {
            backgroundColor = Color.RED;
            this.transferMode = null;
        }
        
        BackgroundFill fill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        
        this.setBackground(new Background(fill));
    }
    
    private void handleDragExited(DragEvent event) {
        System.out.println("Dragging exited: " + event);
        this.setBackground(originalBackground);
        this.transferMode = null;
    }
    
    private void handleDragOver(DragEvent event) {
        // System.out.println("Dragging over: " + event );
        if (this.transferMode != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            event.consume();
        }
    }

    //~--- set methods ---------------------------------------------------------
    private void setDescriptionText(DescriptionVersion latestDescriptionVersion) {
        if (latestDescriptionVersion != null) {
            this.setText(latestDescriptionVersion.getText());
        }
    }
    
    private void setEmptyText() {
        setEmptyText(this);
    }
    
    private static void setEmptyText(Label label) {
        label.setText(EMPTY_TEXT);
    }
    
    public static void setFullySpecifiedText(ConceptLabel label) {
        ConceptChronology focusedConcept = Get.concept(label.conceptInLabel.get());
        if (focusedConcept != null) {
            focusedConcept
                    .getFullySpecifiedDescription(label.manifold)
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }
    
    public static void setPreferredText(ConceptLabel label) {
        ConceptChronology focusedConcept = Get.concept(label.conceptInLabel.get());
        if (focusedConcept != null) {
            focusedConcept
                    .getPreferredDescription(label.manifold)
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }
    
    @Override
    public Node getEditor() {
        return this;
    }
    
    @Override
    public Integer getValue() {
        return conceptInLabel.get().getNid();
    }
    
    @Override
    public void setValue(Object value) {
        if (value != null) {
            if (value instanceof Integer) {
                Integer intValue = (Integer) value;
                if (intValue < 0) {
                    conceptInLabel.set(Get.concept((Integer) value));
                }
            } else if (value instanceof ConceptSpecification) {
                ConceptSpecification spec = (ConceptSpecification) value;
                if (spec.getNid() < 0) {
                    conceptInLabel.set(Get.concept((ConceptSpecification) value));
                }
            } else {
                throw new UnsupportedOperationException("ConceptLabel can't handle: " + value);
            }
            descriptionTextUpdater.accept(this);
        } else {
            this.setText(EMPTY_TEXT);
        }
    }
    
}
