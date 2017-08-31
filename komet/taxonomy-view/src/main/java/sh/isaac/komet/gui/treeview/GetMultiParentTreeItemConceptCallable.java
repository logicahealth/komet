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



package sh.isaac.komet.gui.treeview;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.concurrent.Task;

import javafx.scene.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.komet.iconography.Iconography;

import sh.komet.gui.interfaces.IconProvider;

//~--- classes ----------------------------------------------------------------

/**
 * A concrete {@link Callable} for fetching concepts.
 *
 * @author ocarlsen
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class GetMultiParentTreeItemConceptCallable
        extends Task<Boolean>
         implements IconProvider {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private final ArrayList<MultiParentTreeItem> childrenToAdd = new ArrayList<>();
   private final MultiParentTreeItem            treeItem;
   private final boolean                        addChildren;
   private ConceptChronology                    concept;

   //~--- constructors --------------------------------------------------------

   GetMultiParentTreeItemConceptCallable(MultiParentTreeItem treeItem) {
      this(treeItem, true);
   }

   GetMultiParentTreeItemConceptCallable(MultiParentTreeItem treeItem, boolean addChildren) {
      this.treeItem    = treeItem;
      this.concept     = (treeItem != null) ? treeItem.getValue()
            : null;
      this.addChildren = addChildren;
      updateTitle("Fetching children");
      updateMessage(treeItem.toString());

      if (addChildren) {
         treeItem.childLoadStarts();
      }

      this.updateProgress(0, 3);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public Boolean call()
            throws Exception {
      this.updateProgress(1, 3);

      try {
         // TODO is current value == old value.getRelationshipVersion()?
         if ((treeItem == null) || (treeItem.getValue() == null)) {
            return false;
         }

         if (MultiParentTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
            return false;
         }

         concept = treeItem.getValue();

         if (MultiParentTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
            return false;
         }

         int numParentsFromTree = treeItem.getTreeView()
                                          .getTaxonomyTree()
                                          .getParentSequences(treeItem.getValue()
                                                .getConceptSequence()).length;

         if (numParentsFromTree > 1) {
            treeItem.setMultiParent(true);
         }

         if (addChildren) {
            // TODO it would be nice to show progress here, by binding this status to the
            // progress indicator in the MultiParentTreeItem - However -that progress indicator displays at 16x16,
            // and ProgressIndicator has a bug, that is vanishes for anything other than indeterminate for anything less than 32x32
            // need a progress indicator that works at 16x16
            for (int destRelSequence: treeItem.getTreeView()
                                              .getTaxonomyTree()
                                              .getChildrenSequences(concept.getConceptSequence())) {
               if (MultiParentTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                  return false;
               }

               MultiParentTreeItem childItem = new MultiParentTreeItem(destRelSequence, treeItem.getTreeView());

               if (childItem.shouldDisplay()) {
                  int numParents = treeItem.getTreeView()
                                           .getTaxonomyTree()
                                           .getParentSequences(childItem.getValue()
                                                 .getConceptSequence()).length;

                  if (numParents > 1) {
                     childItem.setMultiParent(true);
                  }

                  childrenToAdd.add(childItem);
               }

               if (MultiParentTreeView.wasGlobalShutdownRequested() || treeItem.isCancelRequested()) {
                  return false;
               }
            }

            Collections.sort(childrenToAdd);
         }

         CountDownLatch temp = new CountDownLatch(1);

         Platform.runLater(
             () -> {
                this.updateProgress(2, 3);

                ConceptChronology itemValue = treeItem.getValue();

                treeItem.setValue(null);

                if (addChildren) {
                   treeItem.getChildren()
                           .clear();
                   treeItem.getChildren()
                           .addAll(childrenToAdd);
                }

                treeItem.setValue(itemValue);
                treeItem.setValue(concept);
                temp.countDown();
             });
         temp.await();
         this.updateProgress(3, 3);
         return true;
      } catch (InterruptedException e) {
         LOG.error("Unexpected", e);
         throw e;
      } finally {
         this.updateProgress(3, 3);
         Get.activeTasks()
            .remove(this);

         if (!MultiParentTreeView.wasGlobalShutdownRequested() &&!treeItem.isCancelRequested()) {
            treeItem.childLoadComplete();
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Node getIcon() {
      return Iconography.TAXONOMY_ICON.getIconographic();
   }
}

