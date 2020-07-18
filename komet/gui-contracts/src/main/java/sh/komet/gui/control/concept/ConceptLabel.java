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
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.WindowEvent;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.DragAndDropHelper;

import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;

/**
 *
 * @author kec
 * @deprecated use ConceptSpecificationEditor
 */
@Deprecated
public class ConceptLabel
        extends Label implements PropertyEditor<Object> {

    private static final String EMPTY_TEXT = "empty";
    ManifoldCoordinate manifoldCoordinate;
    Consumer<ConceptLabel> descriptionTextUpdater;
    final Function<ConceptLabel, List<MenuItem>> menuSupplier;
    final SimpleObjectProperty<ConceptSpecification> conceptInLabel = new SimpleObjectProperty<>();
    final DragAndDropHelper dragAndDropHelper;

   //~--- constructors --------------------------------------------------------
   public ConceptLabel(ManifoldCoordinate manifoldCoordinate,
                       Consumer<ConceptLabel> descriptionTextUpdater,
                       Function<ConceptLabel, List<MenuItem>> menuSupplier) {
        super(EMPTY_TEXT);
        if (menuSupplier == null) {
            throw new IllegalStateException("Supplier<List<MenuItem>> menuSupplier cannot be null");
        }
        this.manifoldCoordinate = manifoldCoordinate;
        this.descriptionTextUpdater = descriptionTextUpdater;
        this.menuSupplier = menuSupplier;
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.dragAndDropHelper = new DragAndDropHelper(this, () -> Get.concept(this.conceptInLabel.get()),
                this::setValue, mouseEvent -> true, dragEvent -> true);
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

    public void setConceptChronology(ConceptChronology conceptChronology) {
        this.conceptInLabel.set(conceptChronology);
        descriptionTextUpdater.accept(this);
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
                    .getFullySpecifiedDescription(label.manifoldCoordinate)
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
                    .getPreferredDescription(label.manifoldCoordinate)
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
