package sh.isaac.api.coordinate;


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
import java.util.Objects;

@Service
@RunLevel(value = LookupService.SL_L2)
// Singleton from the perspective of HK2 managed instances, there will be more than one
// StampFilterImmutable created in normal use.
public final class LogicCoordinateImmutable implements LogicCoordinate, ImmutableCoordinate {

    private static final ConcurrentReferenceHashMap<LogicCoordinateImmutable, LogicCoordinateImmutable> SINGLETONS =
            new ConcurrentReferenceHashMap<>(ConcurrentReferenceHashMap.ReferenceType.WEAK,
                    ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private static final int marshalVersion = 1;

    private final int classifierNid;
    private final int descriptionLogicProfileNid;
    private final int inferredAssemblageNid;
    private final int statedAssemblageNid;
    private final int conceptAssemblageNid;
    private final int digraphIdentityNid;

    private LogicCoordinateImmutable() {
        // No arg constructor for HK2 managed instance
        // This instance just enables reset functionality...
        this.classifierNid = Integer.MAX_VALUE;
        this.descriptionLogicProfileNid = Integer.MAX_VALUE;
        this.inferredAssemblageNid = Integer.MAX_VALUE;
        this.statedAssemblageNid = Integer.MAX_VALUE;
        this.conceptAssemblageNid = Integer.MAX_VALUE;
        this.digraphIdentityNid = Integer.MAX_VALUE;
    }
    /**
     * {@inheritDoc}
     */
    @PreDestroy
    public void reset() {
        SINGLETONS.clear();
    }

    private LogicCoordinateImmutable(int classifierNid,
                                    int descriptionLogicProfileNid,
                                    int inferredAssemblageNid,
                                    int statedAssemblageNid,
                                    int conceptAssemblageNid,
                                    int digraphIdentityNid) {
        this.classifierNid = classifierNid;
        this.descriptionLogicProfileNid = descriptionLogicProfileNid;
        this.inferredAssemblageNid = inferredAssemblageNid;
        this.statedAssemblageNid = statedAssemblageNid;
        this.conceptAssemblageNid = conceptAssemblageNid;
        this.digraphIdentityNid = digraphIdentityNid;
    }

    private LogicCoordinateImmutable(ByteArrayDataBuffer in) {
        this.classifierNid = in.getNid();
        this.descriptionLogicProfileNid = in.getNid();
        this.inferredAssemblageNid = in.getNid();
        this.statedAssemblageNid = in.getNid();
        this.conceptAssemblageNid = in.getNid();
        this.digraphIdentityNid = in.getNid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicCoordinate)) return false;
        LogicCoordinate that = (LogicCoordinate) o;
        return getClassifierNid() == that.getClassifierNid() &&
                getDescriptionLogicProfileNid() == that.getDescriptionLogicProfileNid() &&
                getInferredAssemblageNid() == that.getInferredAssemblageNid() &&
                getStatedAssemblageNid() == that.getStatedAssemblageNid() &&
                getConceptAssemblageNid() == that.getConceptAssemblageNid() &&
                getDigraphIdentityNid() == that.getDigraphIdentityNid();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassifierNid(), getDescriptionLogicProfileNid(), getInferredAssemblageNid(), getStatedAssemblageNid(), getConceptAssemblageNid(), getDigraphIdentityNid());
    }

    public static LogicCoordinateImmutable make(int classifierNid,
                                                int descriptionLogicProfileNid,
                                                int inferredAssemblageNid,
                                                int statedAssemblageNid,
                                                int conceptAssemblageNid,
                                                int digraphIdentityNid)  {
        return SINGLETONS.computeIfAbsent(new LogicCoordinateImmutable(classifierNid, descriptionLogicProfileNid,
                        inferredAssemblageNid, statedAssemblageNid, conceptAssemblageNid, digraphIdentityNid),
                logicCoordinateImmutable -> logicCoordinateImmutable);
    }

    public static LogicCoordinateImmutable make(ConceptSpecification classifier,
                                                ConceptSpecification descriptionLogicProfile,
                                                ConceptSpecification inferredAssemblage,
                                                ConceptSpecification statedAssemblage,
                                                ConceptSpecification conceptAssemblage,
                                                ConceptSpecification digraphIdentity)  {
        return SINGLETONS.computeIfAbsent(new LogicCoordinateImmutable(classifier.getNid(), descriptionLogicProfile.getNid(),
                        inferredAssemblage.getNid(), statedAssemblage.getNid(), conceptAssemblage.getNid(), digraphIdentity.getNid()),
                logicCoordinateImmutable -> logicCoordinateImmutable);
    }

    @Unmarshaler
    public static LogicCoordinateImmutable make(ByteArrayDataBuffer in) {
        int objectMarshalVersion = in.getInt();
        switch (objectMarshalVersion) {
            case marshalVersion:
                return SINGLETONS.computeIfAbsent(new LogicCoordinateImmutable(in),
                        logicCoordinateImmutable -> logicCoordinateImmutable);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + objectMarshalVersion);
        }
    }

    @Override
    @Marshaler
    public void marshal(ByteArrayDataBuffer out) {
        out.putInt(marshalVersion);
        out.putNid(this.classifierNid);
        out.putNid(this.descriptionLogicProfileNid);
        out.putNid(this.inferredAssemblageNid);
        out.putNid(this.statedAssemblageNid);
        out.putNid(this.conceptAssemblageNid);
        out.putNid(this.digraphIdentityNid);
    }

    @Override
    public int getClassifierNid() {
        return this.classifierNid;
    }

    @Override
    public int getDescriptionLogicProfileNid() {
        return this.descriptionLogicProfileNid;
    }

    @Override
    public int getInferredAssemblageNid() {
        return this.inferredAssemblageNid;
    }

    @Override
    public int getStatedAssemblageNid() {
        return this.statedAssemblageNid;
    }

    @Override
    public int getConceptAssemblageNid() {
        return this.conceptAssemblageNid;
    }

    @Override
    public String toString() {
        return "LogicCoordinateImpl{" + Get.conceptDescriptionText(this.statedAssemblageNid) + "<" +
                this.statedAssemblageNid + ">,\n" + Get.conceptDescriptionText(this.inferredAssemblageNid) +
                "<" + this.inferredAssemblageNid + ">, \n" +
                Get.conceptDescriptionText(this.descriptionLogicProfileNid) + "<" +
                this.descriptionLogicProfileNid + ">, \n" + Get.conceptDescriptionText(this.classifierNid) +
                "<" + this.classifierNid + ">}";
    }
    @Override
    public LogicCoordinateImmutable toLogicCoordinateImmutable() {
        return this;
    }


}
