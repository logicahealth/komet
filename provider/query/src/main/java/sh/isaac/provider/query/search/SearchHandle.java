/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */

package sh.isaac.provider.query.search;

import sh.isaac.api.query.QueryHandle;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;
import sh.isaac.api.query.CompositeQueryResult;

/**
 * Handle object to get search results.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 */
public class SearchHandle implements QueryHandle
{
	private final long searchStartTime = System.currentTimeMillis();

	private final Semaphore resultBlock = new Semaphore(1);

	private volatile boolean cancelled = false;

	private Exception error = null;

	private final Integer searchID;

	private List<CompositeQueryResult> resultList;

	private int offPathResultsFiltered = 0;

	/**
	 * Instantiates a new search handle.
	 *
	 * @param searchID the search ID
	 */
	SearchHandle(Integer searchID)
	{
		this.searchID = searchID;
		//prevent anyone from getting results until they are set.
		resultBlock.acquireUninterruptibly();
	}

    @Override
	public void cancel()
	{
		this.cancelled = true;
	}

	/**
	 * Checks if cancelled.
	 *
	 * @return true, if cancelled
	 */
    @Override
	public boolean isCancelled()
	{
		return this.cancelled;
	}

	protected void setError(Exception e)
	{
		this.error = e;
		resultBlock.release();
	}

	/**
	 * This is not the same as the size of the resultList collection, as results may be merged.
	 *
	 * @return the hit count
	 * @throws Exception the exception
	 */
    @Override
	public int getHitCount() throws Exception
	{
		int result = 0;

		for (final CompositeQueryResult csr : getResults())
		{
			result += csr.getMatchingComponents().size();
		}

		return result;
	}

    @Override
	public int getOffPathFilteredCount()
	{
		return offPathResultsFiltered;
	}

	/**
	 * Blocks until the results are available....
	 *
	 * @return the results
	 * @throws Exception the exception
	 */
    @Override
	public Collection<CompositeQueryResult> getResults() throws Exception
	{
		if (this.error != null)
		{
			throw this.error;
		}
		
		if (this.resultList == null)
		{
			try
			{
				this.resultBlock.acquireUninterruptibly();
			}
			finally
			{
				this.resultBlock.release();
			}
		}
		if (this.error != null)
		{
			throw this.error;
		}
		else
		{
			return this.resultList;
		}
	}

	/**
	 * Sets the results.
	 *
	 * @param results the new results
	 */
	public void setResults(List<CompositeQueryResult> results, int offPathResultsFiltered)
	{
		this.resultList = results;
		this.offPathResultsFiltered = offPathResultsFiltered;
		resultBlock.release();
	}

	/**
	 * Gets the search start time.
	 *
	 * @return the search start time
	 */
    @Override
	public long getSearchStartTime()
	{
		return this.searchStartTime;
	}

	/**
	 * Returns the identifier provided (if any) by the caller when the search was started.
	 *
	 * @return the task id
	 */
    @Override
	public Integer getTaskId()
	{
		return this.searchID;
	}
}
