package sh.isaac.solor.direct.clinvar.model;

import sh.isaac.api.Status;
import sh.isaac.solor.direct.clinvar.model.fields.ConceptFields;
import sh.isaac.solor.direct.clinvar.model.fields.CoreFields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class ConceptArtifact implements CoreFields, ConceptFields {

    private String id;
    private UUID uuid;
    private Status status;
    private long time;
    private int author;
    private int module;
    private int path;
    private int definitionStatus;
    private UUID identifierAssemblageUUID;
    private UUID definitionStatusAssemblageUUID;

    public ConceptArtifact(String id, UUID uuid, Status status, long time, int author, int module, int path, int definitionStatus, UUID identifierAssemblageUUID, UUID definitionStatusAssemblageUUID) {
        this.id = id;
        this.uuid = uuid;
        this.status = status;
        this.time = time;
        this.author = author;
        this.module = module;
        this.path = path;
        this.definitionStatus = definitionStatus;
        this.identifierAssemblageUUID = identifierAssemblageUUID;
        this.definitionStatusAssemblageUUID = definitionStatusAssemblageUUID;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int getAuthor() {
        return this.author;
    }

    @Override
    public void setAuthor(int author) {
        this.author = author;
    }

    @Override
    public int getModule() {
        return this.module;
    }

    @Override
    public void setModule(int module) {
        this.module = module;
    }

    @Override
    public int getPath() {
        return this.path;
    }

    @Override
    public void setPath(int path) {
        this.path = path;
    }

    @Override
    public int getDefinitionStatus() {
        return this.definitionStatus;
    }

    @Override
    public void setDefinitionStatus(int definitionStatus) {
        this.definitionStatus = definitionStatus;
    }

    @Override
    public UUID getIdentifierAssemblageUUID() {
        return this.identifierAssemblageUUID;
    }

    @Override
    public void setIdentifierAssemblageUUID(UUID identifierAssemblageUUID) {
        this.identifierAssemblageUUID = identifierAssemblageUUID;
    }

    @Override
    public UUID getDefinitionStatusAssemblageUUID() {
        return this.definitionStatusAssemblageUUID;
    }

    @Override
    public void setDefinitionStatusAssemblageUUID(UUID definitionStatusAssemblageUUID) {
        this.definitionStatusAssemblageUUID = definitionStatusAssemblageUUID;
    }

    @Override
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }
}
