package sh.isaac.komet.iconography;

import de.jensd.fx.glyphs.emojione.EmojiOneView;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.icons525.Icons525View;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class IconographyHelper {

    public ImageView getImageView(Icons icon) {
        Node node = getIconographic(icon);
        node.applyCss();
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setViewport(new Rectangle2D(0, 0, node.getLayoutBounds().getWidth(), node.getLayoutBounds().getHeight()));
        Image image = node.snapshot(snapshotParameters, null);
        return new ImageView(image);
    }

    public Node getIconographic(double size, Icons icon) {
        Node node = getIconographic(icon);
        StackPane.setMargin(node, new Insets(0,0,5,0));
        StackPane stack = new StackPane(node);
        stack.setMinSize(size, size);
        stack.setMaxSize(size, size);
        stack.setPrefSize(size, size);
        return stack;
    }

    public static Node getIconographic(Icons icon) {
        switch (icon.getSource()) {
            case MATERIAL_DESIGNS_WEBFONT:
                if (icon.getUnicode() != null) {
                    return new MaterialDesignIconView((MaterialDesignIcon) icon.getUnicode()).setStyleClass(icon.getCssClass());
                }
                return new MaterialDesignIconView().setStyleClass(icon.getCssClass());
            case FONT_AWSOME:
                if (icon.getUnicode() != null) {
                    return new FontAwesomeIconView((FontAwesomeIcon) icon.getUnicode()).setStyleClass(icon.getCssClass());
                }
                return new FontAwesomeIconView().setStyleClass(icon.getCssClass());
            case SVG:
                return new SvgIconographic().setStyleClass(icon.getCssClass());
            case MATERIAL_ICON:
                return new MaterialIconView().setStyleClass(icon.getCssClass());
            case EMOJI_ONE:
                return new EmojiOneView().setStyleClass(icon.getCssClass());
            case ICONS_525:
                return new Icons525View().setStyleClass(icon.getCssClass());
            case OCT_ICON:
                return new OctIconView().setStyleClass(icon.getCssClass());
            default:
                throw new UnsupportedOperationException("ao Can't handle: " + icon.getSource());
        }
    }

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

    public static Node combine(Icons... icons) {
        HBox hbox = new HBox(1);
        hbox.getStyleClass().add("hbox");
        for (Icons icon: icons) {
            hbox.getChildren().add(icon.getIconographic());
        }
        return hbox;
    }
    public static Node combine(Node... icons) {
        HBox hbox = new HBox(1);
        hbox.getStyleClass().add("hbox");
        for (Node icon: icons) {
            hbox.getChildren().add(icon);
        }
        return hbox;
    }


}
