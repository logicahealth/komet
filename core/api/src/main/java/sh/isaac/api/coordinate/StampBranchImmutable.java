package sh.isaac.api.coordinate;

import java.util.Objects;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public class StampBranchImmutable implements StampBranch, ImmutableCoordinate, StaticIsaacCache {

    private static final ConcurrentReferenceHashMap<Integer, StampBranchImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.STRONG,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    private final int branchConceptNid;

    private final long branchOriginTime;

    private StampBranchImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.branchConceptNid = Integer.MAX_VALUE;
        this.branchOriginTime = Long.MIN_VALUE;
    }
    
    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    private StampBranchImmutable(int branchConceptNid, long branchOriginTime) {
        this.branchConceptNid = branchConceptNid;
        this.branchOriginTime = branchOriginTime;
    }

    private StampBranchImmutable(ByteArrayDataBuffer in) {
        this.branchConceptNid = in.getNid();
        this.branchOriginTime = in.getLong();
    }

    public static StampBranchImmutable make(int pathConceptNid, long branchOriginTime) {
        return SINGLETONS.computeIfAbsent(pathConceptNid,
                pathNid -> new StampBranchImmutable(pathConceptNid, branchOriginTime));
    }

    @Override
    public long getBranchOriginTime() {
        return branchOriginTime;
    }

    @Unmarshaler
    public static StampBranchImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                StampBranchImmutable stampBranch = new StampBranchImmutable(in);
                return SINGLETONS.computeIfAbsent(stampBranch.getPathOfBranchNid(),
                        branchConceptNid -> stampBranch);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.branchConceptNid);
        out.putLong(this.branchOriginTime);
    }

    @Override
    public StampBranchImmutable toStampBranchImmutable() {
        return this;
    }

    public int getPathOfBranchNid() {
        return this.branchConceptNid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StampBranch)) return false;
        StampBranch that = (StampBranch) o;
        return getPathOfBranchNid() == that.getPathOfBranchNid() &&
                getBranchOriginTime() == that.getBranchOriginTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPathOfBranchNid(), getBranchOriginTime());
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("StampBranchImmutable:{ At date/time ");

        if (this.branchOriginTime == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.branchOriginTime == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" start branch for '")
                .append(Get.conceptDescriptionText(this.branchConceptNid))
                .append("' path}");
        return sb.toString();
    }


    public String toUserString() {
        final StringBuilder sb = new StringBuilder("At date/time ");

        if (this.branchOriginTime == Long.MAX_VALUE) {
            sb.append("latest");
        } else if (this.branchOriginTime == Long.MIN_VALUE) {
            sb.append("CANCELED");
        } else {
            sb.append(getTimeAsInstant());
        }

        sb.append(" start branch for '")
                .append(Get.conceptDescriptionText(this.branchConceptNid))
                .append("' path}");
        return sb.toString();
    }

}
