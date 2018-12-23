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
package sh.isaac.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyProperty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.observable.ObservableChronology;
import sh.isaac.api.observable.ObservableVersion;
import static sh.isaac.api.query.Clause.getParentClauses;
import sh.isaac.api.xml.JoinSpecificationAdaptor;

/**
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class Join 
        extends ParentClause {

    List<? extends JoinSpecification> joinSpecifications = new ArrayList<>();
    List<int[]> joinResults = new ArrayList<>();
    
    /**
     * Default no arg constructor for Jaxb.
     */
    public Join() {
        super();
    }

    /**
     * Instantiates a new or.
     *
     * @param enclosingQuery the enclosing query
     * @param clauses the clauses
     */
    public Join(Query enclosingQuery, Clause... clauses) {
        super(enclosingQuery, clauses);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void resetResults() {
        // no cached data in task. 
    }

    @XmlElement(name = "JoinSpecification")
    @XmlJavaTypeAdapter(JoinSpecificationAdaptor.class)    
    @XmlElementWrapper(name = "Join")
    public List<? extends JoinSpecification> getJoinSpecifications() {
        return joinSpecifications;
    }

    public void setJoinSpecifications(List<? extends JoinSpecification> joinSpecifications) {
        this.joinSpecifications = joinSpecifications;
    }

    public int[][] getJoinResults() {
        return joinResults.toArray(new int[joinResults.size()][]);
    }

    /**
     * Compute possible components.
     *
     * @param searchSpace the search space
     * @return the nid set
     */
    @Override
    public Map<ConceptSpecification, NidSet> computeComponents(Map<ConceptSpecification, NidSet> searchSpace) {
        joinResults.clear();
        for (Clause child: getChildren()) {
            child.computeComponents(searchSpace);
        }
        for (JoinSpecification joinSpec: joinSpecifications) {
            NidSet nidSet1 = searchSpace.get(joinSpec.getFirstAssemblage());
            NidSet nidSet2 = searchSpace.get(joinSpec.getSecondAssemblage());
            StampCoordinate stampCoordinate = getLetItem(joinSpec.getStampCoordinateKey());
            for (int nid1: nidSet1.asArray()) {
                ObservableChronology chron1 = Get.observableChronologyService().getObservableChronology(nid1);
                LatestVersion<ObservableVersion> version1Latest = chron1.getLatestObservableVersion(stampCoordinate);
                if (version1Latest.isPresent() && version1Latest.get().getPropertyMap().containsKey(joinSpec.getFirstField())) {
                    ObservableVersion v1 = version1Latest.get();
                    ReadOnlyProperty<?> v1Prop = v1.getPropertyMap().get(joinSpec.getFirstField());
                    for (int nid2: nidSet2.asArray()) {
                        ObservableChronology chron2 = Get.observableChronologyService().getObservableChronology(nid2);
                        LatestVersion<ObservableVersion> version2Latest = chron2.getLatestObservableVersion(stampCoordinate);
                        if (version2Latest.isPresent() && version2Latest.get().getPropertyMap().containsKey(joinSpec.getSecondField())) {
                            ObservableVersion v2 = version2Latest.get();
                            ReadOnlyProperty<?> v2Prop = v2.getPropertyMap().get(joinSpec.getSecondField());
                            if (v1Prop.getValue().equals(v2Prop.getValue())) {
                                joinResults.add(new int[] {nid1, nid2});
                            } 
                        } 
                    }
                } 
            }
        }

        return searchSpace;
    }

    @Override
    public Map<ConceptSpecification, NidSet> computePossibleComponents(Map<ConceptSpecification, NidSet> incomingPossibleComponents) {
        joinResults.clear();
        for (Clause child: getChildren()) {
            child.computePossibleComponents(incomingPossibleComponents);
        }
        return incomingPossibleComponents;
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public ClauseSemantic getClauseSemantic() {
        return ClauseSemantic.JOIN;
    }

    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    @Override
    public WhereClause getWhereClause() {
        final WhereClause whereClause = new WhereClause();

        whereClause.setSemantic(ClauseSemantic.JOIN);
        getChildren().stream().forEach((clause) -> {
            whereClause.getChildren()
                    .add(clause.getWhereClause());
        });
        return whereClause;
    }

    @Override
    public Clause[] getAllowedSubstutitionClauses() {
        return getParentClauses();
    }

    @Override
    public Clause[] getAllowedChildClauses() {
        return getAllClauses();
    }

    @Override
    public Clause[] getAllowedSiblingClauses() {
        return getAllClauses();
    }
}
