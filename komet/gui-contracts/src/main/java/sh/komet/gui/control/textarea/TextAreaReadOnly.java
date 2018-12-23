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
package sh.komet.gui.control.textarea;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextBoundsType;

/**
 *
 * @author kec
 */
public class TextAreaReadOnly extends TextArea {

    public TextAreaReadOnly() {
        setEditable(false);
        setWrapText(true);
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            selectRange(0,0);
        });
    }

    public double computeTextHeight(double wrappingWidth) {
        TextAreaReadOnly forLayout = new TextAreaReadOnly();
        forLayout.setText(this.getText());
        forLayout.setFont(this.getFont());
        forLayout.setWrapText(true);
        forLayout.setMinWidth(wrappingWidth);
        forLayout.setPrefWidth(wrappingWidth);
        forLayout.setMaxWidth(wrappingWidth);
        forLayout.getStyleClass().addAll(this.getStyleClass());
        HBox.setHgrow(forLayout, Priority.NEVER);

        HBox hbox = new HBox();
        hbox.setFillHeight(false);
        hbox.setAlignment(Pos.BASELINE_LEFT);
        Scene snapshotScene = new Scene(hbox, 1000, 1000);

        hbox.getChildren().addAll(forLayout);
        hbox.applyCss();
        hbox.layout();

        forLayout.getLayoutBounds();
        return forLayout.getHeight() + 10;
    }


}
