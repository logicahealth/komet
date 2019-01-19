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
package sh.isaac.komet.iconography;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;

/**
 *
 * @author kec
 */
public class GlyphFontsApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        GlyphFont fontAwesome = GlyphFonts.fontAwesome();
        GlyphFont materialDesign = GlyphFonts.materialIcon();

        TilePane iconsPane = new TilePane(3, 3);


      iconsPane.getChildren().add(Iconography2.TAXONOMY_ICON.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_ROOT_ICON.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_DEFINED_MULTIPARENT_CLOSED.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_DEFINED_MULTIPARENT_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_DEFINED_SINGLE_PARENT.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_PRIMITIVE_SINGLE_PARENT.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_CLICK_TO_OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography2.TAXONOMY_CLICK_TO_CLOSE.getIconographic());
      iconsPane.getChildren().add(Iconography2.INFERRED_VIEW.getIconographic());
      iconsPane.getChildren().add(Iconography2.STATED_VIEW.getIconographic());
      iconsPane.getChildren().add(Iconography2.SHORT_TEXT.getIconographic());
      iconsPane.getChildren().add(Iconography2.LONG_TEXT.getIconographic());
      iconsPane.getChildren().add(Iconography2.SET_AND.getIconographic());
      iconsPane.getChildren().add(Iconography2.SET_OR.getIconographic());
      iconsPane.getChildren().add(Iconography2.RUN.getIconographic());
      iconsPane.getChildren().add(Iconography2.LINK.getIconographic());
      iconsPane.getChildren().add(Iconography2.LINK_BROKEN.getIconographic());
      iconsPane.getChildren().add(Iconography2.FLWOR_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography2.SIMPLE_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography2.SETTINGS_GEAR.getIconographic());
      iconsPane.getChildren().add(Iconography2.TREE_ASPECT_RATIO_LAYOUT.getIconographic());
      iconsPane.getChildren().add(Iconography2.CLASSIC_TREE_LAYOUT.getIconographic());
      iconsPane.getChildren().add(Iconography2.HIERARCHICAL_LAYOUT.getIconographic());

      iconsPane.getChildren().add(Iconography2.PIN.getIconographic());
      iconsPane.getChildren().add(Iconography2.PINNED_CLOSE.getIconographic());
      iconsPane.getChildren().add(Iconography2.EDIT_PENCIL.getIconographic());
      iconsPane.getChildren().add(Iconography2.LOOK_EYE.getIconographic());
      iconsPane.getChildren().add(Iconography2.LOOK_EYE2.getIconographic());
      iconsPane.getChildren().add(Iconography2.DRAG_DROP_ICON.getIconographic());

      iconsPane.getChildren().add(Iconography2.MAGNIFY.getIconographic());
      iconsPane.getChildren().add(Iconography2.MAGNIFY_MINUS.getIconographic());
      iconsPane.getChildren().add(Iconography2.MAGNIFY_PLUS.getIconographic());
      iconsPane.getChildren().add(Iconography2.ONE_TO_ONE.getIconographic());
      iconsPane.getChildren().add(Iconography2.ZOOM_TO_FIT.getIconographic());

      iconsPane.getChildren().add(Iconography2.CIRCLE_A.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_B.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_C.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_D.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_E.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_F.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_G.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_H.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_I.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_J.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_K.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_L.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_M.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_N.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_O.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_P.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_Q.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_R.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_S.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_T.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_U.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_V.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_W.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_X.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_Y.getIconographic());
      iconsPane.getChildren().add(Iconography2.CIRCLE_Z.getIconographic());
      

      //iconsPane.getChildren().add(Iconography2.getImage("/sh/isaac/komet/iconography/svg_icons/14x14/icon_caseSensitive.png", 16));
      iconsPane.getChildren().add(Iconography2.CASE_SENSITIVE.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CLOSE.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_EDIT.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_EXPAND.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_EXPORT.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_GO_TO_LINK.getIconographic());
      iconsPane.getChildren().add(Iconography2.CASE_SENSITIVE_NOT.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_RELOAD.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_BATCH_EDIT.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CHECK_MARK.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CLASSIFIER1.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CLASSIFIER2.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CLASSIFIER3.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_CLASSIFIER4.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_LINK_TO_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_LINK_TO_TAXONOMY.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_NOT_LINKED.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_SETTINGS1.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_SETTINGS2.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_SETTINGS3.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_SEARCH.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_TAXONOMY.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_DEFINED_ARROW.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_DEFINED.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_PRIMITIVE.getIconographic());
      iconsPane.getChildren().add(Iconography2.ICON_PRIMITIVE_ARROW.getIconographic());    
      iconsPane.getChildren().add(Iconography2.PAPERCLIP.getIconographic());    
      iconsPane.getChildren().add(Iconography2.SOURCE_BRANCH.getIconographic());    
      iconsPane.getChildren().add(Iconography2.SOURCE_BRANCH_1.getIconographic());    
      iconsPane.getChildren().add(Iconography2.PLUS.getIconographic());    
      iconsPane.getChildren().add(Iconography2.CONCEPT_DETAILS.getIconographic());    
      iconsPane.getChildren().add(Iconography2.CONCEPT_TABLE.getIconographic());    
      iconsPane.getChildren().add(Iconography2.KOMET.getIconographic());    
      iconsPane.getChildren().add(Iconography2.SPINNER.getIconographic());    
      iconsPane.getChildren().add(Iconography2.STOP_CIRCLE.getIconographic());    
      iconsPane.getChildren().add(Iconography2.STOP_STOP.getIconographic());    
      iconsPane.getChildren().add(Iconography2.STOP_SQUARE.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_CONFIRM.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_ERROR.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_INFORM.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_WARN.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_CONFIRM2.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_ERROR2.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_INFORM2.getIconographic());    
      iconsPane.getChildren().add(Iconography2.ALERT_WARN2.getIconographic());    
      iconsPane.getChildren().add(Iconography2.CHECKERED_FLAG.getIconographic());    
      iconsPane.getChildren().add(Iconography2.DASHBOARD.getIconographic());    
      iconsPane.getChildren().add(Iconography2.JAVASCRIPT.getIconographic());  
      iconsPane.getChildren().add(Iconography2.LAMBDA.getIconographic());
      iconsPane.getChildren().add(Iconography2.ROLE_GROUP.getIconographic());
      iconsPane.getChildren().add(Iconography2.INFERRED.getIconographic());
      iconsPane.getChildren().add(Iconography2.STATED.getIconographic());
      iconsPane.getChildren().add(Iconography2.LINK_EXTERNAL.getIconographic());
      iconsPane.getChildren().add(Iconography2.NEW_CONCEPT.getIconographic());
      iconsPane.getChildren().add(Iconography2.CANCEL.getIconographic());
      iconsPane.getChildren().add(Iconography2.DUPLICATE.getIconographic());
      
      iconsPane.getChildren().add(Iconography2.OPEN.getIconographic());
      iconsPane.getChildren().add(Iconography2.CLOSE.getIconographic());
      
      iconsPane.getChildren().add(Iconography2.FEATURE_FUNCTION.getIconographic());
      iconsPane.getChildren().add(Iconography2.FEATURE_INFO.getIconographic());
      iconsPane.getChildren().add(Iconography2.FEATURE_RULER.getIconographic());
      iconsPane.getChildren().add(Iconography2.LITERAL_STRING.getIconographic());
      iconsPane.getChildren().add(Iconography2.LITERAL_NUMERIC.getIconographic());
      iconsPane.getChildren().add(Iconography2.COPY.getIconographic());
      iconsPane.getChildren().add(Iconography2.TARGET.getIconographic());
      iconsPane.getChildren().add(Iconography2.EXCLAMATION.getIconographic());
      iconsPane.getChildren().add(Iconography2.INFORMATION.getIconographic());
      iconsPane.getChildren().add(Iconography2.ADD.getIconographic());
      iconsPane.getChildren().add(Iconography2.ARROW_UP.getIconographic());
      iconsPane.getChildren().add(Iconography2.ARROW_DOWN.getIconographic());
      iconsPane.getChildren().add(Iconography2.DELETE_TRASHCAN.getIconographic());
      
        Scene scene = new Scene(new ScrollPane(iconsPane), 500, 500);
        stage.setScene(scene);
        scene.getStylesheets().add(this.getClass().getResource("/sh/isaac/komet/iconography/Iconography.css").toString());
        stage.setTitle("Isaac Iconography2: " + iconsPane.getChildren().size() + " Glyph Icons");
        stage.show();
    }
   public static void main(String[] args) {
      launch(args);
   }

}
