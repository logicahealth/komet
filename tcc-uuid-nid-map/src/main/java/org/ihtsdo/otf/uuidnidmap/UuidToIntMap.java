package org.ihtsdo.otf.uuidnidmap;

import java.util.UUID;

/**
 * Created by kec on 7/27/14.
 */
public interface UuidToIntMap {

    boolean containsKey(UUID key);

    boolean containsValue(int value);

    int get(UUID key);

    boolean put(UUID key, int value);
}
