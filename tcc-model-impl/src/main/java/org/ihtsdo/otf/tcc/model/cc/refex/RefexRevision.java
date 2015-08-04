package org.ihtsdo.otf.tcc.model.cc.refex;

//~--- non-JDK imports --------------------------------------------------------

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.Revision;

import java.beans.PropertyVetoException;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

public abstract class RefexRevision<V extends RefexRevision<V, C>, C extends RefexMember<V, C>>
        extends Revision<V, C> implements RefexAnalogBI<V>, SememeVersion {

    public RefexRevision() {
        super();
    }

    public RefexRevision(int statusAtPositionNid, C primordialComponent) {
        super(statusAtPositionNid, primordialComponent);
    }

    public RefexRevision(TtkRevision eVersion, C member)  throws IOException{
        super(eVersion.getStatus(), eVersion.getTime(), PersistentStore.get().getNidForUuids(eVersion.getAuthorUuid()),
                 PersistentStore.get().getNidForUuids(eVersion.getModuleUuid()), PersistentStore.get().getNidForUuids(eVersion.getPathUuid()),  member);
    }

    public RefexRevision(DataInputStream input, C primordialComponent) throws IOException {
        super(input, primordialComponent);
    }

    public RefexRevision(Status status, long time, int authorNid, int moduleNid, int pathNid, C primordialComponent) {
        super(status, time, authorNid, moduleNid, pathNid, primordialComponent);
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(primordialComponent.referencedComponentNid);
        allNids.add(primordialComponent.assemblageNid);
        addRefsetTypeNids(allNids);
    }

    protected abstract void addRefsetTypeNids(Set<Integer> allNids);

    protected abstract void addSpecProperties(RefexCAB rcs);
    @Override
    public RefexType getRefexType() {
        return getTkRefsetType();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RefexRevision.class.isAssignableFrom(obj.getClass())) {
            RefexRevision<?, ?> another = (RefexRevision<?, ?>) obj;

            if (this.stamp == another.stamp) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        return primordialComponent.refexFieldsEqual(another);
    }

    public abstract V makeAnalog();

    public abstract boolean readyToWriteRefsetRevision();

    @Override
    public final boolean readyToWriteRevision() {
        assert readyToWriteRefsetRevision() : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return primordialComponent.assemblageNid;
    }

    @Override
    @Deprecated
    public int getRefexExtensionNid() {
        return getAssemblageNid();
    }

    @Override
    public RefexMember getPrimordialVersion() {
        return primordialComponent;
    }

    @Override
    public int getReferencedComponentNid() {
        return primordialComponent.getReferencedComponentNid();
    }

    @Override
    public RefexCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException,
            InvalidCAB, ContradictionException {
        RefexCAB rcs = new RefexCAB(
                getTkRefsetType(),
                PersistentStore.get().getUuidPrimordialForNid(getReferencedComponentNid()),
                getAssemblageNid(),
                getVersion(vc), 
                Optional.of(vc), 
                idDirective, 
                refexDirective);

        addSpecProperties(rcs);

        return rcs;
    }

    protected abstract RefexType getTkRefsetType();

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        primordialComponent.setAssemblageNid(collectionNid);
    }

    @Override
    @Deprecated
    public void setRefexExtensionNid(int collectionNid) throws PropertyVetoException, IOException {
        setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
        primordialComponent.setReferencedComponentNid(componentNid);
    }


    @Override
    public List<? extends RefexVersionBI<V>> getVersionList() {
        return getVersions();
    }

    @Override
    public int getSememeSequence() {
        return primordialComponent.getSememeSequence();
    }

    @Override
    public int getAssemblageSequence() {
       return primordialComponent.getAssemblageSequence();
    }

    @Override
    public Optional<LatestVersion<RefexVersionBI<V>>> getLatestVersion(Class<RefexVersionBI<V>> type, StampCoordinate<?> coordinate) {
        return primordialComponent.getLatestVersion(type, coordinate);
    }
    
    @Override
    public SememeChronology getChronology() {
       throw new UnsupportedOperationException("For OCHRE implementation only");
    }
    
}
