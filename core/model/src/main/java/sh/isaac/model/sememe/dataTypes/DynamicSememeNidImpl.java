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

import sh.isaac.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link DynamicSememeNidImpl}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSememeNidImpl
        extends DynamicSememeDataImpl
         implements DynamicSememeNid {
   private ObjectProperty<Integer> property_;

   //~--- constructors --------------------------------------------------------

   protected DynamicSememeNidImpl(byte[] data) {
      super(data);
   }

   public DynamicSememeNidImpl(int nid) {
      super();
      data_ = DynamicSememeIntegerImpl.intToByteArray(nid);
   }

   protected DynamicSememeNidImpl(byte[] data, int assemblageSequence, int columnNumber) {
      super(data, assemblageSequence, columnNumber);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI#getDataNid()
    */
   @Override
   public int getDataNid() {
      return DynamicSememeIntegerImpl.getIntFromByteArray(data_);
   }

   /**
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.dataTypes.RefexDynamicNidBI#getDataNidProperty()
    */
   @Override
   public ReadOnlyObjectProperty<Integer> getDataNidProperty() {
      if (property_ == null) {
         property_ = new SimpleObjectProperty<>(null, getName(), getDataNid());
      }

      return property_;
   }

   /**
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObject()
    */
   @Override
   public Object getDataObject() {
      return getDataNid();
   }

   /**
    * @see org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI#getDataObjectProperty()
    */
   @Override
   public ReadOnlyObjectProperty<?> getDataObjectProperty() {
      return getDataNidProperty();
   }
}

