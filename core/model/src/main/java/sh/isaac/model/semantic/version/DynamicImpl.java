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



package sh.isaac.model.semantic.version;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.UUID;

import javax.naming.InvalidNameException;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.model.semantic.DynamicUtilityImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicTypeToClassUtility;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;
import sh.isaac.api.component.semantic.version.MutableDynamicVersion;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicImpl
        extends AbstractVersionImpl
         implements MutableDynamicVersion<DynamicImpl> {
   /** The bootstrap mode. */
   private static boolean bootstrapMode = Get.configurationService()
                                             .inBootstrapMode();

   //~--- fields --------------------------------------------------------------

   /** The dynamicData. */
   private DynamicData[] data = null;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic element impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    */
   public DynamicImpl(SemanticChronologyImpl container,
                            int stampSequence) {
      super(container, stampSequence);
   }

   /**
    * Instantiates a new dynamic element impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param data the dynamicData
    */
   public DynamicImpl(SemanticChronologyImpl container,
                            int stampSequence,
                            ByteArrayDataBuffer data) {
      super(container, stampSequence);

      // read the following format - dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
      final int colCount = data.getInt();

      this.data = new DynamicData[colCount];

      for (int i = 0; i < colCount; i++) {
         final DynamicDataType dt = DynamicDataType.getFromToken(data.getInt());

         if (dt == DynamicDataType.UNKNOWN) {
            this.data[i] = null;
         } else {
            if (data.isExternalData() && (dt == DynamicDataType.NID)) {
               final UUID temp =
                  ((DynamicUUIDImpl) DynamicTypeToClassUtility.typeToClass(DynamicDataType.UUID,
                                                                                       data.getByteArrayField(),
                                                                                       0,
                                                                                       0)).getDataUUID();

               this.data[i] = DynamicTypeToClassUtility.typeToClass(dt,
                     new DynamicNidImpl(Get.identifierService().getNidForUuids(temp)).getData(),
                     getAssemblageNid(),
                     i);
            } else {
               this.data[i] = DynamicTypeToClassUtility.typeToClass(dt,
                     data.getByteArrayField(),
                     getAssemblageNid(),
                     i);
            }
         }
      }
   }
   private DynamicImpl(DynamicImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
      this.data = new DynamicData[other.data.length];
      System.arraycopy(other.data, 0, this.data, 0, this.data.length);
   }

   @Override
   public <V extends Version> V makeAnalog(EditCoordinate ec) {
      final int stampSequence = Get.stampService()
                                   .getStampSequence(
                                       this.getStatus(),
                                       Long.MAX_VALUE,
                                       ec.getAuthorNid(),
                                       this.getModuleNid(),
                                       ec.getPathNid());
      SemanticChronologyImpl chronologyImpl = (SemanticChronologyImpl) this.chronicle;
      final DynamicImpl newVersion = new DynamicImpl(this, stampSequence);

      chronologyImpl.addVersion(newVersion);
      return (V) newVersion;   
   }


   //~--- methods -------------------------------------------------------------

   /**
    * Data to string.
    *
    * @return the string
    */
   @Override
   public String dataToString() {
      return DynamicUtilityImpl.toString(getData());
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

      final DynamicData[] dynamicData = getData();

      // make sure the column numbers are set, so lookups can happen for column names.
      for (int i = 0; i < dynamicData.length; i++) {
         if (dynamicData[i] != null) {
            dynamicData[i].configureNameProvider(getAssemblageNid(), i);
         }
      }

      sb.append(Arrays.toString(getData()));
      toString(sb);  // stamp info
      sb.append("≥DSD}");
      return sb.toString();
   }

   /**
    * Write version dynamicData.
    *
    * @param data the dynamicData
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);

      // Write with the following format -
      // dataFieldCount [dataFieldType dataFieldBytes] [dataFieldType dataFieldBytes] ...
      if (getData() != null) {
         data.putInt(getData().length);

         for (final DynamicData column: getData()) {
            if (column == null) {
               data.putInt(DynamicDataType.UNKNOWN.getTypeToken());
            } else {
               data.putInt(column.getDynamicDataType()
                                 .getTypeToken());

               if (data.isExternalData() && (column.getDynamicDataType() == DynamicDataType.NID)) {
                  final DynamicUUIDImpl temp = new DynamicUUIDImpl(
                                                        Get.identifierService().getUuidPrimordialForNid(((DynamicNidImpl) column).getDataNid()));

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
    * Gets the dynamicData.
    *
    * @return the dynamicData
    * @see sh.isaac.api.component.sememe.version.DynamicSememe#getData()
    */
   @Override
   public DynamicData[] getData() {
      return (this.data == null) ? new DynamicData[] {}
                                  : this.data;
   }

   /**
    * Gets the dynamicData.
    *
    * @param columnNumber the column number
    * @return the dynamicData
    * @throws IndexOutOfBoundsException the index out of bounds exception
    */
   @Override
   public DynamicData getData(int columnNumber)
            throws IndexOutOfBoundsException {
      return getData()[columnNumber];
   }

   /**
    * Gets the dynamicData.
    *
    * @param columnName the column name
    * @return the dynamicData
    * @throws InvalidNameException the invalid name exception
    */
   @Override
   public DynamicData getData(String columnName)
            throws InvalidNameException {
      for (final DynamicColumnInfo ci: getDynamicUsageDescription().getColumnInfo()) {
         if (ci.getColumnName()
               .equals(columnName)) {
            return getData(ci.getColumnOrder());
         }
      }

      throw new InvalidNameException("Could not find a column with name '" + columnName + "'");
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the dynamicData.
    *
    * @param data the new dynamicData
    */
   @Override
   public void setData(DynamicData[] data) {
      if (this.data != null) {
         checkUncommitted();
      }

      // TODO [DAN 3] while this checks basic sememe structure / column alignment, it can't fire certain validators, as those require coordinates.
      // The column-specific validators will have to be fired during commit.
      if (!bootstrapMode) {  // We can't run the validators when we are building the initial system.
         final DynamicUsageDescription dsud = DynamicUsageDescriptionImpl.read(getAssemblageNid());

         LookupService.get()
                      .getService(DynamicUtility.class)
                      .validate(dsud, data, getReferencedComponentNid(), getStampSequence());
      }

      this.data = (data == null) ? new DynamicData[] {}
                                  : data;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the dynamic element usage description.
    *
    * @return the dynamic element usage description
    */
   @Override
   public DynamicUsageDescription getDynamicUsageDescription() {
      return DynamicUsageDescriptionImpl.read(this.getAssemblageNid());
   }

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSemanticType() {
      return VersionType.DYNAMIC;
   }
   
   

   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      DynamicImpl otherImpl = (DynamicImpl) other;
      if (!Arrays.equals(this.data, otherImpl.data)) {
         editDistance++;
      }
      return editDistance;
   }

   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof DynamicImpl)) {
         return false;
      }
      DynamicImpl otherImpl = (DynamicImpl) other;
      return Arrays.equals(this.data, otherImpl.data);
   }
   
}

