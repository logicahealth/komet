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
package sh.isaac.api.convert.differ;

import java.io.File;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 * An abstract base class to allow useage of this API into the mojo projects, and to not break isaac
 * when the actual implementation of the diff tooling isn't included on the classpath.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Contract
public abstract class IBDFDiffTool extends TimedTaskWithProgressTracker<Void>
{

	public static IBDFDiffTool getInstance(File outputDirectory, File initialStateIBDF, File newStateIBDF, UUID author, long time, boolean ignoreTimeInCompare,
			boolean ignoreSiblingModules, boolean generateRetiresForMissingModuleMetadata) throws IllegalStateException
	{
		IBDFDiffTool idt = LookupService.get().getService(IBDFDiffTool.class);
		if (idt == null)
		{
			throw new IllegalStateException("No implementation of an IBDFDiffTool is available on the classpath");
		}
		else
		{
			idt.init(outputDirectory, initialStateIBDF, newStateIBDF, author, time, ignoreTimeInCompare, ignoreSiblingModules, generateRetiresForMissingModuleMetadata);
			return idt;
		}
	}

	/**
	 * @param outputDirectory - The folder where to write the resulting IBDF diff file (and any other debug files)
	 * @param initialStateIBDF - The starting point IBDF file to examine
	 * @param newStateIBDF - The new state IBDF file to examine
	 * @param author - The author to use for any component retirements
	 * @param time - The time to use for any component retirements
	 * @param ignoreTimeInCompare - in certain cases, the source content doesn't provide a time, so the IBDF converter invents a time.
	 *            In these cases, we don't want to compare on time. This is only allowed when the incoming file only has one version per chronology.
	 *            Also, metadata is usually generated with the terminology change time, which is usually different on each import.
	 * @param ignoreSiblingModules - When processing version 8 of something, against version 7, typically, the module is specified
	 *            as a version-specific module - 7 or 8. But both 7 and 8 will share an unversioned 'parent' module. If true, we will ignore
	 *            module differences, as long as the parent module of each module is the same. If false, the module must be identical.
	 * @param generateRetiresForMissingModuleMetadata - if true, we treat the version specific module concept as if it were any other concept,
	 *            and generate retirements for it, if it isn't present in the new IBDF file (which it usually wouldn't be). If false, we won't
	 *            generate retire diffs for any sibling module concepts, or their attached semantics.
	 *            TODO when alias commits are better supported, perhaps we also produce alias's to put the content on both versioned modules...
	 */
	public abstract void init(File outputDirectory, File initialStateIBDF, File newStateIBDF, UUID author, long time, boolean ignoreTimeInCompare,
			boolean ignoreSiblingModules, boolean generateRetiresForMissingModuleMetadata);

	/**
	 * @return The count of new chronology objects that were found while running the diff (and will be present in the output diff file)
	 */
	public abstract long getAddedChronologies();

	/**
	 * @return The count of chronology objects that were present in the starting IBDF file, but not present in the new state file.
	 */
	public abstract long getRemovedChronologies();

	/**
	 * @return The count of {@link Status#INACTIVE} version objects that were created to properly process the {@link #getRemovedChronologies()}
	 */
	public abstract long getRemovedChronologiesVersionsCreated();

	/**
	 * @return The count of chronologies that had at least one version which differed from an existing version
	 */
	public abstract long getChangedChronologies();

	/**
	 * @return the total number of changed chronology versions passed into the diff file
	 */
	public abstract long getChangedChronologyVersions();

	/**
	 * @return the total number of module metadata components that were retained, even though they were not present in the
	 *         new IBDF file
	 */
	public abstract int getRetainedModuleMetadataComponents();

}