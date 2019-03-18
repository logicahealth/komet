package sh.isaac.solor.direct.clinvar.model;

import sh.isaac.api.Status;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.clinvar.model.fields.ComponentFields;
import sh.isaac.solor.direct.clinvar.model.fields.DefinitionStatusFields;
import sh.isaac.solor.direct.clinvar.model.fields.IdentifierFields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class ConceptArtifact implements ComponentFields, IdentifierFields, DefinitionStatusFields {

    //Concept Fields
    private final UUID componentUUID;
    private final Status status;
    private final long time;
    private final int authorNid;
    private final int moduleNid;
    private final int pathNid;

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
        this.identifierValue = identifierValue;
        this.identifierAssemblageUUID = identifierAssemblageUUID;
        this.definitionStatusNid = definitionStatusNid;
        this.definitionStatusAssemblageUUID = definitionStatusAssemblageUUID;
    }

    @Override
    public UUID getComponentUUID() {
        return this.componentUUID;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public int getAuthorNid() {
        return this.authorNid;
    }

    @Override
    public int getModuleNid() {
        return this.moduleNid;
    }

    @Override
    public int getPathNid() {
        return this.pathNid;
    }

    @Override
    public UUID getIdentifierComponentUUID() {
        return UuidT5Generator.get(this.identifierAssemblageUUID, this.identifierValue);
    }

    @Override
    public String getIdentifierValue() {
        return this.identifierValue;
    }

    @Override
    public UUID getIdentifierAssemblageUUID() {
        return this.identifierAssemblageUUID;
    }

    @Override
    public UUID getDefinitionStatusComponentUUID() {
        return UuidT5Generator.get(this.definitionStatusAssemblageUUID, this.componentUUID.toString());
    }

    @Override
    public int getDefinitionStatusNid() {
        return this.definitionStatusNid;
    }

    @Override
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
