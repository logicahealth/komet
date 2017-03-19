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
import javafx.concurrent.Task;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.progress.ProgressMonitor;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DownloadUnzipTask}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DownloadUnzipTask
        extends Task<File> {
   
   /** The log. */
   private static Logger log = LoggerFactory.getLogger(DownloadUnzipTask.class);

   //~--- fields --------------------------------------------------------------

   /** The cancel. */
   private boolean cancel_ = false;
   
   /** The psswrd. */
   String          username_, psswrd_;
   
   /** The url. */
   URL             url_;
   
   /** The unzip. */
   private final boolean unzip_;
   
   /** The fail on bad cheksum. */
   private final boolean failOnBadCheksum_;
   
   /** The target folder. */
   private File    targetFolder_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new download unzip task.
    *
    * @param username (optional) used if provided
    * @param psswrd (optional) used if provided
    * @param url The URL to download from
    * @param unzip - Treat the file as a zip file, and unzip it after the download
    * @param failOnBadChecksum - If a checksum file is found on the repository - fail if the downloaded file doesn't match the expected value.
    * (If no checksum file is found on the repository, this option is ignored and the download succeeds)
    * @param targetFolder (optional) download and/or extract into this folder.  If not provided, a folder
    * will be created in the system temp folder for this purpose.
    * @throws IOException Signals that an I/O exception has occurred.
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

      if (this.targetFolder_ == null) {
         this.targetFolder_ = File.createTempFile("ISAAC", "");
         this.targetFolder_.delete();
      } else {
         this.targetFolder_ = this.targetFolder_.getAbsoluteFile();
      }

      this.targetFolder_.mkdirs();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Cancel.
    *
    * @param mayInterruptIfRunning the may interrupt if running
    * @return true, if successful
    * @see javafx.concurrent.Task#cancel(boolean)
    */
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      super.cancel(mayInterruptIfRunning);
      this.cancel_ = true;
      return true;
   }

   /**
    * Call.
    *
    * @return the file
    * @throws Exception the exception
    * @see javafx.concurrent.Task#call()
    */
   @Override
   protected File call()
            throws Exception {
      final File   dataFile            = download(this.url_);
      String calculatedSha1Value = null;
      String expectedSha1Value   = null;;

      try {
         log.debug("Attempting to get .sha1 file");

         final File sha1File = download(new URL(this.url_.toString() + ".sha1"));

         expectedSha1Value = Files.readAllLines(sha1File.toPath())
                                  .get(0);

         final Task<String> calculateTask = ChecksumGenerator.calculateChecksum("SHA1", dataFile);

         calculateTask.messageProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> updateMessage(newValue));
         calculateTask.progressProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> updateProgress(calculateTask.getProgress(), calculateTask.getTotalWork()));
         WorkExecutors.get()
                      .getExecutor()
                      .execute(calculateTask);
         calculatedSha1Value = calculateTask.get();
         sha1File.delete();
      } catch (final Exception e1) {
         log.debug("Failed to get .sha1 file", e1);
      }

      if ((calculatedSha1Value != null) &&!calculatedSha1Value.equals(expectedSha1Value)) {
         if (this.failOnBadCheksum_) {
            throw new RuntimeException("Checksum of downloaded file '" + this.url_.toString() +
                                       "' does not match the expected value!");
         } else {
            log.warn("Checksum of downloaded file '" + this.url_.toString() + "' does not match the expected value!");
         }
      }

      if (this.cancel_) {
         log.debug("Download cancelled");
         throw new Exception("Cancelled!");
      }

      if (this.unzip_) {
         updateTitle("Unzipping");

         try {
            final ZipFile zipFile = new ZipFile(dataFile);

            zipFile.setRunInThread(true);
            zipFile.extractAll(this.targetFolder_.getAbsolutePath());

            while (zipFile.getProgressMonitor()
                          .getState() == ProgressMonitor.STATE_BUSY) {
               if (this.cancel_) {
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
               } catch (final InterruptedException e) {
                  // noop
               }
            }

            log.debug("Unzip complete");
         } catch (final Exception e) {
            log.error("error unzipping", e);
            throw new Exception("The downloaded file doesn't appear to be a zip file");
         } finally {
            dataFile.delete();
         }

         return this.targetFolder_;
      } else {
         return dataFile;
      }
   }

   /**
    * Download.
    *
    * @param url the url
    * @return the file
    * @throws Exception the exception
    */
   private File download(URL url)
            throws Exception {
      log.debug("Beginning download from " + url);
      updateMessage("Download from " + url);

      final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

      if (StringUtils.isNotBlank(this.username_) || StringUtils.isNotBlank(this.psswrd_)) {
         final String encoded = Base64.getEncoder()
                                .encodeToString((this.username_ + ":" + this.psswrd_).getBytes());

         httpCon.setRequestProperty("Authorization", "Basic " + encoded);
      }

      httpCon.setDoInput(true);
      httpCon.setRequestMethod("GET");
      httpCon.setConnectTimeout(30 * 1000);
      httpCon.setReadTimeout(60 * 60 * 1000);

      final long   fileLength = httpCon.getContentLengthLong();
      String temp       = url.toString();

      temp = temp.substring(temp.lastIndexOf('/') + 1, temp.length());

      final File file = new File(this.targetFolder_, temp);

      try (InputStream in = httpCon.getInputStream();
         FileOutputStream fos = new FileOutputStream(file);) {
         final byte[] buf       = new byte[1048576];
         int    read      = 0;
         long   totalRead = 0;

         while (!this.cancel_ && (read = in.read(buf, 0, buf.length)) > 0) {
            totalRead += read;

            // update every 1 MB
            updateProgress(totalRead, fileLength);

            final float percentDone = ((int) (((float) totalRead / (float) fileLength) * 1000)) / 10f;

            updateMessage("Downloading - " + url + " - " + percentDone + " % - out of " + fileLength + " bytes");
            fos.write(buf, 0, read);
         }
      }

      if (this.cancel_) {
         log.debug("Download cancelled");
         throw new Exception("Cancelled!");
      } else {
         log.debug("Download complete");
      }

      return file;
   }
}

