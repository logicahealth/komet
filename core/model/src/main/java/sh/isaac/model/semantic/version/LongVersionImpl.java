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



package sh.isaac.model.semantic.version;

import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.MutableLongVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.model.semantic.SemanticChronologyImpl;

/**
 * Used for path origins by path manager.
 * @author kec
 */
public class LongVersionImpl
        extends AbstractVersionImpl
         implements MutableLongVersion {

   long longValue = Long.MAX_VALUE;
   
   @Override
   public StringBuilder toString(StringBuilder builder) {
      builder.append(" ")
              .append("{longValue: ").append(longValue).append(" ")
              .append(Get.stampService()
                      .describeStampSequence(this.getStampSequence())).append("}");
      return builder;
   }

   /**
    * Instantiates a new long semantic impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    */
   public LongVersionImpl(SemanticChronologyImpl container, int stampSequence) {
      super(container, stampSequence);
   }

   /**
    * Instantiates a new long semantic impl.
    *
    * @param container the container
    * @param stampSequence the stamp sequence
    * @param data the data
    */
   public LongVersionImpl(SemanticChronologyImpl container,
                         int stampSequence,
                         ByteArrayDataBuffer data) {
      super(container, stampSequence);
      this.longValue = data.getLong();
   }
   
   private LongVersionImpl(LongVersionImpl other, int stampSequence) {
      super(other.getChronology(), stampSequence);
      this.longValue = other.longValue;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <V extends Version> V makeAnalog(int stampSequence) {
      final LongVersionImpl newVersion = new LongVersionImpl(this, stampSequence);
      getChronology().addVersion(newVersion);
      return (V) newVersion;
   }

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
    * {@inheritDoc}
    */
   @Override
   public void writeVersionData(ByteArrayDataBuffer data) {
      super.writeVersionData(data);
      data.putLong(this.longValue);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long getLongValue() {
      return this.longValue;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setLongValue(long time) {
      if (this.longValue != Long.MAX_VALUE) {
         checkUncommitted();
      }

      this.longValue = time;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public final VersionType getSemanticType() {
      return VersionType.LONG;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected int editDistance3(AbstractVersionImpl other, int editDistance) {
      LongVersionImpl otherImpl = (LongVersionImpl) other;
      if (this.longValue != otherImpl.longValue) {
         editDistance++;
      }
      return editDistance;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean deepEquals3(AbstractVersionImpl other) {
      if (!(other instanceof LongVersionImpl)) {
         return false;
      }
      LongVersionImpl otherImpl = (LongVersionImpl) other;
      return this.longValue == otherImpl.longValue;
   }
}