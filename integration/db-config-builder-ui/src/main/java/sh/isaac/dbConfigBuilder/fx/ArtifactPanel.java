/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.dbConfigBuilder.fx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Aligns with the AritfactPanel FXML file
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ArtifactPanel
{
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	protected TextField artifactReadUrl;
	
	@FXML
	protected TextField artifactDeployUrl;

	@FXML
	protected TextField artifactUsername;

	@FXML
	protected PasswordField artifactPassword;

	@FXML
	void initialize()
	{
		assert artifactReadUrl != null : "fx:id=\"artifactReadUrl\" was not injected: check your FXML file 'ArtifactPanel.fxml'.";
		assert artifactDeployUrl != null : "fx:id=\"artifactDeployUrl\" was not injected: check your FXML file 'ArtifactPanel.fxml'.";
		assert artifactUsername != null : "fx:id=\"artifactUsername\" was not injected: check your FXML file 'ArtifactPanel.fxml'.";
		assert artifactPassword != null : "fx:id=\"artifactPassword\" was not injected: check your FXML file 'ArtifactPanel.fxml'.";

	}
}
