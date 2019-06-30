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
package sh.komet.assemblage.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.util.number.NumberUtil;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.concept.ConceptLabelToolbar;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import static sh.komet.gui.style.StyleClasses.ASSEMBLAGE_DETAIL;

/**
 *
 * @author kec
 */
public class AssemblageViewProvider implements ExplorationNode, Supplier<List<MenuItem>>  {
    protected static final Logger LOG = LogManager.getLogger();


   private final BorderPane assemblageDetailPane = new BorderPane();
   private final Manifold manifold;
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("listing of assemblage members");
   private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty assemblage view");
   private final ConceptLabelToolbar conceptLabelToolbar;

   public AssemblageViewProvider(Manifold manifold) {
      try {
         this.manifold = manifold;
         this.assemblageDetailPane.getStyleClass().setAll(ASSEMBLAGE_DETAIL.toString());
         this.conceptLabelToolbar = ConceptLabelToolbar.make(manifold, this, Optional.empty());
         this.assemblageDetailPane.setTop(conceptLabelToolbar.getToolbarNode());
         manifold.setGroupName(ManifoldGroup.UNLINKED.getGroupName());

         FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/assemblage/view/AssemblageDetail.fxml"));
         loader.load();
         AssemblageDetailController assemblageDetailController = loader.getController();
         assemblageDetailController.setManifold(manifold);
         assemblageDetailController.getManifold().focusedConceptProperty().addListener(this::focusConceptChanged);
         assemblageDetailPane.setCenter(assemblageDetailController.getAssemblageDetailRootPane());
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }

   }
   
   private void focusConceptChanged(ObservableValue<? extends ConceptSpecification> observable,
           ConceptSpecification oldValue,
           ConceptSpecification newValue) {
      if (newValue == null) {
         LOG.warn("Not sure on intent.... but hey, I didn't break the GUI...");
         return;
      }
      titleProperty.set(manifold.getPreferredDescriptionText(newValue));
      toolTipProperty.set("View of all " + manifold.getPreferredDescriptionText(newValue) + " assemblage members");
      int count = (int) Get.identifierService().getNidsForAssemblage(newValue.getNid()).count();
      this.conceptLabelToolbar.getRightInfoLabel().setText(NumberUtil.formatWithGrouping(count) + " elements");
   }
   
   @Override
   public Node getMenuIcon() {
      return Iconography.PAPERCLIP.getIconographic();
   }
   
   @Override
   public Manifold getManifold() {
      return manifold;
   }

   @Override
   public Node getNode() {
      return assemblageDetailPane;
   }

   @Override
   public ReadOnlyProperty<String> getToolTip() {
      return toolTipProperty;
   }

   @Override
   public ReadOnlyProperty<String> getTitle() {
      return titleProperty;
   }

   @Override
   public Optional<Node> getTitleNode() {
      return Optional.empty();
   }
    @Override
    public List<MenuItem> get() {
        List<MenuItem> assemblageMenuList = new ArrayList<>();
        Menu assemblagesMenu = new Menu("Populated assemblages");
        assemblageMenuList.add(assemblagesMenu);
        Menu versionByTypeMenu = new Menu("Assemblages by version type");
        assemblageMenuList.add(versionByTypeMenu);
        HashMap<VersionType, Menu> versionTypeMenuMap = new HashMap();
 
        
        for (VersionType versionType: VersionType.values()) {
            Menu versionTypeMenu = new Menu(versionType.toString());
            versionTypeMenuMap.put(versionType, versionTypeMenu);
            versionByTypeMenu.getItems().add(versionTypeMenu);
        }
        
        int[] assembalgeNids = Get.assemblageService().getAssemblageConceptNids();
        
        LOG.debug("Assemblage nid count: " + assembalgeNids.length + "\n" + Arrays.toString(assembalgeNids));
        
        for (int assemblageNid : Get.assemblageService().getAssemblageConceptNids()) {
            MenuItem menu = new MenuItem(manifold.getPreferredDescriptionText(assemblageNid));
            menu.setOnAction((event) -> {
                manifold.setFocusedConceptChronology(Get.concept(assemblageNid));
            });
            assemblagesMenu.getItems().add(menu);
            String preferredDescText = manifold.getPreferredDescriptionText(assemblageNid);
            LOG.debug("Assemblage name <" + assemblageNid + ">: " + preferredDescText);
            MenuItem menu2 = new MenuItem(preferredDescText);
            menu2.setOnAction((event) -> {
                manifold.setFocusedConceptChronology(Get.concept(assemblageNid));
            });
            VersionType versionType = Get.assemblageService().getVersionTypeForAssemblage(assemblageNid);
            versionTypeMenuMap.get(versionType).getItems().add(menu2);
        }
        assemblagesMenu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText()); 
            });
        for (Menu menu: versionTypeMenuMap.values()) {
            menu.getItems().sort((o1, o2) -> {
                return o1.getText().compareTo(o2.getText()); 
            });
        }
        
        return assemblageMenuList;
    }

    @Override
    public void close() {
        // nothing to do...
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
