package sh.isaac.solor.direct.clinvar.generic.model;

import sh.isaac.solor.direct.clinvar.generic.model.fields.ConceptFields;
import sh.isaac.solor.direct.clinvar.generic.model.fields.CoreFields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class ConceptArtifact implements CoreFields, ConceptFields {

    private String id;
    private String time;
    private String status;
    private String module;
    private String definitionStatus;

    public ConceptArtifact(String id, String time, String status, String module, String definitionStatus) {
        this.id = id;
        this.time = time;
        this.status = status;
        this.module = module;
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
    public String getTime() {
        return this.time;
    }

    @Override
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getModule() {
        return this.module;
    }

    @Override
    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public String getDefinitionStatus() {
        return this.definitionStatus;
    }

    @Override
    public void setDefinitionStatus(String definitionStatus) {
        this.definitionStatus = definitionStatus;
    }
}
