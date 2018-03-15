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
package sh.komet.gui.control.list;

import java.util.function.Function;
import java.util.function.Supplier;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 * @param <T>
 */
public class ListEditorCell<T extends Object> extends ListCell<T> {
    Button cancelButton = new Button("", Iconography.CANCEL.getIconographic());
    Button duplicateButton = new Button("", Iconography.DUPLICATE.getIconographic());
    VBox buttonBox = new VBox(cancelButton, duplicateButton);
    GridPane gridPane = new GridPane();
    private final ListView listView;
    private final Manifold manifold;
    private PropertyEditor<T> propertyEditor;
    private final Function<Manifold,PropertyEditor<T>> newEditorSupplier;
    private final Supplier<T> newObjectSupplier;

    public ListEditorCell(ListView listView, 
            Function<Manifold,PropertyEditor<T>> newEditorSupplier, 
            Supplier<T> newObjectSupplier,
            Manifold manifold) {
        this.listView = listView;
        this.manifold = manifold;
        this.duplicateButton.setOnAction(this::duplicate);
        this.cancelButton.setOnAction(this::cancel);
        this.newEditorSupplier = newEditorSupplier;
        this.newObjectSupplier = newObjectSupplier;
    }
    
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText("");
            if (this.propertyEditor != null) {
                propertyEditor.setValue(null);
                this.propertyEditor = null;
                this.gridPane.getChildren().clear();
            }
        } else {
            
            if (this.propertyEditor == null) {
                this.propertyEditor = newEditorSupplier.apply(manifold);
                Node editorNode = propertyEditor.getEditor();
                GridPane.setConstraints(this.buttonBox,
                                  0,
                                  0,
                                  1,
                                  1,
                                  HPos.LEFT,
                                  VPos.TOP, 
                                  Priority.NEVER, 
                                  Priority.NEVER);
                GridPane.setConstraints(editorNode,
                                  1,
                                  0,
                                  1,
                                  2,
                                  HPos.LEFT,
                                  VPos.TOP, 
                                  Priority.ALWAYS, 
                                  Priority.NEVER);
                this.gridPane.getChildren().setAll(this.buttonBox, this.propertyEditor.getEditor());
                this.setGraphic(this.gridPane);
            }
            if (item != propertyEditor.getValue()) {
                propertyEditor.setValue(item);
            }
            setText("");
        }
    }
    
    private void duplicate(Event event) {
        T newObject = this.newObjectSupplier.get();
        // TODO: copy all fields. 
        listView.getItems().add(this.getIndex() + 1, newObject);
    }
    private void cancel(Event event) {
        listView.getItems().remove(this.getIndex());
    }
}
