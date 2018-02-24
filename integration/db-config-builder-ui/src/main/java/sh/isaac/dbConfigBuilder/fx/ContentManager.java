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

import java.io.IOException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sh.isaac.api.LookupService;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         A GUI for uploading, creating / running content converters, and building databases for ISAAC.
 */
public class ContentManager extends Application
{

	private static Logger log = LogManager.getLogger();
	private Stage primaryStage_;
	private ContentManagerController cmc_;

	public static void main(String[] args) throws ClassNotFoundException, IOException
	{
		LookupService.setRunLevel(LookupService.SL_NEG_1_WORKERS_STARTED_RUNLEVEL);
		launch(args);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		URL resource = ContentManager.class.getResource("ContentManager.fxml");
		log.debug("FXML for " + ContentManager.class + ": " + resource);
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		cmc_ = loader.getController();
		primaryStage.setTitle("ISAAC ContentManager");
		primaryStage.setScene(new Scene(loader.getRoot()));
		primaryStage.setWidth(1280);
		primaryStage.setHeight(1024);
		primaryStage.getIcons().add(Images.PACKAGE.getImage());

		// Handle window close event.
		primaryStage.setOnHiding(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				shutdown();
			}
		});

		cmc_.finishSetup(this);
		
		primaryStage.show();
		primaryStage_ = primaryStage;
	}

	protected void shutdown()
	{
		log.info("Shutting down");
		if (primaryStage_.isShowing())
		{
			primaryStage_.hide();
		}
		LookupService.shutdownSystem();
		Platform.exit();
	}

	public Stage getPrimaryStage()
	{
		return primaryStage_;
	}
}
