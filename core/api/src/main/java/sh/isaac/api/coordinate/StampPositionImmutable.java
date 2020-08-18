package sh.isaac.api.coordinate;

import org.eclipse.collections.api.set.ImmutableSet;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class StampPositionImmutable
        implements StampPosition, Comparable<StampPosition>, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<StampPositionImmutable, StampPositionImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                                             ConcurrentReferenceHashMap.ReferenceType.WEAK);



    public static final int marshalVersion = 1;

    /** The time. */
    private final long time;

    private final int pathForPositionNid;

    private transient StampPathImmutable stampPath;

    private StampPositionImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.time    = Long.MIN_VALUE;
        this.pathForPositionNid = Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    /**
     * Instantiates a new stamp position impl.
     *
     * @param time the time
     * @param pathForPositionNid the path nid
     */
    private StampPositionImmutable(long time, int pathForPositionNid) {
        this.time    = time;
        this.pathForPositionNid = pathForPositionNid;
    }

    public static StampPositionImmutable make(long time, int pathNid) {
        return SINGLETONS.computeIfAbsent(new StampPositionImmutable(time, pathNid), stampPositionImmutable -> stampPositionImmutable);
    }

    public static StampPositionImmutable make(long time, ConceptSpecification conceptSpecification) {
        return SINGLETONS.computeIfAbsent(new StampPositionImmutable(time, conceptSpecification.getNid()), stampPositionImmutable -> stampPositionImmutable);
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putLong(this.time);
        out.putNid(this.pathForPositionNid);
    }


    @Unmarshaler
    public static StampPositionImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return new StampPositionImmutable(in.getLong(), in.getNid());
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return this.time;
    }

    @Override
    public StampPositionImmutable toStampPositionImmutable() {
        return this;
    }

    /**
     * Compare to.
     *
     * @param o the o
     * @return the int
     */
    @Override
    public int compareTo(StampPosition o) {
        final int comparison = Long.compare(this.time, o.getTime());

        if (comparison != 0) {
            return comparison;
        }

        return Integer.compare(this.pathForPositionNid, o.getPathForPositionNid());
    }

    //~--- get methods ---------------------------------------------------------


    /**
     * Gets the stamp path.
     *
     * @return the stamp path
     */
    public StampPath getStampPath() {
        throw new UnsupportedOperationException();
        //return new StampPathImpl(this.stampPathConceptSpecification);
    }

    public int getPathForPositionNid() {
        return this.pathForPositionNid;
    }

    /**
     * Gets the stamp path concept nid.
     *
     * @return the stamp path concept nid
     */
    public ConceptSpecification getPathForPositionConcept() {
        return Get.conceptSpecification(this.pathForPositionNid);
    }

    /**
     * Equals.
     *
     * @param obj the obj
     * @return true, if successful
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof StampPosition)) {
            return false;
        }

        final StampPosition other = (StampPosition) obj;

        if (this.time != other.getTime()) {
            return false;
        }

        return this.pathForPositionNid == other.getPathForPositionNid();
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = 83 * hash + (int) (this.time ^ (this.time >>> 32));
        hash = 83 * hash + Integer.hashCode(this.pathForPositionNid);
        return hash;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("StampPosition:{");

        if (this.time == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" on '")
                .append(Get.conceptDescriptionText(this.pathForPositionNid))
                .append("' path}");
        return sb.toString();
    }


    public String toUserString() {
        final StringBuilder sb = new StringBuilder();


        if (this.time == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.time == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" on '")
                .append(Get.conceptDescriptionText(this.pathForPositionNid));
        return sb.toString();
    }

    public ImmutableSet<StampPositionImmutable> getPathOrigins() {
        if (this.stampPath == null) {
            this.stampPath = StampPathImmutable.make(getPathForPositionNid());
        }
        return this.stampPath.getPathOrigins();
    }


}
