package sh.isaac.komet.batch.iconography;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import sh.isaac.komet.iconography.IconSource;
import sh.isaac.komet.iconography.IconographyHelper;
import sh.isaac.komet.iconography.Icons;

import static sh.isaac.komet.iconography.IconSource.MATERIAL_DESIGNS_WEBFONT;

public enum PluginIcons implements Icons {
    SCRIPT_ICON(MATERIAL_DESIGNS_WEBFONT, "batch-icon", MaterialDesignIcon.SCRIPT);

    String cssClass;
    IconSource source;
    Enum unicode;

    private PluginIcons(IconSource source, String cssClass, Enum unicode) {
        this.source = source;
        this.cssClass = cssClass;
        this.unicode = unicode;
    }

    private PluginIcons(IconSource source, String cssClass) {
        this.source = source;
        this.cssClass = cssClass;
        this.unicode = null;
    }

    public String getCssClass() {
        return cssClass;
    }

    public IconSource getSource() {
        return source;
    }

    public Enum getUnicode() {
        return unicode;
    }

    @Override
    public Node getIconographic() {
        return IconographyHelper.getIconographic(this);
    }

    @Override
    public AnchorPane getStyledIconographic() {
        return getStyledIconographic(getClass().getResource("/sh/isaac/komet/batch/fxml/ListViewNode.css").toString());
    }

    @Override
    public AnchorPane getIconographicWithStyleClasses(String... styleClasses) {
        return getStyledIconographic(getClass().getResource("/sh/isaac/komet/batch/fxml/ListViewNode.css").toString(),
                styleClasses);
    }

}
