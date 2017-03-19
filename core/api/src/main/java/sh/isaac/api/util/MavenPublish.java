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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.util.Base64;
import java.util.concurrent.ExecutionException;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.value.ChangeListener;

import javafx.concurrent.Task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- classes ----------------------------------------------------------------

/**
 * {@link MavenPublish}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MavenPublish
        extends Task<Integer> {
   /** The log. */
   private static Logger log = LoggerFactory.getLogger(MavenPublish.class);

   //~--- fields --------------------------------------------------------------

   /** The group id. */
   String groupId_;

   /** The artifact id. */
   String artifactId_;

   /** The version. */
   String version_;

   /** The pom file. */
   File pomFile_;

   /** The data files. */
   File[] dataFiles_;

   /** The url. */
   String url_;

   /** The username. */
   String username_;

   /** The psswrd. */
   String psswrd_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new maven publish.
    *
    * @param groupId the group id
    * @param artifactId the artifact id
    * @param version the version
    * @param pomFile the pom file
    * @param dataFiles the data files
    * @param url the url
    * @param username the username
    * @param psswrd the psswrd
    * @throws Exception the exception
    */
   public MavenPublish(String groupId,
                       String artifactId,
                       String version,
                       File pomFile,
                       File[] dataFiles,
                       String url,
                       String username,
                       String psswrd)
            throws Exception {
      this.groupId_    = groupId;
      this.artifactId_ = artifactId;
      this.version_    = version;
      this.pomFile_    = pomFile;
      this.dataFiles_  = dataFiles;
      this.url_        = url;
      this.username_   = username;
      this.psswrd_     = psswrd;
      log.debug("Maven Publish task constructed for GAV: {}:{}:{}", groupId, artifactId, version);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Call.
    *
    * @return the integer
    * @throws Exception the exception
    * @see javafx.concurrent.Task#call()
    */
   @Override
   protected Integer call()
            throws Exception {
      log.debug("Maven publish task begins");
      updateProgress(-1, 0);
      updateMessage("Creating Checksum Files");
      writeChecksumFile(this.pomFile_, "MD5");
      writeChecksumFile(this.pomFile_, "SHA1");

      for (final File f: this.dataFiles_) {
         writeChecksumFile(f, "MD5");
         writeChecksumFile(f, "SHA1");
      }

      updateMessage("Uploading data files");

      for (final File f: this.dataFiles_) {
         // TODO check maven upload order
         putFile(f, null);
         putFile(new File(f.getParentFile(), f.getName() + ".md5"), null);
         putFile(new File(f.getParentFile(), f.getName() + ".sha1"), null);
      }

      updateMessage("Uploading pom files");
      putFile(this.pomFile_, "pom");
      putFile(new File(this.pomFile_.getParentFile(), this.pomFile_.getName() + ".md5"), "pom.md5");
      putFile(new File(this.pomFile_.getParentFile(), this.pomFile_.getName() + ".sha1"), "pom.sha1");
      updateMessage("Publish Complete");
      updateProgress(10, 10);
      log.debug("Maven Publish Task Complete");
      return 0;
   }

   /**
    * Put file.
    *
    * @param file the file
    * @param targetFileName the target file name
    * @throws Exception the exception
    */
   private void putFile(File file, String targetFileName)
            throws Exception {
      final String groupIdTemp = this.groupId_.replaceAll("\\.", "//");
      final URL    url         = new URL(this.url_ + (this.url_.endsWith("/") ? ""
            : "/") + groupIdTemp + "/" + this.artifactId_ + "/" + this.version_ + "/" +
                     ((targetFileName == null) ? file.getName()
            : targetFileName));

      log.info("Uploading " + file.getAbsolutePath() + " to " + url.toString());
      updateMessage("Uploading " + file.getName());
      updateProgress(0, file.length());

      final HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

      if ((this.username_.length() > 0) || (this.psswrd_.length() > 0)) {
         final String encoded = Base64.getEncoder()
                                      .encodeToString((this.username_ + ":" + this.psswrd_).getBytes());

         httpCon.setRequestProperty("Authorization", "Basic " + encoded);
      }

      httpCon.setDoOutput(true);
      httpCon.setRequestMethod("PUT");
      httpCon.setConnectTimeout(30 * 1000);
      httpCon.setReadTimeout(60 * 60 * 1000);

      final long fileLength = file.length();

      httpCon.setFixedLengthStreamingMode(fileLength);

      final byte[] buf       = new byte[8192];
      long         loopCount = 0;
      int          read      = 0;

      try (OutputStream out = httpCon.getOutputStream();
         FileInputStream fis = new FileInputStream(file);) {
         while ((read = fis.read(buf, 0, buf.length)) > 0) {
            // update every MB
            if (loopCount++ % 128 == 0) {
               updateProgress((loopCount * 8192l), fileLength);
               updateMessage("Uploading " + file.getName() + " - " + (loopCount * 8192l) + " / " + fileLength);
            }

            out.write(buf, 0, read);
         }

         out.flush();
      }

      final StringBuilder sb = new StringBuilder();

      try (InputStream is = httpCon.getInputStream();) {
         read = 0;

         final byte[]     buffer  = new byte[1024];
         final CharBuffer cBuffer = ByteBuffer.wrap(buffer)
                                              .asCharBuffer();

         while (read != -1) {
            read = is.read(buffer);

            if (read > 0) {
               sb.append(cBuffer, 0, read);
            }
         }
      }

      httpCon.disconnect();

      if (sb.toString()
            .trim()
            .length() > 0) {
         throw new Exception("The server reported an error during the publish operation:  " + sb.toString());
      }

      log.info("Upload Successful");
      updateMessage("");
      updateProgress(-1, 0);
   }

   /**
    * Write checksum file.
    *
    * @param file the file
    * @param type the type
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws InterruptedException the interrupted exception
    * @throws ExecutionException the execution exception
    */
   private void writeChecksumFile(File file, String type)
            throws IOException, InterruptedException, ExecutionException {
      updateMessage("Calculating Checksum for " + file.getName());

      final Task<String> gen = ChecksumGenerator.calculateChecksum(type, file);

      gen.messageProperty()
         .addListener((ChangeListener<String>) (observable, oldValue, newValue) -> updateMessage(newValue));
      gen.progressProperty()
         .addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> updateProgress(gen.getWorkDone(),
               gen.getTotalWork()));
      WorkExecutors.get()
                   .getExecutor()
                   .execute(gen);

      final String checksum = gen.get();

      updateMessage("Writing checksum file");
      log.debug("Writing {} checksum file with {}", type, checksum);
      Files.write(new File(file.getParentFile(), file.getName() + "." + type.toLowerCase()).toPath(),
                  (checksum + "  " + file.getName()).getBytes(),
                  StandardOpenOption.WRITE,
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING);
      updateMessage("");
      updateProgress(-1, 0);
   }
}

