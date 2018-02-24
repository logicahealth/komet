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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import sh.isaac.api.Get;
import sh.isaac.api.util.AlphanumComparator;
import sh.isaac.dbConfigBuilder.fx.fxUtil.ErrorMarkerUtils;
import sh.isaac.dbConfigBuilder.fx.fxUtil.UpdateableBooleanBinding;
import sh.isaac.dbConfigBuilder.fx.fxUtil.ValidBooleanBinding;
import sh.isaac.pombuilder.VersionFinder;
import sh.isaac.pombuilder.artifacts.IBDFFile;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         GUI controller for the Content Manager UI
 */

public class ContentManagerController
{

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Menu file;

	@FXML
	private MenuItem fileExit;

	@FXML
	private Menu options;

	@FXML
	private MenuItem optionsGitConfig;

	@FXML
	private MenuItem optionsArtifacts;

	@FXML
	private MenuItem optionsMaven;

	@FXML
	private Tab tabSrcUpload;

	@FXML
	private ChoiceBox<String> sourceUploadOperation;

	@FXML
	private ChoiceBox<String> sourceUploadType;

	@FXML
	private TextField sourceUploadVersion;

	@FXML
	private TextField sourceUploadExtension;

	@FXML
	private GridPane sourceUploadFilesGrid;

	@FXML
	private Tab tabSourceConversion;

	@FXML
	private ChoiceBox<String> sourceConversionOperation;

	@FXML
	private TextField sourceConversionConverterVersion;

	@FXML
	private GridPane sourceConversionGrid;

	@FXML
	private TextArea sourceConversionContent;

	@FXML
	private Button sourceConversionContentSelect;

	@FXML
	private Button sourceConversionConverterSelect;

	@FXML
	private Tab tabDatabaseCreation;

	@FXML
	private TextField databaseName;

	@FXML
	private TextField databaseVersion;

	@FXML
	private TextField databaseClassifier;

	@FXML
	private CheckBox databaseOpClassify;

	@FXML
	private ComboBox<String> databaseMetadataVersion;

	@FXML
	private TextArea databaseDescription;

	@FXML
	private ListView<IBDFFile> databaseIbdfList;

	@FXML
	private Button databaseAdd;

	@FXML
	private Button databaseRemove;

	@FXML
	private CheckBox databaseOpCreate;

	@FXML
	private CheckBox databaseOpTag;

	@FXML
	private CheckBox databaseOpInstall;

	@FXML
	private CheckBox databaseOpDeploy;

	@FXML
	private TextField workingFolder;

	@FXML
	private Button workingFolderSelect;

	@FXML
	private CheckBox workingFolderCleanup;

	@FXML
	private GridPane databaseGrid;

	@FXML
	private Button run;

	private ContentManager cm_;
	
	private UpdateableBooleanBinding allRequiredReady_;
	
	private ArrayList<IBDFFile> ibdfFiles = new ArrayList<>();
	
	private ArrayList<ValidBooleanBinding> databaseTabValidityCheckers = new ArrayList<ValidBooleanBinding>();

	@FXML
	void initialize() throws IOException
	{
		assert file != null : "fx:id=\"file\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert fileExit != null : "fx:id=\"fileExit\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert options != null : "fx:id=\"options\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert optionsGitConfig != null : "fx:id=\"optionsGitConfig\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert optionsArtifacts != null : "fx:id=\"optionsArtifacts\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert optionsMaven != null : "fx:id=\"optionsMaven\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert tabSrcUpload != null : "fx:id=\"tabSrcUpload\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceUploadOperation != null : "fx:id=\"sourceUploadOperation\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceUploadType != null : "fx:id=\"sourceUploadType\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceUploadVersion != null : "fx:id=\"sourceUploadVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceUploadExtension != null : "fx:id=\"sourceUploadExtension\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceUploadFilesGrid != null : "fx:id=\"sourceUploadFilesGrid\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert tabSourceConversion != null : "fx:id=\"tabSourceConversion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionOperation != null : "fx:id=\"sourceConversionOperation\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionConverterVersion != null : "fx:id=\"sourceConversionConverterVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionGrid != null : "fx:id=\"sourceConversionGrid\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionContent != null : "fx:id=\"sourceConversionContent\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionContentSelect != null : "fx:id=\"sourceConversionContentSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionConverterSelect != null : "fx:id=\"sourceConversionConverterSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert tabDatabaseCreation != null : "fx:id=\"tabDatabaseCreation\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseName != null : "fx:id=\"databaseName\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseVersion != null : "fx:id=\"databaseVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseClassifier != null : "fx:id=\"databaseClassifier\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseOpClassify != null : "fx:id=\"databaseClassify\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseMetadataVersion != null : "fx:id=\"databaseMetadataVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseDescription != null : "fx:id=\"databaseDescription\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseIbdfList != null : "fx:id=\"databaseIbdfList\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseAdd != null : "fx:id=\"databaseAdd\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseRemove != null : "fx:id=\"databaseRemove\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseOpCreate != null : "fx:id=\"databaseOpCreate\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseOpTag != null : "fx:id=\"databaseOpTag\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseOpInstall != null : "fx:id=\"databaseOpInstall\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseOpDeploy != null : "fx:id=\"databaseOpDeploy\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolder != null : "fx:id=\"workingFolder\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolderSelect != null : "fx:id=\"workingFolderSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolderCleanup != null : "fx:id=\"workingFolderCleanup\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert run != null : "fx:id=\"run\" was not injected: check your FXML file 'ContentManager.fxml'.";

		tabDatabaseCreation.getTabPane().getSelectionModel().select(tabDatabaseCreation);

		ErrorMarkerUtils.setupDisabledInfoMarker(databaseOpCreate, new SimpleStringProperty("Creation of the maven project (and POM file) is always required"),
				true);
		databaseTabValidityCheckers.add(ErrorMarkerUtils.setupErrorMarker(databaseName, (s) -> (StringUtils.isBlank(s) ? "The database name must be specified" : ""), true));
		databaseTabValidityCheckers.add(ErrorMarkerUtils.setupErrorMarker(databaseVersion, (s) -> (StringUtils.isBlank(s) ? "The database version must be specified" : ""), true));
		databaseTabValidityCheckers.add(ErrorMarkerUtils.setupErrorMarker(databaseDescription, (s) -> (StringUtils.isBlank(s) ? "The database description must be specified" : ""), true));

		workingFolder.setText(Files.createTempDirectory("contentManager").toFile().getAbsolutePath());
		workingFolderSelect.setOnAction((actionEvent) -> {
			DirectoryChooser fc = new DirectoryChooser();
			fc.setTitle("Select the folder where the project will be created");
			File f = fc.showDialog(cm_.getPrimaryStage().getScene().getWindow());
			if (f != null)
			{
				workingFolder.setText(f.getAbsolutePath());
			}
		});
		
		databaseAdd.setOnAction(action -> 
		{
			if (ibdfFiles.size() == 0)
			{
				readAvailableIBDFFiles();
			}
			System.out.println("foo");
		});
		
		databaseIbdfList.setItems(FXCollections.observableArrayList());
		
		BooleanBinding ibdfItemSelected = new BooleanBinding()
		{
			{
				bind(databaseIbdfList.getSelectionModel().getSelectedIndices());
			}
			@Override
			protected boolean computeValue()
			{
				return databaseIbdfList.getSelectionModel().getSelectedItems().size() > 0;
			}
		};
		
		databaseRemove.disableProperty().bind(ibdfItemSelected.not());
		databaseRemove.setOnAction((actionEvent) -> {
			for (IBDFFile f : databaseIbdfList.getSelectionModel().getSelectedItems())
			{
				databaseIbdfList.getItems().remove(f);
			}
		});
		
		
		allRequiredReady_ = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				for (ValidBooleanBinding vbb: ContentManagerController.this.databaseTabValidityCheckers)
				{
					addBinding(vbb);
				}
			}
			@Override
			protected boolean computeValue()
			{
				setInvalidReason(getInvalidReasonFromAllBindings());
				return allBindingsValid();
			}
		};
		
		run.disableProperty().bind(allRequiredReady_.not());
	}

	/**
	 * @param contentManager
	 */
	public void finishSetup(ContentManager contentManager)
	{
		cm_ = contentManager;
		fileExit.setOnAction((action) -> cm_.shutdown());
		
		final TreeSet<String> metadataVersions = new TreeSet<>(new AlphanumComparator(true));
		metadataVersions.add(VersionFinder.findProjectVersion(true));
		metadataVersions.add(VersionFinder.findProjectVersion(false));
		
		
		//TODO read local m2 / read remote
		
		Platform.runLater(() -> {
			databaseMetadataVersion.getItems().addAll(metadataVersions);
			databaseMetadataVersion.getSelectionModel().select(metadataVersions.size() - 1);
		});
	}
	
	private void readAvailableIBDFFiles()
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				Thread.sleep(5000);
				Platform.runLater(() -> databaseIbdfList.getItems().add(new IBDFFile("a", "b", "c")));;
				//TODO actually read files... show new dialog
				return null;
			}};
		
		Get.workExecutors().getExecutor().execute(t);
		ProgressDialog pd = new ProgressDialog(t);
		pd.setTitle("Reading IBDF Files");
		pd.setHeaderText(null);
		pd.setContentText("Reading available IBDF Files");
	}
}
