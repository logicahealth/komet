package sh.isaac.solor.direct.clinvar;

import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.clinvar.model.ConceptArtifact;
import sh.isaac.solor.direct.clinvar.model.DescriptionArtifact;
import sh.isaac.solor.direct.clinvar.writers.GenomicConceptWriter;
import sh.isaac.solor.direct.clinvar.writers.GenomicDescriptionWriter;
import sh.isaac.solor.direct.clinvar.writers.GenomicNonDefiningTaxonomyWriter;

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

    private final List<ConceptArtifact> genomicConceptArtifacts = new ArrayList<>();
    private final List<ConceptArtifact> geneConceptArtifacts = new ArrayList<>();
    private final List<DescriptionArtifact> variantDescriptionArtifacts = new ArrayList<>();
    private final List<DescriptionArtifact> geneDescriptionArtifacts = new ArrayList<>();
    private final List<Integer[][]> varToGeneNonDefiningTaxonomy = new ArrayList<>();
    private final List<Integer[][]> geneToPhenotypeNonDefiningTaxonomy = new ArrayList<>();
    private final String clinvarNamespace = "gov.nih.nlm.ncbi.clinvar.";
    private final UUID variantNamspaceUUID = UuidT5Generator.get(clinvarNamespace + "variant.");
    private final UUID geneNamespaceUUID = UuidT5Generator.get(clinvarNamespace + "gene.");
    private  Semaphore writeSemaphore;
    private int WRITE_PERMITS;
    private long time = System.currentTimeMillis();
    private final IdentifierService identifierService;

    public ClinvarImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {

        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;

        this.identifierService = Get.identifierService();
    }

    public void runImport(BufferedReader bufferedReader){

        final int VARIANT_ID_INDEX = 2;
        final int GENE_ID_INDEX = 3;
        final int GENE_SYMBOL_INDEX = 4;
        final int PHENOTYPE_IDs_INDEX = 12;

        try {

            String rowString;
            bufferedReader.readLine();

            HashMap<UUID, String[][]> variantConceptIdentifierAndDescription = new HashMap<>();
            HashMap<UUID, String[][]> geneConceptIdentifierAndDescription = new HashMap<>();
            Map<UUID, Set<UUID>> variantToGene = new HashMap<>();
            Map<UUID, Set<UUID>> geneToSCTID = new HashMap<>();


            while ((rowString = bufferedReader.readLine()) != null) {

                String[] columns = rowString.split("\t");

                if (!columns[VARIANT_ID_INDEX].isEmpty() && !columns[VARIANT_ID_INDEX].contains(",") && !columns[GENE_ID_INDEX].isEmpty() && !columns[GENE_SYMBOL_INDEX].isEmpty()
                && !columns[GENE_SYMBOL_INDEX].contains(";") && !columns[GENE_SYMBOL_INDEX].contains(",") && !columns[GENE_SYMBOL_INDEX].contains(":")
                && !columns[GENE_ID_INDEX].equals("-1")){

                    UUID variantUUID = UuidT5Generator.get(this.variantNamspaceUUID, columns[VARIANT_ID_INDEX]);
                    UUID geneUUID = UuidT5Generator.get(this.geneNamespaceUUID, columns[GENE_ID_INDEX]);

                    //Concet with Descriptions
                    if(!variantConceptIdentifierAndDescription.containsKey(variantUUID)){
                        variantConceptIdentifierAndDescription.put(variantUUID, new String[][]
                                {{columns[VARIANT_ID_INDEX]},{columns[VARIANT_ID_INDEX]}});
                    }
                    if(!geneConceptIdentifierAndDescription.containsKey(geneUUID)){
                        geneConceptIdentifierAndDescription.put(geneUUID, new String[][]
                                {{columns[GENE_ID_INDEX]},{columns[GENE_SYMBOL_INDEX]}});
                    }

                    //Non-Defining Relationships
                    if(variantToGene.containsKey(variantUUID)){
                        variantToGene.get(variantUUID).add(geneUUID);
                    } else {
                        Set<UUID> geneIdUUIDs = new HashSet<>();
                        geneIdUUIDs.add(geneUUID);
                        variantToGene.put(variantUUID, geneIdUUIDs);
                    }
                    if(columns[PHENOTYPE_IDs_INDEX].contains("SNOMED CT:")) {
                        Matcher matcher = Pattern.compile("(SNOMED CT:\\d*)").matcher(columns[PHENOTYPE_IDs_INDEX]);
                        while (matcher.find()) {

                            if(geneToSCTID.containsKey(geneUUID)){
                                geneToSCTID.get(geneUUID).add(UuidT3Generator.fromSNOMED(matcher.group().split(":")[1]));
                            }else{
                                Set<UUID> sctIdUUIDs = new HashSet<>();
                                sctIdUUIDs.add(UuidT3Generator.fromSNOMED(matcher.group().split(":")[1]));
                                geneToSCTID.put(geneUUID, sctIdUUIDs);
                            }
                        }
                    }
                }
            }

            variantConceptIdentifierAndDescription.entrySet().stream()
                    .forEach(variantEntry -> this.genomicConceptArtifacts.add(new ConceptArtifact(
                            variantEntry.getValue()[0][0],
                            variantEntry.getKey(),
                            Status.ACTIVE,
                            time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.CLINVAR_VARIANT_ID____SOLOR.getPrimordialUuid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()
                    )));

            geneConceptIdentifierAndDescription.entrySet().stream()
                    .forEach(geneEntry -> this.genomicConceptArtifacts.add(new ConceptArtifact(
                            geneEntry.getValue()[0][0],
                            geneEntry.getKey(),
                            Status.ACTIVE,
                            time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.NCBI_GENE_ID____SOLOR.getPrimordialUuid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()
                    )));

            //Write Concepts
            Get.executor().submit(new GenomicConceptWriter(this.genomicConceptArtifacts, this.writeSemaphore));

            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Get.conceptService().sync();

            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Get.assemblageService().sync();
            this.writeSemaphore.release(WRITE_PERMITS);

//            //Write Descriptions
//            variantConceptIdentifierAndDescription.entrySet().stream()
//                    .forEach(uuidStringEntry -> {
//
//                        this.variantDescriptionArtifacts.add(new DescriptionArtifact(
//
//
//
//                        ));
//
//
//
//                    });
//
//
//
//
//            variantIds.stream()
//                    .forEach(variantID ->
//                    {
//                        try {
//
//                            UUID variantConceptUUID = UuidT5Generator.get(this.variantNamspaceUUID, variantID);
//
//                            if (this.identifierService.hasUuid(variantConceptUUID)) {
//                                this.variantDescriptionArtifacts.add(new DescriptionArtifact(
//                                        UuidT5Generator.get(variantID).toString(),
//                                        Status.ACTIVE,
//                                        time,
//                                        MetaData.CLINVAR_USER____SOLOR.getNid(),
//                                        MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
//                                        TermAux.DEVELOPMENT_PATH.getNid(),
//                                        this.identifierService.getNidForUuids(variantConceptUUID),
//                                        TermAux.ENGLISH_LANGUAGE.getNid(),
//                                        MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
//                                        variantID,
//                                        MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid()));
//
//                            } else {
//                                System.out.println("Variant Concept (" + variantID + ") Not Found for Desc Attachment");
//                            }
//
//                        }catch (NoSuchElementException nseE){
//                            nseE.printStackTrace();
//                            System.out.println(", try again..." + this.identifierService.getNidForUuids(variantConceptUUID));
//                        }
//
//                    });
//
//            geneConceptIdentifierAndDescription.stream()
//                    .map(s -> s.split("delimeter"))
//                    .forEach(strings -> {
//
//                        try {
//
//                            UUID geneConceptUUID = UuidT5Generator.get(this.geneNamespaceUUID, strings[0]);
//
//                            if(this.identifierService.hasUuid(geneConceptUUID)) {
//
//                                this.geneDescriptionArtifacts.add(new DescriptionArtifact(
//                                        UuidT5Generator.get(strings[1]).toString(),
//                                        Status.ACTIVE,
//                                        time,
//                                        MetaData.CLINVAR_USER____SOLOR.getNid(),
//                                        MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
//                                        TermAux.DEVELOPMENT_PATH.getNid(),
//                                        this.identifierService.getNidForUuids(geneConceptUUID),
//                                        TermAux.ENGLISH_LANGUAGE.getNid(),
//                                        MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
//                                        strings[1],
//                                        MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid()));
//                            } else {
//                                System.out.println("Gene Concept (" + strings[0] + ") Not Found for Desc Attachment");
//                            }
//
//                        }catch (NoSuchElementException nseE){
//                            nseE.printStackTrace();
//                            System.out.println(", try again..." + this.identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, strings[0])));
//                        }
//                    });
//
//            //Descriptions
//            Get.executor().submit(new GenomicDescriptionWriter(this.variantDescriptionArtifacts, this.writeSemaphore,
//                    this.variantNamspaceUUID, MetaData.CLINVAR_DESCRIPTION_ID____SOLOR));
//            Get.executor().submit(new GenomicDescriptionWriter(this.geneDescriptionArtifacts, this.writeSemaphore,
//                   this.geneNamespaceUUID, MetaData.CLINVAR_DESCRIPTION_ID____SOLOR));
//
//            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
//            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
//                try {
//                    indexer.sync().get();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            Get.assemblageService().sync();
//            this.writeSemaphore.release(WRITE_PERMITS);
//
//            variantToGene.stream()
//                    .map(s -> s.split("delimeter"))
//                    .forEach(strings ->
//                        this.varToGeneNonDefiningTaxonomy.add(new Integer[][]{
//                                {this.identifierService.getNidForUuids(UuidT5Generator.get(this.variantNamspaceUUID, strings[0])),
//                                        this.identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, strings[1]))}
//                        })
//                    );
//
//            geneToSCTID.entrySet().stream()
//                    .forEach(entry -> entry.getValue().stream()
//                            .forEach(sctID -> {
//
//                                if (Get.identifierService().hasUuid(UuidT3Generator.fromSNOMED(sctID))) {
//
//                                    this.geneToPhenotypeNonDefiningTaxonomy.add(new Integer[][]{
//                                            {this.identifierService.getNidForUuids(UuidT5Generator.get(this.geneNamespaceUUID, entry.getKey())),
//                                                    this.identifierService.getNidForUuids(UuidT3Generator.fromSNOMED(sctID))}
//                                    });
//                                }else {
//                                    System.out.println("No SCTID: " + sctID);
//                                }
//                            })
//                    );
//
//            //Non-Defining Taxonomies
//            Get.executor().submit(new GenomicNonDefiningTaxonomyWriter(this.varToGeneNonDefiningTaxonomy, this.writeSemaphore, MetaData.CLINVAR_VARIANT_TO_GENE_NON_DEFINING_TAXONOMY____SOLOR.getNid()));
//            Get.executor().submit(new GenomicNonDefiningTaxonomyWriter(this.geneToPhenotypeNonDefiningTaxonomy, this.writeSemaphore, MetaData.CLINVAR_GENE_TO_PHENOTYPE_NON_DEFINING_TAXONOMY____SOLOR.getNid()));
//
//            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
//            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
//                try {
//                    indexer.sync().get();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            Get.assemblageService().sync();
//            this.writeSemaphore.release(WRITE_PERMITS);

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
