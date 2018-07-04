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
import javafx.application.Platform;
import org.jvnet.hk2.annotations.Contract;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sh.isaac.api.ApplicationStates;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;

/**
 * An interface various modules can implement to provide menus that will be
 * automatically added into the application.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface MenuProvider {

    AtomicInteger WINDOW_COUNT = new AtomicInteger(1);
    AtomicInteger WINDOW_SEQUENCE = new AtomicInteger(1);

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

        MenuProvider.WINDOW_COUNT.decrementAndGet();

    }
}
