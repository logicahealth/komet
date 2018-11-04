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
package sh.isaac.integration.tests.postgres;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.commit.CommitService;
import sh.isaac.api.externalizable.BinaryDataReaderService;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.memory.HeapUseTicker;
import sh.isaac.api.progress.ActiveTasksTicker;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.provider.postgres.PostgresProvider;

/**
 *
 * @author kec
 */
public final class PostgresMain {
    private static final Logger LOG = LogManager.getLogger();

    PostgresProvider postgresProvider;
    
    public PostgresMain() throws Exception {
        this.postgresProvider = new PostgresProvider();
    }

    protected void shutdown() {
        postgresProvider.shutdown();
        System.out.print(":SHUTDOWN: PostgresProvider.main()\n");
        System.out.print(":::::SHUTDOWN:::::SHUTDOWN:::::SHUTDOWN::::\n");
    }

    protected void startup() throws Exception {
        postgresProvider.startup();
        
        wipSetup();
        wipLoad();
        wipTeardown();
    }
    public void wipTeardown() throws Exception {
        LOG.info("WIP teardown");
        LookupService.shutdownSystem();
        ActiveTasksTicker.stop();
        HeapUseTicker.stop();
    }


    // **** :WIP:BEGIN: **** :WIP:BEGIN: **** :WIP:BEGIN: **** :WIP:BEGIN: ****
    // :WIP: "Work In Progress" can be useful for standalone development
    // :WIP: section can be removed when no longer needed.
    // :WIP: main() used for standalone Netbeans execution & debugging. 
    // :WIP: not used in production.  not used with other modules.
    public static void main(String[] args) {
        try {
            System.out.print(":::::STARTUP:::::STARTUP:::::STARTUP:::::\n");
            System.out.print(":STARTUP:01: PostgresProvider.main()\n");
            PostgresMain main = new PostgresMain();
            main.startup();
            main.shutdown();

        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }



    public void wipSetup() throws Exception {
        LOG.info("WIP Setup");
        RecursiveDelete.delete(new File("target/_datastore"));
        Get.configurationService().setDataStoreFolderPath(new File("target/_datastore").toPath());
        LookupService.startupPreferenceProvider();

        LOG.info("termstore folder path exists: " + Get.configurationService().getDataStoreFolderPath().toFile().exists());

        LookupService.startupIsaac();
        ActiveTasksTicker.start(10);
        HeapUseTicker.start(10);

        // Allocate 
    }

    public void wipLoad() {
        LOG.info("WIP load");

        int descriptionAssemblageNid = TermAux.DESCRIPTION_ASSEMBLAGE.getNid();
        int chroniclePropertiesNid = MetaData.CHRONICLE_PROPERTIES____SOLOR.getNid();
        int statedAssemblageNid = MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid();

        try {

            final Path path = Paths.get("src", "main", "resources", "IsaacMetadataAuxiliary.ibdf");
            //final Path path = Paths.get("target", "classes", "IsaacMetadataAuxiliary.ibdf");
            LOG.info("path =" + path.toString());
            final BinaryDataReaderService reader = Get.binaryDataReader(path);
            final CommitService commitService = Get.commitService();

            //this.importStats = new IsaacExternalizableStatsTestFilter();
            reader.getStream()
                //.filter(this.importStats)
                .forEach((object) -> {
                    System.out.println(":WIP: " + object.getClass());
                    // sh.isaac.model.concept.ConceptChronologyImpl
                    // sh.isaac.model.semantic.SemanticChronologyImpl
                    if (object instanceof SemanticChronologyImpl) { // SemanticChronology
                        SemanticChronologyImpl sci = (SemanticChronologyImpl) object;
                        // putUuidAll();
                        postgresProvider.putChronologyData(sci);

                        if (sci.getReferencedComponentNid() == chroniclePropertiesNid || sci.getReferencedComponentNid() == descriptionAssemblageNid) {
                            if (sci.getAssemblageNid() == statedAssemblageNid) {
                                try {
                                    LOG.info("Found watch def: " + sci);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                    throw e;
                                }
                            }
                        }
                    } else if (object instanceof ConceptChronologyImpl) { // ConceptChronology
                        ConceptChronologyImpl cci = (ConceptChronologyImpl) object;
                        postgresProvider.putChronologyData(cci);
                    }
                    commitService.importNoChecks(object);
                });
            Get.startIndexTask().get();
            commitService.postProcessImportNoChecks();
            //LOG.info("Loaded components: " + this.importStats);
            LOG.info(
                "\n     CONCEPT count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.CONCEPT).count()
                + "\n    SEMANTIC count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.SEMANTIC).count()
                + "\n       STAMP count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.STAMP).count()
                + "\n STAMP_ALIAS count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.STAMP_ALIAS).count()
                + "\nSTAMP_COMMENT count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.STAMP_COMMENT).count()
                + "\n      UNKNOWN count: " + Get.identifierService().getNidStreamOfType(IsaacObjectType.UNKNOWN).count());

        } catch (final FileNotFoundException e) {
            LOG.error("testingLoad error: File not found", e);
        } catch (InterruptedException ex) {
            LOG.error("testingLoad error: Interrpted", ex);
        } catch (ExecutionException ex) {
            LOG.error("testingLoad error: Execution exception", ex);
        }
    }
    
}
