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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

//~--- non-JDK imports --------------------------------------------------------

import javafx.application.Platform;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.util.NaturalOrder;
import sh.komet.gui.manifold.Manifold;

//~--- classes ----------------------------------------------------------------

/**
 * A {@link TreeItem} for modeling nodes in ISAAC taxonomies.
 *
 * The {@code MultiParentTreeItemImpl} is not a visual component. The {@code MultiParentTreeCell} provides the rendering for
 * this tree item.
 *
 * @author kec
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @see MultiParentTreeCell
 */
public class MultiParentTreeItemImpl
        extends TreeItem<ConceptChronology>
         implements MultiParentTreeItem, Comparable<MultiParentTreeItemImpl> {
   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private final List<MultiParentTreeItemImpl> extraParents        = new ArrayList<>();
   private CountDownLatch                      childrenLoadedLatch = new CountDownLatch(1);

   // -2 when not yet started, -1 for started indeterminate - between 0 and 1, if we can determine, 1 when complete.
   private final DoubleProperty childLoadPercentComplete = new SimpleDoubleProperty(-2.0);
   private volatile boolean     cancelLookup             = false;
   private boolean              defined                  = false;
   private boolean              multiParent              = false;
   private int                  multiParentDepth         = 0;
   private boolean              secondaryParentOpened    = false;
   private MultiParentTreeView  treeView;
   private String               conceptDescriptionText;  // Cached to speed up comparisons with toString method.
   private final int            nid;
   private int[] childNids;

   //~--- constructors --------------------------------------------------------

   MultiParentTreeItemImpl(int conceptSequence, MultiParentTreeView treeView) {
      this(Get.conceptService()
              .getConceptChronology(conceptSequence), treeView, null);
   }

   MultiParentTreeItemImpl(ConceptChronology conceptChronology, MultiParentTreeView treeView, Node graphic) {
      super(conceptChronology, graphic);
      this.treeView = treeView;
      this.nid      = conceptChronology.getNid();
   }

   //~--- methods -------------------------------------------------------------

   public void blockUntilChildrenReady()
            throws InterruptedException {
      childrenLoadedLatch.await();
   }

   public void clearChildren() {
      cancelLookup = true;
      childrenLoadedLatch.countDown();
      getChildren().forEach(
          (child) -> {
             ((MultiParentTreeItemImpl) child).clearChildren();
          });
      getChildren().clear();
   }

   @Override
   public int compareTo(MultiParentTreeItemImpl o) {
      return NaturalOrder.compareStrings(this.toString(), o.toString());
   }

   public Node computeGraphic() {
      return treeView.getDisplayPolicies()
                     .computeGraphic(this);
   }

   public void invalidate() {
      this.conceptDescriptionText = null;

      for (TreeItem<ConceptChronology> child: getChildren()) {
         MultiParentTreeItemImpl multiParentTreeItem = (MultiParentTreeItemImpl) child;

         multiParentTreeItem.invalidate();
      }
   }

   public void removeChildren() {
      this.getChildren()
          .clear();
   }

   public boolean shouldDisplay() {
      return treeView.getDisplayPolicies()
                     .shouldDisplay(this);
   }

   /**
    * @see javafx.scene.control.TreeItem#toString() WARNING: toString is currently used in compareTo()
    */
   @Override
   public String toString() {
      try {
         if (this.getValue() != null) {
            if ((conceptDescriptionText == null) || conceptDescriptionText.startsWith("no description for ")) {
               LatestVersion<String> latestDescriptionText = treeView.getManifold()
                                                                     .getDescriptionText(nid);

               latestDescriptionText.ifPresent((descriptionText) -> this.conceptDescriptionText = descriptionText)
                                    .ifAbsent(() -> this.conceptDescriptionText = "no description for " + nid);
            }

            return this.conceptDescriptionText;
         }

         return "root";
      } catch (RuntimeException | Error re) {
         LOG.error("Caught {} \"{}\"", re.getClass()
                                         .getName(), re.getLocalizedMessage());
         throw re;
      }
   }

   void addChildrenNow() {
      if (getChildren().isEmpty()) {
         childLoadStarts();

         try {
            final ConceptChronology conceptChronology = getValue();

            if (!shouldDisplay()) {
               // Don't add children to something that shouldn't be displayed
               LOG.debug("this.shouldDisplay() == false: not adding children to " + this.getConceptUuid());
            } else if (conceptChronology == null) {
               LOG.debug("addChildren(): conceptChronology={}", conceptChronology);
            } else {  // if (conceptChronology != null)
               // Gather the children
               ArrayList<MultiParentTreeItemImpl> childrenToAdd    = new ArrayList<>();
               TaxonomySnapshotService            taxonomySnapshot = treeView.getTaxonomySnapshot();

               for (int childNid: taxonomySnapshot.getTaxonomyChildConceptNids(conceptChronology.getNid())) {
                  ConceptChronology childChronology = Get.concept(childNid);
                  MultiParentTreeItemImpl childItem = new MultiParentTreeItemImpl(childChronology, treeView, null);
                  Manifold manifold = treeView.getManifold();
                  childItem.setDefined(childChronology.isSufficientlyDefined(manifold, manifold));
                  childItem.toString();
                  childItem.setMultiParent(taxonomySnapshot.getTaxonomyParentConceptNids(childNid).length > 1);

                  if (childItem.shouldDisplay()) {
                     childrenToAdd.add(childItem);
                  } else {
                     LOG.debug(
                         "item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of " +
                         this.getConceptUuid());
                  }
               }

               Collections.sort(childrenToAdd);

               if (cancelLookup) {
                  return;
               }
               getChildren().addAll(childrenToAdd);
            }
         } catch (Exception e) {
            LOG.error("Unexpected error computing children and/or grandchildren for " + this.conceptDescriptionText, e);
         } finally {
            childLoadComplete();
         }
      }
   }
   void addChildren() {
      if (getChildren().isEmpty()) {
         childLoadStarts();

         try {
            final ConceptChronology conceptChronology = getValue();

            if (!shouldDisplay()) {
               // Don't add children to something that shouldn't be displayed
               LOG.debug("this.shouldDisplay() == false: not adding children to " + this.getConceptUuid());
            } else if (conceptChronology == null) {
               LOG.debug("addChildren(): conceptChronology={}", conceptChronology);
            } else {  // if (conceptChronology != null)
               // Gather the children
               ArrayList<MultiParentTreeItemImpl> childrenToAdd    = new ArrayList<>();
               TaxonomySnapshotService            taxonomySnapshot = treeView.getTaxonomySnapshot();

               for (int childNid: taxonomySnapshot.getTaxonomyChildConceptNids(conceptChronology.getNid())) {
                  ConceptChronology childChronology = Get.concept(childNid);
                  MultiParentTreeItemImpl childItem = new MultiParentTreeItemImpl(childChronology, treeView, null);
                  Manifold manifold = treeView.getManifold();
                  childItem.setDefined(childChronology.isSufficientlyDefined(manifold, manifold));
                  childItem.toString();
                  childItem.setMultiParent(taxonomySnapshot.getTaxonomyParentConceptNids(childNid).length > 1);

                  if (childItem.shouldDisplay()) {
                     childrenToAdd.add(childItem);
                  } else {
                     LOG.debug(
                         "item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of " +
                         this.getConceptUuid());
                  }
               }

               Collections.sort(childrenToAdd);

               if (cancelLookup) {
                  return;
               }

               Platform.runLater(
                   () -> {
                      getChildren().addAll(childrenToAdd);
                   });
            }
         } catch (Exception e) {
            LOG.error("Unexpected error computing children and/or grandchildren for " + this.conceptDescriptionText, e);
         } finally {
            childLoadComplete();
         }
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

   /**
    * Can be called on either a background or the FX thread
    */
   protected void childLoadStarts() {
      CountDownLatch cdl = new CountDownLatch(1);
      Runnable       r   = () -> {
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

   protected void resetChildrenCalculators() {
      CountDownLatch cdl = new CountDownLatch(1);
      Runnable       r   = () -> {
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

   //~--- get methods ---------------------------------------------------------

   protected boolean isCancelRequested() {
      return cancelLookup;
   }

   /**
    * returns -2 when not yet started, -1 when started, but indeterminate otherwise, a value between 0 and 1 (1 when
    * complete)
    *
    * @return the percent load complete.
    */
   public DoubleProperty getChildLoadPercentComplete() {
      return childLoadPercentComplete;
   }

   @Override
   public int getConceptNid() {
      return (getValue() != null) ? getValue().getNid()
                                  : Integer.MIN_VALUE;
   }

   private static int getConceptNid(TreeItem<ConceptChronology> item) {
      return ((item != null) && (item.getValue() != null)) ? item.getValue()
            .getNid()
            : null;
   }

   public UUID getConceptUuid() {
      return (getValue() != null) ? getValue().getPrimordialUuid()
                                  : null;
   }

   @Override
   public boolean isDefined() {
      return defined;
   }

   //~--- set methods ---------------------------------------------------------

   public void setDefined(boolean defined) {
      this.defined = defined;
   }

   //~--- get methods ---------------------------------------------------------

   MultiParentTreeItemDisplayPolicies getDisplayPolicies() {
      return this.treeView.getDisplayPolicies();
   }

   public List<MultiParentTreeItemImpl> getExtraParents() {
      return extraParents;
   }

   @Override
   public boolean isLeaf() {
      if (multiParentDepth > 0) {
         return true;
      }
      if (this.childNids == null) {
          this.childNids = this.treeView.getTaxonomySnapshot().getTaxonomyChildConceptNids(nid);
      }

      return this.childNids.length == 0;
   }

   @Override
   public boolean isMultiParent() {
      return multiParent;
   }

   //~--- set methods ---------------------------------------------------------

   public void setMultiParent(boolean multiParent) {
      this.multiParent = multiParent;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public int getMultiParentDepth() {
      return multiParentDepth;
   }

   //~--- set methods ---------------------------------------------------------

   public void setMultiParentDepth(int multiParentDepth) {
      this.multiParentDepth = multiParentDepth;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean isRoot() {
      if (MetaData.SOLOR_CONCEPT____SOLOR.isIdentifiedBy(this.getConceptUuid())) {
         return true;
      } else if (this.getParent() == null) {
         return true;
      } else {
         TreeItem<ConceptChronology> root = getTreeRoot(this);

         if (this == root) {
            return true;
         } else {
            return getConceptNid(root) == getConceptNid();
         }
      }
   }

   @Override
   public boolean isSecondaryParentOpened() {
      return secondaryParentOpened;
   }

   //~--- set methods ---------------------------------------------------------

   public void setSecondaryParentOpened(boolean secondaryParentOpened) {
      this.secondaryParentOpened = secondaryParentOpened;
   }

   //~--- get methods ---------------------------------------------------------

   private static TreeItem<ConceptChronology> getTreeRoot(TreeItem<ConceptChronology> item) {
      TreeItem<ConceptChronology> parent = item.getParent();

      if (parent == null) {
         return item;
      } else {
         return getTreeRoot(parent);
      }
   }

   public MultiParentTreeView getTreeView() {
      return treeView;
   }
}

