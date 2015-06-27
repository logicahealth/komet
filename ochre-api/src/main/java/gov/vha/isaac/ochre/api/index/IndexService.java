/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.index;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import java.io.File;
import java.util.concurrent.Future;
import org.jvnet.hk2.annotations.Contract;

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
public interface IndexService {

    /**
     * Clear index, resulting in an empty index. Used prior to the
     * environment recreating the index by iterating over all components
     * and calling the {@code index(ComponentChronicleBI chronicle)}
     * with each component of the iteration. May be used for initial index
     * creation, or if indexing properties have changed.
     */
    void clearIndex();

    /**
     * Close the index writer as part of normal shutdown.
     */
    void closeWriter();

    /**
     * Checkpoints the index writer.
     */
    void commitWriter();

    /**
     * To maximize search performance, you can optionally call forceMerge.
     * forceMerge is a costly operation, so generally call it when the
     * index is relatively static (after finishing a bulk addition of documents)
     */
    void forceMerge();

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
    IndexedGenerationCallable getIndexedGenerationCallable(int nid);

    /**
     *
     * @return File representing the folder where the indexer stores its files.
     */
    File getIndexerFolder();

    /**
     *
     * @return the name of this indexer.
     */
    String getIndexerName();

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
    Future<Long> index(ObjectChronology<?> chronicle);

    /**
     *
     * @return true if this indexer is enabled.
     */
    boolean isEnabled();

    /**
     * Enables or disables an indexer. A disabled indexer will take
     * no action when the index method is called.
     * @param enabled true if the indexer is enabled, otherwise false.
     */
    void setEnabled(boolean enabled);

    
}
