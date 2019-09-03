/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.contract;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.jvnet.hk2.annotations.Contract;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.preferences.PreferenceGroup;
import sh.komet.gui.util.FxGet;

/**
 * An interface various modules can implement to provide menus that will be
 * automatically added into the application.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface MenuProvider {
    enum Keys {
        WINDOW_PREFERENCE_ABSOLUTE_PATH
    }
    public static final String PARENT_PREFERENCES = MenuProvider.class.getName() + ".PARENT_PREFERENCES";
    AtomicInteger WINDOW_COUNT = new AtomicInteger(0);

    /**
     * @return the parent menus this provider creates items for
     */
    EnumSet<AppMenu> getParentMenus();

    /**
     * @param parentMenu
     * @param window the window this menu will be part of
     * @return the menu item to add to the app level menu
     */
    MenuItem[] getMenuItems(AppMenu parentMenu, Window window);

    static void handleCloseRequest(WindowEvent e) {
        if (MenuProvider.WINDOW_COUNT.get() == 1) {
            e.consume();
            Get.applicationStates().remove(ApplicationStates.RUNNING);
            Get.applicationStates().add(ApplicationStates.STOPPING);
            // need shutdown to all happen on a non event thread...
            Thread shutdownThread = new Thread(() -> {  //Can't use the thread poool for this, because shutdown 
                //system stops the thread pool, which messes up the shutdown sequence.
                LookupService.shutdownSystem();
                Platform.runLater(() -> {
                    try {
                        Platform.exit();
                        System.exit(0);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                });
            }, "shutdown-thread");
            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }
        String absolutePath = (String) ((Stage) e.getTarget()).getScene().getProperties().get(Keys.WINDOW_PREFERENCE_ABSOLUTE_PATH);
        try {
            IsaacPreferences windowPreferencesNode =
                        Get.preferencesService().getConfigurationPreferences().node(absolutePath);
            IsaacPreferences windowParentNode = windowPreferencesNode.parent();
            windowPreferencesNode.clear();
            windowPreferencesNode.flush();
            windowPreferencesNode.removeNode();
            windowPreferencesNode.flush();
            PreferenceGroup.removeChild(windowParentNode, windowPreferencesNode.name());
            windowParentNode.flush();
        } catch (BackingStoreException ex) {
            FxGet.dialogs().showErrorDialog(ex);
        }
        MenuProvider.WINDOW_COUNT.decrementAndGet();

    }
}
