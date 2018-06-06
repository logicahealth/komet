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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.property.editor.PropertyEditor;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.statement.MeasureImpl;
import sh.komet.gui.action.ConceptAction;
import sh.komet.gui.control.concept.ConceptLabel;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.style.StyleClasses;
import tornadofx.control.DateTimePicker;

/**
 *
 * @author kec
 */
public class MeasureEditor implements PropertyEditor<MeasureImpl> {

    private MeasureImpl measure;

    private final GridPane measureNode = new GridPane();

    TitledPane measureEditorPane = new TitledPane("[a,b] ± d ISO 8601", measureNode);

    private final RangeSlider measureBoundsSlider = new RangeSlider(0, 100, 10, 90);
    private final DateTimePicker lowerBoundDateTimePicker = new DateTimePicker();
    private final DateTimePicker upperBoundDateTimePicker = new DateTimePicker();
    private final Slider resolutionSlider = new Slider(0, 1, 0.5);
    private final UpperBoundToggle includeUpperBound = new UpperBoundToggle();
    private final LowerBoundToggle includeLowerBound = new LowerBoundToggle();
    private final ConceptLabel measureSemantic;
    private final Manifold manifold;

    public MeasureEditor(Manifold manifold) {
        this.manifold = manifold;
        this.measureEditorPane.getStyleClass().add(StyleClasses.MEASURE.toString());
        this.measureSemantic = new ConceptLabel(manifold,
                ConceptLabel::setPreferredText, (label) -> {
                    ConceptAction iso8601 = new ConceptAction(TermAux.ISO_8601, this::handleMeasureSemanticChange);
                    ConceptAction iso8601After = new ConceptAction(TermAux.ISO_8601_AFTER, this::handleMeasureSemanticChange);
                    ConceptAction iso8601Prior = new ConceptAction(TermAux.ISO_8601_PRIOR, this::handleMeasureSemanticChange);
                    List<MenuItem> menuItems = new ArrayList<>();
                    menuItems.add(ActionUtils.createMenuItem(iso8601));
                    menuItems.add(ActionUtils.createMenuItem(iso8601After));
                    menuItems.add(ActionUtils.createMenuItem(iso8601Prior));
                    return menuItems;
                });
        this.measureSemantic.setValue(TermAux.ISO_8601);
        this.measureBoundsSlider.setShowTickMarks(true);
        this.measureBoundsSlider.setShowTickLabels(true);
        this.measureBoundsSlider.setBlockIncrement(5);
        this.resolutionSlider.valueProperty().addListener(this::convertResolutionSliderToDuration);

        this.includeUpperBound.setGraphic(Iconography.ALERT_ERROR.getIconographic());

        addToGrid(includeLowerBound, 0, // columnIndex
                0, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.TOP,
                Priority.NEVER, // hgrow
                Priority.SOMETIMES, // vgrow
                new Insets(1));
        addToGrid(measureBoundsSlider, 1, // columnIndex
                0, // rowIndex
                2, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.ALWAYS, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));

        addToGrid(lowerBoundDateTimePicker, 1, // columnIndex
                0, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.SOMETIMES, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));
        lowerBoundDateTimePicker.setVisible(false);
        addToGrid(upperBoundDateTimePicker, 2, // columnIndex
                0, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.SOMETIMES, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));
        upperBoundDateTimePicker.setVisible(false);

        addToGrid(includeUpperBound, 3, // columnIndex
                0, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.TOP,
                Priority.NEVER, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));
        Label plusMinusLabel = new Label("  ±");
        plusMinusLabel.getStyleClass().add(StyleClasses.INTERVAL_BOUND.toString());
        addToGrid(plusMinusLabel, 0, // columnIndex
                1, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.NEVER, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));
        addToGrid(resolutionSlider, 1, // columnIndex
                1, // rowIndex
                2, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.SOMETIMES, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));

        addToGrid(measureSemantic, 4, // columnIndex
                1, // rowIndex
                1, // columnspan
                1, // rowspan
                HPos.LEFT,
                VPos.CENTER,
                Priority.SOMETIMES, // hgrow
                Priority.NEVER, // vgrow
                new Insets(1));

        this.measureBoundsSlider.highValueProperty().addListener(this::highValueChanged);
        this.measureBoundsSlider.lowValueProperty().addListener(this::lowValueChanged);
        this.resolutionSlider.valueProperty().addListener(this::resolutionValueChanged);
        this.upperBoundDateTimePicker.dateTimeValueProperty().addListener((observable, oldValue, newValue) -> {
            if (this.measure != null) {
                this.measure.setUpperBound(newValue.toInstant(ZoneOffset.UTC).toEpochMilli());
            }
        });
        this.lowerBoundDateTimePicker.dateTimeValueProperty().addListener((observable, oldValue, newValue) -> {
            if (this.measure != null) {
                this.measure.setLowerBound(newValue.toInstant(ZoneOffset.UTC).toEpochMilli());
            }
        });
    }

    private void resolutionValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        adjustRange();
        adjustTicks();
    }

    private void highValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (this.measure != null) {
            if (measureBoundsSlider.isHighValueChanging()) {
                if (measureBoundsSlider.getMax() <= newValue.doubleValue()) {
                    measureBoundsSlider.setMax(measureBoundsSlider.getMax() + this.measure.getResolution().get());
                }
                adjustTicks();
            }
        }
    }

    private void lowValueChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (this.measure != null) {
            if (measureBoundsSlider.isLowValueChanging()) {
                if (measureBoundsSlider.getMin() > 0 && measureBoundsSlider.getMin() >= newValue.doubleValue()) {
                    double newMin = measureBoundsSlider.getMin() - this.measure.getResolution().get();
                    if (newMin < 0) {
                        newMin = 0;
                    }
                    measureBoundsSlider.setMin(newMin);
                    adjustTicks();
                }
            }
        }
    }

    private void convertResolutionSliderToDuration(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
        long resolution = (long) resolutionSlider.getValue();
        if (resolution == 0) {
            resolution = 1;
        }
        if (resolution < 60) {
            if (resolution == 0) {
                resolution = 1;
            }
            resolution = resolution * DateTimeUtil.MS_IN_SEC;
        } else if (resolution < 120) {
            resolution = resolution - 60;
            if (resolution == 0) {
                resolution = 1;
            }
            resolution = resolution * DateTimeUtil.MS_IN_MINUTE;
        } else if (resolution < 180) {
            resolution = resolution - 120;
            if (resolution == 0) {
                resolution = 1;
            } else {
                resolution = resolution * 24 / 60;
                if (resolution == 0) {
                    resolution = 1;
                }
            }
            resolution = resolution * DateTimeUtil.MS_IN_HOUR;
        } else if (resolution < 240) {
            resolution = resolution - 180;
            if (resolution == 0) {
                resolution = 1;
            } else {
                resolution = resolution * 30 / 60;
                if (resolution == 0) {
                    resolution = 1;
                }
            }
            resolution = resolution * DateTimeUtil.MS_IN_DAY;
        } else if (resolution < 300) {
            resolution = resolution - 240;
            if (resolution == 0) {
                resolution = 1;
            } else {
                resolution = resolution * 12 / 60;
                if (resolution == 0) {
                    resolution = 1;
                }
            }
            resolution = resolution * DateTimeUtil.MS_IN_MONTH;
        } else {
            resolution = resolution - 300;
            if (resolution == 0) {
                resolution = 1;
            }
            resolution = resolution * DateTimeUtil.MS_IN_YEAR;
        }

        if (this.measure != null) {
            measure.resolutionProperty().set(resolution);
        }
    }

    private void adjustRange() {
        if (this.measure != null) {
            double range = adjustTicks();

            double margin = range / 4;
            measureBoundsSlider.setMin(measure.getLowerBound() - margin);
            measureBoundsSlider.setMax(measure.getUpperBound() + margin);
        }

    }

    private double adjustTicks() {
        if (this.measure != null) {
            double range = measure.getUpperBound() - measure.getLowerBound();
            if (range == 0) {
                if (measure.getUpperBound() == 0) {
                    range = 2;
                } else {
                    range = measure.getUpperBound() / 4;
                }
            }
            if (measure.getMeasureSemantic().getNid() == TermAux.ISO_8601.getNid()
                    || measure.getMeasureSemantic().getNid() == TermAux.ISO_8601_AFTER.getNid()
                    || measure.getMeasureSemantic().getNid() == TermAux.ISO_8601_PRIOR.getNid()) {
                measureBoundsSlider.setShowTickMarks(false);
                measureBoundsSlider.setShowTickLabels(false);
            } else {
                measureBoundsSlider.setBlockIncrement(range / 100);
                measureBoundsSlider.setMajorTickUnit(range / 10);
                measureBoundsSlider.setMinorTickCount(0);
            }
            return range;
        }
        return Double.NaN;
    }

    private void changePrecisionSlider(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
        if (measure.getMeasureSemantic() != null) {

            if (measure.getMeasureSemantic().getNid() == TermAux.ISO_8601.getNid()
                    || measure.getMeasureSemantic().getNid() == TermAux.ISO_8601_AFTER.getNid()
                    || measure.getMeasureSemantic().getNid() == TermAux.ISO_8601_PRIOR.getNid()) {
                resolutionSlider.setMin(0);
                resolutionSlider.setMax(314);
                resolutionSlider.setSnapToTicks(false);
                resolutionSlider.setMajorTickUnit(60);
                resolutionSlider.setMinorTickCount(7);
                resolutionSlider.setShowTickMarks(true);
                resolutionSlider.setShowTickLabels(true);
                resolutionSlider.setLabelFormatter(new StringConverter<Double>() {
                    @Override
                    public String toString(Double object) {
                        if (object < 60) {
                            return "Sec";
                        }
                        if (object < 120) {
                            return "Min";
                        }
                        if (object < 180) {
                            return "Hour";
                        }
                        if (object < 240) {
                            return "Day";
                        }
                        if (object < 300) {
                            return "Month";
                        }
                        if (object == 300) {
                            return "Year";
                        }
                        return "";
                    }

                    @Override
                    public Double fromString(String string) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                });
            }
        }
    }

    private void handleMeasureSemanticChange(ActionEvent event) {
        Object conceptSpec = ((MenuItem) event.getSource()).getProperties().get(ConceptAction.CONCEPT_PROPERTY);
        setMeasureSemantic((ConceptSpecification) conceptSpec);
    }

    private void setMeasureSemantic(ConceptSpecification conceptSpecification) {
        measureSemantic.setValue(conceptSpecification);
        if (measure.getMeasureSemantic().getNid() == TermAux.ISO_8601.getNid()) {
            lowerBoundDateTimePicker.setVisible(true);
            lowerBoundDateTimePicker.setDateTimeValue(LocalDateTime.now());
            upperBoundDateTimePicker.setVisible(true);
            upperBoundDateTimePicker.setDateTimeValue(LocalDateTime.now().plusMinutes(45));
            measureBoundsSlider.setVisible(false);
            resolutionSlider.setValue(60);

        } else {
            lowerBoundDateTimePicker.setVisible(false);
            upperBoundDateTimePicker.setVisible(false);
            measureBoundsSlider.setVisible(true);
        }
    }

    private void addToGrid(Node child, int columnIndex, int rowIndex,
            int columnspan,
            int rowspan,
            HPos halignment,
            VPos valignment,
            Priority hgrow,
            Priority vgrow,
            Insets margin) {
        GridPane.setConstraints(child,
                columnIndex, rowIndex,
                columnspan, rowspan,
                halignment,
                valignment,
                hgrow,
                vgrow,
                margin);
        measureNode.getChildren().add(child);
    }

    public GridPane getMeasureNode() {
        return measureNode;
    }

    @Override
    public Node getEditor() {
        return measureEditorPane;
    }

    @Override
    public MeasureImpl getValue() {
        return measure;
    }

    @Override
    public void setValue(MeasureImpl measure) {
        if (measure == null) {
            this.measureEditorPane.textProperty().unbind();
            if (this.measure != null) {
                this.measureBoundsSlider.highValueProperty().unbindBidirectional(this.measure.upperBoundProperty());
                this.measureBoundsSlider.lowValueProperty().unbindBidirectional(this.measure.lowerBoundProperty());
                this.includeUpperBound.selectedProperty().unbindBidirectional(this.measure.includeUpperBoundProperty());
                this.includeLowerBound.selectedProperty().unbindBidirectional(this.measure.includeLowerBoundProperty());
            }

        } else {
            measure.measureSemanticProperty().bind(measureSemantic.conceptInLabelProperty());
            measure.measureSemanticProperty().addListener(this::changePrecisionSlider);
            this.measureEditorPane.textProperty().bind(measure.narrativeProperty());
            this.measureBoundsSlider.highValueProperty().bindBidirectional(measure.upperBoundProperty());
            this.measureBoundsSlider.lowValueProperty().bindBidirectional(measure.lowerBoundProperty());
            this.includeUpperBound.selectedProperty().bindBidirectional(measure.includeUpperBoundProperty());
            this.includeLowerBound.selectedProperty().bindBidirectional(measure.includeLowerBoundProperty());
        }
        this.measure = measure;
        adjustRange();
    }

}
