/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.model.cc.refex2;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexUsageDescriptionBI;
import org.ihtsdo.otf.tcc.api.refex2.RefexVersionBI;
import org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.dto.component.TtkRevision;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.model.cc.NidPair;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * {@link RefexMember}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexMember
	extends ConceptComponent<RefexRevision, RefexMember> implements RefexChronicleBI<RefexRevision>, RefexAnalogBI<RefexRevision>
{
    public int referencedComponentNid;
    public int assemblageNid;
    protected List<? extends Version> versions;

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
        //TODO this probably needs work
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(referencedComponentNid);
        allNids.add(assemblageNid);
        addRefsetTypeNids(allNids);
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

        if (RefexMember.class.isAssignableFrom(obj.getClass())) {
            RefexMember another = (RefexMember) obj;

            return this.referencedComponentNid == another.referencedComponentNid;
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<RefexRevision, RefexMember> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            RefexMember another = (RefexMember) obj;

            //TODO add more on new data

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

    @SuppressWarnings("unchecked")
    public RefexMember merge(RefexMember component) throws IOException {
        return (RefexMember) super.merge(component);
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
                revisions = new RevisionSet<>(primordialStamp);
            }

            for (int i = 0; i < additionalVersionCount; i++) {
                RefexRevision r = readMemberRevision(input);

                if ((r.stamp != -1) && (r.getTime() != Long.MIN_VALUE)) {
                    revisions.add(r);
                }
            }
        }
    }

    @Override
    public final boolean readyToWriteComponent() {
        assert referencedComponentNid != Integer.MAX_VALUE : assertionString();
        assert referencedComponentNid != 0 : assertionString();
        assert assemblageNid != Integer.MAX_VALUE : assertionString();
        assert assemblageNid != 0 : assertionString();
        assert readyToWriteRefsetMember() : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(" refset:");
        addNidToBuffer(buf, assemblageNid);
//        buf.append(" type:");
//        buf.append(getTkRefsetType());  //TODO redo
        buf.append(" rcNid:");
        addNidToBuffer(buf, referencedComponentNid);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
        //TODO enhance
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
    public String validate(RefexMember another) throws IOException {
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

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<RefexRevision> additionalVersionsToWrite = new ArrayList<>();

        if (revisions != null) {
            for (RefexRevision p : revisions) {
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

        for (RefexRevision p : additionalVersionsToWrite) {
            p.writeRevisionBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return assemblageNid;
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
//        RefexCAB rcs = new RefexCAB(getTkRefsetType(),
//                P.s.getUuidPrimordialForNid(getReferencedComponentNid()),
//                getAssemblageNid(),
//                getVersion(vc), vc, idDirective, refexDirective);
//
//        addSpecProperties(rcs);
//TODO fix CAB stuff
        return null;//rcs;
    }

    @Override
    public RefexMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<RefexMember.Version> vForC = getVersions(c);

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

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends Version> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<Version> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new Version(this));
            }

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
    public List<RefexMember.Version> getVersions(ViewCoordinate c) {
        List<RefexMember.Version> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<RefexMember.Version> getVersions(ViewCoordinate c, long time) {
        List<RefexMember.Version> returnTuples = new ArrayList<>(2);

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

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<RefexRevision, RefexMember>.Version
            implements RefexAnalogBI<RefexRevision> {

        public Version(RefexAnalogBI<RefexRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        public RefexRevision makeAnalog() {
            if (RefexMember.this != cv) {
            }

            return (RefexRevision) RefexMember.this.makeAnalog();
        }

        @Override
        public RefexRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
            return getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent.Version another) {
            RefexMember.Version anotherVersion = (RefexMember.Version) another;
//            if (this.getTypeNid() != anotherVersion.getTypeNid()) {
//                return false;
//            }
            //TODO account for new data

            if (this.getAssemblageNid() != anotherVersion.getAssemblageNid()) {
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
        public int getAssemblageNid() {
            return assemblageNid;
        }

        RefexAnalogBI<RefexRevision> getCv() {
            return (RefexAnalogBI<RefexRevision>) cv;
        }

        public TtkRefexAbstractMemberChronicle<?> getERefsetMember() throws IOException {
            throw new UnsupportedOperationException("subclass must override");
            //TODO implement
        }

        public TtkRevision getERefsetRevision() throws IOException {
            throw new UnsupportedOperationException("subclass must override");
            //TODO implement
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

        @Override
        public IntArrayList getVariableVersionNids() {
            if (RefexMember.this != getCv()) {
                return ((RefexRevision) getCv()).getVariableVersionNids();
            } else {
                return RefexMember.this.getVariableVersionNids();
            }
        }

        @Override
        public RefexMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return RefexMember.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return RefexMember.this.getVersions();
        }

        @Override
        public Collection<RefexMember.Version> getVersions(ViewCoordinate c) {
            return RefexMember.this.getVersions(c);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
            RefexMember.this.setAssemblageNid(collectionNid);
        }

        @Override
        public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
            RefexMember.this.setReferencedComponentNid(componentNid);
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescriptorNid()
         */
        @Override
        public int getRefexUsageDescriptorNid() {
            // TODO Auto-generated method stub
            return 0;
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescription()
         */
        @Override
        public RefexUsageDescriptionBI getRefexUsageDescription() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData()
         */
        @Override
        public RefexDataBI[] getData() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData(int)
         */
        @Override
        public RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setRefexUsageDescriptorNid(int)
         */
        @Override
        public void setRefexUsageDescriptorNid(int refexUsageDescriptorNid) {
            // TODO Auto-generated method stub

        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI[])
         */
        @Override
        public void setData(RefexDataBI[] data) throws PropertyVetoException {
            // TODO Auto-generated method stub

        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(int,
         *      org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI)
         */
        @Override
        public void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException {
            // TODO Auto-generated method stub

        }
    }
    
    /**
     * From MembershipMember below here
     */
    
    private static VersionComputer<RefexMember.Version> computer =
            new VersionComputer<>();
    
    protected void addRefsetTypeNids(Set<Integer> allNids) {

       //
    }

    protected void addSpecProperties(RefexCAB rcs) {

       // no fields to add...
    }

    public RefexRevision makeAnalog() {
       RefexRevision newR = new RefexRevision(getStatus(), getTime(), getAuthorNid(), getModuleNid(), getPathNid(), this);

       return newR;
    }

    @Override
    public RefexRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
       RefexRevision newR = new RefexRevision(status, time, authorNid, moduleNid, pathNid, this);

       addRevision(newR);

       return newR;
    }

    protected boolean refexFieldsEqual(ConceptComponent<RefexRevision, RefexMember> obj) {
       if (RefexMember.class.isAssignableFrom(obj.getClass())) {
          return true;
       }
//TODO add impl for new data
       return false;
    }
    
    
    @Override
     public boolean refexFieldsEqual(RefexVersionBI<?> another) {
         if(RefexMemberVersionBI.class.isAssignableFrom(another.getClass())){
             return true;
         }
         return false;
     }

    protected void readMemberFields(TupleInput input) {

       // nothing to read...
        //TODO now there is
    }

    protected final RefexRevision readMemberRevision(TupleInput input) {
       return new RefexRevision(input, this);
    }

    public boolean readyToWriteRefsetMember() {
       return true;
    }

    protected void writeMember(TupleOutput output) {

       // nothing to write
        //TODO now there is
    }

    @Override
    protected IntArrayList getVariableVersionNids() {
       return new IntArrayList(2);
    }

    protected VersionComputer<RefexMember.Version> getVersionComputer() {
       return computer;
    }

    /**
     * New methods here down
     */
    
    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setRefexUsageDescriptorNid(int)
     */
    @Override
    public void setRefexUsageDescriptorNid(int refexUsageDescriptorNid) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI[])
     */
    @Override
    public void setData(RefexDataBI[] data) throws PropertyVetoException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexAnalogBI#setData(int, org.ihtsdo.otf.tcc.api.refex2.data.RefexDataBI)
     */
    @Override
    public void setData(int columnNumber, RefexDataBI data) throws IndexOutOfBoundsException, PropertyVetoException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescriptorNid()
     */
    @Override
    public int getRefexUsageDescriptorNid() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getRefexUsageDescription()
     */
    @Override
    public RefexUsageDescriptionBI getRefexUsageDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData()
     */
    @Override
    public RefexDataBI[] getData() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refex2.RefexChronicleBI#getData(int)
     */
    @Override
    public RefexDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }
}
