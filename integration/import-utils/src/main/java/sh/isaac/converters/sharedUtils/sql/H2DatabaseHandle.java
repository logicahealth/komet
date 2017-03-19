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



package sh.isaac.converters.sharedUtils.sql;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.converters.sharedUtils.ConsoleUtil;

//~--- classes ----------------------------------------------------------------

/**
 * The Class H2DatabaseHandle.
 */
public class H2DatabaseHandle {
   /** The connection. */
   protected Connection connection_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new h 2 database handle.
    */
   public H2DatabaseHandle() {
      super();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * If file provided, created or opened at that path.  If file is null, an in-memory db is created.
    * Returns false if the database already existed, true if it was newly created.
    *
    * @param dbFile the db file
    * @return true, if successful
    * @throws ClassNotFoundException the class not found exception
    * @throws SQLException the SQL exception
    */
   public boolean createOrOpenDatabase(File dbFile)
            throws ClassNotFoundException, SQLException {
      boolean createdNew = true;

      if (dbFile != null) {
         final File temp = new File(dbFile.getParentFile(), dbFile.getName() + ".h2.db");

         if (temp.exists()) {
            createdNew = false;
         }
      }

      Class.forName("org.h2.Driver");

      if (dbFile == null) {
         this.connection_ = DriverManager.getConnection("jdbc:h2:mem:;MV_STORE=FALSE");
      } else {
         this.connection_ = DriverManager.getConnection("jdbc:h2:" + dbFile.getAbsolutePath() +
               ";LOG=0;CACHE_SIZE=1024000;LOCK_MODE=0;;MV_STORE=FALSE");
      }

      return createdNew;
   }

   /**
    * Creates the table.
    *
    * @param td the td
    * @throws SQLException the SQL exception
    */
   public void createTable(TableDefinition td)
            throws SQLException {
      final Statement     s         = this.connection_.createStatement();
      final StringBuilder sql       = new StringBuilder();
      String              tableName = td.getTableName();

      if (tableName.indexOf('/') > 0) {
         tableName = tableName.substring(tableName.indexOf('/') + 1);
      }

      sql.append("CREATE TABLE " + tableName + " (");

      for (final ColumnDefinition cd: td.getColumns()) {
         sql.append(cd.asH2());
         sql.append(",");
      }

      sql.setLength(sql.length() - 1);
      sql.append(")");
      ConsoleUtil.println("Creating Table " + tableName);
      s.executeUpdate(sql.toString());
   }

   /**
    * Load data into table.
    *
    * @param td the td
    * @param data the data
    * @return the int
    * @throws SQLException the SQL exception
    * @throws IOException Signals that an I/O exception has occurred.
    * @returns rowCount loaded
    */
   public int loadDataIntoTable(TableDefinition td, TerminologyFileReader data)
            throws SQLException, IOException {
      return loadDataIntoTable(td, data, null, null);
   }

   /**
    * Load data into table.
    *
    * @param td the td
    * @param data the data
    * @param includeValuesColumnName - (optional) the name of the column to check for an include values filter
    * @param includeValues - (optional) - the values to include.  If this parameter, and the above parameter are specified, only rows which have
    * a column name that matches 'includeValuesColumnName' with a value from the set of 'includeValues" will be loaded.
    * @return row count loaded
    * @throws SQLException the SQL exception
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public int loadDataIntoTable(TableDefinition td,
                                TerminologyFileReader data,
                                String includeValuesColumnName,
                                Collection<String> includeValues)
            throws SQLException,
                   IOException {
      ConsoleUtil.println("Loading table " + td.getTableName());

      final StringBuilder insert = new StringBuilder();

      insert.append("INSERT INTO ");

      String tableName = td.getTableName();

      if (tableName.indexOf('/') > 0) {
         tableName = tableName.substring(tableName.indexOf('/') + 1);
      }

      insert.append(tableName);
      insert.append("(");

      for (final ColumnDefinition cd: td.getColumns()) {
         insert.append(cd.getColumnName());
         insert.append(",");
      }

      insert.setLength(insert.length() - 1);
      insert.append(") VALUES (");

      for (int i = 0; i < td.getColumns().length; i++) {
         insert.append("?,");
      }

      insert.setLength(insert.length() - 1);
      insert.append(")");

      final PreparedStatement ps           = this.connection_.prepareStatement(insert.toString());
      int                     filterColumn = -1;
      HashSet<String>         sabHashSet   = null;

      if ((includeValues != null) && (includeValues.size() > 0) && (includeValuesColumnName != null)) {
         sabHashSet = new HashSet<>(includeValues);

         int pos = 0;

         // Find the skip column in this table, if it has one.
         for (final ColumnDefinition cd: td.getColumns()) {
            if (cd.getColumnName()
                  .equalsIgnoreCase(includeValuesColumnName)) {
               filterColumn = pos;
               break;
            }

            pos++;
         }
      }

      int                   rowCount     = 0;
      int                   sabSkipCount = 0;
      final HashSet<String> skippedSabs  = new HashSet<>();

      while (data.hasNextRow()) {
         final List<String> cols = data.getNextRow();

         if (cols.size() != td.getColumns().length) {
            throw new RuntimeException("Data length mismatch!");
         }

         if ((sabHashSet != null) && (filterColumn >= 0)) {
            if (!sabHashSet.contains(cols.get(filterColumn))) {
               skippedSabs.add(cols.get(filterColumn));
               sabSkipCount++;
               continue;
            }
         }

         ps.clearParameters();

         int psIndex = 1;

         for (final String s: cols) {
            final DataType colType = td.getColumns()[psIndex - 1]
                                       .getDataType();

            if (colType.isBoolean()) {
               if ((s == null) || (s.length() == 0)) {
                  ps.setNull(psIndex, Types.BOOLEAN);
               } else {
                  ps.setBoolean(psIndex, (s.equalsIgnoreCase("true") || s.equals("1")));
               }
            } else if (colType.isInteger()) {
               if ((s == null) || (s.length() == 0)) {
                  ps.setNull(psIndex, Types.INTEGER);
               } else {
                  ps.setInt(psIndex, Integer.parseInt(s));
               }
            } else if (colType.isLong()) {
               if ((s == null) || (s.length() == 0)) {
                  ps.setNull(psIndex, Types.BIGINT);
               } else {
                  ps.setLong(psIndex, Long.parseLong(s));
               }
            } else if (colType.isString()) {
               if ((s == null) || (s.length() == 0)) {
                  ps.setNull(psIndex, Types.VARCHAR);
               } else {
                  ps.setString(psIndex, s);
               }
            } else if (colType.isBigDecimal()) {
               if ((s == null) || (s.length() == 0)) {
                  ps.setNull(psIndex, Types.DECIMAL);
               } else {
                  ps.setBigDecimal(psIndex, new BigDecimal(s));
               }
            } else {
               throw new RuntimeException("Unsupported data type");
            }

            psIndex++;
         }

         ps.execute();
         rowCount++;

         if (rowCount % 10000 == 0) {
            ConsoleUtil.showProgress();
         }
      }

      ps.close();
      data.close();
      ConsoleUtil.println("Loaded " + rowCount + " rows");

      if (sabSkipCount > 0) {
         ConsoleUtil.println("Skipped " + sabSkipCount + " rows for not matching the include filter - " +
                             Arrays.toString(skippedSabs.toArray(new String[] {})));
      }

      return rowCount;
   }

   /**
    * Shutdown.
    *
    * @throws SQLException the SQL exception
    */
   public void shutdown()
            throws SQLException {
      this.connection_.close();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the connection.
    *
    * @return the connection
    */
   public Connection getConnection() {
      return this.connection_;
   }
}

