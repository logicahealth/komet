package org.ihtsdo.otf.tcc.chronicle.cc.refex;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


//import org.dwfa.ace.api.I_IntSet;

import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPair;
import org.ihtsdo.otf.tcc.chronicle.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.api.refex.RefexType;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

public abstract class RefexMember<R extends RefexRevision<R, C>, C extends RefexMember<R, C>>
        extends ConceptComponent<R, C> implements RefexChronicleBI<R>, RefexAnalogBI<R> {

    public int referencedComponentNid;
    public int refexExtensionNid;
    protected List<? extends Version> versions;

    //~--- constructors --------------------------------------------------------
    public RefexMember() {
        super();
        referencedComponentNid = Integer.MAX_VALUE;
        refexExtensionNid = Integer.MAX_VALUE;
    }

    public RefexMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public RefexMember(TtkRefexAbstractMemberChronicle<?> refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        refexExtensionNid = P.s.getNidForUuids(refsetMember.refexExtensionUuid);
        referencedComponentNid = P.s.getNidForUuids(refsetMember.getComponentUuid());
        primordialStamp = P.s.getStamp(refsetMember);
        assert primordialStamp != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        assert refexExtensionNid != Integer.MAX_VALUE;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(referencedComponentNid);
        allNids.add(refexExtensionNid);
        addRefsetTypeNids(allNids);
    }

    protected abstract void addRefsetTypeNids(Set<Integer> allNids);

    protected abstract void addSpecProperties(RefexCAB rcs);

    @Override
    public void clearVersions() {
        versions = null;
        clearAnnotationVersions();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RefexMember.class.isAssignableFrom(obj.getClass())) {
            RefexMember<?, ?> another = (RefexMember<?, ?>) obj;

            return this.referencedComponentNid == another.referencedComponentNid;
        }

        return false;
    }

    @Deprecated
    public abstract int getTypeNid();
    
    @Override
    public boolean fieldsEqual(ConceptComponent<R, C> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            RefexMember<R, C> another = (RefexMember<R, C>) obj;

            if (this.getTypeNid() != another.getTypeNid()) {
                return false;
            }

            if (refexFieldsEqual(obj)) {
                return conceptComponentFieldsEqual(another);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{referencedComponentNid});
    }

    public abstract R makeAnalog();

    protected abstract boolean refexFieldsEqual(ConceptComponent<R, C> obj);

    @SuppressWarnings("unchecked")
    public RefexMember<R, C> merge(RefexMember<R, C> component, Set<ConceptChronicleBI> indexedAnnotationConcepts) throws IOException {
        return (RefexMember<R, C>) super.merge((C) component, indexedAnnotationConcepts);
    }

    @Override
    public void readFromBdb(TupleInput input) {
        refexExtensionNid = input.readInt();
        referencedComponentNid = input.readInt();
        assert refexExtensionNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        readMemberFields(input);

        int additionalVersionCount = input.readShort();

        if (additionalVersionCount > 0) {
            if (revisions == null) {
                revisions = new RevisionSet<>(primordialStamp);
            }

            for (int i = 0; i < additionalVersionCount; i++) {
                R r = readMemberRevision(input);

                if ((r.stamp != -1) && (r.getTime() != Long.MIN_VALUE)) {
                    revisions.add(r);
                }
            }
        }
    }

    protected abstract void readMemberFields(TupleInput input);

    protected abstract R readMemberRevision(TupleInput input);

    @Override
    public final boolean readyToWriteComponent() {
        assert referencedComponentNid != Integer.MAX_VALUE : assertionString();
        assert referencedComponentNid != 0 : assertionString();
        assert refexExtensionNid != Integer.MAX_VALUE : assertionString();
        assert refexExtensionNid != 0 : assertionString();
        assert readyToWriteRefsetMember() : assertionString();

        return true;
    }

    public abstract boolean readyToWriteRefsetMember();

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(" refset:");
        addNidToBuffer(buf, refexExtensionNid);
        buf.append(" type:");
        buf.append(getTkRefsetType());
        buf.append(" rcNid:");
        addNidToBuffer(buf, referencedComponentNid);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        return toString();
    }

    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        ComponentVersionBI c1Component = snapshot.getConceptVersion(refexExtensionNid);

        return "refex: " + c1Component.toUserString(snapshot);
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(RefexMember<?, ?> another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        if (this.referencedComponentNid != another.referencedComponentNid) {
            buf.append(
                    "\tRefsetMember.referencedComponentNid not equal: \n"
                    + "\t\tthis.referencedComponentNid = ").append(this.referencedComponentNid).append(
                    "\n" + "\t\tanother.referencedComponentNid = ").append(
                    another.referencedComponentNid).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    protected abstract void writeMember(TupleOutput output);

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<RefexRevision<R, C>> additionalVersionsToWrite = new ArrayList<>();

        if (revisions != null) {
            for (RefexRevision<R, C> p : revisions) {
                if ((p.getStamp() > maxReadOnlyStatusAtPositionNid)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    additionalVersionsToWrite.add(p);
                }
            }
        }

        assert refexExtensionNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        output.writeInt(refexExtensionNid);
        output.writeInt(referencedComponentNid);
        writeMember(output);
        output.writeShort(additionalVersionsToWrite.size());

        NidPairForRefex npr = NidPair.getRefexNidMemberNidPair(refexExtensionNid, nid);
        try {
            P.s.addXrefPair(referencedComponentNid, npr);
        } catch (IOException ex) {
           throw new RuntimeException(ex);
        }

        for (RefexRevision<R, C> p : additionalVersionsToWrite) {
            p.writeRevisionBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getRefexExtensionNid() {
        return refexExtensionNid;
    }

    @Override
    public RefexMember getPrimordialVersion() {
        return RefexMember.this;
    }

    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }

    @Override
    public RefexCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException,
            InvalidCAB, ContradictionException {
        RefexCAB rcs = new RefexCAB(getTkRefsetType(), 
                P.s.getUuidPrimordialForNid(getReferencedComponentNid()),
                getRefexExtensionNid(),
                getVersion(vc), vc, idDirective, refexDirective);

        addSpecProperties(rcs);

        return rcs;
    }

  
    protected abstract RefexType getTkRefsetType();

    @Override
    public RefexMember<R, C>.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<RefexMember<R, C>.Version> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return null;
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        if (!vForC.isEmpty()) {
            return vForC.get(0);
        }
        return null;
    }

    protected abstract VersionComputer<RefexMember<R, C>.Version> getVersionComputer();

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Version> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<Version> list = new ArrayList<>(count);

            list.add(new Version(this));

            if (revisions != null) {
                for (RefexRevision rv : revisions) {
                    list.add(new Version(rv));
                }
            }

            versions = list;
        }

        return (List<Version>) versions;
    }

    @Override
    public List<RefexMember<R, C>.Version> getVersions(ViewCoordinate c) {
        List<RefexMember<R, C>.Version> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getPositionSet(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<RefexMember<R, C>.Version> getVersions(ViewCoordinate c, long time) {
        List<RefexMember<R, C>.Version> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getPositionSet(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager(), time);

        return returnTuples;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setRefexExtensionNid(int collectionNid) throws PropertyVetoException, IOException {
        if ((this.refexExtensionNid == Integer.MAX_VALUE) || (this.refexExtensionNid == collectionNid)
                || (getTime() == Long.MAX_VALUE)) {
            if (this.refexExtensionNid != collectionNid) {
                if ((this.refexExtensionNid != 0) && (this.nid != 0)) {
                    NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.refexExtensionNid, this.nid);

                    P.s.forgetXrefPair(this.referencedComponentNid, oldNpr);
                }

                // new xref is added on the dbWrite.
                this.refexExtensionNid = collectionNid;
                modified();
            }
        } else {
            throw new PropertyVetoException("Cannot change refset unless member is uncommitted...", null);
        }
    }

    @Override
    public void setReferencedComponentNid(int referencedComponentNid) throws IOException {
        assert referencedComponentNid != Integer.MAX_VALUE: "referencedComponentNid is Integer.MAX_VALUE";
        assert refexExtensionNid != Integer.MAX_VALUE: "refexExtensionNid is Integer.MAX_VALUE";
        assert nid != Integer.MAX_VALUE: "nid is Integer.MAX_VALUE";
        if (this.referencedComponentNid != referencedComponentNid) {
            if ((this.referencedComponentNid != Integer.MAX_VALUE) && (this.refexExtensionNid != 0) && (this.nid != 0)) {
                NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.refexExtensionNid, this.nid);

                P.s.forgetXrefPair(this.referencedComponentNid, oldNpr);
            }

            // new xref is added on the dbWrite.
            this.referencedComponentNid = referencedComponentNid;
            modified();
        }
    }

    @Override
    public RefexType getRefexType() {
        return getTkRefsetType();
    }

 
    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<R, C>.Version
            implements RefexAnalogBI<R> {

        public Version(RefexAnalogBI<R> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public RefexType getRefexType() {
            return RefexMember.this.getRefexType();
        }


        public R makeAnalog() {
            if (RefexMember.this != cv) {
            }

            return (R) RefexMember.this.makeAnalog();
        }

 
        @Override
        public R makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
            return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent.Version another) {
            RefexMember.Version anotherVersion = (RefexMember.Version) another;
            if (this.getTypeNid() != anotherVersion.getTypeNid()) {
                return false;
            }

            if (this.getRefexExtensionNid() != anotherVersion.getRefexExtensionNid()) {
                return false;
            }

            if (this.getReferencedComponentNid() != anotherVersion.getReferencedComponentNid()) {
                return false;
            }

            if (this.refexFieldsEqual(anotherVersion)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean refexFieldsEqual(RefexVersionBI another) {
            return getCv().refexFieldsEqual(another);
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getRefexExtensionNid() {
            return refexExtensionNid;
        }

        RefexAnalogBI<R> getCv() {
            return (RefexAnalogBI<R>) cv;
        }

        public TtkRefexAbstractMemberChronicle<?> getERefsetMember() throws IOException {
            throw new UnsupportedOperationException("subclass must override");
        }

        public TtkRevision getERefsetRevision() throws IOException {
            throw new UnsupportedOperationException("subclass must override");
        }

        @Override
        public RefexMember getPrimordialVersion() {
            return RefexMember.this;
        }

        @Override
        public int getReferencedComponentNid() {
            return RefexMember.this.getReferencedComponentNid();
        }

        @Override
        public RefexCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
            return getCv().makeBlueprint(vc, idDirective, refexDirective);
        }
    
        public int getTypeNid() {
            return RefexMember.this.getTypeNid();
        }

        @Override
        public IntArrayList getVariableVersionNids() {
            if (RefexMember.this != getCv()) {
                return ((RefexRevision) getCv()).getVariableVersionNids();
            } else {
                return RefexMember.this.getVariableVersionNids();
            }
        }

        @Override
        public RefexMember<R, C>.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return RefexMember.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return RefexMember.this.getVersions();
        }

        @Override
        public Collection<RefexMember<R, C>.Version> getVersions(ViewCoordinate c) {
            return RefexMember.this.getVersions(c);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setRefexExtensionNid(int collectionNid) throws PropertyVetoException, IOException {
            RefexMember.this.setRefexExtensionNid(collectionNid);
        }

        @Override
        public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
            RefexMember.this.setReferencedComponentNid(componentNid);
        }

    }
}
