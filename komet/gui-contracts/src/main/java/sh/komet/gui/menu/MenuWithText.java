package sh.komet.gui.menu;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class MenuWithText extends Menu {

    public MenuWithText() {
    }

    public MenuWithText(String text) {
        super(text);
    }

    public MenuWithText(String text, Node graphic) {
        super(text, graphic);
    }

    public MenuWithText(String text, Node graphic, MenuItem... items) {
        super(text, graphic, items);
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
