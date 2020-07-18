/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.provider.concept.comparison;

import java.util.EnumSet;
import java.util.UUID;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class ConceptCorrelationViewMenuProvider implements MenuProvider {
    protected static final Logger LOG = LogManager.getLogger();

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.NEW_WINDOW);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window, WindowPreferences windowPreference) {
        if (parentMenu == AppMenu.NEW_WINDOW) {
            MenuItem newComparisonWindowMenuItem = new MenuItemWithText("Correlation Window");
            newComparisonWindowMenuItem.setOnAction(this::newCorelationView);
            return new MenuItem[] {newComparisonWindowMenuItem};
        }
        return new MenuItem[] {};
    }
    
    private void newCorelationView(ActionEvent event) {
        MenuItem eventMenu = (MenuItem) event.getSource();
        IsaacPreferences parentPreferences = (IsaacPreferences) eventMenu.getProperties().get(MenuProvider.PARENT_PREFERENCES);
        IsaacPreferences correlationPreferences = parentPreferences.node(UUID.randomUUID().toString());
        ViewProperties correlationViewProperties = FxGet.newDefaultViewProperties();
        ActivityFeed correlationViewFeed = correlationViewProperties.getActivityFeed(ViewProperties.CORRELATION);
        ConceptCorrelationController conceptCorrelationController = ConceptCorrelationView.show(correlationViewProperties,
                correlationViewFeed,
                correlationPreferences,
                MenuProvider::handleCloseRequest);
        MenuProvider.WINDOW_COUNT.incrementAndGet();
    }

    
}
