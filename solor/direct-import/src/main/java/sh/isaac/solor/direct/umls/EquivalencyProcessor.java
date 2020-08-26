package sh.isaac.solor.direct.umls;

import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.solor.direct.umls.apis.ApiClient;
import sh.isaac.solor.direct.umls.apis.umls.UmlsApiClient;
import sh.isaac.solor.direct.umls.model.TerminologyCode;
import sh.isaac.solor.direct.umls.writers.EquivalencyWriter;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * 4/10/2019
 *
 * @author kmaulden
 */
public class EquivalencyProcessor {

    private Semaphore writeSemaphore;
    private int WRITE_PERMITS;
    private long time = System.currentTimeMillis();
    private final IdentifierService identifierService;

    private List<Terminologies> targetTerminologies = Arrays.asList(Terminologies.SNOMEDCT_US);
    private Terminologies sourceTerminology = Terminologies.LNC;
    private final String username = "cholanr";
    private final String password = "Eminem9!";

    private Map<Integer, Integer> nidSet = new HashMap<>();

    public EquivalencyProcessor(Semaphore writeSemaphore, int WRITE_PERMITS) {
        this.writeSemaphore = writeSemaphore;
        this.WRITE_PERMITS = WRITE_PERMITS;
        this.identifierService = Get.identifierService();
    }

    //TODO how and where is find equivalencies called? Same place runImport is called for genomics
    //TODO how do we get the list of input codes?
    public void findEquivalencies(List<String> inputCodes) {
        try {

            //call api and get values
            ApiClient apiClient = new UmlsApiClient(sourceTerminology, targetTerminologies, username, password);

            for (String inputCode : inputCodes) {
                List<TerminologyCode> targetCodes = apiClient.getTargetCodes(inputCode);

                int sourceNid = getTerminologyConceptNid(inputCode,this.sourceTerminology);
                int targetNid;

                for (TerminologyCode targetCode : targetCodes) {

                    targetNid = getTerminologyConceptNid(inputCode,targetCode.getRootSourceEnum());

                    this.nidSet.put(sourceNid, targetNid);

//                    this.equivalencyArtifacts.add(new EquivalencyArtifact(
//                            Status.ACTIVE,
//                            this.time,
//                            MetaData.UMLS_USER____SOLOR.getNid(),
//                            MetaData.SOLOR_UMLS_MODULE____SOLOR.getNid(),
//                            MetaData.DEVELOPMENT_PATH____SOLOR.getNid(),
//                            UuidT5Generator.get(sourceTerminology.getNamespace(), inputCode),
//                            UuidT5Generator.get(targetCode.getRootSourceEnum().getNamespace(), targetCode.getUi()),
//                            MetaData.UMLS_EQUIVALENCY_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
//                            ""));

                }


            }

            //write all equivalency artifacts in set
            writeEquivalencyArtifacts();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeEquivalencyArtifacts() {

        Get.executor().submit(new EquivalencyWriter(this.nidSet, this.writeSemaphore));

        this.writeSemaphore.acquireUninterruptibly(WRITE_PERMITS);
        for (IndexBuilderService indexer : LookupService.get().getAllServices(IndexBuilderService.class)) {
            try {
                indexer.sync().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.writeSemaphore.release(WRITE_PERMITS);
    }

    private int getTerminologyConceptNid(String sourceCode, Terminologies terminology) {
        int returnedNid = 0;
        UUID sourceUUID;

        //TODO have to deal with the case if UUID is not the concepts primordial UUID ->
        switch (terminology) {
            case SNOMEDCT_US:
                sourceUUID = UuidT3Generator.fromSNOMED(sourceCode);
                if (this.identifierService.hasUuid(sourceUUID)) {
                    returnedNid = this.identifierService.getNidForUuids(sourceUUID);
                }
                break;
            default:
                sourceUUID = UuidT5Generator.get(terminology.getNamespace(), sourceCode);
                if (this.identifierService.hasUuid(sourceUUID)) {
                    returnedNid = this.identifierService.getNidForUuids(sourceUUID);
                }
                break;
        }

        return returnedNid;
    }


}
