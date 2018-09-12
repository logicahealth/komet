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

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.Get;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LogicGraphTransformerAndWriter;
import sh.isaac.solor.direct.TransformationGroup;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class DeveloperMenus implements MenuProvider {

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.EDIT);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window) {
        if (parentMenu == AppMenu.EDIT) {
            
            MenuItem debugConversion = new MenuItem("Debug Respiratory region of nose conversion");
            debugConversion.setOnAction(this::debugNoseConversion);

            MenuItem debugEarFindingConversion = new MenuItem("Debug Ear Finding conversion");
            debugEarFindingConversion.setOnAction(this::debugEarFindingConversion);

            MenuItem debugIsoniazidConversion = new MenuItem("Debug Isoniazid conversion");
            debugIsoniazidConversion.setOnAction(this::debugIsoniazidConversion);

            MenuItem debugLamivudineConversion = new MenuItem("Debug Lamivudine conversion");
            debugLamivudineConversion.setOnAction(this::debugLamivudineConversion);

            MenuItem debugConnectiveTissueConversion = new MenuItem("Debug Connective Tissue conversion");
            debugConnectiveTissueConversion.setOnAction(this::debugConnectiveTissueConversion);

            MenuItem debugAdductorMuscleConversion = new MenuItem("Debug Adductor Muscle conversion");
            debugAdductorMuscleConversion.setOnAction(this::debugAdductorMuscleConversion);

            MenuItem debugPrematureConversion = new MenuItem("Debug Premature conversion");
            debugPrematureConversion.setOnAction(this::debugPrematureConversion);

            MenuItem debugSepsisConversion = new MenuItem("Debug Sepsis conversion");
            debugSepsisConversion.setOnAction(this::debugSepsisConversion);

            MenuItem debugDizzinessConversion = new MenuItem("Debug Dizziness conversion");
            debugDizzinessConversion.setOnAction(this::debugDizzinessConversion);

            MenuItem debugNonallopathicConversion = new MenuItem("Debug Nonallopathic conversion");
            debugNonallopathicConversion.setOnAction(this::debugNonallopathicConversion);

            MenuItem debugNephronophthisisConversion = new MenuItem("Debug Nephronophthisis conversion");
            debugNephronophthisisConversion.setOnAction(this::debugNephronophthisisConversion);

            MenuItem debugTenosynovitisConversion = new MenuItem("Debug Tenosynovitis conversion");
            debugTenosynovitisConversion.setOnAction(this::debugTenosynovitisConversion);

            MenuItem debugCprConversion = new MenuItem("Debug CPR conversion");
            debugCprConversion.setOnAction(this::debugCprConversion);

            return new MenuItem[]{debugConversion, debugEarFindingConversion, debugIsoniazidConversion,
            debugLamivudineConversion, debugConnectiveTissueConversion, debugAdductorMuscleConversion,
            debugPrematureConversion, debugSepsisConversion, debugDizzinessConversion,
            debugNonallopathicConversion, debugNephronophthisisConversion, debugTenosynovitisConversion, 
            debugCprConversion};
        }
        return new MenuItem[]{};
    }
    
    private void debugNoseConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Structure of respiratory region of nose (body structure)", UUID.fromString("aebff175-243b-38b3-9b02-5910f7388d0d"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugEarFindingConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Ear and auditory finding (finding)", UUID.fromString("703b38f0-8f1d-3832-8e6d-b27322e3b9c2"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void debugIsoniazidConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Product containing isoniazid and rifampicin (medicinal product)", UUID.fromString("7c342128-1eb4-329f-91f7-ad917e16847c"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void debugLamivudineConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Product containing lamivudine and zidovudine (medicinal product)", UUID.fromString("4f1120bf-ff56-3396-8689-bfe71e6f90d3"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void debugConnectiveTissueConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Disorder of connective tissue (disorder)", UUID.fromString("47641f4c-7d62-398a-a2dd-8ced54edea08"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void debugAdductorMuscleConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Structure of adductor muscle of lower limb (body structure)", UUID.fromString("18d3fc15-8a84-315f-8a88-a1a2af7b7ab5"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugPrematureConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Premature labor (finding)", UUID.fromString("23763987-fe6a-3afb-8c1e-56f48d426c68"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugSepsisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Sepsis (disorder)", UUID.fromString("052b5b5e-46c6-3fab-aa24-bd8d6bd7f9cd"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugDizzinessConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Dizziness (finding)", UUID.fromString("075bd2cc-1a6e-3e89-9eb9-08e383c1665a"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugNonallopathicConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Nonallopathic lesion (finding)", UUID.fromString("10daed5a-d009-3dcb-9fb2-86ab63ec19f3"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugNephronophthisisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Nephronophthisis - medullary cystic disease (disorder)", UUID.fromString("842add9a-148e-317b-a6f3-ac66ecfae611"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugTenosynovitisConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Tenosynovitis (disorder)", UUID.fromString("51c3117f-245b-3fab-a704-4687d6b55de4"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugCprConversion(ActionEvent event) {
        try {
            ConceptProxy debugProxy = new ConceptProxy("Cardiopulmonary resuscitation (procedure)", UUID.fromString("61e1cc85-7e14-3935-8af2-a8523c0dfe5d"));
            processRecords(debugProxy);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(DeveloperMenus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void processRecords(ConceptProxy debugProxy) throws InterruptedException, ExecutionException, NoSuchElementException {
        NidSet relNids = Get.assemblageService().getSemanticNidsForComponentFromAssemblage(debugProxy.getNid(), MetaData.RF2_INFERRED_RELATIONSHIP_ASSEMBLAGE____SOLOR.getNid());
        TransformationGroup transformationGroup = new TransformationGroup(debugProxy.getNid(), relNids.asArray(), PremiseType.INFERRED);
        List<TransformationGroup> transformationRecords = new ArrayList<>();
        transformationRecords.add(transformationGroup);
        Semaphore writeSemaphore = new Semaphore(1);
        LogicGraphTransformerAndWriter transformer = new LogicGraphTransformerAndWriter(transformationRecords,
                writeSemaphore, ImportType.FULL, Instant.now());
        Get.executor().execute(transformer);
        transformer.get();
    }
}
