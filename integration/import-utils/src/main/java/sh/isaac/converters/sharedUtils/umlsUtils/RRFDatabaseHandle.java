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



package sh.isaac.converters.sharedUtils.umlsUtils;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import org.codehaus.plexus.util.StringUtils;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import sh.isaac.converters.sharedUtils.sql.ColumnDefinition;
import sh.isaac.converters.sharedUtils.sql.DataType;
import sh.isaac.converters.sharedUtils.sql.H2DatabaseHandle;
import sh.isaac.converters.sharedUtils.sql.TableDefinition;
import sh.isaac.converters.sharedUtils.sql.TerminologyFileReader;

//~--- classes ----------------------------------------------------------------

public class RRFDatabaseHandle
        extends H2DatabaseHandle {
   public void loadDataIntoTable(TableDefinition td,
                                 TerminologyFileReader data,
                                 Collection<String> SABFilterList)
            throws SQLException,
                   IOException {
      loadDataIntoTable(td, data, "SAB", SABFilterList);
   }

   /**
    * Create a set of tables that from the UMLS supplied MRCOLS
    */
   public List<TableDefinition> loadTableDefinitionsFromMRCOLS(InputStream MRFILES,
         InputStream MRCOLS,
         HashSet<String> filesToSkip)
            throws Exception {
      // MRFILEs contains fileName/Description/Comma sep col list/col count/row count/byte count
      // MRCOLs contains: col name/description_/doc section number/MIN char/AV char/MAX char/fileName/dataType
      filesToSkip.add("MRFILES.RRF");
      filesToSkip.add("MRCOLS.RRF");

      ArrayList<String> prefixSkips = new ArrayList<>();

      for (String s: filesToSkip) {
         if (s.endsWith("*")) {
            prefixSkips.add(s.substring(0, s.length() - 1));
         }
      }

      ArrayList<String[]> mrFile = new ArrayList<>();
      BufferedReader      br     = new BufferedReader(new InputStreamReader(MRFILES));

      br.lines()
        .map(line -> line.trim())
        .filter(line -> !StringUtils.isBlank(line))
        .forEach(line -> {
                    String[] temp = line.split("\\|");

                    if (temp.length > 0) {
                       mrFile.add(temp);
                    }
                 });
      br.close();

      // Filename -> col -> datatype
      HashMap<String, HashMap<String, String>> mrCol = new HashMap<>();

      br = new BufferedReader(new InputStreamReader(MRCOLS));

      String line = br.readLine();

      while (line != null) {
         String[] temp = line.split("\\|");

         if (temp.length > 0) {
            HashMap<String, String> nested = mrCol.get(temp[6]);

            if (nested == null) {
               nested = new HashMap<String, String>();
               mrCol.put(temp[6], nested);
            }

            nested.put(temp[0], temp[7]);
         }

         line = br.readLine();
      }

      br.close();

      ArrayList<TableDefinition> tables = new ArrayList<>();

      for (String[] table: mrFile) {
         String  fileName = table[0];
         boolean skip     = false;

         for (String prefix: prefixSkips) {
            if (fileName.startsWith(prefix)) {
               skip = true;
               break;
            }
         }

         if (skip || filesToSkip.contains(fileName)) {
            continue;
         }

         TableDefinition         td   = new TableDefinition(fileName.substring(0, fileName.indexOf('.')));
         HashMap<String, String> cols = mrCol.get(fileName);

         for (String col: table[2].split(",")) {
            td.addColumn(new ColumnDefinition(col, new DataType(cols.get(col), null)));
         }

         tables.add(td);
         createTable(td);
      }

      MRFILES.close();
      MRCOLS.close();
      return tables;
   }

   /**
    * Create a set of tables that from an XML file that matches the schema DatabaseDefinition.xsd
    */
   public List<TableDefinition> loadTableDefinitionsFromXML(InputStream is)
            throws Exception {
      SAXBuilder                 builder = new SAXBuilder();
      Document                   d       = builder.build(is);
      Element                    root    = d.getRootElement();
      ArrayList<TableDefinition> tables  = new ArrayList<>();

      for (Element table: root.getChildren()) {
         TableDefinition td = new TableDefinition(table.getAttributeValue("name"));

         for (Element column: table.getChildren()) {
            Integer size = null;

            if (column.getAttributeValue("size") != null) {
               size = Integer.parseInt(column.getAttributeValue("size"));
            }

            Boolean allowNull = null;

            if (column.getAttributeValue("allowNull") != null) {
               allowNull = Boolean.valueOf(column.getAttributeValue("allowNull"));
            }

            td.addColumn(new ColumnDefinition(column.getAttributeValue("name"),
                                              new DataType(
                                              DataType.SUPPORTED_DATA_TYPE.parse(column.getAttributeValue("type")),
                                              size,
                                              allowNull)));
         }

         tables.add(td);
         createTable(td);
      }

      is.close();
      return tables;
   }

   public static void main(String[] args)
            throws ClassNotFoundException, SQLException {
      RRFDatabaseHandle rrf = new RRFDatabaseHandle();

      rrf.createOrOpenDatabase(new File("/mnt/SSD/scratch/h2Test"));

      TableDefinition td = new TableDefinition("Test");

      td.addColumn(new ColumnDefinition("testcol", new DataType(DataType.SUPPORTED_DATA_TYPE.STRING, 50, true)));
      rrf.createTable(td);
      rrf.shutdown();
   }
}

