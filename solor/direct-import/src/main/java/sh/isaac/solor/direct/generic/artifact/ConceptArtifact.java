package sh.isaac.solor.direct.generic.artifact;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class ConceptArtifact implements GenericArtifact {

    //Component Fields
    private final UUID componentUUID;
    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

    //Concept Fields
    private final int conceptAssemblageNid;

    //Identifier Fields
    private final String identifierValue;
    private final UUID identifierAssemblageUUID;

    //Definition Status Fields
    private final int definitionStatusNid;
    private final UUID definitionStatusAssemblageUUID;

    public ConceptArtifact(
            UUID componentUUID,
            Status status,
            long time,
            int authorNid,
            int moduleNid,
            int pathNid,
            int conceptAssemblageNid,
            String identifierValue,
            UUID identifierAssemblageUUID,
            int definitionStatusNid,
            UUID definitionStatusAssemblageUUID) {

        this.componentUUID = componentUUID;
        this.status = status;
        this.time = time;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.conceptAssemblageNid = conceptAssemblageNid;
        this.identifierValue = identifierValue;
        this.identifierAssemblageUUID = identifierAssemblageUUID;
        this.definitionStatusNid = definitionStatusNid;
        this.definitionStatusAssemblageUUID = definitionStatusAssemblageUUID;
    }

    @Override
    public UUID getComponentUUID() {
        return this.componentUUID;
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

    public int getConceptAssemblageNid() {
        return conceptAssemblageNid;
    }

    public UUID getIdentifierComponentUUID() {
        return UuidT5Generator.get(this.identifierAssemblageUUID, this.identifierValue);
    }

    public String getIdentifierValue() {
        return this.identifierValue;
    }

    public UUID getIdentifierAssemblageUUID() {
        return this.identifierAssemblageUUID;
    }

    public UUID getDefinitionStatusComponentUUID() {
        return UuidT5Generator.get(this.definitionStatusAssemblageUUID, this.componentUUID.toString());
    }

    public int getDefinitionStatusNid() {
        return this.definitionStatusNid;
    }

    public UUID getDefinitionStatusAssemblageUUID() {
        return this.definitionStatusAssemblageUUID;
    }

    @Override
    public int hashCode() {
        return this.getComponentUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof ConceptArtifact){
            return this.getComponentUUID().equals(((ConceptArtifact) obj).getComponentUUID());
        }else {
            return false;
        }
    }
}
