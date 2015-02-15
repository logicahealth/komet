/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.ihtsdo.otf.tcc.ddo.concept;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Handle writing of UTF data, considering that data may sometimes be
 * > than the 64K limit.
 *
 * Note that we are using max #of characters for the default read/write UTF
 * as 24000 (~64K/3 since there are a max of 3 bytes per UTF-8 character).
 *
 * @author kec
 * @author Jack Hahn
 */
public class UtfHelper {
   private static int MAX_CHARS = 21000;

   //~--- methods -------------------------------------------------------------

   public static String readUtfV6(DataInput in, int dataVersion) throws IOException {
      if (dataVersion < 6) {
         return in.readUTF();
      } else if (dataVersion == 6) {
         int textlength = in.readInt();

         if (textlength > 64000) {
            int    textBytesLength = in.readInt();
            byte[] textBytes       = new byte[textBytesLength];

            in.readFully(textBytes);

            return new String(textBytes, "UTF-8");
         } else {
            return in.readUTF();
         }
      } else {
         boolean isBig = in.readBoolean();

         if (isBig) {
            int    textBytesLength = in.readInt();
            byte[] textBytes       = new byte[textBytesLength];

            in.readFully(textBytes);

            return new String(textBytes, "UTF-8");
         } else {
            return in.readUTF();
         }
      }
   }

   public static String readUtfV7(DataInput in, int dataVersion) throws IOException {
      if (dataVersion < 7) {
         return in.readUTF();
      } else {
         boolean isBig = in.readBoolean();

         if (isBig) {
            int    textBytesLength = in.readInt();
            byte[] textBytes       = new byte[textBytesLength];

            in.readFully(textBytes);

            return new String(textBytes, "UTF-8");
         } else {
            return in.readUTF();
         }
      }
   }

   public static void writeUtf(DataOutput out, String utfData) throws IOException {
      boolean isBig = utfData.length() > MAX_CHARS;

      out.writeBoolean(isBig);

      if (isBig) {
         byte[] textBytes = utfData.getBytes("UTF-8");

         out.writeInt(textBytes.length);
         out.write(textBytes);
      } else {
         out.writeUTF(utfData);
      }
   }
}
