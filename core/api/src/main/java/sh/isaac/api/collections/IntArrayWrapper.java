package sh.isaac.api.collections;

import java.util.Arrays;

/**
 * Class to wrap an int array, so it behaves properly (equals, hashcode...)
 * in sets.
 */
public class IntArrayWrapper {
    private final int[] wrappedSet;

    public IntArrayWrapper(int[] wrappedSet) {
        this.wrappedSet = wrappedSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntArrayWrapper that = (IntArrayWrapper) o;
        return Arrays.equals(wrappedSet, that.wrappedSet);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(wrappedSet);
    }

    public int[] getWrappedSet() {
        return wrappedSet;
    }
}
