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

import org.eclipse.collections.api.set.ImmutableSet;
import sh.isaac.api.Get;
import sh.isaac.api.VersionManagmentPathService;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.coordinate.StampPositionImmutable;

/**
 * A class for passing Author, Module and/or Path restrictions into lucene queries.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 *
 */
public class AuthorModulePathRestriction {

	private NidSet authors;
	private NidSet modules;
	private NidSet paths;

	private AuthorModulePathRestriction() {

	}

	public static AuthorModulePathRestriction restrictAuthor(NidSet authors) {
		AuthorModulePathRestriction ar = new AuthorModulePathRestriction();
		ar.authors = authors;
		return ar;
	}

	public static AuthorModulePathRestriction restrictModule(NidSet modules) {
		AuthorModulePathRestriction ar = new AuthorModulePathRestriction();
		ar.modules = modules;
		return ar;
	}

	public static AuthorModulePathRestriction restrictPath(NidSet paths) {
		AuthorModulePathRestriction ar = new AuthorModulePathRestriction();
		ar.paths = paths;
		return ar;
	}

	public static AuthorModulePathRestriction restrict(NidSet authors,
			NidSet modules,
			NidSet paths) {
		AuthorModulePathRestriction ar = new AuthorModulePathRestriction();
		ar.authors = authors;
		ar.modules = modules;
		ar.paths = paths;
		return ar;
	}
	
	/**
	 * Build an AuthorModulePathRestriction by extracting the modules and Path from the {@link ManifoldCoordinate#getViewStampFilter()}
	 * Path is pulled recursively, including its origins, from the {@link VersionManagmentPathService#getOrigins(int)}
	 * @see #restrict(StampFilter)
	 * @param mc
	 * @return
	 */
	public static AuthorModulePathRestriction restrict(ManifoldCoordinate mc) {
		return restrict(mc.getViewStampFilter());
	}
	
	/**
	 * Build an AuthorModulePathRestriction by extracting the modules and Path from the StampFilter
	 * Path is pulled recursively, including its origins, from the {@link VersionManagmentPathService#getOrigins(int)}
	 * @param stampFilter
	 * @return
	 */
	public static AuthorModulePathRestriction restrict(StampFilter stampFilter) {
		AuthorModulePathRestriction ar = new AuthorModulePathRestriction();
		ar.authors = new NidSet();
		ar.modules = NidSet.of(stampFilter.getModuleNids().toArray());
		ar.paths = new NidSet();
		ar.paths.add(stampFilter.getStampPosition().getPathForPositionConcept().getNid());
		//So, paths have a hierarchy now.  Buried away in another provider.  In every use case I can think of, we would also want the origin path included.
		ImmutableSet<StampPositionImmutable> sp = Get.versionManagmentPathService().getOrigins(stampFilter.getStampPosition().getPathForPositionConcept().getNid());
		addPathNids(sp, ar.paths);
		return ar;
	}
	
	private static void addPathNids(ImmutableSet<StampPositionImmutable> sp, NidSet setToAddTo)
	{
		if (sp == null) {
			return;
		}
		for (StampPositionImmutable spi : sp) {
			setToAddTo.add(spi.getPathForPositionNid());
			addPathNids(spi.getPathOrigins(), setToAddTo);
		}
	}

	public NidSet getAuthors() {
		return authors;
	}

	public NidSet getModules() {
		return modules;
	}

	public NidSet getPaths() {
		return paths;
	}
}
