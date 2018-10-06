/*
 * Copyright 2018 Your Organisation.
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
package sh.isaac.komet.changeset.view;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;

/**
 *
 * @author kec
 */
@Service
@Singleton
public class ChangeSetMenus implements MenuProvider {

    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.FILE);
    }

    @Override
    public MenuItem[] getMenuItems(AppMenu parentMenu, final Window window) {
        if (parentMenu == AppMenu.FILE) {
            MenuItem openChangeSetMenuItem = new MenuItem("Open change sets...");
            openChangeSetMenuItem.setOnAction((action) -> {
                openChangeSetAction(window);
            });
            return new MenuItem[]{openChangeSetMenuItem};
        }
        return new MenuItem[]{};
    }

    private void openChangeSetAction(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select change set files to open...");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("IBDF Files", "*.ibdf"));
        fileChooser.setInitialDirectory(new File("target/data/isaac.data/changesets"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(window);
        if (selectedFiles != null) {
            for (File selectedFile : selectedFiles) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
                    Parent root = loader.load();
                    FXMLController controller = loader.getController();
                    controller.setFile(selectedFile);
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add("/styles/Styles.css");
                    Stage stage = new Stage();
                    stage.setTitle(selectedFile.getName());
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

}
