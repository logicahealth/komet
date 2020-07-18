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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.model.observable.commitaware.CommitAwareIntegerProperty;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableConceptVersionImpl;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.*;
import sh.komet.gui.util.FxGet;
 
/**
 *
 * @author kec
 */
public class NewConceptVersionEditor implements PropertyEditor<ObservableConceptVersionImpl> {
    private final GridPane editorGridPane = new GridPane();
    private final BorderPane editorNode = new BorderPane(editorGridPane);
    private final SimpleObjectProperty<ObservableConceptVersionImpl> conceptVersionProperty
            = new SimpleObjectProperty(
                    this,
                    ObservableFields.CONCEPT_VERSION.toExternalString(),
                    null);
    private final SimpleBooleanProperty conceptIsAssemblageProperty 
            = new SimpleBooleanProperty(this, ObservableFields.CONCEPT_IS_ASSEMBLAGE.toExternalString(), false);
    
    private final CommitAwareIntegerProperty semanticTypeForAssemblageProperty 
            = new CommitAwareIntegerProperty(this, TermAux.SEMANTIC_TYPE.toExternalString(), TermAux.MEMBERSHIP_SEMANTIC.getNid());
    private final SimpleStringProperty nameForAssemblageFieldProperty 
            = new SimpleStringProperty(this, ObservableFields.SEMANTIC_FIELD_NAME.toExternalString());
            
    private UUID conceptUuid = null;
    
    private final ManifoldCoordinate manifoldCoordinate;
    
    private final PropertyEditorFactory propertyEditorFactory;
    private final List<PropertySheet.Item> wrappedProperties = new ArrayList<>();
    
    private final SimpleBooleanProperty showSemanticFieldName 
            = new SimpleBooleanProperty(false);

    
    public NewConceptVersionEditor(ManifoldCoordinate manifoldCoordinate) {
        this(null, manifoldCoordinate);
    }
    
    public NewConceptVersionEditor(UUID conceptUuid, ManifoldCoordinate manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
        this.conceptUuid = conceptUuid;
        this.propertyEditorFactory = new PropertyEditorFactory(manifoldCoordinate);
        if (conceptUuid != null) {
            setupWithConceptUuid();
        }
        semanticTypeForAssemblageProperty.addListener((observable, oldValue, newValue) -> {
            if (conceptIsAssemblageProperty.get() && newValue.intValue() == TermAux.MEMBERSHIP_SEMANTIC.getNid()) {
                showSemanticFieldName.set(false);
            } else {
                showSemanticFieldName.set(true);
            }
        });
        
        conceptIsAssemblageProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue && semanticTypeForAssemblageProperty.get() != TermAux.MEMBERSHIP_SEMANTIC.getNid()) {
                showSemanticFieldName.set(true);
            } else {
                showSemanticFieldName.set(false);
            }
        });
        
    }

    public boolean conceptIsAssemblage() {
        return conceptIsAssemblageProperty.get();
    }

    public int getSemanticTypeForAssemblageNid() {
        return semanticTypeForAssemblageProperty.get();
    }

    public ConceptSpecification getSemanticTypeForAssemblage() {
        return Get.conceptSpecification(semanticTypeForAssemblageProperty.get());
    }
    
    public String getFieldNameForSemantic() {
        return nameForAssemblageFieldProperty.get();
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
        this.conceptVersionProperty.set(new ObservableConceptVersionImpl(conceptUuid, MetaData.SOLOR_CONCEPT____SOLOR.getNid()));
        setupProperties();
    }

    private void setupProperties() {
        editorGridPane.getChildren().clear();
        wrappedProperties.clear();
        if (conceptVersionProperty.get() != null) {
            ObservableConceptVersionImpl concept = conceptVersionProperty.get();
            
            
            PropertySheetItem moduleProperty = createConceptPropertyItem(concept.moduleNidProperty());
            PropertySheetItem pathProperty = createConceptPropertyItem(concept.pathNidProperty());
            PropertySheetItem assemblageProperty = createPropertyItem(conceptIsAssemblageProperty);
            PropertySheetItem semanticTypeProperty = createPropertyItem(semanticTypeForAssemblageProperty);
            PropertySheetItem nameForFieldProperty = createPropertyItem(nameForAssemblageFieldProperty);

            WindowPreferences windowPreferences = FxGet.windowPreferences(this.editorNode);


            FxGet.rulesDrivenKometService().populateWrappedProperties(wrappedProperties,
                    manifoldCoordinate.toManifoldCoordinateImmutable(),
                    windowPreferences.getViewPropertiesForWindow().getEditCoordinate().getValue());

            PropertyEditor<?> modulePropEditor = propertyEditorFactory.call(moduleProperty);
            PropertyEditor<?> pathPropertyEditor = propertyEditorFactory.call(pathProperty);
            PropertyEditor<?> conceptIsAssemblageEditor = propertyEditorFactory.call(assemblageProperty);
            PropertyEditor<?> semanticTypeEditor = propertyEditorFactory.call(semanticTypeProperty);
            PropertyEditor<?> nameForFieldEditor = propertyEditorFactory.call(nameForFieldProperty);
            
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

            editor = conceptIsAssemblageEditor.getEditor();
            GridPane.setConstraints(editor, 
                    0, 2, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(5, 1, 1, 25)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = semanticTypeEditor.getEditor();
            editor.visibleProperty().bind(conceptIsAssemblageProperty);
            GridPane.setConstraints(editor, 
                    2, 2, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 1)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = nameForFieldEditor.getEditor();
            editor.visibleProperty().bind(showSemanticFieldName);
            GridPane.setConstraints(editor, 
                    2, 3, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 1)); //t,r,b,l
            editorGridPane.getChildren().add(editor);
        }
    }

    private PropertySheetItem createConceptPropertyItem(IntegerProperty property) {
        PropertySheetItem wrappedProperty = new PropertySheetItem(property.getValue(), property, manifoldCoordinate, PropertySheetPurpose.UNSPECIFIED);
        wrappedProperty.setEditorType(EditorType.CONCEPT_SPEC_CHOICE_BOX);
        wrappedProperties.add(wrappedProperty);
        return wrappedProperty;
    }
    
            
    private PropertySheetItem createPropertyItem(Property<?> property) {
        PropertySheetItem wrappedProperty = new PropertySheetItem(property.getValue(), property, manifoldCoordinate, PropertySheetPurpose.UNSPECIFIED);
        wrappedProperties.add(wrappedProperty);
        return wrappedProperty;
    }
    
}
