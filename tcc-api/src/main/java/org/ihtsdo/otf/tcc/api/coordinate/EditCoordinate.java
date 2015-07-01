package org.ihtsdo.otf.tcc.api.coordinate;

import gov.vha.isaac.ochre.api.Get;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.ihtsdo.otf.tcc.api.nid.NidSet;
import org.ihtsdo.otf.tcc.api.nid.NidSetBI;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

@XmlRootElement(name = "edit-coordinate")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EditCoordinate implements gov.vha.isaac.ochre.api.coordinate.EditCoordinate {
    
    private int authorNid;
    private int moduleNid;
    private NidSetBI editPaths = new NidSet();

    public EditCoordinate() {
    }

    public EditCoordinate(gov.vha.isaac.ochre.api.coordinate.EditCoordinate another) {
        this.authorNid = Get.identifierService().getConceptNid(another.getAuthorSequence());
        this.moduleNid = Get.identifierService().getConceptNid(another.getModuleSequence());
        this.editPaths.add(Get.identifierService().getConceptNid(another.getPathSequence()));
    }

    public EditCoordinate(int authorNid, int moduleNid, NidSetBI editPaths) {
        super();
        assert editPaths != null;
        assert authorNid != Integer.MIN_VALUE;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.editPaths = editPaths;
    }

    public EditCoordinate(int authorNid, int moduleNid, int... editPathNids) {
        super();
        assert editPathNids != null;
        assert authorNid != Integer.MIN_VALUE;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.editPaths = new NidSet(editPathNids);
    }

    public ConceptSpec getAuthorSpec() throws IOException {
        return new ConceptSpec(this.authorNid);
    }

    public void setAuthorSpec(ConceptSpec spec) throws IOException {
        this.authorNid = spec.getNid();
    }

    public ConceptSpec getModuleSpec() throws IOException {
        return new ConceptSpec(this.moduleNid);
    }

    public void setModuleSpec(ConceptSpec spec) throws IOException {
        this.moduleNid = spec.getNid();
    }

    public List<ConceptSpec> getEditPathListSpecs() throws IOException {
        List<ConceptSpec> editPathSpecs = new ArrayList<>(editPaths.size());
        for (int pathNid: editPaths.getSetValues()) {
            editPathSpecs.add(new ConceptSpec(pathNid));
        }
        return editPathSpecs;
    }

    public void setEditPathListSpecs(List<ConceptSpec> specs) throws IOException {
        this.editPaths.clear();
        specs.stream().forEach((spec) -> {
            this.editPaths.add(spec.getNid());
        });
    }


    public int getAuthorNid() {
        return authorNid;
    }

    public int getModuleNid() {
        return moduleNid;
    }

    public NidSetBI getEditPaths() {
        return editPaths;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("authorNid: ").append(authorNid);
        sb.append("moduleNid: ").append(moduleNid);
        sb.append("editPaths: ").append(editPaths);
        return sb.toString();
    }

    @Override
    public int getAuthorSequence() {
        return Get.identifierService().getConceptSequence(authorNid);
    }

    @Override
    public int getModuleSequence() {
       return Get.identifierService().getConceptSequence(moduleNid);
    }

    @Override
    public int getPathSequence() {
        return Get.identifierService().getConceptSequence(editPaths.getMin());
    }
}
