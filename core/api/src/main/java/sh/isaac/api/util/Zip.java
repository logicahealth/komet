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

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

import org.apache.commons.lang3.StringUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

//~--- classes ----------------------------------------------------------------

/**
 * {@link Zip}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Zip {
   /** The total work. */
   private final ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper();

   /** The work complete. */
   private final ReadOnlyDoubleWrapper workComplete = new ReadOnlyDoubleWrapper();

   /** The status. */
   private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper();

   /** The zf. */
   private final ZipFile zf;

   /** The zp. */
   private final ZipParameters zp;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new zip.
    *
    * @param artifactId the artifact id
    * @param version the version
    * @param classifier - optional
    * @param dataType - optional
    * @param outputFolder the output folder
    * @param zipContentCommonRoot the zip content common root
    * @param createArtifactTopLevelFolder - true to create a top level folder in the zip, false to just add the files starting at the root level
    * @return - progress moniter to utilize during {@link #addFiles(List)}
    * @throws ZipException the zip exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public Zip(String artifactId,
              String version,
              String classifier,
              String dataType,
              File outputFolder,
              File zipContentCommonRoot,
              boolean createArtifactTopLevelFolder)
            throws ZipException,
                   IOException {
      String classifierTemp = "";

      if (StringUtils.isNotBlank(classifier)) {
         classifierTemp = "-" + classifier.trim();
      }

      String dataTypeTemp = "";

      if (StringUtils.isNotBlank(dataType)) {
         dataTypeTemp = "." + dataType;
      }

      outputFolder.mkdir();
      this.zf = new ZipFile(new File(outputFolder,
                                     artifactId + "-" + version + classifierTemp + dataTypeTemp + ".zip"));
      this.zf.setRunInThread(true);
      this.zp = new ZipParameters();
      this.zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
      this.zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      this.zp.setDefaultFolderPath(zipContentCommonRoot.getAbsolutePath());

      final String rootFolder = (createArtifactTopLevelFolder
                                 ? (artifactId + "-" + version + classifierTemp + dataTypeTemp)
                                 : "");

      this.zp.setRootFolderInZip(rootFolder);
      this.zp.setIncludeRootFolder(createArtifactTopLevelFolder);
      this.status.set("Waiting for files");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * This will block during add - see the getTotalWork / getWorkComplete methods to monitor progress.
    *
    * @param dataFiles the data files
    * @return the file
    * @throws Throwable the throwable
    */
   public File addFiles(ArrayList<File> dataFiles)
            throws Throwable {
      this.zf.addFiles(dataFiles, this.zp);

      while (this.zf.getProgressMonitor()
                    .getResult() == ProgressMonitor.RESULT_WORKING) {
         this.totalWork.set(this.zf.getProgressMonitor()
                                   .getTotalWork());
         this.workComplete.set(this.zf.getProgressMonitor()
                                      .getWorkCompleted());
         this.status.set("Compressing " + this.zf.getProgressMonitor().getFileName());
         Thread.sleep(100);
      }

      this.status.set("Done");
      this.workComplete.set(1);
      this.totalWork.set(1);

      if (this.zf.getProgressMonitor()
                 .getResult() == ProgressMonitor.RESULT_ERROR) {
         throw this.zf.getProgressMonitor()
                      .getException();
      }

      return this.zf.getFile();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the status.
    *
    * @return the status
    */
   public ReadOnlyStringProperty getStatus() {
      return this.status.getReadOnlyProperty();
   }

   /**
    * Gets the total work.
    *
    * @return the total work
    */
   public ReadOnlyDoubleProperty getTotalWork() {
      return this.totalWork.getReadOnlyProperty();
   }

   /**
    * Gets the work complete.
    *
    * @return the work complete
    */
   public ReadOnlyDoubleProperty getWorkComplete() {
      return this.workComplete.getReadOnlyProperty();
   }
}

