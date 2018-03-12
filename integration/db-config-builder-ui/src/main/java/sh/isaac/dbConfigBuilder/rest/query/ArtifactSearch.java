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
package sh.isaac.dbConfigBuilder.rest.query;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sh.isaac.pombuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.artifacts.SDOSourceContent;

/**
 * Interface for reading metadata from an artifact server
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public interface ArtifactSearch
{
	/**
	 * @return the version identifiers of all available metadata versions on the artifact server
	 */
	public Set<String> readMetadataVersions();
	
	/**
	 * @return all available IBDFFiles on the artifact server.
	 */
	public Set<IBDFFile> readIBDFFiles();
	
	/**
	 * @param artifactIdFilter - optional - filter for only returning versions of a particular artifact id
	 * @return all available SDOSourceContentFiles on the artifact server
	 */
	Set<SDOSourceContent> readSDOFiles(String artifactIdFilter);
	
	/**
	 * If the file name has a classifier, returns just the classifier portion
	 * @param value the file name - something like 20170731T150000Z-loader-4.48-SNAPSHOT/rf2-ibdf-sct-20170731T150000Z-loader-4.48-20180301.213413-1-Delta.ibdf.zip
	 * @return the classifier
	 */
	public default Optional<String> findClassifier(String value)
	{
		//Take in something like this: 
		//"..../rf2-ibdf-sct/20170731T150000Z-loader-4.48-SNAPSHOT/rf2-ibdf-sct-20170731T150000Z-loader-4.48-20180301.213413-1-Delta.ibdf.zip"
		//and parse the classifier (Delta) out of it.
		Pattern r = Pattern.compile(".*-([a-zA-Z]{1}[a-zA-Z0-9]*)\\.ibdf\\.zip");

		// Now create matcher object.
		Matcher m = r.matcher(value);
		if (m.matches())
		{
			return Optional.of(m.group(1));
		}
		else
		{
			return Optional.empty();
		}
	}

	/**
	 * Takes in a version like 2017-08-24-loader-4.48-20180301.172716-1 and returns 2017-08-24-loader-4.48-SNAPSHOT
	 * In the case of a release version, like 14.0, it does nothing, and returns version
	 * @param version the version to check
	 * @return the version with the data portions replaced with SNAPSHOT
	 */
	public default String convertSnapshotVersion(String version)
	{
		// figure out that a pattern like this: 2017-08-24-loader-4.48-20180301.172716-1
		// should actually be 2017-08-24-loader-4.48-SNAPSHOT

		Pattern r = Pattern.compile("(.*)(-[\\d]{8}\\.[\\d]{6}-[\\d]?)");

		// Now create matcher object.
		Matcher m = r.matcher(version);
		if (m.matches())
		{
			return m.group(1) + "-SNAPSHOT";
		}
		else
		{
			return version;
		}
	}
}
