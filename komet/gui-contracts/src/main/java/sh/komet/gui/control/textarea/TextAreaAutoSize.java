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

import javafx.beans.InvalidationListener;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 *
 * @author kec
 */
public class TextAreaAutoSize extends TextArea {
    
 public static final double DEFAULT_HEIGHT = 18;

    public TextAreaAutoSize() {
        setSkin(new TextAreaAutoSizeSkin(this));
    }

    public TextAreaAutoSize(String text) {
        super(text);
        setSkin(new TextAreaAutoSizeSkin(this));
    }
 
 @Override
  protected void layoutChildren() {
    super.layoutChildren();
    setWrapText(true);
    addListenerToTextHeight();
  }

  private void addListenerToTextHeight() {
    ScrollPane scrollPane = (ScrollPane) lookup(".scroll-pane");
    scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);

    StackPane viewport = (StackPane) scrollPane.lookup(".viewport");

    Region content = (Region) viewport.lookup(".content");

    Text text = (Text) content.lookup(".text");
    setHeight(text);
    text.textProperty().addListener(textHeightListener(text));
  }

  private InvalidationListener textHeightListener(Text text) {
    return (property) -> {
      setHeight(text);
    };
  }    

    private void setHeight(Text text) {
        // + 1 for little margin
        double textHeight = text.getBoundsInLocal().getHeight() + 1;
        
        //To prevent the TextArea from being smaller than the TextField
        //I used DEFAULT_HEIGHT = 18.0
        if (textHeight < DEFAULT_HEIGHT) {
            textHeight = DEFAULT_HEIGHT;
        }
        
        setMinHeight(textHeight);
        setPrefHeight(textHeight);
        setMaxHeight(textHeight);
    }
}
