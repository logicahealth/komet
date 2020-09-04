package sh.isaac.api.coordinate;

import java.util.Objects;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.StaticIsaacCache;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.collections.jsr166y.ConcurrentReferenceHashMap;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.marshal.Marshaler;
import sh.isaac.api.marshal.Unmarshaler;


//This class is not treated as a service, however, it needs the annotation, so that the reset() gets fired at appropriate times.
@Service
public class EditCoordinateImmutable implements EditCoordinate, ImmutableCoordinate, StaticIsaacCache{
    private static final int marshalVersion = 2;

    private static final ConcurrentReferenceHashMap<EditCoordinateImmutable, EditCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private final int authorNid;
    private final int defaultModuleNid;
    private final int promotionPathNid;
    private final int destinationModuleNid;

    private EditCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.authorNid = Integer.MAX_VALUE;
        this.defaultModuleNid = Integer.MAX_VALUE;
        this.promotionPathNid = Integer.MAX_VALUE;
        this.destinationModuleNid = Integer.MAX_VALUE;
    }

    @Override
    public void reset() {
        SINGLETONS.clear();
    }

    private EditCoordinateImmutable(int authorNid, int defaultModuleNid, int promotionPathNid, int destinationModuleNid) {
        this.authorNid = authorNid;
        this.defaultModuleNid = defaultModuleNid;
        this.promotionPathNid = promotionPathNid;
        this.destinationModuleNid = destinationModuleNid;
    }

    public static EditCoordinateImmutable make(int authorNid, int defaultModuleNid, int promotionPathNid, int destinationModuleNid) {
        return SINGLETONS.computeIfAbsent(new EditCoordinateImmutable(authorNid, defaultModuleNid, promotionPathNid, destinationModuleNid),
                editCoordinateImmutable -> editCoordinateImmutable);
    }

    public static EditCoordinateImmutable make(ConceptSpecification author, ConceptSpecification defaultModule, ConceptSpecification promotionPath,
                                               ConceptSpecification destinationModule) {
        return make(author.getNid(), defaultModule.getNid(), promotionPath.getNid(), destinationModule.getNid());
    }

    @Unmarshaler
    public static EditCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new EditCoordinateImmutable(in.getNid(), in.getNid(), in.getNid(), in.getNid()),
                        editCoordinateImmutable -> editCoordinateImmutable);
            case 1:
                return SINGLETONS.computeIfAbsent(new EditCoordinateImmutable(in.getNid(), in.getNid(), in.getNid(), TermAux.UNSPECIFIED_MODULE.getNid()),
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
        out.putNid(this.defaultModuleNid);
        out.putNid(this.promotionPathNid);
        out.putNid(this.destinationModuleNid);
    }

    @Override
    public int getAuthorNidForChanges() {
        return this.authorNid;
    }

    @Override
    public int getDefaultModuleNid() {
        return this.defaultModuleNid;
    }

    @Override
    public int getPromotionPathNid() {
        return this.promotionPathNid;
    }

    @Override
    public int getDestinationModuleNid() {
        return this.destinationModuleNid;
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
        return getAuthorNidForChanges() == that.getAuthorNidForChanges() &&
                getDefaultModuleNid() == that.getDefaultModuleNid() &&
                getPromotionPathNid() == that.getPromotionPathNid() &&
                getDestinationModuleNid() == that.getDestinationModuleNid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuthorNidForChanges(), getDefaultModuleNid(), getPromotionPathNid(), getDestinationModuleNid());
    }

    @Override
    public String toString() {
        return "EditCoordinateImmutable{" +
                toUserString() +
                '}';
    }
}
