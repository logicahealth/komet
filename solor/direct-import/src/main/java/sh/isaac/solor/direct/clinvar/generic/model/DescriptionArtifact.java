package sh.isaac.solor.direct.clinvar.generic.model;

import sh.isaac.solor.direct.clinvar.generic.model.fields.CoreFields;
import sh.isaac.solor.direct.clinvar.generic.model.fields.DescriptionFields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class DescriptionArtifact implements CoreFields, DescriptionFields {

    private String id;
    private String time;
    private String status;
    private String module;
    private String concept;
    private String languageCode;
    private String type;
    private String term;
    private String caseSignificance;

    public DescriptionArtifact(String id, String time, String status, String module, String concept, String languageCode, String type, String term, String caseSignificance) {
        this.id = id;
        this.time = time;
        this.status = status;
        this.module = module;
        this.concept = concept;
        this.languageCode = languageCode;
        this.type = type;
        this.term = term;
        this.caseSignificance = caseSignificance;
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
    public String getConcept() {
        return this.concept;
    }

    @Override
    public void setConcept(String concept) {
        this.concept = concept;
    }

    @Override
    public String getLanguageCode() {
        return this.languageCode;
    }

    @Override
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getTerm() {
        return this.term;
    }

    @Override
    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public String getCaseSignificance() {
        return this.caseSignificance;
    }

    @Override
    public void setCaseSignificance(String caseSignificance) {
        this.caseSignificance = caseSignificance;
    }
}
