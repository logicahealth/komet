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



package sh.isaac.model.sememe.dataTypes;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicSememeStringImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeStringImpl
        extends DynamicSememeDataImpl
         implements DynamicSememeString {
   
   /** The property. */
   private ObjectProperty<String> property_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic sememe string impl.
    *
    * @param data the data
    */
   protected DynamicSememeStringImpl(byte[] data) {
      super(data);
   }

   /**
    * Instantiates a new dynamic sememe string impl.
    *
    * @param string the string
    */
   public DynamicSememeStringImpl(String string) {
      super();

      if (string == null) {
         throw new RuntimeException("The string value cannot be null", null);
      }

      this.data_ = string.getBytes();
   }

   /**
    * Instantiates a new dynamic sememe string impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicSememeStringImpl(byte[] data, int assemblageSequence, int columnNumber) {
      super(data, assemblageSequence, columnNumber);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data object.
    *
    * @return the data object
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
    */
   @Override
   public Object getDataObject() {
      return getDataString();
   }

   /**
    * Gets the data object property.
    *
    * @return the data object property
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
    */
   @Override
   public ReadOnlyObjectProperty<?> getDataObjectProperty() {
      return getDataStringProperty();
   }

   /**
    * Gets the data string.
    *
    * @return the data string
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI#getDataString()
    */
   @Override
   public String getDataString() {
      return new String(this.data_);
   }

   /**
    * Gets the data string property.
    *
    * @return the data string property
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicStringBI#getDataStringProperty()
    */
   @Override
   public ReadOnlyObjectProperty<String> getDataStringProperty() {
      if (this.property_ == null) {
         this.property_ = new SimpleObjectProperty<>(null, getName(), getDataString());
      }

      return this.property_;
   }
}

