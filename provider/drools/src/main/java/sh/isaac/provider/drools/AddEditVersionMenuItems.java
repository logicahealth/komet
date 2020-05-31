/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.drools;

import sh.komet.gui.control.PropertySheetMenuItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.control.MenuItem;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.observable.ObservableVersion;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class AddEditVersionMenuItems {

    final List<MenuItem> menuItems = new ArrayList<>();
    final ViewProperties viewProperties;
    final ObservableCategorizedVersion categorizedVersion;
    final Consumer<PropertySheetMenuItem> propertySheetConsumer;
    final HashMap<String, PropertySheetMenuItem> propertySheetMenuItems = new HashMap<>();

    public AddEditVersionMenuItems(ViewProperties viewProperties,
                                   ObservableCategorizedVersion categorizedVersion,
                                   Consumer<PropertySheetMenuItem> propertySheetConsumer) {
        this.viewProperties = viewProperties;
        this.categorizedVersion = categorizedVersion;
        this.propertySheetConsumer = propertySheetConsumer;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public List<ReadOnlyProperty<?>> getProperties() {
        return categorizedVersion.getProperties();
    }

    public ObservableCategorizedVersion getCategorizedVersion() {
        return categorizedVersion;
    }

    public ConceptSpecification getAssemblageForVersion() {
        return new ConceptProxy(categorizedVersion.getAssemblageNid());
    }

    public VersionType getVersionType() {
        return categorizedVersion.getSemanticType();
    }

    public PropertySheetMenuItem makePropertySheetMenuItem(String menuText) {
        if (propertySheetMenuItems.containsKey(menuText)) {
            return propertySheetMenuItems.get(menuText);
        }
        PropertySheetMenuItem propertySheetMenuItem = new PropertySheetMenuItem(viewProperties, categorizedVersion);
        propertySheetMenuItems.put(menuText, propertySheetMenuItem);
        MenuItem menuItem = new MenuItemWithText(menuText);
        menuItem.setOnAction((event) -> {
            // create version to edit here
            ObservableVersion uncommittedVersion = categorizedVersion.makeAutonomousAnalog(FxGet.editCoordinate());
            propertySheetMenuItem.setVersionInFlight(uncommittedVersion);
            propertySheetMenuItem.prepareToExecute();
            propertySheetConsumer.accept(propertySheetMenuItem);
        });
        menuItems.add(menuItem);
        return propertySheetMenuItem;
    }
}
