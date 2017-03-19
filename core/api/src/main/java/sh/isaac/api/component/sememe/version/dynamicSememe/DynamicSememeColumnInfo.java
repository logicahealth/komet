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



package sh.isaac.api.component.sememe.version.dynamicSememe;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.LookupService;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeColumnInfo}
 *
 * A user friendly class for containing the information parsed out of the Assemblage concepts which defines the DynamicSememe.
 * See the class description for {@link DynamicSememeUsageDescriptionBI} for more details.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeColumnInfo
         implements Comparable<DynamicSememeColumnInfo> {
   private UUID             columnDescriptionConceptUUID_;
   private transient String columnName_;
   private transient String columnDescription_;
   private transient Boolean indexColumn_;  // This is not populated by default, nor is it stored.  Typically used to pass data from a constant, rather

   // than run-time lookup of the index configuration.
   private int                          columnOrder_;
   private UUID                         assemblageConcept_;
   private DynamicSememeDataType        columnDataType_;
   private DynamicSememeData            defaultData_;
   private boolean                      columnRequired_;
   private DynamicSememeValidatorType[] validatorType_;
   private DynamicSememeData[]          validatorData_;

   //~--- constructors --------------------------------------------------------

   /**
    * Useful for building up a new one step by step
    */
   public DynamicSememeColumnInfo() {}

   /**
    * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType[], DynamicSememeDataBI[])
    * with a null assemblage concept, null validator info
    */
   public DynamicSememeColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicSememeDataType columnDataType,
                                  DynamicSememeData defaultData,
                                  Boolean columnRequired,
                                  Boolean index) {
      this(null, columnOrder, columnDescriptionConcept, columnDataType, defaultData, columnRequired, null, null, index);
   }

   /**
    * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType[], DynamicSememeDataBI[])
    * with a null assemblage concept, and a single array item for the validator info
    */
   public DynamicSememeColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicSememeDataType columnDataType,
                                  DynamicSememeData defaultData,
                                  Boolean columnRequired,
                                  DynamicSememeValidatorType validatorType,
                                  DynamicSememeData validatorData,
                                  Boolean index) {
      this(null,
           columnOrder,
           columnDescriptionConcept,
           columnDataType,
           defaultData,
           columnRequired,
           (validatorType == null) ? null
                                   : new DynamicSememeValidatorType[] { validatorType },
           (validatorData == null) ? null
                                   : new DynamicSememeData[] { validatorData },
           index);
   }

   /**
    * calls {@link #DynamicSememeColumnInfo(UUID, int, UUID, DynamicSememeDataType, DynamicSememeDataBI, Boolean, DynamicSememeValidatorType, DynamicSememeDataBI)
    * with a null assemblage concept
    */
   public DynamicSememeColumnInfo(int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicSememeDataType columnDataType,
                                  DynamicSememeData defaultData,
                                  Boolean columnRequired,
                                  DynamicSememeValidatorType[] validatorType,
                                  DynamicSememeData[] validatorData,
                                  Boolean index) {
      this(null,
           columnOrder,
           columnDescriptionConcept,
           columnDataType,
           defaultData,
           columnRequired,
           validatorType,
           validatorData,
           index);
   }

   /**
    * Create this object by reading the columnName and columnDescription from the provided columnDescriptionConcept.
    *
    * If a suitable concept to use for the column Name/Description does not yet exist, see
    * {@link DynamicSememeColumnInfo#createNewDynamicSememeColumnInfoConcept(String, String)}
    *
    * and pass the result in here.
    *
    * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
    * @param columnOrder - the column order as defined in the assemblage concept
    * @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
    * @param columnDataType - the data type as defined in the assemblage concept
    * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example,
    * if columnDataType is set to {@link DynamicSememeDataType#FLOAT} then this field must be a {@link DynamicSememeFloat}.
    * @param columnRequired - Is this column required when creating an instance of the refex?  True for yes, false or null for no.
    * @param validatorType - The Validator to use when creating an instance of this Refex.  Null for no validator
    * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the
    * validatorType field.  See {@link DynamicSememeValidatorType} for details on the valid data for this field.  Should be null when validatorType is null.
    * @param index - set to true, if this column should be indexed.
    */
   public DynamicSememeColumnInfo(UUID assemblageConcept,
                                  int columnOrder,
                                  UUID columnDescriptionConcept,
                                  DynamicSememeDataType columnDataType,
                                  DynamicSememeData defaultData,
                                  Boolean columnRequired,
                                  DynamicSememeValidatorType[] validatorType,
                                  DynamicSememeData[] validatorData,
                                  Boolean index) {
      this.assemblageConcept_            = assemblageConcept;
      this.columnOrder_                  = columnOrder;
      this.columnDescriptionConceptUUID_ = columnDescriptionConcept;
      this.columnDataType_               = columnDataType;
      this.defaultData_                  = defaultData;
      this.columnRequired_               = ((columnRequired == null) ? false
            : columnRequired);
      this.validatorType_                = validatorType;
      this.validatorData_                = validatorData;
      this.indexColumn_                  = index;
   }

   //~--- methods -------------------------------------------------------------

   /*
    * @see java.lang.Comparable#compareTo(java.lang.Object)
    */
   @Override
   public int compareTo(DynamicSememeColumnInfo o) {
      return Integer.compare(this.getColumnOrder(), o.getColumnOrder());
   }

   @Override
   public String toString() {
      return "DynamicSememeColumnInfo [columnName_=" + this.columnName_ + ", columnDescription_=" + this.columnDescription_ +
             ", columnOrder_=" + this.columnOrder_ + ", assemblageConcept_=" + this.assemblageConcept_ + ", columnDataType_=" +
             this.columnDataType_ + ", defaultData_=" + this.defaultData_ + ", columnRequired_=" + this.columnRequired_ +
             ", validatorType_=" + Arrays.toString(this.validatorType_) + ", validatorData_=" +
             Arrays.toString(this.validatorData_) + "]";
   }

   private void read() {
      final DynamicSememeColumnUtility util = LookupService.get()
                                                     .getService(DynamicSememeColumnUtility.class);

      if (util == null) {
         this.columnName_        = "Unable to locate reader!";
         this.columnDescription_ = "Unable to locate reader!";
      } else {
         final String[] temp = util.readDynamicSememeColumnNameDescription(this.columnDescriptionConceptUUID_);

         this.columnName_        = temp[0];
         this.columnDescription_ = temp[1];
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the UUID of the assemblage concept that this column data was read from
    * or null in the case where this column is not yet associated with an assemblage.
    */
   public UUID getAssemblageConcept() {
      return this.assemblageConcept_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param assemblageConcept - the assemblage concept that this was read from (or null, if not yet part of an assemblage)
    */
   public void setAssemblageConcept(UUID assemblageConcept) {
      this.assemblageConcept_ = assemblageConcept;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return The defined data type for this column of the sememe.  Note that this value will be identical to the {@link DynamicSememeDataType}
    * returned by {@link DynamicSememeData} EXCEPT for cases where this returns {@link DynamicSememeDataType#POLYMORPHIC}.  In those cases, the
    * data type can only be determined by examining the actual member data in {@link DynamicSememeData}
    */
   public DynamicSememeDataType getColumnDataType() {
      return this.columnDataType_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param columnDataType - the data type as defined in the assemblage concept
    */
   public void setColumnDataType(DynamicSememeDataType columnDataType) {
      this.columnDataType_ = columnDataType;
   }

   /**
    * @param defaultData - The type of this Object must align with the data type specified in columnDataType.  For example,
    * if columnDataType is set to {@link DynamicSememeDataType#FLOAT} then this field must be a {@link DynamicSememeFloat}.
    */
   public void setColumnDefaultData(DynamicSememeData defaultData) {
      this.defaultData_ = defaultData;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return The user-friendly description of this column of data.  To be used by GUIs to provide a more detailed explanation of
    * the type of data found in this column.
    */
   public String getColumnDescription() {
      if (this.columnDescription_ == null) {
         read();
      }

      return this.columnDescription_;
   }

   /**
    * @return The UUID of the concept where the columnName and columnDescription were read from.
    */
   public UUID getColumnDescriptionConcept() {
      return this.columnDescriptionConceptUUID_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param columnDescriptionConcept - The concept where columnName and columnDescription should be read from
    */
   public void setColumnDescriptionConcept(UUID columnDescriptionConcept) {
      this.columnDescriptionConceptUUID_ = columnDescriptionConcept;
      this.columnName_                   = null;
      this.columnDescription_            = null;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return The user-friendly name of this column of data.  To be used by GUIs to label the data in this column.
    */
   public String getColumnName() {
      if (this.columnName_ == null) {
         read();
      }

      return this.columnName_;
   }

   /**
    * @return Defined the order in which the data columns will be stored, so that the column name / description can be aligned
    * with the {@link DynamicSememeData} columns in the {@link DynamicSememeVersionBI#getData(int)}.
    *
    * Note, this value is 0 indexed (It doesn't start at 1)
    */
   public int getColumnOrder() {
      return this.columnOrder_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param columnOrder - the column order as defined in the assemblage concept
    */
   public void setColumnOrder(int columnOrder) {
      this.columnOrder_ = columnOrder;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return When creating this refex, must this column be provided?
    */
   public boolean isColumnRequired() {
      return this.columnRequired_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param columnRequired - Is this column required when creating an instance of the sememe?  True for yes, false or null for no.
    */
   public void setColumnRequired(boolean columnRequired) {
      this.columnRequired_ = columnRequired;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @return the default value to use for this column, if no value is specified in a refex that is created using this column info
    */
   public DynamicSememeData getDefaultColumnValue() {
      // Handle folks sending empty strings gracefully
      if ((this.defaultData_ != null) &&
            (this.defaultData_ instanceof DynamicSememeString) &&
            ((DynamicSememeString) this.defaultData_).getDataString().length() == 0) {
         return null;
      }

      return this.defaultData_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * This is typically used in the metadata code to pass in the initial index configuration for a column.  It has no impact on
    * the actual config at runtime, in a running system.
    */
   public void setDefaultIndexConfig(boolean indexColumn) {
      this.indexColumn_ = indexColumn;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Return true, if this column is currently configured for indexing, otherwise false.  Note, this is not currently implemented runtime usage
    * and will instead throw an UnsupportedOperationException.  This is only used in the construction of metadata concepts.
    */
   public boolean getIndexConfig() {
      if (this.indexColumn_ == null) {
         throw new UnsupportedOperationException(
             "Convenience method to read current index config from lucene indexer not yet implemented");
      } else {
         return this.indexColumn_;
      }
   }

   /**
    * @return The type of the validator(s) (if any) which must be used to validate user data before accepting the refex
    */
   public DynamicSememeValidatorType[] getValidator() {
      return this.validatorType_;
   }

   /**
    * @param validatorData - The data required to execute the validatorType specified.  The format and type of this will depend on the
    * validatorType field.  See {@link DynamicSememeValidatorType} for details on the valid data for this field.  Should be null when validatorType is null.
    */
   public DynamicSememeData[] getValidatorData() {
      return this.validatorData_;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * @param validatorType - The Validator(s) to use when creating an instance of this Refex.  Null for no validator
    */
   public void setValidatorData(DynamicSememeData[] validatorData) {
      this.validatorData_ = validatorData;
   }

   /**
    * @param validatorType - The Validator(s) to use when creating an instance of this sememe.  Null for no validator
    */
   public void setValidatorType(DynamicSememeValidatorType[] validatorType) {
      this.validatorType_ = validatorType;
   }
}

