package sh.isaac.solor.direct.clinvar.model;

import sh.isaac.api.Status;
import sh.isaac.solor.direct.clinvar.model.fields.ConceptFields;
import sh.isaac.solor.direct.clinvar.model.fields.CoreFields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class ConceptArtifact implements CoreFields, ConceptFields {

    private String id;
    private Status status;
    private long time;
    private int author;
    private int module;
    private int path;
    private int definitionStatus;

    public ConceptArtifact(String id, Status status, long time, int author, int module, int path, int definitionStatus) {
        this.id = id;
        this.status = status;
        this.time = time;
        this.author = author;
        this.module = module;
        this.path = path;
        this.definitionStatus = definitionStatus;
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
    public int hashCode() {
        return this.getID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }
}
