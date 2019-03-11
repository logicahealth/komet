package sh.isaac.solor.direct.clinvar;

import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.util.UuidT5Generator;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ClinvarImporter {

    private final Set<String> variantConcepts = new HashSet<>();
    private final Set<String> geneConcepts = new HashSet<>();
    private final String clinvarNamespace;
    private final String variantNamespace;
    private final String geneNamespace;
    private final String relationshipNamespace;
    private final ConceptProxy clinvarModuleProxy;
    private final ConceptProxy variantIdentifierProxy;
    private final ConceptProxy geneIdentifierProxy;
    private final ConceptService conceptService;
    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private  Semaphore writeSemaphore;


    public ClinvarImporter() {

        clinvarNamespace = "gov.nih.nlm.ncbi.clinvar.";
        variantNamespace = clinvarNamespace + "variant.";
        geneNamespace = clinvarNamespace + "gene.";
        relationshipNamespace = clinvarNamespace + "rel.";

        this.clinvarModuleProxy = new ConceptProxy("Clinvar Solor Module",
                UuidT5Generator.get(this.clinvarNamespace + "Clinvar Solor Module"));
        this.variantIdentifierProxy = new ConceptProxy("Clinvar Variant Name",
                UuidT5Generator.get(this.clinvarNamespace + "Clinvar Variant Name"));
        this.geneIdentifierProxy = new ConceptProxy("NCBI Gene ID",
                UuidT5Generator.get(this.clinvarNamespace + "NCBI Gene ID"));

        this.conceptService = Get.conceptService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();

    }

    public void extract(BufferedReader bufferedReader){

    }

    public void transform(){

    }

    public void load(){

    }
}
