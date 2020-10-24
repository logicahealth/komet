package sh.komet.gui.menu;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public class MenuItemWithText extends MenuItem {
    public MenuItemWithText() {
    }

    public MenuItemWithText(String text) {
        super(text);
    }

    public MenuItemWithText(String text, Node graphic) {
        super(text, graphic);
    }

    @Override public String toString() {
        String superString = super.toString();
        int insertPosition = superString.indexOf('[');
        StringBuilder sbuf = new StringBuilder(superString.substring(0, insertPosition + 1));
        sbuf.append("text=");
        sbuf.append(getText());
        sbuf.append(", ");
        sbuf.append(superString.substring(insertPosition + 1));
        return sbuf.toString();
    }
}
