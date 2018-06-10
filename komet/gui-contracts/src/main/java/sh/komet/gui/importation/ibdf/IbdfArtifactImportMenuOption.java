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
package sh.komet.gui.importation.ibdf;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.stage.Window;
import sh.isaac.api.Get;
import sh.isaac.api.task.TimedTask;
import sh.isaac.api.util.StringUtils;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.mojo.LoadTermstore;
import sh.komet.gui.contract.AppMenu;
import sh.komet.gui.contract.MenuProvider;
import sh.komet.gui.util.FxUtils;

/**
 * The menu item that allows you to import IBDF files into the enviornment.
 * 
 * {@link IbdfArtifactImportMenuOption}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class IbdfArtifactImportMenuOption implements MenuProvider
{

	private final Logger LOG = LogManager.getLogger();
	private final StoredPrefs storedPrefs = new StoredPrefs("".toCharArray());

	private Window window_;

	private IbdfArtifactImportMenuOption()
	{
		// created by HK2
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", 0);
		// TODO tie this to a real StoredPrefs in the GUI. For now, just a default, so we can at least read a local .m2 folder
		// make this system property read go away
		String temp = System.getProperty("M2_PATH");
		if (StringUtils.isNotBlank(temp))
		{
			this.storedPrefs.setLocalM2FolderPath(temp);
		}
	}

	private void ibdfImport(boolean delta)
	{
		try
		{
			ArrayList<IBDFFile> ibdfSourceFiles_ = new ArrayList<>();

			ListView<IBDFFile> ibdfPicker = new ListView<>();

			FxUtils.waitWithProgress("Reading IBDF Files", "Reading available IBDF Files", MavenArtifactUtils.readAvailableIBDFFiles(delta, storedPrefs, (results) -> {
				ibdfSourceFiles_.clear();
				// TODO tie this to some sort of dynamic thing about what types are supported by the direct importer...
				for (IBDFFile sdo : results)
				{
					ibdfSourceFiles_.add(sdo);
				}

			}), window_);

			ibdfPicker.setItems(FXCollections.observableArrayList(ibdfSourceFiles_));

			ibdfPicker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			ibdfPicker.setCellFactory(param -> new ListCell<IBDFFile>()
			{
				@Override
				protected void updateItem(IBDFFile item, boolean empty)
				{
					super.updateItem(item, empty);

					if (empty || item == null)
					{
						setText(null);
					}
					else
					{
						setText(item.getArtifactId() + (item.hasClassifier() ? " : " + item.getClassifier() : "") + " : " + item.getVersion());
					}
				}
			});

			Alert ibdfDialog = new Alert(AlertType.CONFIRMATION);
			ibdfDialog.setTitle("Select Files");
			ibdfDialog.setHeaderText("Select 1 or more IBDF Files to import");
			ibdfDialog.getDialogPane().setContent(ibdfPicker);
			ibdfPicker.setPrefWidth(1024);
			ibdfDialog.initOwner(window_);

			ArrayList<File> filesToLoad = new ArrayList<>();

			if (ibdfDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				for (IBDFFile ibdf : ibdfPicker.getSelectionModel().getSelectedItems())
				{
					Optional<File> local = ibdf.getLocalPath(storedPrefs);
					if (local.isPresent())
					{
						filesToLoad.add(local.get());
					}
				}
			}
			
			if (filesToLoad.size() > 0)
			{
				
				TimedTask<Void> tt = new TimedTask<Void>()
				{
					@Override
					protected Void call() throws Exception
					{
						updateTitle("Loading IBDF Content");
						updateProgress(-1, Long.MAX_VALUE);
						Get.activeTasks().add(this);
						ArrayList<ZipFile> zipFiles = new ArrayList<>();
						
						try 
						{
							//Need to open the artifact zip file, and get the stream for the IBDF file(s).
							InputStream[] inputStreams = new InputStream[filesToLoad.size()];
							for (int i = 0; i < inputStreams.length; i++)
							{
								ZipFile zf = new ZipFile(filesToLoad.get(i), Charset.forName("UTF-8"));
								zipFiles.add(zf);
								Enumeration<? extends ZipEntry> zipEntries = zf.entries();
								while (zipEntries.hasMoreElements())
								{
									ZipEntry ze = zipEntries.nextElement();
									if (ze.getName().toLowerCase().endsWith(".ibdf")) {
										inputStreams[i] = zf.getInputStream(ze);
										break;
									}
								}
								if (inputStreams[i] == null) {
									throw new RuntimeException("Didn't find .ibdf file in zipped artifact");
								}
							}
							
							LoadTermstore lt = new LoadTermstore(inputStreams, true, true);
							lt.dontSetDBMode();
							lt.execute();
							Get.taxonomyService().notifyTaxonomyListenersToRefresh();
							Platform.runLater(() -> 
							{
								Alert ibdfDialog = new Alert(AlertType.INFORMATION);
								ibdfDialog.setTitle("IBDF Import complete");
								ibdfDialog.setHeaderText("IBDF Import complete");
								ibdfPicker.setPrefWidth(400);
								ibdfDialog.initOwner(window_);
								ibdfDialog.showAndWait();
							});
							return null;
						} 
						finally 
						{
							Get.activeTasks().remove(this);
							for (ZipFile zf : zipFiles) {
								zf.close();
							}
						}
					}
				};
				
				Get.workExecutors().getExecutor().execute(tt);
			}
		}
		catch (Exception e)
		{
			LOG.error("Unexpected!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EnumSet<AppMenu> getParentMenus()
	{
		return EnumSet.of(AppMenu.TOOLS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MenuItem[] getMenuItems(AppMenu appMenu, Window window)
	{
		if (appMenu == AppMenu.TOOLS)
		{
			this.window_ = window;
			MenuItem miImport = new MenuItem("IBDF Artifact Import");
			miImport.setOnAction(event -> {
				ibdfImport(false);
			});
			
			MenuItem miDeltaImport = new MenuItem("IBDF Delta Artifact Import");
			miDeltaImport.setOnAction(event -> {
				ibdfImport(true);
			});
			return new MenuItem[] { miImport, miDeltaImport };
		}
		return new MenuItem[] {};
	}
}
