/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.hash;

/**
 *
 * @author kec
 */
public class Hashcode {

    public static int compute(int... parts) {
        int hash = 0;
        int len = parts.length;

        for (int i = 0; i < len; i++) {
            hash <<= 1;

            if (hash < 0) {
                hash |= 1;
            }

            hash ^= parts[i];
        }

        return hash;
    }

    public static int computeLong(long... parts) {
        int[] intParts = new int[parts.length * 2];

        for (int i = 0; i < parts.length; i++) {
            intParts[i * 2] = (int) parts[i];
            intParts[i * 2 + 1] = (int) (parts[i] >>> 32);
        }

        return compute(intParts);
    }

    public static short compute(short... parts) {
        short hash = 0;
        for (int i = 0; i < parts.length; i++) {
            hash <<= 1;
            if (hash < 0) {
                hash |= 1;
            }
            hash ^= parts[i];
        }
        return hash;
    }

    public static short intHashToShortHash(int hash) {
        short[] parts = new short[2];
        parts[0] = (short) hash; // low order short
        parts[1] = (short) (hash >> 16); // high order short
        return compute(parts);
    }
}
