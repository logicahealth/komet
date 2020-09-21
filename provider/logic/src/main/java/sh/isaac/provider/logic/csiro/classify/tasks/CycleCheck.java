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

package sh.isaac.provider.logic.csiro.classify.tasks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.model.logic.ClassifierResultsImpl;

/**
 * {@link CycleCheck}
 * 
 * This implementation will return a null ClassifierResults if there was no cycle.  If there were one or more cycles, 
 * those cycles will be returned in the result ClassifierResults object.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class CycleCheck extends TimedTaskWithProgressTracker<ClassifierResults>
{
	private Logger log = LogManager.getLogger();
	private ManifoldCoordinate mc;
	private HashSet<Integer> orphans = new HashSet<>();

	/**
	 * Set up a new cycle checker task
	 * @param manifoldCoordinate
	 *
	 */
	public CycleCheck(ManifoldCoordinate manifoldCoordinate)
	{
		updateTitle("Cycle Check");
		mc = manifoldCoordinate.toManifoldCoordinateImmutable();
	}

	/**
	 * Returns null, if there is no cycle, otherwise, it returns a ClassifierResults with the cycle details.
	 * @see javafx.concurrent.Task#call()
	 */
	@Override
	protected ClassifierResults call() throws Exception
	{
		Get.activeTasks().add(this);
		try
		{
			TaxonomySnapshot ts = Get.taxonomyService().getSnapshot(mc);
			Map<Integer, Set<int[]>> results = new ConcurrentHashMap<>();
			
			Get.conceptService().getConceptNidStream(true).forEach(nid -> 
			{
				Set<int[]> conceptCycles = getCycles(nid, ts);
				if (conceptCycles.size() > 0)
				{
					results.put(nid, conceptCycles);
				}
			});
			if (results.size() > 0)
			{
				log.info("Found {} concepts with cycles in their path to root", results.size());
				return new ClassifierResultsImpl(results, orphans, mc);
			}
			else
			{
				return null;
			}
		}
		finally
		{
			Get.activeTasks().remove(this);
		}
	}

	/**
	 * @param nid
	 * @param ts
	 * @return
	 */
	private Set<int[]> getCycles(int nid, TaxonomySnapshot ts)
	{
		HashSet<int[]> result = new HashSet<>();
		int[] parents = ts.getTaxonomyParentConceptNids(nid);
		for (int parent : parents)
		{
			//loop to self
			if (parent == nid)
			{
				result.add(new int[] {parent});
			}
			else
			{
				MutableIntList path = IntLists.mutable.empty();
				path.add(parent);
				if (hasCycle(path, parent, nid, ts, 0))
				{
					int[] cycle = new int[path.size()];
					for (int i = 0; i < cycle.length; i++)
					{
						cycle[i] = path.get(i);
					}
					result.add(cycle);
				}
				
			}
		}
		if (parents.length == 0 && nid != TermAux.SOLOR_ROOT.getNid()
			//Only mark as an orphan if the concept is active, as most inactive rels aren't currently loaded.
							&& Get.concept(nid).getLatestVersion(ts.getManifoldCoordinate().getVertexStampFilter()).isPresentAnd(v -> v.isActive()))

		{
			//orphan
			orphans.add(nid);
		}
		return result;
	}


	/**
	 *
	 * @param path
	 * @param nidOnPathToRoot
	 * @param startNid
	 * @param ts
	 * @return
	 */
	private boolean hasCycle(MutableIntList path, int nidOnPathToRoot, int startNid, TaxonomySnapshot ts, int recursionDepth)
	{
		if (recursionDepth > 200) {
			StringBuilder sb = new StringBuilder("Recursion depth = " + recursionDepth + "\n");
			sb.append(Get.conceptDescriptionTextList(path.toArray()));
			throw new IllegalStateException(sb.toString());
		}
		int[] parents = ts.getTaxonomyParentConceptNids(nidOnPathToRoot);
		if (recursionDepth > 150) {
			StringBuilder sb = new StringBuilder("Warn: Recursion depth = " + recursionDepth);
			sb.append("\nnidOnPathToRoot: ").append(nidOnPathToRoot).append(" ").append(Get.conceptDescriptionText(nidOnPathToRoot));
			sb.append("\npath: ").append(Get.conceptDescriptionTextList(path.toArray()));
			sb.append("\nparents: ").append(Get.conceptDescriptionTextList(parents));
			LOG.debug(sb.toString());
		}
		for (int parent : parents)
		{
			if (parent == startNid)
			{
				path.add(parent);
				return true;
			}
			else
			{
				return hasCycle(path, parent, startNid, ts, recursionDepth + 1);
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return The list of orphaned oncepts identified during the cycle check
	 */
	public Set<Integer> getOrphans() {
		return orphans;
	}
}
