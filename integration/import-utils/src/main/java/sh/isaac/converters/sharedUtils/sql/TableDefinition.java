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

import java.util.LinkedHashMap;

//~--- classes ----------------------------------------------------------------

/**
 * The Class TableDefinition.
 */
public class TableDefinition {
   /** The columns. */
   private final LinkedHashMap<String, ColumnDefinition> columns_ = new LinkedHashMap<>();

   /** The table name. */
   private final String tableName_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new table definition.
    *
    * @param tableName the table name
    */
   public TableDefinition(String tableName) {
      this.tableName_ = tableName;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Adds the column.
    *
    * @param cd the cd
    */
   public void addColumn(ColumnDefinition cd) {
      this.columns_.put(cd.getColumnName()
                          .toLowerCase(), cd);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the col data type.
    *
    * @param columnName the column name
    * @return the col data type
    */
   public DataType getColDataType(String columnName) {
      final ColumnDefinition x = this.columns_.get(columnName.toLowerCase());

      return (x == null) ? null
                         : x.getDataType();
   }

   /**
    * Gets the columns.
    *
    * @return the columns
    */
   public ColumnDefinition[] getColumns() {
      return this.columns_.values()
                          .toArray(new ColumnDefinition[this.columns_.size()]);
   }

   /**
    * Gets the table name.
    *
    * @return the table name
    */
   public String getTableName() {
      return this.tableName_;
   }
}

