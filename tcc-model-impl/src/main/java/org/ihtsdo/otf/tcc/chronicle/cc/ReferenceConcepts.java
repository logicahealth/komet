package org.ihtsdo.otf.tcc.chronicle.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf1;
import org.ihtsdo.otf.tcc.api.metadata.binding.SnomedMetadataRf2;
import org.ihtsdo.otf.tcc.api.metadata.binding.TermAux;

public enum ReferenceConcepts {

    REFSET_PATHS(TermAux.PATH_REFSET.getUuids()),
    PATH(TermAux.PATH.getUuids()),
    REFSET_PATH_ORIGINS(TermAux.PATH_ORIGIN_REFSET.getUuids()),
    TERM_AUXILIARY_PATH(TermAux.WB_AUX_PATH.getUuids()),
    /**
     * @deprecated use SnomedMetadataRfx.getSTATUS_CURRENT_NID()
     */
    CURRENT(SnomedMetadataRf1.CURRENT_RF1.getUuids()),
    /**
     * @deprecated use SnomedMetadataRfx.getSTATUS_RETIRED_NID()
     */
    RETIRED(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getUuids()),
    SNOROCKET(TermAux.IHTSDO_CLASSIFIER.getUuids()),
    PREFERRED_RF1(SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getUuids()),
    FULLY_SPECIFIED_RF1(SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUuids()),
    FULLY_SPECIFIED_RF2(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()),
    PREFERRED_ACCEPTABILITY_RF1(SnomedMetadataRf1.PREFERRED_ACCEPTABILITY_RF1.getUuids()),
    PREFERRED_ACCEPTABILITY_RF2(SnomedMetadataRf2.PREFERRED_RF2.getUuids()),
    ACCEPTABLE_ACCEPTABILITY(SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getUuids()),
    SYNONYM_RF1(SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getUuids()),
    SYNONYM_RF2(SnomedMetadataRf2.SYNONYM_RF2.getUuids());
    private int nid;
    private List<UUID> uuids;

    private ReferenceConcepts(Collection<UUID> uuids) {
        try {
            this.uuids = new ArrayList<>(uuids);
            this.nid = P.s.getNidForUuids(uuids);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ReferenceConcepts(UUID[] uuids) {
        try {
            this.uuids = Arrays.asList(uuids);
            this.nid = P.s.getNidForUuids(this.uuids);
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
                c.nid = P.s.getNidForUuids(c.uuids);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
