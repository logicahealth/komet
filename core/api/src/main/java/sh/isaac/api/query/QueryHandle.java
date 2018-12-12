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

import java.util.Collection;
import java.util.List;

/**
 *
 * @author kec
 */
public interface QueryHandle {

    void cancel();

    /**
     * This is not the same as the size of the resultList collection, as results may be merged.
     *
     * @return the hit count
     * @throws Exception the exception
     */
    int getHitCount() throws Exception;

    int getOffPathFilteredCount();

    /**
     * Blocks until the results are available....
     *
     * @return the results
     * @throws Exception the exception
     */
    Collection<CompositeQueryResult> getResults() throws Exception;

    void setResults(List<CompositeQueryResult> results, int offPathResultsFiltered);
    /**
     * Gets the search start time.
     *
     * @return the search start time
     */
    long getSearchStartTime();

    /**
     * Returns the identifier provided (if any) by the caller when the search was started.
     *
     * @return the task id
     */
    Integer getTaskId();

    /**
     * Checks if cancelled.
     *
     * @return true, if cancelled
     */
    boolean isCancelled();
    
}
