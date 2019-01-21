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
package sh.isaac.dbConfigBuilder.artifacts.rest.query;

import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import sh.isaac.dbConfigBuilder.artifacts.Converter;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.diff.DiffExecutionCreator;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

/**
 * An implementation that uses the Nexus Rest API to query nexus for available artifacts.
 * 
 * TODO Note, this currently supports Nexus 3, not Nexus 2.  I should have a second implementation for Nexus 2....
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class NexusRead implements ArtifactSearch
{
	private Logger log = LogManager.getLogger();
	StoredPrefs sp_;

	/**
	 * @param sp
	 */
	public NexusRead(StoredPrefs sp)
	{
		sp_ = sp;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<Converter> readConverterVersions()
	{
		HashSet<Converter> results = new HashSet<>();
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put(JsonReader.USE_MAPS, true);

			URLConnection service = new URL(getRestURL() + "beta/search?sh.isaac.misc&maven.artifactId=importers").openConnection();
			String userpass = sp_.getArtifactUsername() + ":" + new String(sp_.getArtifactPassword());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			service.setRequestProperty("Authorization", basicAuth);

			JsonObject data = (JsonObject) JsonReader.jsonToJava(service.getInputStream(), args);
			Object[] items = (Object[]) data.get("items");
			if (items != null)
			{
				for (Object item : items)
				{
					JsonObject jo = (JsonObject)item;
					String group = SrcUploadCreator.SRC_UPLOAD_GROUP;
					String artifactId = (String)jo.get("name");
					String version = convertSnapshotVersion((String)jo.get("version"));
					results.add(new Converter(group, artifactId, version));
				}
			}
		}
		catch (Exception e)
		{
			log.error("error querying nexus", e);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<String> readMetadataVersions()
	{
		HashSet<String> results = new HashSet<>();
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put(JsonReader.USE_MAPS, true);

			URLConnection service = new URL(getRestURL() + "beta/search?sh.isaac.core&maven.artifactId=metadata").openConnection();
			String userpass = sp_.getArtifactUsername() + ":" + new String(sp_.getArtifactPassword());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			service.setRequestProperty("Authorization", basicAuth);

			JsonObject data = (JsonObject) JsonReader.jsonToJava(service.getInputStream(), args);
			Object[] items = (Object[]) data.get("items");
			if (items != null)
			{
				for (Object item : items)
				{
					JsonObject jo = (JsonObject)item;
					results.add(convertSnapshotVersion((String)jo.get("version")));
				}
			}
		}
		catch (Exception e)
		{
			log.error("error querying nexus", e);
		}
		return results;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<SDOSourceContent> readSDOFiles(String artifactIdFilter)
	{
		//TODO need to support paging
		HashSet<SDOSourceContent> results = new HashSet<>();
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put(JsonReader.USE_MAPS, true);

			URLConnection service = new URL(getRestURL() + "beta/search?maven.groupId=" + SrcUploadCreator.SRC_UPLOAD_GROUP 
					+ (StringUtils.isNotBlank(artifactIdFilter) ? "&maven.artifactId=" + artifactIdFilter : "")).openConnection();
			String userpass = sp_.getArtifactUsername() + ":" + new String(sp_.getArtifactPassword());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			service.setRequestProperty("Authorization", basicAuth);

			JsonObject data = (JsonObject) JsonReader.jsonToJava(service.getInputStream(), args);

			Object[] items = (Object[]) data.get("items");
			if (items != null)
			{
				for (Object item : items)
				{
					JsonObject jo = (JsonObject)item;
					String group = SrcUploadCreator.SRC_UPLOAD_GROUP;
					String artifactId = (String)jo.get("name");
					String version = convertSnapshotVersion((String)jo.get("version"));
					//To get the classifier, if it has one, we have to look at the assets.
					
					
					Object[] assets = (Object[])jo.get("assets");
					if (assets != null)
					{
						for (Object asset : assets)
						{
							JsonObject castAsset = (JsonObject)asset;
							String path = (String)castAsset.get("path");
							if (path.endsWith(".zip"))
							{
								Optional<String> classifier = findClassifier(path, version);
								results.add(new SDOSourceContent(group, artifactId, version, classifier.orElse("")));
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("error querying nexus", e);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Set<IBDFFile> readIBDFFiles(boolean deltaArtifacts)
	{
		//TODO need to support paging
		HashSet<IBDFFile> results = new HashSet<>();
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put(JsonReader.USE_MAPS, true);

			URLConnection service = new URL(getRestURL() + "beta/search?maven.groupId=" + (deltaArtifacts ? DiffExecutionCreator.IBDF_OUTPUT_GROUP :  
				ContentConverterCreator.IBDF_OUTPUT_GROUP)).openConnection();
			String userpass = sp_.getArtifactUsername() + ":" + new String(sp_.getArtifactPassword());
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			service.setRequestProperty("Authorization", basicAuth);

			JsonObject data = (JsonObject) JsonReader.jsonToJava(service.getInputStream(), args);

			Object[] items = (Object[]) data.get("items");
			if (items != null)
			{
				for (Object item : items)
				{
					JsonObject jo = (JsonObject)item;
					String group = (deltaArtifacts ? DiffExecutionCreator.IBDF_OUTPUT_GROUP : ContentConverterCreator.IBDF_OUTPUT_GROUP);
					String artifactId = (String)jo.get("name");
					String version = convertSnapshotVersion((String)jo.get("version"));
					//To get the classifier, if it has one, we have to look at the assets.
					
					
					Object[] assets = (Object[])jo.get("assets");
					if (assets != null)
					{
						for (Object asset : assets)
						{
							JsonObject castAsset = (JsonObject)asset;
							String path = (String)castAsset.get("path");
							if (path.endsWith(".ibdf.zip"))
							{
								Optional<String> classifier = findClassifier(path, version);
								results.add(new IBDFFile(group, artifactId, version, classifier.orElse("")));
							}
						}
					}
					
				}
			}
		}
		catch (Exception e)
		{
			log.error("error querying nexus", e);
		}

		return results;
	}
	
	private String getRestURL()
	{
		// should be something like https://sagebits.net/nexus/repository/tmp-content/
		String temp = sp_.getArtifactReadURL();

		int i = temp.indexOf("/repository");
		if (i < 0)
		{
			if (temp.indexOf("content/repositories") >= 0)
			{
				throw new RuntimeException(temp + " looks like a nexus 2 connection - which isn't yet supported.");
			}
			else
			{
				throw new RuntimeException(temp + " is not a known Nexus URL format");
			}
		}
		
		// Looks like Nexus 3
		// Create a URL like https://sagebits.net/nexus/service/rest/
		temp = temp.substring(0, i);
		return temp + "/service/rest/";
	}
}
