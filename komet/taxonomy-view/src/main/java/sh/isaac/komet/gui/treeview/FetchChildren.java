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

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomyLink;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.task.TaskCountManager;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.komet.gui.manifold.Manifold;
import sh.isaac.api.TaxonomySnapshot;
import sh.komet.gui.util.FxGet;

/**
 *
 * @author kec
 */
public class FetchChildren extends TimedTaskWithProgressTracker<Void> {
   private static final AtomicInteger FETCHER_SEQUENCE = new AtomicInteger(1);
   private static final ConcurrentHashMap<Integer, FetchChildren> FETCHER_MAP = new ConcurrentHashMap<>();

    private final CountDownLatch childrenLoadedLatch;
    private final MultiParentTreeItemImpl treeItemImpl;
    private final int fetcherId = FETCHER_SEQUENCE.incrementAndGet();
    private int childrenFound = 0;
    private final String parentName;

    public FetchChildren(CountDownLatch childrenLoadedLatch,
            MultiParentTreeItemImpl treeItemImpl) {
        this.childrenLoadedLatch = childrenLoadedLatch;
        this.treeItemImpl = treeItemImpl;
        this.parentName = treeItemImpl.getTreeView()
                .getManifold().getPreferredDescriptionText(treeItemImpl.getValue());
        updateTitle("Fetching children for: " + this.parentName);
        Get.activeTasks().add(this);
        LOG.debug("###Starting Adding children for: " + treeItemImpl.getValue().getNid()
                                    + " from: " + fetcherId);
        
        FetchChildren oldFetcher = FETCHER_MAP.put(treeItemImpl.getValue().getNid(), this);
        
        if (oldFetcher != null) {
            oldFetcher.cancel(false);  //Interrupts are bad for code that uses NIO.  
            Get.activeTasks().remove(oldFetcher);
        }
        this.setCompleteMessageGenerator((task) -> {
            String message = "Found " + childrenFound + " children in " + getFormattedDuration() + " for " + this.parentName;
            updateMessage(message);
            FxGet.statusMessageService().reportStatus(message);
        });
    }

    @Override
    protected Void call() throws Exception {
        try {
            final ConceptChronology conceptChronology = treeItemImpl.getValue();

            if (conceptChronology == null) {
                LOG.debug("addChildren(): conceptChronology={}", conceptChronology);
            } else {  // if (conceptChronology != null)
                // Gather the children
                ConcurrentSkipListSet<MultiParentTreeItemImpl> childrenToAdd = new ConcurrentSkipListSet<>();
                TaxonomySnapshot taxonomySnapshot = treeItemImpl.getTreeView().getTaxonomySnapshot();
                Manifold manifold = treeItemImpl.getTreeView().getManifold();
                Collection<TaxonomyLink>  children = taxonomySnapshot.getTaxonomyChildLinks(conceptChronology.getNid());
                addToTotalWork(children.size() + 1);

                TaskCountManager taskCountManager = Get.taskCountManager();
                for (TaxonomyLink childLink : children) {
                    taskCountManager.acquire();
                    Get.executor().execute(() -> {
                        try {
                            ConceptChronology childChronology = Get.concept(childLink.getDestinationNid());
                            MultiParentTreeItemImpl childItem = new MultiParentTreeItemImpl(childChronology, treeItemImpl.getTreeView(), childLink.getTypeNid(), null);
                            childItem.setDefined(childChronology.isSufficientlyDefined(manifold, manifold));
                            childItem.toString();
                            childItem.setMultiParent(taxonomySnapshot.getTaxonomyParentConceptNids(childLink.getDestinationNid()).length > 1);
                            childItem.isLeaf();

                            if (childItem.shouldDisplay()) {
                                childrenToAdd.add(childItem);
                            } else {
                                LOG.debug(
                                        "item.shouldDisplay() == false: not adding " + childItem.getConceptUuid() + " as child of "
                                                + treeItemImpl.getConceptUuid());
                            }
                        } finally {
                            taskCountManager.release();
                        }
                    });

                    completedUnitOfWork();
                    if (isCancelled()) return null;
                }
                taskCountManager.waitForCompletion();
                if (isCancelled()) return null;
                Platform.runLater(() -> {
                    treeItemImpl.setExpanded(false);
                });
                Platform.runLater(
                        () -> {
                            if (!FetchChildren.this.isCancelled()) {
                                LOG.debug("###Adding children for: " + treeItemImpl.getValue().getNid()
                                        + " from: " + fetcherId);
                                treeItemImpl.getChildren().setAll(childrenToAdd);
                                treeItemImpl.setExpanded(true);
                                completedUnitOfWork();
                            }

                        });

                childrenFound = childrenToAdd.size();
            }

            return null;
        } finally {
            this.done();
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
