/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.komet.progress.view;

import javafx.scene.Node;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;

import jakarta.inject.Singleton;

//~--- non-JDK imports --------------------------------------------------------

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service(name = "Activity panel factory")
@Singleton
public class TaskProgressNodeFactory
         implements ExplorationNodeFactory {
   
   public static final String TITLE_BASE = "Activities";
   public static final String TITLE_BASE_SINGULAR = "Activity";
   @Override
   public TaskProgressNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
      TaskProgressNode taskProgressNode = new ActiveTasksProgressNode(viewProperties);
      return taskProgressNode;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Node getMenuIcon() {
      return Iconography.SPINNER.getIconographic();
   }

   @Override
   public String getMenuText() {
      return TITLE_BASE;
   }

  /** 
   * {@inheritDoc}
   * @return
   */
  @Override
  public String[] getDefaultActivityFeed() {
     return new String[] {ViewProperties.UNLINKED};
  }

   @Override
   public ConceptSpecification getPanelType() {
      return MetaData.ACTIVITIES_PANEL____SOLOR;
   }
}

