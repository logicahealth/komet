/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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



package sh.isaac.api.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.StampFilter;

/**
 * {@link ConceptSearchResult}
 * Class to support merging search results based on the concepts that are associated with.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConceptSearchResult implements SearchResult {

	/**
	 * The native id of the component(s) that matches the search, and their scores.
	 */
	private TreeSet<Map.Entry<Float,Integer>> nids;
	
	
	/** The Nid of the concept most closely related to the search result (the concept referenced by a description, for example). */
	private int conceptNid;
	
	private ConceptSearchResult(StampFilter coordForSort)
	{
		nids = new TreeSet<>(new Comparator<Map.Entry<Float,Integer>>()
		{
			@Override 
			public int compare(Map.Entry<Float,Integer> e1, Map.Entry<Float,Integer> e2) {
				
				int temp = -1 * Float.compare(e1.getKey(), e2.getKey());
				if (temp == 0 && coordForSort != null)
				{
					//If same score, see if we can sort better by status...
					if (Get.identifiedObjectService().getChronology(e1.getValue()).get().getLatestVersion(coordForSort).isPresentAnd(x -> x.isActive()))
					{
						//left (or both) is active, ranks higher, but invert.
						return 1;
					}
					else if (Get.identifiedObjectService().getChronology(e2.getValue()).get().getLatestVersion(coordForSort).isPresentAnd(x -> x.isActive()))
					{
						//right is active, ranks higher, but invert
						return -1;
					}
				}
				
				return temp;
			}
		});
	}
	
	/**
	 * Instantiates a new concept search result.
	 *
	 * @param conceptNid the concept Nid
	 * @param componentNid the component nid
	 * @param score the score
	 */
	public ConceptSearchResult(int conceptNid, int componentNid, float score) {
		this(conceptNid, componentNid, score, null);
	}

	/**
	 * Instantiates a new concept search result.
	 *
	 * @param conceptNid the concept Nid
	 * @param componentNid the component nid
	 * @param score the score
	 * @param stampFilterForNidRanking used to sort nids when the scores tie (prefer active over inactive)
	 */
	public ConceptSearchResult(int conceptNid, int componentNid, float score, StampFilter stampFilterForNidRanking) {
		this(stampFilterForNidRanking);
		this.conceptNid = conceptNid;
		this.nids.add(new Map.Entry<Float, Integer>()
		{
			@Override
			public Integer getValue()
			{
				return componentNid;
			}
			
			@Override
			public Float getKey()
			{
				return score;
			}

			@Override
			public Integer setValue(Integer value)
			{
				throw new UnsupportedOperationException();
			}
		});
	}

	/**
	 * Merge.
	 *
	 * @param other the other
	 */
	public void merge(ConceptSearchResult other) {
		if (this.conceptNid != other.conceptNid) {
			throw new RuntimeException("Unmergeable!");
		}

		this.nids.addAll(other.nids);
	}

	/**
	 * Merge.
	 *
	 * @param other the other
	 */
	public void merge(SearchResult other) {
		this.nids.add(new Map.Entry<Float, Integer>()
		{
			@Override
			public Integer getValue()
			{
				return other.getNid();
			}
			
			@Override
			public Float getKey()
			{
				return other.getScore();
			}

			@Override
			public Integer setValue(Integer value)
			{
				throw new UnsupportedOperationException();
			}
		});
	}

	/**
	 * Gets the score of the component with the best score, relative to the other matches.
	 *
	 * @return the score of the component with the best score, relative to the other matches
	 */
	public float getBestScore() {
		return this.nids.first().getValue();
	}

	/**
	 * Gets the Nid of the concept most closely related to the search result (the concept referenced by a description, for example).
	 *
	 * @return the Nid of the concept most closely related to the search result (the concept referenced by a description, for example)
	 */
	public int getConceptNid() {
		return this.conceptNid;
	}

	/**
	 * Gets the matching components, in a collection that is ranked from best score to worst.
	 *
	 * @return the matching components
	 */
	public Collection<? extends Integer> getMatchingComponents() {
		ArrayList<Integer> temp = new ArrayList<>(this.nids.size());
		for (Entry<Float, Integer> x : this.nids)
		{
			temp.add(x.getValue());
		}
		return temp;
	}

	/**
	 * Returns the best scoring match nid from the set of component match nids.
	 * If multiple matching nids had the same score, will prefer active over inactive nids.
	 *
	 * @return the nid
	 */
	@Override
	public int getNid() {
		return this.nids.first().getValue();
	}

	/**
	 * returns {@link #getBestScore()}.
	 *
	 * @return the score
	 */
	@Override
	public float getScore() {
		return getBestScore();
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(conceptNid);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ConceptSearchResult) {
			return Integer.valueOf(conceptNid).equals(((ConceptSearchResult)obj).conceptNid);
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Concept Nid: " + getConceptNid() + " Score: " + getBestScore();
	}
}