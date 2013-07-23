/*
 *  Copyright 2010 kec.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ihtsdo.otf.tcc.api.uuid;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 *
 * @author kec
 */
public class UuidT5Generator {
    public static final String encoding = "8859_1";

    public static UUID get(UUID namespace, String name) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");

        // Generate the digest.
        sha1Algorithm.reset();
        if (namespace != null) {
            sha1Algorithm.update(getRawBytes(namespace));
        }
        sha1Algorithm.update(name.getBytes(encoding));
        byte[] sha1digest = sha1Algorithm.digest();

        sha1digest[6] &= 0x0f; /* clear version */
        sha1digest[6] |= 0x50; /* set to version 5 */
        sha1digest[8] &= 0x3f; /* clear variant */
        sha1digest[8] |= 0x80; /* set to IETF variant */

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (sha1digest[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (sha1digest[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }

    public static UUID get(String name) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return get(null, name);
    }

    
   public static UUID getDescUuid(String text, 
           UUID langPrimUuid, 
           UUID conceptPrimUuid) throws NoSuchAlgorithmException, UnsupportedEncodingException {
      return get(langPrimUuid, text + conceptPrimUuid.toString());
   }

    /**
     * This routine adapted from org.safehaus.uuid.UUID,
     * which is licensed under Apache 2.
     *
     * @param uid
     * @return
     */
    public static byte[] getRawBytes(UUID uid) {
        String id = uid.toString();
        byte[] rawBytes = new byte[16];

        for (int i = 0, j = 0; i < 36; ++j) {
            // Need to bypass hyphens:
            switch (i) {
            case 8:
            case 13:
            case 18:
            case 23:
                ++i;
            }
            char c = id.charAt(i);

            if (c >= '0' && c <= '9') {
                rawBytes[j] = (byte) ((c - '0') << 4);
            } else if (c >= 'a' && c <= 'f') {
                rawBytes[j] = (byte) ((c - 'a' + 10) << 4);
            }

            c = id.charAt(++i);

            if (c >= '0' && c <= '9') {
                rawBytes[j] |= (byte) (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                rawBytes[j] |= (byte) (c - 'a' + 10);
            }
            ++i;
        }
        return rawBytes;
    }
    
    
    /**
     * Generates a uuid from the given <code>byteArray</code>.
     *
     * @param byteArray the bytes to use for generating the uuid
     * @return the generated uuid
     */
    public static UUID getUuidFromRawBytes(byte[] byteArray) {
        if (byteArray.length != 16) {
            throw new NumberFormatException("UUID must be 16 bytes");
        }

        ByteBuffer raw = ByteBuffer.wrap(byteArray);

        return new UUID(raw.getLong(raw.position()), raw.getLong(raw.position() + 8));
    }

}
