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
package org.ihtsdo.otf.tcc.api.nid;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author AKF
 */
public class NidSetHelper {
    public static void writeIntSet(ObjectOutputStream out, NidSetBI set) throws IOException {
        if (set == null) {
            out.writeInt(Integer.MIN_VALUE);
            return;
        }

        ArrayList<List<UUID>> outList = new ArrayList<>();
        for (int i : set.getSetValues()) {
            List<UUID> uuids = Ts.get().getUuidsForNid(i);
            if (uuids != null && uuids.size() > 0) {
                outList.add(uuids);
            }
        }

        out.writeInt(outList.size());
        for (List<UUID> i : outList) {
            out.writeObject(i);
        }
    }
    
        public static NidSetBI readIntSetIgnoreMapErrors(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntSet(in, true);
    }

    public static NidSetBI readIntSetStrict(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntSet(in, false);
    }

    @SuppressWarnings("unchecked")
    private static NidSetBI readIntSet(ObjectInputStream in, boolean ignore) throws IOException, ClassNotFoundException {
        int unmappedIds = 0;
        int size = in.readInt();
        if (size == Integer.MIN_VALUE) {
            return new NidSet();
        }
        int[] set = new int[size];
        for (int i = 0; i < size; i++) {
            try {
                if (ignore) {
                        set[i] = Ts.get().getNidForUuids((List<UUID>) in.readObject());

                } else {
                    set[i] = Ts.get().getNidForUuids((List<UUID>) in.readObject());
                }
            } catch (IOException | ClassNotFoundException e) {
                IOException newEx = new IOException();
                newEx.initCause(e);
                throw newEx;
            }
        }
        if (unmappedIds > 0) {
            int[] setMinusUnmapped = new int[size - unmappedIds];
            int i = 0;
            for (int j = 0; j < setMinusUnmapped.length; j++) {
                setMinusUnmapped[j] = set[i];
                while (setMinusUnmapped[j] == Integer.MAX_VALUE) {
                    i++;
                    setMinusUnmapped[j] = set[i];
                }
                i++;
            }
            set = setMinusUnmapped;
        }
        Arrays.sort(set);
        NidSetBI returnSet = new NidSet(set);
        return returnSet;
    }
    
}
