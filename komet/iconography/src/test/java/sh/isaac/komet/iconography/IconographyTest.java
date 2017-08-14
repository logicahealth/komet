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

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 *
 * @author kec
 */
public class IconographyTest extends Application {

   @Override
   public void start(Stage stage) throws Exception {
      SvgImageLoaderFactory.install();
      
      FlowPane iconsPane = new FlowPane(3, 3);

      FontAwesomeIconView thumbsUpIcon = new FontAwesomeIconView();
      thumbsUpIcon.setStyleClass("thumbs-up-icon");
      iconsPane.getChildren().add(thumbsUpIcon);

      iconsPane.getChildren().add(Iconography.TAXONOMY_ICON.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_ROOT_ICON.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_DEFINED_MULTIPARENT_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_PRIMITIVE_SINGLE_PARENT.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography.TAXONOMY_CLOSED.getIconographic());
      iconsPane.getChildren().add(Iconography.INFERRED_VIEW.getIconographic());
      iconsPane.getChildren().add(Iconography.STATED_VIEW.getIconographic());
      iconsPane.getChildren().add(Iconography.SHORT_TEXT.getIconographic());
      iconsPane.getChildren().add(Iconography.LONG_TEXT.getIconographic());
      iconsPane.getChildren().add(Iconography.SET_AND.getIconographic());
      iconsPane.getChildren().add(Iconography.SET_OR.getIconographic());
      iconsPane.getChildren().add(Iconography.RUN.getIconographic());
      iconsPane.getChildren().add(Iconography.LINK.getIconographic());
      iconsPane.getChildren().add(Iconography.LINK_BROKEN.getIconographic());
      iconsPane.getChildren().add(Iconography.FLOWR_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography.SIMPLE_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography.SETTINGS_GEAR.getIconographic());
      iconsPane.getChildren().add(Iconography.TREE_ASPECT_RATIO_LAYOUT.getIconographic());
      iconsPane.getChildren().add(Iconography.CLASSIC_TREE_LAYOUT.getIconographic());
      iconsPane.getChildren().add(Iconography.HIERARCHICAL_LAYOUT.getIconographic());

      iconsPane.getChildren().add(Iconography.PIN.getIconographic());
      iconsPane.getChildren().add(Iconography.PINNED_CLOSE.getIconographic());
      iconsPane.getChildren().add(Iconography.EDIT_PENCIL.getIconographic());
      iconsPane.getChildren().add(Iconography.LOOK_EYE.getIconographic());
      iconsPane.getChildren().add(Iconography.LOOK_EYE2.getIconographic());
      iconsPane.getChildren().add(Iconography.DRAG_DROP_ICON.getIconographic());

      iconsPane.getChildren().add(Iconography.MAGNIFY.getIconographic());
      iconsPane.getChildren().add(Iconography.MAGNIFY_MINUS.getIconographic());
      iconsPane.getChildren().add(Iconography.MAGNIFY_PLUS.getIconographic());
      iconsPane.getChildren().add(Iconography.ONE_TO_ONE.getIconographic());
      iconsPane.getChildren().add(Iconography.ZOOM_TO_FIT.getIconographic());
      Scene scene = new Scene(new ScrollPane(iconsPane), 500, 500);
      stage.setScene(scene);

      scene.getStylesheets().add(this.getClass().getResource("/sh/isaac/komet/iconography/Iconography.css").toString());
      stage.setTitle("Isaac Iconography: " + iconsPane.getChildren().size() + " Icons");
      stage.show();
   }

   public static void main(String[] args) {
      launch(args);
   }

}
