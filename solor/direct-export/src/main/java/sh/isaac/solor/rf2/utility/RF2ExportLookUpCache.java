package sh.isaac.solor.rf2.utility;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;

import java.util.List;
import java.util.stream.Collectors;

/*
 * aks8m - 5/20/18
 */
public class RF2ExportLookUpCache {

    private static List<Integer> sctidNids = Get.assemblageService()
            .getReferencedComponentNidStreamFromAssemblage(TermAux.SNOMED_IDENTIFIER)
            .boxed()
            .collect(Collectors.toList());
    private static List<Integer> rxnormNids = Get.assemblageService()
            .getReferencedComponentNidStreamFromAssemblage(MetaData.RXNORM_CUI____SOLOR)
            .boxed()
            .collect(Collectors.toList());
    private static List<Integer> loincNids = Get.assemblageService()
            .getReferencedComponentNidStreamFromAssemblage(MetaData.LOINC_ID_ASSEMBLAGE____SOLOR)
            .boxed()
            .collect(Collectors.toList());

    public static boolean isSCTID(Chronology chronology){
        return sctidNids.contains(chronology.getNid());
    }

    public static boolean isRxNorm(Chronology chronology){
        return rxnormNids.contains(chronology.getNid());
    }

    public static boolean isLoinc(Chronology chronology){
        return loincNids.contains(chronology.getNid());
    }
}
