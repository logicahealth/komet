package sh.isaac.solor.direct.livd.writer;

import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.version.DynamicImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 2019-05-19
 * aks8m - https://github.com/aks8m
 */
public class LIVDAssemblageWriter extends TimedTaskWithProgressTracker<Void> {

    /**
     * Per the LIVD execel specification https://ivdconnectivity.org/livd/
     * The 6th row of the second worksheet of the entire workbook is where the data we need starts :)
     *
     * The below breakdown of the LIVD specificaiton fields will be stored as a Dynamic Assemblage mostly of Strings
     * TODO: Be more specific with the field types for the LIVD Assemblage/Semantic
     *
     * Publication Version ID       - String
     * Manufacturer                 - String
     * Model                        - String
     * Equipment UID                - String
     * Equipment UID Type           - String
     * Vendor Analyte Code          - String
     * Vendor Analyte Name          - String
     * Vendor Specimen Description  - String
     * Vendor Result Description    - String
     * Vendor Reference ID          - String
     * Vendor Comment               - String
     * LOINC Code                   - Component (aka the referencedComponentNID)
     * LOINC long Name              - String
     * Component                    - String
     * Property                     - String
     * Time                         - String
     * System                       - String
     * Scale                        - String
     * Method                       - String
     *
     * Effectively this would be an RF2 refset of type sssssssssssssssssss....uhhh :)
     */


    private final List<String[]> valuesToWrite;
    private final Semaphore writeSemaphore;
    private final int batchSize = 10000;
    private final List<IndexBuilderService> indexers;
    private final StampService stampService = Get.stampService();
    private final long time = System.currentTimeMillis();
    private int versionStamp;
    private final AssemblageService assemblageService = Get.assemblageService();

    public LIVDAssemblageWriter(List<String[]> valuesToWrite, Semaphore writeSemaphore) {
        this.valuesToWrite = valuesToWrite;
        this.writeSemaphore = writeSemaphore;

        this.indexers = LookupService.get().getAllServices(IndexBuilderService.class);

        this.writeSemaphore.acquireUninterruptibly();
        updateTitle("Importing LIVD batch of size: " + this.valuesToWrite.size());
        updateMessage("Solorizing LIVD Data");
        addToTotalWork(this.valuesToWrite.size() / this.batchSize);
        Get.activeTasks().add(this);
    }

    @Override
    protected Void call() throws Exception {

        final AtomicInteger batchCount = new AtomicInteger(0);

        try {

            this.versionStamp = stampService.getStampSequence(
                    Status.ACTIVE, time,
                    MetaData.LIVD_USER____SOLOR.getNid(),
                    MetaData.SOLOR_LIVD_MODULE____SOLOR.getNid(),
                    TermAux.DEVELOPMENT_PATH.getNid());

            ArrayList<DynamicColumnInfo> dynamicColumnInfos = new ArrayList<>();
            dynamicColumnInfos.add(new DynamicColumnInfo(0, MetaData.LIVD_PUBLICATION_VERSION_ID____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(1, MetaData.LIVD_MANUFACTURER____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(2, MetaData.LIVD_MODEL____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(3, MetaData.LIVD_EQUIPMENT_UID____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(4, MetaData.LIVD_EQUIPMENT_UID_TYPE____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(5, MetaData.LIVD_VENDOR_ANALYTE_CODE____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(6, MetaData.LIVD_VENDOR_ANALYTE_NAME____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(7, MetaData.LIVD_VENDOR_SPECIMEN_DESCRIPTION____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(8, MetaData.LIVD_VENDOR_RESULT_DESCRIPTION____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(9, MetaData.LIVD_VENDOR_REFERENCE_ID____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));
            dynamicColumnInfos.add(new DynamicColumnInfo(10, MetaData.LIVD_VENDOR_COMMENT____SOLOR.getPrimordialUuid(),
                    DynamicDataType.STRING, null, false, true));


            int[] assemblageStamps = Get.concept(MetaData.LIVD_ASSEMBLAGE____SOLOR).getVersionStampSequences();
            Arrays.sort(assemblageStamps);
            int stampSequence = assemblageStamps[assemblageStamps.length - 1];  //use the largest (newest) stamp on the concept,

            List<Chronology> items = LookupService.getService(DynamicUtility.class).configureConceptAsDynamicSemantic(MetaData.LIVD_ASSEMBLAGE____SOLOR.getNid(),
                    "DynamicDefinition for LIVD Assemblage",
                    dynamicColumnInfos.toArray(new DynamicColumnInfo[dynamicColumnInfos.size()]),
                    null, null, stampSequence);

            for (Chronology c : items)
            {
                index(c);
                assemblageService.writeSemanticChronology((SemanticChronology)c);
            }


            this.valuesToWrite.stream()
                    .forEach(valueArray -> {

                        batchCount.incrementAndGet();

                        int referencedComponentNid;
                        DynamicData[] data = new DynamicData[11];

                        UUID elementUuid = UuidT5Generator.get(MetaData.LIVD_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
                                valueArray[0] + valueArray[1] + valueArray[2] + valueArray[3] + valueArray[4] +
                                        valueArray[5] + valueArray[6] + valueArray[7] + valueArray[8] + valueArray[9] + valueArray[10] +
                                        valueArray[11] + valueArray[12] + valueArray[13] + valueArray[14] + valueArray[15] + valueArray[16] +
                                        valueArray[17] + valueArray[18]);

                        data[0] = new DynamicStringImpl(valueArray[0]);
                        data[1] = new DynamicStringImpl(valueArray[1]);
                        data[2] = new DynamicStringImpl(valueArray[2]);
                        data[3] = new DynamicStringImpl(valueArray[3]);
                        data[4] = new DynamicStringImpl(valueArray[4]);
                        data[5] = new DynamicStringImpl(valueArray[5]);
                        data[6] = new DynamicStringImpl(valueArray[6]);
                        data[7] = new DynamicStringImpl(valueArray[7]);
                        data[8] = new DynamicStringImpl(valueArray[8]);
                        data[9] = new DynamicStringImpl(valueArray[9]);
                        data[10] = new DynamicStringImpl(valueArray[10]);


                        if (Get.identifierService().hasUuid(UuidT5Generator.loincConceptUuid(valueArray[11]))) {
                            referencedComponentNid = Get.identifierService().getNidForUuids(UuidT5Generator.loincConceptUuid(valueArray[11]));
                        } else {
                            referencedComponentNid = TermAux.PHENOMENON.getNid();
                            LOG.info("LOINC Code " + valueArray[11] + " not found!");
                        }

                        SemanticChronologyImpl refsetMemberToWrite = new SemanticChronologyImpl(VersionType.DYNAMIC,
                                elementUuid, MetaData.LIVD_ASSEMBLAGE____SOLOR.getNid(), referencedComponentNid);

                        DynamicImpl dv = refsetMemberToWrite.createMutableVersion(this.versionStamp);
                        dv.setData(data);
                        index(refsetMemberToWrite);
                        assemblageService.writeSemanticChronology(refsetMemberToWrite);

                        if (batchCount.get() % this.batchSize == 0)
                            completedUnitOfWork();
                    });
        }finally {

            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }

        return null;
    }

    private void index(Chronology chronicle)
    {
        for (IndexBuilderService indexer : indexers)
        {
            indexer.indexNow(chronicle);
        }
    }
}
