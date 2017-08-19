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

import de.jensd.fx.glyphs.emojione.EmojiOneView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.icons525.Icons525View;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import static sh.isaac.komet.iconography.Iconography.IconSource.EMOJI_ONE;
import static sh.isaac.komet.iconography.Iconography.IconSource.FONT_AWSOME;
import static sh.isaac.komet.iconography.Iconography.IconSource.ICONS_525;
import static sh.isaac.komet.iconography.Iconography.IconSource.MATERIAL_DESIGNS_ICON;
import static sh.isaac.komet.iconography.Iconography.IconSource.MATERIAL_DESIGNS_WEBFONT;
import static sh.isaac.komet.iconography.Iconography.IconSource.OCT_ICON;
import static sh.isaac.komet.iconography.Iconography.IconSource.SVG;

/**
 *
 * @author kec
 */
public enum Iconography {
   
   
//TODO make Iconagraphy a service/provider 
   TAXONOMY_ICON(MATERIAL_DESIGNS_WEBFONT, "taxonomy-icon"),
   TAXONOMY_ROOT_ICON(MATERIAL_DESIGNS_WEBFONT, "taxonomy-root-icon"),
   TAXONOMY_DEFINED_MULTIPARENT_OPEN(MATERIAL_DESIGNS_WEBFONT, "taxonomy-defined-multiparent-open-icon"),
   TAXONOMY_DEFINED_MULTIPARENT_CLOSED(MATERIAL_DESIGNS_WEBFONT, "taxonomy-defined-multiparent-closed-icon"),
   TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN(MATERIAL_DESIGNS_WEBFONT, "taxonomy-primitive-multiparent-open-icon"),
   TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED(MATERIAL_DESIGNS_WEBFONT, "taxonomy-primitive-multiparent-closed-icon"),
   TAXONOMY_PRIMITIVE_SINGLE_PARENT(MATERIAL_DESIGNS_WEBFONT, "taxonomy-primitive-singleparent-icon"),
   TAXONOMY_DEFINED_SINGLE_PARENT(MATERIAL_DESIGNS_WEBFONT, "taxonomy-defined-singleparent-icon"),
   TAXONOMY_CLICK_TO_CLOSE(FONT_AWSOME, "taxonomy-closed-icon"),
   TAXONOMY_CLICK_TO_OPEN(FONT_AWSOME, "taxonomy-open-icon"),
   STATED_VIEW(SVG, "stated-view"),
   INFERRED_VIEW(SVG, "inferred-view"),
   SHORT_TEXT(MATERIAL_DESIGNS_ICON, "short-text"),
   LONG_TEXT(MATERIAL_DESIGNS_ICON, "long-text"),
   VANITY_BOX(SVG, "vanity-box"),
   SET_AND(SVG, "set-and"),
   SET_OR(SVG, "set-or"),
   RUN(MATERIAL_DESIGNS_WEBFONT, "run"),
   LINK(MATERIAL_DESIGNS_WEBFONT, "link"),
   LINK_BROKEN(MATERIAL_DESIGNS_WEBFONT, "link-broken"),
   FLOWR_SEARCH(MATERIAL_DESIGNS_WEBFONT, "flowr-search"),
   SIMPLE_SEARCH(MATERIAL_DESIGNS_WEBFONT, "simple-search"),
   SETTINGS_GEAR(EMOJI_ONE, "settings-gear"),
   
  TREE_ASPECT_RATIO_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "tree-aspect-ratio-layout"),
  CLASSIC_TREE_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "classic-tree-layout"),
  HIERARCHICAL_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "hierarchical-layout"),
  
  PIN(OCT_ICON, "pin-slider-open"),
  PINNED_CLOSE(MATERIAL_DESIGNS_ICON, "close_slider"),

  
  
  EDIT_PENCIL(MATERIAL_DESIGNS_WEBFONT, "edit-pencil"),
  LOOK_EYE(ICONS_525, "look-eye"),
  LOOK_EYE2(FONT_AWSOME, "look-eye-2"),
  
  DRAG_DROP_ICON(FONT_AWSOME, "drag-drop"),
  
  MAGNIFY(MATERIAL_DESIGNS_WEBFONT, "magnify"),
  MAGNIFY_PLUS(MATERIAL_DESIGNS_WEBFONT, "magnify-plus"),
  MAGNIFY_MINUS(MATERIAL_DESIGNS_WEBFONT, "magnify-minus"),
  ONE_TO_ONE(MATERIAL_DESIGNS_ICON, "one-to-one"),
  ZOOM_TO_FIT(MATERIAL_DESIGNS_ICON, "zoom-to-fit"),
  
  CIRCLE_A(EMOJI_ONE, "circle-a"),
  CIRCLE_B(EMOJI_ONE, "circle-b"),
  CIRCLE_C(EMOJI_ONE, "circle-c"),
  CIRCLE_D(EMOJI_ONE, "circle-d"),
  CIRCLE_E(EMOJI_ONE, "circle-e"),
  CIRCLE_F(EMOJI_ONE, "circle-f"),
  CIRCLE_G(EMOJI_ONE, "circle-g"),
  CIRCLE_H(EMOJI_ONE, "circle-h"),
  CIRCLE_I(EMOJI_ONE, "circle-i"),
  CIRCLE_J(EMOJI_ONE, "circle-j"),
  CIRCLE_K(EMOJI_ONE, "circle-k"),
  CIRCLE_L(EMOJI_ONE, "circle-l"),
  CIRCLE_M(EMOJI_ONE, "circle-m"),
  CIRCLE_N(EMOJI_ONE, "circle-n"),
  CIRCLE_O(EMOJI_ONE, "circle-o"),
  CIRCLE_P(EMOJI_ONE, "circle-p"),
  CIRCLE_Q(EMOJI_ONE, "circle-q"),
  CIRCLE_R(EMOJI_ONE, "circle-r"),
  CIRCLE_S(EMOJI_ONE, "circle-s"),
  CIRCLE_T(EMOJI_ONE, "circle-t"),
  CIRCLE_U(EMOJI_ONE, "circle-u"),
  CIRCLE_V(EMOJI_ONE, "circle-v"),
  CIRCLE_W(EMOJI_ONE, "circle-w"),
  CIRCLE_X(EMOJI_ONE, "circle-x"),
  CIRCLE_Y(EMOJI_ONE, "circle-y"),
  CIRCLE_Z(EMOJI_ONE, "circle-z"),
  
  CASE_SENSITIVE(SVG, "case-sensitive"),
  ICON_CLOSE(SVG, "icon-close"),
  ICON_EDIT(SVG, "icon-edit"),
  ICON_EXPAND(SVG, "icon-expand"),
  ICON_EXPORT(SVG, "icon-export"),
  ICON_GO_TO_LINK(SVG, "icon-goto-link"),
  CASE_SENSITIVE_NOT(SVG, "not-case-sensitive"),
  ICON_RELOAD(SVG, "icon-reload"),
  ICON_BATCH_EDIT(SVG, "icon-batch-edit"),
  ICON_CHECK_MARK(SVG, "icon-check-mark"),
  ICON_CLASSIFIER1(SVG, "icon-classifier-1"),
  ICON_CLASSIFIER2(SVG, "icon-classifier-2"),
  ICON_CLASSIFIER3(SVG, "icon-classifier-3"),
  ICON_CLASSIFIER4(SVG, "icon-classifier-4"),
  ICON_LINK_TO_SEARCH(SVG, "icon-link-to-search"),
  ICON_LINK_TO_TAXONOMY(SVG, "icon-link-to-taxonomy"),
  ICON_NOT_LINKED(SVG, "icon-not-linked"),
  ICON_SETTINGS1(SVG, "icon-settings-1"),
  ICON_SETTINGS2(SVG, "icon-settings-2"),
  ICON_SETTINGS3(SVG, "icon-settings-3"),
  ICON_SEARCH(SVG, "icon-search"),
  ICON_TAXONOMY(SVG, "icon-taxonomy"),
  ICON_DEFINED_ARROW(SVG, "icon-defined-arrow"),
  ICON_DEFINED(SVG, "icon-defined"),
  ICON_PRIMITIVE(SVG, "icon-primitive"),
  ICON_PRIMITIVE_ARROW(SVG, "icon-primitive-arrow"),
  
   ;

   String cssClass;
   IconSource source;

   private Iconography(IconSource source, String cssClass) {
      this.source = source;
      this.cssClass = cssClass;
   }

   public Node getIconographic() {
      switch (source) {
         case MATERIAL_DESIGNS_WEBFONT:
            return new MaterialDesignIconView().setStyleClass(cssClass);
         case FONT_AWSOME:
            return new FontAwesomeIconView().setStyleClass(cssClass);
         case SVG:
            return new SvgIconographic().setStyleClass(cssClass);
         case MATERIAL_DESIGNS_ICON:
            return new MaterialIconView().setStyleClass(cssClass);
         case EMOJI_ONE:
            return new EmojiOneView().setStyleClass(cssClass);
         case ICONS_525:
            return new Icons525View().setStyleClass(cssClass);
         case OCT_ICON:
            return new OctIconView().setStyleClass(cssClass);
         default:
            throw new UnsupportedOperationException("Can't handle: " + source);
      }
   }


   enum IconSource {
      MATERIAL_DESIGNS_WEBFONT, MATERIAL_DESIGNS_ICON, FONT_AWSOME, SVG, EMOJI_ONE, 
      ICONS_525, OCT_ICON,
   };
   public static ImageView getImage(String resourceLocation, int size) {
      ImageView imageView = new ImageView(Iconography.class.getResource(resourceLocation).toString());
      imageView.setPreserveRatio(true);
      imageView.setFitHeight(size);
      return imageView;
   }
   
   public static ImageView getImage(String resourceLocation) {
      return new ImageView(Iconography.class.getResource(resourceLocation).toString());
   }
   public static String getStyleSheetStringUrl() {
      return Iconography.class.getResource("/sh/isaac/komet/iconography/Iconography.css").toString();
   }
   
   static boolean  fontsLoaded = LoadFonts.load();
}
