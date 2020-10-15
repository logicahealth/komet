/*
 * Copyright 2018 ISAAC's KOMET Collaborators.
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
package sh.komet.fx.stage;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.A;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.*;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticBuilder;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.api.logic.LogicServiceElk;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.sync.MergeFailOption;
import sh.isaac.api.sync.MergeFailure;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidFactory;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.api.util.time.DateTimeUtil;
import sh.isaac.komet.gui.exporter.ExportView;
import sh.isaac.model.concept.ConceptChronologyImpl;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ComponentNidVersionImpl;
import sh.isaac.model.semantic.version.DescriptionVersionImpl;
import sh.isaac.provider.sync.git.SyncServiceGIT;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;
import sh.komet.fx.stage.spreadsheet.IndustryImporter;
import sh.komet.fx.stage.spreadsheet.OccupationImporter;
import sh.komet.fx.stage.spreadsheet.SocImporter2010;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.menu.MenuItemWithText;
import sh.komet.gui.util.FxGet;

import jakarta.inject.Singleton;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class KometBaseMenus implements MenuProvider {

    private static final Logger LOG = LogManager.getLogger();

    private static void setupGit(ActionEvent event) {
        SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
        ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
        Path dataPath = configurationService.getDataStoreFolderPath();
        File changeSetFolder = new File(dataPath.toFile(), "changesets");
        syncService.setRootLocation(changeSetFolder);
        Optional<RemoteServiceInfo> gitConfigOptional = Get.configurationService().getGlobalDatastoreConfiguration().getGitConfiguration();
        gitConfigOptional.ifPresent((t) -> {
            try {
                syncService.linkAndFetchFromRemote(t.getURL(), t.getUsername(), t.getPassword());
            } catch (IllegalArgumentException | IOException ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        });
    }

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.FILE, AppMenu.TOOLS);
    }
/*
 */
    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window, WindowPreferences windowPreference) {
        switch (parentMenu) {
            case FILE: {
                MenuItem selectiveImport = new MenuItemWithText("Selective import and transform");
                selectiveImport.setUserData(windowPreference);
                selectiveImport.setOnAction((ActionEvent event) -> {
                    ImportView.show(FxGet.newDefaultViewProperties());
                });

                MenuItem selectiveExport = new MenuItemWithText("Selective export");
                selectiveExport.setUserData(windowPreference);
                selectiveExport.setOnAction(event -> ExportView.show(FxGet.newDefaultViewProperties()));


                Menu synchronize = new Menu("Synchronize");

                MenuItem initializeLocal = new MenuItemWithText("Initialize local");
                initializeLocal.setUserData(windowPreference);
                synchronize.getItems().add(initializeLocal);
                initializeLocal.setOnAction((ActionEvent event) -> {
                    //
                    SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                    ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
                    Path dataPath = configurationService.getDataStoreFolderPath();
                    File changeSetFolder = new File(dataPath.toFile(), "changesets");
                    syncService.setRootLocation(changeSetFolder);
                    StringBuilder gitIgnoreContent = new StringBuilder();
                    gitIgnoreContent.append("lastUser.txt").append(System.lineSeparator());
                    gitIgnoreContent.append("*.json").append(System.lineSeparator());
                    syncService.setGitIgnoreContent(gitIgnoreContent.toString());
                    if (!syncService.isRootLocationConfiguredForSCM()) {
                        try {
                            LOG.info("Initializing for git: " + changeSetFolder);
                            syncService.initializeLocalRepository();
                        } catch (IOException ex) {
                            LOG.error(ex.getLocalizedMessage(), ex);
                        }
                    }

                    LOG.info("Sync folder: " + changeSetFolder + " configured: " + syncService.isRootLocationConfiguredForSCM());

                });

                MenuItem initializeFromRemote = new MenuItemWithText("Initialize from remote...");
                initializeFromRemote.setUserData(windowPreference);
                synchronize.getItems().add(initializeFromRemote);
                initializeFromRemote.setOnAction(KometBaseMenus::setupGit);

                MenuItem pullFromRemote = new MenuItemWithText("Pull...");
                pullFromRemote.setUserData(windowPreference);
                synchronize.getItems().add(pullFromRemote);
                pullFromRemote.setOnAction((event) -> {
                    SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                    ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
                    Path dataPath = configurationService.getDataStoreFolderPath();
                    File changeSetFolder = new File(dataPath.toFile(), "changesets");
                    syncService.setRootLocation(changeSetFolder);
                    Optional<RemoteServiceInfo> gitConfigOptional = Get.configurationService().getGlobalDatastoreConfiguration().getGitConfiguration();
                    gitConfigOptional.ifPresent((t) -> {
                        try {
                            syncService.updateFromRemote(t.getUsername(), t.getPassword(), MergeFailOption.KEEP_LOCAL);
                        } catch (IllegalArgumentException | IOException | MergeFailure ex) {
                            LOG.error(ex.getLocalizedMessage(), ex);
                        }
                    });
                });

                MenuItem pushToRemote = new MenuItemWithText("Push...");
                pushToRemote.setUserData(windowPreference);
                synchronize.getItems().add(pushToRemote);
                pushToRemote.setOnAction((event) -> {
                    SyncServiceGIT syncService = Get.service(SyncServiceGIT.class);
                    ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
                    Path dataPath = configurationService.getDataStoreFolderPath();
                    File changeSetFolder = new File(dataPath.toFile(), "changesets");
                    syncService.setRootLocation(changeSetFolder);
                    Optional<RemoteServiceInfo> gitConfigOptional = Get.configurationService().getGlobalDatastoreConfiguration().getGitConfiguration();
                    gitConfigOptional.ifPresent((t) -> {

                        try {
                            syncService.updateCommitAndPush("User push", t.getUsername(), t.getPassword(), MergeFailOption.KEEP_LOCAL, (String[]) null);
                        } catch (IllegalArgumentException | IOException | MergeFailure ex) {
                            LOG.error(ex.getLocalizedMessage(), ex);
                        }
                    });
                });

                MenuItem exportNative = new MenuItemWithText("Native format export to file...");
                exportNative.setUserData(windowPreference);
                exportNative.setOnAction(this::exportNative);

                MenuItem importNative = new MenuItemWithText("Native format file to CSV...");
                importNative.setUserData(windowPreference);
                importNative.setOnAction(this::importNative);

                MenuItem splitChangeSet = new MenuItemWithText("Split change set...");
                splitChangeSet.setUserData(windowPreference);
                splitChangeSet.setOnAction(this::splitChangeSet);

                MenuItem executeFlwor = new MenuItemWithText("Execute FLWOR...");
                executeFlwor.setUserData(windowPreference);
                executeFlwor.setOnAction(this::executeFlwor);

                MenuItem executeSctOwl = new MenuItemWithText("SimpleExtensionFunction SNOMED OWL");
                executeSctOwl.setUserData(windowPreference);
                executeSctOwl.setOnAction(this::executeSctOwl);

                MenuItem executeRxNormOwl = new MenuItemWithText("SimpleExtensionFunction RxNorm OWL");
                executeRxNormOwl.setUserData(windowPreference);
                executeRxNormOwl.setOnAction(this::executeRxNormOwl);

                return new MenuItem[]{selectiveImport, selectiveExport,
                    synchronize, exportNative, importNative, splitChangeSet, executeFlwor,
                        executeSctOwl, executeRxNormOwl
                };
            }

            case TOOLS: {

                MenuItem showCommitTimes = new MenuItemWithText("Show commit times");
                showCommitTimes.setUserData(windowPreference);
                showCommitTimes.setOnAction((ActionEvent event) -> {
                    ImmutableLongList timesInUse = Get.stampService().getTimesInUse();
                    StringBuilder builder = new StringBuilder();
                    timesInUse.forEach(time -> {
                        builder.append(DateTimeUtil.format(time)).append("\n");
                    });
                    FxGet.dialogs().showInformationDialog("Commit times (" + timesInUse.size() + ")", builder.toString());
                });

                MenuItem showActiveAuthors = new MenuItemWithText("Show active authors");
                showActiveAuthors.setUserData(windowPreference);
                showActiveAuthors.setOnAction((ActionEvent event) -> {
                    ImmutableIntSet authorsInUse = Get.stampService().getAuthorsInUse();
                    ImmutableList<String> authorNames = windowPreference.getViewPropertiesForWindow().getManifoldCoordinate().getPreferredDescriptionTextList(authorsInUse.toArray());
                    StringBuilder builder = new StringBuilder();
                    authorNames.forEach(moduleName -> builder.append(moduleName).append("\n"));
                    FxGet.dialogs().showInformationDialog("Active authors (" + authorNames.size() + ")", builder.toString());
                });

                MenuItem showActivePaths = new MenuItemWithText("Show active paths");
                showActivePaths.setUserData(windowPreference);
                showActivePaths.setOnAction((ActionEvent event) -> {
                    ImmutableIntSet pathsInUse = Get.stampService().getPathsInUse();
                    ImmutableList<String> pathNames = windowPreference.getViewPropertiesForWindow().getManifoldCoordinate().getPreferredDescriptionTextList(pathsInUse.toArray());
                    StringBuilder builder = new StringBuilder();
                    pathNames.forEach(moduleName -> builder.append(moduleName).append("\n"));

                    FxGet.dialogs().showInformationDialog("Active paths(" + pathsInUse.size() + ")", builder.toString());
                });

                MenuItem showActiveModules = new MenuItemWithText("Show active modules");
                showActiveModules.setUserData(windowPreference);
                showActiveModules.setOnAction((ActionEvent event) -> {
                    ImmutableIntSet moduleNids = Get.stampService().getModulesInUse();
                    ImmutableList<String> moduleNames = windowPreference.getViewPropertiesForWindow().getManifoldCoordinate().getPreferredDescriptionTextList(moduleNids.toArray());
                    StringBuilder builder = new StringBuilder();
                    moduleNames.forEach(moduleName -> builder.append(moduleName).append("\n"));

                    FxGet.dialogs().showInformationDialog("Active modules(" + moduleNames.size() + ")", builder.toString());
                });


                MenuItem transformModuleDependencies = new MenuItemWithText("Transform Module dependencies (SCT) to Dependency management (Solor)");
                transformModuleDependencies.setUserData(windowPreference);
                transformModuleDependencies.setOnAction((ActionEvent event) -> {
                    Get.executor().submit(new DependencyManagementCollector(windowPreference.getViewPropertiesForWindow().getManifoldCoordinate()));
                });

                MenuItem transformSourcesFull = new MenuItemWithText("Transform RF2 to EL++ - FULL");
                transformSourcesFull.setUserData(windowPreference);
                transformSourcesFull.setOnAction((ActionEvent event) -> {
                    Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(null, ImportType.FULL);
                    Get.executor().submit(transformer);
                });

                MenuItem transformSourcesActiveOnly = new MenuItem("Transform RF2 to EL++ - SNAPSHOT ACTIVE ONLY");
                transformSourcesActiveOnly.setUserData(windowPreference);
                transformSourcesActiveOnly.setOnAction((ActionEvent event) -> {
                    Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(null, ImportType.SNAPSHOT_ACTIVE_ONLY);
                    Get.executor().submit(transformer);
                });

                MenuItem testGAE = new MenuItemWithText("SimpleExtensionFunction GAE");
                testGAE.setUserData(windowPreference);
                testGAE.setOnAction(this::testGAE);

                MenuItem completeClassify = new MenuItemWithText("Complete classify");
                completeClassify.setUserData(windowPreference);
                completeClassify.setOnAction((ActionEvent event) -> {
                    //TODO change how we get the edit coordinate. 
                    EditCoordinate editCoordinate = Get.coordinateFactory().createDefaultUserSolorOverlayEditCoordinate();
                    ClassifierService classifierService = Get.logicService().getClassifierService(windowPreference.getViewPropertiesForWindow().getManifoldCoordinate().toManifoldCoordinateImmutable());
                    classifierService.classify();
                });

                MenuItem completeReindex = new MenuItemWithText("Complete reindex");
                completeReindex.setUserData(windowPreference);
                completeReindex.setOnAction((ActionEvent event) -> {
                    Get.startIndexTask();
                });

                MenuItem recomputeTaxonomy = new MenuItemWithText("Recompute taxonomy");
                recomputeTaxonomy.setUserData(windowPreference);
                recomputeTaxonomy.setOnAction((ActionEvent event) -> {
                    Get.taxonomyService().notifyTaxonomyListenersToRefresh();
                });


                MenuItem loadIndustry = new MenuItemWithText("Load Industry");
                loadIndustry.setUserData(windowPreference);
                loadIndustry.setOnAction((ActionEvent event) -> {
                    IndustryImporter importer = new IndustryImporter();
                    Get.workExecutors().getExecutor().execute(importer);
                });

                MenuItem loadSOC = new MenuItemWithText("Load SOC");
                loadSOC.setUserData(windowPreference);
                loadSOC.setOnAction((ActionEvent event) -> {
                    SocImporter2010 importer = new SocImporter2010();
                    Get.workExecutors().getExecutor().execute(importer);
                });

                MenuItem loadOccupation = new MenuItemWithText("Load Occupation");
                loadOccupation.setUserData(windowPreference);
                loadOccupation.setOnAction((ActionEvent event) -> {
                    OccupationImporter importer = new OccupationImporter();
                    Get.workExecutors().getExecutor().execute(importer);
                });

                MenuItem testElk = new MenuItemWithText("Test elk");
                testElk.setUserData(windowPreference);
                testElk.setOnAction((ActionEvent event) -> {
                    LogicServiceElk logicServiceElk = Get.service(LogicServiceElk.class);
                    if (logicServiceElk != null) {
                        Get.workExecutors().getExecutor().execute(() -> {
                            ClassifierService elkClassifier = logicServiceElk.getClassifierService(windowPreference.getViewPropertiesForWindow().getManifoldCoordinate().toManifoldCoordinateImmutable());
                            elkClassifier.classify();
                            LOG.info("Classify complete...");
                        });
                    }
                });

                return new MenuItem[]{
                        showActiveModules, showActivePaths, showActiveAuthors, showCommitTimes,
                        completeClassify, completeReindex, recomputeTaxonomy,
                    transformSourcesFull, transformSourcesActiveOnly, testGAE,
                        transformModuleDependencies, testElk, loadOccupation, loadIndustry, loadSOC
                };
            }
        }

        return new MenuItem[]{};
    }


    private void testGAE(ActionEvent actionEvent) {
        ConceptProxy gaeProxy = new ConceptProxy("Granulomatous amebic encephalitis (disorder)", UUID.fromString("8202f7c4-8390-3c72-96fa-a34d3d21c032"));
        List<SemanticChronology> semanticChronologies = Get.assemblageService().getSemanticChronologiesForComponentFromAssemblage(gaeProxy.getNid(), MetaData.EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE____SOLOR.getNid());
        for (SemanticChronology semanticChronology: semanticChronologies) {
            Get.taxonomyService().updateTaxonomy(semanticChronology);
        }
    }

    private void executeSctOwl(ActionEvent actionEvent) {
        // refset id = 733073007
        // 9a119252-b2da-3e62-8767-706558be8e4b
        try {
            File sctOwlFile = new File("/Users/kec/solor-source-artifact-transformer/solor-terminology-sources/", "SnomedCT_InternationalRF2_PRODUCTION_20190731T120000Z.zip");
            try (ZipFile zipFile = new ZipFile(sctOwlFile, Charset.forName("UTF-8"))) {
                zipFile.stream()
                        .filter(entry -> !entry.getName().contains("__MACOSX") && !entry.getName().contains("._") && !entry.getName().contains(".DS_Store"))
                        .forEach((ZipEntry zipEntry) -> {
                            if (zipEntry.getName().equals("SnomedCT_InternationalRF2_PRODUCTION_20190731T120000Z/Full/Terminology/sct2_sRefset_OWLExpressionFull_INT_20190731.txt")) {
                                LOG.info("SCT Entry: " + zipEntry.getName());
                                try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {

                                    final int ID = 0;
                                    final int EFFECTIVE_TIME = 1;
                                    final int ACTIVE = 2;
                                    final int MODULE_ID = 3;
                                    final int REFSET_ID = 4;
                                    final int REFERENCED_COMPONENT_ID = 5;
                                    final int OWL_EXPRESSION = 6;
                                    String line = br.readLine();
                                    // First line is header
                                    // id	effectiveTime	active	moduleId	refsetId	referencedComponentId	owlExpression
                                    while((line = br.readLine()) != null) {
                                        String[] fields = line.split("\t");
                                        LOG.info(fields[OWL_EXPRESSION]);
                                        //LogicalExpression expression = SctOwlUtilities.sctToLogicalExpression(fields[REFERENCED_COMPONENT_ID], new BufferedReader(new StringReader(fields[OWL_EXPRESSION])));
                                        //LOG.info(expression);
                                    }

                                } catch (IOException ex) {
                                    FxGet.dialogs().showErrorDialog(ex);
                                }
                            }
                        });
            }
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }
    private void executeRxNormOwl(ActionEvent actionEvent) {
        try {
            File rxNormOwlFile = new File("/Users/kec/solor-source-artifact-transformer/solor-terminology-sources/", "RxNorm-defined-with-SNCT-classes-20190719.zip");
            try (ZipFile zipFile = new ZipFile(rxNormOwlFile, Charset.forName("UTF-8"))) {
                zipFile.stream()
                        .filter(entry -> !entry.getName().contains("__MACOSX") && !entry.getName().contains("._") && !entry.getName().contains(".DS_Store"))
                        .forEach((ZipEntry zipEntry) -> {
                            if (zipEntry.getName().equals("RxNorm-defined-with-SNCT-classes-20190719.owl")) {
                                LOG.info("RxNorm Entry: " + zipEntry.getName());
                                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {

                                } catch (IOException ex) {
                                    FxGet.dialogs().showErrorDialog(ex);
                                }

                            }
                        });
            }
        } catch (IOException e) {
            FxGet.dialogs().showErrorDialog(e);
        }
    }

    private void exportNative(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify zip file to export into");
        fileChooser.setInitialFileName("native-export.zip");
        File zipFile = fileChooser.showSaveDialog(null);
        if (zipFile != null) {
            NativeExport export = new NativeExport(zipFile);
            Get.executor().submit(export);
        }

    }

    private void importNative(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify zip file to import from");
        fileChooser.setInitialFileName("native-export.zip");
        File zipFile = fileChooser.showOpenDialog(null);
        if (zipFile != null) {
            NativeImport importFile = new NativeImport(zipFile);
            Get.executor().submit(importFile);
        }
    }

    private void splitChangeSet(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Specify change set to split");
        fileChooser.setInitialFileName("changeset.ibdf");
        File zipFile = fileChooser.showOpenDialog(null);
        if (zipFile != null) {
            SplitChangeSet changeSet = new SplitChangeSet(zipFile);
            Get.executor().submit(changeSet);
        }
    }

    private void executeFlwor(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open FLWOR query...");
        fileChooser.setInitialFileName("query.flwor");
        File flworFile = fileChooser.showOpenDialog(null);
        if (flworFile != null) {
            try (FileReader reader = new FileReader(flworFile)) {
                throw new UnsupportedOperationException();
//                Query queryFromDisk = Query.fromXml(reader);
//
//                fileChooser.setTitle("Specify query result file");
//                fileChooser.setInitialFileName("results.txt");
//                File resultsFile = fileChooser.showSaveDialog(null);
//
//                List<List<String>> results = queryFromDisk.executeQuery();
//
//                try (FileWriter writer = new FileWriter(resultsFile, Charset.forName(StandardCharsets.UTF_8.name()))) {
//                    for (List<String> row: results) {
//                        for (int i = 0; i < row.size(); i++) {
//                            writer.append(row.get(i));
//                            if (i < row.size() -1) {
//                                writer.append("\t");
//                            } else {
//                                writer.append("\n");
//                            }
//                        }
//                    }
//                }

            } catch (Throwable ex) {
                FxGet.dialogs().showErrorDialog("Error importing " + flworFile.getName(), ex);
            }

        }

    }
}
