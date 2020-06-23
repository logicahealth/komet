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
package sh.isaac.provider.datastore.navigator;

import java.util.List;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.Edge;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.navigation.Navigator;
import sh.isaac.api.tree.EdgeImpl;

/**
 * Implements the Tree interface by decorating an assemblage with tree functions.  
 * The referenced component of the semantic is the parent. The ComponentNid field of a
 * ComponentNid type semantic is the child. 
 * @author kec
 */
public class ComponentNidAssemblageNavigator implements Navigator {

    private final SingleAssemblageSnapshot<ComponentNidVersion> navigationAssemblage;
    private final int[] treeAssemblageNidAsArray;
    private final ManifoldCoordinate manifoldCoordinate;

    public ComponentNidAssemblageNavigator(SingleAssemblageSnapshot<ComponentNidVersion> navigationAssemblage, ManifoldCoordinate manifoldCoordinate) {
        this.navigationAssemblage = navigationAssemblage;
        this.treeAssemblageNidAsArray = new int[] {navigationAssemblage.getAssemblageNid() };
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public int[] getChildNids(int parentNid) {
        List<LatestVersion<ComponentNidVersion>> children = navigationAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        NidSet childrenNids = new NidSet();
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            childSemantic.ifPresent((semantic) -> {
                if (Get.concept(semantic.getComponentNid()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
                    childrenNids.add(semantic.getComponentNid());
                }
            });
        }
        return childrenNids.asArray();
    }

    @Override
    public int[] getParentNids(int childNid) {
        NidSet parentNids = new NidSet();
        List<SearchResult> matches = Get.indexSemanticService().queryNidReference(childNid, treeAssemblageNidAsArray, null, null, null, null, null, Long.MIN_VALUE);
        for (SearchResult match: matches) {
            int semanticNid = match.getNid();
            navigationAssemblage.getLatestSemanticVersion(semanticNid).ifPresent((t) -> {
                if (Get.concept(t.getReferencedComponentNid()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
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
        List<LatestVersion<ComponentNidVersion>> children = navigationAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            if (childSemantic.isPresent()) {
                if (childSemantic.get().getComponentNid() == childNid &&
                        Get.concept(childSemantic.get().getComponentNid()).getLatestVersion(manifoldCoordinate.getVertexStampFilter()).isPresent()) {
                    return true;
                }
            }
        }
        return false;
   }

    @Override
    public boolean isLeaf(int conceptNid) {
        return getChildNids(conceptNid).length == 0;
    }

    @Override
    public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return manifoldCoordinate;
    }

    @Override
    public ImmutableCollection<Edge> getParentLinks(int parentConceptNid) {
        int[] parentNids = getParentNids(parentConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(parentNids.length);
        for (int parentNid: parentNids) {
            links.add(new EdgeImpl(this.treeAssemblageNidAsArray[0], parentNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getChildLinks(int childConceptNid) {
        int[] childNids = getChildNids(childConceptNid);
        MutableList<Edge> links = Lists.mutable.ofInitialCapacity(childNids.length);
        for (int childNid: childNids) {
            links.add(new EdgeImpl(this.treeAssemblageNidAsArray[0], childNid));
        }
        return links.toImmutable();
    }
}
