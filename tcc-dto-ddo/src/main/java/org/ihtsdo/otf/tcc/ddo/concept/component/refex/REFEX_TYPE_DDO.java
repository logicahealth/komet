package org.ihtsdo.otf.tcc.ddo.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_boolean.RefexNidBooleanVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_float.RefexNidNidNidFloatVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_int.RefexNidNidNidIntVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_long.RefexNidNidNidLongVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_nid_string.RefexNidNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.otf.tcc.api.refex.type_string.RefexStringVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Enum description
 *
 */
public enum REFEX_TYPE_DDO {

   /**
    * COMP = Component IDentifier
    *
    * @author kec
    *
    */
   MEMBER(1, RefexVersionBI.class), COMP(2, RefexNidVersionBI.class),
                                    COMP_COMP(3, RefexNidNidVersionBI.class),
                                    COMP_COMP_COMP(4, RefexNidNidNidVersionBI.class),
                                    COMP_COMP_STR(5, RefexNidNidStringVersionBI.class),
                                    STR(6, RefexStringVersionBI.class), INT(7, RefexIntVersionBI.class),
                                    COMP_INT(8, RefexNidIntVersionBI.class),
                                    BOOLEAN(9, RefexBooleanVersionBI.class),
                                    COMP_STR(10, RefexNidStringVersionBI.class),
                                    COMP_FLOAT(11, RefexNidFloatVersionBI.class),
                                    COMP_LONG(12, RefexNidLongVersionBI.class),
                                    LONG(13, RefexLongVersionBI.class),
                                    ARRAY_BYTEARRAY(14, RefexArrayOfBytearrayVersionBI.class),
                                    COMP_COMP_COMP_FLOAT(15, RefexNidNidNidFloatVersionBI.class),
                                    COMP_COMP_COMP_INT(16, RefexNidNidNidIntVersionBI.class),
                                    COMP_COMP_COMP_LONG(17, RefexNidNidNidLongVersionBI.class),
                                    COMP_COMP_COMP_STRING(18, RefexNidNidNidStringVersionBI.class),
                                    COMP_BOOLEAN(19, RefexNidBooleanVersionBI.class),
                                    UNKNOWN(Byte.MAX_VALUE, null);

   /** Field description */
   private int externalizedToken;

   /** Field description */
   private Class<? extends RefexVersionBI> rxc;

   /**
    * Constructs ...
    *
    *
    * @param externalizedToken
    * @param rxc
    */
   REFEX_TYPE_DDO(int externalizedToken, Class<? extends RefexVersionBI> rxc) {
      this.externalizedToken = externalizedToken;
      this.rxc               = rxc;
   }

   /**
    * Method description
    *
    *
    * @param c
    *
    * @return
    */
   public static REFEX_TYPE_DDO classToType(Class<?> c) {
      if (RefexNidNidNidVersionBI.class.isAssignableFrom(c)) {
         return COMP_COMP_COMP;
      }

      if (RefexNidNidStringVersionBI.class.isAssignableFrom(c)) {
         return COMP_COMP_STR;
      }

      if (RefexNidNidVersionBI.class.isAssignableFrom(c)) {
         return COMP_COMP;
      }

      if (RefexNidIntVersionBI.class.isAssignableFrom(c)) {
         return COMP_INT;
      }

      if (RefexNidStringVersionBI.class.isAssignableFrom(c)) {
         return COMP_STR;
      }

      if (RefexNidFloatVersionBI.class.isAssignableFrom(c)) {
         return COMP_FLOAT;
      }

      if (RefexNidLongVersionBI.class.isAssignableFrom(c)) {
         return COMP_LONG;
      }

      if (RefexBooleanVersionBI.class.isAssignableFrom(c)) {
         return BOOLEAN;
      }

      if (RefexNidVersionBI.class.isAssignableFrom(c)) {
         return COMP;
      }

      if (RefexStringVersionBI.class.isAssignableFrom(c)) {
         return STR;
      }

      if (RefexIntVersionBI.class.isAssignableFrom(c)) {
         return INT;
      }

      if (RefexLongVersionBI.class.isAssignableFrom(c)) {
         return LONG;
      }

      if (RefexVersionBI.class.isAssignableFrom(c)) {
         return MEMBER;
      }

      return UNKNOWN;
   }

   /**
    * Method description
    *
    *
    * @param input
    *
    * @return
    *
    * @throws IOException
    */
   public static REFEX_TYPE_DDO readType(DataInput input) throws IOException {
      int type = input.readByte();

      return getFromToken(type);
   }

   /**
    * Method description
    *
    *
    * @param output
    *
    * @throws IOException
    */
   public void writeType(DataOutput output) throws IOException {
      output.writeByte(externalizedToken);
   }

   /**
    * Method description
    *
    *
    * @param type
    *
    * @return
    *
    * @throws UnsupportedOperationException
    */
   public static REFEX_TYPE_DDO getFromToken(int type) throws UnsupportedOperationException {
      switch (type) {
      case 1 :
         return MEMBER;

      case 2 :
         return COMP;

      case 3 :
         return COMP_COMP;

      case 4 :
         return COMP_COMP_COMP;

      case 5 :
         return COMP_COMP_STR;

      case 6 :
         return STR;

      case 7 :
         return INT;

      case 8 :
         return COMP_INT;

      case 9 :
         return BOOLEAN;

      case 10 :
         return COMP_STR;

      case 11 :
         return COMP_FLOAT;

      case 12 :
         return COMP_LONG;

      case 13 :
         return LONG;
      }

      throw new UnsupportedOperationException("Can't handle type: " + type);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public Class<? extends RefexVersionBI> getRefexClass() {
      return rxc;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public int getTypeToken() {
      return this.externalizedToken;
   }
}
