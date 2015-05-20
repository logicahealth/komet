package org.ihtsdo.otf.tcc.model.cc.description;

//~--- non-JDK imports --------------------------------------------------------
import java.io.IOException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ihtsdo.otf.tcc.api.blueprint.DescriptionCAB;
import org.ihtsdo.otf.tcc.api.blueprint.IdDirective;
import org.ihtsdo.otf.tcc.api.blueprint.InvalidCAB;
import org.ihtsdo.otf.tcc.api.blueprint.RefexDirective;
import org.ihtsdo.otf.tcc.api.concept.ConceptChronicleBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionManagerBI;
import org.ihtsdo.otf.tcc.api.coordinate.Position;
import org.ihtsdo.otf.tcc.api.coordinate.Precedence;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.description.DescriptionAnalogBI;
import org.ihtsdo.otf.tcc.api.hash.Hashcode;
import org.ihtsdo.otf.tcc.api.lang.LanguageCode;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionRevision;
import org.ihtsdo.otf.tcc.model.cc.PersistentStore;
import org.ihtsdo.otf.tcc.model.cc.component.ConceptComponent;
import org.ihtsdo.otf.tcc.model.cc.component.RevisionSet;
import org.ihtsdo.otf.tcc.model.version.VersionComputer;

public class Description extends ConceptComponent<DescriptionRevision, Description>
        implements DescriptionAnalogBI<DescriptionRevision> {
    private static VersionComputer<DescriptionVersion> computer = new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    protected boolean initialCaseSignificant;
    protected String lang;
    protected String text;
    protected int typeNid;

    /*
     * Consider depreciating the below methods...
     */
    List<DescriptionVersion> versions;

    //~--- constructors --------------------------------------------------------
    public Description() {
        super();
    }

    public Description(TtkDescriptionChronicle eDesc, ConceptChronicleBI enclosingConcept) throws IOException {
        super(eDesc, enclosingConcept.getNid());
        initialCaseSignificant = eDesc.isInitialCaseSignificant();
        lang = eDesc.getLang();
        text = eDesc.getText();
        typeNid = PersistentStore.get().getNidForUuids(eDesc.getTypeUuid());
        primordialStamp = PersistentStore.get().getStamp(eDesc);

        if (eDesc.getRevisionList() != null) {
            revisions = new RevisionSet<>(primordialStamp);

            for (TtkDescriptionRevision edv : eDesc.getRevisionList()) {
                
                    revisions.add(new DescriptionRevision(edv, this));
                
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
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;

            if (this.nid == another.nid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean fieldsEqual(ConceptComponent<DescriptionRevision, Description> obj) {
        if (Description.class.isAssignableFrom(obj.getClass())) {
            Description another = (Description) obj;

            if (this.initialCaseSignificant != another.initialCaseSignificant) {
                return false;
            }

            if (!this.text.equals(another.text)) {
                return false;
            }

            if (!this.lang.equals(another.lang)) {
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
        return Hashcode.compute(new int[]{nid});
    }

    @Override
    public DescriptionRevision makeAnalog(Status status, long time, int authorNid, int moduleNid, int pathNid) {
        DescriptionRevision newR;
        newR = new DescriptionRevision(this, status, time, authorNid,
                moduleNid, pathNid, this);
        addRevision(newR);

        return newR;
    }

    @Override
    public boolean matches(Pattern p) {
        String lastText = null;

        for (DescriptionVersion desc : getVersions()) {
            if (!desc.getText().equals(lastText)) {
                lastText = desc.getText();

                Matcher m = p.matcher(lastText);

                if (m.find()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean readyToWriteComponent() {
        assert text != null : assertionString();
        assert typeNid != Integer.MAX_VALUE : assertionString();
        assert lang != null : assertionString();

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
        buf.append("cNid: ").append(this.enclosingConceptNid).append(" ");
        buf.append("text: '").append(this.getText()).append("'");
        buf.append(" caseSig: ").append(isInitialCaseSignificant());
        buf.append(" type:");
        ConceptComponent.addNidToBuffer(buf, typeNid);
        buf.append(" lang:").append(this.getLang());
        buf.append(" ");
        buf.append(super.toString());
        buf.append("}");

        return buf.toString();
    }
    
    public String toSimpleString(){
        StringBuilder buf = new StringBuilder();
        buf.append("-nid: ").append(nid);
        buf.append("-enclosing concept nid: ").append(enclosingConceptNid);
        buf.append("-text: ").append(text);
        buf.append("-cs: ").append(initialCaseSignificant);
        buf.append("-lang: ").append(lang);
        return buf.toString();
    }

    @Override
    public String toUserString() {
        StringBuilder buf = new StringBuilder();

        ConceptComponent.addTextToBuffer(buf, typeNid);
        buf.append(": ");
        buf.append("'").append(this.getText()).append("'");

        return buf.toString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * @param another
     * @return either a zero length String, or a String containing a description of the
     * validation failures.
     * @throws IOException
     */
    public String validate(Description another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();

        if (this.initialCaseSignificant != another.initialCaseSignificant) {
            buf.append(
                    "\tDescription.initialCaseSignificant not equal: \n"
                    + "\t\tthis.initialCaseSignificant = ").append(this.initialCaseSignificant).append(
                    "\n" + "\t\tanother.initialCaseSignificant = ").append(
                    another.initialCaseSignificant).append("\n");
        }

        if (!this.text.equals(another.text)) {
            buf.append("\tDescription.text not equal: \n" + "\t\tthis.text = ").append(this.text).append("\n"
                    + "\t\tanother.text = ").append(another.text).append("\n");
        }

        if (!this.lang.equals(another.lang)) {
            buf.append("\tDescription.lang not equal: \n" + "\t\tthis.lang = ").append(this.lang).append("\n"
                    + "\t\tanother.lang = ").append(another.lang).append("\n");
        }

        if (this.typeNid != another.typeNid) {
            buf.append("\tDescription.typeNid not equal: \n"
                    + "\t\tthis.typeNid = ").append(this.typeNid).append("\n"
                    + "\t\tanother.typeNid = ").append(another.typeNid).append("\n");
        }

        // Compare the parents
        buf.append(super.validate(another));

        return buf.toString();
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    @Override
    public DescriptionCAB makeBlueprint(ViewCoordinate vc, 
            IdDirective idDirective, RefexDirective refexDirective) throws IOException, ContradictionException, InvalidCAB {
        DescriptionCAB descBp = new DescriptionCAB(getConceptNid(), getTypeNid(),
                LanguageCode.getLangCode(lang), getText(), initialCaseSignificant,
                getVersion(vc), Optional.of(vc), idDirective, refexDirective);
        return descBp;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public Description getPrimordialVersion() {
        return Description.this;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public Optional<DescriptionVersion> getVersion(ViewCoordinate c) throws ContradictionException {
        List<DescriptionVersion> vForC = getVersions(c);

        if (vForC.isEmpty()) {
            return Optional.empty();
        }

        if (vForC.size() > 1) {
            vForC = c.getContradictionManager().resolveVersions(vForC);
        }

        if (vForC.size() > 1) {
            throw new ContradictionException(vForC.toString());
        }

        if (!vForC.isEmpty()) {
            return Optional.of(vForC.get(0));
        }
        return null;
    }
    @Override
    public List<DescriptionVersion> getVersionList() {
        return getVersions();
    }

    @Override
    public List<DescriptionVersion> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<DescriptionVersion> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new DescriptionVersion(this, this, primordialStamp));
                for (int stampAlias : getCommitManager().getAliases(primordialStamp)) {
                    list.add(new DescriptionVersion(this, this, stampAlias));
                }
            }

            if (revisions != null) {
                for (DescriptionRevision rev : revisions) {
                    if (rev.getTime() != Long.MIN_VALUE) {
                        list.add(new DescriptionVersion(rev, this, rev.stamp));
                        for (int stampAlias : getCommitManager().getAliases(rev.stamp)) {
                            list.add(new DescriptionVersion(rev, this, stampAlias));
                        }
                    }
                }
            }

            versions = list;
        }

        return versions;
    }

    @Override
    public List<DescriptionVersion> getVersions(ViewCoordinate c) {
        List<DescriptionVersion> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(c.getAllowedStatus(), (NidSetBI) null, c.getViewPosition(),
                returnTuples, getVersions(), c.getPrecedence(),
                c.getContradictionManager());

        return returnTuples;
    }

    public List<DescriptionVersion> getVersions(EnumSet<Status> allowedStatus, NidSetBI allowedTypes,
            Position viewPosition, Precedence precedence, ContradictionManagerBI contradictionMgr) {
        List<DescriptionVersion> returnTuples = new ArrayList<>(2);

        computer.addSpecifiedVersions(allowedStatus, allowedTypes, viewPosition, returnTuples, getVersions(),
                precedence, contradictionMgr);

        return returnTuples;
    }

    @Override
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setInitialCaseSignificant(boolean initialCaseSignificant) {
        this.initialCaseSignificant = initialCaseSignificant;
        modified();
    }

    @Override
    public void setLang(String lang) {
        this.lang = lang;
        modified();
    }

    @Override
    public void setText(String text) {
        this.text = text;
        modified();
    }

    @Override
    public void setTypeNid(int typeNid) {
        this.typeNid = typeNid;
        modified();
    }

}
