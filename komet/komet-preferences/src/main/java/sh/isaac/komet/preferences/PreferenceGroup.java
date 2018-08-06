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

import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public interface PreferenceGroup {
    enum Keys {
        INITIALIZED, 
        GROUP_NAME;
    }

    /**
     * 
     * @param manifold
     * @return property sheet for editing properties in this group.
     */
    PropertySheet getPropertySheet(Manifold manifold);

    /**
     * 
     * @return possibly null panel
     */
    Node getRightPanel(Manifold manifold);
    
    /**
     * 
     * @return possibly null panel
     */
    Node getTopPanel(Manifold manifold);
    
    /**
     * 
     * @return possibly null panel
     */
    Node getBottomPanel(Manifold manifold);
    
    /**
     * 
     * @return possibly null panel
     */
    Node getLeftPanel(Manifold manifold);
    
    /**
     * 
     * @return name for this group. Will be used in tree view navigation of 
     * preferences.
     */
    String getGroupName();
    /**
     * Save preferences in group to preferences store
     */
    void save();
    /**
     * Revert any changed preferences to values currently in preferences store
     */
    void revert();
    /**
     * 
     * @return True of this PreferenceGroup is previously initialized, and was
     * read from preferences. False if this PreferenceGroup is to be newly created
     * with default values. 
     */
    boolean initialized();
}
