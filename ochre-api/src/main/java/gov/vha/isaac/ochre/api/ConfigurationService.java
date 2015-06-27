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
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.constants.Constants;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Contract;


/**
 * An interface used for system configuration.  Services started by the {@link LookupService} will 
 * utilize an implementation of this service in order to configure themselves.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface ConfigurationService
{
	/**
	 * The default {@code ConceptModel} is {@link ConceptModel#OCHRE_CONCEPT_MODEL}.  This can be overridden by 
	 * setting the system property {@code Constants#CONCEPT_MODEL_PROPERTY}.  However, the actual value returned depends 
	 * on the actual implementation of the ConfigurationService - and if or how it overrides this method.  The recommended
	 * behavior of an implementation, is that any call to {@link #setConceptModel(ConceptModel)} should override this default
	 * behavior (including the reading of the system property)
	 * 
	 * @return the {@code ConceptModel} the database shall use. 
	 */
	public default ConceptModel getConceptModel()
	{
		String conceptModel = System.getProperty(Constants.CONCEPT_MODEL_PROPERTY);
		if (StringUtils.isNotBlank(conceptModel))
		{
			try
			{
				return ConceptModel.valueOf(conceptModel);
			}
			catch (Exception e)
			{
				throw new RuntimeException("The system property " + Constants.CONCEPT_MODEL_PROPERTY + " is not parseable as an instance of a ConceptModel enum");
			}
		}
		return ConceptModel.OCHRE_CONCEPT_MODEL;
	}
	
	/**
	 * Set an alternative {@code ConceptModel}. 
	 * This method can only be utilized prior populating a database.
	 * 
	 * @param model {@code ConceptModel} the database shall use.
	 * 
	 * @throws IllegalStateException if this is called after database has transitioned to a running state.
	 */
	public default void setConceptModel(ConceptModel model) throws IllegalStateException
	{
		throw new IllegalStateException("Not supported by the supplied ConfigurationService implementation." 
				+ "  The implementation must override the setConceptModel(...) method");
	}
		
	/**
	 * @return The root folder of the database - this method returns a value the returned path should contain subfolders
	 * of {@link Constants#DEFAULT_CHRONICLE_FOLDER} and {@link Constants#DEFAULT_SEARCH_FOLDER}.
	 * 
	 * Note that if this returns no value, {@link #getChronicleFolderPath()} and {@link #getSearchFolderPath()} must
	 * return valid values.
	 * 
	 * This method will return (in the following order):
	 * 
	 *  - The value specified by a call to {@link #setDataStoreFolderPath(Path)} 
	 *  - a path constructed from the value of {@link Constants#DATA_STORE_ROOT_LOCATION_PROPERTY} if {@link #setDataStoreFolderPath(Path)} 
	 *  was never called
	 *  - Nothing if {@link Constants#DATA_STORE_ROOT_LOCATION_PROPERTY} has not been set.
	 * 
	 * If a value is returned, the returned path will exist on disk at the time that this method returns.
	 */
	public Optional<Path> getDataStoreFolderPath();
	
	/**
	 * Specify the root folder of the database.  The specified folder should contain subfolders
	 * of {@link Constants#DEFAULT_CHRONICLE_FOLDER} and {@link Constants#DEFAULT_SEARCH_FOLDER}.
	 * 
	 * This method can only be utilized prior to the first call to {@link LookupService#startupIsaac()}
	 * 
	 * @param dataStoreFolderPath
	 * @throws IllegalStateException if this is called after the system has already started.
	 * @throws IllegalArgumentException if the provided dbFolderPath is an existing file, rather than a folder.
	 */
	public void setDataStoreFolderPath(Path dataStoreFolderPath) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * @return The root folder of the database - one would expect to find a data-store specific folder such as "cradle" 
	 * inside this folder.  The default implementation returns either:
	 * 
	 * A path as specified exactly via {@link Constants#CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY} (if the property is set)
	 * or 
	 * the result of {@link #getDataStoreFolderPath()} + {@link Constants#DEFAULT_CHRONICLE_FOLDER}
	 * 
	 * The returned path exists on disk at the time that this method returns.
	 */
	public default Path getChronicleFolderPath()
	{
		Path result;
		String path = System.getProperty(Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY);
		if (StringUtils.isNotBlank(path))
		{
			result = Paths.get(path);
		}
		else
		{
			Optional<Path> rootPath = getDataStoreFolderPath();
			if (!rootPath.isPresent())
			{
				throw new IllegalStateException("The ConfigurationService implementation has not been configured by a call to setDataStoreFolderPath(),"
						+" and the system property " + Constants.DATA_STORE_ROOT_LOCATION_PROPERTY + " has not been set.  Cannot construct the chronicle folder path.");
			}
			else
			{
				result = rootPath.get().resolve(Constants.DEFAULT_CHRONICLE_FOLDER);
			}
		}
		try
		{
			Files.createDirectories(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return result;
	}
	
	/**
	 * @return The root folder of the search data store - one would expect to find a data-store specific folder such as "lucene" 
	 * inside this folder.  The default implementation returns either:
	 * 
	 * A path as specified exactly via {@link Constants#SEARCH_ROOT_LOCATION_PROPERTY} (if the property is set)
	 * or 
	 * the result of {@link #getDataStoreFolderPath()} + {@link Constants#DEFAULT_SEARCH_FOLDER}
	 * 
	 * The returned path exists on disk at the time that this method returns.
	 */
	public default Path getSearchFolderPath()
	{
		Path result;
		String path = System.getProperty(Constants.SEARCH_ROOT_LOCATION_PROPERTY);
		if (StringUtils.isNotBlank(path))
		{
			result = Paths.get(path);
		}
		else
		{
			Optional<Path> rootPath = getDataStoreFolderPath();
			if (!rootPath.isPresent())
			{
				throw new IllegalStateException("The ConfigurationService implementation has not been configured by a call to setDataStoreFolderPath(),"
						+" and the system property " + Constants.DATA_STORE_ROOT_LOCATION_PROPERTY + " has not been set.  Cannot construct the search folder path.");
			}
			else
			{
				result = rootPath.get().resolve(Constants.DEFAULT_SEARCH_FOLDER);
			}
		}
		try
		{
			Files.createDirectories(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return result;
	}
}
