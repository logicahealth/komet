package sh.isaac.solor.direct.generic.artifact;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;

import java.util.UUID;

/**
 * 2019-06-04
 * aks8m - https://github.com/aks8m
 */
public final class IdentifierArtifact implements GenericArtifact {

    //Component Fields
    private final UUID componentUUID;
    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

    //Identifier Fields
    private final UUID referencedComponentUUID;
    private final String identifierValue;
    private final UUID identifierAssemblageUUID;

    public IdentifierArtifact(UUID componentUUID, Status status, long time, int authorNid, int moduleNid, int pathNid, UUID referencedComponentUUID, String identifierValue, UUID identifierAssemblageUUID) {
        this.componentUUID = componentUUID;
        this.status = status;
        this.time = time;
        this.authorNid = authorNid;
        this.moduleNid = moduleNid;
        this.pathNid = pathNid;
        this.referencedComponentUUID = referencedComponentUUID;
        this.identifierValue = identifierValue;
        this.identifierAssemblageUUID = identifierAssemblageUUID;
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

    public UUID getReferencedComponentUUID() {
        return referencedComponentUUID;
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

    @Override
    public int hashCode() {
        return this.getComponentUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof IdentifierArtifact){
            return this.getComponentUUID().equals(((IdentifierArtifact) obj).getComponentUUID());
        }else {
            return false;
        }
    }
}
