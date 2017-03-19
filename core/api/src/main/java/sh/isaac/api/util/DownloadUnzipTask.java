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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.file.Files;

import java.util.Base64;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.concurrent.Task;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DownloadUnzipTask}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DownloadUnzipTask
        extends Task<File> {
   private static Logger log = LoggerFactory.getLogger(DownloadUnzipTask.class);

   //~--- fields --------------------------------------------------------------

   private boolean cancel_ = false;
   String          username_, psswrd_;
   URL             url_;
   private boolean unzip_;
   private boolean failOnBadCheksum_;
   private File    targetFolder_;

   //~--- constructors --------------------------------------------------------

   /**
    * @param username (optional) used if provided
    * @param psswrd (optional) used if provided
    * @param url The URL to download from
    * @param unzip - Treat the file as a zip file, and unzip it after the download
    * @param failOnBadChecksum - If a checksum file is found on the repository - fail if the downloaded file doesn't match the expected value.
    * (If no checksum file is found on the repository, this option is ignored and the download succeeds)
    * @param targetFolder (optional) download and/or extract into this folder.  If not provided, a folder
    * will be created in the system temp folder for this purpose.
    * @throws IOException
    */
   public DownloadUnzipTask(String username,
                            String psswrd,
                            URL url,
                            boolean unzip,
                            boolean failOnBadChecksum,
                            File targetFolder)
            throws IOException {
      this.username_         = username;
      this.psswrd_           = psswrd;
      this.url_              = url;
      this.unzip_            = unzip;
      this.targetFolder_     = targetFolder;
      this.failOnBadCheksum_ = failOnBadChecksum;

      if (targetFolder_ == null) {
         targetFolder_ = File.createTempFile("ISAAC", "");
         targetFolder_.delete();
      } else {
         targetFolder_ = targetFolder_.getAbsoluteFile();
      }

      targetFolder_.mkdirs();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * @see javafx.concurrent.Task#cancel(boolean)
    */
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      super.cancel(mayInterruptIfRunning);
      cancel_ = true;
      return true;
   }

   /**
    * @see javafx.concurrent.Task#call()
    */
   @Override
   protected File call()
            throws Exception {
      File   dataFile            = download(url_);
      String calculatedSha1Value = null;
      String expectedSha1Value   = null;;

      try {
         log.debug("Attempting to get .sha1 file");

         File sha1File = download(new URL(url_.toString() + ".sha1"));

         expectedSha1Value = Files.readAllLines(sha1File.toPath())
                                  .get(0);

         Task<String> calculateTask = ChecksumGenerator.calculateChecksum("SHA1", dataFile);

         calculateTask.messageProperty().addListener(new ChangeListener<String>() {
                                      @Override
                                      public void changed(ObservableValue<? extends String> observable,
                                            String oldValue,
                                            String newValue) {
                                         updateMessage(newValue);
                                      }
                                   });
         calculateTask.progressProperty().addListener(new ChangeListener<Number>() {
                                      @Override
                                      public void changed(
                                      ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                         updateProgress(calculateTask.getProgress(), calculateTask.getTotalWork());
                                      }
                                   });
         WorkExecutors.get()
                      .getExecutor()
                      .execute(calculateTask);
         calculatedSha1Value = calculateTask.get();
         sha1File.delete();
      } catch (Exception e1) {
         log.debug("Failed to get .sha1 file", e1);
      }

      if ((calculatedSha1Value != null) &&!calculatedSha1Value.equals(expectedSha1Value)) {
         if (failOnBadCheksum_) {
            throw new RuntimeException("Checksum of downloaded file '" + url_.toString() +
                                       "' does not match the expected value!");
         } else {
            log.warn("Checksum of downloaded file '" + url_.toString() + "' does not match the expected value!");
         }
      }

      if (cancel_) {
         log.debug("Download cancelled");
         throw new Exception("Cancelled!");
      }

      if (unzip_) {
         updateTitle("Unzipping");

         try {
            ZipFile zipFile = new ZipFile(dataFile);

            zipFile.setRunInThread(true);
            zipFile.extractAll(targetFolder_.getAbsolutePath());

            while (zipFile.getProgressMonitor()
                          .getState() == ProgressMonitor.STATE_BUSY) {
               if (cancel_) {
                  zipFile.getProgressMonitor()
                         .cancelAllTasks();
                  log.debug("Download cancelled");
                  throw new Exception("Cancelled!");
               }

               updateProgress(zipFile.getProgressMonitor()
                                     .getPercentDone(), 100);
               updateMessage("Unzipping " + dataFile.getName() + " at " +
                             zipFile.getProgressMonitor().getPercentDone() + "%");

               try {
                  // TODO see if there is an API where I don't have to poll for completion
                  Thread.sleep(25);
               } catch (InterruptedException e) {
                  // noop
               }
            }

            log.debug("Unzip complete");
         } catch (Exception e) {
            log.error("error unzipping", e);
            throw new Exception("The downloaded file doesn't appear to be a zip file");
         } finally {
            dataFile.delete();
         }

         return targetFolder_;
      } else {
         return dataFile;
      }
   }

   private File download(URL url)
            throws Exception {
      log.debug("Beginning download from " + url);
      updateMessage("Download from " + url);

      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

      if (StringUtils.isNotBlank(username_) || StringUtils.isNotBlank(psswrd_)) {
         String encoded = Base64.getEncoder()
                                .encodeToString((username_ + ":" + psswrd_).getBytes());

         httpCon.setRequestProperty("Authorization", "Basic " + encoded);
      }

      httpCon.setDoInput(true);
      httpCon.setRequestMethod("GET");
      httpCon.setConnectTimeout(30 * 1000);
      httpCon.setReadTimeout(60 * 60 * 1000);

      long   fileLength = httpCon.getContentLengthLong();
      String temp       = url.toString();

      temp = temp.substring(temp.lastIndexOf('/') + 1, temp.length());

      File file = new File(targetFolder_, temp);

      try (InputStream in = httpCon.getInputStream();
         FileOutputStream fos = new FileOutputStream(file);) {
         byte[] buf       = new byte[1048576];
         int    read      = 0;
         long   totalRead = 0;

         while (!cancel_ && (read = in.read(buf, 0, buf.length)) > 0) {
            totalRead += read;

            // update every 1 MB
            updateProgress(totalRead, fileLength);

            float percentDone = ((int) (((float) totalRead / (float) fileLength) * 1000)) / 10f;

            updateMessage("Downloading - " + url + " - " + percentDone + " % - out of " + fileLength + " bytes");
            fos.write(buf, 0, read);
         }
      }

      if (cancel_) {
         log.debug("Download cancelled");
         throw new Exception("Cancelled!");
      } else {
         log.debug("Download complete");
      }

      return file;
   }
}

