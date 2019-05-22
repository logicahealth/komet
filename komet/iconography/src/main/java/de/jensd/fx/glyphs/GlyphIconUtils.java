package de.jensd.fx.glyphs;

import javafx.css.CssParser;
import javafx.css.Stylesheet;
import javafx.scene.text.Font;

public class GlyphIconUtils {

    private final static CssParser CSS_PARSER = new CssParser();
    private final static Number DEFAULT_SIZE = 12.0;

    public static Number convert(String sizeString, Font font) {
        Stylesheet stylesheet = CSS_PARSER.parse("{-fx-font-size: ".concat(sizeString).concat(";}"));
        if(stylesheet.getRules().isEmpty()){
            return DEFAULT_SIZE;
        }
        else if(stylesheet.getRules().get(0).getDeclarations().isEmpty()){
            return DEFAULT_SIZE;
        }
        return (Number)stylesheet.getRules().get(0).getDeclarations().get(0).getParsedValue().convert(font);
    }
}
