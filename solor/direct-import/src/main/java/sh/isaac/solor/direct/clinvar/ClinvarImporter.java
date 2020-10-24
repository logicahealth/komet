package sh.isaac.solor.direct.clinvar;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.generic.GenericImporter;
import sh.isaac.solor.direct.generic.artifact.ConceptArtifact;
import sh.isaac.solor.direct.generic.artifact.DescriptionArtifact;
import sh.isaac.solor.direct.generic.artifact.NonDefiningTaxonomyArtifact;
import sh.isaac.solor.direct.generic.writer.GenericConceptWriter;
import sh.isaac.solor.direct.generic.writer.GenericDescriptionWriter;
import sh.isaac.solor.direct.generic.writer.GenericNonDefiningTaxonomyWriter;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019-03-07
 * aks8m - https://github.com/aks8m
 */
public class ClinvarImporter extends GenericImporter<BufferedReader> {

    private final ArrayList<ConceptArtifact> genomicConcepts = new ArrayList<>();
    private final ArrayList<DescriptionArtifact> genomicDescriptions = new ArrayList<>();
    private final ArrayList<NonDefiningTaxonomyArtifact> genomicNonDefiningTaxonomyArtifacts = new ArrayList<>();
    private final UUID variantNamespaceUUID = UuidT5Generator.get("gov.nih.nlm.ncbi.clinvar.variant.");
    private final UUID geneNamespaceUUID = UuidT5Generator.get("gov.nih.nlm.ncbi.clinvar.gene.");
    private  Semaphore writeSemaphore;
    private long time = System.currentTimeMillis();

    private final int VARIANT_ID_INDEX = 2;
    private final int GENE_ID_INDEX = 3;
    private final int GENE_SYMBOL_INDEX = 4;
    private final int PHENOTYPE_IDs_INDEX = 12;

    public ClinvarImporter(Semaphore writeSemaphore, int WRITE_PERMITS) {
        super(writeSemaphore, WRITE_PERMITS);

        this.writeSemaphore = writeSemaphore;
    }

    @Override
    public void runImport(BufferedReader data) {

        try {

            String rowString;
            data.readLine();

            while ((rowString = data.readLine()) != null) {

                String[] columns = rowString.split("\t");

                if (!columns[VARIANT_ID_INDEX].isEmpty() && !columns[VARIANT_ID_INDEX].contains(",") &&
                        !columns[GENE_ID_INDEX].isEmpty() && !columns[GENE_SYMBOL_INDEX].isEmpty() &&
                        !columns[GENE_SYMBOL_INDEX].contains(";") && !columns[GENE_SYMBOL_INDEX].contains(",") &&
                        !columns[GENE_SYMBOL_INDEX].contains(":") && !columns[GENE_ID_INDEX].equals("-1")){

                    UUID variantComponentUUID = UuidT5Generator.get(this.variantNamespaceUUID, columns[VARIANT_ID_INDEX]);
                    UUID geneComponentUUID = UuidT5Generator.get(this.geneNamespaceUUID, columns[GENE_ID_INDEX]);


                    super.checkForArtifactUniqueness (new ConceptArtifact(
                            variantComponentUUID,
                            Status.ACTIVE,
                            this.time,
                            MetaData.USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid(),
                            columns[VARIANT_ID_INDEX],
                            MetaData.CLINVAR_VARIANT_ID____SOLOR.getPrimordialUuid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()), this.genomicConcepts);

                    super.checkForArtifactUniqueness(new ConceptArtifact(
                            geneComponentUUID,
                            Status.ACTIVE,
                            this.time,
                            MetaData.USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid(),
                            columns[GENE_ID_INDEX],
                            MetaData.NCBI_GENE_ID____SOLOR.getPrimordialUuid(),
                            MetaData.NECESSARY_BUT_NOT_SUFFICIENT_CONCEPT_DEFINITION____SOLOR.getNid(),
                            MetaData.CLINVAR_DEFINITION_ASSEMBLAGE____SOLOR.getPrimordialUuid()), this.genomicConcepts);

                    super.checkForArtifactUniqueness( new DescriptionArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            variantComponentUUID,
                            "en",
                            MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                            columns[VARIANT_ID_INDEX],
                            MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid(),
                            UuidT5Generator.get(columns[VARIANT_ID_INDEX]).toString(),
                            MetaData.CLINVAR_DESCRIPTION_ID____SOLOR.getPrimordialUuid()), this.genomicDescriptions);

                   super.checkForArtifactUniqueness(new DescriptionArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            geneComponentUUID,
                            "en",
                            MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid(),
                            columns[GENE_SYMBOL_INDEX],
                            MetaData.CASE_INSENSITIVE_EVALUATION____SOLOR.getNid(),
                            UuidT5Generator.get(columns[GENE_SYMBOL_INDEX]).toString(),
                            MetaData.CLINVAR_DESCRIPTION_ID____SOLOR.getPrimordialUuid()),this.genomicDescriptions);

                   super.checkForArtifactUniqueness(new NonDefiningTaxonomyArtifact(
                            Status.ACTIVE,
                            this.time,
                            MetaData.USER____SOLOR.getNid(),
                            MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                            TermAux.DEVELOPMENT_PATH.getNid(),
                            variantComponentUUID,
                            geneComponentUUID,
                            MetaData.CLINVAR_VARIANT_TO_GENE_NON_DEFINING_TAXONOMY____SOLOR.getPrimordialUuid()),this.genomicNonDefiningTaxonomyArtifacts);

                    if(columns[PHENOTYPE_IDs_INDEX].contains("SNOMED CT:")) {
                        Matcher matcher = Pattern.compile("(SNOMED CT:\\d*)").matcher(columns[PHENOTYPE_IDs_INDEX]);
                        while (matcher.find()) {


                           super.checkForArtifactUniqueness(new NonDefiningTaxonomyArtifact(
                                    Status.ACTIVE,
                                    this.time,
                                    MetaData.USER____SOLOR.getNid(),
                                    MetaData.SOLOR_GENOMIC_MODULE____SOLOR.getNid(),
                                    TermAux.DEVELOPMENT_PATH.getNid(),
                                    geneComponentUUID,
                                    UuidT3Generator.fromSNOMED(matcher.group().split(":")[1]),
                                    MetaData.CLINVAR_GENE_TO_PHENOTYPE_NON_DEFINING_TAXONOMY____SOLOR.getPrimordialUuid()), this.genomicNonDefiningTaxonomyArtifacts);

                        }
                    }
                }
            }

            Get.executor().submit(new GenericConceptWriter(this.genomicConcepts, this.writeSemaphore));
            super.waitForAll();

            Get.executor().submit(new GenericDescriptionWriter(this.genomicDescriptions, this.writeSemaphore));
            super.waitForAll();

            Get.executor().submit(new GenericNonDefiningTaxonomyWriter(this.genomicNonDefiningTaxonomyArtifacts, this.writeSemaphore));
            super.waitForAll();

        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
    }
}
