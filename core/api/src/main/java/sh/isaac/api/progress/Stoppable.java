/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.api.progress;

import sh.isaac.api.LookupService;

/**
 * A simple interface to pass a stop request to a threaded executor that may not otherwise know that shutdown 
 * has been requested.
 */
public interface Stoppable
{
	/**
	 * Must call this method to enable auto-notification
	 * @param stopIfRunLevelGoingBelow If the {@link LookupService} will be going below this runlevel, 
	 * request the lookup service to stop this job prior to initiating the shutdown.
	 */
	public default void register(int stopIfRunLevelGoingBelow) {
		LookupService.registerStoppable(this, stopIfRunLevelGoingBelow);
	}
	
	/**
	 * Call this method to request the threaded job stop processing immediately.
	 */
	public abstract void stopJob();
}
