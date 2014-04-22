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
package org.ihtsdo.otf.tcc.model.cc.refexDynamic;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.naming.InvalidNameException;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDynamicCAB;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataType;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicUsageDescription;
import org.ihtsdo.otf.tcc.api.store.TerminologySnapshotDI;
import org.ihtsdo.otf.tcc.model.cc.NidPair;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.P;
import org.ihtsdo.otf.tcc.model.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.model.cc.refexDynamic.data.RefexDynamicData;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * {@link RefexDynamicMember}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("deprecation")
public class RefexDynamicMember extends ConceptComponent<RefexDynamicRevision, RefexDynamicMember> implements RefexDynamicChronicleBI<RefexDynamicRevision>, 
    RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI
{
    public int referencedComponentNid;
    public int assemblageNid;
    private RefexDynamicDataBI[] data_;
    protected List<? extends Version> versions;

    //~--- constructors --------------------------------------------------------
    public RefexDynamicMember() {
        super();
        referencedComponentNid = Integer.MAX_VALUE;
        assemblageNid = Integer.MAX_VALUE;
    }

    public RefexDynamicMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    //TODO [REFEX] do I need this?  before I can implement this, I'd need to create TtkRefexDynamicMemberChronicle....
//    public RefexDynamicMember(TtkRefexAbstractMemberChronicle<?> refsetMember, int enclosingConceptNid) throws IOException {
//        super(refsetMember, enclosingConceptNid);
//        assemblageNid = P.s.getNidForUuids(refsetMember.refexExtensionUuid);
//        referencedComponentNid = P.s.getNidForUuids(refsetMember.getComponentUuid());
//        primordialStamp = P.s.getStamp(refsetMember);
//        assert primordialStamp != Integer.MAX_VALUE;
//        assert referencedComponentNid != Integer.MAX_VALUE;
//        assert assemblageNid != Integer.MAX_VALUE;
//        
//        
////        c1Nid      = P.s.getNidForUuids(refsetMember.getUuid1());
////        floatValue = refsetMember.getDa
////
////        if (refsetMember.getRevisionList() != null) {
////           revisions = new RevisionSet<>(primordialStamp);
////
////           for (TtkRefexUuidFloatRevision eVersion : refsetMember.getRevisionList()) {
////              revisions.add(new NidFloatRevision(eVersion, this));
////           }
////        }
//        
//    }

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

        if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicMember another = (RefexDynamicMember) obj;
            if (this.getAssemblageNid() == another.getAssemblageNid() && this.getReferencedComponentNid() == another.getReferencedComponentNid() 
                    && Arrays.deepEquals(this.getData(), another.getData())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            RefexDynamicMember another = (RefexDynamicMember) obj;

            if (this.getAssemblageNid() != another.getAssemblageNid()) {
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
        return Hashcode.compute(new int[]{referencedComponentNid, assemblageNid, Arrays.deepHashCode(getData())});
    }

    @Override
    public RefexDynamicMember merge(RefexDynamicMember component) throws IOException {
        return (RefexDynamicMember) super.merge(component);
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
                RefexDynamicRevision r = readMemberRevision(input);

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
        buf.append(" rcNid:");
        addNidToBuffer(buf, referencedComponentNid);
        buf.append(" ");
        buf.append(Arrays.toString(getData()));
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

    //TODO [REFEX] no idea what this is for, or if we need it
//    /**
//     * Test method to check to see if two objects are equal in all respects.
//     *
//     * @param another
//     * @return either a zero length String, or a String containing a description
//     * of the validation failures.
//     * @throws IOException
//     */
//    public String validate(RefexDynamicMember another) throws IOException {
//        assert another != null;
//
//        StringBuilder buf = new StringBuilder();
//
//        if (this.referencedComponentNid != another.referencedComponentNid) {
//            buf.append(
//                    "\tRefsetMember.referencedComponentNid not equal: \n"
//                    + "\t\tthis.referencedComponentNid = ").append(this.referencedComponentNid).append(
//                    "\n" + "\t\tanother.referencedComponentNid = ").append(
//                    another.referencedComponentNid).append("\n");
//        }
//
//        // Compare the parents
//        buf.append(super.validate(another));
//
//        return buf.toString();
//    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<RefexDynamicRevision> additionalVersionsToWrite = new ArrayList<>();

        if (revisions != null) {
            for (RefexDynamicRevision p : revisions) {
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

        for (RefexDynamicRevision p : additionalVersionsToWrite) {
            p.writeRevisionBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public int getAssemblageNid() {
        return assemblageNid;
    }

    @Override
    public RefexDynamicMember getPrimordialVersion() {
        return RefexDynamicMember.this;
    }

    @Override
    public int getReferencedComponentNid() {
        return referencedComponentNid;
    }

    @Override
    public RefexDynamicCAB makeBlueprint(ViewCoordinate vc,
            IdDirective idDirective, RefexDirective refexDirective) throws IOException,
            InvalidCAB, ContradictionException {

        RefexDynamicCAB rdc = new RefexDynamicCAB(
                P.s.getUuidPrimordialForNid(getReferencedComponentNid()),
                getAssemblageNid(),
                getVersion(vc), 
                vc, 
                idDirective, 
                refexDirective);

        rdc.setData(getData());
        return rdc;
    }

    @Override
    public RefexDynamicMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<RefexDynamicMember.Version> vForC = getVersions(c);

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
                for (RefexDynamicRevision rv : revisions) {
                    list.add(new Version(rv));
                }
            }

            versions = list;
        }

        return (List<Version>) versions;
    }

    @Override
    public List<RefexDynamicMember.Version> getVersions(ViewCoordinate c) {
        List<RefexDynamicMember.Version> returnTuples = new ArrayList<>(2);

        getVersionComputer().addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null,
                c.getViewPosition(), returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<RefexDynamicMember.Version> getVersions(ViewCoordinate c, long time) {
        List<RefexDynamicMember.Version> returnTuples = new ArrayList<>(2);

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
    public class Version extends ConceptComponent<RefexDynamicRevision, RefexDynamicMember>.Version
            implements RefexDynamicVersionBI<RefexDynamicRevision>, RefexDynamicBuilderBI {

        public Version(RefexDynamicVersionBI<RefexDynamicRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        public RefexDynamicRevision makeAnalog() {
            throw new UnsupportedOperationException("Must use Blueprints");
        }

        @Override
        public RefexDynamicRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
            throw new UnsupportedOperationException("Must use Blueprints");
        }

        @Override
        public boolean fieldsEqual(@SuppressWarnings("rawtypes") ConceptComponent.Version another) {
            RefexDynamicMember.Version anotherVersion = (RefexDynamicMember.Version) another;

            if (this.getAssemblageNid() != anotherVersion.getAssemblageNid()) {
                return false;
            }

            if (this.getReferencedComponentNid() != anotherVersion.getReferencedComponentNid()) {
                return false;
            }

            if (this.refexDataFieldsEqual(anotherVersion.getData())) {
                return true;
            }
            return false;
        }

        @Override
        public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
            return getCv().refexDataFieldsEqual(another);
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getAssemblageNid() {
            return assemblageNid;
        }

        @SuppressWarnings("unchecked")
        RefexDynamicVersionBI<RefexDynamicRevision> getCv() {
            return (RefexDynamicVersionBI<RefexDynamicRevision>) cv;
        }

      //TODO [REFEX] not sure if I need these - don't have the types yet
//        public TtkRefexAbstractMemberChronicle<?> getERefsetMember() throws IOException {
//            throw new UnsupportedOperationException("subclass must override");
//        }
//
//        public TtkRevision getERefsetRevision() throws IOException {
//            throw new UnsupportedOperationException("subclass must override");
//        }

        @Override
        public RefexDynamicMember getPrimordialVersion() {
            return RefexDynamicMember.this;
        }

        @Override
        public int getReferencedComponentNid() {
            return RefexDynamicMember.this.getReferencedComponentNid();
        }

        @Override
        public RefexDynamicCAB makeBlueprint(ViewCoordinate vc,
                IdDirective idDirective, RefexDirective refexDirective) throws IOException, InvalidCAB, ContradictionException {
            return getCv().makeBlueprint(vc, idDirective, refexDirective);
        }

        @Override
        public IntArrayList getVariableVersionNids() {
            if (RefexDynamicMember.this != getCv()) {
                return ((RefexDynamicRevision) getCv()).getVariableVersionNids();
            } else {
                return RefexDynamicMember.this.getVariableVersionNids();
            }
        }

        @Override
        public RefexDynamicMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return RefexDynamicMember.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return RefexDynamicMember.this.getVersions();
        }

        @Override
        public Collection<RefexDynamicMember.Version> getVersions(ViewCoordinate c) {
            return RefexDynamicMember.this.getVersions(c);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setAssemblageNid(int collectionNid) throws PropertyVetoException, IOException {
            RefexDynamicMember.this.setAssemblageNid(collectionNid);
        }

        @Override
        public void setReferencedComponentNid(int componentNid) throws PropertyVetoException, IOException {
            RefexDynamicMember.this.setReferencedComponentNid(componentNid);
        }

        /**
         * @throws ContradictionException 
         * @throws IOException 
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getRefexDynamicUsageDescription()
         */
        @Override
        public RefexDynamicUsageDescription getRefexDynamicUsageDescription() throws IOException, ContradictionException {
            return getCv().getRefexDynamicUsageDescription();
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getData()
         */
        @Override
        public RefexDynamicDataBI[] getData() {
            return getCv().getData();
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getData(int)
         */
        @Override
        public RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
            return getCv().getData(columnNumber);
        }
        
        /**
         * @throws ContradictionException 
         * @throws IOException 
         * @throws InvalidNameException 
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(java.lang.String)
         */
        @Override
        public RefexDynamicDataBI getData(String columnName) throws IndexOutOfBoundsException, InvalidNameException, IOException, ContradictionException {
            return getCv().getData(columnName);
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI[])
         */
        @Override
        public void setData(RefexDynamicDataBI[] data) throws PropertyVetoException
        {
            ((RefexDynamicRevision)getCv()).setData(data);
            
        }

        /**
         * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(int, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI)
         */
        @Override
        public void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
        {
            ((RefexDynamicRevision)getCv()).setData(columnNumber, data);
        }
    }
    
    /**
     * From MembershipMember below here
     */
    
    private static VersionComputer<RefexDynamicMember.Version> computer = new VersionComputer<>();

    protected void addRefsetTypeNids(Set<Integer> allNids) {
        for (RefexDynamicDataBI data : getData())
        {
            if (data.getRefexDataType() == RefexDynamicDataType.NID)
            {
                allNids.add((int)data.getDataObject());
            }
        }
    }

    @Override
    public RefexDynamicRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
       throw new UnsupportedOperationException("Must use Blueprints");
    }

    protected boolean refexFieldsEqual(ConceptComponent<RefexDynamicRevision, RefexDynamicMember> obj) {
       if (RefexDynamicMember.class.isAssignableFrom(obj.getClass())) {
          RefexDynamicMember another = (RefexDynamicMember) obj;
          return refexDataFieldsEqual(another.getData());
       }
       return false;
    }
    
    @Override
    public boolean refexDataFieldsEqual(RefexDynamicDataBI[] another) {
        return Arrays.deepEquals(getData(), another);
    }

    protected void readMemberFields(TupleInput input) {

        //read the following format - 
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        int colCount = input.readInt();
        data_ = new RefexDynamicDataBI[colCount];
        for (int i = 0; i < colCount; i++)
        {
            RefexDynamicDataType dt = RefexDynamicDataType.getFromToken(input.readInt());
            int dataLength = input.readInt();
            byte[] data = new byte[dataLength];
            input.read(data);
            
            data_[i] = RefexDynamicData.typeToClass(dt, data, getAssemblageNid(), i);
        }
    }

    protected final RefexDynamicRevision readMemberRevision(TupleInput input) {
       return new RefexDynamicRevision(input, this);
    }

    public boolean readyToWriteRefsetMember() {
        //I don't think I need to do any validation here, as the blueprint process shouldn't allow the creation of data that isn't ready
       return true;
    }

    protected void writeMember(TupleOutput output) {

        //Write with the following format - 
        //dataFieldCount [dataFieldType dataFieldSize dataFieldBytes] [dataFieldType dataFieldSize dataFieldBytes] ...
        output.writeInt(getData().length);
        for (RefexDynamicDataBI column : getData())
        {
            output.writeInt(column.getRefexDataType().getTypeToken());
            output.writeInt(column.getData().length);
            output.write(column.getData());
        }
    }

    @Override
    protected IntArrayList getVariableVersionNids() {
       return new IntArrayList(2);
    }

    protected VersionComputer<RefexDynamicMember.Version> getVersionComputer() {
       return computer;
    }

    /**
     * New methods here down
     */
    
    /**
     * @throws ContradictionException 
     * @throws IOException 
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicChronicleBI#getRefexDynamicUsageDescription()
     */
    @Override
    public RefexDynamicUsageDescription getRefexDynamicUsageDescription() throws IOException, ContradictionException {
        return RefexDynamicUsageDescription.read(getAssemblageNid());
    }
    
    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData()
     */
    @Override
    public RefexDynamicDataBI[] getData() {
        if (data_ == null)
        {
            data_ = new RefexDynamicData[] {};
        }
        return data_;
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(int)
     */
    @Override
    public RefexDynamicDataBI getData(int columnNumber) throws IndexOutOfBoundsException {
        RefexDynamicDataBI[] temp = getData();
        if (columnNumber >= temp.length)
        {
            throw new IndexOutOfBoundsException("Data contains " + temp.length + " columns.  Can't ask for column " + columnNumber);
        }
        return temp[columnNumber];
    }
    
    /**
     * @throws ContradictionException 
     * @throws IOException 
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicVersionBI#getData(java.lang.String)
     */
    @Override
    public RefexDynamicDataBI getData(String columnName) throws IndexOutOfBoundsException, IOException, ContradictionException {
        for (RefexDynamicColumnInfo ci : getRefexDynamicUsageDescription().getColumnInfo())
        {
            if (ci.getColumnName().equals(columnName))
            {
                return getData(ci.getColumnOrder());
            }
        }
        throw new IndexOutOfBoundsException("Could not find a column with name '" + columnName + "'");
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI[])
     */
    @Override
    public void setData(RefexDynamicDataBI[] data) throws PropertyVetoException
    {
        if (data == null)
        {
            data_ = new RefexDynamicData[] {};
        }
        else
        {
            data_ = data;
        }
    }

    /**
     * @see org.ihtsdo.otf.tcc.api.refexDynamic.RefexDynamicBuilderBI#setData(int, org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicDataBI)
     */
    @Override
    public void setData(int columnNumber, RefexDynamicDataBI data) throws IndexOutOfBoundsException, PropertyVetoException
    {
        RefexDynamicDataBI[] temp = getData();
        if (columnNumber >= temp.length)
        {
            throw new IndexOutOfBoundsException("Data size is " + temp.length + " columns.  Can't set column " + columnNumber);
        }
        temp[columnNumber] = data;
    }
}
