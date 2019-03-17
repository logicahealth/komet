package sh.isaac.solor.direct.clinvar;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.clinvar.model.ConceptArtifact;
import sh.isaac.solor.direct.clinvar.model.DescriptionArtifact;
import sh.isaac.solor.direct.clinvar.model.NonDefiningTaxonomyArtifact;
import sh.isaac.solor.direct.clinvar.writers.GenomicConceptWriter;
import sh.isaac.solor.direct.clinvar.writers.GenomicDescriptionWriter;
import sh.isaac.solor.direct.clinvar.writers.GenomicNonDefiningTaxonomyWriter;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ClinvarImporter {

    private final Set<ConceptArtifact> genomicConcepts = new HashSet<>();
    private final Set<DescriptionArtifact> genomicDescriptions = new HashSet<>();
    private final Set<NonDefiningTaxonomyArtifact> genomicNonDefiningTaxonomyArtifacts = new HashSet<>();
    private final UUID variantNamespaceUUID = UuidT5Generator.get("gov.nih.nlm.ncbi.clinvar.variant.");
    private final UUID geneNamespaceUUID = UuidT5Generator.get("gov.nih.nlm.ncbi.clinvar.gene.");
    private  Semaphore writeSemaphore;
    private int WRITE_PERMITS;
    private long time = System.currentTimeMillis();
    private final IdentifierService identifierService;

    private final int VARIANT_ID_INDEX = 2;
    private final int GENE_ID_INDEX = 3;
    private final int GENE_SYMBOL_INDEX = 4;
    private final int PHENOTYPE_IDs_INDEX = 12;

    public ClinvarImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {

        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;

        this.identifierService = Get.identifierService();
    }

    public void runImport(BufferedReader bufferedReader){

        try {

            String rowString;
            bufferedReader.readLine();

            while ((rowString = bufferedReader.readLine()) != null) {

                String[] columns = rowString.split("\t");

                if (!columns[VARIANT_ID_INDEX].isEmpty() && !columns[VARIANT_ID_INDEX].contains(",") &&
                        !columns[GENE_ID_INDEX].isEmpty() && !columns[GENE_SYMBOL_INDEX].isEmpty() &&
                        !columns[GENE_SYMBOL_INDEX].contains(";") && !columns[GENE_SYMBOL_INDEX].contains(",") &&
                        !columns[GENE_SYMBOL_INDEX].contains(":") && !columns[GENE_ID_INDEX].equals("-1")){

                    UUID variantComponentUUID = UuidT5Generator.get(this.variantNamespaceUUID, columns[VARIANT_ID_INDEX]);
                    UUID geneComponentUUID = UuidT5Generator.get(this.geneNamespaceUUID, columns[GENE_ID_INDEX]);

                    this.genomicConcepts.add(new ConceptArtifact(
                            variantComponentUUID,
                            Status.ACTIVE,
                            this.time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            columns[VARIANT_ID_INDEX],
                            MetaData.CLINVAR_VARIANT_ID____SOLOR.getPrimordialUuid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()));

                    this.genomicConcepts.add(new ConceptArtifact(
                            geneComponentUUID,
                            Status.ACTIVE,
                            this.time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            columns[GENE_ID_INDEX],
                            MetaData.NCBI_GENE_ID____SOLOR.getPrimordialUuid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()));

                    this.genomicDescriptions.add(new DescriptionArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            variantComponentUUID,
                            "en",
                            MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                            columns[VARIANT_ID_INDEX],
                            MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid(),
                            UuidT5Generator.get(columns[VARIANT_ID_INDEX]).toString(),
                            MetaData.CLINVAR_DESCRIPTION_ID____SOLOR.getPrimordialUuid()));

                    this.genomicDescriptions.add(new DescriptionArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            geneComponentUUID,
                            "en",
                            MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                            columns[GENE_SYMBOL_INDEX],
                            MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid(),
                            UuidT5Generator.get(columns[GENE_SYMBOL_INDEX]).toString(),
                            MetaData.CLINVAR_DESCRIPTION_ID____SOLOR.getPrimordialUuid()));

                    this.genomicNonDefiningTaxonomyArtifacts.add(new NonDefiningTaxonomyArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.CLINVAR_USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            variantComponentUUID,
                            geneComponentUUID,
                            MetaData.CLINVAR_VARIANT_TO_GENE_NON_DEFINING_TAXONOMY____SOLOR.getPrimordialUuid()));

                    if(columns[PHENOTYPE_IDs_INDEX].contains("SNOMED CT:")) {
                        Matcher matcher = Pattern.compile("(SNOMED CT:\\d*)").matcher(columns[PHENOTYPE_IDs_INDEX]);
                        while (matcher.find()) {

                            this.genomicNonDefiningTaxonomyArtifacts.add(new NonDefiningTaxonomyArtifact(
                                    Status.ACTIVE,
                                    this.time,
                                    MetaData.CLINVAR_USER____SOLOR.getNid(),
                                    MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                                    TermAux.DEVELOPMENT_PATH.getNid(),
                                    geneComponentUUID,
                                    UuidT3Generator.fromSNOMED(matcher.group().split(":")[1]),
                                    MetaData.CLINVAR_GENE_TO_PHENOTYPE_NON_DEFINING_TAXONOMY____SOLOR.getPrimordialUuid()));
                        }
                    }
                }
            }

            writeGenomicConcepts();
            writeGenomicDescriptions();
            writeNonDefiningRelationships();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void writeGenomicConcepts(){

        Get.executor().submit(new GenomicConceptWriter(this.genomicConcepts, this.writeSemaphore));

        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Get.conceptService().sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void writeGenomicDescriptions(){

            Get.executor().submit(new GenomicDescriptionWriter(this.genomicDescriptions, this.writeSemaphore));

            this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
            for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
                try {
                    indexer.sync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Get.assemblageService().sync();
            this.writeSemaphore.release(WRITE_PERMITS);
    }

    private void writeNonDefiningRelationships(){

        Get.executor().submit(new GenomicNonDefiningTaxonomyWriter(this.genomicNonDefiningTaxonomyArtifacts, this.writeSemaphore));

        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Get.assemblageService().sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }
}
