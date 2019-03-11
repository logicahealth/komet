package sh.isaac.solor.direct.clinvar;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.component.concept.ConceptService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.clinvar.generic.model.ConceptArtifact;
import sh.isaac.solor.direct.clinvar.generic.model.DescriptionArtifact;
import sh.isaac.solor.direct.clinvar.generic.writers.ConceptWriter;
import sh.isaac.solor.direct.clinvar.generic.writers.DescriptionWriter;
import sh.isaac.solor.direct.clinvar.generic.writers.SemanticWriter;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ClinvarImporter {

    private final List<ConceptArtifact> variantConceptArtifacts = new ArrayList<>();
    private final List<ConceptArtifact> geneConceptArtifacts = new ArrayList<>();
    private final List<DescriptionArtifact> variantDescriptionArtifacts = new ArrayList<>();
    private final List<DescriptionArtifact> geneDescriptionArtifacts = new ArrayList<>();
    private final List<Integer[][]> nonDefiningTaxonomies = new ArrayList<>();
    private final String clinvarNamespace = "gov.nih.nlm.ncbi.clinvar.";
    private final UUID variantNamspaceUUID = UuidT5Generator.get(clinvarNamespace + "variant.");
    private final UUID geneNamespaceUUID = UuidT5Generator.get(clinvarNamespace + "gene.");
    private final String relationshipNamespace = clinvarNamespace + "rel.";
    private final ConceptService conceptService;
    private final AssemblageService assemblageService;
    private final IdentifierService identifierService;
    private  Semaphore writeSemaphore;
    private int WRITE_PERMITS;
    private long time = System.currentTimeMillis();

    public ClinvarImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {

        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;

        this.conceptService = Get.conceptService();
        this.assemblageService = Get.assemblageService();
        this.identifierService = Get.identifierService();

    }

    public void go(BufferedReader bufferedReader){

        final int VARIANT_ID_INDEX = 2;
        final int GENE_ID_INDEX = 3;
        final int GENE_SYMBOL_INDEX = 4;
        final int PHENOTYPE_IDs_INDEX = 12;

        try {

            String rowString;
            bufferedReader.readLine();

            HashSet<String> variantIds = new HashSet<>();
            HashSet<String> geneIds = new HashSet<>();
            HashSet<String> geneDesc = new HashSet<>();
            HashSet<String> variantToGene = new HashSet<>();
            Map<String, Set<String>> geneToSCTID = new HashMap<>();


            while ((rowString = bufferedReader.readLine()) != null) {

                String[] columns = rowString.split("\t");

                if (!columns[VARIANT_ID_INDEX].isEmpty() && !columns[GENE_ID_INDEX].isEmpty() && !columns[GENE_SYMBOL_INDEX].isEmpty()
                && !columns[GENE_SYMBOL_INDEX].contains(";") && !columns[GENE_SYMBOL_INDEX].contains(",") && !columns[GENE_SYMBOL_INDEX].contains(":")
                && !columns[GENE_ID_INDEX].equals("-1")){
                    variantIds.add(columns[VARIANT_ID_INDEX]);
                    geneIds.add(columns[GENE_ID_INDEX]);
                    geneDesc.add(columns[GENE_ID_INDEX] + "delimeter" + columns[GENE_SYMBOL_INDEX]);

                    variantToGene.add(columns[VARIANT_ID_INDEX] + "delimeter" + columns[GENE_ID_INDEX]);

                    if(columns[PHENOTYPE_IDs_INDEX].contains("SNOMED CT:")) {
                        Matcher matcher = Pattern.compile("(SNOMED CT:\\d*)").matcher(columns[PHENOTYPE_IDs_INDEX]);
                        while (matcher.find()) {

                            if(geneToSCTID.containsKey(columns[GENE_ID_INDEX])){
                                geneToSCTID.get(columns[GENE_ID_INDEX]).add(matcher.group().split(":")[1]);
                            }else{
                                Set<String> sctIDs = new HashSet<>();
                                sctIDs.add(matcher.group().split(":")[1]);
                                geneToSCTID.put(columns[GENE_ID_INDEX], sctIDs);
                            }
                        }
                    }
                }
            }

            variantIds.stream()
                    .forEach(variantID -> this.variantConceptArtifacts.add(new ConceptArtifact(
                            variantID,
                            Status.ACTIVE,
                            time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid()))
                    );

            geneIds.stream()
                    .forEach(geneID -> this.geneConceptArtifacts.add(new ConceptArtifact(
                            geneID,
                            Status.ACTIVE,
                            time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid()
                    )));

            //Concepts
            Get.executor().submit(new ConceptWriter(this.variantConceptArtifacts,
                    this.writeSemaphore, this.variantNamspaceUUID,
                    MetaData.CLINVAR_VARIANT_ID____SOLOR, MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR));
            Get.executor().submit(new ConceptWriter(this.geneConceptArtifacts,
                    this.writeSemaphore, this.geneNamespaceUUID,
                    MetaData.NCBI_GENE_ID____SOLOR, MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR));

            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            conceptService.sync();
            this.writeSemaphore.release(WRITE_PERMITS);

            variantIds.stream()
                    .forEach(variantID -> this.variantDescriptionArtifacts.add(new DescriptionArtifact(
                            UuidT5Generator.get(variantID).toString(),
                            Status.ACTIVE,
                            time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            identifierService.getNidForUuids(UuidT5Generator.get(this.variantNamspaceUUID, variantID)),
                            TermAux.ENGLISH_LANGUAGE.getNid(),
                            MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                            variantID,
                            MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid()))
                    );

            geneDesc.stream()
                    .map(s -> s.split("delimeter"))
                    .forEach(strings -> {

                        try {
                            this.geneDescriptionArtifacts.add(new DescriptionArtifact(
                                    UuidT5Generator.get(strings[1]).toString(),
                                    Status.ACTIVE,
                                    time,
                                    MetaData.CLINVAR_USER____SOLOR.getNid(),
                                    MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                                    TermAux.DEVELOPMENT_PATH.getNid(),
                                    identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, strings[0])),
                                    TermAux.ENGLISH_LANGUAGE.getNid(),
                                    MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                                    strings[1],
                                    MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid()));
                        }catch (NoSuchElementException nseE){
                            nseE.printStackTrace();
                        }
                    });

            System.out.println("break");

            //Descriptions
            Get.executor().submit(new DescriptionWriter(this.variantDescriptionArtifacts, this.writeSemaphore,
                    this.variantNamspaceUUID, MetaData.CLINVAR_DESCRIPTION_ID____SOLOR));
            Get.executor().submit(new DescriptionWriter(this.geneDescriptionArtifacts, this.writeSemaphore,
                   this.geneNamespaceUUID, MetaData.CLINVAR_DESCRIPTION_ID____SOLOR));

            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            assemblageService.sync();
            this.writeSemaphore.release(WRITE_PERMITS);

            variantToGene.stream()
                    .map(s -> s.split("delimeter"))
                    .forEach(strings ->
                        this.nonDefiningTaxonomies.add(new Integer[][]{
                                {identifierService.getNidForUuids(UuidT5Generator.get(this.variantNamspaceUUID, strings[0])),
                                        identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, strings[1]))}
                        })
                    );

            geneToSCTID.entrySet().stream()
                    .forEach(entry -> entry.getValue().stream()
                            .forEach(sctID -> {

                                if (identifierService.hasUuid(UuidT3Generator.fromSNOMED(sctID))) {

                                    this.nonDefiningTaxonomies.add(new Integer[][]{
                                            {identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, entry.getKey())),
                                                    identifierService.getNidForUuids(UuidT3Generator.fromSNOMED(sctID))}
                                    });
                                }else {
                                    System.out.println("No SCTID: " + sctID);
                                }
                            })
                    );

            Get.executor().submit(new SemanticWriter(this.nonDefiningTaxonomies, this.writeSemaphore));

            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            assemblageService.sync();
            this.writeSemaphore.release(WRITE_PERMITS);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void transform(){

        System.out.println("");



    }

    public void load(){

    }
}
