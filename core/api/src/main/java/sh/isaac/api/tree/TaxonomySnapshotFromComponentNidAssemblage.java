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
package sh.isaac.api.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.TaxonomyLink;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.SearchResult;

/**
 * Implements the Tree interface by decorating an assemblage with tree functions.  
 * The referenced component of the semantic is the parent. The ComponentNid field of a
 * ComponentNid type semantic is the child. 
 * @author kec
 */
public class TaxonomySnapshotFromComponentNidAssemblage implements TaxonomySnapshot {

    private final SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage;
    private final int[] treeAssemblageNidAsArray;
    private final ManifoldCoordinate manifoldCoordinate;

    public TaxonomySnapshotFromComponentNidAssemblage(SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage, ManifoldCoordinate manifoldCoordinate) {
        this.treeAssemblage = treeAssemblage;
        this.treeAssemblageNidAsArray = new int[] {treeAssemblage.getAssemblageNid() };
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public int[] getTaxonomyChildConceptNids(int parentNid) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        NidSet childrenNids = new NidSet();
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            childSemantic.ifPresent((semantic) -> {
                if (manifoldCoordinate.optionalDestinationStampCoordinate().isPresent() &&
                        Get.concept(semantic.getComponentNid()).getLatestVersion(manifoldCoordinate.optionalDestinationStampCoordinate().get()).isPresent()) {
                    childrenNids.add(semantic.getComponentNid());
                }
            });
        }
        return childrenNids.asArray();
    }

    @Override
    public int[] getTaxonomyParentConceptNids(int childNid) {
        NidSet parentNids = new NidSet();
        List<SearchResult> matches = Get.indexSemanticService().queryNidReference(childNid, treeAssemblageNidAsArray, null, null, null, null, null, Long.MIN_VALUE);
        for (SearchResult match: matches) {
            int semanticNid = match.getNid();
            treeAssemblage.getLatestSemanticVersion(semanticNid).ifPresent((t) -> {
                if (manifoldCoordinate.optionalDestinationStampCoordinate().isPresent() &&
                        Get.concept(t.getReferencedComponentNid()).getLatestVersion(manifoldCoordinate.optionalDestinationStampCoordinate().get()).isPresent()) {
                    parentNids.add(t.getReferencedComponentNid());
                }
            });
        }
        return parentNids.asArray();
    }

    @Override
    public int[] getRootNids() {
        return new int[] {};
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            if (childSemantic.isPresent()) {
                if (manifoldCoordinate.optionalDestinationStampCoordinate().isPresent() && childSemantic.get().getComponentNid() == childNid &&
                        Get.concept(childSemantic.get().getComponentNid()).getLatestVersion(manifoldCoordinate.optionalDestinationStampCoordinate().get()).isPresent()) {
                    return true;
                }
            }
        }
        return false;
   }

    @Override
    public boolean isLeaf(int conceptNid) {
        return getTaxonomyChildConceptNids(conceptNid).length == 0;
    }

    @Override
    public boolean isKindOf(int childConceptNid, int parentConceptNid) {
        throw new UnsupportedOperationException("Not supported by assemblage."); 
    }

    @Override
    public NidSet getKindOfConceptNidSet(int rootConceptNid) {
        throw new UnsupportedOperationException("Not supported by assemblage."); 
    }
    @Override
    public boolean isDescendentOf(int descendantConceptNid, int parentConceptNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree getTaxonomyTree() {
        throw new UnsupportedOperationException("Not supported by assemblage."); 
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    @Override
    public TaxonomySnapshot makeAnalog(ManifoldCoordinate manifoldCoordinate) {
        return new TaxonomySnapshotFromComponentNidAssemblage(treeAssemblage.makeAnalog(manifoldCoordinate), manifoldCoordinate);
    }

    @Override
    public Collection<TaxonomyLink> getTaxonomyParentLinks(int parentConceptNid) {
        int[] parentNids = getTaxonomyParentConceptNids(parentConceptNid);
        ArrayList<TaxonomyLink> links = new ArrayList<>(parentNids.length);
        for (int parentNid: parentNids) {
            links.add(new TaxonomyLinkage(this.treeAssemblageNidAsArray[0], parentNid));
        }
        return links;
    }

    @Override
    public Collection<TaxonomyLink> getTaxonomyChildLinks(int childConceptNid) {
        int[] childNids = getTaxonomyChildConceptNids(childConceptNid);
        ArrayList<TaxonomyLink> links = new ArrayList<>(childNids.length);
        for (int childNid: childNids) {
            links.add(new TaxonomyLinkage(this.treeAssemblageNidAsArray[0], childNid));
        }
        return links;
    }
}
