/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
package sh.isaac.komet.gui.assemblageviewer;

import java.io.IOException;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import sh.isaac.komet.gui.semanticViewer.SemanticViewer;
import sh.komet.gui.util.FxGet;

/**
 * {@link AssemblageViewer}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class AssemblageViewer
{
	AssemblageViewerController drlvc_;
	private final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private AssemblageViewer()
	{
		// created by HK2
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
	}
	
	public void showView(Window parent)
	{
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.initOwner(parent);
		
		BorderPane root = new BorderPane();
		
		Label title = new Label("Assemblage Viewer");
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPadding(new Insets(5, 5, 5, 5));
		root.setTop(title);
		root.setCenter(getView());
		
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Assemblage Viewer");
		stage.getScene().getStylesheets().add(SemanticViewer.class.getResource("/css/isaac-shared-styles.css").toString());
		stage.setWidth(800);
		stage.setHeight(600);
		stage.onHiddenProperty().set((eventHandler) ->
		{
			stage.setScene(null);
		});
		stage.show();
	}

	public Region getView()
	{
		if (drlvc_ == null)
		{
			try
			{
				drlvc_ = AssemblageViewerController.construct();
			}
			catch (IOException e)
			{
				LoggerFactory.getLogger(this.getClass()).error("Unexpected error initing AssemblageViewer", e);
				FxGet.dialogs().showErrorDialog("Unexpected error creating AssemblageViewer", e);
				return new Label("Unexpected error initializing view, see log file");
			}

		}
		return drlvc_.getRoot();
	}
}
