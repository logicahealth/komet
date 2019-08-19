/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.komet.gui.exporter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import sh.isaac.api.Get;
import sh.isaac.misc.exporters.TableExporter;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.menu.MenuItemWithText;

/**
 * {@link ExporterViewer}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class ExporterViewer implements MenuProvider {

    private final Logger LOG = LogManager.getLogger();

    private ExporterController ec_;
    private Window window_;

    private ExporterViewer() {
        // created by HK2
        LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
    }

    private void showView() {
        try {
            URL resource = ExporterController.class.getResource("Exporter.fxml");
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            ec_ = loader.getController();

            Alert stampDialog = new Alert(AlertType.CONFIRMATION);
            stampDialog.setTitle("Configure Table Export");
            stampDialog.setHeaderText("Please specify the parameters for export");
            stampDialog.getDialogPane().setContent(ec_.getView());
            stampDialog.setResizable(true);
            stampDialog.initOwner(window_);

            if (stampDialog.showAndWait().orElse(null) == ButtonType.OK) {
                try {
                    File temp = new File(ec_.exportLocation.getText());

                    TableExporter te = new TableExporter((ec_.exportText.isSelected() ? new File(temp, "text") : null),
                            (ec_.exportH2.isSelected() ? new File(temp, "h2") : null),
                            (ec_.exportExcel.isSelected() ? new File(temp, "excel") : null));

                    Get.activeTasks().add(te);
                    Get.workExecutors().getExecutor().execute(te);
                } catch (Exception e) {
                    LOG.error("Error launching export", e);
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Unexpected error launching export");
                    alert.initOwner(window_);
                    alert.setResizable(true);
                    alert.showAndWait();
                }
            }
        } catch (IOException e) {
            LOG.error("Unexpected!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<AppMenu> getParentMenus() {
        return EnumSet.of(AppMenu.TOOLS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuItem[] getMenuItems(AppMenu appMenu, Window window) {
        if (appMenu == AppMenu.TOOLS) {
            this.window_ = window;
            MenuItem mi = new MenuItemWithText("Table Exporter");
            mi.setOnAction(event
                    -> {
                showView();
            });
            return new MenuItem[] { mi };
        }
        return new MenuItem[] {};
    }
}
