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



package sh.isaac.model.observable;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.SimpleObjectProperty;

//~--- classes ----------------------------------------------------------------

/**
 * The Class CommitAwareObjectProperty.
 *
 * @author kec
 * @param <T> the generic type
 */
public class CommitAwareObjectProperty<T>
        extends SimpleObjectProperty<T> {
   /**
    * Instantiates a new commit aware object property.
    *
    * @param bean the bean
    * @param name the name
    * @param initialValue the initial value
    */
   public CommitAwareObjectProperty(Object bean, String name, T initialValue) {
      super(bean, name, initialValue);
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set.
    *
    * @param newValue the new value
    */
   @Override
   public void set(T newValue) {
      CommitAwareIntegerProperty.checkChangesAllowed(getBean());
      super.set(newValue);
   }

   /**
    * Sets the value.
    *
    * @param v the new value
    */
   @Override
   public void setValue(T v) {
      CommitAwareIntegerProperty.checkChangesAllowed(getBean());
      super.setValue(v);
   }
}

