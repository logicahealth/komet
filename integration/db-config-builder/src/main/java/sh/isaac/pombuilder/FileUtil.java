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



package sh.isaac.pombuilder;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.pom._4_0.Model;
import org.apache.maven.pom._4_0.ObjectFactory;

import sh.isaac.pombuilder.dbbuilder.DBConfigurationCreator;

//~--- classes ----------------------------------------------------------------

/**
 * {@link FileUtil}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class FileUtil {
   /** The Constant LOG. */
   private static final Logger LOG = LogManager.getLogger();

   //~--- methods -------------------------------------------------------------

   /**
    * Read file.
    *
    * @param fileName the file name
    * @return the string
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static String readFile(String fileName)
            throws IOException {
      try (InputStream is = DBConfigurationCreator.class.getResourceAsStream("/" + fileName);) {
         final byte[] buffer = new byte[is.available()];

         is.read(buffer);
         return new String(buffer, Charset.forName("UTF-8"));
      }
   }

   /**
    * Recursive delete.
    *
    * @param file the file
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void recursiveDelete(File file)
            throws IOException {
      if ((file == null) ||!file.exists()) {
         return;
      }

      Files.walkFileTree(file.toPath(),
                         new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                     throws IOException {
                               Files.delete(file);
                               return FileVisitResult.CONTINUE;
                            }
                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                     throws IOException {
                               Files.delete(dir);
                               return FileVisitResult.CONTINUE;
                            }
                         });
      file.delete();
   }

   /**
    * Write file.
    *
    * @param fromFolder the from folder
    * @param relativePath the relative path
    * @param toFolder the to folder
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void writeFile(String fromFolder, String relativePath, File toFolder)
            throws IOException {
      writeFile(fromFolder, relativePath, toFolder, null, null);
   }

   /**
    * Write file.
    *
    * @param fromFolder the from folder
    * @param relativePath the relative path
    * @param toFolder the to folder
    * @param replacementValues the replacement values
    * @param append the append
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void writeFile(String fromFolder,
                                String relativePath,
                                File toFolder,
                                HashMap<String, String> replacementValues,
                                String append)
            throws IOException {
      try (InputStream is = FileUtil.class.getResourceAsStream("/" + fromFolder + "/" + relativePath);) {
         final byte[] buffer = new byte[is.available()];

         is.read(buffer);

         String temp = new String(buffer, Charset.forName("UTF-8"));

         if (replacementValues != null) {
            for (final Entry<String, String> item: replacementValues.entrySet()) {
               while (temp.contains(item.getKey())) {
                  temp = temp.replace(item.getKey(), item.getValue());
               }
            }
         }

         if (relativePath.startsWith("DOT")) {
            relativePath = relativePath.replaceFirst("^DOT", ".");   // front of string
         } else if (relativePath.contains("/DOT")) {
            relativePath = relativePath.replaceFirst("/DOT", "/.");  // down in the relative path
         }

         final File targetFile = new File(toFolder, relativePath);

         targetFile.getParentFile()
                   .mkdirs();

         try (OutputStream outStream = new FileOutputStream(targetFile);) {
            outStream.write(temp.getBytes());

            if (StringUtils.isNotBlank(append)) {
               outStream.write(append.getBytes());
            }
         }
      }
   }

   /**
    * Write pom file.
    *
    * @param model the model
    * @param projectFolder the project folder
    * @throws Exception the exception
    */
   public static void writePomFile(Model model, File projectFolder)
            throws Exception {
      try {
         final JAXBContext ctx = JAXBContext.newInstance(Model.class);
         final Marshaller  ma  = ctx.createMarshaller();

         ma.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
         ma.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         ma.marshal(new ObjectFactory().createProject(model), new File(projectFolder, "pom.xml"));
      } catch (final JAXBException e) {
         LOG.error("Error writing", e);
         throw new Exception("Error writing pom: " + e);
      }
      catch (Exception e)
      {
         LOG.error("Error writing", e);
         throw e;
      }
   }
}

