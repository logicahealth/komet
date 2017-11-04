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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import sh.isaac.api.component.semantic.version.dynamic.types.DynamicBoolean;

//~--- classes ----------------------------------------------------------------

/**
 * {@link DynamicBooleanImpl}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicBooleanImpl
        extends DynamicDataImpl
         implements DynamicBoolean {
   /** The property. */
   private ObjectProperty<Boolean> property;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new dynamic boolean impl.
    *
    * @param b the b
    */
   public DynamicBooleanImpl(boolean b) {
      super();
      this.data = (b ? new byte[] { 1 }
                      : new byte[] { 0 });
   }

   /**
    * Instantiates a new dynamic boolean impl.
    *
    * @param data the data
    */
   protected DynamicBooleanImpl(byte[] data) {
      super(data);
   }

   /**
    * Instantiates a new dynamic boolean impl.
    *
    * @param data the data
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   protected DynamicBooleanImpl(byte[] data, int assemblageSequence, int columnNumber) {
      super(data, assemblageSequence, columnNumber);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data boolean.
    *
    * @return the data boolean
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI#getDataBoolean()
    */
   @Override
   public boolean getDataBoolean() {
      return (this.data[0] == 1) ? true
                                  : false;
   }

   /**
    * Gets the data boolean property.
    *
    * @return the data boolean property
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicBooleanBI#getDataBooleanProperty()
    */
   @Override
   public ReadOnlyObjectProperty<Boolean> getDataBooleanProperty() {
      if (this.property == null) {
         this.property = new SimpleObjectProperty<>(null, getName(), getDataBoolean());
      }

      return this.property;
   }

   /**
    * Gets the data object.
    *
    * @return the data object
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
    */
   @Override
   public Object getDataObject() {
      return getDataBoolean();
   }

   /**
    * Gets the data object property.
    *
    * @return the data object property
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
    */
   @Override
   public ReadOnlyObjectProperty<?> getDataObjectProperty() {
      return getDataBooleanProperty();
   }
}

