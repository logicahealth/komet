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

//~--- JDK imports ------------------------------------------------------------

import java.util.function.Consumer;

//~--- non-JDK imports --------------------------------------------------------

import javafx.scene.Node;

import org.glassfish.hk2.runlevel.RunLevel;

import org.jvnet.hk2.annotations.Service;

import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
@Service(name = "Activity panel factory")
@RunLevel(value = 1)
public class TaskProgressNodeFactory
         implements ExplorationNodeFactory {
   
   public static final String TITLE_BASE = "Activities";
   public static final String TITLE_BASE_SINGULAR = "Activity";
   @Override
   public ExplorationNode createExplorationNode(Manifold manifold, Consumer<Node> nodeConsumer) {
      TaskProgressNode taskProgressNode = new TaskProgressNode(manifold);

      nodeConsumer.accept(taskProgressNode.getNode());
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
}

