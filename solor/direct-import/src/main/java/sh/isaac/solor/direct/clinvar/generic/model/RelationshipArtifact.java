package sh.isaac.solor.direct.clinvar.generic.model;

import sh.isaac.api.Status;
import sh.isaac.solor.direct.clinvar.generic.model.fields.CoreFields;
import sh.isaac.solor.direct.clinvar.generic.model.fields.RelationshipFields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class RelationshipArtifact implements CoreFields, RelationshipFields {

    private String id;
    private Status status;
    private long time;
    private int author;
    private int module;
    private int path;
    private int source;
    private int destination;
    private int relationshipGroup;
    private int type;
    private int characteristicType;
    private int modifier;

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
    public int getSource() {
        return this.source;
    }

    @Override
    public void setSource(int source) {
        this.source = source;
    }

    @Override
    public int getDestination() {
        return this.destination;
    }

    @Override
    public void setDestination(int destination) {
        this.destination = destination;
    }

    @Override
    public int getRelationshipGroup() {
        return this.relationshipGroup;
    }

    @Override
    public void setRelationshipGroup(int relationshipGroup) {
        this.relationshipGroup = relationshipGroup;
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
    public int getCharacteristicType() {
        return this.characteristicType;
    }

    @Override
    public void setCharacteristicType(int characteristicType) {
        this.characteristicType = characteristicType;
    }

    @Override
    public int getModifier() {
        return this.modifier;
    }

    @Override
    public void setModifier(int modifier) {
        this.modifier = modifier;
    }
}
