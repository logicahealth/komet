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



package sh.isaac.model.sememe.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.UUID;

import javax.naming.InvalidNameException;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.sememe.SememeType;
import sh.isaac.api.component.sememe.version.MutableDynamicSememe;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import sh.isaac.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.sememe.DynamicSememeUsageDescriptionImpl;
import sh.isaac.model.sememe.DynamicSememeUtilityImpl;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeNidImpl;
import sh.isaac.model.sememe.dataTypes.DynamicSememeTypeToClassUtility;
import sh.isaac.model.sememe.dataTypes.DynamicSememeUUIDImpl;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeImpl
        extends SememeVersionImpl<DynamicSememeImpl>
         implements MutableDynamicSememe<DynamicSememeImpl> {
   
   /** The bootstrap mode. */
   private static boolean bootstrapMode = Get.configurationService()
                                             .inBootstrapMode();

   //~--- fields --------------------------------------------------------------

   /** The data. */
   private DynamicSememeData[] data_ = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    */
   public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container,
                            int stampSequence,
                            short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   /**
    * Instantiates a new dynamic sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    * @param data the data
    */
   public DynamicSememeImpl(SememeChronologyImpl<DynamicSememeImpl> container,
                            int stampSequence,
                            short versionSequence,
                            ByteArrayDataBuffer data) {
      super(container, stampSequence, versionSequence);

      // read the following format - dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
      final int colCount = data.getInt();

      this.data_ = new DynamicSememeData[colCount];

      for (int i = 0; i < colCount; i++) {
         final DynamicSememeDataType dt = DynamicSememeDataType.getFromToken(data.getInt());

         if (dt == DynamicSememeDataType.UNKNOWN) {
            this.data_[i] = null;
         } else {
            if (data.isExternalData() && (dt == DynamicSememeDataType.NID)) {
               final UUID temp =
                  ((DynamicSememeUUIDImpl) DynamicSememeTypeToClassUtility.typeToClass(DynamicSememeDataType.UUID,
                                                                                       data.getByteArrayField(),
                                                                                       0,
                                                                                       0)).getDataUUID();

               this.data_[i] = DynamicSememeTypeToClassUtility.typeToClass(dt,
                     new DynamicSememeNidImpl(Get.identifierService().getNidForUuids(temp)).getData(),
                     getAssemblageSequence(),
                     i);
            } else {
               this.data_[i] = DynamicSememeTypeToClassUtility.typeToClass(dt,
                     data.getByteArrayField(),
                     getAssemblageSequence(),
                     i);
            }
         }
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Data to string.
    *
    * @return the string
    */
   @Override
   public String dataToString() {
      return DynamicSememeUtilityImpl.toString(getData());
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{DynamicSememeData≤");

      final DynamicSememeData[] data = getData();

      // make sure the column numbers are set, so lookups can happen for column names.
      for (int i = 0; i < data.length; i++) {
         if (data[i] != null) {
            data[i].configureNameProvider(getAssemblageSequence(), i);
         }
      }

      sb.append(Arrays.toString(getData()));
      toString(sb);  // stamp info
      sb.append("≥DSD}");
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);

      // Write with the following format -
      // dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
      if (getData() != null) {
         data.putInt(getData().length);

         for (final DynamicSememeData column: getData()) {
            if (column == null) {
               data.putInt(DynamicSememeDataType.UNKNOWN.getTypeToken());
            } else {
               data.putInt(column.getDynamicSememeDataType()
                                 .getTypeToken());

               if (data.isExternalData() && (column.getDynamicSememeDataType() == DynamicSememeDataType.NID)) {
                  final DynamicSememeUUIDImpl temp = new DynamicSememeUUIDImpl(
                                                   Get.identifierService().getUuidPrimordialForNid(
                                                      ((DynamicSememeNidImpl) column).getDataNid()).get());

                  data.putByteArrayField(temp.getData());
               } else {
                  data.putByteArrayField(column.getData());
               }
            }
         }
      } else {
         data.putInt(0);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data.
    *
    * @return the data
    * @see sh.isaac.api.component.sememe.version.DynamicSememe#getData()
    */
   @Override
   public DynamicSememeData[] getData() {
      return (this.data_ == null) ? new DynamicSememeData[] {}
                             : this.data_;
   }

   /**
    * Gets the data.
    *
    * @param columnNumber the column number
    * @return the data
    * @throws IndexOutOfBoundsException the index out of bounds exception
    */
   @Override
   public DynamicSememeData getData(int columnNumber)
            throws IndexOutOfBoundsException {
      return getData()[columnNumber];
   }

   /**
    * Gets the data.
    *
    * @param columnName the column name
    * @return the data
    * @throws InvalidNameException the invalid name exception
    */
   @Override
   public DynamicSememeData getData(String columnName)
            throws InvalidNameException {
      for (final DynamicSememeColumnInfo ci: getDynamicSememeUsageDescription().getColumnInfo()) {
         if (ci.getColumnName()
               .equals(columnName)) {
            return getData(ci.getColumnOrder());
         }
      }

      throw new InvalidNameException("Could not find a column with name '" + columnName + "'");
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the data.
    *
    * @param data the new data
    */
   @Override
   public void setData(DynamicSememeData[] data) {
      if (this.data_ != null) {
         checkUncommitted();
      }

      // TODO while this checks basic sememe structure / column alignment, it can't fire certain validators, as those require coordinates.
      // The column-specific validators will have to be fired during commit.
      if (!bootstrapMode) {  // We can't run the validators when we are building the initial system.
         final DynamicSememeUsageDescription dsud = DynamicSememeUsageDescriptionImpl.read(getAssemblageSequence());

         LookupService.get()
                      .getService(DynamicSememeUtility.class)
                      .validate(dsud, data, getReferencedComponentNid(), null, null);
      }

      this.data_ = (data == null) ? new DynamicSememeData[] {}
                             : data;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the dynamic sememe usage description.
    *
    * @return the dynamic sememe usage description
    */
   @Override
   public DynamicSememeUsageDescription getDynamicSememeUsageDescription() {
      return DynamicSememeUsageDescriptionImpl.read(this.getAssemblageSequence());
   }

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public SememeType getSememeType() {
      return SememeType.DYNAMIC;
   }
}

