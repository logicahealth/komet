package sh.isaac.api.collections;

import org.apache.mahout.math.function.IntObjectProcedure;
import org.apache.mahout.math.map.AbstractIntObjectMap;
import org.apache.mahout.math.map.OpenIntObjectHashMap;

public class IntObjectHashMap<T> extends OpenIntObjectHashMap<T> {
    public IntObjectHashMap() {
    }

    public IntObjectHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IntObjectHashMap(int initialCapacity, double minLoadFactor, double maxLoadFactor) {
        super(initialCapacity, minLoadFactor, maxLoadFactor);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof AbstractIntObjectMap)) {
            return false;
        }
        final AbstractIntObjectMap other = (AbstractIntObjectMap) obj;
        if (other.size() != size()) {
            return false;
        }

        return
                forEachPair(
                        new IntObjectProcedure() {
                            @Override
                            public boolean apply(int key, Object value) {
                                return other.containsKey(key) && other.get(key).equals(value);
                            }
                        }
                )
                        &&
                        other.forEachPair(
                                new IntObjectProcedure() {
                                    @Override
                                    public boolean apply(int key, Object value) {
                                        return containsKey(key) && get(key).equals(value);
                                    }
                                }
                        );
    }

}
