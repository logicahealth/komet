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
 * {@link Zip}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Zip {
   private ReadOnlyDoubleWrapper totalWork    = new ReadOnlyDoubleWrapper();
   private ReadOnlyDoubleWrapper workComplete = new ReadOnlyDoubleWrapper();
   private ReadOnlyStringWrapper status       = new ReadOnlyStringWrapper();
   private ZipFile               zf;
   private ZipParameters         zp;

   //~--- constructors --------------------------------------------------------

   /**
    *
    * @param artifactId
    * @param version
    * @param classifier - optional
    * @param dataType - optional
    * @param zipContentCommonRoot
    * @param createArtifactTopLevelFolder - true to create a top level folder in the zip, false to just add the files starting at the root level
    * @return - progress moniter to utilize during {@link #addFiles(List)}
    * @throws ZipException
    * @throws IOException
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
      zf = new ZipFile(new File(outputFolder, artifactId + "-" + version + classifierTemp + dataTypeTemp + ".zip"));
      zf.setRunInThread(true);
      zp = new ZipParameters();
      zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
      zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
      zp.setDefaultFolderPath(zipContentCommonRoot.getAbsolutePath());

      String rootFolder = (createArtifactTopLevelFolder ? (artifactId + "-" + version + classifierTemp + dataTypeTemp)
            : "");

      zp.setRootFolderInZip(rootFolder);
      zp.setIncludeRootFolder(createArtifactTopLevelFolder);
      status.set("Waiting for files");
   }

   //~--- methods -------------------------------------------------------------

   /**
    * This will block during add - see the getTotalWork / getWorkComplete methods to monitor progress.
    */
   public File addFiles(ArrayList<File> dataFiles)
            throws Throwable {
      zf.addFiles(dataFiles, zp);

      while (zf.getProgressMonitor()
               .getResult() == ProgressMonitor.RESULT_WORKING) {
         totalWork.set(zf.getProgressMonitor()
                         .getTotalWork());
         workComplete.set(zf.getProgressMonitor()
                            .getWorkCompleted());
         status.set("Compressing " + zf.getProgressMonitor().getFileName());
         Thread.sleep(100);
      }

      status.set("Done");
      workComplete.set(1);
      totalWork.set(1);

      if (zf.getProgressMonitor()
            .getResult() == ProgressMonitor.RESULT_ERROR) {
         throw zf.getProgressMonitor()
                 .getException();
      }

      return zf.getFile();
   }

   //~--- get methods ---------------------------------------------------------

   public ReadOnlyStringProperty getStatus() {
      return status.getReadOnlyProperty();
   }

   public ReadOnlyDoubleProperty getTotalWork() {
      return totalWork.getReadOnlyProperty();
   }

   public ReadOnlyDoubleProperty getWorkComplete() {
      return workComplete.getReadOnlyProperty();
   }
}

