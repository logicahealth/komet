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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.drag.drop.DragAndDropHelper;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.Optional;
import java.util.function.Consumer;

import static sh.komet.gui.style.StyleClasses.CONCEPT_LABEL;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class ConceptLabelWithDragAndDrop
        extends Label implements PropertyEditor<IdentifiedObject> {

    public static final String EMPTY_TEXT = "empty";
    final ViewProperties viewProperties;
    final SimpleObjectProperty<IdentifiedObject> conceptFocusProperty;
    final Consumer<ConceptLabelWithDragAndDrop> descriptionTextUpdater;
    final DragAndDropHelper dragAndDropHelper;
    final SimpleIntegerProperty selectionIndexProperty;
    final Runnable unlink;
    final AddToContextMenu[] contextMenuProviders;


    //~--- constructors --------------------------------------------------------
    public ConceptLabelWithDragAndDrop(ViewProperties viewProperties,
                                       SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                                       Consumer<ConceptLabelWithDragAndDrop> descriptionTextUpdater,
                                       SimpleIntegerProperty selectionIndexProperty,
                                       Runnable unlink,
                                       AddToContextMenu[] contextMenuProviders) {
        super(EMPTY_TEXT);
        this.viewProperties = viewProperties;
        this.conceptFocusProperty = conceptFocusProperty;
        this.descriptionTextUpdater = descriptionTextUpdater;
        this.selectionIndexProperty = selectionIndexProperty;
        this.unlink = unlink;
        this.contextMenuProviders = contextMenuProviders;
        this.descriptionTextUpdater.accept(this);
        this.conceptFocusProperty.addListener((observable, oldValue, newValue) -> {
            this.descriptionTextUpdater.accept(this);
        });
        this.getStyleClass().add(CONCEPT_LABEL.toString());
        this.dragAndDropHelper = new DragAndDropHelper(this, () -> {
            Optional<IdentifiedObject> optionalConcept = Optional.ofNullable(conceptFocusProperty.get());

            if (optionalConcept.isPresent()) {
                return optionalConcept.get();
            }
            return null;

        } , this::droppedValue, mouseEvent -> true,
                dragEvent -> true);


        this.setMinWidth(100);

        ContextMenu contextMenu = new ContextMenu();

        for (String activityFeedName : ViewProperties.ACTIVITY_FEED_NAMES) {
            MenuItem item = new MenuItemWithText(activityFeedName + " history");
            contextMenu.getItems().add(item);
        }

        this.setContextMenu(contextMenu);
        contextMenu.setOnShowing(this::handle);
    }

    //~--- methods -------------------------------------------------------------

    void droppedValue(IdentifiedObject value) {
        this.unlink.run();
        setValue(value);
    }


    private void handle(WindowEvent event) {
        ContextMenu contextMenu = (ContextMenu) event.getSource();
        contextMenu.getItems().clear();
        for (AddToContextMenu contextMenuProvider: contextMenuProviders) {
            contextMenuProvider.addToContextMenu(contextMenu, this.viewProperties,
                    this.conceptFocusProperty, this.selectionIndexProperty, this.unlink);
        }

    }

    public void setConcept(ConceptSpecification conceptSpecification) {
        this.conceptFocusProperty.set(conceptSpecification);
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

    public static void setFullyQualifiedText(ConceptLabelWithDragAndDrop label) {

        Optional<IdentifiedObject> optionalConcept = Optional.ofNullable(label.conceptFocusProperty.getValue());
        if (optionalConcept.isPresent()) {
            label.viewProperties.getManifoldCoordinate().getFullyQualifiedDescription(optionalConcept.get().getNid())
                    .ifPresent(label::setDescriptionText)
                    .ifAbsent(label::setEmptyText);
        } else {
            setEmptyText(label);
        }
    }

    public static void setPreferredText(ConceptLabelWithDragAndDrop label) {
        Optional<IdentifiedObject> optionalConcept = Optional.ofNullable(label.conceptFocusProperty.getValue());
        if (optionalConcept.isPresent()) {
            label.viewProperties.getManifoldCoordinate().getPreferredDescription(optionalConcept.get().getNid())
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
    public IdentifiedObject getValue() {
        return this.conceptFocusProperty.getValue();
    }

    @Override
    public void setValue(IdentifiedObject value) {
        if (value instanceof ConceptSpecification) {
            ConceptSpecification spec = (ConceptSpecification) value;
            if (spec.getNid() < 0) {
                this.setConcept(Get.concept((ConceptSpecification) value));
            }
        } else {
            throw new UnsupportedOperationException("ConceptLabel can't handle: " + value);
        }
    }
}
