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



/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package sh.isaac.api.externalizable;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import sh.isaac.api.DataSource;

//~--- enums ------------------------------------------------------------------

/**
 * The Enum IsaacObjectType. all enumns must have a token > 0 and a version > 0;
 *
 * @author kec
 */
public enum IsaacObjectType {
   /**
    * A concept. An identifier with status. Descriptions and definitions of concepts
    * are provided as SEMANTICs.
    */
   CONCEPT((byte) 1, (byte) 1, "Concept"),

   /**
    * A semantic unit of meaning, associated with a concept or another SEMANTIC.
    */
   SEMANTIC((byte) 2, (byte) 1, "Semantic"),
   
   //3 was a commit record, deprecated, should not be reused

   /**
    * A stamp comment.
    */
   STAMP_COMMENT((byte) 4, (byte) 1, "Stamp Comment"),

   /**
    * A stamp alias.
    */
   STAMP_ALIAS((byte) 5, (byte) 1, "Stamp Alias"),

   /**
    * A stamp.
    */
   STAMP((byte) 6, (byte) 1, "Stamp"),

   //7 was logical expression, deprecated, should not be reused

   /**
    * A logical expression.
    */
   UNKNOWN((byte) 128, (byte) 0, "Unknown");

   /** The token. */
   private final byte token;
   private final byte dataFormatVersion;
   private final String niceName;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new object type.
    *
    * @param token the token
    */
   private IsaacObjectType(byte token, byte dataFormatVersion, String niceName) {
      this.token             = token;
      this.dataFormatVersion = dataFormatVersion;
      this.niceName = niceName;
   }

   //~--- methods -------------------------------------------------------------

   public static IsaacObjectType fromByteArrayDataBuffer(ByteArrayDataBuffer input) {
      final byte token = input.getByte();

      return fromToken(token);
   }

   /**
    * From data stream.
    *
    * @param input the input
    * @return the Isaac object type
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static IsaacObjectType fromDataStream(DataInput input)
            throws IOException {
      final byte token = input.readByte();

      return fromToken(token);
   }

   public static IsaacObjectType fromToken(final byte token)
            throws UnsupportedOperationException {
      switch (token) {
      case 1:
         return CONCEPT;

      case 2:
         return SEMANTIC;

      case 3:
         throw new UnsupportedOperationException("Commit record deprecated: " + token);

      case 4:
         return STAMP_COMMENT;

      case 5:
         return STAMP_ALIAS;

      case 6:
         return STAMP;

      case 7:
         throw new UnsupportedOperationException("Logical Expression deprecated: " + token);

      default:
         return UNKNOWN;
      }
   }

   public void readAndValidateHeader(ByteArrayDataBuffer data) {
      byte readToken = data.getByte();

      if (this.token != readToken) {
         throw new IllegalStateException("Expecting token for: " + this + " found: " + 
                 fromToken(readToken) + "(token: " + readToken + ")");
      }

      data.setObjectDataFormatVersion(data.getByte());
      DataSource source = DataSource.fromByteArrayDataBuffer(data);
      switch (source) {
         case EXTERNAL:
            data.externalData = true;
            break;
         case INTERNAL:
            data.externalData = false;
            break;
         default:
               throw new UnsupportedOperationException("Can't handle: " + source);
      }
   }

   /**
    * Writes the data format version to the output stream, and also sets the
    * object data format version on the ByteArrayDataBuffer.
    * @param out
    */
   public void writeObjectDataFormatVersion(ByteArrayDataBuffer out) {
      out.setObjectDataFormatVersion(this.dataFormatVersion);
      out.putByte(this.dataFormatVersion);
   }

   public void writeObjectTypeToken(ByteArrayDataBuffer out) {
      out.putByte(this.token);
   }

   /**
    * Writes the type token and the data format version to the output stream, and sets the
    * object data format version on the ByteArrayDataBuffer.
    * @param out
    */
   public void writeTypeVersionHeader(ByteArrayDataBuffer out) {
      writeObjectTypeToken(out);
      writeObjectDataFormatVersion(out);
      if (out.externalData) {
         DataSource.EXTERNAL.writeDataSourceToken(out);
      } else {
         DataSource.INTERNAL.writeDataSourceToken(out);
      }
   }
   
   /**
    * Parses the.
    *
    * @param nameOrEnumId the name or enum id
    * @param exceptionOnParseFail the exception on parse fail
    * @return the object chronology type
    */
   public static IsaacObjectType parse(String nameOrEnumId, boolean exceptionOnParseFail) {
      if (nameOrEnumId == null) {
      	if (exceptionOnParseFail) {
            throw new InvalidParameterException("Could not determine ObjectChronologyType from 'null'");
         }
         return null;
      }

      final String clean = nameOrEnumId.toLowerCase(Locale.ENGLISH).trim();

      if (StringUtils.isBlank(clean)) {
      	if (exceptionOnParseFail) {
            throw new InvalidParameterException("Could not determine ObjectChronologyType from 'null'");
         }
         return null;
      }

      for (final IsaacObjectType ct: values()) {
         if (ct.name().toLowerCase(Locale.ENGLISH).equals(clean) ||
               ct.niceName.toLowerCase(Locale.ENGLISH).equals(clean) ||
               (ct.ordinal() + "").equals(clean)) {
            return ct;
         }
      }

      if (exceptionOnParseFail) {
         throw new InvalidParameterException("Could not determine ObjectChronologyType from " + nameOrEnumId);
      }

      return UNKNOWN;
   }

   //~--- get methods ---------------------------------------------------------

   public byte getDataFormatVersion() {
      return dataFormatVersion;
   }

   /**
    * Gets the token.
    *
    * @return the token
    */
   public byte getToken() {
      return this.token;
   }
   
   @Override
   public String toString() {
      return this.niceName;
   }
}

