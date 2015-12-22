/*
 *
 *  Copyright 2010, 2015. The US Veterans Health Administration
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

package gov.vha.isaac.ochre.util;

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
    public static final UUID PATH_ID_FROM_FS_DESC = UUID.fromString("5a2e7786-3e41-11dc-8314-0800200c9a66");
    public static final UUID REL_GROUP_NAMESPACE = UUID.fromString("8972fef0-ad53-11df-94e2-0800200c9a66");
    public static final UUID USER_FULLNAME_NAMESPACE = UUID.fromString("cad85220-1ed4-11e1-8bc2-0800200c9a66");
    public static final UUID TAXONOMY_COORDINATE_NAMESPACE = UUID.fromString("c58dcdb6-185b-11e5-b60b-1697f925ec7b");
   public static final UUID REL_ADAPTOR_NAMESPACE = UUID.fromString("9cb2bf66-1863-11e5-b60b-1697f925ec7");
    
    public static final UUID AUTHOR_TIME_ID = UUID.fromString("c6915290-30fc-11e1-b86c-0800200c9a66");

    public static UUID get(UUID namespace, String name) {
        try {
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
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
           throw new RuntimeException(ex);
        }
    }

    public static UUID get(String name) {
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
     * Generates a uuid from the given {@code byteArray}.
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
