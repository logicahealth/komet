/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.gui.control.concept;

//~--- JDK imports ------------------------------------------------------------
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

//~--- non-JDK imports --------------------------------------------------------
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;

import javafx.geometry.Insets;
import javafx.scene.Node;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
import sh.isaac.api.component.semantic.version.DescriptionVersion;

import sh.komet.gui.drag.drop.DragImageMaker;
import sh.komet.gui.drag.drop.IsaacClipboard;
import sh.komet.gui.manifold.HistoryRecord;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.docbook.DocBook;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ManifoldLinkedConceptLabel
        extends Label implements PropertyEditor<Object> {

    private static final String EMPTY_TEXT = "empty";
    TransferMode[] transferMode = null;
    Manifold manifold;
    Consumer<ManifoldLinkedConceptLabel> descriptionTextUpdater;
    Background originalBackground;
    final Supplier<List<MenuItem>> menuSupplier;

    //~--- constructors --------------------------------------------------------
    public ManifoldLinkedConceptLabel(Manifold manifold,
            Consumer<ManifoldLinkedConceptLabel> descriptionTextUpdater,
            Supplier<List<MenuItem>> menuSupplier) {
        super(EMPTY_TEXT);
        if (menuSupplier == null) {
            throw new IllegalStateException("Supplier<List<MenuItem>> menuSupplier cannot be null");
        }
        this.manifold = manifold;
        this.descriptionTextUpdater = descriptionTextUpdater;
        this.menuSupplier = menuSupplier;
        this.manifold.focusedConceptProperty().addListener(
                (ObservableValue<? extends ConceptSpecification> observable,
                        ConceptSpecification oldValue,
                        ConceptSpecification newValue) -> {
                    this.descriptionTextUpdater.accept(this);
                });
        if (this.manifold.getFocusedConcept().isPresent()) {
           this.descriptionTextUpdater.accept(this);
        }
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.setOnDragOver(this::handleDragOver);
        this.setOnDragEntered(this::handleDragEntered);
        this.setOnDragDetected(this::handleDragDetected);
        this.setOnDragExited(this::handleDragExited);
        this.setOnDragDropped(this::handleDragDropped);
        this.setOnDragDone(this::handleDragDone);
        this.setMinWidth(100);

        ContextMenu contextMenu = new ContextMenu();

        for (String manifoldGroupName : Manifold.getGroupNames()) {
            MenuItem item = new MenuItem(manifoldGroupName + " history");
            contextMenu.getItems()
                    .add(item);
        }

        this.setContextMenu(contextMenu);
        contextMenu.setOnShowing(this::handle);
    }

    //~--- methods -------------------------------------------------------------
    private MenuItem makeCopyMenuItem() {
        Menu copyMenu = new Menu("copy");
        MenuItem copyDocBookMenuItem = new MenuItem("docbook glossary entry");
        copyMenu.getItems().add(copyDocBookMenuItem);
        copyDocBookMenuItem.setOnAction((event) -> {
            Optional<ConceptSpecification> concept = this.manifold.getFocusedConcept();
            if (concept.isPresent()) {
                String docbookXml = DocBook.getGlossentry(concept.get(), manifold);
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(docbookXml);
                clipboard.setContent(content);
            }
        });
        MenuItem copyJavaShortSpecMenuItem = new MenuItem("Java concept specification");
        copyMenu.getItems().add(copyJavaShortSpecMenuItem);
        copyJavaShortSpecMenuItem.setOnAction((event) -> {
            Optional<ConceptSpecification> concept = this.manifold.getFocusedConcept();
            if (concept.isPresent()) {
                ConceptSpecification conceptSpec = (ConceptSpecification) concept.get();
                StringBuilder builder = new StringBuilder();
                builder.append("new ConceptProxy(\"");
                builder.append(conceptSpec.toExternalString());
                builder.append("\")");
                
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(builder.toString());
                clipboard.setContent(content);
            }
        });
        MenuItem copyJavaSpecMenuItem = new MenuItem("Java qualified concept specification");
        copyMenu.getItems().add(copyJavaSpecMenuItem);
        copyJavaSpecMenuItem.setOnAction((event) -> {
            Optional<ConceptSpecification> concept = this.manifold.getFocusedConcept();
            if (concept.isPresent()) {
                ConceptSpecification conceptSpec = (ConceptSpecification) concept.get();
                StringBuilder builder = new StringBuilder();
                builder.append("new sh.isaac.api.ConceptProxy(\"");
                builder.append(conceptSpec.toExternalString());
                builder.append("\")");
                
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(builder.toString());
                clipboard.setContent(content);
            }
        });
        

        return copyMenu;

    }

    private void handle(WindowEvent event) {
        ContextMenu contextMenu = (ContextMenu) event.getSource();
        contextMenu.getItems().clear();

        if (this.menuSupplier != null) {
            List<MenuItem> menuItems = this.menuSupplier.get();
            if (!menuItems.isEmpty()) {
                for (MenuItem menu : menuItems) {
                    contextMenu.getItems().add(menu);
                }
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
        }
        contextMenu.getItems()
                .add(makeCopyMenuItem());
        contextMenu.getItems()
                .add(new SeparatorMenuItem());

        Menu manifoldHistoryMenu = new Menu("history");
        contextMenu.getItems().add(manifoldHistoryMenu);
        Collection<HistoryRecord> historyCollection = this.manifold.getHistoryRecords();

        setupHistoryMenuItem(historyCollection, manifoldHistoryMenu);

        for (String manifoldGroupName : Manifold.getGroupNames()) {
            Menu groupHistory = new Menu(manifoldGroupName + " history");
            contextMenu.getItems().add(groupHistory);
            setupHistoryMenuItem(Manifold.getGroupHistory(manifoldGroupName), groupHistory);
        }
    }

    private void setupHistoryMenuItem(Collection<HistoryRecord> historyCollection, Menu manifoldHistoryMenu) {
        for (HistoryRecord historyRecord : historyCollection) {
            MenuItem historyItem = new MenuItem(historyRecord.getComponentString());
            historyItem.setUserData(historyRecord);
            historyItem.setOnAction((ActionEvent actionEvent) -> {
                unlink();
                MenuItem historyMenuItem = (MenuItem) actionEvent.getSource();
                HistoryRecord itemHistoryRecord = (HistoryRecord) historyItem.getUserData();
                this.manifold.setFocusedConceptChronology(Get.concept(itemHistoryRecord.getComponentId()));
            });
            manifoldHistoryMenu.getItems().add(historyItem);
        }
    }

    private void unlink() {
        if (!this.manifold
                .getGroupName()
                .equals(ManifoldGroup.UNLINKED.getGroupName())) {
            this.manifold
                    .setGroupName(ManifoldGroup.UNLINKED.getGroupName());
        }
    }

    private void handleDragDetected(MouseEvent event) {
        System.out.println("Drag detected: " + event);

        if (this.manifold.getFocusedConcept().isPresent()) {
            DragImageMaker dragImageMaker = new DragImageMaker(this);
            Dragboard db = this.startDragAndDrop(TransferMode.COPY);

            db.setDragView(dragImageMaker.getDragImage());

            /* put a string on dragboard */
            IsaacClipboard content = new IsaacClipboard(Get.concept(this.manifold.getFocusedConcept().get()));
            db.setContent(content);
            event.consume();
        }
    }

    private void handleDragDone(DragEvent event) {
        System.out.println("Dragging done: " + event);
        this.setBackground(originalBackground);
        this.transferMode = null;
    }

    public void setConceptChronology(ConceptChronology conceptChronology) {
        this.manifold
                .setFocusedConceptChronology(conceptChronology);
    }

    private void handleDragDropped(DragEvent event) {
        System.out.println("Dragging dropped: " + event);

        unlink();

        Dragboard db = event.getDragboard();

        if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT)) {
            ConceptChronology conceptChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT);

            this.manifold
                    .setFocusedConceptChronology(conceptChronology);
        } else if (db.hasContent(IsaacClipboard.ISAAC_CONCEPT_VERSION)) {
            ConceptVersion conceptVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_CONCEPT_VERSION);

            this.manifold
                    .setFocusedConceptChronology(conceptVersion.getChronology());
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION)) {
            SemanticChronology semanticChronology = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION);

            this.manifold
                    .setFocusedConceptChronology(Get.conceptService()
                            .getConceptChronology(semanticChronology.getReferencedComponentNid()));
        } else if (db.hasContent(IsaacClipboard.ISAAC_DESCRIPTION_VERSION)) {
            DescriptionVersion descriptionVersion = Get.serializer()
                    .toObject(db, IsaacClipboard.ISAAC_DESCRIPTION_VERSION);

            this.manifold
                    .setFocusedConceptChronology(
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

    public static void setFullySpecifiedText(ManifoldLinkedConceptLabel label) {
        
        if (label.manifold.getFocusedConcept().isPresent()) {
            ConceptChronology focusedConcept = Get.concept(label.manifold.getFocusedConcept().get());
            focusedConcept
                    .getFullySpecifiedDescription(label.manifold)
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }

    public static void setPreferredText(ManifoldLinkedConceptLabel label) {
        if (label.manifold.getFocusedConcept().isPresent()) {
            ConceptChronology focusedConcept = Get.concept(label.manifold.getFocusedConcept().get());
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
        if (manifold.getFocusedConcept().isPresent()) {
            return this.manifold.getFocusedConcept().get().getNid();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Integer) {
            Integer intValue = (Integer) value;
            if (intValue < 0) {
                this.manifold.setFocusedConceptChronology(Get.concept((Integer) value));
            }
        } else if (value instanceof ConceptSpecification) {
            ConceptSpecification spec = (ConceptSpecification) value;
            if (spec.getNid() < 0) {
                this.manifold.setFocusedConceptChronology(Get.concept((ConceptSpecification) value));
            }

        } else {
            throw new UnsupportedOperationException("ConceptLabel can't handle: " + value);
        }

    }

}
