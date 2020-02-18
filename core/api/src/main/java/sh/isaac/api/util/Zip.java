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

package sh.isaac.api.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.progress.ProgressMonitor.Result;

/**
 * {@link Zip} - utilities for zipping a folder, or individual files from a folder.
 * 
 * Utility methods for naming the resulting zip in artifact form.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Zip
{
	private final ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper();

	private final ReadOnlyDoubleWrapper workComplete = new ReadOnlyDoubleWrapper();

	private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();

	private ZipFile zf;

	private ZipParameters zp;
	
	/**
	 * Instantiates a new zip, collecting maven file naming parameters
	 *
	 * @param artifactId the artifact id
	 * @param version the version
	 * @param classifier - optional
	 * @param dataType - optional
	 * @param outputFolder the output folder
	 * @param zipContentCommonRoot the zip content common root
	 * @param createArtifactTopLevelFolder - true to create a top level folder in the zip, false to just add the files starting at the root level
	 * @throws ZipException the zip exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Zip(String artifactId, String version, String classifier, String dataType, File outputFolder, File zipContentCommonRoot,
			boolean createArtifactTopLevelFolder) throws ZipException, IOException
	{
		String classifierTemp = "";

		if (StringUtils.isNotBlank(classifier))
		{
			classifierTemp = "-" + classifier.trim();
		}

		String dataTypeTemp = "";

		if (StringUtils.isNotBlank(dataType))
		{
			dataTypeTemp = "." + dataType;
		}
		
		final String artifactName = artifactId + "-" + version + classifierTemp + dataTypeTemp;
		
		File outputZipFile = new File(outputFolder, artifactName + ".zip");
		init(outputZipFile, zipContentCommonRoot, createArtifactTopLevelFolder ? Optional.of(artifactName) : Optional.empty());
	}
	

	/**
	 * @param outputZipFile - where to write the zip file
	 * @param zipCommonRoot - the top folder where all content for the zip will come from
	 * @throws ZipException
	 * @throws IOException
	 */
	public Zip(File outputZipFile, File zipCommonRoot) throws ZipException, IOException
	{
		init(outputZipFile, zipCommonRoot, Optional.empty());
	}
	
	/**
	 * @param outputZipFile - where to write the zip file
	 * @param zipCommonRoot - the top folder where all content for the zip will come from
	 * @param topLevelFolder - if provided, a folder with this name will be the top level inside the zip.  If not provided, files will start
	 *     from the root of the zip file
	 */
	private void init(File outputZipFile, File zipCommonRoot, Optional<String> topLevelFolder)
	{
		outputZipFile.getParentFile().mkdirs();
		this.zf = new ZipFile(outputZipFile);
		this.zf.setRunInThread(true);
		this.zp = new ZipParameters();
		this.zp.setCompressionLevel(CompressionLevel.ULTRA);
		this.zp.setCompressionMethod(CompressionMethod.DEFLATE);
		//include root folder is broken, we don't use it.  Root Folder name in zip seems to do the same thing...
		this.zp.setIncludeRootFolder(false);
		this.zp.setDefaultFolderPath(zipCommonRoot.getAbsolutePath());
		this.zp.setRootFolderNameInZip(topLevelFolder.orElse(""));
		this.status.set("Waiting for files");
	}

	/**
	 * This will block during add - see the getTotalWork / getWorkComplete methods to monitor progress.
	 *
	 * @param dataFiles the data files - must come from the common root this Zip was constructed with
	 * @return this handle to the complete zip file
	 * @throws Throwable the throwable
	 */
	public File addFiles(ArrayList<File> dataFiles) throws Throwable
	{
		this.zf.addFiles(dataFiles, this.zp);

		waitForZip();
		return this.zf.getFile();
	}
	
	/**
	 * This will block until complete - see the getTotalWork / getWorkComplete methods to monitor progress.
	 * @param includeCommmonRootFolder - true to include the top level folder, false to only include its children at the root
	 * of the zip file.
	 * @return this handle to the complete zip file
	 * @throws Exception
	 */
	public File addAllFilesInCommonRoot(boolean includeCommmonRootFolder) throws Exception
	{
		
		File rootFolder = new File(this.zp.getDefaultFolderPath());
		if (includeCommmonRootFolder)
		{
			this.zp.setRootFolderNameInZip(rootFolder.getName());
		}
		else
		{
			this.zp.setRootFolderNameInZip("");
		}
		
		this.zf.addFolder(rootFolder, this.zp);
		waitForZip();
		return this.zf.getFile();
	}
	
	private void waitForZip() throws Exception
	{
		while (this.zf.getProgressMonitor().getResult() == null || this.zf.getProgressMonitor().getResult() == Result.WORK_IN_PROGRESS)
		{
			this.totalWork.set(this.zf.getProgressMonitor().getTotalWork());
			this.workComplete.set(this.zf.getProgressMonitor().getWorkCompleted());
			this.status.set("Compressing " + this.zf.getProgressMonitor().getFileName());
			Thread.sleep(100);
		}

		this.status.set("Done");
		this.workComplete.set(1);
		this.totalWork.set(1);

		if (this.zf.getProgressMonitor().getResult() == Result.ERROR)
		{
			throw this.zf.getProgressMonitor().getException();
		}
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public ReadOnlyStringProperty getStatus()
	{
		return this.status.getReadOnlyProperty();
	}

	/**
	 * Gets the total work.
	 *
	 * @return the total work
	 */
	public ReadOnlyDoubleProperty getTotalWork()
	{
		return this.totalWork.getReadOnlyProperty();
	}

	/**
	 * Gets the work complete.
	 *
	 * @return the work complete
	 */
	public ReadOnlyDoubleProperty getWorkComplete()
	{
		return this.workComplete.getReadOnlyProperty();
	}
	
	public static void main(String[] args) throws Throwable
	{
		//Adhoc testing...
		Zip z = new Zip(new File("target/zipTest1.zip"), new File("target/surefire-reports"));
		z.addAllFilesInCommonRoot(false);
		
		z = new Zip(new File("target/zipTest2.zip"), new File("target/surefire-reports"));
		z.addAllFilesInCommonRoot(true);
		
		ArrayList<File> files = new ArrayList<>();
		files.add(new File("target/surefire-reports/sh.isaac.api.util.AlphanumComparatorTest.txt"));
		files.add(new File("target/surefire-reports/TEST-sh.isaac.util.SctIdTests.xml"));
		
		z = new Zip("a", "b", "c", "d", new File("target"), new File("target/surefire-reports"), true);
		z.addFiles(files);

		z = new Zip("a", "b", "c", "d", new File("target/test-classes/"), new File("target/surefire-reports"), false);
		z.addFiles(files);
		
		System.exit(0);
	}
}
