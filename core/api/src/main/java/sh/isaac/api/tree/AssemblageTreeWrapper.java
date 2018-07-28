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

import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import sh.isaac.api.Get;
import sh.isaac.api.SingleAssemblageSnapshot;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.stream.VersionStream;

/**
 * Implements the Tree interface by decorating an assemblage with tree functions.  
 * The referenced component of the semantic is the parent. The ComponentNid field of a
 * ComponentNid type semantic is the child. 
 * @author kec
 */
public class AssemblageTreeWrapper implements Tree {

    private final SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage;
    private final int[] treeAssemblageNidAsArray;

    public AssemblageTreeWrapper(SingleAssemblageSnapshot<ComponentNidVersion> treeAssemblage) {
        this.treeAssemblage = treeAssemblage;
        this.treeAssemblageNidAsArray = new int[] {treeAssemblage.getAssemblageNid() };
    }
    
    @Override
    public int getConceptAssemblageNid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TreeNodeVisitData breadthFirstProcess(int rootNid, ObjIntConsumer<TreeNodeVisitData> consumer, Supplier<TreeNodeVisitData> emptyDataSupplier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Tree createAncestorTree(int childNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TreeNodeVisitData depthFirstProcess(int rootNid, ObjIntConsumer<TreeNodeVisitData> consumer, Supplier<TreeNodeVisitData> emptyDataSupplier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getChildNids(int parentNid) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        NidSet childrenNids = new NidSet();
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            childSemantic.ifPresent((semantic) -> {
                childrenNids.add(semantic.getComponentNid());
            });
        }
        return childrenNids.asArray();
    }

    @Override
    public NidSet getDescendentNidSet(int parentNid) {
        NidSet descendentNids = new NidSet();
        addDescendentNids(parentNid, descendentNids);
        return descendentNids;
    }
    
    private void addDescendentNids(int parentNid, NidSet descendentNids) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            childSemantic.ifPresent((semantic) -> {
                int childNid = semantic.getComponentNid();
                // prevent cyclic recursion
                if (!descendentNids.contains(childNid)) {
                    descendentNids.add(childNid);
                    addDescendentNids(childNid, descendentNids);
                }
            });
        }
    }

    @Override
    public int[] getParentNids(int childNid) {
        NidSet parentNids = new NidSet();
        List<SearchResult> matches = Get.indexSemanticService().queryNidReference(childNid, treeAssemblageNidAsArray, null, null, null, null, null, Long.MIN_VALUE);
        for (SearchResult match: matches) {
            int semanticNid = match.getNid();
            treeAssemblage.getLatestSemanticVersion(semanticNid).ifPresent((t) -> {
                parentNids.add(t.getReferencedComponentNid());
            });
        }
        return parentNids.asArray();
    }

    @Override
    public void removeParent(int childNid, int parentNid) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public int[] getRootNids() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDescendentOf(int childNid, int parentNid) {
        return isDescendentNid(childNid, parentNid, new NidSet());
    }
    private boolean isDescendentNid(int matchNid, int parentNid, NidSet descendentNids) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            if (childSemantic.isPresent()) {
                int childNid = childSemantic.get().getComponentNid();
                if (matchNid == childNid) {
                    return true;
                }
                // prevent cyclic recursion
                if (!descendentNids.contains(childNid)) {
                    descendentNids.add(childNid);
                    if (isDescendentNid(matchNid, childNid, descendentNids)) {
                        return true;
                    }
                        
                }
            }
        }
        return false;
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        List<LatestVersion<ComponentNidVersion>> children = treeAssemblage.getLatestSemanticVersionsForComponentFromAssemblage(parentNid);
        for (LatestVersion<ComponentNidVersion> childSemantic: children) {
            if (childSemantic.isPresent()) {
                if (childSemantic.get().getComponentNid() == childNid) {
                    return true;
                }
            }
        }
        return false;
   }

    @Override
    public NidSet getNodeNids() {
        VersionStream<ComponentNidVersion> semanticStream = treeAssemblage.getLatestSemanticVersionsFromAssemblage();
        NidSet memberNids = new NidSet();
        semanticStream.forEach((t) -> {
            t.ifPresent((v) -> {
                memberNids.add(v.getReferencedComponentNid());
            });
        });
        return memberNids;
    }
    
}
