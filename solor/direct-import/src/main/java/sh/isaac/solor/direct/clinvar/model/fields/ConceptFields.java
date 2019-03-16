package sh.isaac.solor.direct.clinvar.model.fields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface ConceptFields {

    int getDefinitionStatus();
    void setDefinitionStatus(int definitionStatus);

    UUID getIdentifierAssemblageUUID();
    void setIdentifierAssemblageUUID(UUID identifierAssemblageUUID);

    UUID getDefinitionStatusAssemblageUUID();
    void setDefinitionStatusAssemblageUUID(UUID definitionStatusAssemblageUUID);
}
