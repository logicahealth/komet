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

import sh.isaac.komet.iconography.wrappers.EmojiOneWrapper;
import de.jensd.fx.glyphs.emojione.EmojiOneView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.icons525.Icons525View;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIconView;
import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import sh.isaac.komet.iconography.wrappers.Icons525Wrapper;
import sh.isaac.komet.iconography.wrappers.MaterialDesignWebfontWrapper;
import sh.isaac.komet.iconography.wrappers.MaterialDesignIconWrapper;
import sh.isaac.komet.iconography.wrappers.OctIconWrapper;

/**
 *
 * @author kec
 */
public class GlyphFonts {

    private static final GlyphFonts FONTS = new GlyphFonts();

    public static GlyphFont fontAwesome() {
        return GlyphFontRegistry.font("FontAwesome");
    }

    public static GlyphFont materialIcon() {
        return GlyphFontRegistry.font(MaterialDesignIconWrapper.FONT_NAME);
    }

    public static GlyphFont materialDesignWebfont() {
        return GlyphFontRegistry.font(MaterialDesignWebfontWrapper.FONT_NAME);
    }

    public static GlyphFont emojiOne() {
        return GlyphFontRegistry.font(EmojiOneWrapper.FONT_NAME);
    }

    public static GlyphFont icons525() {
        return GlyphFontRegistry.font(Icons525Wrapper.FONT_NAME);
    }

    public static GlyphFont octIcon() {
        return GlyphFontRegistry.font(OctIconWrapper.FONT_NAME);
    }


    private GlyphFonts() {
        GlyphFontRegistry.register(new EmojiOneWrapper());
        GlyphFontRegistry.register(new MaterialDesignIconWrapper());
        GlyphFontRegistry.register(new MaterialDesignWebfontWrapper());
        GlyphFontRegistry.register(new Icons525Wrapper());
        GlyphFontRegistry.register(new OctIconWrapper());
    }
}
