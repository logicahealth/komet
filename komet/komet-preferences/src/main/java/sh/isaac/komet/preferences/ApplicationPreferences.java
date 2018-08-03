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
package sh.isaac.komet.preferences;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.preferencesfx.model.Category;
import sh.isaac.komet.preferencesfx.model.Setting;

/**
 *
 * @author kec
 */
public class ApplicationPreferences extends AbstractPreferences {

    private final BooleanProperty enableEdit = new SimpleBooleanProperty(this, "Enable edit");
    private final List<ManifoldGroupPreferences> manifoldGroups = new ArrayList<>();
    private final List<WindowPreferences> windows = new ArrayList<>();

    // HashMap for manifolds?
    // HashMap for Windows?
    public ApplicationPreferences(IsaacPreferences preferencesNode) {
        super(preferencesNode, "Application preferences");
        this.enableEdit.setValue(preferencesNode.getBoolean(enableEdit.getName(), true));
        
        IsaacPreferences manifoldNode = preferencesNode.node(ManifoldGroupPreferences.class);
        manifoldGroups.add(new ManifoldGroupPreferences(manifoldNode));
    }

    @Override
    public Property<?>[] getProperties() {
        return new Property<?>[]{enableEdit};
    }
    private List<Category> getWindowCategories() {
        List<Category> windowCategorys = new ArrayList<>();
        for (WindowPreferences windowPreferences : windows) {
            for (Category category : windowPreferences.getCategories()) {
                windowCategorys.add(category);
            }
        }
        return windowCategorys;
    }
    
    private List<Category> getManifoldCategories() {
        List<Category> manifoldCategorys = new ArrayList<>();
        for (ManifoldGroupPreferences manifoldGroupPreferences : manifoldGroups) {
            for (Category category : manifoldGroupPreferences.getCategories()) {
                manifoldCategorys.add(category);
            }
        }
        return manifoldCategorys;
    }

    @Override
    public Category[] getCategories() {
        List<Category> applicationCategorys = new ArrayList<>();
        applicationCategorys.add(Category.of("General",
                Setting.of("Enable editing", enableEdit)
        ));
        applicationCategorys.add(Category.of("Manifolds")
            .subCategories(getManifoldCategories().toArray(new Category[0])));
        applicationCategorys.add(Category.of("Windows")
            .subCategories(getWindowCategories().toArray(new Category[0])));
        return applicationCategorys.toArray(new Category[applicationCategorys.size()]);
    }
}
