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



package org.ihtsdo.otf.tcc.model.index.service;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.index.IndexService;
import gov.vha.isaac.ochre.api.index.SearchResult;
import java.util.List;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;

import org.jvnet.hk2.annotations.Contract;

//~--- JDK imports ------------------------------------------------------------


/**
 * The contract interface for indexing services.
 * <br>
 * {@code IndexerBI} implementations
 * must not throw exceptions. Throwing exceptions could cause the underlying
 * source data to corrupt. Since indexes can be regenerated, indexes should
 * mark themselves as invalid somehow, and recreate themselves when necessary.
 * @author aimeefurber
 * @author kec
 */
@Contract
public interface IndexerBI extends IndexService {
    
    
    /**
     * TODO pull up into index service after standardizing on a component property
     * enum shared by the observable objects. 
     * Query index with no specified target generation of the index.
     *
     * @param query The query to apply.
     * @param field The component field to be queried.
     * @param sizeLimit The maximum size of the result list.
     * @return a List of {@code SearchResult</codes> that contins the nid of the
     * component that matched, and the score of that match relative to other
     * matches.
     */
    List<SearchResult> query(String query, ComponentProperty field, int sizeLimit);

    /**
     * TODO pull up into index service after standardizing on a component property
     * enum shared by the observable objects. 
     *
     * @param query The query to apply.
     * @param field The component field to be queried.
     * @param sizeLimit The maximum size of the result list.
     * @param targetGeneration target generation that must be included in the search
     * or Long.MIN_VALUE if there is no need to wait for a target generation.
     * @return a List of {@code SearchResult</codes> that contins the nid of the
     * component that matched, and the score of that match relative to other
     * matches.
     */
    List<SearchResult> query(String query, ComponentProperty field, int sizeLimit, long targetGeneration);
}
