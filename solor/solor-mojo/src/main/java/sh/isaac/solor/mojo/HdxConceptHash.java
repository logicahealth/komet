package sh.isaac.solor.mojo;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class HdxConceptHash {
    final String name;
    final int[] parents;
    final String refid;

    public HdxConceptHash(String name, int[] parents, String refid) {
        this.name = name;
        Arrays.sort(parents);
        this.parents = parents.clone();
        this.refid = refid;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Arrays.hashCode(this.parents);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HdxConceptHash other = (HdxConceptHash) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Arrays.equals(this.parents, other.parents);
    }


}
