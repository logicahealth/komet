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

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;

import static sh.isaac.komet.iconography.IconSource.*;

/**
 *
 * @author kec
 */
public enum Iconography2 {

    // TODO Consider replacement with: Ikonli
    // https://dlsc.com/2020/03/11/javafx-tip-32-need-icons-use-ikonli/
    // https://github.com/kordamp/ikonli

    //TODO make Iconagraphy a service/provider
    TAXONOMY_ICON(MATERIAL_DESIGNS_WEBFONT, "FILE_TREE", "taxonomy-icon"),
    TAXONOMY_ROOT_ICON(MATERIAL_DESIGNS_WEBFONT, "HEXAGON_OUTLINE", "taxonomy-root-icon"),
    TAXONOMY_DEFINED_MULTIPARENT_OPEN(MATERIAL_DESIGNS_WEBFONT, "ARROW_UP_BOLD_CIRCLE_OUTLINE","taxonomy-defined-multiparent-open-icon"),
    TAXONOMY_DEFINED_MULTIPARENT_CLOSED(MATERIAL_DESIGNS_WEBFONT, "ARROW_UP_BOLD_CIRCLE_OUTLINE", "taxonomy-defined-multiparent-closed-icon"),
    TAXONOMY_PRIMITIVE_MULTIPARENT_OPEN(MATERIAL_DESIGNS_WEBFONT, "ARROW_UP_BOLD_HEXAGON_OUTLINE", "taxonomy-primitive-multiparent-open-icon"),
    TAXONOMY_PRIMITIVE_MULTIPARENT_CLOSED(MATERIAL_DESIGNS_WEBFONT, "ARROW_UP_BOLD_HEXAGON_OUTLINE", "taxonomy-primitive-multiparent-closed-icon"),
    TAXONOMY_PRIMITIVE_SINGLE_PARENT(MATERIAL_DESIGNS_WEBFONT, "HEXAGON", "taxonomy-primitive-singleparent-icon"),
    TAXONOMY_DEFINED_SINGLE_PARENT(MATERIAL_DESIGNS_WEBFONT, "CHECKBOX_BLANK_CIRCLE_OUTLINE", "taxonomy-defined-singleparent-icon"),
    TAXONOMY_CLICK_TO_CLOSE(FONT_AWSOME, "CARET_DOWN", "taxonomy-closed-icon"),
    TAXONOMY_CLICK_TO_OPEN(FONT_AWSOME, "CARET_RIGHT", "taxonomy-open-icon"),
    STATED_VIEW(SVG, "", "stated-view"),
    INFERRED_VIEW(SVG, "", "inferred-view"),
    SHORT_TEXT(MATERIAL_ICON, "SHORT_TEXT", "short-text"),
    LONG_TEXT(MATERIAL_ICON, "WRAP_TEXT", "long-text"),
    VANITY_BOX(SVG, "", "vanity-box"),
    SET_AND(SVG, "", "set-and"),
    SET_OR(SVG, "", "set-or"),
    RUN(MATERIAL_DESIGNS_WEBFONT, "RUN", "run"),
    LINK(MATERIAL_DESIGNS_WEBFONT, "LINK_VARIANT", "link"),
    LINK_BROKEN(MATERIAL_DESIGNS_WEBFONT, "LINK_VARIANT_OFF", "link-broken"),
    FLWOR_SEARCH(MATERIAL_DESIGNS_WEBFONT, "FLOWER", "flowr-search"),
    SEARCH_MINUS(FONT_AWSOME, "SEARCH_MINUS", "search-minus"),
    SEARCH_FILTER(FONT_AWSOME, "FILTER", "search-filter"),
    SIMPLE_SEARCH(MATERIAL_DESIGNS_WEBFONT, "MAGNIFY", "simple-search"),
    TARGET(MATERIAL_DESIGNS_WEBFONT, "TARGET", "target"),
    EXCLAMATION(MATERIAL_DESIGNS_WEBFONT, "ALERT_CIRCLE_OUTLINE", "alert-circle-outline"),
    INFORMATION(MATERIAL_DESIGNS_WEBFONT, "INFORMATION_OUTLINE", "information-outline"),
    COPY(MATERIAL_DESIGNS_WEBFONT, "CONTENT_COPY", "content-copy"),
    SETTINGS_GEAR(EMOJI_ONE, "GEAR", "settings-gear"),

    TREE_ASPECT_RATIO_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "ARRANGE_SEND_TO_BACK", "tree-aspect-ratio-layout"),
    CLASSIC_TREE_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "FILE_TREE", "classic-tree-layout"),
    HIERARCHICAL_LAYOUT(MATERIAL_DESIGNS_WEBFONT, "CROWN", "hierarchical-layout"),

    PIN(OCT_ICON, "PIN", "pin-slider-open"),
    PINNED_CLOSE(MATERIAL_ICON, "HIGHLIGHT_OFF", "close_slider"),



    EDIT_PENCIL(MATERIAL_DESIGNS_WEBFONT, "LEAD_PENCIL", "edit-pencil"),
    LOOK_EYE(ICONS_525, "EYE", "look-eye"),
    LOOK_EYE2(FONT_AWSOME, "EYE", "look-eye-2"),

    DRAG_DROP_ICON(FONT_AWSOME, "ARROWS", "drag-drop"),

    MAGNIFY(MATERIAL_DESIGNS_WEBFONT, "MAGNIFY", "magnify"),
    MAGNIFY_PLUS(MATERIAL_DESIGNS_WEBFONT, "MAGNIFY_PLUS", "magnify-plus"),
    MAGNIFY_MINUS(MATERIAL_DESIGNS_WEBFONT, "MAGNIFY_MINUS", "magnify-minus"),
    ONE_TO_ONE(MATERIAL_ICON, "FULLSCREEN_EXIT", "one-to-one"),
    ZOOM_TO_FIT(MATERIAL_ICON, "FULLSCREEN", "zoom-to-fit"),

    CIRCLE_A(EMOJI_ONE, "REGIONAL_INDICATOR_A", "circle-a"),
    CIRCLE_B(EMOJI_ONE, "REGIONAL_INDICATOR_B", "circle-b"),
    CIRCLE_C(EMOJI_ONE, "REGIONAL_INDICATOR_C", "circle-c"),
    CIRCLE_D(EMOJI_ONE, "REGIONAL_INDICATOR_D", "circle-d"),
    CIRCLE_E(EMOJI_ONE, "REGIONAL_INDICATOR_E", "circle-e"),
    CIRCLE_F(EMOJI_ONE, "REGIONAL_INDICATOR_F", "circle-f"),
    CIRCLE_G(EMOJI_ONE, "REGIONAL_INDICATOR_G", "circle-g"),
    CIRCLE_H(EMOJI_ONE, "REGIONAL_INDICATOR_H", "circle-h"),
    CIRCLE_I(EMOJI_ONE, "REGIONAL_INDICATOR_I", "circle-i"),
    CIRCLE_J(EMOJI_ONE, "REGIONAL_INDICATOR_J", "circle-j"),
    CIRCLE_K(EMOJI_ONE, "REGIONAL_INDICATOR_K", "circle-k"),
    CIRCLE_L(EMOJI_ONE, "REGIONAL_INDICATOR_L", "circle-l"),
    CIRCLE_M(EMOJI_ONE, "REGIONAL_INDICATOR_M", "circle-m"),
    CIRCLE_N(EMOJI_ONE, "REGIONAL_INDICATOR_N", "circle-n"),
    CIRCLE_O(EMOJI_ONE, "REGIONAL_INDICATOR_O", "circle-o"),
    CIRCLE_P(EMOJI_ONE, "REGIONAL_INDICATOR_P", "circle-p"),
    CIRCLE_Q(EMOJI_ONE, "REGIONAL_INDICATOR_Q", "circle-q"),
    CIRCLE_R(EMOJI_ONE, "REGIONAL_INDICATOR_R", "circle-r"),
    CIRCLE_S(EMOJI_ONE, "REGIONAL_INDICATOR_S", "circle-s"),
    CIRCLE_T(EMOJI_ONE, "REGIONAL_INDICATOR_T", "circle-t"),
    CIRCLE_U(EMOJI_ONE, "REGIONAL_INDICATOR_U", "circle-u"),
    CIRCLE_V(EMOJI_ONE, "REGIONAL_INDICATOR_V", "circle-v"),
    CIRCLE_W(EMOJI_ONE, "REGIONAL_INDICATOR_W", "circle-w"),
    CIRCLE_X(EMOJI_ONE, "REGIONAL_INDICATOR_X", "circle-x"),
    CIRCLE_Y(EMOJI_ONE, "REGIONAL_INDICATOR_Y", "circle-y"),
    CIRCLE_Z(EMOJI_ONE, "REGIONAL_INDICATOR_Z", "circle-z"),

    CASE_SENSITIVE(SVG, "", "case-sensitive"),
    ICON_CLOSE(SVG, "", "icon-close"),
    ICON_EDIT(SVG, "", "icon-edit"),
    ICON_EXPAND(MATERIAL_DESIGNS_WEBFONT, "EXPORT", "icon-expand"),
    ICON_EXPORT(SVG, "", "icon-export"),
    ICON_GO_TO_LINK(SVG, "", "icon-goto-link"),
    CASE_SENSITIVE_NOT(SVG, "", "not-case-sensitive"),
    ICON_RELOAD(SVG, "", "icon-reload"),
    ICON_BATCH_EDIT(SVG, "", "icon-batch-edit"),
    ICON_CHECK_MARK(SVG, "", "icon-check-mark"),
    ICON_CLASSIFIER1(SVG, "", "icon-classifier-1"),
    ICON_CLASSIFIER2(SVG, "", "icon-classifier-2"),
    ICON_CLASSIFIER3(SVG, "", "icon-classifier-3"),
    ICON_CLASSIFIER4(SVG, "", "icon-classifier-4"),
    ICON_LINK_TO_SEARCH(SVG, "", "icon-link-to-search"),
    ICON_LINK_TO_TAXONOMY(SVG, "", "icon-link-to-taxonomy"),
    ICON_NOT_LINKED(SVG, "", "icon-not-linked"),
    ICON_SETTINGS1(SVG, "", "icon-settings-1"),
    ICON_SETTINGS2(SVG, "", "icon-settings-2"),
    ICON_SETTINGS3(SVG, "", "icon-settings-3"),
    ICON_SEARCH(SVG, "", "icon-search"),
    ICON_TAXONOMY(SVG, "", "icon-taxonomy"),
    ICON_DEFINED_ARROW(SVG, "", "icon-defined-arrow"),
    ICON_DEFINED(SVG, "", "icon-defined"),
    ICON_PRIMITIVE(SVG, "", "icon-primitive"),
    ICON_PRIMITIVE_ARROW(SVG, "", "icon-primitive-arrow"),
    PAPERCLIP(FONT_AWSOME, "PAPERCLIP", "paperclip"),
    SOURCE_BRANCH(OCT_ICON, "GIT_BRANCH", "branch"),
    SOURCE_BRANCH_1(MATERIAL_DESIGNS_WEBFONT, "SOURCE_BRANCH", "branch-1"),
    PLUS(OCT_ICON, "PLUS", "plus"),
    CONCEPT_DETAILS(MATERIAL_ICON, "VIEW_WEEK", "concept-details"),
    CONCEPT_TABLE(MATERIAL_DESIGNS_WEBFONT, "TABLE", "concept-table"),
    KOMET(EMOJI_ONE, "COMET", "komet"),
    SPINNER(ICONS_525, "SPINNER", "spinner"),
    SPINNER0(ICONS_525, "SPINNER", "spinner"),
    SPINNER1(ICONS_525, "SPINNER", "spinner-1"),
    SPINNER2(ICONS_525, "SPINNER", "spinner-2"),
    SPINNER3(ICONS_525, "SPINNER", "spinner-3"),
    SPINNER4(ICONS_525, "SPINNER", "spinner-4"),
    SPINNER5(ICONS_525, "SPINNER", "spinner-5"),
    SPINNER6(ICONS_525, "SPINNER", "spinner-6"),
    SPINNER7(ICONS_525, "SPINNER", "spinner-7"),
    STOP_CIRCLE(FONT_AWSOME, "STOP_CIRCLE", "stop-circle"),
    STOP_STOP(ICONS_525, "STOP_SIGN", "stop-sign"),
    STOP_SQUARE(MATERIAL_ICON, "STOP", "stop-square"),
    ALERT_CONFIRM(SVG, "", "alert-confirm"),
    ALERT_INFORM(SVG, "", "alert-info"),
    ALERT_ERROR(SVG, "", "alert-error"),
    ALERT_WARN(SVG, "", "alert-warn"),
    ALERT_CONFIRM2(FONT_AWSOME, "QUESTION_CIRCLE", "alert-confirm-2"),
    ALERT_INFORM2(ICONS_525, "INFO_CIRCLE", "alert-info-2"),
    ALERT_ERROR2(MATERIAL_DESIGNS_WEBFONT, "ALERT_OCTAGON", "alert-error-2"),
    ALERT_WARN2(MATERIAL_DESIGNS_WEBFONT, "ALERT_CIRCLE_OUTLINE", "alert-warn-2"),
    TEMPORARY_FIX(MATERIAL_ICON, "HEALING", "temporary-fix"),
    CHECK(FONT_AWSOME, "CHECK", "check"),
    CHECKERED_FLAG(FONT_AWSOME, "CHECKERED_FLAG", "checkered-flag"),
    // Icons525.PLUS, MaterialDesignIconWebfont.PLUS, OctIcon.PLUS
    DASHBOARD(OCT_ICON, "DASHBOARD", "dashboard"),

    JAVASCRIPT(FONT_AWSOME, "CODE", "code"),

    LAMBDA(MATERIAL_DESIGNS_WEBFONT, "LAMBDA", "lambda"),

    ROLE_GROUP(MATERIAL_DESIGNS_WEBFONT, "FORMAT_LIST_BULLETED_TYPE", "role-group"),
    INFERRED(FONT_AWSOME, "GEARS", "inferred-form"),
    STATED(ICONS_525, "CHAT", "stated-form"),
    LINK_EXTERNAL(OCT_ICON, "LINK_EXTERNAL", "link-external"),
    NEW_CONCEPT(MATERIAL_DESIGNS_WEBFONT, "SHAPE_CIRCLE_PLUS", "new-concept"),
    CANCEL(ICONS_525, "CIRCLEDELETE", "cancel"),
    DUPLICATE(MATERIAL_ICON, "QUEUE", "duplicate"),
    ADD(MATERIAL_ICON, "ADD_BOX", "add"),
    // Icons525.CIRCLEDELETE
    // MaterialIcon.QUEUE
    OPEN(MATERIAL_DESIGNS_WEBFONT, "MENU_DOWN", "open"),
    CLOSE(MATERIAL_DESIGNS_WEBFONT, "MENU_RIGHT", "close"),
    FEATURE_FUNCTION(MATERIAL_DESIGNS_WEBFONT, "FUNCTION", "feature-function"),
    FEATURE_INFO(ICONS_525, "INFO", "feature-info"),
    FEATURE_RULER(MATERIAL_DESIGNS_WEBFONT, "RULER", "feature-ruler"),
    LITERAL_STRING(MATERIAL_ICON, "FORMAT_QUOTE", "literal-string"),
    LITERAL_NUMERIC(FONT_AWSOME, "HASHTAG", "literal-numeric"),
    ARROW_UP(MATERIAL_DESIGNS_WEBFONT, "ARROW_UP_BOLD", "arrow-up"),
    ARROW_DOWN(MATERIAL_DESIGNS_WEBFONT, "ARROW_DOWN_BOLD", "arrow-down"),
    DELETE_TRASHCAN(MATERIAL_ICON, "DELETE_FOREVER", "delete-trashcan"),

    // MaterialDesignIcon.MENU_DOWN
    // MaterialDesignIcon.MENU_RIGHT
    // MaterialDesignIcon.PLAY_CIRCLE_OUTLINE
    // MaterialDesignIcon.TOGGLE_SWITCH
    ;

    String cssClass;
    String glyphName;
    IconSource source;
    private Iconography2(IconSource source, String glyphName, String cssClass) {
        this.source = source;
        this.glyphName = glyphName;
        this.cssClass = cssClass;
    }

    public Glyph getGlyph() {
        GlyphFont font;
        switch (source) {
            case EMOJI_ONE:
                font = GlyphFonts.emojiOne();
                break;
            case FONT_AWSOME:
                font = GlyphFonts.fontAwesome();
                break;
            case ICONS_525:
                font = GlyphFonts.icons525();
                break;
            case MATERIAL_DESIGNS_WEBFONT:
                font = GlyphFonts.materialDesignWebfont();
                break;
            case MATERIAL_ICON:
                font = GlyphFonts.materialIcon();
                break;
            case OCT_ICON:
                font = GlyphFonts.octIcon();
                break;
            default:
                throw new UnsupportedOperationException("ao Can't handle: " + source);
        }

        Glyph g = font.create(glyphName);
        //g.getStyleClass().add(cssClass);
        return g;
    }
    public Node getIconographic() {
        GlyphFont font;
        switch (source) {
            case EMOJI_ONE:
                font = GlyphFonts.emojiOne();
                break;
            case FONT_AWSOME:
                font = GlyphFonts.fontAwesome();
                break;
            case ICONS_525:
                font = GlyphFonts.icons525();
                break;
            case MATERIAL_ICON:
                font = GlyphFonts.materialIcon();
                break;
            case MATERIAL_DESIGNS_WEBFONT:
                font = GlyphFonts.materialDesignWebfont();
                break;
            case OCT_ICON:
                font = GlyphFonts.octIcon();
                break;
            case SVG:
                return new SvgIconographic().setStyleClass(cssClass);
            default:
                throw new UnsupportedOperationException("ao Can't handle: " + source);
        }

        Glyph g = font.create(glyphName);
        g.setBorder(new Border(
                new BorderStroke(javafx.scene.paint.Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 0))
        ));
        g.setTooltip(new Tooltip(this.glyphName + ": " + this.source + ": " + this.cssClass));
        ///g.getStyleClass().add(cssClass);
        return g;
    }

    public static String getStyleSheetStringUrl() {
        return Iconography2.class.getResource("/sh/isaac/komet/iconography/Iconography.css").toString();
    }

}
