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

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.collections.NidSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.query.provider.ClauseComputeType;
import gov.vha.isaac.ochre.query.provider.ClauseSemantic;
import gov.vha.isaac.ochre.query.provider.LeafClause;
import gov.vha.isaac.ochre.query.provider.Query;
import gov.vha.isaac.ochre.query.provider.WhereClause;

/**
 * Returns descriptions matching the input string using Lucene.
 *
 * @author kec
 */
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class DescriptionLuceneMatch extends LeafClause {

    @XmlElement
    String luceneMatchKey;
    @XmlElement
    String viewCoordinateKey;

    public DescriptionLuceneMatch(Query enclosingQuery, String luceneMatchKey, String viewCoordinateKey) {
        super(enclosingQuery);
        this.luceneMatchKey = luceneMatchKey;
        this.viewCoordinateKey = viewCoordinateKey;
    }
    protected DescriptionLuceneMatch() {
    }
    @Override
    public EnumSet<ClauseComputeType> getComputePhases() {
        return PRE_ITERATION;
    }

    @Override
    public final NidSet computePossibleComponents(NidSet incomingPossibleComponents) {
        String luceneMatch = (String) enclosingQuery.getLetDeclarations().get(luceneMatchKey);

        TaxonomyCoordinate taxonomyCoordinate = (TaxonomyCoordinate) this.enclosingQuery.getLetDeclarations().get(viewCoordinateKey);


        NidSet nids = new NidSet();
        List<IndexServiceBI> indexers = LookupService.get().getAllServices(IndexServiceBI.class);
        IndexServiceBI descriptionIndexer = null;
        for (IndexServiceBI li : indexers) {
            if (li.getIndexerName().equals("descriptions")) {
                descriptionIndexer = li;
            }
        }
        if (descriptionIndexer == null) {
            throw new IllegalStateException("No description indexer found in: " + indexers);
        }
        List<SearchResult> queryResults = descriptionIndexer.query(luceneMatch, 1000);
        queryResults.stream().forEach((s) -> {
            nids.add(s.nid);
        });
        //Filter the results, based upon the input ViewCoordinate
        nids.stream().forEach((nid) -> {
            Optional<? extends ObjectChronology<? extends StampedVersion>> chronology = 
                    Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
            if (chronology.isPresent()) {
                if (!chronology.get().isLatestVersionActive(taxonomyCoordinate.getStampCoordinate())) {
                    getResultsCache().remove(nid);
                }
            } else {
                getResultsCache().remove(nid);
            }
        });
        getResultsCache().or(nids);
        return nids;

    }

    @Override
    public void getQueryMatches(ConceptVersion conceptVersion) {
        getResultsCache();
    }

    @Override
    public WhereClause getWhereClause() {
        WhereClause whereClause = new WhereClause();
        whereClause.setSemantic(ClauseSemantic.DESCRIPTION_LUCENE_MATCH);
        whereClause.getLetKeys().add(luceneMatchKey);
        return whereClause;
    }
}
