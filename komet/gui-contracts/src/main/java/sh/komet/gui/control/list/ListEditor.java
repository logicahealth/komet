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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 * @param <T>
 */
public class ListEditor<T extends Object>
        implements PropertyEditor<ObservableList<T>> {

    private final BorderPane editorPane = new BorderPane();
    private final Button newItem = new Button("", Iconography.ADD.getIconographic());
    private final ToolBar editorToolbar = new ToolBar(newItem);
    private final ListView listView = new ListView();
    private final Supplier<T> newObjectSupplier;
    private final Function<Manifold,PropertyEditor<T>> newEditorSupplier;
    private final Manifold manifold;
    
    public ListEditor(Manifold manifold, Supplier<T> newObjectSupplier, Function<Manifold,PropertyEditor<T>> newEditorSupplier) {
        this.editorPane.setTop(editorToolbar);
        this.editorPane.setCenter(listView);
        this.newItem.setOnAction(this::newItem);
        this.newObjectSupplier = newObjectSupplier;
        this.newEditorSupplier = newEditorSupplier;
        this.manifold = manifold;
        listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListEditorCell<T> call(ListView<T> listView) {
                return new ListEditorCell(listView, ListEditor.this.newEditorSupplier, newObjectSupplier, ListEditor.this.manifold);
            }
        });
    }
    
    private void newItem(Event event) {
       listView.getItems().add(newObjectSupplier.get());
       listView.requestLayout();
    }

    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public ObservableList<T> getValue() {
        return listView.getItems();
    }

    @Override
    public void setValue(ObservableList<T> value) {
        listView.setItems(value);
    }

}
