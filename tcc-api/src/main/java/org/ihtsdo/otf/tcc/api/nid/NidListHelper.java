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
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.store.Ts;

/**
 *
 * @author AKF
 */
public class NidListHelper {
     public static void writeIntList(ObjectOutputStream out, NidListBI list) throws IOException {
        if (list == null) {
            out.writeInt(Integer.MIN_VALUE);
            return;
        }

        ArrayList<List<UUID>> outList = new ArrayList<>();
        for (int i : list.getListValues()) {
            if (i != 0 && i != Integer.MAX_VALUE) {
                outList.add(Ts.get().getUuidsForNid(i));
            }
        }

        out.writeInt(outList.size());
        for (List<UUID> i : outList) {
            out.writeObject(i);
        }
    }
     
      public static NidListBI readIntListIgnoreMapErrors(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }

    public static NidListBI readIntListStrict(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return readIntList(in, true);
    }
     @SuppressWarnings("unchecked")
    private static NidListBI readIntList(ObjectInputStream in, boolean ignoreMappingErrors) throws IOException,
            ClassNotFoundException {
        int unmappedIds = 0;
        int size = in.readInt();
        if (size == Integer.MIN_VALUE) {
            return new NidList();
        }
        int[] list = new int[size];
        for (int i = 0; i < size; i++) {
            if (ignoreMappingErrors) {
                Object uuidObj = in.readObject();
                if (uuidObj != null) {
                    if (List.class.isAssignableFrom(uuidObj.getClass())) {
                        list[i] = Ts.get().getNidForUuids((List<UUID>) uuidObj);
                    }
                }


            } else {
                list[i] = Ts.get().getNidForUuids((List<UUID>) in.readObject());
            }
        }
        if (unmappedIds > 0) {
            int[] listMinusUnmapped = new int[size - unmappedIds];
            int i = 0;
            for (int j = 0; j < listMinusUnmapped.length; j++) {
                listMinusUnmapped[j] = list[i];
                while (listMinusUnmapped[j] == Integer.MAX_VALUE) {
                    i++;
                    listMinusUnmapped[j] = list[i];
                }
                i++;
            }
            list = listMinusUnmapped;
        }
        NidListBI returnSet = new NidList(list);
        return returnSet;
    }
}
