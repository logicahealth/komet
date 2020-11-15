package sh.isaac.komet.importer.menu;

import jakarta.inject.Singleton;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.jvnet.hk2.annotations.Service;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.menu.MenuItemWithText;

import java.util.EnumSet;
import java.util.Optional;

@Service
@Singleton
public class ArtifactImporterMenuProvider implements MenuProvider {
    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.TASK);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window, WindowPreferences windowPreference) {
        switch (parentMenu) {
            case TASK: {
                MenuItem artifactImport = new MenuItemWithText("Artifact Import");
                artifactImport.setOnAction((ActionEvent event) -> {
                    ArtifactImporter.startArtifactImport(window);
                });
                Optional<MenuItem> beerMenu =  BeerImporter.getBeerMenu(window);
                if (beerMenu.isPresent()) {
                    return new MenuItem[] { artifactImport, beerMenu.get() };
                }
                return new MenuItem[] { artifactImport };
            }
        }
        return new MenuItem[0];
    }
}
