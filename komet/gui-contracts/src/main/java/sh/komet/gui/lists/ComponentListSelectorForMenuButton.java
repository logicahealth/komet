package sh.komet.gui.lists;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.coordinate.ObservableManifoldCoordinate;
import sh.isaac.api.util.UuidStringKey;
import sh.komet.gui.interfaces.ComponentList;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

import java.util.*;
import java.util.stream.Stream;

public class ComponentListSelectorForMenuButton {
    protected static final Logger LOG = LogManager.getLogger();
    private static final String EMPTY_LIST_NAME = "Empty list";
    public static final UuidStringKey EMPTY_LIST_KEY = new UuidStringKey(UUID.fromString("df5d6715-8b20-4448-bc00-52d7da9ab95e"), EMPTY_LIST_NAME);
    public static final EmptyList EMPTY_LIST = new EmptyList();
    private final MenuButton listSelectionMenuButton;
    private final ObservableManifoldCoordinate manifoldCoordinate;
    private final SimpleObjectProperty<UuidStringKey> componentListProperty = new SimpleObjectProperty<>();

    public ComponentListSelectorForMenuButton(MenuButton listSelectionMenuButton, ObservableManifoldCoordinate manifoldCoordinate) {
        this.listSelectionMenuButton = listSelectionMenuButton;
        this.manifoldCoordinate = manifoldCoordinate;
        listSelectionMenuButton.getItems().clear();
        listSelectionMenuButton.getItems().addAll(getMenuItems());
        componentListProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                listSelectionMenuButton.setText("list selection...");
            } else {
                listSelectionMenuButton.setText(newValue.toString());
            }
        });
    }

    public UuidStringKey getComponentListKey() {
        return componentListProperty.get();
    }

    public SimpleObjectProperty<UuidStringKey> componentListProperty() {
        return componentListProperty;
    }

    private List<MenuItem> getMenuItems() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();

        MenuItem emptyListMenuItem = new MenuItem(EMPTY_LIST_NAME);
        assemblageMenuList.add(emptyListMenuItem);
        emptyListMenuItem.setOnAction(event -> {
            componentListProperty.set(EMPTY_LIST_KEY);
        });

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

        StampFilterImmutable viewFilter = manifoldCoordinate.getViewStampFilter().toStampFilterImmutable();
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            if (Get.conceptActiveService().isConceptActive(assemblageNid, viewFilter)) {
                String preferredDescText = manifoldCoordinate.getPreferredDescriptionText(assemblageNid);
                MenuItem menu = new MenuItemWithText(preferredDescText);
                menu.setOnAction((event) -> {
                    componentListProperty.set(new UuidStringKey(Get.identifierService().getUuidPrimordialForNid(assemblageNid), preferredDescText));
                });
                assemblagesMenu.getItems().add(menu);
                LOG.debug("Assemblage name <" + assemblageNid + ">: " + preferredDescText);
                MenuItem menu2 = new MenuItemWithText(preferredDescText);
                menu2.setOnAction((event) -> {
                    componentListProperty.set(new UuidStringKey(Get.identifierService().getUuidPrimordialForNid(assemblageNid), preferredDescText));
                });
                VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
                versionTypeMenuMap.get(versionType).getItems().add(menu2);
            }
        }
        assemblagesMenu.getItems().sort((o1, o2) -> {
            return o1.getText().compareTo(o2.getText());
        });
        for (Menu menu: versionTypeMenuMap.values()) {
            menu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText());
            });
        }

        Menu simpleListsMenu = new Menu("Simple lists");
        assemblageMenuList.add(simpleListsMenu);
        for (UuidStringKey listKey: FxGet.componentListKeys()) {
            MenuItem menu = new MenuItemWithText(listKey.getString());
            menu.setOnAction((event) -> {
                componentListProperty.set(listKey);
            });
            assemblagesMenu.getItems().add(menu);
        }
        return assemblageMenuList;
    }


    private static class EmptyList implements ComponentList {

        StringProperty nameProperty = new SimpleStringProperty(this, EMPTY_LIST_NAME + "name property", EMPTY_LIST_NAME);

        @Override
        public Stream<Chronology> getComponentStream() {
            return Stream.empty();
        }

        @Override
        public int listSize() {
            return 0;
        }

        @Override
        public Optional<ObservableList<ObservableChronology>> getOptionalObservableComponentList() {
            return Optional.empty();
        }

        @Override
        public StringProperty nameProperty() {
            return nameProperty;
        }

        @Override
        public UuidStringKey getUuidStringKey() {
            return EMPTY_LIST_KEY;
        }
    }
}
