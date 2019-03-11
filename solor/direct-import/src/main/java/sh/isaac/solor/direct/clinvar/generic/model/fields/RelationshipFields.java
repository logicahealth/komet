package sh.isaac.solor.direct.clinvar.generic.model.fields;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface RelationshipFields {

    String getSource();
    void setSource(String source);

    String getDestination();
    void setDestination(String destination);

    String getRelationshipGroup();
    void setRelationshipGroup(String relationshipGroup);

    String getType();
    void setType(String type);

    String getCharacteristicType();
    void setCharacteristicType(String characteristicType);

    String getModifier();
    void setModifier(String modifier);
}
