/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.oia.terminology.converters.sharedUtils.stats;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keep counts on all of the types of things that are converted.
 * 
 * @author Daniel Armbrust
 */

public class LoadStats
{
	private AtomicInteger concepts_ = new AtomicInteger();
	private AtomicInteger clonedConcepts_ = new AtomicInteger();
	private AtomicInteger skippedPropertiesCounter_ = new AtomicInteger();
	private TreeMap<String, Integer> descriptions_ = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> conceptIds_ = new TreeMap<String, Integer>();
	private TreeMap<String, TreeMap<String, Integer>> componentIds_ = new TreeMap<String, TreeMap<String, Integer>>();
	private TreeMap<String, Integer> refsetMembers_ = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> relationships_ = new TreeMap<String, Integer>();
	private TreeMap<String, TreeMap<String, Integer>> annotations_ = new TreeMap<String, TreeMap<String, Integer>>();
	
	private Object syncLock = new Object();

	public void addConcept()
	{
		concepts_.incrementAndGet();
	}

	public int getConceptCount()
	{
		return concepts_.get();
	}
	
	public void addConceptClone()
	{
		clonedConcepts_.incrementAndGet();
	}

	public int getClonedConceptCount()
	{
		return clonedConcepts_.get();
	}
	
	public void addSkippedProperty()
	{
		skippedPropertiesCounter_.incrementAndGet();
	}

	public int getSkippedPropertyCount()
	{
		return skippedPropertiesCounter_.get();
	}

	public void addDescription(String descName)
	{
		increment(descriptions_, descName);
	}

	public void addConceptId(String idName)
	{
		increment(conceptIds_, idName);
	}

	public void addComponentId(String annotatedItem, String annotationName)
	{
		increment(componentIds_, annotatedItem, annotationName);
	}

	public void addAnnotation(String annotatedItem, String annotationName)
	{
		increment(annotations_, annotatedItem, annotationName);
	}

	public void addRefsetMember(String refsetName)
	{
		increment(refsetMembers_, refsetName);
	}

	public void addRelationship(String relName)
	{
		increment(relationships_, relName);
	}

	public ArrayList<String> getSummary()
	{
		ArrayList<String> result = new ArrayList<String>();

		result.add("Concepts: " + concepts_.get());
		
		if (clonedConcepts_.get() > 0)
		{
			result.add("Cloned Concepts: " + clonedConcepts_.get());
		}

		int sum = 0;
		for (Map.Entry<String, Integer> value : relationships_.entrySet())
		{
			sum += value.getValue();
			result.add("Relationship '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Relationships Total: " + sum);

		sum = 0;
		for (Map.Entry<String, Integer> value : conceptIds_.entrySet())
		{
			sum += value.getValue();
			result.add("Concept ID '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Concept IDs Total: " + sum);

		sum = 0;
		int nestedSum = 0;
		for (Map.Entry<String, TreeMap<String, Integer>> value : componentIds_.entrySet())
		{
			nestedSum = 0;
			for (Map.Entry<String, Integer> nestedValue : value.getValue().entrySet())
			{
				result.add("Component ID '" + value.getKey() + ":" + nestedValue.getKey() + "': " + nestedValue.getValue());
				nestedSum += nestedValue.getValue();
			}
			sum += nestedSum;
			if (value.getValue().size() > 1)
			{
				result.add("Component ID '" + value.getKey() + "' Total: " + nestedSum);
			}
		}
		result.add("Component IDs Total: " + sum);

		sum = 0;
		for (Map.Entry<String, Integer> value : descriptions_.entrySet())
		{
			sum += value.getValue();
			result.add("Description '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Descriptions Total: " + sum);

		sum = 0;
		nestedSum = 0;
		for (Map.Entry<String, TreeMap<String, Integer>> value : annotations_.entrySet())
		{
			nestedSum = 0;
			for (Map.Entry<String, Integer> nestedValue : value.getValue().entrySet())
			{
				result.add("Annotation '" + value.getKey() + ":" + nestedValue.getKey() + "': " + nestedValue.getValue());
				nestedSum += nestedValue.getValue();
			}
			sum += nestedSum;
			if (value.getValue().size() > 1)
			{
				result.add("Annotation '" + value.getKey() + "' Total: " + nestedSum);
			}
		}
		result.add("Annotations Total: " + sum);
		
		if (skippedPropertiesCounter_.get() > 0)
		{
			result.add("Skipped Properties: " + skippedPropertiesCounter_.get());
		}

		sum = 0;
		for (Map.Entry<String, Integer> value : refsetMembers_.entrySet())
		{
			sum += value.getValue();
			result.add("Refset Members '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Refset Members Total: " + sum);

		return result;
	}

	private void increment(TreeMap<String, Integer> dataHolder, String type)
	{
		synchronized (syncLock)
		{
			Integer i = dataHolder.get(type);
			if (i == null)
			{
				i = new Integer(1);
			}
			else
			{
				i++;
			}
			dataHolder.put(type, i);
		}
	}

	private void increment(TreeMap<String, TreeMap<String, Integer>> dataHolder, String annotatedType, String type)
	{
		synchronized (syncLock)
		{
			TreeMap<String, Integer> map = dataHolder.get(annotatedType);
	
			if (map == null)
			{
				map = new TreeMap<String, Integer>();
			}
	
			Integer i = map.get(type);
	
			if (i == null)
			{
				i = new Integer(1);
			}
			else
			{
				i++;
			}
			map.put(type, i);
			dataHolder.put(annotatedType, map);
		}
	}
}
