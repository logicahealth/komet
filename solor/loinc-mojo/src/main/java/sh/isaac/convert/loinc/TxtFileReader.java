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



package sh.isaac.convert.loinc;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.converters.sharedUtils.ConsoleUtil;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link TxtFileReader}
 *
 * A reader for various txt file formats used by LOINC.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TxtFileReader
        extends LOINCReader {
   /** The data reader. */
   BufferedReader dataReader = null;

   /** The version. */
   String version;

   /** The release date. */
   String releaseDate;

   /** The header line. */
   String headerLine;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new txt file reader.
    *
    * @param f the f
    * @throws Exception the exception
    */
   public TxtFileReader(File f)
            throws Exception {
      ConsoleUtil.println("Using the data file " + f.getAbsolutePath());
      this.dataReader = new BufferedReader(new FileReader(f));

      // Line 1 of the file is version, line 2 is date. Hope they are consistent.....
      this.version     = this.dataReader.readLine();
      this.releaseDate = this.dataReader.readLine();

      // Scan forward in the data file for the "cutoff" point
      int i = 0;

      while (true) {
         i++;

         final String temp = this.dataReader.readLine();

         if (temp.equals("<----Clip Here for Data----->")) {
            break;
         }

         if (i > 500) {
            throw new Exception(
                "Couldn't find '<----Clip Here for Data----->' constant.  Format must have changed.  Failing");
         }
      }

      this.headerLine = this.dataReader.readLine();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Close.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void close()
            throws IOException {
      this.dataReader.close();
   }

   /**
    * Read line.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public String[] readLine()
            throws IOException {
      final String line = this.dataReader.readLine();

      if ((line != null) && (line.length() > 0)) {
         return getFields(line);
      }

      return null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the fields.
    *
    * @param line the line
    * @return the fields
    */
   private String[] getFields(String line) {
      String[] temp = line.split("\\t");

      for (int i = 0; i < temp.length; i++) {
         if (temp[i].length() == 0) {
            temp[i] = null;
         } else if (temp[i].startsWith("\"") && temp[i].endsWith("\"")) {
            temp[i] = temp[i].substring(1, temp[i].length() - 1);
         }
      }

      if (this.fieldCount_ == 0) {
         this.fieldCount_ = temp.length;

         int i = 0;

         for (final String s: temp) {
            this.fieldMapInverse.put(i, s);
            this.fieldMap.put(s, i++);
         }
      } else if (temp.length < this.fieldCount_) {
         temp = Arrays.copyOf(temp, this.fieldCount_);
      } else if (temp.length > this.fieldCount_) {
         throw new RuntimeException("Data error - to many fields found on line: " + line);
      }

      return temp;
   }

   /**
    * Gets the header.
    *
    * @return the header
    */
   @Override
   public String[] getHeader() {
      return getFields(this.headerLine);
   }

   /**
    * Gets the release date.
    *
    * @return the release date
    */
   @Override
   public String getReleaseDate() {
      return this.releaseDate;
   }

   /**
    * Gets the version.
    *
    * @return the version
    */
   @Override
   public String getVersion() {
      return this.version;
   }
}

