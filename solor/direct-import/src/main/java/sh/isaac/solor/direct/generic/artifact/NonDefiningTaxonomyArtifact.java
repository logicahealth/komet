package sh.isaac.solor.direct.generic.artifact;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;

import java.util.UUID;

/**
 * 2019-03-16
 * aks8m - https://github.com/aks8m
 */
public final class NonDefiningTaxonomyArtifact implements GenericArtifact{

    //Concept Fields
    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

    //Nid Semantic Fields
    private final UUID referencedComponentUUID;
    private final UUID semanticComponentUUID;
    private final UUID nidSemanticAssemblageUUID;

    public NonDefiningTaxonomyArtifact(
            Status status,
            long time,
            int authorNid,
            int moduleNid,
            int pathNid,
            UUID referencedComponentUUID,
            UUID semanticComponentUUID,
            UUID nidSemanticAssemblageUUID) {

        this.status = status;
        this.time = time;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.referencedComponentUUID = referencedComponentUUID;
        this.semanticComponentUUID = semanticComponentUUID;
        this.nidSemanticAssemblageUUID = nidSemanticAssemblageUUID;
    }

    @Override
    public UUID getComponentUUID() {
        return UuidT5Generator.get(nidSemanticAssemblageUUID,
                this.referencedComponentUUID.toString() + this.semanticComponentUUID.toString());
    }

    public Status getStatus() {
        return this.status;
    }

    public long getTime() {
        return this.time;
    }

    public int getAuthorNid() {
        return this.authorNid;
    }

    public int getModuleNid() {
        return this.moduleNid;
    }

    public int getPathNid() {
        return this.pathNid;
    }

    public UUID getReferencedComponent() {
        return this.referencedComponentUUID;
    }

    public UUID getSemanticComponentUUID() {
        return this.semanticComponentUUID;
    }

    public UUID getNidSemanticAssemblageUUID() {
        return this.nidSemanticAssemblageUUID;
    }

    @Override
    public int hashCode() {
        return this.getComponentUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof NonDefiningTaxonomyArtifact){
            return this.getComponentUUID().equals(((NonDefiningTaxonomyArtifact) obj).getComponentUUID());
        }else {
            return false;
        }
    }
}
