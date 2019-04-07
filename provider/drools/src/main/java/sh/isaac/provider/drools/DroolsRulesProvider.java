/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.provider.drools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.action.Action;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import sh.isaac.api.*;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.komet.gui.contract.RulesDrivenKometService;
import sh.komet.gui.control.PropertySheetMenuItem;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service
@RunLevel(value = LookupService.SL_L2)
public class DroolsRulesProvider implements BusinessRulesService, RulesDrivenKometService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LogManager.getLogger();
    private KieServices kieServices;
    private KieContainer kContainer;
    private StatelessKieSession kSession;
    private KieRepository kRepo;
    private KieFileSystem kfs;
    private Path droolsPath;
    /**
     * Start me.
     */
    @PostConstruct
    protected void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Startings Drools provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting Drools Rules Provider post-construct");

            ConfigurationService configurationService = LookupService.getService(ConfigurationService.class);
            Path folderPath = configurationService.getDataStoreFolderPath();
            this.droolsPath = folderPath.resolve("drools");
            Files.createDirectories(droolsPath);
            Files.copy(getClass().getResourceAsStream("/rules/sh/isaac/provider/drools/AddAttachmentRules.drl"),
                    droolsPath.resolve("AddAttachmentRules.drl"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(getClass().getResourceAsStream("/rules/sh/isaac/provider/drools/EditLogicalExpressionRules.drl"),
                    droolsPath.resolve("EditLogicalExpressionRules.drl"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(getClass().getResourceAsStream("/rules/sh/isaac/provider/drools/EditVersionRules.drl"),
                    droolsPath.resolve("EditVersionRules.drl"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(getClass().getResourceAsStream("/rules/sh/isaac/provider/drools/PopulateProperties.drl"),
                    droolsPath.resolve("PopulateProperties.drl"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(getClass().getResourceAsStream("/META-INF/kmodule.xml"),
                    droolsPath.resolve("kmodule.xml"), StandardCopyOption.REPLACE_EXISTING);
            
            this.kieServices = KieServices.Factory.get();
            this.kRepo = this.kieServices.getRepository();
            this.kfs = this.kieServices.newKieFileSystem();
            
            this.kfs.writeKModuleXML(Files.readAllBytes(droolsPath.resolve("kmodule.xml")));

            this.kfs.write("src/main/resources/rules/sh/isaac/provider/drools/AddAttachmentRules.drl", 
                    Files.readAllBytes(droolsPath.resolve("AddAttachmentRules.drl")));
            this.kfs.write("src/main/resources/rules/sh/isaac/provider/drools/EditLogicalExpressionRules.drl", 
                    Files.readAllBytes(droolsPath.resolve("EditLogicalExpressionRules.drl")));
            this.kfs.write("src/main/resources/rules/sh/isaac/provider/drools/EditVersionRules.drl", 
                    Files.readAllBytes(droolsPath.resolve("EditVersionRules.drl")));
            this.kfs.write("src/main/resources/rules/sh/isaac/provider/drools/PopulateProperties.drl", 
                    Files.readAllBytes(droolsPath.resolve("PopulateProperties.drl")));
            
            updateRules();
            
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            progressTask.finished();
        }
        
    }

    protected void updateRules() throws RuntimeException {
        KieBuilder kb = this.kieServices.newKieBuilder(kfs);
        
        kb.buildAll(); // kieModule is automatically deployed to KieRepository if successfully built.
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        
        this.kContainer = this.kieServices.newKieContainer(kRepo.getDefaultReleaseId());
        this.kSession = this.kContainer.newStatelessKieSession();
    }

    @Override
    public void addResourcesAndUpdate(BusinessRulesResource... ruleResources) {
        for (BusinessRulesResource resource: ruleResources) {
            try {
                int index = resource.getResourceLocation().lastIndexOf("/");
                Files.copy(new ByteArrayInputStream(resource.getResourceBytes()),
                        droolsPath.resolve(resource.getResourceLocation().substring(index + 1)), StandardCopyOption.REPLACE_EXISTING);
                
                this.kfs.write(resource.getResourceLocation(),
                        resource.getResourceBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        updateRules();
    }

    /**
     * Stop me.
     */
    @PreDestroy
    protected void stopMe() {
        LOG.info("Stopping Drools Rules Provider.");
        this.kieServices = null;
        this.kContainer = null;
        this.kSession = null;
    }
    
    @Override
    public List<Action> getEditLogicalExpressionNodeMenuItems(Manifold manifold,
            LogicNode nodeToEdit,
            LogicalExpression expressionContiningNode,
            Consumer<LogicalExpression> expressionUpdater, 
            MouseEvent mouseEvent) {
        AddEditLogicalExpressionNodeMenuItems executionItem
                = new AddEditLogicalExpressionNodeMenuItems(manifold, nodeToEdit,
                        expressionContiningNode, expressionUpdater, mouseEvent);
        this.kSession.execute(executionItem);
        executionItem.sortActionItems();
        return executionItem.getActionItems();        
    }
    
    @Override
    public List<MenuItem> getEditVersionMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
            Consumer<PropertySheetMenuItem> propertySheetConsumer) {
        AddEditVersionMenuItems executionItem = new AddEditVersionMenuItems(manifold, categorizedVersion, propertySheetConsumer);
        this.kSession.execute(executionItem);
        return executionItem.menuItems;
    }
    
    @Override
    public List<MenuItem> getAddAttachmentMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
            BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer) {
        AddAttachmentMenuItems executionItem = new AddAttachmentMenuItems(manifold, categorizedVersion, newAttachmentConsumer);
        this.kSession.execute(executionItem);
        executionItem.sortMenuItems();
        return executionItem.getMenuItems();
    }
    
    @Override
    public void populatePropertySheetEditors(PropertySheetMenuItem propertySheetMenuItem) {
        this.kSession.execute(propertySheetMenuItem);
    }
    
    @Override
    public void populateWrappedProperties(List<PropertySheet.Item> items) {
        this.kSession.execute(items);
    }


    /*
A fluent API was created to allow programatic creation of rules as an alternative to the previously suggested method of template creation.

PackageDescr pkg = DescrFactory.newPackage()
                   .name("org.drools.example")
                   .newRule().name("Xyz")
                       .attribute("ruleflow-grou","bla")
                   .lhs()
                       .and()
                           .pattern("Foo").id("$foo").constraint("bar==baz").constraint("x>y").end()
                           .not().pattern("Bar").constraint("a+b==c").end()
                       .end()
                   .end()
                   .rhs( "System.out.println();" )
                   .end();   
     */
}
