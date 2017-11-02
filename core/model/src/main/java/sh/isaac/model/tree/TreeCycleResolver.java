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



package sh.isaac.model.tree;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

//~--- non-JDK imports --------------------------------------------------------

import javafx.concurrent.Task;

import sh.isaac.api.Get;
import sh.isaac.api.alert.Alert;
import sh.isaac.api.alert.AlertCategory;
import sh.isaac.api.alert.ResolutionPersistence;
import sh.isaac.api.alert.Resolver;
import sh.isaac.api.alert.SuccessAlert;
import sh.isaac.api.tree.TreeNodeVisitData;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class TreeCycleResolver
         implements Resolver {
   final TreeCycleError treeCycleError;

   //~--- constructors --------------------------------------------------------

   public TreeCycleResolver(TreeCycleError treeCycleError) {
      this.treeCycleError = treeCycleError;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public Task<Void> resolve() {
      ResolutionTask task = new ResolutionTask();
      Get.executor().execute(task);
      return task;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getDescription() {
      return "Remove a relationship from the computed tree to break the cycle.";
   }

   @Override
   public ResolutionPersistence getPersistence() {
      return ResolutionPersistence.TEMPORARY;
   }

   @Override
   public String getTitle() {
      return "break cycle";
   }

   //~--- inner classes -------------------------------------------------------

   private class ResolutionTask
           extends Task<Void> {
      @Override
      protected Void call()
               throws Exception {
         int bottomConceptIndex = Integer.MAX_VALUE;
         int parentConceptIndex;
         int maxDepth = 0;

         for (int i = 0; i < treeCycleError.cycle.length; i++) {
            int conceptSeqeuence = treeCycleError.cycle[i];
            int newDepth         = treeCycleError.visitData.getDistance(conceptSeqeuence);

            if (newDepth > maxDepth) {
               maxDepth           = newDepth;
               bottomConceptIndex = i;
            }
         }

         int bottomConceptSequence = treeCycleError.cycle[bottomConceptIndex];

         if (bottomConceptIndex > 0) {
            parentConceptIndex = treeCycleError.cycle[bottomConceptIndex - 1];
         } else {
            parentConceptIndex = treeCycleError.cycle[1];
         }

         System.out.println("Parent concept in cycle is: " + Get.conceptDescriptionText(parentConceptIndex));
         System.out.println("Bottom concept in cycle is: " + Get.conceptDescriptionText(bottomConceptSequence));
         treeCycleError.tree.removeParent(bottomConceptSequence, parentConceptIndex);

         // test resolution
         TreeNodeVisitData visitData = treeCycleError.tree.breadthFirstProcess(treeCycleError.tree.getRootNids()[0],
                                                 (TreeNodeVisitData t,
                                                       int thisSequence) -> {},
         Get.taxonomyService().getTreeNodeVisitDataSupplier(treeCycleError.tree.getAssemblageNid()));

         if (!visitData.getCycleSet()
                       .isEmpty()) {
            System.out.println("Cycle found: " + Arrays.asList(visitData.getCycleSet()));
         } else {
            System.out.println("Cycle fixed. ");
            Alert.publishRetraction(treeCycleError);
            SuccessAlert alert = new SuccessAlert("Cycle fixed.", "Cycle temporarily fixed in computed tree. ", AlertCategory.TAXONOMY);
            Alert.publishAddition(alert);
            new Timer().schedule(new TimerTask() {
               @Override
               public void run() {
                  Alert.publishRetraction(alert);
               }
            }, 10*1000);
         }

         return null;
      }
   }
}

