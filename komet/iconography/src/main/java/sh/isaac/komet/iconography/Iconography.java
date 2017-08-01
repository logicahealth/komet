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
   TAXONOMY_CLOSED(FONT_AWSOME, "taxonomy-closed-icon"),
   TAXONOMY_OPEN(FONT_AWSOME, "taxonomy-open-icon"),
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
   FL0WR_SEARCH(MATERIAL_DESIGNS_WEBFONT, "flowr-search"),
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
   
   public static String getStyleSheetStringUrl() {
      return Iconography.class.getResource("/sh/isaac/komet/iconography/Iconography.css").toString();
   }
}
