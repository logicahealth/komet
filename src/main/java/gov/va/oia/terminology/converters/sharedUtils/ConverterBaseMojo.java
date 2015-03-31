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
package gov.va.oia.terminology.converters.sharedUtils;

import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import java.io.DataOutputStream;
import java.io.File;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * {@link ConverterBaseMojo}
 *
 * Base mojo class with shared parameters for reuse by terminology specific converters.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class ConverterBaseMojo extends AbstractMojo
{
	/**
	 * Location to write the output file
	 */
	@Parameter( required = true, defaultValue = "${project.build.directory}" )
	protected File outputDirectory;

	/**
	 * Location of the input source file(s).  May be a file or a directory, depending on the specific loader.
	 * Usually a directory.
	 */
	@Parameter (required = true)
	protected File inputFileLocation;

	/**
	 * Loader version number
	 */
	@Parameter (required = true, defaultValue = "${loader.version}")
	protected String loaderVersion;

	/**
	 * Converter result version number
	 */
	@Parameter (required = true, defaultValue = "${project.version}")
	protected String converterResultVersion;
	
	/**
	 * Set '-Dsdp' (skipUUIDDebugPublish) on the command line, to prevent the publishing of the 
	 * debug UUID map (it will still be created, and written to a file)
	 * 
	 * At the moment, this param is never used in code - it is just used as a pom trigger (but documented here)
	 */
	@Parameter (required = false, defaultValue = "${sdp}")
	private String createDebugUUIDMapSkipPublish;
	
	/**
	 * Set '-DskipUUIDDebug' on the command line, to disable the in memory UUID Debug map entirely (this disables UUID duplicate detection, but
	 * significantly cuts the required RAM overhead to run a loader).
	 */
	@Parameter (required = false, defaultValue = "${skipUUIDDebug}")
	private String createDebugUUIDMap;
	
	/**
	 * An optional list of annotation type names which should be skipped during this transformation.
	 */
	@Parameter (required = false)
	protected List<String> annotationSkipList;
	
	/**
	 * An optional list of description type names which should be skipped during this transformation.
	 */
	@Parameter (required = false)
	protected List<String> descriptionSkipList;
	
	/**
	 * An optional list of id type names which should be skipped during this transformation.
	 */
	@Parameter (required = false)
	protected List<String> idSkipList;
	
	/**
	 * An optional list of member refset names which should be skipped during this transformation.
	 */
	@Parameter (required = false)
	protected List<String> memberRefsetSkipList;
	
	/**
	 * An optional list of relationship names which should be skipped during this transformation.
	 */
	@Parameter (required = false)
	protected List<String> relationshipSkipList;
	
	protected DataOutputStream dos_;
	protected EConceptUtility conceptUtility_;
	
	@Override
	public void execute() throws MojoExecutionException
	{
		ConverterUUID.disableUUIDMap_ = ((createDebugUUIDMap == null || createDebugUUIDMap.length() == 0) ? false : Boolean.parseBoolean(createDebugUUIDMap));
		if (ConverterUUID.disableUUIDMap_)
		{
			ConsoleUtil.println("The UUID Debug map is disabled - this also prevents duplicate ID detection");
		}
		
		// Set up the output
		if (!outputDirectory.exists())
		{
			outputDirectory.mkdirs();
		}
		
		checkSkipListSupport();
	}
	
	private boolean notEmpty(List<String> item)
	{
		if (item != null && item.size() > 0)
		{
			return true;
		}
		return false;
	}
	
	private void checkSkipListSupport()
	{
		if (notEmpty(annotationSkipList))
		{
			supportsAnnotationSkipList();
		}
		if (notEmpty(idSkipList))
		{
			supportsIdSkipList();
		}
		if (notEmpty(memberRefsetSkipList))
		{
			supportsRefsetSkipList();
		}
		if (notEmpty(relationshipSkipList))
		{
			supportsRelationshipSkipList();
		}
		if (notEmpty(descriptionSkipList))
		{
			supportsDescriptionSkipList();
		}
	}
	
	//Individual loaders need to override the methods below, if they wish to support the various skiplists
	
	protected boolean supportsAnnotationSkipList()
	{
		throw new UnsupportedOperationException("This loader does not support an annotation skip list");
	}
	
	protected boolean supportsIdSkipList()
	{
		throw new UnsupportedOperationException("This loader does not support an id skip list");
	}
	
	protected boolean supportsRefsetSkipList()
	{
		throw new UnsupportedOperationException("This loader does not support a refset skip list");
	}
	
	protected boolean supportsRelationshipSkipList()
	{
		throw new UnsupportedOperationException("This loader does not support a relationsihp skip list");
	}
	
	protected boolean supportsDescriptionSkipList()
	{
		throw new UnsupportedOperationException("This loader does not support a description skip list");
	}
}