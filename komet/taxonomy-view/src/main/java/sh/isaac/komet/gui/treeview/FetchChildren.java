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
package sh.isaac.komet.gui.treeview;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshotService;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class FetchChildren extends TimedTaskWithProgressTracker<Void> {
   private static final AtomicInteger FETCHER_SEQUENCE = new AtomicInteger(1);
   private static final ConcurrentHashMap<Integer, FetchChildren> FETCHER_MAP = new ConcurrentHashMap<>();

   private static final int CHILD_BATCH_SIZE = 25;
    private final CountDownLatch childrenLoadedLatch;
    private final MultiParentTreeItemImpl treeItemImpl;
    private final int fetcherId = FETCHER_SEQUENCE.incrementAndGet();

    public FetchChildren(CountDownLatch childrenLoadedLatch,
            MultiParentTreeItemImpl treeItemImpl) {
        this.childrenLoadedLatch = childrenLoadedLatch;
        this.treeItemImpl = treeItemImpl;
        updateTitle("Fetching children for: " + treeItemImpl.getTreeView()
                .getManifold().getPreferredDescriptionText(treeItemImpl.getValue()));
        Get.activeTasks().add(this);
        LOG.debug("###Starting Adding children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
        
        FetchChildren oldFetcher = FETCHER_MAP.put(treeItemImpl.getValue().getNid(), this);
        
        if (oldFetcher != null) {
            oldFetcher.cancel(false);  //Interrupts are bad for code that uses NIO.  
            Get.activeTasks().remove(oldFetcher);
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            final ConceptChronology conceptChronology = treeItemImpl.getValue();

            if (conceptChronology == null) {
                LOG.debug("addChildren(): conceptChronology={}", conceptChronology);
            } else {  // if (conceptChronology != null)
                // Gather the children
                TreeSet<MultiParentTreeItemImpl> childrenToAdd = new TreeSet<>();
                TaxonomySnapshotService taxonomySnapshot = treeItemImpl.getTreeView().getTaxonomySnapshot();
                Manifold manifold = treeItemImpl.getTreeView().getManifold();
                int[]  children = taxonomySnapshot.getTaxonomyChildConceptNids(conceptChronology.getNid());
                int batchCount = children.length/CHILD_BATCH_SIZE;
                addToTotalWork(children.length + batchCount);
                
                
                for (int childNid : children) {
                    ConceptChronology childChronology = Get.concept(childNid);
                    MultiParentTreeItemImpl childItem = new MultiParentTreeItemImpl(childChronology, treeItemImpl.getTreeView(), null);
                    childItem.setDefined(childChronology.isSufficientlyDefined(manifold, manifold));
                    childItem.toString();
                    childItem.setMultiParent(taxonomySnapshot.getTaxonomyParentConceptNids(childNid).length > 1);
                    childItem.isLeaf();

                    if (childItem.shouldDisplay()) {
                        childrenToAdd.add(childItem);
                    } else {
                        LOG.debug(
                                "item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of "
                                + treeItemImpl.getConceptUuid());
                    }
                    completedUnitOfWork();
                    if (isCancelled()) return null;
                }
                
                int counter = 0;
                ArrayList<ArrayList<MultiParentTreeItemImpl>> itemListList = new ArrayList<>();
                ArrayList<MultiParentTreeItemImpl> itemList = new ArrayList<>();
                itemListList.add(itemList);
                for (MultiParentTreeItemImpl treeItem: childrenToAdd) {
                    
                    if (counter <= CHILD_BATCH_SIZE) {
                        counter++;
                        itemList.add(treeItem);
                    } else {
                        counter = 0;
                        itemList = new ArrayList<>();
                        itemListList.add(itemList);
                    }
                    if (isCancelled()) return null;
                }
                    Platform.runLater(
                        () -> {
                            if (!FetchChildren.this.isCancelled()) {
                                LOG.debug("###Clearing children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
                                treeItemImpl.getChildren().clear();
                                completedUnitOfWork();
                            }
                            
                        });
                
                for (ArrayList<MultiParentTreeItemImpl> items: itemListList) {
                    if (isCancelled()) return null;
                    Platform.runLater(
                        () -> {
                            if (!FetchChildren.this.isCancelled()) {
                                LOG.debug("###Adding children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
                                treeItemImpl.getChildren().addAll(items);
                                completedUnitOfWork();
                            }
                            
                        });
                }
                    

            }
            return null;
        } finally {
            childrenLoadedLatch.countDown();
            Get.activeTasks().remove(this);
            FETCHER_MAP.remove(treeItemImpl.getValue().getNid());
            if (FetchChildren.this.isCancelled()) {
                LOG.debug("###Canceled Adding children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
            } else {
                LOG.debug("###Finished Adding children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
            }
        }
    }

}
