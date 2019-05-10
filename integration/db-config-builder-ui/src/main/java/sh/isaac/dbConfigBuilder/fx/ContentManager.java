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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sh.isaac.api.LookupService;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.komet.iconography.IconographyHelper;

/**
 * A GUI for uploading, creating / running content converters, and building databases for ISAAC.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ContentManager extends Application
{
	private static Logger log = LogManager.getLogger();
	private Stage primaryStage_;
	private ContentManagerController cmc_;

	private File prefsFileStorage_;
	protected StoredPrefs sp_;

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
		primaryStage.getScene().getStylesheets().add(IconographyHelper.getStyleSheetStringUrl());
		primaryStage.getScene().getStylesheets().add(ContentManager.class.getResource("/contentManager.css").toString());

		primaryStage.show();
		primaryStage_ = primaryStage;

		prefsFileStorage_ = new File(new File(System.getProperty("user.home")), "ContentManagerPrefs.json").getAbsoluteFile();
		prefsFileStorage_.getParentFile().mkdirs();

		char[] pw = promptPassword();

		if (prefsFileStorage_.isFile())
		{
			while (true)
			{
				try
				{
					sp_ = StoredPrefs.readStoredPrefs(prefsFileStorage_, pw, false);
					break;
				}
				catch (IllegalArgumentException e)
				{
					// Invalid password
					List<String> choices = new ArrayList<>();
					choices.add("Reenter password");
					choices.add("Use provided password, reset encrypted values");
					choices.add("Exit");

					ChoiceDialog<String> alert = new ChoiceDialog<>(choices.get(0), choices);
					alert.setTitle("Incorrect password");
					alert.setHeaderText("The provided password is incorrect.  ");
					alert.setContentText("Please choose how to proceed");

					Optional<String> result = alert.showAndWait();
					if (result.isPresent())
					{
						if (result.get().equals(choices.get(0)))
						{
							pw = promptPassword();
							// loop and retry...
						}
						else if (result.get().equals(choices.get(1)))
						{
							// passing true for the final value makes it just use the new password, blanking encrypted fields
							sp_ = StoredPrefs.readStoredPrefs(prefsFileStorage_, pw, true);
							break;
						}
						else if (result.get().equals(choices.get(2)))
						{
							shutdown();
							return;
						}
					}
					else
					{
						//cancel button
						shutdown();
						return;
					}
				}
				catch (Exception e)
				{
					log.error("Unexpected error reading stored prefs", e);
					shutdown();
					return;
				}
			}
		}
		else
		{
			sp_ = new StoredPrefs(pw);
		}

		cmc_.readData(sp_);
	}

	private char[] promptPassword()
	{
		TextInputDialog tid = new TextInputDialog();
		tid.setTitle("Password Decryption Password");
		tid.setHeaderText(null);
		tid.setContentText("The password to decrypt stored passwords");
		// These are from the Password Field class
		tid.getEditor().getStyleClass().add("password-field");
		tid.getEditor().setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
		Tooltip.install(tid.getEditor(), new Tooltip("This password is used to encrypt and decrypt the git and artifact repository passwords."));
		tid.initOwner(primaryStage_.getOwner());

		return (tid.showAndWait().orElse("")).toCharArray();
	}
	
	public void storePrefsFile() throws FileNotFoundException
	{
		sp_.store(prefsFileStorage_);
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
