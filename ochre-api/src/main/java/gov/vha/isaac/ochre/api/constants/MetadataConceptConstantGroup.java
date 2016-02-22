package gov.vha.isaac.ochre.api.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class MetadataConceptConstantGroup extends MetadataConceptConstant {

    private List<MetadataConceptConstant> children_ = new ArrayList<>();

    protected MetadataConceptConstantGroup(String fsn, String preferredSynonym, UUID uuid) {
        super(fsn, preferredSynonym, uuid);
    }

    protected MetadataConceptConstantGroup(String fsn, UUID uuid) {
        super(fsn, fsn, uuid);
    }

    protected MetadataConceptConstantGroup(String fsn, UUID uuid, String definition) {
        super(fsn, uuid, definition);
    }

    protected void addChild(MetadataConceptConstant child) {
        children_.add(child);
        child.setParent(this);
    }

    /**
     * @return The constants that should be created under this constant in the
     * taxonomy (if any). Will not return null.
     */
    public List<MetadataConceptConstant> getChildren() {
        return children_;
    }
}
