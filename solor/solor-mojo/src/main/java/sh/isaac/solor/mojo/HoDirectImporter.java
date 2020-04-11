package sh.isaac.solor.mojo;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.progress.PersistTaskResult;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.solor.direct.DirectImporter;

/**
 *
 * @author kec
 */
public class HoDirectImporter extends TimedTaskWithProgressTracker<Void>
        implements PersistTaskResult {

    public final static ConceptProxy LEGACY_HUMAN_DX_ROOT_CONCEPT = new ConceptProxy("Human Dx legacy entity", UUID.fromString("29d825d3-6536-4bb8-8ea6-844dfcb3e8f8"));
    public final static ConceptProxy HUMAN_DX_ROOT_CONCEPT = new ConceptProxy("Human Dx concept", UUID.fromString("24eb96e0-8770-405a-94e4-1eff3c1bc6e2"));

    public final static ConceptProxy HUMAN_DX_MODULE = new ConceptProxy("Human Dx module", UUID.fromString("f4904690-b9f7-489b-ab63-f649a001a074"));
    public final static ConceptProxy LEGACY_HUMAN_DX_MODULE = new ConceptProxy("Legacy Human Dx module", UUID.fromString("e2d16527-bc6c-4378-b016-75ee945bfd65"));
    public final static ConceptProxy HUMAN_DX_ASSEMBLAGES = new ConceptProxy("Human Dx assemblage", UUID.fromString("6e2f04f7-28dd-4318-8576-c5adf02511bd"));

    public final static ConceptProxy HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE = new ConceptProxy("Human Dx Solor concepts", UUID.fromString("17bea17e-f74e-4612-ab4d-7fe28389232a"));
    public final static ConceptProxy HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE = new ConceptProxy("Human Dx Solor descriptions", UUID.fromString("a4bc9f7c-0db5-4ae4-a003-4ac5e57fe385"));

    public final static ConceptProxy REFID_ASSEMBLAGE = new ConceptProxy("HDx refid", UUID.fromString("c7290eda-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy ALLERGEN_ASSEMBLAGE = new ConceptProxy("HDx Allergen", UUID.fromString("c7291164-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy IS_DIAGNOSIS_ASSEMBLAGE = new ConceptProxy("HDx Diagnosis", UUID.fromString("c729136c-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy IS_CATEGORY_ASSEMBLAGE = new ConceptProxy("HDx Category", UUID.fromString("c72915f6-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy SNOMED_MAP_ASSEMBLAGE = new ConceptProxy("HDx SNOMED ID", UUID.fromString("c7291754-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy INEXACT_SNOMED_ASSEMBLAGE = new ConceptProxy("HDx Inexact SNOMED match", UUID.fromString("c729188a-3655-11e9-b210-d663bd873d93"));
    public final static ConceptProxy INEXACT_RXCUI_ASSEMBLAGE = new ConceptProxy("HDx Inexact RXCUI match", UUID.fromString("d16bc844-e6db-45cf-8b7e-a62ae6e1da32"));
    public final static ConceptProxy SNOMED_SIB_CHILD_ASSEMBLAGE = new ConceptProxy("HDx SNOMED sibling/child", UUID.fromString("c7291a10-3655-11e9-b210-d663bd873d93"));

    public final static ConceptProxy INEXACT_RXNORM_PRODUCT = new ConceptProxy("HDx inexact RxNorm product", UUID.fromString("b98cab5f-31ae-4228-90af-556f207cab22"));
    public final static ConceptProxy INEXACT_RXNORM_ASSOCIATION_TYPE = new ConceptProxy("HDx inexact RxNorm association type", UUID.fromString("d7207ff4-b66e-4562-b4ac-d0116e9d08ff"));
    public final static ConceptProxy RXNORM_PRODUCT = new ConceptProxy("HDx RxNorm product", UUID.fromString("02e622ab-732c-4aab-af9b-b0dedfc02bfb"));

    public final static ConceptProxy HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE = new ConceptProxy("HDx Solor equivalence", UUID.fromString("11e3eea1-b918-452e-87c1-f903d5ff035f"));

    public final static ConceptProxy HDX_LEGACY_IS_A = new ConceptProxy("HDx legacy is-a", UUID.fromString("80da6f99-74c5-4eb4-8eca-f0fe9893ba01"));

    public final static ConceptProxy HDX_DEPRECATED =
            new ConceptProxy("HDx Deprecated",
                    UUID.fromString("04ef59c2-e6c0-44d0-b7f4-f884024667ce"));

    public final static ConceptProxy HDX_ICD10CM_MAP =
            new ConceptProxy("HDx ICD-10-CM inexact map",
                    UUID.fromString("5f6cca2d-4667-466a-8113-ec55d54d0f3b"));

    public final static ConceptProxy HDX_ICD10PCS_MAP =
            new ConceptProxy("HDx ICD-10-PCS inexact map",
                    UUID.fromString("794f9e7d-5a90-487b-9f93-8ebbacfd492f"));

    public final static ConceptProxy HDX_ICF_MAP =
            new ConceptProxy("HDx ICF inexact map",
                    UUID.fromString("3903d7a5-3a69-4f1b-8de1-a7c7844f2953"));

    public final static ConceptProxy HDX_ICPC_MAP =
            new ConceptProxy("HDx ICPC inexact map",
                    UUID.fromString("0653274c-f1e5-491d-b754-9eaa2be3e67f"));

    public final static ConceptProxy HDX_MDC_MAP =
            new ConceptProxy("HDx MDC inexact map",
                    UUID.fromString("3f687795-8ef7-4a98-a4b2-bb7d5d022f4d"));

    public final static ConceptProxy HDX_MESH_MAP =
            new ConceptProxy("HDx MESH inexact map",
                    UUID.fromString("70ead1cb-2f80-4f8e-a0ae-584fd254c714"));

    public final static ConceptProxy HDX_RADLEX_MAP =
            new ConceptProxy("HDx RADLEX inexact map",
                    UUID.fromString("7686650f-97e0-4447-affc-e19c9e3e0180"));

    public final static ConceptProxy HDX_RXCUI_MAP =
            new ConceptProxy("HDx RXCUI map",
                    UUID.fromString("fdd1f394-180e-4582-a1d0-e104c86f2adc"));

    public final static ConceptProxy HDX_CCS_SINGLE_ICD_MAP =
            new ConceptProxy("HDx CCS-single category icd 10 map",
                    UUID.fromString("b3d86f60-6618-47d4-ab78-abe39d9cfa97"));

    public final static ConceptProxy HDX_CCS_MULTI_1_ICD_MAP =
            new ConceptProxy("HDx CCS-multi level 1 ICD 10 map",
                    UUID.fromString("e5998d38-ea14-4717-9b29-f6db9d21f607"));

    public final static ConceptProxy HDX_CCS_MULTI_2_ICD_MAP =
            new ConceptProxy("HDx CCS-multi level 2 ICD 10 map",
                    UUID.fromString("10cd5e46-e4a0-485d-80b0-aabb74e1ff4d"));

    public final static ConceptProxy HDX_ENTITY_ASSEMBLAGE = new ConceptProxy("HDx entity",
            UUID.fromString("1c82bc0e-3160-491f-9454-a45a5ab5556d"));

    public final static ConceptProxy HDX_REVIEW_REQUIRED = new ConceptProxy("HDx review required",
            UUID.fromString("cb4191ad-5a6c-40b8-a1f8-6c3efb83a70d"));

    private static final int WRITE_PERMITS = Runtime.getRuntime()
            .availableProcessors() * 2;

    public static HashSet<String> watchTokens = new HashSet<>();

    protected final Semaphore writeSemaphore = new Semaphore(WRITE_PERMITS);
    private List<IndexBuilderService> indexers;
    private final ConcurrentHashMap<HdxConceptHash, ConceptSpecification> hdxSolorConcepts = new ConcurrentHashMap<>();
    private final TaxonomySnapshot taxonomy;

    public HoDirectImporter() {
        File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
        updateTitle("Importing HO from " + importDirectory.getAbsolutePath());
        Get.activeTasks()
                .add(this);
        this.taxonomy = Get.taxonomyService().getSnapshot(Get.coordinateFactory().createDefaultStatedManifoldCoordinate());
    }

    public ConcurrentHashMap<HdxConceptHash, ConceptSpecification> getHdxSolorConcepts() {
        return hdxSolorConcepts;
    }

    public TaxonomySnapshot getTaxonomy() {
        return taxonomy;
    }

    @Override
    protected Void call() throws Exception {
        try {
            Transaction transaction = Get.commitService().newTransaction(Optional.empty(), ChangeCheckerMode.INACTIVE);

            ConceptProxy covid19Proxy = new ConceptProxy("Disease caused by severe acute respiratory syndrome coronavirus 2 (disorder)",
                    UUID.fromString("938c3040-c6cb-3b9d-9a7e-489217d82aa9"));
            // Add extra UUID to COVID-19 for Human Dx
            Get.identifierService().addUuidForNid(UUID.fromString("8adcf6e4-233c-4f07-bf0c-d89b660fc203"), covid19Proxy.getNid());
            // Add extra idendifiers to the Human Dx root concept
            ConceptProxy humanDxConcept = new ConceptProxy("Human Dx concept (SOLOR)", UUID.fromString("24eb96e0-8770-405a-94e4-1eff3c1bc6e2"));
            Get.identifierService().addUuidForNid(UUID.fromString("f5148e63-bb5b-5426-868c-490e5e75a610"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("ac2d3dbc-bf5b-5eeb-b08c-af2148968a3c"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("3baa897b-147f-5814-beca-26ad4735328d"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("29844160-ffa3-5461-a941-0ae7c30dbca6"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("fdb09934-aefd-5792-b3aa-c2fbd044093e"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("2e38befe-b646-5926-82a9-3171778d5375"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("28b02349-2168-5a83-a2c9-4c36d996c59b"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("16dea474-0102-5da3-8a87-679685614816"), humanDxConcept.getNid());
            Get.identifierService().addUuidForNid(UUID.fromString("b47bf361-d7c8-5db6-a8ec-6ba86b69bf92"), humanDxConcept.getNid());

            this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);
            StampService stampService = Get.stampService();
            int authorNid = TermAux.USER.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            int moduleNid = HUMAN_DX_MODULE.getNid();
            int stamp = stampService.getStampSequence(Status.ACTIVE, System.currentTimeMillis(), authorNid, moduleNid, pathNid);
            int legacyStamp = stampService.getStampSequence(Status.ACTIVE, System.currentTimeMillis(), authorNid, LEGACY_HUMAN_DX_MODULE.getNid(), pathNid);

            buildConcept(transaction, HUMAN_DX_ASSEMBLAGES.getPrimordialUuid(),
                    HUMAN_DX_ASSEMBLAGES.getFullyQualifiedName(), stamp, MetaData.ASSEMBLAGE____SOLOR.getNid());

            buildConcept(transaction, INEXACT_RXNORM_ASSOCIATION_TYPE.getPrimordialUuid(),
                    INEXACT_RXNORM_ASSOCIATION_TYPE.getFullyQualifiedName(), stamp,
                    HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, INEXACT_RXNORM_PRODUCT.getPrimordialUuid(),
                    INEXACT_RXNORM_PRODUCT.getFullyQualifiedName(), stamp,
                    new ConceptProxy("Medicinal product (product)",
                            UUID.fromString("ae364969-80c6-32b9-9c1d-eaebed617d9b")).getNid());

            buildConcept(transaction, RXNORM_PRODUCT.getPrimordialUuid(),
                    RXNORM_PRODUCT.getFullyQualifiedName(), stamp,
                    new ConceptProxy("Medicinal product (product)",
                            UUID.fromString("ae364969-80c6-32b9-9c1d-eaebed617d9b")).getNid());


            buildConcept(transaction, LEGACY_HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(),
                    LEGACY_HUMAN_DX_ROOT_CONCEPT.getFullyQualifiedName(), legacyStamp, MetaData.SOLOR_CONCEPT____SOLOR.getNid());

            buildConcept(transaction, HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getPrimordialUuid(),
                    HDX_SOLOR_EQUIVALENCE_ASSEMBLAGE.getFullyQualifiedName(), legacyStamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_LEGACY_IS_A.getPrimordialUuid(),
                    HDX_LEGACY_IS_A.getFullyQualifiedName(), legacyStamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, LEGACY_HUMAN_DX_MODULE.getPrimordialUuid(),
                    LEGACY_HUMAN_DX_MODULE.getFullyQualifiedName(), legacyStamp, MetaData.MODULE____SOLOR.getNid());

            buildConcept(transaction, HUMAN_DX_ROOT_CONCEPT.getPrimordialUuid(),
                    HUMAN_DX_ROOT_CONCEPT.getFullyQualifiedName(), stamp, MetaData.SOLOR_CONCEPT____SOLOR.getNid());

            buildConcept(transaction, HUMAN_DX_MODULE.getPrimordialUuid(),
                    HUMAN_DX_MODULE.getFullyQualifiedName(), stamp, MetaData.MODULE____SOLOR.getNid());


            buildConcept(transaction, REFID_ASSEMBLAGE.getPrimordialUuid(),
                    REFID_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_DEPRECATED.getPrimordialUuid(),
                    HDX_DEPRECATED.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE.getPrimordialUuid(),
                    HUMAN_DX_SOLOR_CONCEPT_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE.getPrimordialUuid(),
                    HUMAN_DX_SOLOR_DESCRIPTION_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, ALLERGEN_ASSEMBLAGE.getPrimordialUuid(),
                    ALLERGEN_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, IS_DIAGNOSIS_ASSEMBLAGE.getPrimordialUuid(),
                    IS_DIAGNOSIS_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, IS_CATEGORY_ASSEMBLAGE.getPrimordialUuid(),
                    IS_CATEGORY_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, SNOMED_MAP_ASSEMBLAGE.getPrimordialUuid(),
                    SNOMED_MAP_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, INEXACT_SNOMED_ASSEMBLAGE.getPrimordialUuid(),
                    INEXACT_SNOMED_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, INEXACT_RXCUI_ASSEMBLAGE.getPrimordialUuid(),
                    INEXACT_RXCUI_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, SNOMED_SIB_CHILD_ASSEMBLAGE.getPrimordialUuid(),
                    SNOMED_SIB_CHILD_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_ENTITY_ASSEMBLAGE.getPrimordialUuid(),
                    HDX_ENTITY_ASSEMBLAGE.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());


            buildConcept(transaction, HDX_ICD10CM_MAP.getPrimordialUuid(),
                    HDX_ICD10CM_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_ICD10PCS_MAP.getPrimordialUuid(),
                    HDX_ICD10PCS_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_ICF_MAP.getPrimordialUuid(),
                    HDX_ICF_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_ICPC_MAP.getPrimordialUuid(),
                    HDX_ICPC_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_MDC_MAP.getPrimordialUuid(),
                    HDX_MDC_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_MESH_MAP.getPrimordialUuid(),
                    HDX_MESH_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_RADLEX_MAP.getPrimordialUuid(),
                    HDX_RADLEX_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_RXCUI_MAP.getPrimordialUuid(),
                    HDX_RXCUI_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_CCS_SINGLE_ICD_MAP.getPrimordialUuid(),
                    HDX_CCS_SINGLE_ICD_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_CCS_MULTI_1_ICD_MAP.getPrimordialUuid(),
                    HDX_CCS_MULTI_1_ICD_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_CCS_MULTI_2_ICD_MAP.getPrimordialUuid(),
                    HDX_CCS_MULTI_2_ICD_MAP.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            buildConcept(transaction, HDX_REVIEW_REQUIRED.getPrimordialUuid(),
                    HDX_REVIEW_REQUIRED.getFullyQualifiedName(), stamp, HUMAN_DX_ASSEMBLAGES.getNid());

            File importDirectory = Get.configurationService().getIBDFImportPath().toFile();
            System.out.println("Importing from: " + importDirectory.getAbsolutePath());

            int fileCount = loadDatabase(transaction, importDirectory);
            transaction.commit("Health Ontology Import");
            return null;
        } finally {
            Get.taxonomyService().notifyTaxonomyListenersToRefresh();
            Get.activeTasks()
                    .remove(this);
        }
    }

    private int loadDatabase(Transaction transaction, File contentDirectory)
            throws Exception {
        int fileCount = 0;
        List<Path> zipFiles = Files.walk(contentDirectory.toPath())
                .filter(p -> (p.toString().toLowerCase().endsWith(".zip")
                        && p.toFile().getName().toLowerCase().startsWith("ho-ho-ho")))
                .collect(Collectors.toList());
        for (Path zipFilePath : zipFiles) {
            try (ZipFile zipFile = new ZipFile(zipFilePath.toFile(), Charset.forName("UTF-8"))) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName()
                            .toLowerCase();
                    if (entryName.endsWith(".tsv") &! entryName.startsWith("__MACOSX".toLowerCase())) {
                        CSVReader reader
                                = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry),
                                Charset.forName("UTF-8"))))
                                .withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
                        fileCount++;

                        readHo(transaction, reader, entry);
                    }
                }
            }
        }
        return fileCount;
    }

    private void readHo(Transaction transaction, CSVReader reader, ZipEntry entry)
            throws IOException {
        long commitTime = System.currentTimeMillis();
        AssemblageService assemblageService = Get.assemblageService();
        final int writeSize = 102400;
        ArrayList<String[]> columnsToWrite = new ArrayList<>(writeSize);
        String[] columns;
        int deprecated = 0;

        reader.readNext();  // discard header row
        boolean empty = true;
        while ((columns = reader.readNext()) != null) {
            empty = false;
            if (columns[HoWriter.DEPRECATED].equals("True")) {
                deprecated++;
                continue;
            }

            columnsToWrite.add(columns);

            if (columnsToWrite.size() == writeSize) {
                HoWriter hoWriter = new HoWriter(transaction,
                        columnsToWrite,
                        this.writeSemaphore,
                        "Processing HO records from: " + DirectImporter.trimZipName(
                                entry.getName()),
                        commitTime, this);

                columnsToWrite = new ArrayList<>(writeSize);
                Get.executor()
                        .submit(hoWriter);
            }
        }
        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }

        if (empty) {
            LOG.warn("No data in file: " + entry.getName());
        }
        if (!columnsToWrite.isEmpty()) {
            HoWriter hoWriter = new HoWriter(transaction,
                    columnsToWrite,
                    this.writeSemaphore,
                    "Processing HO records from: " + DirectImporter.trimZipName(
                            entry.getName()),
                    commitTime, this);
            Get.executor()
                    .submit(hoWriter);
        }

        updateMessage("Waiting for HO file completion...");
        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                LOG.error("problem calling sync on index", e);
            }
        }
        LOG.info("Deprecated entities: " + deprecated);
        updateMessage("Synchronizing HO records to database...");
        assemblageService.sync();
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    protected void buildConcept(Transaction transaction, UUID conceptUuid, String conceptName, int stamp, int parentConceptNid) throws IllegalStateException, NoSuchElementException {
        LogicalExpressionBuilder eb = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();
        eb.necessarySet(eb.and(eb.conceptAssertion(parentConceptNid)));
        ConceptBuilderService builderService = Get.conceptBuilderService();
        ConceptBuilder builder = builderService.getDefaultConceptBuilder(conceptName,
                "HO",
                eb.build(),
                TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getNid());
        builder.setPrimordialUuid(conceptUuid);
        List<Chronology> builtObjects = new ArrayList<>();
        builder.build(transaction, stamp, builtObjects);
        for (Chronology chronology : builtObjects) {
            Get.identifiedObjectService().putChronologyData(chronology);
            index(chronology);
        }
    }

    private void index(Chronology chronicle) {
        if (chronicle instanceof SemanticChronology) {
            if (chronicle.getVersionType() == VersionType.LOGIC_GRAPH) {
                Get.taxonomyService().updateTaxonomy((SemanticChronology) chronicle);
            }
        }
        for (IndexBuilderService indexer : indexers) {
            indexer.indexNow(chronicle);
        }
    }

}
