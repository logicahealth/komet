/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.komet.gui.treeview;

import sh.komet.gui.interfaces.MultiParentTreeItemDisplayPolicies;
import sh.komet.gui.interfaces.MultiParentTreeItemI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.util.AlphanumComparator;

/**
 * A {@link TreeItem} for modeling nodes in ISAAC taxonomies.
 *
 * The {@code MultiParentTreeItem} is not a visual component. The 
 * {@code MultiParentTreeCell} provides the rendering for this
 * tree item. 
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @see MultiParentTreeCell
 */
public class MultiParentTreeItem extends TreeItem<ConceptChronology<? extends ConceptVersion<?>>> 
        implements MultiParentTreeItemI, Comparable<MultiParentTreeItem> {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   private final List<MultiParentTreeItem> extraParents = new ArrayList<>();
   private CountDownLatch childrenLoadedLatch = new CountDownLatch(1);
   //-2 when not yet started, -1 for started indeterminate - between 0 and 1, if we can determine, 1 when complete.
   private final DoubleProperty childLoadPercentComplete = new SimpleDoubleProperty(-2.0);
   private volatile boolean cancelLookup = false;
   private boolean defined = false;
   private boolean multiParent = false;
   private int multiParentDepth = 0;
   private boolean secondaryParentOpened = false;
   private MultiParentTreeView treeView;

   public MultiParentTreeView getTreeView() {
      return treeView;
   }

   private static TreeItem<ConceptChronology<? extends ConceptVersion<?>>> getTreeRoot(TreeItem<ConceptChronology<? extends ConceptVersion<?>>> item) {
      TreeItem<ConceptChronology<? extends ConceptVersion<?>>> parent = item.getParent();

      if (parent == null) {
         return item;
      } else {
         return getTreeRoot(parent);
      }
   }

   MultiParentTreeItem(int conceptSequence, MultiParentTreeView treeView) {
      this(Get.conceptService().getConcept(conceptSequence), treeView, null);
   }
   MultiParentTreeItem(int conceptSequence, MultiParentTreeView treeView, Node graphic) {
      this(Get.conceptService().getConcept(conceptSequence), treeView, graphic);
   }

   MultiParentTreeItem(ConceptChronology<? extends ConceptVersion<?>> conceptChronology, MultiParentTreeView treeView, Node graphic) {
      super(conceptChronology, graphic);
      this.treeView = treeView;
   }

   MultiParentTreeItemDisplayPolicies getDisplayPolicies() {
      return this.treeView.getDisplayPolicies();
   }

   void addChildren() {
      childLoadStarts();
      try {
         final ConceptChronology<? extends ConceptVersion<?>> conceptChronology = getValue();
         if (!shouldDisplay()) {
            // Don't add children to something that shouldn't be displayed
            LOG.debug("this.shouldDisplay() == false: not adding children to " + this.getConceptUuid());
         } else if (conceptChronology == null) {
            LOG.debug("addChildren(): conceptChronology={}", conceptChronology);
         } else { // if (conceptChronology != null)
            // Gather the children
            ArrayList<MultiParentTreeItem> childrenToAdd = new ArrayList<>();
            ArrayList<GetMultiParentTreeItemConceptCallable> childrenToProcess = new ArrayList<>();

            for (int childSequence : treeView.getTaxonomyTree().getChildrenSequences(conceptChronology.getConceptSequence())) {
               MultiParentTreeItem childItem = new MultiParentTreeItem(childSequence, treeView);
               if (childItem.shouldDisplay()) {
                  childrenToAdd.add(childItem);
                  childrenToProcess.add(new GetMultiParentTreeItemConceptCallable(childItem));
               } else {
                  LOG.debug("item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of " + this.getConceptUuid());
               }
            }

            Collections.sort(childrenToAdd);
            if (cancelLookup) {
               return;
            }

            Platform.runLater(()
                    -> {
               getChildren().addAll(childrenToAdd);
            });
            //This loads the children of this child
            childrenToProcess.forEach((child) -> {
               Get.workExecutors().getPotentiallyBlockingExecutor().execute(child);
            });

         }
      } catch (Exception e) {
         LOG.error("Unexpected error computing children and/or grandchildren", e);
      } finally {
         childLoadComplete();
      }
   }

   void addChildrenConceptsAndGrandchildrenItems() {
      ArrayList<GetMultiParentTreeItemConceptCallable> grandChildrenToProcess = new ArrayList<>();
      childLoadStarts();
      try {
         if (!shouldDisplay()) {
            // Don't add children to something that shouldn't be displayed
            LOG.debug("this.shouldDisplay() == false: not adding children concepts and grandchildren items to " + this.getConceptUuid());
         } else {
            for (TreeItem<ConceptChronology<? extends ConceptVersion<?>>> child : getChildren()) {
               if (cancelLookup) {
                  return;
               }
               if (((MultiParentTreeItem) child).shouldDisplay()) {
                  if (child.getChildren().isEmpty() && (child.getValue() != null)) {
                     if (treeView.getTaxonomyTree().getChildrenSequences(child.getValue().getConceptSequence()).length == 0) {
                        ConceptChronology<? extends ConceptVersion<?>> value = child.getValue();
                        child.setValue(null);
                        MultiParentTreeItem noChildItem = (MultiParentTreeItem) child;
                        noChildItem.computeGraphic();
                        noChildItem.setValue(value);
                     } else if (((MultiParentTreeItem) child).getChildLoadPercentComplete().get() == -2.0) { //If this child hasn't yet been told to load
                        ArrayList<MultiParentTreeItem> grandChildrenToAdd = new ArrayList<>();
                        ((MultiParentTreeItem) child).childLoadStarts();

                        for (int childSequence : treeView.getTaxonomyTree().getChildrenSequences(child.getValue().getConceptSequence())) {
                           if (cancelLookup) {
                              return;
                           }
                           MultiParentTreeItem grandChildItem = new MultiParentTreeItem(childSequence, treeView);

                           if (grandChildItem.shouldDisplay()) {
                              grandChildrenToProcess.add(new GetMultiParentTreeItemConceptCallable(grandChildItem));
                              grandChildrenToAdd.add(grandChildItem);
                           } else {
                              LOG.debug("grandChildItem.shouldDisplay() == false: not adding " + grandChildItem.getConceptUuid() + " as child of " + ((MultiParentTreeItem) child).getConceptUuid());
                           }
                        }

                        Collections.sort(grandChildrenToAdd);
                        if (cancelLookup) {
                           return;
                        }

                        CountDownLatch wait = new CountDownLatch(1);
                        Platform.runLater(()
                                -> {
                           child.getChildren().addAll(grandChildrenToAdd);
                           ((MultiParentTreeItem) child).childLoadComplete();
                           wait.countDown();
                        });
                        wait.await();
                     }
                  } else if ((child.getValue() == null) && ((MultiParentTreeItem) child).getChildLoadPercentComplete().get() == -2.0) {
                     grandChildrenToProcess.add(new GetMultiParentTreeItemConceptCallable((MultiParentTreeItem) child));
                  }
               } else {
                  LOG.debug("childItem.shouldDisplay() == false: not adding " + ((MultiParentTreeItem) child).getConceptUuid() + " as child of " + this.getConceptUuid());
               }
            }

            if (cancelLookup) {
               return;
            }

            //This loads the childrens children
            grandChildrenToProcess.forEach((childsChild) -> {
               Get.workExecutors().getPotentiallyBlockingExecutor().execute(childsChild);
            });
         }
      } catch (InterruptedException e) {
         LOG.error("Unexpected error computing children and/or grandchildren", e);
      } finally {
         childLoadComplete();
      }
   }

   @Override
   public int compareTo(MultiParentTreeItem o) {
      return AlphanumComparator.compare(this.toString(), o.toString(), true);
   }

   public UUID getConceptUuid() {
      return getValue() != null ? getValue().getPrimordialUuid() : null;
   }

   @Override
   public int getConceptNid() {
      return getValue() != null ? getValue().getNid() : Integer.MIN_VALUE;
   }

   private static int getConceptNid(TreeItem<ConceptChronology<? extends ConceptVersion<?>>> item) {
      return item != null && item.getValue() != null ? item.getValue().getNid() : null;
   }

   @Override
   public boolean isRoot() {
      if (MetaData.ISAAC_ROOT.getPrimordialUuid().equals(this.getConceptUuid())) {
         return true;
      } else if (this.getParent() == null) {
         return true;
      } else {
         TreeItem<ConceptChronology<? extends ConceptVersion<?>>> root = getTreeRoot(this);

         if (this == root) {
            return true;
         } else {
            return getConceptNid(root) == getConceptNid();
         }
      }
   }

   public Node computeGraphic() {
      return treeView.getDisplayPolicies().computeGraphic(this);
   }

   public boolean shouldDisplay() {
      return treeView.getDisplayPolicies().shouldDisplay(this);
   }

   /**
    * @see javafx.scene.control.TreeItem#toString() WARNING: toString is currently used in compareTo()
    */
   @Override
   public String toString() {
      try {
         if (this.getValue() != null) {
            return treeView.manifoldProperty.get().getConceptSnapshotService().conceptDescriptionText(this.getValue().getNid());
         }
         return "root";
      } catch (RuntimeException | Error re) {
         LOG.error("Caught {} \"{}\"", re.getClass().getName(), re.getLocalizedMessage());
         throw re;
      }
   }

   public List<MultiParentTreeItem> getExtraParents() {
      return extraParents;
   }

   @Override
   public int getMultiParentDepth() {
      return multiParentDepth;
   }

   /**
    * returns -2 when not yet started, -1 when started, but indeterminate otherwise, a value between 0 and 1 (1 when
    * complete)
    */
   public DoubleProperty getChildLoadPercentComplete() {
      return childLoadPercentComplete;
   }

   @Override
   public boolean isDefined() {
      return defined;
   }

   @Override
   public boolean isLeaf() {
      if (multiParentDepth > 0) {
         return true;
      }

      return super.isLeaf();
   }

   @Override
   public boolean isMultiParent() {
      return multiParent;
   }

   @Override
   public boolean isSecondaryParentOpened() {
      return secondaryParentOpened;
   }

   public void setDefined(boolean defined) {
      this.defined = defined;
   }

   public void setMultiParent(boolean multiParent) {
      this.multiParent = multiParent;
   }

   public void setMultiParentDepth(int multiParentDepth) {
      this.multiParentDepth = multiParentDepth;
   }

   public void setSecondaryParentOpened(boolean secondaryParentOpened) {
      this.secondaryParentOpened = secondaryParentOpened;
   }

   public void blockUntilChildrenReady() throws InterruptedException {
      childrenLoadedLatch.await();
   }

   public void clearChildren() {
      cancelLookup = true;
      childrenLoadedLatch.countDown();
      getChildren().forEach((child) -> {
         ((MultiParentTreeItem) child).clearChildren();
      });
      getChildren().clear();
   }

   protected void resetChildrenCalculators() {
      CountDownLatch cdl = new CountDownLatch(1);
      Runnable r = () -> {
         cancelLookup = false;
         childLoadPercentComplete.set(-2);
         childrenLoadedLatch.countDown();
         childrenLoadedLatch = new CountDownLatch(1);
         cdl.countDown();
      };
      if (Platform.isFxApplicationThread()) {
         r.run();
      } else {
         Platform.runLater(r);
      }
      try {
         cdl.await();
      } catch (InterruptedException e) {
         LOG.error("unexpected interrupt", e);
      }
   }

   public void removeGrandchildren() {
      getChildren().stream().map((child) -> {
         ((MultiParentTreeItem) child).clearChildren();
         return child;
      }).forEachOrdered((child) -> {
         ((MultiParentTreeItem) child).resetChildrenCalculators();
      });
   }

   /**
    * Can be called on either a background or the FX thread
    */
   protected void childLoadStarts() {
      CountDownLatch cdl = new CountDownLatch(1);
      Runnable r = () -> {
         childLoadPercentComplete.set(-1);
         cdl.countDown();
      };
      if (Platform.isFxApplicationThread()) {
         r.run();
      } else {
         Platform.runLater(r);
      }
      try {
         cdl.await();
      } catch (InterruptedException e) {
         LOG.error("unexpected interrupt", e);
      }
   }

   /**
    * Can be called on either a background or the FX thread
    */
   protected void childLoadComplete() {
      Runnable r = () -> {
         childLoadPercentComplete.set(1.0);
         childrenLoadedLatch.countDown();
      };
      if (Platform.isFxApplicationThread()) {
         r.run();
      } else {
         Platform.runLater(r);
      }
   }

   protected boolean isCancelRequested() {
      return cancelLookup;
   }
}
