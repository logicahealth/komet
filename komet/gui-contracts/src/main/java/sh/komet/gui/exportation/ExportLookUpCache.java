package sh.komet.gui.exportation;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;

import java.util.List;
import java.util.stream.Collectors;

/*
 * aks8m - 5/20/18
 */
public class ExportLookUpCache {

    private List<Integer> sctidNids;
    private List<Integer> rxnormNids;
    private List<Integer> loincNids;

    public void generateCache(){
        sctidNids = Get.assemblageService()
                .getReferencedComponentNidStreamFromAssemblage(TermAux.SNOMED_IDENTIFIER)
                .boxed()
                .collect(Collectors.toList());
        rxnormNids = Get.assemblageService()
                .getReferencedComponentNidStreamFromAssemblage(MetaData.RXNORM_CUI_ASSEMBLAGE____SOLOR)
                .boxed()
                .collect(Collectors.toList());
        loincNids = Get.assemblageService()
                .getReferencedComponentNidStreamFromAssemblage(MetaData.CODE____SOLOR)
                .boxed()
                .collect(Collectors.toList());
    }

    public List<Integer> getSctidNids() {
        return sctidNids;
    }

    public List<Integer> getRxnormNids() {
        return rxnormNids;
    }

    public List<Integer> getLoincNids() {
        return loincNids;
    }
}
