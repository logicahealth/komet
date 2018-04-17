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
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import sh.isaac.api.Get;

public class ExporterController
{
	private final Logger LOG = LogManager.getLogger();

	@FXML
	private ResourceBundle resources;

	@FXML
	protected TextField exportLocation;

	@FXML
	private Button locationButton;

	@FXML
	protected CheckBox exportText;

	@FXML
	protected CheckBox exportExcel;

	@FXML
	protected CheckBox exportH2;

	@FXML
	private GridPane rootGridPane;

	@FXML
	void initialize()
	{
		assert exportLocation != null : "fx:id=\"location\" was not injected: check your FXML file 'Exporter.fxml'.";
		assert locationButton != null : "fx:id=\"locationButton\" was not injected: check your FXML file 'Exporter.fxml'.";
		assert exportText != null : "fx:id=\"exportText\" was not injected: check your FXML file 'Exporter.fxml'.";
		assert exportExcel != null : "fx:id=\"exportExcel\" was not injected: check your FXML file 'Exporter.fxml'.";
		assert exportH2 != null : "fx:id=\"exportH2\" was not injected: check your FXML file 'Exporter.fxml'.";

		try
		{
			exportLocation.setText(new File("export").getCanonicalPath());
			
			locationButton.setOnAction(event -> 
			{
				DirectoryChooser dc = new DirectoryChooser();
				File temp = new File(exportLocation.getText());
				if (temp.isDirectory())
				{
					dc.setInitialDirectory(temp);
				}
				dc.setTitle("Select output folder");
				File selection = dc.showDialog(locationButton.getScene().getWindow());
				if (selection != null)
				{
					try
					{
						exportLocation.setText(selection.getCanonicalPath());
					}
					catch (IOException e)
					{
						LOG.error("Unexpected", e);
						throw new RuntimeException(e);
					}
				}
			});
			
			if (Get.conceptService().getConceptCount() > 20000)
			{
				exportExcel.setSelected(false);
				exportExcel.setDisable(true);
			}
			
		}
		catch (Exception e)
		{
			LOG.error("Unexpected", e);
			throw new RuntimeException(e);
		}
	}
	
	protected GridPane getView()
	{
		return rootGridPane;
	}
}
