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
package gov.vha.isaac.ochre.api.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class ArtifactUtilities
{
	public static String makeMavenRelativePath(String groupId, String artifactId, String version, String classifier, String type)
	{
		if (version.endsWith("-SNAPSHOT"))
		{
			throw new RuntimeException("Cannot create a valid path to a -SNAPSHOT url without downloading the corresponding maven-metadata.xml file.");
		}
		try
		{
			return makeMavenRelativePath(null, null, null, groupId, artifactId, version, classifier, type);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}
	
	public static String makeMavenRelativePath(String baseMavenURL, String mavenUsername, String mavenPassword, String groupId, String artifactId, 
			String version, String classifier, String type) throws Exception
	{
		String temp = groupId.replaceAll("\\.", "/");
		String snapshotVersion = "";
		String versionWithoutSnapshot = version;
		if (version.endsWith("-SNAPSHOT"))
		{
			versionWithoutSnapshot = version.substring(0, version.lastIndexOf("-SNAPSHOT"));
			URL metadataUrl = new URL(baseMavenURL + (baseMavenURL.endsWith("/") ? "" : "/") + temp + "/" + artifactId + "/" + version + "/maven-metadata.xml");
			//Need to download the maven-metadata.xml file
			Task<File> task = new DownloadUnzipTask(mavenUsername, mavenPassword, metadataUrl, false, false, null);
			Get.workExecutors().getExecutor().execute(task);
			File metadataFile = task.get();

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			Document dDoc = null;
			XPath xPath = XPathFactory.newInstance().newXPath();

			builder = domFactory.newDocumentBuilder();

			dDoc = builder.parse(metadataFile);
			String timestamp = ((Node)xPath.evaluate("/metadata/versioning/snapshot/timestamp", dDoc, XPathConstants.NODE)).getTextContent();
			String buildNumber = ((Node)xPath.evaluate("/metadata/versioning/snapshot/buildNumber", dDoc, XPathConstants.NODE)).getTextContent();
			
			snapshotVersion = "-" + timestamp + "-" + buildNumber;
			metadataFile.delete();
			//The download task makes a subfolder in temp for this, delete that too
			metadataFile.getParentFile().delete();
		}
		
		return temp + "/" + artifactId + "/" + version + "/" + artifactId + "-" + versionWithoutSnapshot + snapshotVersion +
				(StringUtils.isNotBlank(classifier) ? "-" + classifier : "") + "." + type;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException
	{
		try
		{
			String username = "foo";
			String password = "foo";
			
			LookupService.startupWorkExecutors();
			
			URL release = new URL("http://vadev.mantech.com:8081/nexus/content/repositories/central/" 
					+ makeMavenRelativePath("aopalliance", "aopalliance", "1.0", null, "jar"));
			Task<File> task = new DownloadUnzipTask(null, null, release, false, true, null);
			Get.workExecutors().getExecutor().submit(task);
			File foo = task.get();
			System.out.println(foo.getCanonicalPath());
			foo.delete();
			foo.getParentFile().delete();
			
			File where = new File("").getAbsoluteFile();
			URL snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/" 
					+ makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/", username, password, 
							"gov.vha.isaac.db", "vhat", "2016.01.07-1.0-SNAPSHOT", "all", "cradle.zip"));
			task = new DownloadUnzipTask(username, password, snapshot, true, true, where);
			Get.workExecutors().getExecutor().submit(task);
			foo = task.get();
			
			snapshot = new URL("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/" 
					+ makeMavenRelativePath("http://vadev.mantech.com:8081/nexus/content/repositories/termdatasnapshots/", username, password, 
							"gov.vha.isaac.db", "vhat", "2016.01.07-1.0-SNAPSHOT", "all", "lucene.zip"));
			task = new DownloadUnzipTask(username, password, snapshot, true, true, where);
			Get.workExecutors().getExecutor().submit(task);
			foo = task.get();
			
			System.out.println(foo.getCanonicalPath());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Platform.exit();
	}
}
