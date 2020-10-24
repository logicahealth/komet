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
package sh.isaac.model.tree;

import java.util.Arrays;
import java.util.concurrent.Callable;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.AlertObject;
import sh.isaac.api.alert.AlertType;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.tree.TreeNodeVisitData;

/**
 *
 * @author kec
 */
public class TreeCycleError extends AlertObject {

   final TreeNodeVisitData visitData;
   final Tree tree;

   public TreeCycleError(int[] cycle, TreeNodeVisitData visitData, Tree tree, String alertText, String alertDescription, AlertType alertType) {
      super(alertText, alertDescription, alertType, AlertCategory.TAXONOMY, null, cycle);
       this.visitData = visitData;
      this.tree = tree;
   }
   public TreeCycleError(int[] cycle, TreeNodeVisitData visitData, Tree tree, String alertText, String alertDescription, AlertType alertType, Callable<Boolean> resolutionTester) {
      super(alertText, alertDescription, alertType, AlertCategory.TAXONOMY, resolutionTester, cycle);
      this.visitData = visitData;
      this.tree = tree;
   }

   @Override
   public String toString() {
      return super.toString()  + " cycle=" + Arrays.asList(getAffectedComponents());
   }

}
