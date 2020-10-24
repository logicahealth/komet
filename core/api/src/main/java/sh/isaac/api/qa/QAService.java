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
 */
package sh.isaac.api.qa;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.task.TimedTask;

/**
 * Placeholder for QA execution API
 * 
 * @author darmbrust
 *
 */
@Contract
public interface QAService
{

	/**
	 * Run QA across the entire datastore.
	 * 
	 * @param coordinate The coordinate to utilize when running the QA job
	 * 
	 * @return A task that can be used to block, if the caller wishes to wait
	 *         for the results. The task is already executed, when returned.
	 */
	TimedTask<QAResults> runQA(StampFilter coordinate);

	/**
	 * Run QA for the specified version of a component
	 * 
	 * @param component the item to check
	 * @param coordinate the coordinate to use while checking
	 * @return the QAResults (which will be empty, if it didn't fail)
	 */
	QAResults runQA(Version component, StampFilter coordinate);
}
