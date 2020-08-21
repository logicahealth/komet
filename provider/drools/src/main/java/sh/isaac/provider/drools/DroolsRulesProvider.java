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
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import sh.isaac.api.BusinessRulesResource;
import sh.isaac.api.BusinessRulesService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.coordinate.EditCoordinateImmutable;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.isaac.api.task.LabelTaskWithIndeterminateProgress;
import sh.komet.gui.contract.RulesDrivenKometService;
import sh.komet.gui.control.property.wrapper.PropertySheetMenuItem;
import sh.komet.gui.control.property.ViewProperties;

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
    public static final String KOMET_SESSION = "komet-session";
    public static final String MANIFOLD_COORDINATE = "manifoldCoordinate";
    public static final String EDIT_COORDINATE = "editCoordinate";

    private final KieServices kieServices = KieServices.Factory.get();;
    private KieContainer classPathContainer;
    private StatelessKieSession staticSession;
    private StatelessKieSession dynamicSession;
    private final ReleaseId dynamicReleaseId = this.kieServices.newReleaseId("sh.komet.rules", "dynamic", "latest");
    private final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
    private final KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
    /**
     * Start me.
     */
    @PostConstruct
    protected void startMe() {
        LabelTaskWithIndeterminateProgress progressTask = new LabelTaskWithIndeterminateProgress("Starting Drools provider");
        Get.executor().execute(progressTask);
        try {
            LOG.info("Starting Drools Rules Provider post-construct");

            this.classPathContainer = kieServices.getKieClasspathContainer();
            this.staticSession = this.classPathContainer.newStatelessKieSession("komet-static-session");
            KieBaseModel kieBaseModelUser = kieModuleModel.newKieBaseModel( "dynamic-komet-rules")
                    .setDefault( true )
                    .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                    .setEventProcessingMode( EventProcessingOption.STREAM );

            kieBaseModelUser.newKieSessionModel(KOMET_SESSION)
                    .setDefault( true )
                    .setType( KieSessionModel.KieSessionType.STATELESS )
                    .setClockType( ClockTypeOption.get("realtime") );

            this.kieFileSystem.writeKModuleXML(kieModuleModel.toXML());

            this.kieFileSystem.generateAndWritePomXML(dynamicReleaseId);


        } finally {
            progressTask.finished();
        }
    }


    @Override
    public void addResourcesAndUpdate(BusinessRulesResource... ruleResources) {
        // TODO return messages from build, and surface to user as appropriate.
        // For dynamic rules...

        for (BusinessRulesResource ruleResource: ruleResources) {
            kieFileSystem.write( "src/main/resources/dynamic-komet-rules/" + ruleResource.getResourceLocation(), kieServices.getResources().newInputStreamResource(
                    new ByteArrayInputStream(ruleResource.getResourceBytes())) );
        }
        KieBuilder builder = kieServices.newKieBuilder(kieFileSystem);
        builder.buildAll();

        for (Message message: builder.getResults().getMessages()) {
            LOG.info(message.getText());
        }
        KieContainer dynamicContainer = this.kieServices.newKieContainer(this.dynamicReleaseId);
        this.dynamicSession = dynamicContainer.newStatelessKieSession(KOMET_SESSION);

    }

    /**
     * Stop me.
     */
    @PreDestroy
    protected void stopMe() {
        LOG.info("Stopping Drools Rules Provider.");
        this.classPathContainer = null;
        this.staticSession = null;
        LOG.info("Stopped Drools Rules Provider.");
    }
    
    @Override
    public List<Action> getEditLogicalExpressionNodeMenuItems(ViewProperties viewProperties,
                                                              LogicNode nodeToEdit,
                                                              LogicalExpression expressionContiningNode,
                                                              Consumer<LogicalExpression> expressionUpdater,
                                                              MouseEvent mouseEvent) {
        AddEditLogicalExpressionNodeMenuItems executionItem
                = new AddEditLogicalExpressionNodeMenuItems(viewProperties, nodeToEdit,
                        expressionContiningNode, expressionUpdater, mouseEvent);
        this.staticSession.execute(executionItem);
        executionItem.sortActionItems();
        return executionItem.getActionItems();        
    }
    
    @Override
    public List<MenuItem> getEditVersionMenuItems(ManifoldCoordinate manifoldCoordinate, ObservableCategorizedVersion categorizedVersion,
                                                  Consumer<PropertySheetMenuItem> propertySheetConsumer) {
        AddEditVersionMenuItems executionItem = new AddEditVersionMenuItems(manifoldCoordinate, categorizedVersion, propertySheetConsumer);
        this.staticSession.execute(executionItem);
        if (this.dynamicSession != null) {
            this.dynamicSession.execute(executionItem);
        }
        return executionItem.menuItems;
    }
    
    @Override
    public List<MenuItem> getAddAttachmentMenuItems(ManifoldCoordinate manifoldCoordinate, ObservableCategorizedVersion categorizedVersion,
                                                    BiConsumer<PropertySheetMenuItem, ConceptSpecification> newAttachmentConsumer) {
        AddAttachmentMenuItems executionItem = new AddAttachmentMenuItems(manifoldCoordinate, categorizedVersion, newAttachmentConsumer);
        this.staticSession.execute(executionItem);
        if (this.dynamicSession != null) {
            this.dynamicSession.execute(executionItem);
        }
        executionItem.sortMenuItems();
        return executionItem.getMenuItems();
    }
    
    @Override
    public void populatePropertySheetEditors(PropertySheetMenuItem propertySheetMenuItem) {
        this.staticSession.execute(propertySheetMenuItem);
        if (this.dynamicSession != null) {
            this.dynamicSession.execute(propertySheetMenuItem);
        }
    }
    
    @Override
    public void populateWrappedProperties(List<PropertySheet.Item> items,
                                          ManifoldCoordinateImmutable manifoldCoordinate,
                                          EditCoordinateImmutable editCoordinate) {

        this.staticSession.setGlobal(MANIFOLD_COORDINATE, manifoldCoordinate);
        this.staticSession.setGlobal(EDIT_COORDINATE, editCoordinate);
        this.staticSession.execute(items);
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
