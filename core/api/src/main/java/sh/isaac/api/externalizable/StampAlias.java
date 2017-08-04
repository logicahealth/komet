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



package sh.isaac.api.externalizable;

/**
 * The Class StampAlias.
 *
 * @author kec
 */
public final class StampAlias
         implements IsaacExternalizable {
   /** The stamp sequence. */
   int stampSequence;

   /** The stamp alias. */
   int stampAlias;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp alias.
    *
    * @param in the in
    */
   public StampAlias(ByteArrayDataBuffer in) {
      final byte version = in.getByte();

      if (version == getDataFormatVersion()) {
         this.stampSequence = StampUniversal.get(in)
                                            .getStampSequence();
         this.stampAlias    = StampUniversal.get(in)
                                            .getStampSequence();
      } else {
         throw new UnsupportedOperationException("Can't handle version: " + version);
      }
   }

   /**
    * Instantiates a new stamp alias.
    *
    * @param stampSequence the stamp sequence
    * @param stampAlias the stamp alias
    */
   public StampAlias(int stampSequence, int stampAlias) {
      this.stampSequence = stampSequence;
      this.stampAlias    = stampAlias;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Equals.
    *
    * @param o the o
    * @return true, if successful
    */
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if ((o == null) || (getClass() != o.getClass())) {
         return false;
      }

      final StampAlias that = (StampAlias) o;

      if (this.stampSequence != that.stampSequence) {
         return false;
      }

      return this.stampAlias == that.stampAlias;
   }

   /**
    * Hash code.
    *
    * @return the int
    */
   @Override
   public int hashCode() {
      int result = this.stampSequence;

      result = 31 * result + this.stampAlias;
      return result;
   }

   /**
    * Put external.
    *
    * @param out the out
    */
   @Override
   public void putExternal(ByteArrayDataBuffer out) {
      out.putByte(getDataFormatVersion());
      StampUniversal.get(this.stampSequence)
                    .writeExternal(out);
      StampUniversal.get(this.stampAlias)
                    .writeExternal(out);
   }

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return "StampAlias{" + "stampSequence=" + this.stampSequence + ", stampAlias=" + this.stampAlias + '}';
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the data format version.
    *
    * @return the data format version
    */
   @Override
   public byte getDataFormatVersion() {
      return 0;
   }

   /**
    * Gets the Isaac object type.
    *
    * @return the Isaac object type
    */
   @Override
   public IsaacExternalizableObjectType getExternalizableObjectType() {
      return IsaacExternalizableObjectType.STAMP_ALIAS;
   }

   /**
    * Gets the stamp alias.
    *
    * @return the stamp alias
    */
   public int getStampAlias() {
      return this.stampAlias;
   }

   /**
    * Gets the stamp sequence.
    *
    * @return the stamp sequence
    */
   public int getStampSequence() {
      return this.stampSequence;
   }
}

