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
import java.util.Optional;
import java.util.UUID;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import static sh.isaac.komet.gui.treeview.MultiParentTreeView.wasGlobalShutdownRequested;

/**
 *
 * @author kec
 */
public class ShowConceptInTaxonomyTask extends TimedTaskWithProgressTracker<MultiParentTreeItemImpl> {

    private final MultiParentTreeView multiParentTreeView;
    private final UUID conceptUUID;

    public ShowConceptInTaxonomyTask(MultiParentTreeView multiParentTreeView, UUID conceptUUID) {
        this.multiParentTreeView = multiParentTreeView;
        this.conceptUUID = conceptUUID;
        Get.activeTasks().add(this);
    }

    @Override
    protected MultiParentTreeItemImpl call()
            throws Exception {
        try {
        // await() init() completion.
        LOG.debug("Looking for concept {} in tree", conceptUUID);

        final ArrayList<UUID> pathToRoot = new ArrayList<>();

        pathToRoot.add(conceptUUID);

        // Walk up taxonomy to origin until no parent found.
        UUID current = conceptUUID;

        while (true) {
            Optional<? extends ConceptChronology> conceptOptional = Get.conceptService()
                    .getOptionalConcept(current);

            if (!conceptOptional.isPresent()) {
                // Must be a "pending concept".
                // Not handled yet.
                return null;
            }

            ConceptChronology concept = conceptOptional.get();

            // Look for an IS_A relationship to origin.
            boolean found = false;

            for (int parent : multiParentTreeView.getTaxonomySnapshot().getTaxonomyParentConceptNids(concept.getNid())) {
                current = Get.identifierService()
                        .getUuidPrimordialForNid(parent)
                        .get();
                pathToRoot.add(current);
                found = true;
                break;
            }

            // No parent IS_A relationship found, stop looking.
            if (!found) {
                break;
            }
        }

        LOG.debug("Calculated root path {}", Arrays.toString(pathToRoot.toArray()));

        MultiParentTreeItemImpl currentTreeItem = multiParentTreeView.getRoot();

        // Walk down path from root.
        for (int i = pathToRoot.size() - 1; i >= 0; i--) {
            MultiParentTreeItemImpl child = multiParentTreeView.findChild(currentTreeItem, pathToRoot.get(i));

            if (child == null) {
                break;
            }

            currentTreeItem = child;
        }

        return currentTreeItem;
        } finally {
            Get.activeTasks().remove(this);
        }
    }

    @Override
    protected void succeeded() {
        final MultiParentTreeItemImpl lastItemFound = this.getValue();

        // Expand tree to last item found.
        if (lastItemFound != null) {
            int row = multiParentTreeView.getTreeView().getRow(lastItemFound);

            multiParentTreeView.getTreeView().scrollTo(row);
            multiParentTreeView.getTreeView().getSelectionModel()
                    .clearAndSelect(row);
        }
    }

    @Override
    protected void failed() {
        Throwable ex = getException();

        if (!wasGlobalShutdownRequested()) {
            LOG.warn("Unexpected error trying to find concept in Tree", ex);

        }
    }
}
