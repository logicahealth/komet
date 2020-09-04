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

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.set.primitive.ImmutableIntSet;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.TaxonomySnapshot;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.tree.Tree;
import sh.isaac.api.util.time.DurationUtil;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LogicGraphTransformerAndWriter;
import sh.isaac.solor.direct.TransformationGroup;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.contract.preferences.WindowPreferences;
import sh.komet.gui.menu.MenuItemWithText;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class DeveloperMenus implements MenuProvider {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.EDIT);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window, WindowPreferences windowPreference) {
        if (parentMenu == AppMenu.EDIT) {
            
            MenuItem debugConversion = new MenuItemWithText("Debug Respiratory region of nose conversion");
            debugConversion.setUserData(windowPreference);
            debugConversion.setOnAction(this::debugNoseConversion);

            MenuItem debugEarFindingConversion = new MenuItemWithText("Debug Ear Finding conversion");
            debugEarFindingConversion.setUserData(windowPreference);
            debugEarFindingConversion.setOnAction(this::debugEarFindingConversion);

            MenuItem debugIsoniazidConversion = new MenuItemWithText("Debug Isoniazid conversion");
            debugIsoniazidConversion.setUserData(windowPreference);
            debugIsoniazidConversion.setOnAction(this::debugIsoniazidConversion);

            MenuItem debugLamivudineConversion = new MenuItemWithText("Debug Lamivudine conversion");
            debugLamivudineConversion.setUserData(windowPreference);
            debugLamivudineConversion.setOnAction(this::debugLamivudineConversion);

            MenuItem debugConnectiveTissueConversion = new MenuItemWithText("Debug Connective Tissue conversion");
            debugConnectiveTissueConversion.setUserData(windowPreference);
            debugConnectiveTissueConversion.setOnAction(this::debugConnectiveTissueConversion);

            MenuItem debugAdductorMuscleConversion = new MenuItemWithText("Debug Adductor Muscle conversion");
            debugAdductorMuscleConversion.setUserData(windowPreference);
            debugAdductorMuscleConversion.setOnAction(this::debugAdductorMuscleConversion);

            MenuItem debugPrematureConversion = new MenuItemWithText("Debug Premature conversion");
            debugPrematureConversion.setUserData(windowPreference);
            debugPrematureConversion.setOnAction(this::debugPrematureConversion);

            MenuItem debugSepsisConversion = new MenuItemWithText("Debug Sepsis conversion");
            debugSepsisConversion.setUserData(windowPreference);
            debugSepsisConversion.setOnAction(this::debugSepsisConversion);

            MenuItem debugDizzinessConversion = new MenuItemWithText("Debug Dizziness conversion");
            debugDizzinessConversion.setUserData(windowPreference);
            debugDizzinessConversion.setOnAction(this::debugDizzinessConversion);

            MenuItem debugNonallopathicConversion = new MenuItemWithText("Debug Nonallopathic conversion");
            debugNonallopathicConversion.setUserData(windowPreference);
            debugNonallopathicConversion.setOnAction(this::debugNonallopathicConversion);

            MenuItem debugNephronophthisisConversion = new MenuItemWithText("Debug Nephronophthisis conversion");
            debugNephronophthisisConversion.setUserData(windowPreference);
            debugNephronophthisisConversion.setOnAction(this::debugNephronophthisisConversion);

            MenuItem debugTenosynovitisConversion = new MenuItemWithText("Debug Tenosynovitis conversion");
            debugTenosynovitisConversion.setUserData(windowPreference);
            debugTenosynovitisConversion.setOnAction(this::debugTenosynovitisConversion);

            MenuItem debugCprConversion = new MenuItemWithText("Debug CPR conversion");
            debugCprConversion.setUserData(windowPreference);
            debugCprConversion.setOnAction(this::debugCprConversion);

            MenuItem testTaxonomyDistance = new MenuItemWithText("SimpleExtensionFunction taxonomy distance");
            testTaxonomyDistance.setUserData(windowPreference);
            testTaxonomyDistance.setOnAction(this::testTaxonomyDistance);

            return new MenuItem[]{debugConversion, debugEarFindingConversion, debugIsoniazidConversion,
            debugLamivudineConversion, debugConnectiveTissueConversion, debugAdductorMuscleConversion,
            debugPrematureConversion, debugSepsisConversion, debugDizzinessConversion,
            debugNonallopathicConversion, debugNephronophthisisConversion, debugTenosynovitisConversion, 
            debugCprConversion, testTaxonomyDistance};
        }
        return new MenuItem[]{};
    }

    private void debugNoseConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Structure of respiratory region of nose (body structure)", UUID.fromString("aebff175-243b-38b3-9b02-5910f7388d0d"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugEarFindingConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Ear and auditory finding (finding)", UUID.fromString("703b38f0-8f1d-3832-8e6d-b27322e3b9c2"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }


    private void debugIsoniazidConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Product containing isoniazid and rifampicin (medicinal product)", UUID.fromString("7c342128-1eb4-329f-91f7-ad917e16847c"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }


    private void debugLamivudineConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Product containing lamivudine and zidovudine (medicinal product)", UUID.fromString("4f1120bf-ff56-3396-8689-bfe71e6f90d3"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }


    private void debugConnectiveTissueConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Disorder of connective tissue (disorder)", UUID.fromString("47641f4c-7d62-398a-a2dd-8ced54edea08"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }


    private void debugAdductorMuscleConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Structure of adductor muscle of lower limb (body structure)", UUID.fromString("18d3fc15-8a84-315f-8a88-a1a2af7b7ab5"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugPrematureConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Premature labor (finding)", UUID.fromString("23763987-fe6a-3afb-8c1e-56f48d426c68"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugSepsisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Sepsis (disorder)", UUID.fromString("052b5b5e-46c6-3fab-aa24-bd8d6bd7f9cd"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugDizzinessConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Dizziness (finding)", UUID.fromString("075bd2cc-1a6e-3e89-9eb9-08e383c1665a"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugNonallopathicConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Nonallopathic lesion (finding)", UUID.fromString("10daed5a-d009-3dcb-9fb2-86ab63ec19f3"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugNephronophthisisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Nephronophthisis - medullary cystic disease (disorder)", UUID.fromString("842add9a-148e-317b-a6f3-ac66ecfae611"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugTenosynovitisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Tenosynovitis (disorder)", UUID.fromString("51c3117f-245b-3fab-a704-4687d6b55de4"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void debugCprConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Cardiopulmonary resuscitation (procedure)", UUID.fromString("61e1cc85-7e14-3935-8af2-a8523c0dfe5d"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void testTaxonomyDistance(ActionEvent event) {

        WindowPreferences windowPreferences = (WindowPreferences) ((MenuItem) event.getSource()).getUserData();
        Instant startInstant = Instant.now();

        // minimal common ancestor = new ConceptProxy("Disorder of body system (disorder)", UUID.fromString("1088bacb-ed50-3fa0-8f75-6b7b9030487e"))
        ConceptProxy concept1 = new ConceptProxy("Renal anasarca (disorder)", UUID.fromString("fb334dc1-c6fd-3136-9f56-0b1dfeb417c2"));
        ConceptProxy concept2 = new ConceptProxy("Hearing loss of bilateral ears caused by noise (disorder)", UUID.fromString("749ad125-b01a-3973-afbb-04bb9d599e98"));

        TaxonomySnapshot snapshot = Get.taxonomyService().getSnapshot(windowPreferences.getViewPropertiesForWindow().getManifoldCoordinate());

        Tree snapshotTree = snapshot.getTaxonomyTree();

        LOG.info("Distance setup time: " + DurationUtil.format(Duration.between(startInstant, Instant.now())));
        startInstant = Instant.now();
        Float distanceDirected = snapshotTree.getTaxonomyDistance(concept1.getNid(), concept2.getNid(), true);
        LOG.info("Directed distance: " + distanceDirected + " in " + DurationUtil.format(Duration.between(startInstant, Instant.now())));
        startInstant = Instant.now();

        Float distanceUndirected = snapshotTree.getTaxonomyDistance(concept1.getNid(), concept2.getNid(), false);
        LOG.info("Undirected distance: " + distanceUndirected + " in " + DurationUtil.format(Duration.between(startInstant, Instant.now())));
    }

    protected void processRecords(ConceptProxy debugProxy) throws InterruptedException, ExecutionException, NoSuchElementException {
        ImmutableIntSet relNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(debugProxy.getNid(), MetaData.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
        TransformationGroup transformationGroup = new TransformationGroup(debugProxy.getNid(), relNids.toArray(), PremiseType.INFERRED);
        List<TransformationGroup> transformationRecords = new ArrayList<>();
        transformationRecords.add(transformationGroup);
        Semaphore writeSemaphore = new Semaphore(1);
        LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(transformationRecords,
                writeSemaphore, ImportType.FULL, Instant.now());
        Get.executor().execute(transformer);
        transformer.get();
    }
}
