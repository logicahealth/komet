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

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ReadOnlyObjectProperty;

//~--- interfaces -------------------------------------------------------------

/**
 * {@link DynamicSememeData}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public interface DynamicSememeData {
   /**
    * In some cases, data objects are created without the necessary data to calculate their names.
    * If necessary, the missing information can be set via this method, so that the toString and getDataXXXProperty() methods
    * can have an appropriate name in them.  This method does nothing, if it already had the information necessary to calculate
    *
    * @param assemblageSequence the assemblage sequence
    * @param columnNumber the column number
    */
   public void configureNameProvider(int assemblageSequence, int columnNumber);

   /**
    * Return a string representation of the data fields.
    *
    * @return the string
    */
   public String dataToString();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data.
    *
    * @return The data object itself, in its most compact, serialized form. You
    *         probably don't want this method unless you are doing something clever....
    *         For a getData() method that doesn't require deserialization, see the {@link #getDataObject()} method.
    *         For a method that doesn't require casting the output, see the getDataXXX() method available within
    *         implementations of the {@link DynamicSememeData} interface.
    */
   public byte[] getData();

   /**
    * Gets the data object.
    *
    * @return The data object itself.
    *         For a getData() method that doesn't  require casting of the output, see the getDataXXX() method
    *         available within implementations of the {@link DynamicSememeData} interface.
    */
   public Object getDataObject();

   /**
    * Gets the data object property.
    *
    * @return The data object itself.
    *         For a getDataProperty() method that doesn't  require casting of the output, see the getDataXXXProperty() methods
    *         available within implementations of the {@link DynamicSememeData} interface.
    */
   public ReadOnlyObjectProperty<?> getDataObjectProperty();

   /**
    * Gets the dynamic sememe data type.
    *
    * @return The type information of the data
    */
   public DynamicSememeDataType getDynamicSememeDataType();
}

