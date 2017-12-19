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



package sh.isaac.api.component.semantic.version;

//~--- JDK imports ------------------------------------------------------------

import javax.naming.InvalidNameException;
import sh.isaac.api.chronicle.VersionType;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;

//~--- interfaces -------------------------------------------------------------

/**
 * The Interface DynamicVersion.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * @param <T> the generic type
 */
public interface DynamicVersion<T extends DynamicVersion<T>>
        extends SemanticVersion {
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
    * @return All of the data columns that are part of this DynamicVersion. See
         {@link #getData(int)}. May be empty, will not be null.
    */
   DynamicData[] getData();

   /**
    * The type and data (if any) in the specified column of the DynamicVersion.
    *
    * @param columnNumber the column number
    * @return The DynamicData which contains the type and data (if any) for
    *         the specified column
    * @throws IndexOutOfBoundsException the index out of bounds exception
    */
   DynamicData getData(int columnNumber)
            throws IndexOutOfBoundsException;

   /**
    * The type and data (if any) in the specified column of the DynamicVersion.
    *
    * @param columnName the column name
    * @return The DynamicData which contains the type and data (if any) for the specified column
    * @throws InvalidNameException the invalid name exception
    */
   DynamicData getData(String columnName)
            throws InvalidNameException;

   /**
    * A convenience method that reads the concept referenced in {@link #getAssemblageNid()} and returns the actual column
    * information that is contained within that concept.
    *
    * @return the dynamic usage description
    */
   public DynamicUsageDescription getDynamicUsageDescription();

   @Override
   default VersionType getSemanticType() {
      return VersionType.DYNAMIC;
   }
}

