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

package sh.isaac.api;

//~--- non-JDK imports --------------------------------------------------------

import org.jvnet.hk2.annotations.Contract;

//~--- interfaces -------------------------------------------------------------

/**
 * Contract for data or service caches that need to be reset when the services
 * are started and stopped on possibly different databases
 * 
 * While this doesn't appear to offer anything over the IsaacCache interface, there is an important distinction.
 * 
 * The LookupService will only call {@link #reset()} on ACTIVATED IsaacCache objects, in order to avoid instantiating
 * things that were never needed.
 * 
 * However, this also means that it won't call {@link #reset()} on caches that utilize a static access method - and
 * the LookupService has no idea if those have been triggered or not. So these services ALWAYS need to have reset called
 * (and their implementations need to be safe to call - aka, no side effects in the constructor that depend on service
 * level activated classes)
 * 
 * This class should be used by any cache object that offers static methods.
 * 
 */
@Contract
public interface StaticIsaacCache extends IsaacCache
{
	/**
	 * Eliminate all references to cached services or data.
	 */
        @Override
	void reset();
}
