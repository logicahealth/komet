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



package gov.vha.isaac.rf2.convert.mojo;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

//~--- classes ----------------------------------------------------------------

/**
 * The Class CopyMinusFileNameDate.
 *
 * @author kec
 */
@Mojo(
   name         = "add-rf2-distribution",
   defaultPhase = LifecyclePhase.INITIALIZE
)
public class CopyMinusFileNameDate
        extends AbstractMojo {
   /**
    * Location of the input source file(s).  May be a file or a directory, depending on the specific loader.
    * Usually a directory.
    */
   @Parameter(
      required     = true,
      defaultValue = "${project.basedir}/src/main/rf2-dist"
   )
   protected File rf2WithDatesLocation;

   /** Location to write the output file. */
   @Parameter(
      required     = true,
      defaultValue = "${project.basedir}/src/main/resources"
   )
   protected File outputDirectory;

   //~--- methods -------------------------------------------------------------

   /**
    * Execute.
    *
    * @throws MojoExecutionException the mojo execution exception
    * @throws MojoFailureException the mojo failure exception
    */
   @Override
   public void execute()
            throws MojoExecutionException, MojoFailureException {
      this.rf2WithDatesLocation.mkdirs();

      if (hasSubDirectory(this.rf2WithDatesLocation)) {
         try {
            delete(this.rf2WithDatesLocation);
            processDirectory(this.rf2WithDatesLocation, this.outputDirectory);
         } catch (final IOException ex) {
            Logger.getLogger(CopyMinusFileNameDate.class.getName())
                  .log(Level.SEVERE, null, ex);
         }
      }
   }

   /**
    * Delete.
    *
    * @param directory the directory
    */
   private void delete(File directory) {
      for (final File f: directory.listFiles()) {
         if (f.isDirectory()) {
            delete(f);
         }

         f.delete();
      }
   }

   /**
    * File copy.
    *
    * @param in the in
    * @param out the out
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void fileCopy(File in, File out)
            throws IOException {
      final FileChannel inChannel  = new FileInputStream(in).getChannel();
      final FileChannel outChannel = new FileOutputStream(out).getChannel();

      try {
//       inChannel.transferTo(0, inChannel.size(), outChannel);      
//        original -- apparently has trouble copying large files on Windows
//        magic number for Windows, 64Mb - 32Kb)
         final int  maxCount = (64 * 1024 * 1024) - (32 * 1024);
         final long size     = inChannel.size();
         long       position = 0;

         while (position < size) {
            position += inChannel.transferTo(position, maxCount, outChannel);
         }
      } finally {
         if (inChannel != null) {
            inChannel.close();
         }

         if (outChannel != null) {
            outChannel.close();
         }
      }
   }

   /**
    * Filter name.
    *
    * @param f the f
    * @return the string
    */
   private String filterName(File f) {
      return f.getName()
              .replaceAll("_[1-3][0-9][0-9][0-9][0-1][0-9][0-3][0-9].txt", ".txt");
   }

   /**
    * Process directory.
    *
    * @param sourceDirectory the source directory
    * @param targetDirectory the target directory
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void processDirectory(File sourceDirectory, File targetDirectory)
            throws IOException {
      targetDirectory.mkdir();

      for (final File f: sourceDirectory.listFiles()) {
         if (f.isDirectory()) {
            processDirectory(f, new File(targetDirectory, filterName(f)));
         } else {
            // fileCopy(f, new File(f, f.getName()));
            f.renameTo(new File(targetDirectory, filterName(f)));
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks for sub directory.
    *
    * @param directory the directory
    * @return true, if successful
    */
   private boolean hasSubDirectory(File directory) {
      for (final File f: directory.listFiles()) {
         if (f.isDirectory()) {
            return true;
         }
      }

      return false;
   }
}

