/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.isaac.komet.iconography;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

/**
 *
 * @author kec
 */
public class SvgIconographic extends Label {

   private static final StyleablePropertyFactory<SvgIconographic> FACTORY
           = new StyleablePropertyFactory<>(Label.getClassCssMetaData());

   private final StyleableProperty<Number> myHeight
           = FACTORY.createStyleableNumberProperty(this, "myHeight", "-my-height", s -> s.myHeight);

   private final StyleableProperty<Number> myWidth
           = FACTORY.createStyleableNumberProperty(this, "myWidth", "-my-width", s -> s.myWidth);

   public SvgIconographic() {
      super();
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      myHeightProperty().addListener((ObservableValue<? extends Double> observable, Double oldValue, Double newValue) -> {
         Node graphic = getGraphic();
         if (graphic != null) {
            ((ImageView) graphic).setFitHeight(newValue);
         }
      });
      myWidthProperty().addListener((ObservableValue<? extends Double> observable, Double oldValue, Double newValue) -> {
         Node graphic = getGraphic();
         if (graphic != null) {
            ((ImageView) graphic).setFitWidth(newValue);
         }
      });
   }

   public final SvgIconographic setStyleClass(String styleClass) {
      getStyleClass().add(styleClass);
      return this;
   }

   public final ObservableValue<Double> myHeightProperty() {
      return (ObservableValue<Double>) myHeight;
   }

   public final Double getMyHeight() {
      return myHeight.getValue().doubleValue();
   }

   public final void setMyHeight(Double height) {
      myHeight.setValue(height);
   }

   public final ObservableValue<Double> myWidthProperty() {
      return (ObservableValue<Double>) myWidth;
   }

   public final Double getMyWidth() {
      return myWidth.getValue().doubleValue();
   }

   public final void setMyWidth(Double width) {
      myWidth.setValue(width);
   }

   public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
      return FACTORY.getCssMetaData();
   }

   @Override
   public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
      return FACTORY.getCssMetaData();
   }

}
