package sh.komet.assemblage.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.identity.IdentifiedObject;
import sh.komet.gui.control.concept.AddToContextMenu;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AssemblageMenuProvider implements AddToContextMenu {
    protected static final Logger LOG = LogManager.getLogger();

    private static AssemblageMenuProvider SINGLETON;

    public static AssemblageMenuProvider get() {
        if (SINGLETON == null) {
            SINGLETON = new AssemblageMenuProvider();
        }
        return SINGLETON;
    }

    private AssemblageMenuProvider() {
    }

    @Override
    public void addToContextMenu(ContextMenu contextMenu, ViewProperties viewProperties,
                                 SimpleObjectProperty<IdentifiedObject> conceptFocusProperty,
                                 SimpleIntegerProperty selectionIndexProperty, Runnable unlink) {
        for (MenuItem menuItem: get(viewProperties, conceptFocusProperty)) {
            contextMenu.getItems().add(menuItem);
        }
        contextMenu.getItems().add(new SeparatorMenuItem());
    }

    public List<MenuItem> get(ViewProperties viewProperties, SimpleObjectProperty<IdentifiedObject> conceptFocusProperty) {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        Menu assemblagesMenu = new Menu("Populated assemblages");
        assemblageMenuList.add(assemblagesMenu);
        Menu versionByTypeMenu = new Menu("Assemblages by version type");
        assemblageMenuList.add(versionByTypeMenu);
        HashMap<VersionType, Menu> versionTypeMenuMap = new HashMap();


        for (VersionType versionType: VersionType.values()) {
            Menu versionTypeMenu = new Menu(versionType.toString());
            versionTypeMenuMap.put(versionType, versionTypeMenu);
            versionByTypeMenu.getItems().add(versionTypeMenu);
        }

        int[] assembalgeNids = Get.assemblageService().getAssemblageConceptNids();

        LOG.debug("Assemblage nid count: " + assembalgeNids.length + "\n" + Arrays.toString(assembalgeNids));

        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            MenuItem menu = new MenuItemWithText(viewProperties.getPreferredDescriptionText(assemblageNid));
            menu.setOnAction((event) -> {
                conceptFocusProperty.set(new ComponentProxy(Get.concept(assemblageNid)));
            });
            assemblagesMenu.getItems().add(menu);
            String preferredDescText = viewProperties.getPreferredDescriptionText(assemblageNid);
            LOG.debug("Assemblage name <" + assemblageNid + ">: " + preferredDescText);
            MenuItem menu2 = new MenuItemWithText(preferredDescText);
            menu2.setOnAction((event) -> {
                conceptFocusProperty.set(new ComponentProxy(Get.concept(assemblageNid)));
            });
            VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
            versionTypeMenuMap.get(versionType).getItems().add(menu2);
        }
        assemblagesMenu.getItems().sort((o1, o2) -> {
            return o1.getText().compareTo(o2.getText());
        });
        for (Menu menu: versionTypeMenuMap.values()) {
            menu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText());
            });
        }

        return assemblageMenuList;
    }
}
