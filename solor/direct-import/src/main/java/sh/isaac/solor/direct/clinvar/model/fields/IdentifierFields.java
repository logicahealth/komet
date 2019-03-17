package sh.isaac.solor.direct.clinvar.model.fields;

import java.util.UUID;

/**
 * 2019-03-16
 * aks8m - https://github.com/aks8m
 */
public interface IdentifierFields {

    UUID getIdentifierComponentUUID();

    String getIdentifierValue();

    UUID getIdentifierAssemblageUUID();

}
