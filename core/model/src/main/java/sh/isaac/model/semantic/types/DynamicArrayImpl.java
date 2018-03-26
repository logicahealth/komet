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



package sh.isaac.model.semantic.types;

//~--- JDK imports ------------------------------------------------------------

import java.lang.reflect.Array;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.HashSet;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicArrayImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @param <T> the generic type
 */
public class DynamicArrayImpl<T extends DynamicData>
        extends DynamicDataImpl
         implements DynamicArray<T> {
   /** The property. */
   private ReadOnlyObjectProperty<T[]> property;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic array impl.
    *
    * @param data the data
    */
   protected DynamicArrayImpl(byte[] data) {
      super(data);
   }

   /**
    * Instantiates a new dynamic array impl.
    *
    * @param dataArray the data array
    */
   public DynamicArrayImpl(T[] dataArray) {
      super();

      if (dataArray == null) {
         throw new RuntimeException("The dataArray cannot be null", null);
      }

      final byte[][] allData    = new byte[dataArray.length][];
      long           totalBytes = 0;

      for (int i = 0; i < dataArray.length; i++) {
         allData[i] = dataArray[i].getData();
         totalBytes += allData[i].length;
      }

      // data size + 4 bytes for the type token (per item) + 4 bytes for the length of each data item
      if ((totalBytes + (new Integer(dataArray.length).longValue() * 8l)) > Integer.MAX_VALUE) {
         throw new RuntimeException("To much data to store", null);
      }

      final int expectedDataSize = (int) totalBytes + (dataArray.length * 8);

      this.data = new byte[expectedDataSize];

      final ByteBuffer data = ByteBuffer.wrap(this.data);

      // Then, for each data item, 4 bytes for the type, 4 bytes for the int size marker of the data, then the data.
      for (int i = 0; i < dataArray.length; i++) {
         // First 4 bytes will be the type token
         data.putInt(DynamicDataType.classToType(dataArray[i].getClass())
                                          .getTypeToken());
         data.putInt(allData[i].length);
         data.put(allData[i]);
      }
   }

   /**
    * Instantiates a new dynamic array impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicArrayImpl(byte[] data, int assemblageSequence, int columnNumber) {
      super(data, assemblageSequence, columnNumber);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data array.
    *
    * @return the data array
    * @see org.ihtsdo.otf.tcc.api.DynamicSemantic.data.dataTypes.DynamicSemanticArrayBI#getDataArray()
    */
   @SuppressWarnings("unchecked")
   @Override
   public T[] getDataArray() {
      final ArrayList<T>                   result     = new ArrayList<>();
      final ByteBuffer                     bb         = ByteBuffer.wrap(this.data);
      final HashSet<DynamicDataType> foundTypes = new HashSet<>();

      while (bb.hasRemaining()) {
         final int                   type = bb.getInt();
         final DynamicDataType dt   = DynamicDataType.getFromToken(type);

         foundTypes.add(dt);

         final int    nextReadSize = bb.getInt();
         final byte[] dataArray    = new byte[nextReadSize];

         bb.get(dataArray);

         final T data = (T) DynamicTypeToClassUtility.typeToClass(dt, dataArray);

         result.add(data);
      }

      return result.toArray((T[]) Array.newInstance((foundTypes.size() > 1) ? DynamicData.class
            : DynamicTypeToClassUtility.implClassForType(foundTypes.iterator()
                  .next()), result.size()));
   }

   /**
    * Gets the data array property.
    *
    * @return the data array property
    * @see org.ihtsdo.otf.tcc.api.DynamicSemantic.data.dataTypes.DynamicSemanticArrayBI#getDataArrayProperty()
    */
   @Override
   public ReadOnlyObjectProperty<T[]> getDataArrayProperty() {
      if (this.property == null) {
         this.property = new SimpleObjectProperty<>(null, getName(), getDataArray());
      }

      return this.property;
   }

   /**
    * Gets the data object.
    *
    * @return the data object
    * @see org.ihtsdo.otf.tcc.api.DynamicSemantic.data.DynamicSemanticDataBI#getDataObject()
    */
   @Override
   public Object getDataObject() {
      return getDataArray();
   }

   /**
    * Gets the data object property.
    *
    * @return the data object property
    * @see org.ihtsdo.otf.tcc.api.DynamicSemantic.data.DynamicSemanticDataBI#getDataObjectProperty()
    */
   @Override
   public ReadOnlyObjectProperty<?> getDataObjectProperty() {
      return getDataArrayProperty();
   }
}

