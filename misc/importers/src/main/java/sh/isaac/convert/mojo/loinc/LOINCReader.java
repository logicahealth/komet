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



package sh.isaac.convert.mojo.loinc;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.util.Hashtable;

//~--- classes ----------------------------------------------------------------

/**
 *
 * {@link LOINCReader}
 *
 * Abstract class for the required methods of a LOINC reader - we have several, as the format has changed
 * with each release, sometimes requiring a new parser.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public abstract class LOINCReader {
   /** The field count. */
   protected int fieldCount_ = 0;

   /** The field map. */
   protected Hashtable<String, Integer> fieldMap = new Hashtable<String, Integer>();

   /** The field map inverse. */
   protected Hashtable<Integer, String> fieldMapInverse = new Hashtable<Integer, String>();

   //~--- methods -------------------------------------------------------------

   /**
    * Close.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public abstract void close()
            throws IOException;

   /**
    * Read line.
    *
    * @return the string[]
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public abstract String[] readLine()
            throws IOException;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the field map.
    *
    * @return the field map
    */
   public Hashtable<String, Integer> getFieldMap() {
      return this.fieldMap;
   }

   /**
    * Gets the field map inverse.
    *
    * @return the field map inverse
    */
   public Hashtable<Integer, String> getFieldMapInverse() {
      return this.fieldMapInverse;
   }

   /**
    * Gets the header.
    *
    * @return the header
    */
   public abstract String[] getHeader();

   /**
    * Gets the position for column.
    *
    * @param col the col
    * @return the position for column
    */
   public int getPositionForColumn(String col) {
      return this.fieldMap.get(col);
   }

   /**
    * Gets the release date.
    *
    * @return the release date
    */
   public abstract String getReleaseDate();

   /**
    * Gets the version.
    *
    * @return the version
    */
   public abstract String getVersion();
}

