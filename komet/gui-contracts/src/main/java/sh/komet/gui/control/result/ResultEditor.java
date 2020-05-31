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
package sh.komet.gui.control.result;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.model.statement.MeasureImpl;
import sh.isaac.model.statement.ResultImpl;
import sh.komet.gui.control.measure.MeasureEditor;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class ResultEditor implements PropertyEditor<ResultImpl> {
    private final BorderPane editorPane = new BorderPane();
    private final SimpleObjectProperty<ResultImpl> editObservable; 
    private final CheckBox normalRangeCheck = new CheckBox("include normal range");
    private final ToolBar editorToolbar;
    private final ViewProperties viewProperties;
    private final MeasureEditor resultEditor;
    
    public ResultEditor(ObservableValue<ResultImpl> editObservable, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.editObservable = (SimpleObjectProperty<ResultImpl>) editObservable;
        this.normalRangeCheck.setOnAction(this::toggleNormalRange);
        this.editorToolbar = new ToolBar(normalRangeCheck);
        this.editorPane.setTop(editorToolbar);
        this.resultEditor =  new MeasureEditor(viewProperties);
        this.resultEditor.setValue((MeasureImpl) editObservable.getValue().getMeasure());
    }
    
    
    private void toggleNormalRange(ActionEvent event) {
        setupEditor();
    }

    private void setupEditor() {
        if (normalRangeCheck.isSelected()) {
            if (!editObservable.getValue().getNormalRange().isPresent()) {
                editObservable.getValue().setNormalRange(new MeasureImpl(viewProperties.getManifoldCoordinate()));
            }
            MeasureEditor normalRangeEditor = new MeasureEditor(viewProperties);
            normalRangeEditor.setValue((MeasureImpl) editObservable.getValue().getNormalRange().get());
            VBox vbox = new VBox(8); // spacing = 8
            vbox.getChildren().addAll(resultEditor.getEditor(), normalRangeEditor.getEditor());

            editorPane.setCenter(vbox);
        } else {
            editObservable.getValue().setNormalRange(null);
            editorPane.setCenter(this.resultEditor.getEditor());
        }
    }

    @Override
    public Node getEditor() {
        return this.editorPane;
    }

    @Override
    public ResultImpl getValue() {
        return editObservable.get();
    }

    @Override
    public void setValue(ResultImpl value) {
        editObservable.set(value);
        setupEditor();
    }
    
    
}
