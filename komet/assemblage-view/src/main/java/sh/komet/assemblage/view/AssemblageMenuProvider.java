package sh.komet.assemblage.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.ComponentProxy;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class AssemblageMenuProvider implements Supplier<List<MenuItem>> {
    protected static final Logger LOG = LogManager.getLogger();

    private final SimpleObjectProperty<Manifold> manifoldProperty;

    public AssemblageMenuProvider(SimpleObjectProperty<Manifold> manifoldProperty) {
        this.manifoldProperty = manifoldProperty;
    }

    @Override
    public List<MenuItem> get() {
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
            MenuItem menu = new MenuItemWithText(this.manifoldProperty.get().getPreferredDescriptionText(assemblageNid));
            menu.setOnAction((event) -> {
                this.manifoldProperty.get().manifoldSelectionProperty().get().setAll(new ComponentProxy(Get.concept(assemblageNid)));
            });
            assemblagesMenu.getItems().add(menu);
            String preferredDescText = this.manifoldProperty.get().getPreferredDescriptionText(assemblageNid);
            LOG.debug("Assemblage name <" + assemblageNid + ">: " + preferredDescText);
            MenuItem menu2 = new MenuItemWithText(preferredDescText);
            menu2.setOnAction((event) -> {
                this.manifoldProperty.get().manifoldSelectionProperty().get().setAll(new ComponentProxy(Get.concept(assemblageNid)));
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
