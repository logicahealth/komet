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
package sh.komet.gui.control.measure;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.model.statement.MeasureImpl;

/**
 *
 * @author kec
 */
public class MeasureEditor implements PropertyEditor<MeasureImpl> {
    private MeasureImpl measure;
    
    private final GridPane measureNode = new GridPane();

    private final RangeSlider measureBoundsSlider = new RangeSlider(0, 100, 10, 90);
    private final Slider resolutionSlider = new Slider(0, 1, 0.5);
    private final CheckBox includeUpperBound = new CheckBox("upper");
    private final CheckBox includeLowerBound = new CheckBox("lower");
    
    public MeasureEditor(MeasureImpl measure) {
        this.measure = measure;
        measureBoundsSlider.setShowTickMarks(true);
        measureBoundsSlider.setShowTickLabels(true);
        measureBoundsSlider.setBlockIncrement(5);
        measureBoundsSlider.highValueProperty().bindBidirectional(measure.upperBoundProperty());
        measureBoundsSlider.lowValueProperty().bindBidirectional(measure.lowerBoundProperty());
        resolutionSlider.valueProperty().bindBidirectional(measure.resolutionProperty());
        includeUpperBound.selectedProperty().bindBidirectional(measure.includeUpperBoundProperty());
        includeLowerBound.selectedProperty().bindBidirectional(measure.includeLowerBoundProperty());
        
        measureNode.add(measureBoundsSlider, 0, 0, 2, 1);
        measureNode.add(includeLowerBound, 0, 1, 1, 1);
        measureNode.add(includeUpperBound, 1, 1, 1, 1);
        
        measureNode.add(resolutionSlider, 0, 2, 2, 1);

    }

    public GridPane getMeasureNode() {
        return measureNode;
    }

    @Override
    public Node getEditor() {
        return measureNode;
    }

    @Override
    public MeasureImpl getValue() {
        return measure;
    }

    @Override
    public void setValue(MeasureImpl value) {
        measure = value;
    }
    
    
}
