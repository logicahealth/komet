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



package sh.isaac.convert.loinc.techPreview;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.input.BOMInputStream;

import com.opencsv.CSVReader;

//~--- classes ----------------------------------------------------------------

/**
 * The Class LoincExpressionReader.
 */
public class LoincExpressionReader {
   /** The field count. */
   protected int fieldCount = 0;

   /** The field map. */
   protected Hashtable<String, Integer> fieldMap = new Hashtable<String, Integer>();

   /** The field map inverse. */
   protected Hashtable<Integer, String> fieldMapInverse = new Hashtable<Integer, String>();

   /** The header. */
   String[] header;

   /** The reader. */
   CSVReader reader;

   /** The zip. */
   ZipFile zip;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new loinc expression reader.
    *
    * @param zipFile the zip file
    * @throws ZipException the zip exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public LoincExpressionReader(File zipFile)
            throws ZipException, IOException {
      this.zip = new ZipFile(zipFile);

      final Enumeration<? extends ZipEntry> entries = this.zip.entries();
      boolean                               found   = false;

      while (entries.hasMoreElements()) {
         final ZipEntry ze = entries.nextElement();

         if (ze.getName()
               .toLowerCase()
               .contains("xder2_sscccRefset_LOINCExpressionAssociationFull".toLowerCase())) {
            found = true;
            init(this.zip.getInputStream(ze));
            break;
         }
      }

      if (!found) {
         throw new IOException(
             "Unable to find expression refset file with the pattern 'xder2_sscccRefset_LOINCExpressionAssociationFull' in the zip file " +
             zipFile.getAbsolutePath());
      }
   }

   /**
    * Instantiates a new loinc expression reader.
    *
    * @param is the is
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public LoincExpressionReader(InputStream is)
            throws IOException {
      init(is);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Close.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public void close()
            throws IOException {
      this.reader.close();
      this.zip.close();
   }

   /**
    * Read line.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public String[] readLine()
            throws IOException {
      String[] temp = this.reader.readNext();

      if (temp != null) {
         if (this.fieldCount == 0) {
            this.fieldCount = temp.length;

            int i = 0;

            for (final String s: temp) {
               this.fieldMapInverse.put(i, s);
               this.fieldMap.put(s, i++);
            }
         } else if (temp.length < this.fieldCount) {
            temp = Arrays.copyOf(temp, this.fieldCount);
         } else if (temp.length > this.fieldCount) {
            throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
         }
      }

      return temp;
   }

   /**
    * Inits the.
    *
    * @param is the is
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private void init(InputStream is)
            throws IOException {
      this.reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(is))), '\t');
      this.header = readLine();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the header.
    *
    * @return the header
    */
   public String[] getHeader() {
      return this.header;
   }

   /**
    * Gets the position for column.
    *
    * @param col the col
    * @return the position for column
    */
   public int getPositionForColumn(String col) {
      return this.fieldMap.get(col);
   }
}

