package org.ihtsdo.otf.tcc.chronicle.cc.media;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;



import org.ihtsdo.otf.tcc.chronicle.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.chronicle.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.chronicle.cc.attributes.ConceptAttributes;
import org.ihtsdo.otf.tcc.chronicle.cc.computer.version.VersionComputer;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.PositionSetBI;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaChronicle;
import org.ihtsdo.otf.tcc.dto.component.media.TtkMediaRevision;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;

import java.util.*;
import org.apache.mahout.math.list.IntArrayList;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.chronicle.cc.P;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.MediaCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;

public class Media extends ConceptComponent<MediaRevision, Media>
        implements MediaVersionFacade {

    private static VersionComputer<Media.Version> computer = new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    private String format;
    private byte[] image;
    private String textDescription;
    private int typeNid;
    List<Version> versions;

    //~--- constructors --------------------------------------------------------
    public Media() {
        super();
    }

    public Media(ConceptChronicleBI enclosingConcept, TupleInput input) throws IOException {
        super(enclosingConcept.getNid(), input);
    }

    public Media(TtkMediaChronicle eMedia, ConceptChronicleBI enclosingConcept) throws IOException {
        super(eMedia, enclosingConcept.getNid());
        image = eMedia.getDataBytes();
        format = eMedia.getFormat();
        textDescription = eMedia.getTextDescription();
        typeNid = P.s.getNidForUuids(eMedia.getTypeUuid());
        primordialStamp = P.s.getStamp(eMedia);

        if (eMedia.getRevisionList() != null) {
            revisions = new RevisionSet<>(primordialStamp);

            for (TtkMediaRevision eiv : eMedia.getRevisionList()) {
                revisions.add(new MediaRevision(eiv, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addComponentNids(Set<Integer> allNids) {
        allNids.add(typeNid);
    }

    @Override
    public void clearVersions() {
        versions = null;
        clearAnnotationVersions();
    }


    // TODO Verify this is a correct implementation
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Media.class.isAssignableFrom(obj.getClass())) {
            Media another = (Media) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<MediaRevision, Media> obj) {
        if (ConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            Media another = (Media) obj;

            if (!this.format.equals(another.format)) {
                return false;
            }

            if (!Arrays.equals(this.image, another.image)) {
                return false;
            }

            if (this.typeNid != another.typeNid) {
                return false;
            }

            return conceptComponentFieldsEqual(another);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{this.getNid()});
    }

    @Override
    public MediaRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
        MediaRevision newR;

        newR = new MediaRevision(this, status, time, authorNid, moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public void readFromBdb(TupleInput input) {

        // nid, list size, and conceptNid are read already by the binder...
        this.format = input.readString();

        int imageBytes = input.readInt();

        image = new byte[imageBytes];
        input.read(image, 0, imageBytes);
        textDescription = input.readString();
        typeNid = input.readInt();

        int additionalVersionCount = input.readShort();

        for (int i = 0; i < additionalVersionCount; i++) {
            MediaRevision ir = new MediaRevision(input, this);

            if (ir.getTime() != Long.MIN_VALUE) {
                revisions.add(ir);
            }
        }
    }

    @Override
    public boolean readyToWriteComponent() {
        assert textDescription != null : assertionString();
        assert format != null : assertionString();
        assert typeNid != Integer.MAX_VALUE : assertionString();
        assert image != null : assertionString();

        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName()).append(":{");
        buf.append("format:'").append(this.format).append("'");
        buf.append(" image:").append(this.image);
        buf.append(" textDescription:'").append(this.textDescription).append("'");
        buf.append(" typeNid:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" ");
        buf.append(super.toString());

        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuffer buf = new StringBuffer();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append("; ");
        buf.append(format);
        buf.append(": ");
        buf.append(textDescription);

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(Media another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        if (!this.format.equals(another.format)) {
            buf.append("\tImage.format not equal: \n\t\tthis.format = ").append(this.format).append(
                    "\n\t\tanother.format = ").append(another.format).append("\n");
        }

        if (!Arrays.equals(this.image, another.image)) {
            buf.append("\tImage.image not equal: \n" + "\t\tthis.image = ").append(this.image).append(
                    "\n\t\tanother.image = ").append(another.image).append("\n");
        }

        if (this.typeNid != another.typeNid) {
            buf.append("\tImage.typeNid not equal: \n\t\tthis.typeNid = ").append(this.typeNid).append(
                    "\n\t\tanother.typeNid = ").append(another.typeNid).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    @Override
    public void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<MediaRevision> partsToWrite = new ArrayList<>();

        if (revisions != null) {
            for (MediaRevision p : revisions) {
                if ((p.getStamp() > maxReadOnlyStatusAtPositionNid)
                        && (p.getTime() != Long.MIN_VALUE)) {
                    partsToWrite.add(p);
                }
            }
        }

        // Start writing
        // conceptNid is the enclosing concept, does not need to be written.
        output.writeString(format);
        output.writeInt(image.length);
        output.write(image);
        output.writeString(textDescription);
        output.writeInt(typeNid);
        output.writeShort(partsToWrite.size());

        for (MediaRevision p : partsToWrite) {
            p.writeRevisionBdb(output);
        }
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getConceptNid()
     */
    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ImageVersioned#getFormat()
     */
    @Override
    public MediaCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        MediaCAB mediaBp = new MediaCAB(getConceptNid(),
                getTypeNid(),
                getFormat(),
                getTextDescription(),
                getMedia(),
                getVersion(vc),
                vc, idDirective, refexDirective);
        return mediaBp;
    }

    @Override
    public String getFormat() {
        return format;
    }



    @Override
    public byte[] getMedia() {
        return image;
    }

    @Override
    public Media getPrimordialVersion() {
        return Media.this;
    }

    @Override
    public String getTextDescription() {
        return textDescription;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public IntArrayList getVariableVersionNids() {
        IntArrayList nidList = new IntArrayList(3);

        nidList.add(typeNid);

        return nidList;
    }

    @Override
    public Media.Version getVersion(ViewCoordinate c) throws ContradictionException {
        List<Media.Version> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return null;
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        return vForC.get(0);
    }

    @Override
    public List<Version> getVersions() {
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
                for (MediaRevision ir : revisions) {
                    if (ir.getTime() != Long.MIN_VALUE) {
                        list.add(new Version(ir));
                    }
                }
            }

            versions = list;
        }

        return versions;
    }

    @Override
    public List<Media.Version> getVersions(ViewCoordinate c) {
        List<Version> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null, c.getPositionSet(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public Collection<Media.Version> getVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
            PositionSetBI viewPositions, Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<Version> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPositions, returnTuples, getVersions(),
                precedence, contradictionMgr);

        return returnTuples;
    }

    //~--- set methods ---------------------------------------------------------
    public void setFormat(String format) {
        this.format = format;
        modified();
    }

    public void setImage(byte[] image) {
        this.image = image;
        modified();
    }

    @Override
    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
        modified();
    }

    @Override
    public void setTypeNid(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }

    //~--- inner classes -------------------------------------------------------
    public class Version extends ConceptComponent<MediaRevision, Media>.Version
            implements MediaVersionFacade {

        public Version(MediaVersionFacade cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------

        public MediaRevision makeAnalog() {
            if (getCv() != Media.this) {
                return new MediaRevision((MediaRevision) getCv(), Media.this);
            }

            return new MediaRevision(Media.this);
        }

        @Override
        public MediaRevision makeAnalog(org.ihtsdo.otf.tcc.api.coordinate.Status status, long time, int authorNid, int moduleNid, int pathNid) {
            return (MediaRevision) getCv().makeAnalog(status, time, authorNid, moduleNid, pathNid);
        }

        @Override
        public boolean fieldsEqual(ConceptComponent.Version another) {
            Media.Version anotherVersion = (Media.Version) another;
            if (!this.getFormat().equals(anotherVersion.getFormat())) {
                return false;
            }

            if (!Arrays.equals(this.getMedia(), anotherVersion.getMedia())) {
                return false;
            }

            if (this.getTypeNid() != anotherVersion.getTypeNid()) {
                return false;
            }

            return true;
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public int getConceptNid() {
            return enclosingConceptNid;
        }

        MediaVersionFacade getCv() {
            return (MediaVersionFacade) cv;
        }

        @Override
        public MediaCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
            return getCv().makeBlueprint(vc, idDirective, refexDirective);
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public byte[] getMedia() {
            return image;
        }
        @Override
        public Media getPrimordialVersion() {
            return Media.this;
        }

        @Override
        public String getTextDescription() {
            return getCv().getTextDescription();
        }

         @Override
        public int getTypeNid() {
            return getCv().getTypeNid();
        }

        @Override
        public IntArrayList getVariableVersionNids() {
            if (Media.this != getCv()) {
                return ((MediaRevision) getCv()).getVariableVersionNids();
            }

            return Media.this.getVariableVersionNids();
        }

        @Override
        public Media.Version getVersion(ViewCoordinate c) throws ContradictionException {
            return Media.this.getVersion(c);
        }

        @Override
        public List<? extends Version> getVersions() {
            return Media.this.getVersions();
        }

        @Override
        public Collection<Media.Version> getVersions(ViewCoordinate c) {
            return Media.this.getVersions(c);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setTextDescription(String name) throws PropertyVetoException {
            getCv().setTextDescription(name);
        }

        @Override
        public void setTypeNid(int type) throws PropertyVetoException {
            getCv().setTypeNid(type);
        }
    }
}
