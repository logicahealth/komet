/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.direct;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import sh.isaac.MetaData;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifierService;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.commit.StampService;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.task.TimedTaskWithProgressTracker;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.StringVersionImpl;

/**
 *
 * @author kec
 */
public class RxNormWriter extends TimedTaskWithProgressTracker<Void> {
    
/*
852877|ENG||||||9264350||318317007||SNOMEDCT_US|PT|318317007|Quinidine sulfate 200 mg oral tablet||N||
*/
private static final int RXCUI = 0; //1 RXCUI RxNorm Unique identiMer for concept (concept ID)
private static final int LAT = 1; //2 LAT Language of Term
private static final int TS = 2; //3 TS Term status (no value provided)
private static final int LUI = 3; //4 LUI Unique identiMer for term (no value provided)
private static final int STT = 4; //5 STT String type (no value provided)
private static final int SUI = 5; //6 SUI Unique identiMer for string (no value provided)
private static final int ISPREF = 6; //7 ISPREF Atom status - preferred (Y) or not (N) for this string within this concept (no value provided)
private static final int RXAUI = 7; //8 RXAUI Unique identiMer for atom (RxNorm Atom ID)
private static final int SAUI = 8; //9 SAUI Source asserted atom identiMer [optional]
private static final int SCUI = 9; //10 SCUI Source asserted concept identiMer [optional]
private static final int SDUI = 10; //11 SDUI Source asserted descriptor identiMer [optional]
private static final int SAB = 11; //12 SAB Source abbreviation
private static final int TTY = 12; //13 TTY Term type in source
private static final int CODE = 13; //14 CODE "Most useful" source asserted identifier (if the source vocabulary has more than one identifier), or a RxNorm-generated source entry identiMer (if the source vocabulary has none.)
private static final int STR = 14; //15 STR String
private static final int SRL = 15; //16 SRL Source Restriction Level (no value provided)
private static final int SUPPRESS = 16; //17 SUPPRESS Suppressible kag. Values = N, O, Y, or E. 
// N - not suppressible. 
// O - Specific individual names (atoms) set as Obsolete because the name is no longer provided by the original source. 
// Y - Suppressed by RxNorm editor. 
// E - unquantified, non-prescribable drug with related quantiMed, prescribable drugs.
// NLM strongly recommends that users not alter editor-assigned suppressibility.
private static final int CVF = 17; //18 CVF Content view kag. RxNorm includes one value, '4096', to denote inclusion in the Current Prescribable Content subset. All rows with CVF='4096' can be found in the subset.

    
    
    private final List<String[]> rxNormConSoRecords;
    private final Semaphore writeSemaphore;
    private final List<IndexBuilderService> indexers;
    private final long commitTime;
    private final IdentifierService identifierService = Get.identifierService();
    private final AssemblageService assemblageService = Get.assemblageService();

    public RxNormWriter(List<String[]> rxNormConSoRecords,
            Semaphore writeSemaphore, String message, long commitTime) {
        this.rxNormConSoRecords = rxNormConSoRecords;
        this.writeSemaphore = writeSemaphore;
        this.writeSemaphore.acquireUninterruptibly();
        this.commitTime = commitTime;
        indexers = LookupService.get().getAllServices(IndexBuilderService.class);
        updateTitle("Importing RxNorm batch of size: " + rxNormConSoRecords.size());
        updateMessage(message);
        addToTotalWork(rxNormConSoRecords.size());
        Get.activeTasks().add(this);
    }

    private void index(Chronology chronicle) {
        for (IndexBuilderService indexer : indexers) {
           indexer.indexNow(chronicle);
        }
    }

    @Override
    protected Void call() throws Exception {
        try {
            StampService stampService = Get.stampService();
            int authorNid = TermAux.USER.getNid();
            int pathNid = TermAux.DEVELOPMENT_PATH.getNid();
            int moduleNid = MetaData.SOLOR_MODULE____SOLOR.getNid();

         List<String[]> noSuchElementList = new ArrayList<>();
         int recordStamp = stampService.getStampSequence(Status.ACTIVE, commitTime, authorNid, moduleNid, pathNid);
 
         for (String[] conSoRecord : rxNormConSoRecords) {
                try {
                    
                    if (conSoRecord[SAB].toUpperCase().startsWith("SNOMEDCT")) {
                        UUID snomedUuid = UuidT3Generator.fromSNOMED(conSoRecord[CODE]);
                        int nid = identifierService.assignNid(snomedUuid);

                            // make a RxCUI semantic
                            UUID rxCuiSemanticUuid = UuidT5Generator.get(MetaData.RXNORM_CUI_ASSEMBLAGE____SOLOR.getPrimordialUuid(),
                                    conSoRecord[RXCUI]);
                            
                            SemanticChronologyImpl rxNormCuiChronicle
                                    = new SemanticChronologyImpl(VersionType.STRING, rxCuiSemanticUuid,
                                            MetaData.RXNORM_CUI_ASSEMBLAGE____SOLOR.getNid(), nid);
                            
                            StringVersionImpl stringVersion = rxNormCuiChronicle.createMutableVersion(recordStamp);
                            stringVersion.setString(conSoRecord[RXCUI]);
                            assemblageService.writeSemanticChronology(rxNormCuiChronicle);
                        
                    }
             } catch (NoSuchElementException ex) {
                 noSuchElementList.add(conSoRecord);
             }
            completedUnitOfWork();
         }
         if (!noSuchElementList.isEmpty()) {
            LOG.error("Continuing after import failed with no such element exception for record count: " + noSuchElementList.size());
         }
            return null;
        } finally {
            this.writeSemaphore.release();
            Get.activeTasks().remove(this);
        }
    }
}
