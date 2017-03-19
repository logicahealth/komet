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



package sh.isaac.converters.sharedUtils.sql;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

//~--- classes ----------------------------------------------------------------

/**
 * The Class DataType.
 */
public class DataType {
   
   /** The data size. */
   private int                 dataSize_ = -1;
   
   /** The scale. */
   private int                 scale_    = -1;
   
   /** The type. */
   private SUPPORTED_DATA_TYPE type_;
   
   /** The allows null. */
   private boolean             allowsNull_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new data type.
    *
    * @param sql92Type the sql 92 type
    * @param allowsNull the allows null
    */
   public DataType(String sql92Type, Boolean allowsNull) {
      if (sql92Type.startsWith("varchar")) {
         this.type_ = SUPPORTED_DATA_TYPE.STRING;
      } else if (sql92Type.startsWith("numeric")) {
         this.type_ = SUPPORTED_DATA_TYPE.BIGDECIMAL;
      } else if (sql92Type.startsWith("integer")) {
         this.type_ = SUPPORTED_DATA_TYPE.INTEGER;
      } else if (sql92Type.startsWith("char")) {
         this.type_ = SUPPORTED_DATA_TYPE.STRING;
      } else {
         throw new RuntimeException("Not yet mapped - " + sql92Type);
      }

      final int index = sql92Type.indexOf('(');

      if ((index > 0) && (this.type_ == SUPPORTED_DATA_TYPE.STRING)) {
         this.dataSize_ = Integer.parseInt(sql92Type.substring((index + 1), sql92Type.indexOf(')', index)));
      }

      if ((index > 0) && (this.type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL)) {
         final int commaPos = sql92Type.indexOf(',', index);

         if (commaPos > 0) {
            this.dataSize_ = Integer.parseInt(sql92Type.substring(index + 1, commaPos));
            this.scale_    = Integer.parseInt(sql92Type.substring((commaPos + 1), sql92Type.indexOf(')', commaPos)));
         } else {
            this.dataSize_ = Integer.parseInt(sql92Type.substring((index + 1), sql92Type.indexOf(')', index)));
         }
      }

      if (allowsNull == null) {
         this.allowsNull_ = true;
      } else {
         this.allowsNull_ = allowsNull.booleanValue();
      }
   }

   /**
    * Instantiates a new data type.
    *
    * @param type the type
    * @param size the size
    * @param allowsNull the allows null
    */
   public DataType(SUPPORTED_DATA_TYPE type, Integer size, Boolean allowsNull) {
      this.type_ = type;

      if (size != null) {
         this.dataSize_ = size;
      }

      if (allowsNull == null) {
         this.allowsNull_ = true;
      } else {
         this.allowsNull_ = allowsNull.booleanValue();
      }
   }

   //~--- enums ---------------------------------------------------------------

   /**
    * The Enum SUPPORTED_DATA_TYPE.
    */
   public enum SUPPORTED_DATA_TYPE {
      
      /** The string. */
      STRING,
      
      /** The integer. */
      INTEGER,
      
      /** The long. */
      LONG,
      
      /** The boolean. */
      BOOLEAN,
      
      /** The bigdecimal. */
      BIGDECIMAL;

      /**
       * Parses the.
       *
       * @param value the value
       * @return the supported data type
       */
      public static SUPPORTED_DATA_TYPE parse(String value) {
         for (final SUPPORTED_DATA_TYPE s: SUPPORTED_DATA_TYPE.values()) {
            if (value.toUpperCase(Locale.ENGLISH)
                     .equals(s.name())) {
               return s;
            }
         }

         throw new RuntimeException("Unknown type " + value);
      }
   }

   ;

   //~--- methods -------------------------------------------------------------

   /**
    * As H 2.
    *
    * @return the string
    */
   public String asH2() {
      final StringBuilder sb = new StringBuilder();

      if (this.type_ == SUPPORTED_DATA_TYPE.STRING) {
         sb.append("VARCHAR ");

         if (this.dataSize_ > 0) {
            sb.append("(" + this.dataSize_ + ") ");
         }
      } else if (this.type_ == SUPPORTED_DATA_TYPE.INTEGER) {
         sb.append("INT ");
      } else if (this.type_ == SUPPORTED_DATA_TYPE.LONG) {
         sb.append("BIGINT ");
      } else if (this.type_ == SUPPORTED_DATA_TYPE.BOOLEAN) {
         sb.append("BOOLEAN ");
      } else if (this.type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL) {
         if (this.scale_ > 0) {
            sb.append("NUMERIC (" + this.dataSize_ + ", " + this.scale_ + ") ");
         } else {
            sb.append("DECIMAL (" + this.dataSize_ + ") ");
         }
      } else {
         throw new RuntimeException("not implemented");
      }

      if (!this.allowsNull_) {
         sb.append("NOT NULL");
      }

      return sb.toString();
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if big decimal.
    *
    * @return true, if big decimal
    */
   public boolean isBigDecimal() {
      return this.type_ == SUPPORTED_DATA_TYPE.BIGDECIMAL;
   }

   /**
    * Checks if boolean.
    *
    * @return true, if boolean
    */
   public boolean isBoolean() {
      return this.type_ == SUPPORTED_DATA_TYPE.BOOLEAN;
   }

   /**
    * Checks if integer.
    *
    * @return true, if integer
    */
   public boolean isInteger() {
      return this.type_ == SUPPORTED_DATA_TYPE.INTEGER;
   }

   /**
    * Checks if long.
    *
    * @return true, if long
    */
   public boolean isLong() {
      return this.type_ == SUPPORTED_DATA_TYPE.LONG;
   }

   /**
    * Checks if string.
    *
    * @return true, if string
    */
   public boolean isString() {
      return this.type_ == SUPPORTED_DATA_TYPE.STRING;
   }
}

