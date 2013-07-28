package org.ihtsdo.otf.tcc.chronicle.cc.attributes;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeAnalogBI;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

public class ConceptAttributes extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
        implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {

    private static VersionComputer<ConceptAttributes.Version> computer =
            new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    private boolean defined;
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public ConceptAttributes() {
        super();
    }

    public ConceptAttributes(ConceptChronicleBI enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
        assert this.nid == enclosingConcept.getNid(): "[1] nid and cNid don't match: " + 
                enclosingConcept + "\n\n" + this;
    }

    public ConceptAttributes(TtkConceptAttributesChronicle eAttr, ConceptChronicleBI c) throws IOException {
        super(eAttr, c.getNid());
        assert this.nid == c.getNid(): "[2] nid and cNid don't match: " + 
                eAttr + "\n\n" + c;
        defined = eAttr.isDefined();

        if (eAttr.getRevisionList() != null) {
            revisions = new RevisionSet(primordialStamp);

            for (TtkConceptAttributesRevision ear : eAttr.getRevisionList()) {
                revisions.add(new ConceptAttributesRevision(ear, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        // nothing to add
    }

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

        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributes another = (ConceptAttributes) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<ConceptAttributesRevision, ConceptAttributes> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            ConceptAttributes another = (ConceptAttributes) obj;

            if (this.defined == another.defined) {
                return conceptComponentFieldsEqual(another);
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid});
    }

    @Override
    public ConceptAttributesRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        ConceptAttributesRevision newR;

        newR = new ConceptAttributesRevision(this, status, time, authorNid, moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public void readFromBdb(TupleInput input) {
        try {

            // nid, list size, and conceptNid are read already by the binder...
            defined = input.readBoolean();

            int additionalVersionCount = input.readShort();

            if (additionalVersionCount > 0) {
                if (revisions == null) {
                    revisions = new RevisionSet(primordialStamp);
                }

                for (int i = 0; i < additionalVersionCount; i++) {
                    ConceptAttributesRevision car = new ConceptAttributesRevision(input, this);

                    if (car.getTime() != Long.MIN_VALUE) {
                        revisions.add(car);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(" Processing nid: " + enclosingConceptNid, e);
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append("defined:").append(this.defined);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuilder buf = new StringBuilder();

        buf.append("concept ");

        if (defined) {
            buf.append("is fully defined");
        } else {
            buf.append("is primitive");
        }

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     *
     * @param another
     * @return either a zero length String, or a String containing a description of the validation failures.
     * @throws IOException
     */
    public String validate(ConceptAttributes another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        // Compare defined
        if (this.defined != another.defined) {
            buf.append("\tConceptAttributes.defined not equal: "
                    + "\n\t\tthis.defined = ").append(this.defined).append("\n"
                    + "\t\tanother.defined = ").append(another.defined).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStamp) {
        List<ConceptAttributesRevision> partsToWrite = new ArrayList<>();

        if (revisions != null) {
            for (ConceptAttributesRevision p : revisions) {
                if ((p.getStamp() > maxReadOnlyStamp)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        // Start writing
        output.writeBoolean(defined);
        output.writeShort(partsToWrite.size());

        for (ConceptAttributesRevision p : partsToWrite) {
            p.writeRevisionBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ConceptAttributeVersioned#getConId()
     */
//    @Override
//    public int getConId() {
//        return nid;
//    }
    @Override
    public ConceptAttributeAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        ConceptAttributeAB conAttrBp = new ConceptAttributeAB(nid, defined,
                getVersion(vc), vc, refexDirective, idDirective);
        return conAttrBp;
    }

    @Override
    public ConceptAttributes getPrimordialVersion() {
        return this;
    }

    @Override
    public IntArrayList getVariableVersionNids() {
        return new IntArrayList(2);
    }

    @Override
    public ConceptAttributes.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<ConceptAttributes.Version> vForC = getVersions(c);

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

    @Override
    public List<Version> getVersions() {
        List<Version> list = versions;

        if (list == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version(this));
            }

            if (revisions != null) {
                for (ConceptAttributesRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(r));
                    }
                }
            }

            versions = list;
        }

        return list;
    }

    @Override
    public List<ConceptAttributes.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public Collection<Version> getVersions(EnumSet<Status> allowedStatus, PositionSetBI viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(allowedStatus, viewPositions, returnTuples, getVersions(), precedence,
                contradictionMgr);

        return returnTuples;
    }

//    @Override
//    public boolean hasExtensions() throws IOException {
//        return getEnclosingConcept().hasExtensionsForComponent(nid);
//    }
    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    public void setConId(int cNid) {
        if (this.nid == Integer.MIN_VALUE) {
            this.nid = cNid;
        } else {
            throw new RuntimeException("Cannot change the cNid once set");
        }
    }

    @Override
    public void setDefined(boolean defined) {
        this.defined = defined;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version
            implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {

        public Version(ConceptAttributeAnalogBI<ConceptAttributesRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        public ConceptAttributesRevision makeAnalog() {
            if (cv == ConceptAttributes.this) {
                return new ConceptAttributesRevision(ConceptAttributes.this, ConceptAttributes.this);
            }

            return new ConceptAttributesRevision((ConceptAttributesRevision) getCv(), ConceptAttributes.this);
        }

        @Override
        public ConceptAttributesRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
            return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent<ConceptAttributesRevision, ConceptAttributes>.Version another) {
            ConceptAttributes.Version anotherVersion = (ConceptAttributes.Version) another;
            if (this.isDefined() == anotherVersion.isDefined()) {
                return true;
            }
            return false;
        }

        public ConceptAttributeAnalogBI<ConceptAttributesRevision> getCv() {
            return (ConceptAttributeAnalogBI<ConceptAttributesRevision>) cv;
        }

        @Override
        public ConceptAttributeAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
            return getCv().makeBlueprint(vc, idDirective, refexDirective);
        }

        @Override
        public ConceptAttributes getPrimordialVersion() {
            return ConceptAttributes.this;
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public IntArrayList getVariableVersionNids() {
            return new IntArrayList(2);
        }

        @Override
        public ConceptAttributes.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return ConceptAttributes.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return ConceptAttributes.this.getVersions();
        }

        @Override
        public Collection<ConceptAttributes.Version> getVersions(ViewCoordinate c) {
            return ConceptAttributes.this.getVersions(c);
        }

        @Override
        public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDefined() {
            return getCv().isDefined();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setDefined(boolean defined) throws PropertyVetoException {
            getCv().setDefined(defined);
        }
    }
}
