/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.model.observable.version;

import javafx.beans.property.LongProperty;
import sh.isaac.api.Get;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.MutableLongVersion;
import sh.isaac.api.observable.sememe.ObservableSememeChronology;
import sh.isaac.api.observable.sememe.version.ObservableLongVersion;
import sh.isaac.model.observable.CommitAwareLongProperty;
import sh.isaac.model.observable.ObservableFields;

/**
 *
 * @author kec
 */
public class ObservableLongVersionImpl 
        extends ObservableSememeVersionImpl 
        implements ObservableLongVersion {
   /** The long property. */
   LongProperty longProperty;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new observable component nid version impl.
    *
    * @param version the stamped version
    * @param chronology the chronology
    */
   public ObservableLongVersionImpl(LongVersion version,
                                    ObservableSememeChronology chronology) {
      super(version, 
              chronology);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Case significance concept sequence property.
    *
    * @return the integer property
    */
   @Override
   public LongProperty longValueProperty() {
      if (this.longProperty == null) {
         this.longProperty = new CommitAwareLongProperty(this,
               ObservableFields.LONG_VALUE_FOR_SEMEME.toExternalString(),
               getLongValue());
      }

      return this.longProperty;
   }


   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the long value.
    *
    * @return the case significance concept sequence
    */
   @Override
   public long getLongValue() {
      if (this.longProperty != null) {
         return this.longProperty.get();
      }

      return ((LongVersion) this.stampedVersion).getLongValue();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the case significance concept sequence.
    *
    * @param componentNid the new case significance concept sequence
    */
   @Override
   public void setLongValue(long longValue) {
      if (this.longProperty != null) {
         this.longProperty.set(longValue);
      } else {
         ((MutableLongVersion) this.stampedVersion).setLongValue(longValue);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String toString() {
      return "ObservableLongVersionImpl{value:" + getLongValue() + '}';
   }
}
   