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
import org.jvnet.hk2.annotations.Contract;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

/**
 * An interface various modules can implement to provide menus that will be
 * automatically added into the application.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public interface MenuProvider {

    /**
     * @return the parent menus this provider creates items for
     */
    public EnumSet<AppMenu> getParentMenus();

    /**
     * @param parentMenu
     * @param window the window this menu will be part of
     * @return the menu item to add to the app level menu
     */
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window);
}
