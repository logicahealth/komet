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
import java.util.prefs.BackingStoreException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.preferences.IsaacPreferences;
import static sh.isaac.komet.preferences.PreferenceGroup.Keys.GROUP_NAME;
import static sh.isaac.komet.preferences.UserPreferences.Keys.USER_CONCEPT;
import static sh.isaac.komet.preferences.UserPreferences.Keys.USER_CONCEPT_OPTIONS;
import sh.isaac.model.observable.ObservableFields;
import sh.komet.gui.control.concept.PropertySheetItemConceptNidWrapper;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public final class UserPreferences extends AbstractPreferences {
    enum Keys {
        USER_CONCEPT,
        USER_CONCEPT_OPTIONS,
    }

    IntegerProperty userConceptNidProperty = new SimpleIntegerProperty(this, ObservableFields.KOMET_USER.toExternalString());
    ObservableList<Integer> userConceptOptions = FXCollections.observableArrayList();
    
    public UserPreferences(IsaacPreferences preferencesNode, Manifold manifold) {
        super(preferencesNode, preferencesNode.get(GROUP_NAME, "User"), manifold);
        revertFields();
        save();
        int[] userConceptOptionNids = new int[userConceptOptions.size()];
        for (int i = 0; i < userConceptOptionNids.length; i++) {
            userConceptOptionNids[i] = userConceptOptions.get(i);
        }
        getItemList().add(new PropertySheetItemConceptNidWrapper(manifold,
           userConceptNidProperty, userConceptOptionNids));
    }

    @Override
    void saveFields() throws BackingStoreException {
        preferencesNode.put(USER_CONCEPT, Get.concept(userConceptNidProperty.get()).toExternalString());
        List<String> userConceptOptionExternalStrings = new ArrayList<>();
        for (Integer nid: userConceptOptions) {
            userConceptOptionExternalStrings.add(Get.concept(nid).toExternalString());
        }
        preferencesNode.putList(USER_CONCEPT_OPTIONS, userConceptOptionExternalStrings);
    }

    @Override
    void revertFields() {
        String userConceptSpec = preferencesNode.get(USER_CONCEPT, MetaData.USER____SOLOR.toExternalString());
        userConceptNidProperty.set(new ConceptProxy(userConceptSpec).getNid());
        List<String> userConceptOptionExternalStrings = preferencesNode.getList(USER_CONCEPT_OPTIONS);
        if (userConceptOptionExternalStrings.isEmpty()) {
            userConceptOptionExternalStrings.add(MetaData.USER____SOLOR.toExternalString());
            userConceptOptionExternalStrings.add(MetaData.KEITH_EUGENE_CAMPBELL____SOLOR.toExternalString());
        }
        userConceptOptions.clear();
        for (String externalString: userConceptOptionExternalStrings) {
            userConceptOptions.add(new ConceptProxy(externalString).getNid());
        }
    }
}
    

