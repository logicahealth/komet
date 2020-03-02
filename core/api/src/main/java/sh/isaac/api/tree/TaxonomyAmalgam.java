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
import sh.isaac.api.TaxonomyLink;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.ManifoldCoordinate;

/**
 *
 * @author kec
 */
public class TaxonomyAmalgam implements TaxonomySnapshot {
    TaxonomySnapshot definingTaxonomy;
    ArrayList<TaxonomySnapshot> taxonomies = new ArrayList<>();
    ArrayList<TaxonomySnapshot> inverseTaxonomies = new ArrayList<>();
    ArrayList<ConceptSpecification> taxonomyRoots = new ArrayList<>();
    final ManifoldCoordinate manifoldCoordinate;

    public TaxonomyAmalgam(ManifoldCoordinate manifoldCoordinate, boolean includeDefiningTaxonomy) {
        this.manifoldCoordinate = manifoldCoordinate;
        if (includeDefiningTaxonomy) {
            this.definingTaxonomy = Get.taxonomyService().getSnapshot(manifoldCoordinate);
            this.taxonomies.add(definingTaxonomy);
        } else {
            this.definingTaxonomy = null;
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
        if (definingTaxonomy != null) {
            return definingTaxonomy.isKindOf(childConceptNid, parentConceptNid);
        }
        return false;
    }

    @Override
    public NidSet getKindOfConceptNidSet(int rootConceptNid) {
        if (definingTaxonomy != null) {
            return getKindOfConceptNidSet(rootConceptNid);
        }
        return new NidSet();
    }

    @Override
    public boolean isDescendentOf(int descendantConceptNid, int parentConceptNid) {
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
    public TaxonomySnapshot makeAnalog(ManifoldCoordinate manifoldCoordinate) {
        TaxonomyAmalgam analog = new TaxonomyAmalgam(manifoldCoordinate, this.definingTaxonomy != null);
        for (TaxonomySnapshot tree: taxonomies) {
            analog.taxonomies.add(tree.makeAnalog(manifoldCoordinate));
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            analog.inverseTaxonomies.add(tree.makeAnalog(manifoldCoordinate));
        }
        return analog;
    }

    @Override
    public Collection<TaxonomyLink> getTaxonomyParentLinks(int parentConceptNid) {
        List<TaxonomyLink> links = new ArrayList<>();
        for (TaxonomySnapshot tree: taxonomies) {
            links.addAll((Collection<? extends TaxonomyLink>) tree.getTaxonomyParentLinks(parentConceptNid));
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            links.addAll((Collection<? extends TaxonomyLink>) tree.getTaxonomyParentLinks(parentConceptNid));
        }
        return links;
    }

    @Override
    public Collection<TaxonomyLink> getTaxonomyChildLinks(int childConceptNid) {
        List<TaxonomyLink> links = new ArrayList<>();
        for (TaxonomySnapshot tree: taxonomies) {
            links.addAll((Collection<? extends TaxonomyLink>) tree.getTaxonomyChildLinks(childConceptNid));
        }
        for (TaxonomySnapshot tree: inverseTaxonomies) {
            links.addAll((Collection<? extends TaxonomyLink>) tree.getTaxonomyChildLinks(childConceptNid));
        }
        return links;
    }
    
    
}
