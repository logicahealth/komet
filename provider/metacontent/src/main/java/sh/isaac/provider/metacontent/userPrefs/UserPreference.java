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



package sh.isaac.provider.metacontent.userPrefs;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.metacontent.userPrefs.StorableUserPreferences;

//~--- classes ----------------------------------------------------------------

/**
 * {@link UserPreference}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UserPreference
         implements StorableUserPreferences {
   /** The sample. */

   // TODO [DAN 3] finish this class as needed, need to see if it can be merged / replaced with the new pref store Keith added.
   private final String sample;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new user preference.
    *
    * @param data the data
    */
   public UserPreference(byte[] data) {
      this.sample = new String(data);
   }

   /**
    * Instantiates a new user preference.
    *
    * @param foo the foo
    */
   public UserPreference(String foo) {
      this.sample = foo;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Serialize.
    *
    * @return the byte[]
    * @see sh.isaac.api.metacontent.userPrefs.StorableUserPreferences#serialize()
    */
   @Override
   public byte[] serialize() {
      return this.sample.getBytes();
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.sample;
   }
}

