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

import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.pombuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.converter.ContentConverterCreator;

/**
 * An implementation that uses the Nexus Rest API to query nexus for available artifacts
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class NexusRead implements ArtifactSearch
{
	private Logger log = LogManager.getLogger();
	StoredPrefs sp_;

	public NexusRead(StoredPrefs sp)
	{
		sp_ = sp;
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
	public Set<IBDFFile> readIBDFFiles()
	{
		HashSet<IBDFFile> results = new HashSet<>();
		try
		{
			Map<String, Object> args = new HashMap<>();
			args.put(JsonReader.USE_MAPS, true);

			URLConnection service = new URL(getRestURL() + "beta/search?maven.groupId=" + ContentConverterCreator.IBDF_OUTPUT_GROUP).openConnection();
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
					String group = ContentConverterCreator.IBDF_OUTPUT_GROUP;
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
								Optional<String> classifier = findClassifier(path);
								System.out.println("path: " + path + " " + classifier);
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
			throw new RuntimeException(temp + " is not a known Nexus URL format");
		}
		// Create a URL like https://sagebits.net/nexus/service/rest/
		temp = temp.substring(0, i);
		return temp + "/service/rest/";
	}
}
