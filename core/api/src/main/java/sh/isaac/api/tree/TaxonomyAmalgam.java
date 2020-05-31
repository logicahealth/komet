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
import java.util.UUID;

import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.collection.MutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import sh.isaac.api.Get;
import sh.isaac.api.RefreshListener;
import sh.isaac.api.Edge;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * @author kec
 */
public class TaxonomyAmalgam implements TaxonomySnapshot {
    final DefiningTaxonomy definingTaxonomy;
    final ArrayList<TaxonomySnapshot> taxonomies = new ArrayList<>();
    final ArrayList<TaxonomySnapshot> inverseTaxonomies = new ArrayList<>();
    final ArrayList<ConceptSpecification> taxonomyRoots = new ArrayList<>();
    final ManifoldCoordinate manifoldCoordinate;
    final boolean includeDefiningTaxonomy;

    public TaxonomyAmalgam(ManifoldCoordinate manifoldCoordinate, boolean includeDefiningTaxonomy) {
        if (manifoldCoordinate == null) {
            throw new NullPointerException("manifoldCoordinate cannot be null. ");
        }
        this.manifoldCoordinate = manifoldCoordinate;
        this.includeDefiningTaxonomy = includeDefiningTaxonomy;
        this.definingTaxonomy = new DefiningTaxonomy();
        if (includeDefiningTaxonomy) {
            taxonomies.add(this.definingTaxonomy);
        }
    }

    public ArrayList<ConceptSpecification> getTaxonomyRoots() {
        return taxonomyRoots;
    }

    public ArrayList<TaxonomySnapshot> getTaxonomies() {
        return taxonomies;
    }

    public ArrayList<TaxonomySnapshot> getInverseTaxonomies() {
        return inverseTaxonomies;
    }

    @Override
    public int[] getTaxonomyChildConceptNids(int parentNid) {
        NidSet childNids = new NidSet();
        for (TaxonomySnapshot tree: taxonomies) {
            childNids.addAll(tree.getTaxonomyChildConceptNids(parentNid));
        }
        for (TaxonomySnapshot inverseTree: inverseTaxonomies) {
            childNids.addAll(inverseTree.getTaxonomyParentConceptNids(parentNid));
        }
        return childNids.asArray();
    }


    @Override
    public int[] getTaxonomyParentConceptNids(int childNid) {
        NidSet parentNids = new NidSet();
        for (TaxonomySnapshot tree: taxonomies) {
            parentNids.addAll(tree.getTaxonomyParentConceptNids(childNid));
        }
        for (TaxonomySnapshot inverseTree: inverseTaxonomies) {
            parentNids.addAll(inverseTree.getTaxonomyChildConceptNids(childNid));
        }
        return parentNids.asArray();
    }


    @Override
    public int[] getRootNids() {
        NidSet rootNids = new NidSet();
        for (TaxonomySnapshot tree: taxonomies) {
            rootNids.addAll(tree.getRootNids());
        }
        for (ConceptSpecification root: taxonomyRoots) {
            rootNids.add(root.getNid());
        }
        return rootNids.asArray();
    }

    @Override
    public boolean isChildOf(int childNid, int parentNid) {
        for (TaxonomySnapshot tree: taxonomies) {
            if (tree.isChildOf(childNid,  parentNid)) {
                return true;
            }
        }
        for (TaxonomySnapshot inverseTree: inverseTaxonomies) {
            if (inverseTree.isChildOf(parentNid, childNid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLeaf(int conceptNid) {
        for (TaxonomySnapshot tree: taxonomies) {
            if (!tree.isLeaf(conceptNid)) {
                return false;
            };
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            if (!tree.isLeaf(conceptNid)) {
                return false;
            };
        }
        return true;
    }

    @Override
    public boolean isKindOf(int childConceptNid, int parentConceptNid) {
        if (includeDefiningTaxonomy) {
            return definingTaxonomy.isKindOf(childConceptNid, parentConceptNid);
        }
        return false;
    }

    @Override
    public ImmutableIntSet getKindOfConcept(int rootConceptNid) {
        if (includeDefiningTaxonomy) {
            return getKindOfConcept(rootConceptNid);
        }
        return IntSets.immutable.empty();
    }

    @Override
    public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Tree getTaxonomyTree() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ManifoldCoordinate getManifoldCoordinate() {
        return this.manifoldCoordinate;
    }

    @Override
    public ImmutableCollection<Edge> getTaxonomyParentLinks(int parentConceptNid) {
        MutableCollection<Edge> links = Lists.mutable.empty();
        for (TaxonomySnapshot tree: taxonomies) {
            links.addAll((Collection<? extends Edge>) tree.getTaxonomyParentLinks(parentConceptNid));
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            links.addAll((Collection<? extends Edge>) tree.getTaxonomyParentLinks(parentConceptNid));
        }
        return links.toImmutable();
    }

    @Override
    public ImmutableCollection<Edge> getTaxonomyChildLinks(int childConceptNid) {
        MutableCollection<Edge> links = Lists.mutable.empty();
        for (TaxonomySnapshot tree: taxonomies) {
            links.addAll((Collection<? extends Edge>) tree.getTaxonomyChildLinks(childConceptNid));
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            links.addAll((Collection<? extends Edge>) tree.getTaxonomyChildLinks(childConceptNid));
        }
        return links.toImmutable();
    }

    public void reset() {
        this.taxonomies.clear();
        this.inverseTaxonomies.clear();
        this.taxonomyRoots.clear();
        if (includeDefiningTaxonomy) {
            taxonomies.add(this.definingTaxonomy);
        }
    }

    private class DefiningTaxonomy implements TaxonomySnapshot, RefreshListener {

        UUID listenerUuid = UUID.randomUUID();

        TaxonomySnapshot definingTaxonomySnapshot;

        public DefiningTaxonomy() {
            this.definingTaxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
            Get.taxonomyService().addTaxonomyRefreshListener(this);
        }

        @Override
        public boolean isLeaf(int conceptNid) {
            return definingTaxonomySnapshot.isLeaf(conceptNid);
        }

        @Override
        public boolean isChildOf(int childConceptNid, int parentConceptNid) {
            return definingTaxonomySnapshot.isChildOf(childConceptNid, parentConceptNid);
        }

        @Override
        public boolean isDescendentOf(int descendantConceptNid, int ancestorConceptNid) {
            return definingTaxonomySnapshot.isDescendentOf(descendantConceptNid, ancestorConceptNid);
        }

        @Override
        public ImmutableIntSet getKindOfConcept(int rootConceptNid) {
            return definingTaxonomySnapshot.getKindOfConcept(rootConceptNid);
        }

        @Override
        public int[] getRootNids() {
            return definingTaxonomySnapshot.getRootNids();
        }

        @Override
        public int[] getTaxonomyChildConceptNids(int parentConceptNid) {
            return definingTaxonomySnapshot.getTaxonomyChildConceptNids(parentConceptNid);
        }

        @Override
        public int[] getTaxonomyParentConceptNids(int childConceptNid) {
            return definingTaxonomySnapshot.getTaxonomyParentConceptNids(childConceptNid);
        }

        @Override
        public ImmutableCollection<Edge> getTaxonomyParentLinks(int parentConceptNid) {
            return definingTaxonomySnapshot.getTaxonomyParentLinks(parentConceptNid);
        }

        @Override
        public ImmutableCollection<Edge> getTaxonomyChildLinks(int childConceptNid) {
            return  definingTaxonomySnapshot.getTaxonomyChildLinks(childConceptNid);
        }

        @Override
        public Tree getTaxonomyTree() {
            return definingTaxonomySnapshot.getTaxonomyTree();
        }

        @Override
        public ManifoldCoordinate getManifoldCoordinate() {
            return TaxonomyAmalgam.this.manifoldCoordinate;
        }

        @Override
        public UUID getListenerUuid() {
            return listenerUuid;
        }

        @Override
        public void refresh() {
            this.definingTaxonomySnapshot = Get.taxonomyService().getSnapshot(manifoldCoordinate);
        }
    }
    
}
