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
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.cli.MavenCli;
import org.controlsfx.dialog.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import sh.isaac.api.Get;
import sh.isaac.api.util.AlphanumComparator;
import sh.isaac.api.util.DeployFile;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.dbConfigBuilder.fx.fxUtil.ErrorMarkerUtils;
import sh.isaac.dbConfigBuilder.fx.fxUtil.StreamRedirect;
import sh.isaac.dbConfigBuilder.fx.fxUtil.UpdateableBooleanBinding;
import sh.isaac.dbConfigBuilder.fx.fxUtil.ValidBooleanBinding;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.dbConfigBuilder.rest.query.NexusRead;
import sh.isaac.pombuilder.VersionFinder;
import sh.isaac.pombuilder.artifacts.IBDFFile;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.pombuilder.converter.UploadFileInfo;
import sh.isaac.pombuilder.dbbuilder.DBConfigurationCreator;
import sh.isaac.pombuilder.upload.SrcUploadCreator;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 *         GUI controller for the Content Manager UI
 */

public class ContentManagerController
{
	private static Logger log = LogManager.getLogger();

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
	private ChoiceBox<SupportedConverterTypes> sourceUploadType;
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
	private CheckBox opClassify;
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
	private CheckBox opCreate;
	@FXML
	private CheckBox opTag;
	@FXML
	private CheckBox opInstall;
	@FXML
	private CheckBox opPackage;
	@FXML
	private CheckBox opDeploy;
	@FXML
	private CheckBox opDirectDeploy;
	@FXML
	private TextField workingFolder;
	@FXML
	private Button workingFolderSelect;
	@FXML
	private CheckBox workingFolderCleanup;
	@FXML
	private GridPane databaseGrid;
	@FXML
	private VBox sourceUploadFilesVBox;
	@FXML
	private TextArea sourceUploadFileDetails;
	@FXML
	private Button run;
	@FXML 
	Tooltip sourceUploadVersionTooltip;

	private ContentManager cm_;

	private UpdateableBooleanBinding allRequiredReady_;
	private ArrayList<IBDFFile> ibdfFiles_ = new ArrayList<>();
	private ArrayList<ValidBooleanBinding> databaseTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private ArrayList<ValidBooleanBinding> sourceUploadTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private UpdateableBooleanBinding allSourceUploadFilesPresent_;
	private ArrayList<TextField> sourceUploadFiles_ = new ArrayList<>();

	private StoredPrefs sp_;

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
		assert opClassify != null : "fx:id=\"databaseClassify\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseMetadataVersion != null : "fx:id=\"databaseMetadataVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseDescription != null : "fx:id=\"databaseDescription\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseIbdfList != null : "fx:id=\"databaseIbdfList\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseAdd != null : "fx:id=\"databaseAdd\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseRemove != null : "fx:id=\"databaseRemove\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opCreate != null : "fx:id=\"opCreate\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opTag != null : "fx:id=\"opTag\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opPackage != null : "fx:id=\"opPackage\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opInstall != null : "fx:id=\"opInstall\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opDeploy != null : "fx:id=\"opDeploy\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opDirectDeploy != null : "fx:id=\"opDirectDeploy\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolder != null : "fx:id=\"workingFolder\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolderSelect != null : "fx:id=\"workingFolderSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert workingFolderCleanup != null : "fx:id=\"workingFolderCleanup\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert run != null : "fx:id=\"run\" was not injected: check your FXML file 'ContentManager.fxml'.";

		tabDatabaseCreation.getTabPane().getSelectionModel().select(tabDatabaseCreation);

		// shared components
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

		// database creation tab
		ErrorMarkerUtils.setupDisabledInfoMarker(opCreate, new SimpleStringProperty("Creation of the maven project (and POM file) is always required"), true);
		databaseTabValidityCheckers_
				.add(ErrorMarkerUtils.setupErrorMarker(databaseName, (s) -> (StringUtils.isBlank(s) ? "The database name must be specified" : ""), true));
		databaseTabValidityCheckers_
				.add(ErrorMarkerUtils.setupErrorMarker(databaseVersion, (s) -> (StringUtils.isBlank(s) ? "The database version must be specified" : ""), true));
		databaseTabValidityCheckers_.add(ErrorMarkerUtils.setupErrorMarker(databaseDescription,
				(s) -> (StringUtils.isBlank(s) ? "The database description must be specified" : ""), true));

		databaseAdd.setOnAction(action -> {
			if (ibdfFiles_.size() == 0)
			{
				readAvailableIBDFFiles();
			}

			ListView<IBDFFile> ibdfPicker = new ListView<>();
			ibdfPicker.setItems(FXCollections.observableArrayList(ibdfFiles_));
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
			ibdfDialog.setHeaderText("Select 1 or more IBDF Files to add");
			ibdfDialog.getDialogPane().setContent(ibdfPicker);
			ibdfPicker.setPrefWidth(1024);

			if (ibdfDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				databaseIbdfList.getItems().addAll(ibdfPicker.getSelectionModel().getSelectedItems());
			}
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

		databaseIbdfList.setCellFactory(param -> new ListCell<IBDFFile>()
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

		databaseRemove.disableProperty().bind(ibdfItemSelected.not());
		databaseRemove.setOnAction((actionEvent) -> {
			for (IBDFFile f : databaseIbdfList.getSelectionModel().getSelectedItems())
			{
				databaseIbdfList.getItems().remove(f);
			}
		});

		ValidBooleanBinding databaseIbdfListPopulated = new ValidBooleanBinding()
		{
			{
				bind(databaseIbdfList.getItems());
			}

			@Override
			protected boolean computeValue()
			{
				if (databaseIbdfList.getItems().size() == 0)
				{
					this.setInvalidReason("At least one IBDF file must be selected");
					return false;
				}
				else
				{
					this.clearInvalidReason();
					return true;
				}
			}
		};

		ErrorMarkerUtils.setupErrorMarker(databaseIbdfList, databaseIbdfListPopulated, true);

		databaseTabValidityCheckers_.add(databaseIbdfListPopulated);

		ValidBooleanBinding databaseIbdfListDependenciesHappy = new ValidBooleanBinding()
		{
			{
				bind(databaseIbdfList.getItems());
			}

			@Override
			protected boolean computeValue()
			{
				for (IBDFFile ibdfFile : databaseIbdfList.getItems())
				{
					// Find the matching SupportedContentType, and see if this conversion has any required IBDF files. If so,
					// we need to have that IBDF File in our list.
					SupportedConverterTypes sc = SupportedConverterTypes.findByIBDFArtifactId(ibdfFile.getArtifactId());
					boolean found = false;
					for (String requiredIbdfArtifactId : sc.getIBDFDependencies())
					{
						for (IBDFFile ibdfFileNested : databaseIbdfList.getItems())
						{
							if (ibdfFileNested.getArtifactId().equals(requiredIbdfArtifactId))
							{
								found = true;
								break;
							}
						}
						if (!found)
						{
							this.setInvalidReason("The IBDF file " + ibdfFile.getArtifactId() + " has a dependency on " + requiredIbdfArtifactId
									+ ".  You must add an IBDF file that matches that artifact type to build the database.");
							return false;
						}
					}
				}
				this.clearInvalidReason();
				return true;
			}
		};

		databaseTabValidityCheckers_.add(databaseIbdfListDependenciesHappy);
		opClassify.visibleProperty().bind(tabDatabaseCreation.selectedProperty());

		// source upload tab

		sourceUploadType.setConverter(new StringConverter<SupportedConverterTypes>()
		{

			@Override
			public String toString(SupportedConverterTypes object)
			{
				return object.getNiceName();
			}

			@Override
			public SupportedConverterTypes fromString(String string)
			{
				for (SupportedConverterTypes s : SupportedConverterTypes.values())
				{
					if (s.getNiceName().equals(string))
					{
						return s;
					}
				}
				throw new RuntimeException("Improbable");
			}
		});
		for (SupportedConverterTypes s : SupportedConverterTypes.values())
		{
			sourceUploadType.getItems().add(s);
		}
		sourceUploadType.getItems().sort(new java.util.Comparator<SupportedConverterTypes>()
		{
			@Override
			public int compare(SupportedConverterTypes o1, SupportedConverterTypes o2)
			{
				return o1.getNiceName().compareTo(o2.getNiceName());
			}
		});
		
		ValidBooleanBinding versionValid = new ValidBooleanBinding()
		{
			{
				bind(sourceUploadVersion.textProperty());
			}
			@Override
			protected boolean computeValue()
			{
				if (StringUtils.isBlank(sourceUploadVersion.getText()))
				{
					setInvalidReason("A version is required");
					return false;
				}
				else if (StringUtils.isNotBlank(sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionRegExpValidator()))
				{
					if (!Pattern.matches(sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionRegExpValidator(), sourceUploadVersion.getText()))
					{
						setInvalidReason("The version needs to match the regular expression " 
								+ sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionRegExpValidator());
						return false;
					}
				}
				clearInvalidReason();
				return true;
			}
		};
		sourceUploadTabValidityCheckers_.add(versionValid);
		ErrorMarkerUtils.setupErrorMarker(sourceUploadVersion, versionValid, true);

		allSourceUploadFilesPresent_ = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				setInvalidReason(getInvalidReasonFromAllBindings());
				return allBindingsValid();
			}
		};
		
		sourceUploadType.getSelectionModel().select(0);
		
		ValidBooleanBinding extensionErrorMarker = ErrorMarkerUtils.setupErrorMarker(sourceUploadExtension,
				((input) -> StringUtils.isBlank(input) && sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId().contains("*")
						? "The extension type is required"
						: ""),
				true);

		sourceUploadTabValidityCheckers_.add(extensionErrorMarker);

		sourceUploadExtension.disableProperty().bind(new BooleanBinding()
		{
			{
				bind(sourceUploadType.getSelectionModel().selectedItemProperty());
			}

			@Override
			protected boolean computeValue()
			{
				// side jobs - hack rather than adding another change listener
				extensionErrorMarker.invalidate();
				sourceUploadVersionTooltip.setText(sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionDescription());
				updateSourceUploadFiles();

				boolean needsExtension = sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId().contains("*");
				if (!needsExtension)
				{
					sourceUploadExtension.setText("");
				}
				return !needsExtension;
			}
		});

		sourceUploadTabValidityCheckers_.add(new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				bind(allSourceUploadFilesPresent_, opPackage.selectedProperty(), opInstall.selectedProperty(), opDeploy.selectedProperty(),
						opDirectDeploy.selectedProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (!allSourceUploadFilesPresent_.get() && (opPackage.isSelected() || opInstall.isSelected() || opDeploy.isSelected() || opDirectDeploy.isSelected()))
				{
					setInvalidReason("All required source files must be specified prior to doing the selected operations");
					return false;
				}
				else
				{
					clearInvalidReason();
					return true;
				}
			}
		});
		

		

		// source conversion tab

		// Shared bottom section
		allRequiredReady_ = new UpdateableBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				for (ValidBooleanBinding vbb : ContentManagerController.this.databaseTabValidityCheckers_)
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

		tabDatabaseCreation.getTabPane().getSelectionModel().selectedItemProperty().addListener((change) -> {
			// align the validators to the tab
			allRequiredReady_.clearBindings();
			ArrayList<ValidBooleanBinding> temp = new ArrayList<>();

			if (tabDatabaseCreation.getTabPane().getSelectionModel().getSelectedItem() == tabDatabaseCreation)
			{
				temp = ContentManagerController.this.databaseTabValidityCheckers_;
			}
			else if (tabDatabaseCreation.getTabPane().getSelectionModel().getSelectedItem() == tabSourceConversion)
			{

			}
			else if (tabDatabaseCreation.getTabPane().getSelectionModel().getSelectedItem() == tabSrcUpload)
			{
				temp = ContentManagerController.this.sourceUploadTabValidityCheckers_;
			}
			else
			{
				log.error("unexpected tab selection");
			}
			for (ValidBooleanBinding vbb : temp)
			{
				allRequiredReady_.addBinding(vbb);
			}
		});

		opDirectDeploy.setOnAction((action) -> {
			if (opDirectDeploy.isSelected())
			{
				opDeploy.setSelected(false);
			}
		});

		opDeploy.setOnAction((action) -> {
			if (opDeploy.isSelected())
			{
				opDirectDeploy.setSelected(false);
			}
		});

		// run button markers - initially tied to DB tab
		run.disableProperty().bind(allRequiredReady_.not());
		ErrorMarkerUtils.setupDisabledInfoMarker(run, allRequiredReady_.getReasonWhyInvalid(), true);

		run.setOnAction((action) -> {
			if (tabDatabaseCreation.isSelected())
			{
				createDatabase();
			}
			else if (tabSourceConversion.isSelected())
			{
				// TODO implement converter create
			}
			else if (tabSrcUpload.isSelected())
			{
				createSourceUpload();
			}
			else
			{
				log.error("unexpected tab selection");
			}
		});

		
	}

	private void updateSourceUploadFiles()
	{
		sourceUploadFiles_.clear();
		sourceUploadFilesVBox.getChildren().clear();
		allSourceUploadFilesPresent_.clearBindings();
		
		final SupportedConverterTypes type = sourceUploadType.getSelectionModel().getSelectedItem();
		
		int row = 0;
		for (UploadFileInfo ufi : type.getUploadFileInfo())
		{
			final int finalRow = row;
			HBox hbox = new HBox();
			hbox.setSpacing(5.0);
			hbox.setPadding(new Insets(5.0));
			TextField fileName = new TextField();
			fileName.setMaxWidth(Double.MAX_VALUE);
			fileName.focusedProperty().addListener((change) -> 
			{
				if (fileName.focusedProperty().get())
				{
					updateDescription(finalRow);
				}
			});
			
			hbox.getChildren().add(fileName);
			HBox.setHgrow(fileName, Priority.ALWAYS);
			sourceUploadFiles_.add(fileName);
			ErrorMarkerUtils.setupErrorMarker(fileName, ((input) -> 
			{
				if (StringUtils.isBlank(fileName.getText()))
				{
					return ufi.fileIsRequired() ? "This file is required" : "";
				}
				else if (StringUtils.isNotBlank(ufi.getExpectedNamingPatternRegExpPattern()))
				{
					File f = new File(fileName.getText());
					boolean matches =  Pattern.matches(ufi.getExpectedNamingPatternRegExpPattern(), f.getName().toLowerCase());
					
					if (!matches)
					{
						return "The specified file does not match the expected pattern"; 
					}
					if (!f.exists())
					{
						return "The specified file does not exist";
					}
				}
				return "";
			}), true);
			
			allSourceUploadFilesPresent_.addBinding(new ValidBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
					bind(fileName.textProperty());
				}
				@Override
				protected boolean computeValue()
				{
					if (!ufi.fileIsRequired() || (fileName.getText().length() > 0 && new File(fileName.getText()).exists()))
					{
						clearInvalidReason();
						return true;
					}
					setInvalidReason("This file is required");
					return false;
				}
			});
			
			Button fileButton = new Button("...");
			
			hbox.getChildren().add(fileButton);
			fileButton.setOnAction((actionEvent) -> {
				updateDescription(finalRow);
				FileChooser fc = new FileChooser();
				fc.setTitle("Select one or more files");
				List<File> f = fc.showOpenMultipleDialog(cm_.getPrimaryStage().getScene().getWindow());
				if (f != null && f.size() > 0)
				{
					if (f.size() == 1) 
					{
						try
						{
							fileName.setText(f.get(0).getCanonicalPath());
						}
						catch (IOException e)
						{
							log.error("unexpected", e);
						}
					}
					else
					{
						for (File multiFile : f)
						{
							int index = 0;
							boolean matched = false;
							for (UploadFileInfo ufiLambda : type.getUploadFileInfo())
							{
								if (StringUtils.isNotBlank(ufiLambda.getExpectedNamingPatternRegExpPattern()) 
										&& Pattern.matches(ufiLambda.getExpectedNamingPatternRegExpPattern(), multiFile.getName().toLowerCase()) 
										&& sourceUploadFiles_.get(index).getText().isEmpty())
								{
									try
									{
										sourceUploadFiles_.get(index).setText(multiFile.getCanonicalPath());
										matched = true;
									}
									catch (IOException e)
									{
										log.error("improbable", e);
									}
									break;
								}
								index++;
							}
							
							if (!matched)
							{
								HBox hboxLambda = new HBox();
								hboxLambda.setSpacing(5.0);
								hboxLambda.setPadding(new Insets(5.0));
								TextField fileNameLambda = new TextField();
								fileNameLambda.setMaxWidth(Double.MAX_VALUE);
								fileNameLambda.focusedProperty().addListener((change) -> 
								{
									if (fileNameLambda.focusedProperty().get())
									{
										updateDescription(-1);
									}
								});
								
								hboxLambda.getChildren().add(fileNameLambda);
								HBox.setHgrow(fileNameLambda, Priority.ALWAYS);
								sourceUploadFilesVBox.getChildren().add(hboxLambda);
								sourceUploadFiles_.add(fileNameLambda);
								try
								{
									fileNameLambda.setText(multiFile.getCanonicalPath());
								}
								catch (IOException e)
								{
									log.error("improbable", e);
								}
							}
						}
					}
				}
			});
			
			row++;
			sourceUploadFilesVBox.getChildren().add(hbox);
		}
		if (sourceUploadFiles_.size() > 0)
		{
			sourceUploadFiles_.get(0).requestFocus();
		}
		
	}

	/**
	 * @param finalRow
	 */
	private void updateDescription(int finalRow)
	{
		sourceUploadFileDetails.clear();
		if (finalRow == -1)
		{
			sourceUploadFileDetails.appendText("User specified file that doesn't match any requirement");
			return;
		}
		SupportedConverterTypes type = sourceUploadType.getSelectionModel().getSelectedItem();
		UploadFileInfo ui = type.getUploadFileInfo().get(finalRow);
		
		if (StringUtils.isNotBlank(ui.getSampleName()))
		{
			sourceUploadFileDetails.appendText("Sample Name\n");
			sourceUploadFileDetails.appendText(ui.getSampleName() + "\n\n");
		}
		if (StringUtils.isNotBlank(ui.getExpectedNamingPatternDescription()))
		{
			sourceUploadFileDetails.appendText(ui.getExpectedNamingPatternDescription() + "\n\n");
		}
		
		if (StringUtils.isNotBlank(ui.getExpectedNamingPatternRegExpPattern()))
		{
			sourceUploadFileDetails.appendText("Regular Expression for File Name\n");
			sourceUploadFileDetails.appendText(ui.getExpectedNamingPatternRegExpPattern() + "\n\n");
		}
		
		if (StringUtils.isNotBlank(ui.getSuggestedSourceLocation()))
		{
			sourceUploadFileDetails.appendText("Suggested Source Location\n");
			sourceUploadFileDetails.appendText(ui.getSuggestedSourceLocation() + "\n\n");
		}
		
		if (StringUtils.isNotBlank(ui.getSuggestedSourceURL()))
		{
			sourceUploadFileDetails.appendText("Suggested Source URL\n");
			sourceUploadFileDetails.appendText(ui.getSuggestedSourceURL() + "\n\n");
		}
	}

	private void createSourceUpload()
	{
		doRun(() -> 
		{
			ArrayList<File> filesToUpload = new ArrayList<File>();
			for (TextField tf : sourceUploadFiles_)
			{
				if (StringUtils.isNotBlank(tf.getText()))
				{
					File f = new File(tf.getText());
					if (f.isFile())
					{
						filesToUpload.add(f);
					}
				}
			}
			
			try
			{
				Task<String> task = SrcUploadCreator.createSrcUploadConfiguration(sourceUploadType.getSelectionModel().getSelectedItem(), sourceUploadVersion.getText(), 
						sourceUploadExtension.getText(), filesToUpload,  (opTag.isSelected() ? sp_.getGitURL() : null), sp_.getGitUsername(),
								sp_.getGitPassword(), null, null, null,  //don't pass the artifact info, otherwise, it will try to zip and direct deploy
										new File(workingFolder.getText()), false, false);
				Get.workExecutors().getExecutor().execute(task);
				return task.get();
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}, 
		() -> 
		{
			try
			{
				ArrayList<DeployFile> temp = new ArrayList<>();
				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId(), sourceUploadVersion.getText(), 
						"", "pom", new File(new File(workingFolder.getText()), SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/pom.xml"),
						sp_.getArtifactDeployURL(), sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				
				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId(), sourceUploadVersion.getText(),
						"", "zip",
						new File(new File(workingFolder.getText()),
								SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/target/" + sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId() 
								+ "-" + sourceUploadVersion.getText() + ".zip"),
						sp_.getArtifactDeployURL(), sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			
		},
		new File(workingFolder.getText() + "/" + SrcUploadCreator.WORKING_SUB_FOLDER_NAME));
	}
	
	
	private void createDatabase()
	{
		doRun(() -> 
		{
			try
			{
				return DBConfigurationCreator.createDBConfiguration(databaseName.getText(), databaseVersion.getText(), databaseDescription.getText(),
						databaseClassifier.getText(), opClassify.isSelected(),
						databaseIbdfList.getItems().toArray(new IBDFFile[databaseIbdfList.getItems().size()]),
						databaseMetadataVersion.getSelectionModel().getSelectedItem(), opTag.isSelected() ? sp_.getGitURL() : null, sp_.getGitUsername(),
						sp_.getGitPassword(), new File(workingFolder.getText()),
						false);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}, 
		() -> 
		{
			try
			{
				ArrayList<DeployFile> temp = new ArrayList<>();
				temp.add(new DeployFile(DBConfigurationCreator.groupId, databaseName.getText(), databaseVersion.getText(), "",
						"pom", new File(new File(workingFolder.getText()), DBConfigurationCreator.parentArtifactId + "/pom.xml"),
						sp_.getArtifactDeployURL(), sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				
				temp.add(new DeployFile(DBConfigurationCreator.groupId, databaseName.getText(), databaseVersion.getText(),
						databaseClassifier.getText(), "isaac.zip",
						new File(new File(workingFolder.getText()),
								DBConfigurationCreator.parentArtifactId + "/target/" + databaseName.getText() + "-" + databaseVersion.getText() + "-"
										+ databaseClassifier.getText() + ".isaac.zip"),
						sp_.getArtifactDeployURL(), sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}, 
		new File(workingFolder.getText() + "/" + DBConfigurationCreator.parentArtifactId));
	}
	
	private void doRun(Supplier<String> jobRunner, Supplier<List<DeployFile>> fileDepoyer, File mavenLaunchFolder)
	{
		AtomicReference<ProgressDialog> pdRef = new AtomicReference<ProgressDialog>(null);

		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				String tag = jobRunner.get();
				if (StringUtils.isNotBlank(tag))
				{
					log.info("The created tag was " + tag);
				}

				if (opInstall.isSelected() || opDeploy.isSelected() || opDirectDeploy.isSelected())
				{
					ArrayList<String> options = new ArrayList<>();
					options.add("-s");
					options.add(sp_.getMavenSettingsFile());
					options.add("-e");
					options.add("clean");
					if (opInstall.isSelected() && !opDeploy.isSelected())
					{
						options.add("install");
					}
					else if (opDeploy.isSelected())
					{
						options.add("deploy");
					}
					else if ((opDirectDeploy.isSelected() && !opInstall.isSelected()) || opPackage.isSelected())
					{
						options.add("package");
					}
					else
					{
						throw new Exception("Bad developer, no cookie");
					}

					TextArea ta = new TextArea();
					ta.setWrapText(true);
					ta.setPadding(new Insets(10.0));
					updateMessage("Running Maven Job");
					Node oldContent = pdRef.get().getDialogPane().getContent();
					Platform.runLater(() -> {
						pdRef.get().setWidth(1024);
						pdRef.get().setHeight(768);
						pdRef.get().getDialogPane().setContent(ta);

					});
					PrintStream ps = new PrintStream(new StreamRedirect(ta), true);
					System.setProperty("maven.multiModuleProjectDirectory", mavenLaunchFolder.getAbsolutePath());
					MavenCli cli = new MavenCli();
					int result = cli.doMain(options.toArray(new String[options.size()]), mavenLaunchFolder.getAbsolutePath(), ps, ps);
					if (result != 0)
					{
						throw new Exception("Maven execution failed");
					}

					Platform.runLater(() -> {
						pdRef.get().getDialogPane().setContent(oldContent);
						pdRef.get().setWidth(400);
						pdRef.get().setHeight(300);
					});

					if (opDirectDeploy.isSelected())
					{
						updateMessage("Deploying artifacts");
						for (final DeployFile deploy : fileDepoyer.get())
						{
							deploy.messageProperty().addListener((change) -> 
							{
								updateMessage(deploy.getMessage());
								Platform.runLater(() -> ta.appendText(deploy.getMessage()));
							});
							Get.workExecutors().getExecutor().execute(deploy);
							deploy.get();
						}
					}
				}

				if (workingFolderCleanup.isSelected())
				{
					RecursiveDelete.delete(new File(workingFolder.getText()));
				}

				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		ProgressDialog pd = new ProgressDialog(t);
		pdRef.set(pd);
		pd.setTitle("Building Configuration");
		pd.setHeaderText(null);
		pd.setContentText("Building Configuration");
		pd.showAndWait();

		try
		{
			t.get();
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Complete");
			alert.setHeaderText("Job complete");
			alert.showAndWait();
		}

		catch (Exception e)
		{
			log.error("Unexpected problem", e);
			Alert errorAlert = new Alert(AlertType.ERROR);
			errorAlert.setTitle("Error");
			errorAlert.setHeaderText("Error creating configuration");
			Text text = new Text("Please see the log file and/or console for details on the error");
			text.wrappingWidthProperty().bind(errorAlert.widthProperty().subtract(10.0));
			errorAlert.getDialogPane().setContent(text);
			errorAlert.getDialogPane().setPadding(new Insets(5.0));
			errorAlert.showAndWait();
		}
	}

	/**
	 * @param contentManager
	 */
	public void finishSetup(ContentManager contentManager)
	{
		cm_ = contentManager;
		fileExit.setOnAction((action) -> cm_.shutdown());

		optionsGitConfig.setOnAction((action) -> gitConfigDialog());
		optionsArtifacts.setOnAction((action) -> artifactsConfigDialog());
		optionsMaven.setOnAction((action) -> mavenConfigDialog());
	}

	private void mavenConfigDialog()
	{
		try
		{
			URL resource = ContentManager.class.getResource("MavenPathsPanel.fxml");
			log.debug("FXML for " + MavenPathsPanel.class + ": " + resource);
			FXMLLoader loader = new FXMLLoader(resource);
			GridPane mavenGridPane = loader.load();
			MavenPathsPanel mavenPanelController = loader.getController();

			Alert mavenPathsDialog = new Alert(AlertType.CONFIRMATION);
			mavenPathsDialog.setTitle("Maven Configuration");
			mavenPathsDialog.setHeaderText("Please specify the Maven configuration");
			mavenPathsDialog.getDialogPane().setContent(mavenGridPane);

			mavenPanelController.m2PathBrowse.setOnAction((actionEvent) -> {
				DirectoryChooser fc = new DirectoryChooser();
				fc.setTitle("Select Maven 'm2' folder");
				File f = fc.showDialog(mavenPathsDialog.getOwner());
				if (f != null)
				{
					mavenPanelController.mavenM2Path.setText(f.getAbsolutePath());
				}
			});

			mavenPanelController.settingsFileBrowse.setOnAction((actionEvent) -> {
				FileChooser fc = new FileChooser();
				fc.setTitle("Select Maven 'settings.xml' file");
				File f = fc.showOpenDialog(mavenPathsDialog.getOwner());
				if (f != null)
				{
					mavenPanelController.mavenSettingsFile.setText(f.getAbsolutePath());
				}
			});

			mavenPanelController.mavenM2Path.setText(sp_.getLocalM2FolderPath());
			mavenPanelController.mavenSettingsFile.setText(sp_.getMavenSettingsFile());

			if (mavenPathsDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sp_.setMavenSettingsFile(mavenPanelController.mavenSettingsFile.getText());
				sp_.setLocalM2FolderPath(mavenPanelController.mavenM2Path.getText());
				cm_.storePrefsFile();
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected error handling git prefs", e);
		}
	}

	private void artifactsConfigDialog()
	{
		try
		{
			URL resource = ContentManager.class.getResource("ArtifactPanel.fxml");
			log.debug("FXML for " + ArtifactPanel.class + ": " + resource);
			FXMLLoader loader = new FXMLLoader(resource);
			GridPane artifactGridPane = loader.load();
			ArtifactPanel artifactController = loader.getController();

			Alert artifactDialog = new Alert(AlertType.CONFIRMATION);
			artifactDialog.setTitle("Artifact Repository Configuration");
			artifactDialog.setHeaderText("Please specify the Artifact Repository configuration");
			artifactDialog.getDialogPane().setContent(artifactGridPane);

			artifactController.artifactReadUrl.setText(sp_.getArtifactReadURL());
			artifactController.artifactDeployUrl.setText(sp_.getArtifactDeployURL());
			artifactController.artifactUsername.setText(sp_.getArtifactUsername());
			artifactController.artifactPassword.setText(new String(sp_.getArtifactPassword()));

			if (artifactDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sp_.setArtifactReadURL(artifactController.artifactReadUrl.getText());
				sp_.setArtifactDeployURL(artifactController.artifactDeployUrl.getText());
				sp_.setArtifactUsername(artifactController.artifactUsername.getText());
				sp_.setArtifactPassword(artifactController.artifactPassword.textProperty().get().toCharArray());
				cm_.storePrefsFile();
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected error handling git prefs", e);
		}
	}

	private void gitConfigDialog()
	{
		try
		{
			URL resource = ContentManager.class.getResource("GitPanel.fxml");
			log.debug("FXML for " + GitPanel.class + ": " + resource);
			FXMLLoader loader = new FXMLLoader(resource);
			GridPane gitGridPane = loader.load();
			GitPanel gpController = loader.getController();

			Alert gitDialog = new Alert(AlertType.CONFIRMATION);
			gitDialog.setTitle("Git Configuration");
			gitDialog.setHeaderText("Please specify the GIT configuration");
			gitDialog.getDialogPane().setContent(gitGridPane);

			gpController.gitUrl.setText(sp_.getGitURL());
			gpController.gitUsername.setText(sp_.getGitUsername());
			gpController.gitPassword.setText(new String(sp_.getGitPassword()));

			if (gitDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sp_.setGitURL(gpController.gitUrl.getText());
				sp_.setGitUsername(gpController.gitUsername.getText());
				sp_.setGitPassword(gpController.gitPassword.textProperty().get().toCharArray());
				cm_.storePrefsFile();
			}
		}
		catch (Exception e)
		{
			log.error("Unexpected error handling git prefs", e);
		}
	}

	public void readData(StoredPrefs sp)
	{
		sp_ = sp;
		if (sp_ == null)
		{
			throw new RuntimeException("StoredPrefs were not passed");
		}

		readAvailableMetadataVersions();
	}

	private void readAvailableMetadataVersions()
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				final TreeSet<String> metadataVersions = new TreeSet<>(new AlphanumComparator(true));
				metadataVersions.add(VersionFinder.findProjectVersion(true));
				metadataVersions.add(VersionFinder.findProjectVersion(false));

				File temp = new File(sp_.getLocalM2FolderPath());
				if (temp.isDirectory())
				{
					log.debug("Reading local m2 folder");
					for (IBDFFile i : DBConfigurationCreator.readLocalMetadataArtifacts(temp))
					{
						metadataVersions.add(i.getVersion());
					}
				}
				try
				{
					if (StringUtils.isNotBlank(sp_.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						metadataVersions.addAll(new NexusRead(sp_).readMetadataVersions());
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository", e);
				}

				Platform.runLater(() -> {
					databaseMetadataVersion.getItems().clear();
					databaseMetadataVersion.getItems().addAll(metadataVersions);
					databaseMetadataVersion.getSelectionModel().select(metadataVersions.size() - 1);
				});
				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		ProgressDialog pd = new ProgressDialog(t);
		pd.setTitle("Reading Metadata Versions");
		pd.setHeaderText(null);
		pd.setContentText("Reading available Metadata Versions");
		pd.showAndWait();
	}

	private void readAvailableIBDFFiles()
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				File temp = new File(sp_.getLocalM2FolderPath());
				HashSet<IBDFFile> foundFiles = new HashSet<>();
				if (temp.isDirectory())
				{
					log.debug("Reading local m2 folder");
					updateMessage("Reading the local m2 folder");

					for (IBDFFile i : DBConfigurationCreator.readLocalIBDFArtifacts(temp))
					{
						if (i.getArtifactId().equals("metadata"))
						{
							continue;
						}
						foundFiles.add(i);
					}
				}

				try
				{
					if (StringUtils.isNotBlank(sp_.getArtifactReadURL()))
					{
						log.debug("Reading available nexus versions");
						// TODO if/when we support more than just nexus, look at the URL, and use it to figure out which reader to construct
						foundFiles.addAll(new NexusRead(sp_).readIBDFFiles());
					}
				}
				catch (Exception e)
				{
					log.error("Error reading nexus repository", e);
				}

				ibdfFiles_.clear();
				ibdfFiles_.addAll(foundFiles);
				Collections.sort(ibdfFiles_);
				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		ProgressDialog pd = new ProgressDialog(t);
		pd.setTitle("Reading IBDF Files");
		pd.setHeaderText(null);
		pd.setContentText("Reading available IBDF Files");
		pd.showAndWait();
	}
}
