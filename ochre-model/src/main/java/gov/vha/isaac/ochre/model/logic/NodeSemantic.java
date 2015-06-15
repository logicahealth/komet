package gov.vha.isaac.ochre.model.logic;

import gov.vha.isaac.ochre.util.UuidT5Generator;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by kec on 12/6/14.
 */
public enum NodeSemantic {
    NECESSARY_SET(),
    SUFFICIENT_SET(),

    AND(),
    OR(),
    DISJOINT_WITH(),
    DEFINITION_ROOT(),

    ROLE_ALL(),
    ROLE_SOME(),

    CONCEPT(),

    FEATURE(),

    LITERAL_BOOLEAN(),
    LITERAL_FLOAT(),
    LITERAL_INSTANT(),
    LITERAL_INTEGER(),
    LITERAL_STRING(),


    TEMPLATE(),
    SUBSTITUTION_CONCEPT(),
    SUBSTITUTION_BOOLEAN(),
    SUBSTITUTION_FLOAT(),
    SUBSTITUTION_INSTANT(),
    SUBSTITUTION_INTEGER(),
    SUBSTITUTION_STRING()
    ;
    
    UUID semanticUuid;

    private NodeSemantic() {
        try {
            this.semanticUuid = UuidT5Generator.get(UUID.fromString("8a834ec8-028d-11e5-a322-1697f925ec7b"),
                    this.getClass().getName() + this.name());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } 
    }
    
    

    public UUID getSemanticUuid() {
        return semanticUuid;
    }
}
