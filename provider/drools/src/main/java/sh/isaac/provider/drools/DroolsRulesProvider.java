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

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.MenuItem;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.PropertySheet;
import org.jvnet.hk2.annotations.Service;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import sh.isaac.MetaData;
import sh.isaac.api.BusinessRulesService;
import sh.isaac.api.ConceptProxy;
import sh.isaac.api.State;
import sh.isaac.api.observable.ObservableCategorizedVersion;
import sh.komet.gui.contract.RulesDrivenKometService;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class DroolsRulesProvider implements BusinessRulesService, RulesDrivenKometService {

   /**
    * The Constant LOG.
    */
   private static final Logger LOG = LogManager.getLogger();
   KieServices kieServices;
   KieContainer kContainer;
   StatelessKieSession kSession;

   /**
    * Start me.
    */
   @PostConstruct
   protected void startMe() {
      LOG.info("Starting Drools Rules Provider post-construct");
      this.kieServices = KieServices.Factory.get();
      this.kContainer = kieServices.getKieClasspathContainer();
      this.kSession = kContainer.newStatelessKieSession("komet-rules");
   }

   /**
    * Stop me.
    */
   @PreDestroy
   protected void stopMe() {
      LOG.info("Stopping Drools Rules Provider.");
   }

   @Override
   public List<MenuItem> getEditMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion, Consumer<PropertySheet> propertySheetConsumer) {
      AddEditVersionMenuItems executionItem = new AddEditVersionMenuItems(manifold, categorizedVersion, propertySheetConsumer);
      this.kSession.execute(executionItem);
      return executionItem.menuItems;
   }

   @Override
   public List<MenuItem> getAttachmentMenuItems(Manifold manifold, ObservableCategorizedVersion categorizedVersion, Consumer<PropertySheet> propertySheetConsumer) {
      AddAttachmentMenuItems executionItem = new AddAttachmentMenuItems(manifold, categorizedVersion, propertySheetConsumer);
      this.kSession.execute(executionItem);
      return executionItem.menuItems;
   }

   private void addPropertySheetEditorAction(Manifold manifold, ObservableCategorizedVersion categorizedVersion,
           Consumer<PropertySheet> propertySheetConsumer, MenuItem editPropertySheetItem) {

      PropertySheet propertySheet = new PropertySheet();
      propertySheet.setPropertyEditorFactory(new IsaacPropertyEditorFactory(manifold));
      // add status and module items...
      IntegerProperty moduleSequenceProperty = categorizedVersion.moduleSequenceProperty();
      // the name of the property is the external string of the concept that specifies the semantics of this field. 
      // TODO: add a gui dialect...
      // TODO: how do we know it is a concept field?
      // TODO: how to determine the constraints? 
      // Need finer granularity than just an Assemblage...
      // Pass the version + field semantics concept spec to Drools. 
      // default rule is to use the field constraints/binding. 
      // Add class-based rules that specialize the constraints. zx
      ConceptProxy moduleSequenceConceptSpec = new ConceptProxy(moduleSequenceProperty.getName());

      // TODO: add a gui dialect...
      // TODO: how do we know it is a concept field?
      ObjectProperty<State> stateProperty = categorizedVersion.stateProperty();
      ConceptProxy stateSpecConceptSpec = new ConceptProxy(stateProperty.getName());
      stateSpecConceptSpec.equals(MetaData.MODULE_SEQUENCE_FOR_VERSION____ISAAC);
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
