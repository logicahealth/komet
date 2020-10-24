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
package sh.komet.gui.control.text;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 *
 * @author kec
 */
public class TextAreaReadOnly extends TextArea {

    TextAreaSkinNoScroller textAreaSkinNoScroller;

    public TextAreaReadOnly() {
        setSkin(new TextAreaSkinNoScroller(this));
        this.textAreaSkinNoScroller = (TextAreaSkinNoScroller) this.getSkin();
        this.textAreaSkinNoScroller.getContentView().heightProperty().addListener((observable, oldValue, newValue) -> {
            setTheHeight(newValue.doubleValue());
        });
        setEditable(false);
        setWrapText(true);
        setPrefRowCount(4);

        this.textProperty().addListener((observable, oldValue, newValue) -> {
            double height = textAreaSkinNoScroller.computePrefHeight(getWidth());
            setTheHeight(height);
        });

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            double height = textAreaSkinNoScroller.computePrefHeight(newValue.doubleValue());
            setTheHeight(height);
        });

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            selectRange(0,0);
        });
    }

    private void setTheHeight(Double height) {
        setPrefHeight(height);
        setMaxHeight(height);
        setHeight(height);
    }


}
