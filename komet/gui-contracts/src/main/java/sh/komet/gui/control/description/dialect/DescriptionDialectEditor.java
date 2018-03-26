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
package sh.komet.gui.control.description.dialect;

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
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.MetaData;
import sh.isaac.model.observable.ObservableDescriptionDialect;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.observable.version.ObservableComponentNidVersionImpl;
import sh.isaac.model.observable.version.ObservableDescriptionVersionImpl;
import sh.komet.gui.control.property.PropertyEditorFactory;
import sh.komet.gui.control.property.PropertySheetItem;
import sh.komet.gui.control.property.PropertySheetPurpose;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class DescriptionDialectEditor implements PropertyEditor<ObservableDescriptionDialect> {

    private final GridPane editorGridPane = new GridPane();
    private final BorderPane editorNode = new BorderPane(editorGridPane);

    private UUID conceptUuid = null;
    private UUID descriptionUuid = null;

    private final SimpleObjectProperty<ObservableDescriptionDialect> descriptionDialectProperty
            = new SimpleObjectProperty(
                    this,
                    ObservableFields.DESCRIPTION_DIALECT.toExternalString(),
                    null);
    private final Manifold manifold;
    
    private final PropertyEditorFactory propertyEditorFactory;
    private final List<Item> wrappedProperties = new ArrayList<>();

    public DescriptionDialectEditor(UUID conceptUuid, Manifold manifold) {
        this.manifold = manifold;
        this.propertyEditorFactory = new PropertyEditorFactory(manifold);
        if (conceptUuid != null) {
            setupWithConceptUuid(conceptUuid);
        }
    }

    private void setupWithConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
        this.descriptionUuid = UUID.randomUUID();
        ObservableDescriptionVersionImpl description = new ObservableDescriptionVersionImpl(descriptionUuid, conceptUuid, MetaData.SOLOR_CONCEPT____SOLOR.getNid());
        ObservableComponentNidVersionImpl dialect = new ObservableComponentNidVersionImpl(UUID.randomUUID(), descriptionUuid, MetaData.ENGLISH_LANGUAGE____SOLOR.getNid());
        this.descriptionDialectProperty.set(new ObservableDescriptionDialect(description, dialect));
        setupProperties();
    }

    public DescriptionDialectEditor(Manifold manifold) {
        this(null, manifold);
    }

    @Override
    public Node getEditor() {
        return editorNode;
    }

    @Override
    public ObservableDescriptionDialect getValue() {
        return descriptionDialectProperty.get();
    }

    @Override
    public void setValue(ObservableDescriptionDialect value) {
        descriptionDialectProperty.set(value);
        setupProperties();
    }

    public void setValue(UUID conceptUuid) {
        setupWithConceptUuid(conceptUuid);
    }

    private void setupProperties() {
        editorGridPane.getChildren().clear();
        wrappedProperties.clear();
        if (descriptionDialectProperty.get() != null) {
            ObservableDescriptionVersionImpl description = descriptionDialectProperty.get().getDescription();
            ObservableComponentNidVersionImpl dialect = descriptionDialectProperty.get().getDialect();
            
            
            PropertySheetItem textProperty = createPropertyItem(description.textProperty());
            PropertySheetItem langProperty = createPropertyItem(description.languageConceptNidProperty());
            PropertySheetItem typeProperty = createPropertyItem(description.descriptionTypeConceptNidProperty());
            PropertySheetItem caseProperty = createPropertyItem(description.caseSignificanceConceptNidProperty());
            PropertySheetItem dialectProperty = createPropertyItem(dialect.assemblageNidProperty());
            PropertySheetItem acceptabilityProperty = createPropertyItem(dialect.componentNidProperty());
            
            
            FxGet.rulesDrivenKometService().populateWrappedProperties(wrappedProperties);

            PropertyEditor<?> textPropEditor = propertyEditorFactory.call(textProperty);
            PropertyEditor<?> langPropertyEditor = propertyEditorFactory.call(langProperty);
            PropertyEditor<?> typePropertyEditor = propertyEditorFactory.call(typeProperty);
            PropertyEditor<?> casePropertyEditor = propertyEditorFactory.call(caseProperty);
            PropertyEditor<?> dialectPropertyEditor = propertyEditorFactory.call(dialectProperty);
            PropertyEditor<?> acceptabilityPropertyEditor = propertyEditorFactory.call(acceptabilityProperty);
            
            Node editor = textPropEditor.getEditor();
            GridPane.setConstraints(editor, 
                    0, 0, // column, row
                    7, 1, // column span, row span
                    HPos.LEFT, VPos.TOP, // halign, valign
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 5)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = langPropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    0, 1, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 5)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = typePropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    2, 1, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 1)); //t,r,b,l
            editorGridPane.getChildren().add(editor);

            editor = casePropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    4, 1, // column, row
                    2, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 1)); //t,r,b,l
            editorGridPane.getChildren().add(editor);
            
            GridPane dialectGrid = new GridPane();
            GridPane.setConstraints(dialectGrid, 
                    0, 2, // column, row
                    7, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 5)); //t,r,b,l
            editorGridPane.getChildren().add(dialectGrid);

            editor = dialectPropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    0, 0, // column, row
                    1, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 1, 1, 25)); //t,r,b,l
            dialectGrid.getChildren().add(editor);

            editor = acceptabilityPropertyEditor.getEditor();
            GridPane.setConstraints(editor, 
                    1, 0, // column, row
                    1, 1, 
                    HPos.LEFT, VPos.TOP, 
                    Priority.ALWAYS, Priority.NEVER, // hgrow, vgrow
                    new Insets(1, 0, 1, 1)); //t,r,b,l
            dialectGrid.getChildren().add(editor);
        }
    }

    private PropertySheetItem createPropertyItem(Property<?> property) {
        PropertySheetItem wrappedProperty = new PropertySheetItem(property.getValue(), property, manifold, PropertySheetPurpose.DESCRIPTION_DIALECT);
        wrappedProperties.add(wrappedProperty);
        return wrappedProperty;
    }

}
