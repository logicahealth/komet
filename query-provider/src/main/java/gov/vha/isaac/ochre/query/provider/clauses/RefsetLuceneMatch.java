/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package gov.vha.isaac.ochre.query.provider.clauses;

import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.collections.NidSet;
import java.util.EnumSet;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexer;

/**
 * Retrieves the refset matching the input SNOMED id.
 *
 * @author dylangrald
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class RefsetLuceneMatch extends LeafClause {

    @XmlElement
    String luceneMatchKey;
    @XmlElement
    String viewCoordinateKey;

    public RefsetLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.luceneMatchKey = luceneMatchKey;
        this.viewCoordinateKey = viewCoordinateKey;
    }

    protected RefsetLuceneMatch() {
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.REFSET_LUCENE_MATCH);
        whereClause.getLetKeys().add(luceneMatchKey);
        return whereClause;
    }

    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);
        String luceneMatch = (String) enclosingQuery.getLetDeclarations().get(luceneMatchKey);

        NidSet nids = new NidSet();

        SememeIndexer si = LookupService.get().getService(SememeIndexer.class);
        if (si == null) {
            throw new IllegalStateException("sememeIndexer is null");
        }
//        List<SearchResult> queryResults = si.query(Long.parseLong(luceneMatch), 1000);
//        queryResults.stream().forEach((s) -> {
//            nids.add(s.nid);
//        });
      //TODO FIX BACK UP
//        nids.stream().forEach((nid) -> {
//            Optional<? extends ObjectChronology<? extends StampedVersion>> optionalObject
//                    = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
//            if (optionalObject.isPresent()) {
//                Optional<? extends LatestVersion<? extends StampedVersion>> optionalVersion = 
//                        optionalObject.get().getLatestVersion(StampedVersion.class, viewCoordinate);
//                if (!optionalVersion.isPresent()) {
//                    nids.remove(nid);
//                }
//            } else {
//                nids.remove(nid);
//            }
//        });
        //Filter the results, based upon the input ViewCoordinate
        getResultsCache().or(nids);
        return nids;
    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
    }
}
