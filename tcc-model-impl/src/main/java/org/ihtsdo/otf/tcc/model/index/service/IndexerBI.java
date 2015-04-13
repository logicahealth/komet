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

import gov.vha.isaac.ochre.api.sememe.SememeChronicle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentChronicleBI;

import org.jvnet.hk2.annotations.Contract;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.Future;
import org.ihtsdo.otf.tcc.api.blueprint.ComponentProperty;

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
public interface IndexerBI {
    /**
     *
     * @param nid for the component that the caller wished to wait until it's
     * document is added to the index.
     * @return a {@code Callable&lt;Long&gt;} object that will block until this
     * indexer has added the document to the index. The {@code call()} method
     * on the object will return the index generation that contains the document,
     * which can be used in search calls to make sure the generation is available
     * to the searcher.
     */
    public IndexedGenerationCallable getIndexedGenerationCallable(int nid);

    /**
     * To maximize search performance, you can optionally call forceMerge.  
     * forceMerge is a costly operation, so generally call it when the 
     * index is relatively static (after finishing a bulk addition of documents)
     */
    public void forceMerge();
    
    /**
     * Query index with no specified target generation of the index.
     *
     * @param query The query to apply.
     * @param field The component field to be queried.
     * @param sizeLimit The maximum size of the result list.
     * @return a List of {@code SearchResult</codes> that contins the nid of the
     * component that matched, and the score of that match relative to other
     * matches.
     * @throws IOException
     */
    public List<SearchResult> query(String query, ComponentProperty field, int sizeLimit)
            throws IOException;

    /**
     *
     * @param query The query to apply.
     * @param field The component field to be queried.
     * @param sizeLimit The maximum size of the result list.
     * @param targetGeneration target generation that must be included in the search
     * or Long.MIN_VALUE if there is no need to wait for a target generation.
     * @return a List of {@code SearchResult</codes> that contins the nid of the
     * component that matched, and the score of that match relative to other
     * matches.
     * @throws IOException
     */
    public List<SearchResult> query(String query, ComponentProperty field, int sizeLimit,
            long targetGeneration)
            throws IOException;
    
    /**
     *
     * @return the name of this indexer.
     */
    public String getIndexerName();
    
    /**
     * 
     * @return File representing the folder where the indexer stores its files. 
     */
    public File getIndexerFolder();

    /**
     * Checkpoints the index writer.
     */
    public void commitWriter();

    /**
     * Close the index writer as part of normal shutdown.
     */
    public void closeWriter();

    /**
     * Clear index, resulting in an empty index. Used prior to the
     * environment recreating the index by iterating over all components
     * and calling the {@code index(ComponentChronicleBI chronicle)}
     * with each component of the iteration. May be used for initial index
     * creation, or if indexing properties have changed.
     */
    public void clearIndex();

    /**
     * Index the chronicle in a manner appropriate to the
     * indexer implementation. The implementation is responsible to
     * determine if the component is appropriate for indexing. All changed
     * components will be sent to all indexers for indexing. The implementation
     * must not perform lengthy operations on this thread.
     *
     * @param chronicle
     * @return a {@code Future<Long>}for the index generation to which this
     * chronicle is attached.  If
     * this chronicle is not indexed by this indexer, the Future returns
     * {@code Long.MIN_VALUE{@code . The generation can be used with searchers
     * to make sure that the component's indexing is complete prior to performing
     * a search where the chronicle's results must be included.
     */
    public Future<Long> index(ComponentChronicleBI chronicle);
    
    /**
     * Index the chronicle in a manner appropriate to the
     * indexer implementation. The implementation is responsible to
     * determine if the component is appropriate for indexing. All changed
     * components will be sent to all indexers for indexing. The implementation
     * must not perform lengthy operations on this thread.
     *
     * @param chronicle
     * @return a {@code Future<Long>}for the index generation to which this
     * chronicle is attached.  If
     * this chronicle is not indexed by this indexer, the Future returns
     * {@code Long.MIN_VALUE{@code . The generation can be used with searchers
     * to make sure that the component's indexing is complete prior to performing
     * a search where the chronicle's results must be included.
     */
    public Future<Long> index(SememeChronicle chronicle);
    
    /**
     * Enables or disables an indexer. A disabled indexer will take
     * no action when the index method is called. 
     * @param enabled true if the indexer is enabled, otherwise false.
     */
    public void setEnabled(boolean enabled);
    
    /**
     * 
     * @return true if this indexer is enabled.
     */
    public boolean isEnabled();
    
}
