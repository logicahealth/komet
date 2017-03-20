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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import sh.isaac.converters.sharedUtils.sql.TerminologyFileReader;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link RF2FileReader}
 *
 * Reads the CSV formatted release files of LOINC, and the custom release notes file
 * to extract the date and time information.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RF2FileReader
         implements TerminologyFileReader {
   /** The field count. */
   private int fieldCount = 0;

   /** The header. */
   private final String[] header;

   /** The reader. */
   private final CSVReader reader;

   /** The next. */
   private String[] next;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new RF 2 file reader.
    *
    * @param in the in
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public RF2FileReader(InputStream in)
            throws IOException {
      this.reader = new CSVReader(new BufferedReader(new InputStreamReader(in, "UTF-8")),
                                  '\t',
                                  CSVParser.NULL_CHARACTER);
      this.header = readLine();

      if (this.header != null) {
         this.next = readLine();
      }
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
      this.reader.close();
   }

   /**
    * Peek next row.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public String[] peekNextRow()
            throws IOException {
      return this.next;
   }

   /**
    * Read line.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private String[] readLine()
            throws IOException {
      String[] temp = this.reader.readNext();

      if (temp != null) {
         if (this.fieldCount == 0) {
            this.fieldCount = temp.length;
         } else if (temp.length < this.fieldCount) {
            temp = Arrays.copyOf(temp, this.fieldCount);
         } else if (temp.length > this.fieldCount) {
            throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
         }
      }

      return temp;
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
    * Gets the next row.
    *
    * @return the next row
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public List<String> getNextRow()
            throws IOException {
      if (this.next != null) {
         final List<String> temp = Arrays.asList(this.next);

         this.next = readLine();
         return temp;
      } else {
         throw new IOException("No more rows");
      }
   }

   /**
    * Checks for next row.
    *
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public boolean hasNextRow()
            throws IOException {
      return this.next != null;
   }
}

