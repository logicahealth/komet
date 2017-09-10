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



package sh.isaac.model.sememe.version;

//~--- non-JDK imports --------------------------------------------------------

import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.sememe.SememeChronologyImpl;
import sh.isaac.api.component.sememe.version.MutableLongVersion;

//~--- classes ----------------------------------------------------------------

/**
 * Used for path origins by path manager.
 * @author kec
 */
public class LongVersionImpl
        extends SememeVersionImpl
         implements MutableLongVersion {
   /** The long value. */
   long longValue = Long.MAX_VALUE;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new long sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    */
   public LongVersionImpl(SememeChronologyImpl container, int stampSequence, short versionSequence) {
      super(container, stampSequence, versionSequence);
   }

   /**
    * Instantiates a new long sememe impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param versionSequence the version sequence
    * @param data the data
    */
   public LongVersionImpl(SememeChronologyImpl container,
                         int stampSequence,
                         short versionSequence,
                         ByteArrayDataBuffer data) {
      super(container, stampSequence, versionSequence);
      this.longValue = data.getLong();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final StringBuilder sb = new StringBuilder();

      sb.append("{Long≤");
      sb.append(this.longValue);
      toString(sb);
      sb.append("≥L}");
      return sb.toString();
   }

   /**
    * Write version data.
    *
    * @param data the data
    */
   @Override
   protected void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putLong(this.longValue);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the long value.
    *
    * @return the long value
    */
   @Override
   public long getLongValue() {
      return this.longValue;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the long value.
    *
    * @param time the new long value
    */
   @Override
   public void setLongValue(long time) {
      if (this.longValue != Long.MAX_VALUE) {
         checkUncommitted();
      }

      this.longValue = time;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the sememe type.
    *
    * @return the sememe type
    */
   @Override
   public VersionType getSememeType() {
      return VersionType.LONG;
   }
}

