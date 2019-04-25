package sh.isaac.solor.direct.umls;

import sh.isaac.api.bootstrap.TermAux;

import java.util.UUID;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public enum Terminologies {
//    LNC,
//    SNOMEDCT_US,
//    RXNORM,
//    UCUM,
//    CVX


    LNC(TermAux.LOINC_CONCEPT_ASSEMBLAGE.getPrimordialUuid()),
    SNOMEDCT_US(TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getPrimordialUuid()),
    RXNORM(TermAux.RXNORM_CONCEPT_ASSEMBLAGE.getPrimordialUuid());

    private UUID namespace;

    Terminologies(UUID namespace) {
        this.namespace=namespace;
    }

    public UUID getNamespace() {
        return this.namespace;
    }
}
