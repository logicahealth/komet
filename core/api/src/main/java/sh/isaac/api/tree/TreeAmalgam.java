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
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import sh.isaac.api.collections.NidSet;

/**
 *
 * @author kec
 */
public class TreeAmalgam implements Tree {
    ArrayList<Tree> trees = new ArrayList<>();
    ArrayList<Tree> inverseTrees = new ArrayList<>();

    public ArrayList<Tree> getTrees() {
        return trees;
    }

    public ArrayList<Tree> getInverseTrees() {
        return inverseTrees;
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
        TreeAmalgam ancestorTree = new TreeAmalgam();
        for (Tree tree: trees) {
            ancestorTree.trees.add(tree.createAncestorTree(childNid));
        }
        for (Tree inverseTree: inverseTrees) {
            ancestorTree.inverseTrees.add(inverseTree.createAncestorTree(childNid));
        }
        return ancestorTree;
   }

    @Override
    public TreeNodeVisitData depthFirstProcess(int rootNid, ObjIntConsumer<TreeNodeVisitData> consumer, Supplier<TreeNodeVisitData> emptyDataSupplier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        return getNodeNids().size();
    }

    @Override
    public int[] getChildNids(int parentNid) {
        NidSet childNids = new NidSet();
        for (Tree tree: trees) {
            childNids.addAll(tree.getChildNids(parentNid));
        }
        for (Tree inverseTree: inverseTrees) {
            childNids.addAll(inverseTree.getParentNids(parentNid));
        }
        return childNids.asArray();
    }

    @Override
    public NidSet getDescendentNidSet(int parentNid) {
        NidSet childNids = new NidSet();
        for (Tree tree: trees) {
            childNids.addAll(tree.getDescendentNidSet(parentNid));
        }
        for (Tree inverseTree: inverseTrees) {
            childNids.addAll(inverseTree.createAncestorTree(parentNid).getNodeNids());
        }
        return childNids;
    }

    @Override
    public int[] getParentNids(int childNid) {
        NidSet parentNids = new NidSet();
        for (Tree tree: trees) {
            parentNids.addAll(tree.getParentNids(childNid));
        }
        for (Tree inverseTree: inverseTrees) {
            parentNids.addAll(inverseTree.getChildNids(childNid));
        }
        return parentNids.asArray();
    }

    @Override
    public void removeParent(int childNid, int parentNid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getRootNids() {
        NidSet rootNids = new NidSet();
        for (Tree tree: trees) {
            rootNids.addAll(tree.getRootNids());
        }
        return rootNids.asArray();
    }

    @Override
    public boolean isDescendentOf(int childNid, int parentNid) {
        for (Tree tree: trees) {
            if (tree.isDescendentOf(childNid,  parentNid)) {
                return true;
            }
        }
        for (Tree inverseTree: inverseTrees) {
            if (inverseTree.isDescendentOf(parentNid, childNid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        for (Tree tree: trees) {
            if (tree.isChildOf(childNid,  parentNid)) {
                return true;
            }
        }
        for (Tree inverseTree: inverseTrees) {
            if (inverseTree.isChildOf(parentNid, childNid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NidSet getNodeNids() {
        NidSet nodeNids = new NidSet();
        for (Tree tree: trees) {
            nodeNids.addAll(tree.getNodeNids());
        }
        for (Tree inverseTree: inverseTrees) {
            nodeNids.addAll(inverseTree.getNodeNids());
        }
        return nodeNids;
    }
}
