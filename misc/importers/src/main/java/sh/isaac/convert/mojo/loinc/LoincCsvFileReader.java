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



package sh.isaac.convert.mojo.loinc;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.input.BOMInputStream;

import com.opencsv.CSVReader;

import sh.isaac.api.util.NaturalOrder;
import sh.isaac.converters.sharedUtils.ConsoleUtil;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link LoincCsvFileReader}
 *
 * Reads the CSV formatted release files of LOINC, and the custom release notes file
 * to extract the date and time information.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class LoincCsvFileReader
        extends LOINCReader {
   /** The version. */
   String version = null;

   /** The release. */
   String release = null;

   /** The version time map. */
   private final TreeMap<String, Long> versionTimeMap = new TreeMap<>(new NaturalOrder());

   /** The header. */
   String[] header;

   /** The reader. */
   CSVReader reader;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new loinc csv file reader.
    *
    * @param is the is
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public LoincCsvFileReader(InputStream is)
            throws IOException {
      // Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
      this.reader = new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(is))));
      this.header = readLine();
   }

   /**
    * Instantiates a new loinc csv file reader.
    *
    * @param f the f
    * @param populateVersionTimeMap the populate version time map
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public LoincCsvFileReader(File f, boolean populateVersionTimeMap)
            throws IOException {
      ConsoleUtil.println("Using the data file " + f.getAbsolutePath());

      // Their new format includes the (optional) UTF-8 BOM, which chokes java for stupid legacy reasons.
      this.reader =
         new CSVReader(new BufferedReader(new InputStreamReader(new BOMInputStream(new FileInputStream(f)))));
      this.header = readLine();
      readReleaseNotes(f.getParentFile(), populateVersionTimeMap);
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
    * Read line.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public String[] readLine()
            throws IOException {
      String[] temp = this.reader.readNext();

      if (temp != null) {
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
            throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
         }
      }

      return temp;
   }

   /**
    * Read release notes.
    *
    * @param dataFolder the data folder
    * @param populateVersionTimeMap the populate version time map
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @SuppressWarnings("deprecation")
   public void readReleaseNotes(File dataFolder, boolean populateVersionTimeMap)
            throws IOException {
      File relNotes = null;

      for (final File f: dataFolder.listFiles()) {
         if (f.getName()
              .toLowerCase()
              .contains("releasenotes.txt")) {
            relNotes = f;
            break;
         }
      }

      if (relNotes.exists()) {
         final SimpleDateFormat[] sdf = new SimpleDateFormat[] { new SimpleDateFormat("MMM dd, yyyy"),
                                                                 new SimpleDateFormat("MMM yyyy"),
                                                                 new SimpleDateFormat("MMMyyyy"),
                                                                 new SimpleDateFormat("MM/dd/yy") };
         try (final BufferedReader br           = new BufferedReader(new FileReader(relNotes)))
         {
        	 
         
	         String               line         = br.readLine();
	         boolean              first        = true;
	         String               versionCache = null;
	
	         while (line != null) {
	            if (line.matches("\\s*\\|\\s*Version [\\w\\.\\-\\(\\)]*\\s*\\|\\s*")) {
	               final String temp = line.substring(line.indexOf("Version") + "Version ".length());
	
	               versionCache = temp.replace('|', ' ')
	                                  .trim();
	
	               if (first) {
	                  this.version = versionCache;
	               }
	            }
	
	            if (line.matches("\\s*\\|\\s*Released [\\w\\s/,]*\\|")) {
	               String temp = line.substring(line.indexOf("Released") + "Released ".length());
	
	               temp = temp.replace('|', ' ')
	                          .trim();
	
	               if (first) {
	                  this.release = temp;
	                  first        = false;
	
	                  if (!populateVersionTimeMap) {
	                     break;
	                  }
	               }
	
	               Long time = -1l;
	
	               for (final SimpleDateFormat f: sdf) {
	                  try {
	                     time = f.parse(temp)
	                             .getTime();
	                     break;
	                  } catch (final ParseException e) {
	                     // noop
	                  }
	               }
	
	               if (time < 0) {
	                  throw new IOException("Failed to parse " + temp);
	               }
	
	               if (versionCache == null) {
	                  ConsoleUtil.printErrorln("No version for line " + line);
	               } else {
	                  this.versionTimeMap.put(versionCache, time);
	               }
	
	               versionCache = null;
	            }
	
	            line = br.readLine();
	         }
	
	         br.close();
	
	         if (populateVersionTimeMap) {
	            // release notes is missing this one...set it to a time before 2.03.
	            if (!this.versionTimeMap.containsKey("2.02")) {
	               final Date temp = new Date(this.versionTimeMap.get("2.03"));
	
	               temp.setMonth(temp.getMonth() - 1);
	               this.versionTimeMap.put("2.02", temp.getTime());
	            }
	
	            // Debug codel
	//          ConsoleUtil.println("Release / Time map read from readme file:");
	//          for (Entry<String, Long> x : versionTimeMap.entrySet())
	//          {
	//                  ConsoleUtil.println(x.getKey() + " " + new Date(x.getValue()).toString());
	//          }
	         }
         }
      } else {
         ConsoleUtil.printErrorln("Couldn't find release notes file - can't read version or release date!");
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the header.
    *
    * @return the header
    */
   @Override
   public String[] getHeader() {
      return this.header;
   }

   /**
    * Gets the release date.
    *
    * @return the release date
    */
   @Override
   public String getReleaseDate() {
      return this.release;
   }

   /**
    * Gets the time version map.
    *
    * @return the time version map
    */
   public TreeMap<String, Long> getTimeVersionMap() {
      return this.versionTimeMap;
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

