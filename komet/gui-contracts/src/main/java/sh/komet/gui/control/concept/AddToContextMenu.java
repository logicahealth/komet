package sh.komet.gui.control.concept;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

import java.util.Optional;

public interface AddToContextMenu {
    void addToContextMenu(ContextMenu contextMenu, ViewProperties viewProperties,
                                 SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                                 SimpleIntegerProperty selectionIndexProperty,
                                 Runnable unlink);
}
