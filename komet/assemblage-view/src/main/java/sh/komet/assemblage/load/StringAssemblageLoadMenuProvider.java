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
package sh.komet.assemblage.load;

import java.io.File;
import java.util.EnumSet;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.Get;
import sh.isaac.api.component.concept.ConceptBuilderService;
import sh.isaac.api.logic.LogicalExpressionBuilderService;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class StringAssemblageLoadMenuProvider implements MenuProvider {
    private final ConceptBuilderService builderService;
    private final LogicalExpressionBuilderService expressionBuilderService;
    
    public StringAssemblageLoadMenuProvider() {
       this.builderService = Get.conceptBuilderService();
        this.expressionBuilderService = Get.logicalExpressionBuilderService();
     }

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.FILE);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, Window window) {
        if (parentMenu == AppMenu.FILE) {
            MenuItem importStringAssembalge = new MenuItem("Import String Assemblage...");
            importStringAssembalge.setOnAction((ActionEvent event) -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Tab Delimited File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Tab delimited text", "*.txt")
                );
                File selectedFile = fileChooser.showOpenDialog(null);
                if (selectedFile != null) {
                    String name = selectedFile.getName();
                    name = name.substring(0, name.length() - 4);
                    TextInputDialog dialog = new TextInputDialog(name);
                    dialog.setTitle("");
                    dialog.setHeaderText("Enter an unused name to \nidentify the assemblage\nthat will hold the imported \nstrings.");
                    dialog.setContentText("Name:");

                    // Traditional way to get the response value.
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        Get.executor().execute(new StringAssemblageLoadTask(selectedFile, result.get()));
                    }
                }

                //Get.executor().execute(new HdxJson(selectedFile));
            });
            return new MenuItem[]{importStringAssembalge};
        }
        return new MenuItem[]{};
    }

}
