package sh.isaac.solor.direct.clinvar.generic.model.fields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface RelationshipFields {

    int getSource();
    void setSource(int source);

    int getDestination();
    void setDestination(int destination);

    int getRelationshipGroup();
    void setRelationshipGroup(int relationshipGroup);

    int getType();
    void setType(int type);

    int getCharacteristicType();
    void setCharacteristicType(int characteristicType);

    int getModifier();
    void setModifier(int modifier);
}
