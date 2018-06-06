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

package sh.isaac.dbConfigBuilder.artifacts;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import sh.isaac.api.util.AlphanumComparator;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;

/**
 *
 * {@link Artifact}
 * A base class for providing artifact information to the config builder tool.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class Artifact implements Comparable<Artifact>
{
	/** The group id. */
	private final String groupId;

	/** The artifact id. */
	private final String artifactId;

	/** The version. */
	private final String version;

	/** The classifier. */
	private final String classifier;
	
	private final String dataType;

	// ~--- constructors --------------------------------------------------------

	/**
	 * Instantiates a new artifact.
	 *
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 */
	public Artifact(String groupId, String artifactId, String version)
	{
		this(groupId, artifactId, version, null);
	}

	/**
	 * Instantiates a new artifact.
	 *
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier the classifier
	 */
	public Artifact(String groupId, String artifactId, String version, String classifier)
	{
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.dataType = "zip";
	}
	
	/**
	 * Instantiates a new artifact.
	 *
	 * @param groupId the group id
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier the classifier
	 * @param dataType the datatype (zip, ibdf.zip, etc)
	 */
	public Artifact(String groupId, String artifactId, String version, String classifier, String dataType)
	{
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.dataType = dataType;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString()
	{
		return "Artifact groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version=" + this.version + ", classifier="
				+ (this.classifier == null ? "" : this.classifier) + "]";
	}

	/**
	 * Gets the artifact id.
	 *
	 * @return the artifact id
	 */
	public String getArtifactId()
	{
		return this.artifactId;
	}

	/**
	 * Gets the classifier.
	 *
	 * @return the classifier
	 */
	public String getClassifier()
	{
		return this.classifier;
	}

	/**
	 * Checks for classifier.
	 *
	 * @return true, if successful
	 */
	public boolean hasClassifier()
	{
		if ((this.classifier == null) || (this.classifier.trim().length() == 0))
		{
			return false;
		}

		return true;
	}

	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public String getGroupId()
	{
		return this.groupId;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion()
	{
		return this.version;
	}
	
	public String getDataType()
	{
		return this.dataType;
	}
	
	
	/**
	 * If this artifact exists in a local m2 folder (at the location of the m2 folder path in stored prefs) then return the file reference.
	 * @param storedPrefs
	 * @return the file ref, if available.
	 */
	public Optional<File> getLocalPath(StoredPrefs storedPrefs)
	{
		File temp = new File(storedPrefs.getLocalM2FolderPath());
		
		if (temp.isDirectory())
		{
			if (StringUtils.isNotBlank(getGroupId()) && StringUtils.isNotBlank(getArtifactId()) && StringUtils.isNotBlank(getVersion()))
			{
				Path potential = Paths.get(temp.getAbsolutePath(), getGroupId().replaceAll("\\.", File.separator), getArtifactId(), getVersion(), 
						getArtifactId() + "-" + getVersion() + (hasClassifier() ? "-" + getClassifier() : "") + "." + getDataType());
				File file = potential.toFile();
				if (file.exists())
				{
					return Optional.of(file);
				}
			}
		}
		return Optional.empty();
	}
	
	

	@Override
	public int compareTo(Artifact o)
	{
		int sort = groupId.compareTo(o.groupId);
		if (sort == 0)
		{
			sort = artifactId.compareTo(o.artifactId);
		}
		if (sort == 0)
		{
			sort = AlphanumComparator.compare(version, o.version, false);
		}
		if (sort == 0)
		{
			sort = classifier.compareTo(o.classifier);
		}
		return sort;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artifact other = (Artifact) obj;
		if (artifactId == null)
		{
			if (other.artifactId != null)
				return false;
		}
		else if (!artifactId.equals(other.artifactId))
			return false;
		if (classifier == null)
		{
			if (other.classifier != null)
				return false;
		}
		else if (!classifier.equals(other.classifier))
			return false;
		if (groupId == null)
		{
			if (other.groupId != null)
				return false;
		}
		else if (!groupId.equals(other.groupId))
			return false;
		if (version == null)
		{
			if (other.version != null)
				return false;
		}
		else if (!version.equals(other.version))
			return false;
		if (dataType == null)
		{
			if (other.dataType != null)
				return false;
		}
		else if (!dataType.equals(other.dataType))
			return false;
		return true;
	}
}
