package sh.isaac.api.coordinate;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Objects;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public class EditCoordinateImmutable implements EditCoordinate, ImmutableCoordinate {
    private static final ConcurrentReferenceHashMap<EditCoordinateImmutable, EditCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;


    private EditCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.authorNid = Integer.MAX_VALUE;
        this.moduleNid = Integer.MAX_VALUE;
        this.pathNid = Integer.MAX_VALUE;
    }
    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private EditCoordinateImmutable(int authorNid, int moduleNid, int pathNid) {
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
    }
    private static final int marshalVersion = 1;

    public static EditCoordinateImmutable make(int authorNid, int moduleNid, int pathNid) {
        return SINGLETONS.computeIfAbsent(new EditCoordinateImmutable(authorNid, moduleNid, pathNid),
                editCoordinateImmutable -> editCoordinateImmutable);
    }

    public static EditCoordinateImmutable make(ConceptSpecification author, ConceptSpecification module, ConceptSpecification path) {
        return make(author.getNid(), module.getNid(), path.getNid());
    }

    @Unmarshaler
    public static EditCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new EditCoordinateImmutable(in.getNid(), in.getNid(), in.getNid()),
                        editCoordinateImmutable -> editCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.authorNid);
        out.putNid(this.moduleNid);
        out.putNid(this.pathNid);
    }

    @Override
    public int getAuthorNid() {
        return this.authorNid;
    }

    @Override
    public int getModuleNid() {
        return this.moduleNid;
    }

    @Override
    public int getPathNid() {
        return this.pathNid;
    }

    @Override
    public EditCoordinateImmutable toEditCoordinateImmutable() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditCoordinateImmutable)) return false;
        EditCoordinateImmutable that = (EditCoordinateImmutable) o;
        return getAuthorNid() == that.getAuthorNid() &&
                getModuleNid() == that.getModuleNid() &&
                getPathNid() == that.getPathNid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuthorNid(), getModuleNid(), getPathNid());
    }
}
