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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.PropertySheetPurpose;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;
 
/**
 *
 * @author kec
 */
public class ConceptVersionEditor implements PropertyEditor<ObservableConceptVersionImpl> {
    private final GridPane editorGridPane = new GridPane();
    private final BorderPane editorNode = new BorderPane(editorGridPane);
    private final SimpleObjectProperty<ObservableConceptVersionImpl> conceptVersionProperty
            = new SimpleObjectProperty(
                    this,
                    ObservableFields.CONCEPT_VERSION.toExternalString(),
                    null);
    private UUID conceptUuid = null;
    private final Manifold manifold;
    
    private final PropertyEditorFactory propertyEditorFactory;
    private final List<PropertySheet.Item> wrappedProperties = new ArrayList<>();
    
    public ConceptVersionEditor(Manifold manifold) {
        this(null, manifold);
    }
    
    public ConceptVersionEditor(UUID conceptUuid, Manifold manifold) {
        this.manifold = manifold;
        this.conceptUuid = conceptUuid;
        this.propertyEditorFactory = new PropertyEditorFactory(manifold);
        if (conceptUuid != null) {
            setupWithConceptUuid();
        }
    }

    @Override
    public Node getEditor() {
        return editorNode;
    }

    @Override
    public ObservableConceptVersionImpl getValue() {
        return this.conceptVersionProperty.get();
    }

    @Override
    public void setValue(ObservableConceptVersionImpl value) {
        if (value != null) {
            this.conceptUuid = value.getPrimordialUuid();
        }
        this.conceptVersionProperty.set(value);
        setupProperties();
    }

    private void setupWithConceptUuid() {
        this.conceptVersionProperty.set(new ObservableConceptVersionImpl(conceptUuid));
        setupProperties();
    }

    private void setupProperties() {
        editorGridPane.getChildren().clear();
        wrappedProperties.clear();
        if (conceptVersionProperty.get() != null) {
            ObservableConceptVersionImpl concept = conceptVersionProperty.get();
            
            
            PropertySheetItem moduleProperty = createPropertyItem(concept.moduleNidProperty());
            PropertySheetItem pathProperty = createPropertyItem(concept.pathNidProperty());
            
            
            FxGet.rulesDrivenKometService().populateWrappedProperties(wrappedProperties);

            PropertyEditor<?> modulePropEditor = propertyEditorFactory.call(moduleProperty);
            PropertyEditor<?> pathPropertyEditor = propertyEditorFactory.call(pathProperty);
            
            Node editor = modulePropEditor.getEditor();
            GridPane.setConstraints(editor, 
                    0, 1, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 5)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = pathPropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    2, 1, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 1)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

        }
    }

    private PropertySheetItem createPropertyItem(Property<?> property) {
        PropertySheetItem wrappedProperty = new PropertySheetItem(property.getValue(), property, manifold, PropertySheetPurpose.DESCRIPTION_DIALECT);
        wrappedProperties.add(wrappedProperty);
        return wrappedProperty;
    }
    
}
