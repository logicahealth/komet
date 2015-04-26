package org.ihtsdo.otf.tcc.model.cc.attributes;

//~--- non-JDK imports --------------------------------------------------------
import java.io.IOException;

import java.util.*;

import org.ihtsdo.otf.tcc.api.blueprint.ConceptAttributeAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.conattr.ConceptAttributeAnalogBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesChronicle;
import org.ihtsdo.otf.tcc.dto.component.attribute.TtkConceptAttributesRevision;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;

public class ConceptAttributes extends ConceptComponent<ConceptAttributesRevision, ConceptAttributes>
        implements ConceptAttributeAnalogBI<ConceptAttributesRevision> {

    private static VersionComputer<ConceptAttributesVersion> computer
            = new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    protected boolean defined;
    List<ConceptAttributesVersion> versions;

    //~--- constructors --------------------------------------------------------
    public ConceptAttributes() {
        super();
    }

    public ConceptAttributes(TtkConceptAttributesChronicle eAttr, ConceptChronicleBI c) throws IOException {
        super(eAttr, c.getNid());
        assert this.nid == c.getNid() : "[2] nid and cNid don't match: "
                + this.nid + ":" + c.getNid() + " processing: "
                + eAttr + "\n\n" + c;
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

    public String toSimpleString() {
        StringBuilder buf = new StringBuilder();
        buf.append(" -nid: ").append(nid);
        buf.append(" -enclosing concept nid: ").append(enclosingConceptNid);
        buf.append(" -defined: ").append(defined);
        buf.append(" -revision count: ").append(revisions.size());
        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     *
     * @param another
     * @return either a zero length String, or a String containing a description
     * of the validation failures.
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
    public ConceptAttributesVersion getVersion(ViewCoordinate c) throws ContradictionException {
        List<ConceptAttributesVersion> vForC = getVersions(c);

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
    public List<ConceptAttributesVersion> getVersions() {
        List<ConceptAttributesVersion> list = versions;

        if (list == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new ConceptAttributesVersion(this, this, primordialStamp));
                for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                    list.add(new ConceptAttributesVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (ConceptAttributesRevision r : revisions) {
                    if (r.getTime() != Long.MIN_VALUE) {
                        list.add(new ConceptAttributesVersion(r, this, r.stamp));
                        for (int stampAlias : getCommitManager().getAliases(r.stamp)) {
                            list.add(new ConceptAttributesVersion(r, this, stampAlias));
                        }
                    }
                }
            }

            versions = list;
        }

        return list;
    }

    @Override
    public List<ConceptAttributesVersion> getVersions(ViewCoordinate c) {
        List<ConceptAttributesVersion> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null, c.getViewPosition(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public Collection<ConceptAttributesVersion> getVersions(EnumSet<Status> allowedStatus, Position viewPositions,
            Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<ConceptAttributesVersion> returnTuples = new ArrayList<>(2);

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

}
