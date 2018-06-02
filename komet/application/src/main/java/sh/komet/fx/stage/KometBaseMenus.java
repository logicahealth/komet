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

import java.util.EnumSet;
import java.util.HashMap;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.classifier.ClassifierService;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LoincDirectImporter;
import sh.isaac.solor.direct.LoincExpressionToConcept;
import sh.isaac.solor.direct.LoincExpressionToNavConcepts;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.exportation.ExportView;
import sh.komet.gui.importation.ImportView;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class KometBaseMenus implements MenuProvider {

    private final HashMap<Manifold.ManifoldGroup, Manifold> manifolds = new HashMap<>();

    public KometBaseMenus() {
       for (Manifold.ManifoldGroup mg : Manifold.ManifoldGroup.values()) {
            manifolds.put(mg, Manifold.make(mg));
        }
    }

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.FILE, AppMenu.TOOLS);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window) {
        switch (parentMenu) {
            case FILE: {
                MenuItem selectiveImport = new MenuItem("Selective import and transform");
                selectiveImport.setOnAction((ActionEvent event) -> {
                    ImportView.show(manifolds.get(Manifold.ManifoldGroup.TAXONOMY));
                });

                MenuItem selectiveExport = new MenuItem("Selective export");
                selectiveExport.setOnAction(event -> ExportView.show(manifolds.get(Manifold.ManifoldGroup.UNLINKED)));

                MenuItem importTransformFull = new MenuItem("Import and transform - FULL");

                importTransformFull.setOnAction((ActionEvent event) -> {
                    ImportAndTransformTask itcTask = new ImportAndTransformTask(manifolds.get(Manifold.ManifoldGroup.TAXONOMY),
                            ImportType.FULL);
                    Get.executor().submit(itcTask);

                });

                MenuItem importSourcesFull = new MenuItem("Import terminology content - FULL");
                importSourcesFull.setOnAction((ActionEvent event) -> {
                    DirectImporter importerFull = new DirectImporter(ImportType.FULL);
                    Get.executor().submit(importerFull);
                });
                return new MenuItem[]{selectiveImport, selectiveExport, importTransformFull,
                    importSourcesFull};
            }

            case TOOLS: {

                MenuItem transformSourcesFull = new MenuItem("Transform RF2 to EL++ - FULL");
                transformSourcesFull.setOnAction((ActionEvent event) -> {
                    Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(ImportType.FULL);
                    Get.executor().submit(transformer);
                });

                MenuItem transformSourcesActiveOnly = new MenuItem("Transform RF2 to EL++ - ACTIVE");
                transformSourcesActiveOnly.setOnAction((ActionEvent event) -> {
                    Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(ImportType.ACTIVE_ONLY);
                    Get.executor().submit(transformer);
                });

                MenuItem completeClassify = new MenuItem("Complete classify");
                completeClassify.setOnAction((ActionEvent event) -> {
                    //TODO change how we get the edit coordinate. 
                    EditCoordinate editCoordinate = Get.coordinateFactory().createDefaultUserSolorOverlayEditCoordinate();
                    ClassifierService classifierService = Get.logicService().getClassifierService(manifolds.get(Manifold.ManifoldGroup.SEARCH), editCoordinate);
                    classifierService.classify();
                });

                MenuItem completeReindex = new MenuItem("Complete reindex");
                completeReindex.setOnAction((ActionEvent event) -> {
                    Get.startIndexTask();
                });

                MenuItem recomputeTaxonomy = new MenuItem("Recompute taxonomy");
                recomputeTaxonomy.setOnAction((ActionEvent event) -> {
                    Get.taxonomyService().notifyTaxonomyListenersToRefresh();
                });

                MenuItem importLoincRecords = new MenuItem("Import LOINC records");
                importLoincRecords.setOnAction((ActionEvent event) -> {
                    LoincDirectImporter importTask = new LoincDirectImporter();
                    Get.executor().execute(importTask);
                });

                MenuItem addLabNavigationConcepts = new MenuItem("Add lab navigation concepts");
                addLabNavigationConcepts.setOnAction((ActionEvent event) -> {
                    LoincExpressionToNavConcepts conversionTask = new LoincExpressionToNavConcepts(manifolds.get(Manifold.ManifoldGroup.UNLINKED));
                    Get.executor().execute(conversionTask);
                });

                MenuItem convertLoincExpressions = new MenuItem("Convert LOINC expressions");
                convertLoincExpressions.setOnAction((ActionEvent event) -> {
                    LoincExpressionToConcept conversionTask = new LoincExpressionToConcept();
                    Get.executor().execute(conversionTask);
                });
                return new MenuItem[]{
                    completeClassify, completeReindex, recomputeTaxonomy,
                    importLoincRecords, addLabNavigationConcepts, convertLoincExpressions,
                    transformSourcesFull, transformSourcesActiveOnly
                };
            }
        }

        return new MenuItem[]{};
    }

}
