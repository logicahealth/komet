package org.ihtsdo.otf.tcc.model.cc.refex;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;


//import org.dwfa.ace.api.I_IntSet;

import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.NidPair;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
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
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;
import org.ihtsdo.otf.tcc.model.cc.component.Version;

public abstract class RefexMember<R extends RefexRevision<R, C>, C extends RefexMember<R, C>>
        extends ConceptComponent<R, C> implements RefexChronicleBI<R>, RefexAnalogBI<R> {

    public int referencedComponentNid;
    public int assemblageNid;
    protected List<? extends RefexMemberVersion> versions;

    //~--- constructors --------------------------------------------------------
    public RefexMember() {
        super();
        referencedComponentNid = Integer.MAX_VALUE;
        assemblageNid = Integer.MAX_VALUE;
    }

    public RefexMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public RefexMember(TtkRefexAbstractMemberChronicle<?> refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        assemblageNid = P.s.getNidForUuids(refsetMember.refexExtensionUuid);
        referencedComponentNid = P.s.getNidForUuids(refsetMember.getComponentUuid());
        primordialStamp = P.s.getStamp(refsetMember);
        assert primordialStamp != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        assert assemblageNid != Integer.MAX_VALUE;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(referencedComponentNid);
        allNids.add(assemblageNid);
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
    public RefexMember<R, C> merge(RefexMember<R, C> component) throws IOException {
        return (RefexMember<R, C>) super.merge((C) component);
    }

    @Override
    public void readFromBdb(TupleInput input) {
        assemblageNid = input.readInt();
        referencedComponentNid = input.readInt();
        assert assemblageNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        readMemberFields(input);

        int additionalVersionCount = input.readShort();

        if (additionalVersionCount > 0) {
            if (revisions == null) {
                revisions = new RevisionSet<R, C>(primordialStamp);
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
        assert assemblageNid != Integer.MAX_VALUE : assertionString();
        assert assemblageNid != 0 : assertionString();
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
        addNidToBuffer(buf, assemblageNid);
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
        ComponentVersionBI c1Component = snapshot.getConceptVersion(assemblageNid);

        return "refex: " + c1Component.toUserString(snapshot);
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     *
     * @param another
     * @return either a zero length String, or a String containing a description
     * of the validation failures.
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

        assert assemblageNid != Integer.MAX_VALUE;
        assert referencedComponentNid != Integer.MAX_VALUE;
        output.writeInt(assemblageNid);
        output.writeInt(referencedComponentNid);
        writeMember(output);
        output.writeShort(additionalVersionsToWrite.size());

        NidPairForRefex npr = NidPair.getRefexNidMemberNidPair(assemblageNid, nid);
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
    public int getAssemblageNid() {
        return assemblageNid;
    }

    @Override
    @Deprecated
    public int getRefexExtensionNid() {
        return getAssemblageNid();
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
                getAssemblageNid(),
                getVersion(vc), vc, idDirective, refexDirective);

        addSpecProperties(rcs);

        return rcs;
    }

    protected abstract RefexType getTkRefsetType();

    @Override
    public RefexMemberVersion<R, C> getVersion(ViewCoordinate c) throws ContradictionException {
        List<RefexMemberVersion<R, C>> vForC = getVersions(c);

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

    protected abstract VersionComputer<RefexMemberVersion<R, C>> getVersionComputer();

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends RefexMemberVersion<R,C>> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<RefexMemberVersion> list = new ArrayList<>(count);

            list.add(new RefexMemberVersion(this, this));

            if (revisions != null) {
                for (RefexRevision rv : revisions) {
                    list.add(new RefexMemberVersion(rv, this));
                }
            }

            versions = list;
        }

        return (List<RefexMemberVersion<R,C>>) versions;
    }

    @Override
    public List<RefexMemberVersion<R, C>> getVersions(ViewCoordinate c) {
        List<RefexMemberVersion<R, C>> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<RefexMemberVersion<R, C>> getVersions(ViewCoordinate c, long time) {
        List<RefexMemberVersion<R, C>> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager(), time);

        return returnTuples;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
        if ((this.assemblageNid == Integer.MAX_VALUE) || (this.assemblageNid == collectionNid)
                || (getTime() == Long.MAX_VALUE)) {
            if (this.assemblageNid != collectionNid) {
                if ((this.assemblageNid != 0) && (this.nid != 0)) {
                    NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.assemblageNid, this.nid);

                    P.s.forgetXrefPair(this.referencedComponentNid, oldNpr);
                }

                // new xref is added on the dbWrite.
                this.assemblageNid = collectionNid;
                modified();
            }
        } else {
            throw new PropertyVetoException("Cannot change refset unless member is uncommitted...", null);
        }
    }

    @Override
    @Deprecated
    public void setRefexExtensionNid(int collectionNid) throws PropertyVetoException, IOException {
        setAssemblageNid(collectionNid);
    }

    @Override
    public void setReferencedComponentNid(int referencedComponentNid) throws IOException {
        assert referencedComponentNid != Integer.MAX_VALUE : "referencedComponentNid is Integer.MAX_VALUE";
        assert assemblageNid != Integer.MAX_VALUE : "assemblageNid is Integer.MAX_VALUE";
        assert nid != Integer.MAX_VALUE : "nid is Integer.MAX_VALUE";
        if (this.referencedComponentNid != referencedComponentNid) {
            if ((this.referencedComponentNid != Integer.MAX_VALUE) && (this.assemblageNid != 0) && (this.nid != 0)) {
                NidPairForRefex oldNpr = NidPair.getRefexNidMemberNidPair(this.assemblageNid, this.nid);

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

}
