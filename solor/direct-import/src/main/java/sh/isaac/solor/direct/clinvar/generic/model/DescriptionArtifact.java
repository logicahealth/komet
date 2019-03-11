package sh.isaac.solor.direct.clinvar.generic.model;

import sh.isaac.api.Status;
import sh.isaac.api.util.Hashcode;
import sh.isaac.solor.direct.clinvar.generic.model.fields.CoreFields;
import sh.isaac.solor.direct.clinvar.generic.model.fields.DescriptionFields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class DescriptionArtifact implements CoreFields, DescriptionFields {

    private String id;
    private Status status;
    private long time;
    private int author;
    private int module;
    private int path;
    private int concept;
    private int languageCode;
    private int type;
    private String term;
    private int caseSignificance;

    public DescriptionArtifact(String id, Status status, long time, int author, int module, int path, int concept, int languageCode, int type, String term, int caseSignificance) {
        this.id = id;
        this.status = status;
        this.time = time;
        this.author = author;
        this.module = module;
        this.path = path;
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
    public int getConcept() {
        return this.concept;
    }

    @Override
    public void setConcept(int concept) {
        this.concept = concept;
    }

    @Override
    public int getLanguageCode() {
        return this.languageCode;
    }

    @Override
    public void setLanguageCode(int languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public void setType(int type) {
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
    public int getCaseSignificance() {
        return this.caseSignificance;
    }

    @Override
    public void setCaseSignificance(int caseSignificance) {
        this.caseSignificance = caseSignificance;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }
}
