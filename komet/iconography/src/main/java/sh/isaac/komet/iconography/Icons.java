package sh.isaac.komet.iconography;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public interface Icons {

    String getCssClass();

    IconSource getSource();

    Enum getUnicode();

    default Node getIconographic() {
        return IconographyHelper.getIconographic(this);
    }

    AnchorPane getStyledIconographic();

    default AnchorPane getStyledIconographic(String styleSheetUrl) {
        Node icon = getIconographic();
        AnchorPane.setTopAnchor(icon, 0.0);
        AnchorPane.setRightAnchor(icon, 0.0);
        AnchorPane.setBottomAnchor(icon, 0.0);
        AnchorPane.setLeftAnchor(icon, 0.0);
        AnchorPane anchorPane = new AnchorPane(icon);
        anchorPane.getStylesheets().add(styleSheetUrl);
        return anchorPane;
    }
    default Node getIconographic(double size) {
        Node node = getIconographic();
        StackPane.setMargin(node, new Insets(0,0,5,0));
        StackPane stack = new StackPane(node);
        stack.setMinSize(size, size);
        stack.setMaxSize(size, size);
        stack.setPrefSize(size, size);
        return stack;
    }

}
