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
package sh.komet.gui.control.titled;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 *
 * @author kec
 */
public class TitledToolbarPane extends AnchorPane {

    Label leftLabel = new Label();
    AnchorPane leftGraphic1 = new AnchorPane();
    AnchorPane rightGraphic = new AnchorPane();
    GridPane titleGrid = new GridPane();
    TitledPane titledPane = new TitledPane();
    final ColumnConstraints column1;
    final ColumnConstraints column2;

    public TitledToolbarPane(String title,
            Node content) {
        this();
        setContent(content);
        setText(title);
    }

    public TitledToolbarPane() {
        super();

        GridPane.setConstraints(leftGraphic1, 0, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
        GridPane.setConstraints(leftLabel, 1, 0, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);

        //leftLabel.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        //rightGraphic.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
        GridPane.setConstraints(rightGraphic, 2, 0, 1, 1, HPos.RIGHT, VPos.CENTER, Priority.NEVER, Priority.NEVER);
        column1 = new ColumnConstraints(0, 0, 0);
        column2 = new ColumnConstraints(30, 250, Double.MAX_VALUE);
        if (!titledPane.isCollapsible()) {
            column2.setMinWidth(30);
            column2.setPrefWidth(275);
            column2.setMaxWidth(Double.MAX_VALUE);
        }

        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints(25, 25, 25);
        titleGrid.getColumnConstraints().addAll(column1, column2, column3); // first column gets any extra width
        titleGrid.getChildren().addAll(leftGraphic1, leftLabel, rightGraphic);
        //titleGrid.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, CornerRadii.EMPTY, Insets.EMPTY)));

        AnchorPane.setTopAnchor(titleGrid, 0.0);
        AnchorPane.setLeftAnchor(titleGrid, 0.0);
        AnchorPane.setBottomAnchor(titleGrid, 0.0);
        AnchorPane.setRightAnchor(titleGrid, 0.0);

        titledPane.setGraphic(new AnchorPane(titleGrid));

        //titledPane.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        titledPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        leftLabel.setWrapText(true);
        AnchorPane.setTopAnchor(titledPane, 0.0);
        AnchorPane.setLeftAnchor(titledPane, 0.0);
        AnchorPane.setBottomAnchor(titledPane, 0.0);
        AnchorPane.setRightAnchor(titledPane, 0.0);
        getChildren().addAll(titledPane);
        titledPane.setText("");
        final ContextMenu contextMenu = new ContextMenu();
        MenuItem customSkin = new MenuItem("Do something");

        customSkin.setOnAction((event) -> {
            //titledPane.setSkin(new TitledToolbarSkin(titledPane));
        });

        contextMenu.getItems().addAll(customSkin);
        titledPane.setContextMenu(contextMenu);

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            titleGrid.setPrefWidth(newValue.doubleValue() - 40);
        });
        
        titledPane.collapsibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                column2.setMinWidth(30);
                column2.setPrefWidth(250);
                column2.setMaxWidth(Double.MAX_VALUE);
            } else {
                column2.setMinWidth(30);
                column2.setPrefWidth(275);
                column2.setMaxWidth(Double.MAX_VALUE);
            }
        });
    }

    public final void setText(String value) {
        leftLabel.setText(value);
    }

    public final StringProperty textProperty() {
        return leftLabel.textProperty();
    }

    public final String getText() {
        return leftLabel.getText();
    }
    public final void setLeftGraphic1(Node value, double width) {
        if (value != null) {
            column1.setMinWidth(width);
            column1.setPrefWidth(width);
            column1.setMaxWidth(width);
        } else {
            column1.setMinWidth(0);
            column1.setPrefWidth(0);
            column1.setMaxWidth(0);
        }
        AnchorPane.setTopAnchor(value, 0.0);
        AnchorPane.setLeftAnchor(value, 0.0);
        AnchorPane.setBottomAnchor(value, 0.0);
        AnchorPane.setRightAnchor(value, 0.0);
        leftGraphic1.getChildren().setAll(value);
    }

    public final void setLeftGraphic1(Node value) {
        setLeftGraphic1(value, 25);
    }

    public final void setLeftGraphic2(Node value) {
        leftLabel.setGraphic(value);
    }

    public final void setRightGraphic(Node value) {
        AnchorPane.setTopAnchor(value, 0.0);
        AnchorPane.setLeftAnchor(value, 0.0);
        AnchorPane.setBottomAnchor(value, 0.0);
        AnchorPane.setRightAnchor(value, 0.0);
        rightGraphic.getChildren().setAll(value);
    }

    public final void setContent(Node value) {
        titledPane.setContent(value);
    }

    public final void setExpanded(boolean value) {
        titledPane.setExpanded(value);
    }

    public final boolean isExpanded() {
        return titledPane.isExpanded();
    }

    public final BooleanProperty expandedProperty() {
        return titledPane.expandedProperty();
    }

    public final void setCollapsible(boolean value) {
        titledPane.setCollapsible(value);
    }

}
