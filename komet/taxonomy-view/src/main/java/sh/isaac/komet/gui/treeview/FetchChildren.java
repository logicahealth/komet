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
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
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

    private final CountDownLatch childrenLoadedLatch;
    private final MultiParentTreeItemImpl treeItemImpl;

    public FetchChildren(CountDownLatch childrenLoadedLatch,
            MultiParentTreeItemImpl treeItemImpl) {
        this.childrenLoadedLatch = childrenLoadedLatch;
        this.treeItemImpl = treeItemImpl;
        updateTitle("Fetching children for: " + treeItemImpl.getTreeView()
                .getManifold().getPreferredDescriptionText(treeItemImpl.getValue()));
        Get.activeTasks().add(this);
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
                addToTotalWork(children.length);
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
                }

                Platform.runLater(
                        () -> {
                            treeItemImpl.getChildren().addAll(childrenToAdd);
                        });
            }
            return null;
        } finally {
            childrenLoadedLatch.countDown();
            Get.activeTasks().remove(this);
        }
    }

}
