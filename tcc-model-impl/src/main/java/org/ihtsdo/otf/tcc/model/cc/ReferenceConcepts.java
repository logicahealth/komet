package org.ihtsdo.otf.tcc.model.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

//TODO this needs to be moved out so that this module is independent of terminologies
public enum ReferenceConcepts {

    REFSET_PATHS(TermAux.PATH_REFSET.getUuids()),
    PATH(TermAux.PATH.getUuids()),
    REFSET_PATH_ORIGINS(TermAux.PATH_ORIGIN_REFSET.getUuids()),
    TERM_AUXILIARY_PATH(TermAux.WB_AUX_PATH.getUuids()),

    SNOROCKET(TermAux.IHTSDO_CLASSIFIER.getUuids()),

    FULLY_SPECIFIED_RF2(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()),

    PREFERRED_ACCEPTABILITY_RF2(SnomedMetadataRf2.PREFERRED_RF2.getUuids()),
    SYNONYM_RF2(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
    private int nid;
    private List<UUID> uuids;

    private ReferenceConcepts(Collection<UUID> uuids) {
        try {
            this.uuids = new ArrayList<>(uuids);
            this.nid = PersistentStore.get().getNidForUuids(uuids);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ReferenceConcepts(UUID[] uuids) {
        try {
            this.uuids = Arrays.asList(uuids);
            this.nid = PersistentStore.get().getNidForUuids(this.uuids);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getNid() {
        return nid;
    }

    public List<UUID> getUuids() {
        return uuids;
    }
    
    public static void reset() {
        for (ReferenceConcepts c: ReferenceConcepts.values()) {
            try {
                c.nid = PersistentStore.get().getNidForUuids(c.uuids);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
