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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Handle object to get search results.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @author ocarlsen
 */
public class SearchHandle
{
	private final long searchStartTime = System.currentTimeMillis();

	private final Semaphore resultBlock = new Semaphore(1);

	private volatile boolean cancelled = false;

	private Exception error = null;

	private final Integer searchID;

	private List<CompositeSearchResult> resultList;

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

	public void cancel()
	{
		this.cancelled = true;
	}

	/**
	 * Checks if cancelled.
	 *
	 * @return true, if cancelled
	 */
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
	public int getHitCount() throws Exception
	{
		int result = 0;

		for (final CompositeSearchResult csr : getResults())
		{
			result += csr.getMatchingComponents().size();
		}

		return result;
	}

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
	public Collection<CompositeSearchResult> getResults() throws Exception
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
	protected void setResults(List<CompositeSearchResult> results, int offPathResultsFiltered)
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
	public long getSearchStartTime()
	{
		return this.searchStartTime;
	}

	/**
	 * Returns the identifier provided (if any) by the caller when the search was started.
	 *
	 * @return the task id
	 */
	public Integer getTaskId()
	{
		return this.searchID;
	}
}
