
/**
 *
 */
package org.ihtsdo.otf.tcc.ddo.concept.component.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.UUID;

public enum IDENTIFIER_PART_TYPES {
   LONG(1), STRING(2), UUID(3);

   private int externalPartTypeToken;

   //~--- constructors --------------------------------------------------------

   IDENTIFIER_PART_TYPES(int externalPartTypeToken) {
      this.externalPartTypeToken = externalPartTypeToken;
   }

   //~--- methods -------------------------------------------------------------

   public static IDENTIFIER_PART_TYPES readType(DataInput input) throws IOException {
      byte typeByte = input.readByte();

      switch (typeByte) {
      case 1 :
         return LONG;

      case 2 :
         return STRING;

      case 3 :
         return UUID;
      }

      throw new UnsupportedOperationException("Can't find byte: " + typeByte);
   }

   public void writeType(DataOutput output) throws IOException {
      output.writeByte(externalPartTypeToken);
   }

   //~--- get methods ---------------------------------------------------------

   public static IDENTIFIER_PART_TYPES getType(Class<?> c) {
      if (Long.class.isAssignableFrom(c)) {
         return LONG;
      } else if (String.class.isAssignableFrom(c)) {
         return STRING;
      } else if (UUID.class.isAssignableFrom(c)) {
         return UUID;
      }

      throw new UnsupportedOperationException();
   }
}
