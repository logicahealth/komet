package sh.isaac.solor.direct.clinvar.generic.model;

import sh.isaac.solor.direct.clinvar.generic.model.fields.CoreFields;
import sh.isaac.solor.direct.clinvar.generic.model.fields.RelationshipFields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public final class RelationshipArtifact implements CoreFields, RelationshipFields {

    private String id;
    private String time;
    private String status;
    private String module;
    private String source;
    private String destination;
    private String relationshipGroup;
    private String type;
    private String characteristicType;
    private String modifier;

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
    public String getSource() {
        return this.source;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String getDestination() {
        return this.destination;
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String getRelationshipGroup() {
        return this.relationshipGroup ;
    }

    @Override
    public void setRelationshipGroup(String relationshipGroup) {
        this.relationshipGroup = relationshipGroup;
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
    public String getCharacteristicType() {
        return this.characteristicType;
    }

    @Override
    public void setCharacteristicType(String characteristicType) {
        this.characteristicType = characteristicType;
    }

    @Override
    public String getModifier() {
        return this.modifier;
    }

    @Override
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
