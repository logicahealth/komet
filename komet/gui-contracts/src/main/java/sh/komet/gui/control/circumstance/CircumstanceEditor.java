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
package sh.komet.gui.control.circumstance;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.model.statement.CircumstanceImpl;
import sh.isaac.model.statement.PerformanceCircumstanceImpl;
import sh.isaac.model.statement.RequestCircumstanceImpl;
import sh.isaac.model.statement.UnstructuredCircumstanceImpl;
import sh.komet.gui.control.property.ViewProperties;

/**
 *
 * @author kec
 */
public class CircumstanceEditor implements PropertyEditor<CircumstanceImpl> {

    private final BorderPane editorPane = new BorderPane();
    private final SimpleObjectProperty<CircumstanceImpl> circumstanceProperty; 
            //= new SimpleObjectProperty<>(this, ObservableFields.STATEMENT_CIRCUMSTANCE.toExternalString());
    private final ChoiceBox circumstanceChoice;
    private final ToolBar circumstanceToolbar;
    private final Button createButton;
    private final Button cancelButton;
    private final ViewProperties viewProperties;
    
    public CircumstanceEditor(ObservableValue<? extends Object> observable, ViewProperties viewProperties) {
        this.viewProperties = viewProperties;
        this.circumstanceProperty = (SimpleObjectProperty<CircumstanceImpl>) observable;
        this.circumstanceChoice = new ChoiceBox();
        this.circumstanceChoice.getItems().addAll("Request", "Performance", "Unstructured");
        this.circumstanceChoice.getSelectionModel().selectFirst(); 
        this.createButton = new Button("Create");
        this.createButton.setOnAction(this::create);
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setOnAction(this::cancel);
        this.circumstanceToolbar = new ToolBar(circumstanceChoice, createButton);
        this.editorPane.setTop(circumstanceToolbar);
        
        if (observable.getValue() != null) {
            this.circumstanceToolbar.getItems().clear();
            this.circumstanceToolbar.getItems().add(this.cancelButton);
        }
    }
    
    private void cancel(ActionEvent event) {
        this.circumstanceToolbar.getItems().clear();
        this.circumstanceToolbar.getItems().add(this.circumstanceChoice);
        this.circumstanceToolbar.getItems().add(this.createButton);
        this.editorPane.setCenter(null);
        setValue(null);
    }
    
    private void create(ActionEvent event) {
        String selectedType = (String) this.circumstanceChoice.getSelectionModel().getSelectedItem();
        this.circumstanceToolbar.getItems().clear();
        this.circumstanceToolbar.getItems().add(this.cancelButton);
        this.circumstanceToolbar.getItems().add(new Label(selectedType));
        switch (selectedType) {
            case "Request":
                RequestCircumstanceImpl requestCircumstance = new RequestCircumstanceImpl(viewProperties.getManifoldCoordinate());
                RequestPropertySheet requestPropertySheet = new RequestPropertySheet(viewProperties);
                requestPropertySheet.setCircumstance(requestCircumstance);
                setValue(requestCircumstance);
                this.editorPane.setCenter(requestPropertySheet.getPropertySheet());
                break;
                
            case "Performance":
                PerformanceCircumstanceImpl performanceCircumstance = new PerformanceCircumstanceImpl(viewProperties.getManifoldCoordinate());
                PerformancePropertySheet performancePropertySheet = new PerformancePropertySheet(viewProperties);
                performancePropertySheet.setCircumstance(performanceCircumstance);
                setValue(performanceCircumstance);
                this.editorPane.setCenter(performancePropertySheet.getPropertySheet());
                break;
                
            case "Unstructured":
                UnstructuredCircumstanceImpl unstructuredCircumstance = new UnstructuredCircumstanceImpl(viewProperties.getManifoldCoordinate());
                UnstructuredPropertySheet unstructuredPropertySheet = new UnstructuredPropertySheet(this.viewProperties);
                unstructuredPropertySheet.setCircumstance(unstructuredCircumstance);
                setValue(unstructuredCircumstance);
                this.editorPane.setCenter(unstructuredPropertySheet.getPropertySheet());
                break;
        }
        
    }
    
    
    
    @Override
    public Node getEditor() {
        return editorPane;
    }

    @Override
    public CircumstanceImpl getValue() {
        return circumstanceProperty.get();
    }

    @Override
    public void setValue(CircumstanceImpl value) {
        circumstanceProperty.set(value);
    }
    
}
