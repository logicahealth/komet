package sh.isaac.solor.direct.clinvar.model.fields;

import java.util.UUID;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public interface DescriptionFields {

    UUID getReferencedComponentUUID();

    int getLanguageConceptNid();

    int getDescriptionAssemblageNid();

    int getTypeNid();

    String getTerm();

    int getCaseSignificanceNid();

}
