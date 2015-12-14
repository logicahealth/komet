package gov.vha.isaac.ochre.sememe.provider;

/**
 * Created by kec on 12/18/14.
 */
public class ReferencedNidAssemblageSequenceSememeSequenceKey implements Comparable<ReferencedNidAssemblageSequenceSememeSequenceKey> {
    int referencedNid;
    int assemblageSequence;
    int sememeSequence;

    public ReferencedNidAssemblageSequenceSememeSequenceKey(int referencedNid, int assemblageSequence, int sememeSequence) {
        this.referencedNid = referencedNid;
        this.assemblageSequence = assemblageSequence;
        this.sememeSequence = sememeSequence;
    }

    @Override
    public int compareTo(ReferencedNidAssemblageSequenceSememeSequenceKey o) {
        if (referencedNid != o.referencedNid) {
            if (referencedNid < o.referencedNid) {
                return -1;
            }
            return 1;
        }
        if (assemblageSequence != o.assemblageSequence) {
            if (assemblageSequence < o.assemblageSequence) {
                return -1;
            }
            return 1;
        }
        if (sememeSequence == o.sememeSequence) {
            return 0;
        }
        if (sememeSequence < o.sememeSequence) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferencedNidAssemblageSequenceSememeSequenceKey sememeKey = (ReferencedNidAssemblageSequenceSememeSequenceKey) o;

        if (referencedNid != sememeKey.referencedNid) return false;
        if (assemblageSequence != sememeKey.assemblageSequence) return false;
        return sememeSequence == sememeKey.sememeSequence;
    }

    @Override
    public int hashCode() {
        int result = referencedNid;
        result = 31 * result + assemblageSequence;
        result = 31 * result + sememeSequence;
        return result;
    }

    public int getReferencedNid() {
        return referencedNid;
    }

    public int getAssemblageSequence() {
        return assemblageSequence;
    }


    public int getSememeSequence() {
        return sememeSequence;
    }

    @Override
    public String toString() {
        return "Key{" +
                "referencedNid=" + referencedNid +
                ", assemblageSequence=" + assemblageSequence +
                ", sememeSequence=" + sememeSequence +
                '}';
    }
}
