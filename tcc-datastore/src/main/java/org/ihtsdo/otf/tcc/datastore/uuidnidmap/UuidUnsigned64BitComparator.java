/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.datastore.uuidnidmap;

/**
 *
 * @author kec
 */
public class UuidUnsigned64BitComparator implements UuidComparatorBI {

    /**
     * This algorithm performs unsigned 64 bit comparison of the msb and lsb of 2 uuids. This method is based
     * on the following routine:
     * <code>
     * public static boolean isLessThanUnsigned(long n1, long n2) {
     * return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
     * }
     * </code> see: http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml
     */
    @Override
    public int compare(long msb1, long lsb1, long msb2, long lsb2) {

        if (msb1 == msb2) {
            if (lsb1 == lsb2) {
                return 0;
            }
            if ((lsb1 < lsb2) ^ ((lsb1 < 0) != (lsb2 < 0))) {
                return -1;
            }
            return 1;
        }
        if ((msb1 < msb2) ^ ((msb1 < 0) != (msb2 < 0))) {
            return -1;
        }
        return 1;
    }
}
