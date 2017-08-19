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
package sh.komet.gui.control;

import com.sun.javafx.css.converters.SizeConverter;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/* Modified from: 
 *
 * Copyright (c) 2015, 2016 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 * @author kec
 */
public class OnOffToggleSwitchSkin extends SkinBase<OnOffToggleSwitch> {

   private final StackPane thumb;
   private final StackPane thumbArea;
   private final Text onText = new Text(4, 3, "ON");
   private final Text offText = new Text(17, 3, "OFF");
   private final TranslateTransition transition;
   private final FadeTransition fadeOnTransition;
   private final FadeTransition fadeOffTransition;

   /**
    * Constructor for all ToggleSwitchSkin instances.
    *
    * @param control The ToggleSwitch for which this Skin should attach to.
    */
   public OnOffToggleSwitchSkin(OnOffToggleSwitch control) {
      super(control);

      thumb = new StackPane();
      setThumbDimensions(thumb);
      thumbArea = new StackPane();

      setThumbAreaDimensions(thumbArea);
      thumbArea.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
      onText.setFill(Color.WHITE);
      onText.setTextAlignment(TextAlignment.LEFT);
      onText.setTextOrigin(VPos.TOP);
      onText.setFont(Font.font("Open Sans", FontWeight.BOLD, 10));
      onText.setMouseTransparent(true);

      offText.setFill(Color.BLACK);
      offText.setTextAlignment(TextAlignment.LEFT);
      offText.setTextOrigin(VPos.TOP);
      offText.setFont(Font.font("Open Sans", FontWeight.BOLD, 10));
      offText.setMouseTransparent(true);

      transition = new TranslateTransition(Duration.millis(getThumbMoveAnimationTime()), thumb);
      fadeOnTransition = new FadeTransition(Duration.millis(getThumbMoveAnimationTime()), onText);
      fadeOffTransition = new FadeTransition(Duration.millis(getThumbMoveAnimationTime()), offText);

      getChildren().addAll(thumbArea, thumb, onText, offText);

      thumb.getStyleClass().setAll("thumb");
      thumbArea.getStyleClass().setAll("thumb-area");

      thumbArea.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
      thumb.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
      selectedStateChanged();
      control.selectedProperty().addListener((observable, oldValue, newValue) -> {
         if (newValue.booleanValue() != oldValue.booleanValue()) {
            selectedStateChanged();
         }
      });
   }

   private void setThumbDimensions(Pane pane) {
      pane.setMinHeight(18);
      pane.setMaxHeight(18);
      pane.setPrefHeight(18);
      pane.setMinWidth(15);
      pane.setMaxWidth(15);
      pane.setPrefWidth(15);
   }

   private void setThumbAreaDimensions(Pane pane) {
      pane.setMinHeight(20);
      pane.setMaxHeight(20);
      pane.setPrefHeight(20);
      pane.setMinWidth(40);
      pane.setMaxWidth(40);
      pane.setPrefWidth(40);
   }

   private void selectedStateChanged() {
      if (transition != null) {
         transition.stop();
      }

      double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
      double thumbWidth = snapSize(thumb.prefWidth(-1));
      double distance = thumbAreaWidth - thumbWidth;
      /**
       * If we are not selected, we need to go from right to left.
       */
      if (!getSkinnable().isSelected()) {
         thumb.setLayoutX(thumbArea.getLayoutX());
         transition.setFromX(distance);
         transition.setToX(0);
         fadeOnTransition.setToValue(0);
         fadeOffTransition.setToValue(1);
      } else {
         thumb.setTranslateX(thumbArea.getLayoutX());
         transition.setFromX(0);
         transition.setToX(distance);
         fadeOnTransition.setToValue(1);
         fadeOffTransition.setToValue(0);
      }
      transition.setCycleCount(1);
      transition.play();
      fadeOnTransition.play();
      fadeOffTransition.play();
   }

   private void mousePressedOnToggleSwitch(OnOffToggleSwitch toggleSwitch) {
      toggleSwitch.setSelected(!toggleSwitch.isSelected());
   }

   /**
    * How many milliseconds it should take for the thumb to go from one edge to the other
    */
   private DoubleProperty thumbMoveAnimationTime = null;

   private DoubleProperty thumbMoveAnimationTimeProperty() {
      if (thumbMoveAnimationTime == null) {
         thumbMoveAnimationTime = new StyleableDoubleProperty(200) {

            @Override
            public Object getBean() {
               return OnOffToggleSwitchSkin.this;
            }

            @Override
            public String getName() {
               return "thumbMoveAnimationTime";
            }

            @Override
            public CssMetaData<OnOffToggleSwitch, Number> getCssMetaData() {
               return THUMB_MOVE_ANIMATION_TIME;
            }
         };
      }
      return thumbMoveAnimationTime;
   }

   private double getThumbMoveAnimationTime() {
      return thumbMoveAnimationTime == null ? 200 : thumbMoveAnimationTime.get();
   }

   @Override
   protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
      OnOffToggleSwitch toggleSwitch = getSkinnable();
      double thumbWidth = snapSize(thumb.prefWidth(-1));
      double thumbHeight = snapSize(thumb.prefHeight(-1));
      thumb.resize(thumbWidth, thumbHeight);
      //We must reset the TranslateX otherwise the thumb is mis-aligned when window is resized.
      if (transition != null) {
         transition.stop();
      }
      thumb.setTranslateX(0);

      double thumbAreaY = snapPosition(contentY);
      double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
      double thumbAreaHeight = snapSize(thumbArea.prefHeight(-1));

      thumbArea.resize(thumbAreaWidth, thumbAreaHeight);
      thumbArea.setLayoutX(contentWidth - thumbAreaWidth);
      thumbArea.setLayoutY(thumbAreaY);

      if (!toggleSwitch.isSelected()) {
         thumb.setLayoutX(thumbArea.getLayoutX());
      } else {
         thumb.setLayoutX(thumbArea.getLayoutX() + thumbAreaWidth - thumbWidth);
      }
      thumb.setLayoutY(thumbAreaY + (thumbAreaHeight - thumbHeight) / 2);
   }

   @Override
   protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
      return leftInset + thumbArea.prefWidth(-1) + rightInset;
   }

   @Override
   protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
      return topInset + thumb.prefHeight(-1) + bottomInset;
   }

   @Override
   protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
      return leftInset + thumbArea.prefWidth(-1) + rightInset;
   }

   @Override
   protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
      return topInset + thumb.prefHeight(-1) + bottomInset;
   }

   @Override
   protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
      return getSkinnable().prefWidth(height);
   }

   @Override
   protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
      return getSkinnable().prefHeight(width);
   }

   private static final CssMetaData<OnOffToggleSwitch, Number> THUMB_MOVE_ANIMATION_TIME
           = new CssMetaData<OnOffToggleSwitch, Number>("-thumb-move-animation-time",
                   SizeConverter.getInstance(), 200) {

      @Override
      public boolean isSettable(OnOffToggleSwitch toggleSwitch) {
         final OnOffToggleSwitchSkin skin = (OnOffToggleSwitchSkin) toggleSwitch.getSkin();
         return skin.thumbMoveAnimationTime == null
                 || !skin.thumbMoveAnimationTime.isBound();
      }

      @Override
      public StyleableProperty<Number> getStyleableProperty(OnOffToggleSwitch toggleSwitch) {
         final OnOffToggleSwitchSkin skin = (OnOffToggleSwitchSkin) toggleSwitch.getSkin();
         return (StyleableProperty<Number>) (WritableValue<Number>) skin.thumbMoveAnimationTimeProperty();
      }
   };

   private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

   static {
      final List<CssMetaData<? extends Styleable, ?>> styleables
              = new ArrayList<>(SkinBase.getClassCssMetaData());
      styleables.add(THUMB_MOVE_ANIMATION_TIME);
      STYLEABLES = Collections.unmodifiableList(styleables);
   }

   /**
    * @return The CssMetaData associated with this class, which may include the CssMetaData of its super classes.
    */
   public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
      return STYLEABLES;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
      return getClassCssMetaData();
   }
}
