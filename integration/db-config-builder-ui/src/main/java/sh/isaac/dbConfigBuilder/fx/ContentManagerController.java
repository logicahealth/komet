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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.ContextMenu;
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
import javafx.util.Pair;
import javafx.util.StringConverter;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.constants.DatabaseImplementation;
import sh.isaac.api.util.DeployFile;
import sh.isaac.api.util.RecursiveDelete;
import sh.isaac.dbConfigBuilder.artifacts.IBDFFile;
import sh.isaac.dbConfigBuilder.artifacts.MavenArtifactUtils;
import sh.isaac.dbConfigBuilder.artifacts.SDOSourceContent;
import sh.isaac.dbConfigBuilder.fx.fxUtil.StreamRedirect;
import sh.isaac.dbConfigBuilder.prefs.StoredPrefs;
import sh.isaac.pombuilder.converter.ContentConverterCreator;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.ConverterOptionParamSuggestedValue;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;
import sh.isaac.pombuilder.converter.UploadFileInfo;
import sh.isaac.pombuilder.dbbuilder.DBConfigurationCreator;
import sh.isaac.pombuilder.diff.DiffExecutionCreator;
import sh.isaac.pombuilder.upload.SrcUploadCreator;
import sh.komet.gui.util.ErrorMarkerUtils;
import sh.komet.gui.util.FxUtils;
import sh.komet.gui.util.UpdateableBooleanBinding;
import sh.komet.gui.util.ValidBooleanBinding;
import tornadofx.control.DateTimePicker;

/**
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 * GUI controller for the Content Manager UI
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
	private MenuItem optionsReadMavenArtifacts;
	@FXML
	private Tab tabSrcUpload;
	@FXML
	private Tab tabDeltaCreation;
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
	private Button sourceConversionContentSelect;
	@FXML
	private ComboBox<String> sourceConversionConverterVersion;
	@FXML
	private Button sourceConversionIBDFSelect;
	@FXML
	private ListView<SDOSourceContent> sourceConversionContent;
	@FXML
	private ListView<IBDFFile> sourceConversionIBDF;
	@FXML
	private ListView<ConverterOptionParam> sourceConversionOptions;
	@FXML
	private TextArea sourceConversionOptionDescription;
	@FXML
	private TextArea sourceConversionOptionValues;
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
	private ComboBox<String> databaseType;
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
	
	@FXML
	private GridPane deltaGridPane;
	@FXML
	private TextField deltaInitialState;
	private IBDFFile deltaInitialStateIBDF;
	@FXML
	private Button deltaInitialStateButton;
	@FXML
	private ChoiceBox<IBDFFile> deltaEndState;
	@FXML
	private ComboBox<UUID> deltaAuthor;

	private DateTimePicker deltaDateTime = new DateTimePicker();
	@FXML
	private CheckBox deltaIgnoreTime;
	@FXML
	private CheckBox deltaIgnoreSibling;
	@FXML
	private CheckBox deltaGenerateRetires;
	
	@FXML
	private ComboBox<String> deltaCalculatorVersion;
	@FXML
	private TextField deltaResultVersion;


	private ContentManager cm_;

	private UpdateableBooleanBinding allRequiredReady_;
	private ArrayList<IBDFFile> ibdfFiles_ = new ArrayList<>();
	private ArrayList<SDOSourceContent> sdoSourceFiles_ = new ArrayList<>();
	private ArrayList<ValidBooleanBinding> databaseTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private ArrayList<ValidBooleanBinding> sourceConvertTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private ArrayList<ValidBooleanBinding> sourceUploadTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private ArrayList<ValidBooleanBinding> deltaTabValidityCheckers_ = new ArrayList<ValidBooleanBinding>();
	private UpdateableBooleanBinding allSourceUploadFilesPresent_;
	private ArrayList<TextField> sourceUploadFiles_ = new ArrayList<>();
	private HashMap<ConverterOptionParam, String> sourceConversionUserOptions = new HashMap<>();
	private final StringBuilder sourceConverterContentIBDFInvalidReason = new StringBuilder();
	private ValidBooleanBinding sourceConverterContentIBDFValid; 

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
		assert sourceConversionContentSelect != null : "fx:id=\"sourceConversionContentSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionConverterVersion != null : "fx:id=\"sourceConversionConverterVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionIBDFSelect != null : "fx:id=\"sourceConversionIBDFSelect\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionContent != null : "fx:id=\"sourceConversionContent\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionIBDF != null : "fx:id=\"sourceConversionIBDF\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionOptions != null : "fx:id=\"sourceConversionOptions\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionOptionDescription != null : "fx:id=\"sourceConversionOptionDescription\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert sourceConversionOptionValues != null : "fx:id=\"sourceConversionOptionValues\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert tabDatabaseCreation != null : "fx:id=\"tabDatabaseCreation\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseName != null : "fx:id=\"databaseName\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseVersion != null : "fx:id=\"databaseVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseClassifier != null : "fx:id=\"databaseClassifier\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert opClassify != null : "fx:id=\"databaseClassify\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseMetadataVersion != null : "fx:id=\"databaseMetadataVersion\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert databaseType != null : "fx:id=\"databaseType\" was not injected: check your FXML file 'ContentManager.fxml'.";
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
		assert deltaGridPane != null : "fx:id=\"deltaGridPane\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert deltaInitialState != null : "fx:id=\"deltaInitialState\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert deltaEndState != null : "fx:id=\"deltaEndState\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert deltaAuthor != null : "fx:id=\"deltaAuthor\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert deltaIgnoreTime != null : "fx:id=\"deltaIgnoreTime\" was not injected: check your FXML file 'ContentManager.fxml'.";
		assert deltaIgnoreSibling != null : "fx:id=\"deltaIgnoreSibling\" was not injected: check your FXML file 'ContentManager.fxml'.";


		tabDatabaseCreation.getTabPane().getSelectionModel().select(tabDatabaseCreation);

		// shared components
		workingFolder.setText(Files.createTempDirectory("contentManager").toFile().getAbsolutePath());
		workingFolderSelect.setOnAction((actionEvent) -> {
			DirectoryChooser fc = new DirectoryChooser();
			fc.setTitle("Select the folder where the project will be created");
			File f = fc.showDialog(cm_.getPrimaryStage().getScene().getWindow());
			if (f != null)
			{
				f = new File(f, "contentManager");
				f.mkdirs();
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
				FxUtils.waitWithProgress("Reading IBDF Files", "Reading available IBDF Files", MavenArtifactUtils.readAvailableIBDFFiles(false, sp_, (results) -> 
				{
					ibdfFiles_.clear();
					ibdfFiles_.addAll(results);
				}), cm_.getPrimaryStage().getScene().getWindow());
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
			ibdfDialog.initOwner(cm_.getPrimaryStage().getOwner());
			ibdfDialog.setHeaderText("Select 1 or more IBDF Files to add");
			ibdfDialog.getDialogPane().setContent(ibdfPicker);
			ibdfDialog.setResizable(true);
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
		
		for (DatabaseImplementation di : DatabaseImplementation.values())
		{
			databaseType.getItems().add(di.name());
		}
		databaseType.getSelectionModel().select(DatabaseImplementation.DEFAULT.name());

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
					if (sc.getIBDFDependencies() != null)
					{
						for (String requiredIbdfArtifactId : sc.getIBDFDependencies())
						{
							boolean found = false;
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
					if (!Pattern.matches(sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionRegExpValidator(),
							sourceUploadVersion.getText()))
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
				sourceUploadVersionTooltip.setText(
						sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionDescription() + "\n" + "[Calculating existing versions ...]");
				readSourceUploadExistingVersions();
				updateSourceUploadFiles();

				boolean needsExtension = sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId().contains("*");
				if (!needsExtension)
				{
					sourceUploadExtension.setText("");
				}
				return !needsExtension;
			}
		});

		sourceUploadExtension.textProperty().addListener((change) -> readSourceUploadExistingVersions());

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
				if (!allSourceUploadFilesPresent_.get()
						&& (opPackage.isSelected() || opInstall.isSelected() || opDeploy.isSelected() || opDirectDeploy.isSelected()))
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
		
		sourceConversionContentSelect.setOnAction(action -> {
			if (sdoSourceFiles_.size() == 0)
			{
				FxUtils.waitWithProgress("Reading SDO Source Files", "Reading available SDO Source Files", MavenArtifactUtils.readAvailableSourceFiles(sp_, items -> 
				{
					sdoSourceFiles_.clear();
					sdoSourceFiles_.addAll(items);
				}), cm_.getPrimaryStage().getScene().getWindow());
			}

			ListView<SDOSourceContent> sdoPicker = new ListView<>();
			sdoPicker.setItems(FXCollections.observableArrayList(sdoSourceFiles_));
			sdoPicker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			sdoPicker.setCellFactory(param -> new ListCell<SDOSourceContent>()
			{
				@Override
				protected void updateItem(SDOSourceContent item, boolean empty)
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

			Alert sdoDialog = new Alert(AlertType.CONFIRMATION);
			sdoDialog.setTitle("Select Files");
			sdoDialog.setHeaderText("Select 1 or more SDO Files to add");
			sdoDialog.getDialogPane().setContent(sdoPicker);
			sdoPicker.setPrefWidth(1024);
			sdoDialog.setResizable(true);
			sdoDialog.initOwner(cm_.getPrimaryStage().getOwner());

			if (sdoDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sourceConversionContent.getItems().addAll(sdoPicker.getSelectionModel().getSelectedItems());
			}
		});
		
		sourceConversionContent.setCellFactory(param -> new ListCell<SDOSourceContent>()
		{
			@Override
			protected void updateItem(SDOSourceContent item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty || item == null)
				{
					setText(null);
				}
				else
				{
					setText(item.getArtifactId() + (item.hasClassifier() ? " : " + item.getClassifier() : "") + " : " + item.getVersion());
					MenuItem mi = new MenuItem("Remove");
					mi.setOnAction(action -> 
					{
						sourceConversionContent.getItems().remove(item);
					});
					ContextMenu cm = new ContextMenu(mi);
					setContextMenu(cm);
				}
			}
		});
		
		sourceConversionIBDFSelect.setOnAction(action -> {
			if (ibdfFiles_.size() == 0)
			{
				FxUtils.waitWithProgress("Reading IBDF Files", "Reading available IBDF Files", MavenArtifactUtils.readAvailableIBDFFiles(false, sp_, (results) -> 
				{
					ibdfFiles_.clear();
					ibdfFiles_.addAll(results);
				}), cm_.getPrimaryStage().getScene().getWindow());
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
			ibdfDialog.setResizable(true);
			ibdfDialog.initOwner(cm_.getPrimaryStage().getOwner());
			ibdfPicker.setPrefWidth(1024);

			if (ibdfDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sourceConversionIBDF.getItems().addAll(ibdfPicker.getSelectionModel().getSelectedItems());
			}
		});
		
		StringBuilder sourceConversionContentInvalidReason = new StringBuilder();
		
		
		final ValidBooleanBinding sourceConversionContentValid = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if (sourceConversionContentInvalidReason.length() == 0)
				{
					clearInvalidReason();
					return true;
				}
				setInvalidReason(sourceConversionContentInvalidReason.toString());
				return false;
			}
		};
		
		ErrorMarkerUtils.setupErrorMarker(sourceConversionContent, sourceConversionContentValid, true);
		sourceConvertTabValidityCheckers_.add(sourceConversionContentValid);
		
		sourceConverterContentIBDFValid = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				if (sourceConverterContentIBDFInvalidReason.length() == 0)
				{
					clearInvalidReason();
					return true;
				}
				setInvalidReason(sourceConverterContentIBDFInvalidReason.toString());
				return false;
			}
		};
		
		ErrorMarkerUtils.setupErrorMarker(sourceConversionIBDF, sourceConverterContentIBDFValid, true);
		sourceConvertTabValidityCheckers_.add(sourceConverterContentIBDFValid);
		
		sourceConversionContent.getItems().addListener(new ListChangeListener<SDOSourceContent>()
		{
			@Override
			public void onChanged(Change<? extends SDOSourceContent> c)
			{
				//First item in the list will be the source to convert.  Anything further, will be treated as additional src dependencies.
				if (sourceConversionContent.getItems().size() > 0)
				{
					SDOSourceContent convert = sourceConversionContent.getItems().get(0);
					
					SupportedConverterTypes converter = SupportedConverterTypes.findBySrcArtifactId(convert.getArtifactId());
					
					if (converter.getArtifactDependencies() != null && converter.getArtifactDependencies().size() > 0)
					{
						sourceConversionContentInvalidReason.setLength(0);
						
						//validate they have all required src dependencies
						
						for (String s : converter.getArtifactDependencies())
						{
							boolean found = false;
							for (int i = 1; i < sourceConversionContent.getItems().size(); i++)
							{
								if (sourceConversionContent.getItems().get(i).getArtifactId().equals(s))
								{
									found = true;
									break;
								}
							}
							if (!found)
							{
								sourceConversionContentInvalidReason.append("The conversion of " + convert.getArtifactId() + " requires a source dependency of " + s + "\n");
							}
						}
						sourceConversionContentValid.invalidate();
					}
					
					checksourceConversionIBDFDependencies();
					
					//Set up converter options
					setupSourceConversionOptions(converter);
				}
				else
				{
					setupSourceConversionOptions(null);
					sourceConverterContentIBDFInvalidReason.setLength(0);
					sourceConverterContentIBDFValid.invalidate();
				}
			}
		});
		
		sourceConversionIBDF.setCellFactory(param -> new ListCell<IBDFFile>()
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
					MenuItem mi = new MenuItem("Remove");
					mi.setOnAction(action -> 
					{
						sourceConversionIBDF.getItems().remove(item);
					});
					ContextMenu cm = new ContextMenu(mi);
					setContextMenu(cm);
				}
			}
		});
		
		sourceConversionIBDF.getItems().addListener((ListChangeListener<IBDFFile>) listener -> checksourceConversionIBDFDependencies());
		
		sourceConversionConverterVersion.getSelectionModel().selectedItemProperty().addListener(change -> 
		{
			if (sourceConversionContent.getItems().size() > 0)
			{
				SDOSourceContent convert = sourceConversionContent.getItems().get(0);
				
				SupportedConverterTypes converter = SupportedConverterTypes.findBySrcArtifactId(convert.getArtifactId());
				setupSourceConversionOptions(converter);
			}
		});
		
		sourceConversionOptions.setCellFactory(item -> new ListCell<ConverterOptionParam>()
		{
			@Override
			protected void updateItem(ConverterOptionParam item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty || item == null)
				{
					setText(null);
				}
				else
				{
					setText(item.getDisplayName());
				}
			}
		});
		
		final AtomicBoolean myChange = new AtomicBoolean();
		
		sourceConversionOptions.getSelectionModel().selectedItemProperty().addListener(change ->
		{
			sourceConversionOptionDescription.clear();
			ConverterOptionParam cop = sourceConversionOptions.getSelectionModel().getSelectedItem();
			String temp = sourceConversionUserOptions.get(cop);
			myChange.set(true);
			sourceConversionOptionValues.setText(temp == null ? "" : temp);
			myChange.set(false);
			
			if (cop == null)
			{
				return;
			}
			sourceConversionOptionDescription.appendText(cop.getDisplayName());
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText(cop.getDescription());
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText("Is selection required: " + !cop.isAllowNoSelection());
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText("Is more than one selection allowed: " + cop.isAllowMultiSelectInPomMode());
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText("\n");
			sourceConversionOptionDescription.appendText("Suggested values:");
			sourceConversionOptionDescription.appendText("\n");
			if (cop.getSuggestedPickListValues() != null)
			{
				for (ConverterOptionParamSuggestedValue s : cop.getSuggestedPickListValues())
				{
					sourceConversionOptionDescription.appendText(s.getValue() + " - " + s.getDescription());
					sourceConversionOptionDescription.appendText("\n");
				}
				sourceConversionOptionDescription.appendText("\n");
				sourceConversionOptionDescription.appendText("Formatting example for suggested values:");
				sourceConversionOptionDescription.appendText("\n");
				for (ConverterOptionParamSuggestedValue s : cop.getSuggestedPickListValues())
				{
					sourceConversionOptionDescription.appendText(s.getValue());
					sourceConversionOptionDescription.appendText("\n");
				}
				
				sourceConversionOptionDescription.appendText("\n");
				sourceConversionOptionDescription.appendText("\n");
				sourceConversionOptionDescription.appendText("Enter your values to the right, one per line");
			}
			
		});
		
		sourceConversionOptionValues.textProperty().addListener(change ->
		{
			if (myChange.get())
			{
				//ignore
				return;
			}
			ConverterOptionParam cop = sourceConversionOptions.getSelectionModel().getSelectedItem();
			if (cop != null)
			{
				sourceConversionUserOptions.put(cop, sourceConversionOptionValues.getText());
			}
		});
		
		ValidBooleanBinding sourceConversionOptionValuesValid = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				bind(sourceConversionOptionValues.textProperty(), sourceConversionOptions.getSelectionModel().selectedItemProperty());
				sourceConversionOptions.getItems().addListener((ListChangeListener<ConverterOptionParam>) change -> invalidate());
			}
			
			@Override
			protected boolean computeValue()
			{
				for (ConverterOptionParam cop : sourceConversionOptions.getItems())
				{
					Set<String> values = parseUserOptions(sourceConversionUserOptions.get(cop));
					if (!cop.isAllowNoSelection() && values.size() == 0)
					{
						setInvalidReason("The option " + cop.getDisplayName() + " must be specified");
						return false;
					}
					if (!cop.isAllowMultiSelectInPomMode() && values.size() > 1)
					{
						setInvalidReason("The option " + cop.getDisplayName() + " only allows one entry");
						return false;
					}
				}
				clearInvalidReason();
				return true;
			}
		};
		
		ErrorMarkerUtils.setupErrorMarker(sourceConversionOptions, sourceConversionOptionValuesValid, true);
		sourceConvertTabValidityCheckers_.add(sourceConversionOptionValuesValid);
		
		
		//Delta Creation Tab
		deltaInitialStateButton.setOnAction(action -> {
			if (ibdfFiles_.size() == 0)
			{
				FxUtils.waitWithProgress("Reading IBDF Files", "Reading available IBDF Files", MavenArtifactUtils.readAvailableIBDFFiles(false, sp_, (results) -> 
				{
					ibdfFiles_.clear();
					ibdfFiles_.addAll(results);
				}), cm_.getPrimaryStage().getScene().getWindow());
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
			ibdfDialog.setHeaderText("Select 1 or 2 IBDF files of the same content type to diff");
			ibdfDialog.getDialogPane().setContent(ibdfPicker);
			ibdfDialog.initOwner(cm_.getPrimaryStage().getOwner());
			ibdfDialog.setResizable(true);
			ibdfPicker.setPrefWidth(1024);

			if (ibdfDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				//The picker should return items in order from lowest version to highest, so put the first one in the initial
				//box, and the next one (if present and the same type) in the end state box.
				//Also, populate the end-state dropdown with any other possible choices.
				deltaEndState.getItems().clear();
				deltaEndState.getSelectionModel().clearSelection();
				deltaInitialStateIBDF = null;
				int count = 0;
				for (IBDFFile selectedIbdf : ibdfPicker.getSelectionModel().getSelectedItems())
				{
					if (count == 0)
					{
						deltaInitialState.setText(selectedIbdf.toString());
						deltaInitialStateIBDF = selectedIbdf;
						for (IBDFFile allIbdf : ibdfFiles_)
						{
							if (allIbdf.getGroupId().equals(deltaInitialStateIBDF.getGroupId()) && allIbdf.getArtifactId().equals(deltaInitialStateIBDF.getArtifactId()))
							{
								deltaEndState.getItems().add(allIbdf);
							}
						}
						deltaEndState.getSelectionModel().select(deltaEndState.getItems().size() - 1);
					}
					else if (count == 1)
					{
						if (deltaEndState.getItems().contains(selectedIbdf)) //JavaFX doesn't seem to work properly, if you tell it to select somethign that isn't in the list
						{
							deltaEndState.getSelectionModel().select(selectedIbdf);
						}
						break;
					}
					count++;
				}
			}
		});
		
		ValidBooleanBinding deltaInitialValid = new ValidBooleanBinding()
		{
			{
				bind(deltaInitialState.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (StringUtils.isBlank(deltaInitialState.getText()))
				{
					setInvalidReason("An initial IBDF file must be selected");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		deltaTabValidityCheckers_.add(deltaInitialValid);
		ErrorMarkerUtils.setupErrorMarker(deltaInitialState, deltaInitialValid, true);
		
		ValidBooleanBinding deltaEndValid = new ValidBooleanBinding()
		{
			{
				setComputeOnInvalidate(true);
				bind(deltaEndState.getItems());
				bind(deltaEndState.valueProperty());
				bind(deltaInitialState.textProperty());
				
			}

			@Override
			protected boolean computeValue()
			{
				if (deltaEndState.getChildrenUnmodifiable().size() < 2)
				{
					setInvalidReason("The selected initial file does not have any other versions available to use for delta calculation");
					return false;
				}
				else if (deltaEndState.getSelectionModel().getSelectedItem() == null)
				{
					setInvalidReason("You must select an end state file");
					return false;
				}
				else if (deltaEndState.getSelectionModel().getSelectedItem().equals(deltaInitialStateIBDF))
				{
					setInvalidReason("Cannot select the same file as the initial selection");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		deltaTabValidityCheckers_.add(deltaEndValid);
		ErrorMarkerUtils.setupErrorMarker(deltaEndState, deltaEndValid, true);
		
		GridPane.setConstraints(deltaDateTime, 1, 3, 2, 1);
		deltaDateTime.setMaxWidth(Double.MAX_VALUE);
		deltaDateTime.setDateTimeValue(LocalDateTime.now());
		deltaGridPane.getChildren().add(deltaDateTime);
		
		deltaAuthor.setConverter(new StringConverter<UUID>()
		{
			@Override
			public String toString(UUID object)
			{
				if (object == null)
				{
					return "";
				}
				else if (object.equals(MetaData.USER____SOLOR.getPrimordialUuid()))
				{
					return "System Default User";
				}
				return object.toString();
			}

			@Override
			public UUID fromString(String string)
			{
				if (string.equals("System Default User"))
				{
					return MetaData.USER____SOLOR.getPrimordialUuid();
				}
				else
				{
					try
					{
						return UUID.fromString(string);
					}
					catch (Exception e)
					{
						return null;
					}
				}
			}
		});
		
		deltaAuthor.getItems().add(MetaData.USER____SOLOR.getPrimordialUuid());
		deltaAuthor.getSelectionModel().select(0);
		
		ValidBooleanBinding deltaAuthorValid = new ValidBooleanBinding()
		{
			{
				bind(deltaAuthor.valueProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (deltaAuthor.getValue() == null)
				{
					setInvalidReason("The entered value isn't a valid UUID");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		deltaTabValidityCheckers_.add(deltaAuthorValid);
		ErrorMarkerUtils.setupErrorMarker(deltaAuthor, deltaAuthorValid, true);
		
		Tooltip.install(deltaResultVersion, new Tooltip("The version number to assign to the resulting IBDF file"));
		ValidBooleanBinding deltaOutputVersionValid = new ValidBooleanBinding()
		{
			{
				bind(deltaResultVersion.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (StringUtils.isBlank(deltaResultVersion.getText()))
				{
					setInvalidReason("The result version is required");
					return false;
				}
				clearInvalidReason();
				return true;
			}
		};
		deltaTabValidityCheckers_.add(deltaOutputVersionValid);
		ErrorMarkerUtils.setupErrorMarker(deltaResultVersion, deltaOutputVersionValid, true);

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
				temp = ContentManagerController.this.sourceConvertTabValidityCheckers_;
			}
			else if (tabDatabaseCreation.getTabPane().getSelectionModel().getSelectedItem() == tabSrcUpload)
			{
				temp = ContentManagerController.this.sourceUploadTabValidityCheckers_;
			}
			else if (tabDatabaseCreation.getTabPane().getSelectionModel().getSelectedItem() == tabDeltaCreation)
			{
				temp = ContentManagerController.this.deltaTabValidityCheckers_;
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
				opInstall.setSelected(true);
				opPackage.setSelected(true);
			}
		});

		opInstall.setOnAction((action) -> {
			if (opInstall.isSelected())
			{
				opPackage.setSelected(true);
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
				createSourceConversion();
			}
			else if (tabSrcUpload.isSelected())
			{
				createSourceUpload();
			}
			else if (tabDeltaCreation.isSelected())
			{
				createDelta();
			}
			else
			{
				log.error("unexpected tab selection");
			}
		});
	}
	
	private Set<String> parseUserOptions(String userEntry)
	{
		HashSet<String> entries = new HashSet<>();
		if (userEntry == null)
		{
			return entries;
		}
		for (String s : userEntry.split("\\R"))
		{
			if (StringUtils.isNotBlank(s))
			{
				entries.add(s);
			}
		}
		return entries;
	}
	
	private void checksourceConversionIBDFDependencies()
	{
		if (sourceConversionContent.getItems().size() > 0)
		{
			SDOSourceContent convert = sourceConversionContent.getItems().get(0);
			SupportedConverterTypes converter = SupportedConverterTypes.findBySrcArtifactId(convert.getArtifactId());
			
			if (converter.getIBDFDependencies() != null && converter.getIBDFDependencies().size() > 0)
			{
				//validate they have all required ibdf dependencies
				sourceConverterContentIBDFInvalidReason.setLength(0);
				for (String s : converter.getIBDFDependencies())
				{
					boolean found = false;
					for (int i = 0; i < sourceConversionIBDF.getItems().size(); i++)
					{
						if (sourceConversionIBDF.getItems().get(i).getArtifactId().equals(s))
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						sourceConverterContentIBDFInvalidReason.append("The conversion of " + convert.getArtifactId() + " requires an ibdf dependency of " + s + "\n");
					}
				}
			}
			else
			{
				sourceConverterContentIBDFInvalidReason.setLength(0);
			}
		}
		else
		{
			sourceConverterContentIBDFInvalidReason.setLength(0);
		}
		sourceConverterContentIBDFValid.invalidate();
	}

	protected Task<Void> readSourceUploadExistingVersions()
	{
		if (sp_ == null)
		{
			// Happens during startup
			return null;
		}
		
		String artifactId = sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId();
		artifactId.replace("*", sourceUploadExtension.getText());
		
		Task<Void> t = MavenArtifactUtils.readSourceUploadExistingVersions(sp_, artifactId, results ->
		{
			StringBuilder sb = new StringBuilder();
			sb.append("\nCurrent Versions:");
			results.forEach(string -> 
			{
				sb.append("\n");
				sb.append(string);
			});
			
			Platform.runLater(() -> {
				sourceUploadVersionTooltip.setText(sourceUploadType.getSelectionModel().getSelectedItem().getSourceVersionDescription() + sb.toString());
			});
		});
		return t;
	}
	
	private void setupSourceConversionOptions(SupportedConverterTypes converter)
	{
		Task<Void> t = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				try
				{
					if (converter != null)
					{
						ConverterOptionParam[] options = ConverterOptionParam.fromArtifact(new File(sp_.getLocalM2FolderPath()), converter,
							sourceConversionConverterVersion.getSelectionModel().getSelectedItem(), sp_.getArtifactReadURL(), sp_.getArtifactUsername(),
							sp_.getArtifactPassword());
						
						Platform.runLater(() ->
						{
							sourceConversionOptions.getItems().clear();
							sourceConversionOptions.getSelectionModel().clearSelection();
							sourceConversionUserOptions.clear();
							sourceConversionOptions.getItems().addAll(options);
						});
					}
					else
					{
						Platform.runLater(() ->
						{
							sourceConversionOptions.getItems().clear();
							sourceConversionOptions.getSelectionModel().clearSelection();
							sourceConversionUserOptions.clear();
						});
					}
				}
				catch (Exception e)
				{
					log.error("Error reading converter options", e);
				}
				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		FxUtils.waitWithProgress("Reading converter options", "Reading converter options", t, cm_.getPrimaryStage().getScene().getWindow());
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
			fileName.focusedProperty().addListener((change) -> {
				if (fileName.focusedProperty().get())
				{
					updateDescription(finalRow);
				}
			});

			hbox.getChildren().add(fileName);
			HBox.setHgrow(fileName, Priority.ALWAYS);
			sourceUploadFiles_.add(fileName);
			ErrorMarkerUtils.setupErrorMarker(fileName, ((input) -> {
				if (StringUtils.isBlank(fileName.getText()))
				{
					return ufi.fileIsRequired() ? "This file is required" : "";
				}
				else if (StringUtils.isNotBlank(ufi.getExpectedNamingPatternRegExpPattern()))
				{
					File f = new File(fileName.getText());
					boolean matches = Pattern.compile(ufi.getExpectedNamingPatternRegExpPattern(), Pattern.CASE_INSENSITIVE).matcher(f.getName()).matches();

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
										&& Pattern.compile(ufiLambda.getExpectedNamingPatternRegExpPattern(), Pattern.CASE_INSENSITIVE)
												.matcher(multiFile.getName()).matches()
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
								fileNameLambda.focusedProperty().addListener((change) -> {
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
	
	private void createSourceConversion()
	{
		doRun(() -> {

			try
			{
				Task<String> task = new Task<String>()
				{
					@Override
					protected String call() throws Exception
					{
						SDOSourceContent sourceContent = sourceConversionContent.getItems().get(0);
						
						SDOSourceContent[] additionalSourceDependencies = new SDOSourceContent[sourceConversionContent.getItems().size() - 1];
						for (int i = 1; i < sourceConversionContent.getItems().size(); i++)
						{
							additionalSourceDependencies[i - 1] = sourceConversionContent.getItems().get(i);
						}
						
						HashMap<ConverterOptionParam, Set<String>> converterOptionValues = new HashMap<>();
						for (Entry<ConverterOptionParam, String> x : sourceConversionUserOptions.entrySet())
						{
							converterOptionValues.put(x.getKey(), parseUserOptions(x.getValue()));
						}
						
						return ContentConverterCreator.createContentConverter(sourceContent, sourceConversionConverterVersion.getSelectionModel().getSelectedItem(), 
								additionalSourceDependencies, sourceConversionIBDF.getItems().toArray(new IBDFFile[0]),
								converterOptionValues, (opTag.isSelected() ? sp_.getGitURL() : null),
								sp_.getGitUsername(), sp_.getGitPassword(), new File(workingFolder.getText()), false);
					}
					
				};
				Get.workExecutors().getExecutor().execute(task);
				return task.get();
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}, () -> {
			try
			{
				String outputVersion = sourceConversionContent.getItems().get(0).getVersion() + "-loader-" 
						+ sourceConversionConverterVersion.getSelectionModel().getSelectedItem();
				Pair <SupportedConverterTypes, String> converterType = SupportedConverterTypes
						.findConverterTypeAndExtensionBySrcArtifactId(sourceConversionContent.getItems().get(0).getArtifactId()); 
				
				String outputArtifactId = converterType.getKey().getConverterOutputArtifactId() + converterType.getValue();
				
				ArrayList<DeployFile> temp = new ArrayList<>();
				temp.add(new DeployFile(ContentConverterCreator.IBDF_OUTPUT_GROUP, outputArtifactId, outputVersion, "", "pom",
						new File(new File(workingFolder.getText()), ContentConverterCreator.WORKING_SUBFOLDER + "/pom.xml"), 
						isSnapshot(outputVersion) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(),
						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				
				for (File f : new File(new File(workingFolder.getText()), ContentConverterCreator.WORKING_SUBFOLDER  + "/target/")
						.listFiles(file -> file.getName().endsWith(".ibdf.zip")))
				{
					//could be rf2-ibdf-sct-20170731T150000Z-loader-4.48-SNAPSHOT-Delta.ibdf.zip 
					//or cpt-ibdf-2017-loader-4.48-SNAPSHOT-.ibdf.zip
					String classifier;
					if (f.getName().endsWith("-.ibdf.zip"))
					{
						classifier = "";
					}
					else
					{
						classifier = f.getName().substring((f.getName().lastIndexOf(outputVersion) + outputVersion.length() + 1), 
								(f.getName().length() - ".ibdf.zip".length()));
					}
					temp.add(new DeployFile(ContentConverterCreator.IBDF_OUTPUT_GROUP, outputArtifactId, outputVersion, classifier, "ibdf.zip", f, 
							isSnapshot(outputVersion) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(),
							sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				}
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

		}, new File(workingFolder.getText() + "/" + ContentConverterCreator.WORKING_SUBFOLDER));
	}

	private void createSourceUpload()
	{
		doRun(() -> {
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
				Task<String> task = SrcUploadCreator.createSrcUploadConfiguration(sourceUploadType.getSelectionModel().getSelectedItem(),
						sourceUploadVersion.getText(), sourceUploadExtension.getText(), filesToUpload, (opTag.isSelected() ? sp_.getGitURL() : null),
						sp_.getGitUsername(), sp_.getGitPassword(), null, null, null,  // don't pass the artifact info, otherwise, it will try to zip
																						  // and direct deploy
						new File(workingFolder.getText()), false, false);
				Get.workExecutors().getExecutor().execute(task);
				return task.get();
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}, () -> {
			try
			{
				ArrayList<DeployFile> temp = new ArrayList<>();
				String updatedArtifactName = sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId().replace("*",
						sourceUploadExtension.getText());
				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, updatedArtifactName, sourceUploadVersion.getText(), "", "pom",
						new File(new File(workingFolder.getText()), SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/pom.xml"), 
						isSnapshot(sourceUploadVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(),
						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));

				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, updatedArtifactName, sourceUploadVersion.getText(), "", "zip",
						new File(new File(workingFolder.getText()),
								SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/target/" + updatedArtifactName + "-" + sourceUploadVersion.getText() + ".zip"),
						isSnapshot(sourceUploadVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(), 
						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

		}, new File(workingFolder.getText() + "/" + SrcUploadCreator.WORKING_SUB_FOLDER_NAME));
	}

	private void createDatabase()
	{
		doRun(() -> {
			try
			{
				return DBConfigurationCreator.createDBConfiguration(databaseName.getText(), databaseVersion.getText(), databaseDescription.getText(),
						databaseClassifier.getText(), opClassify.isSelected(),
						databaseIbdfList.getItems().toArray(new IBDFFile[databaseIbdfList.getItems().size()]),
						databaseMetadataVersion.getSelectionModel().getSelectedItem(), DatabaseImplementation.parse(databaseType.getSelectionModel().getSelectedItem()),
						opTag.isSelected() ? sp_.getGitURL() : null, sp_.getGitUsername(),
						sp_.getGitPassword(), new File(workingFolder.getText()), false);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}, () -> {
			try
			{
				ArrayList<DeployFile> temp = new ArrayList<>();
				temp.add(new DeployFile(DBConfigurationCreator.GROUP_ID, databaseName.getText(), databaseVersion.getText(), "", "pom",
						new File(new File(workingFolder.getText()), DBConfigurationCreator.PARENT_ARTIFIACT_ID + "/pom.xml"), 
						isSnapshot(databaseVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(),
						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));

				temp.add(new DeployFile(DBConfigurationCreator.GROUP_ID, databaseName.getText(), databaseVersion.getText(), databaseClassifier.getText(),
						"isaac.zip",
						new File(new File(workingFolder.getText()),
								DBConfigurationCreator.PARENT_ARTIFIACT_ID + "/target/" + databaseName.getText() + "-" + databaseVersion.getText() + "-"
										+ databaseClassifier.getText() + ".isaac.zip"),
						isSnapshot(databaseVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(), 
						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}, new File(workingFolder.getText() + "/" + DBConfigurationCreator.PARENT_ARTIFIACT_ID));
	}
	
	private void createDelta()
	{
		doRun(() -> {
			

			try
			{
				return DiffExecutionCreator.createDiffExecutor(deltaCalculatorVersion.getValue(), deltaInitialStateIBDF, deltaEndState.getValue(), 
						deltaResultVersion.getText(), deltaAuthor.getValue(), deltaDateTime.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 
						deltaIgnoreTime.isSelected(), deltaIgnoreSibling.isSelected(), deltaGenerateRetires.isSelected(), (opTag.isSelected() ? sp_.getGitURL() : null),
						sp_.getGitUsername(), sp_.getGitPassword(), 
						new File(workingFolder.getText()), false);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}, () -> {
			try
			{
				ArrayList<DeployFile> temp = new ArrayList<>();
//				String updatedArtifactName = sourceUploadType.getSelectionModel().getSelectedItem().getArtifactId().replace("*",
//						sourceUploadExtension.getText());
//				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, updatedArtifactName, sourceUploadVersion.getText(), "", "pom",
//						new File(new File(workingFolder.getText()), SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/pom.xml"), 
//						isSnapshot(sourceUploadVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(),
//						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
//
//				temp.add(new DeployFile(SrcUploadCreator.SRC_UPLOAD_GROUP, updatedArtifactName, sourceUploadVersion.getText(), "", "zip",
//						new File(new File(workingFolder.getText()),
//								SrcUploadCreator.WORKING_SUB_FOLDER_NAME + "/target/" + updatedArtifactName + "-" + sourceUploadVersion.getText() + ".zip"),
//						isSnapshot(sourceUploadVersion.getText()) ? sp_.getArtifactSnapshotDeployURL() : sp_.getArtifactReleaseDeployURL(), 
//						sp_.getArtifactUsername(), new String(sp_.getArtifactPassword())));
				return temp;
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}

		}, new File(workingFolder.getText() + "/" + DiffExecutionCreator.WORKING_SUBFOLDER));
	}
	
	private boolean isSnapshot(String versionString)
	{
		return versionString.toUpperCase().endsWith("-SNAPSHOT");
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

				if (opInstall.isSelected() || opDeploy.isSelected() || opDirectDeploy.isSelected() || opPackage.isSelected())
				{
					ArrayList<String> options = new ArrayList<>();
					options.add("-s");
					options.add(sp_.getMavenSettingsFile());
					options.add("-e");
					options.add("-llr");  //Don't validate files with the path where they came from (legacy mode)
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
					ta.appendText("Starting Maven Execution\n");
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
							deploy.messageProperty().addListener((change) -> {
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
					try
					{
						RecursiveDelete.delete(new File(workingFolder.getText()));
					}
					catch (Exception e)
					{
						log.error("Error running cleanup: ", e);
					}
				}

				return null;
			}
		};

		Get.workExecutors().getExecutor().execute(t);
		ProgressDialog pd = new ProgressDialog(t);
		pd.initOwner(cm_.getPrimaryStage().getOwner());
		pdRef.set(pd);
		pd.setTitle("Building Configuration");
		pd.setHeaderText(null);
		pd.setContentText("Building Configuration");
		pd.setResizable(true);
		pd.showAndWait();

		try
		{
			t.get();
			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Complete");
			alert.setHeaderText("Job complete");
			alert.initOwner(cm_.getPrimaryStage().getOwner());
			alert.setResizable(true);
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
			errorAlert.initOwner(cm_.getPrimaryStage().getOwner());
			errorAlert.setResizable(true);
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
		optionsReadMavenArtifacts.setOnAction((action) -> readData(cm_.sp_));
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
			mavenPathsDialog.initOwner(cm_.getPrimaryStage().getOwner());
			mavenPathsDialog.setResizable(true);

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
			artifactDialog.initOwner(cm_.getPrimaryStage().getOwner());
			artifactDialog.setResizable(true);

			artifactController.artifactReadUrl.setText(sp_.getArtifactReadURL());
			artifactController.artifactReleaseDeployUrl.setText(sp_.getArtifactReleaseDeployURL());
			artifactController.artifactSnapshotDeployUrl.setText(sp_.getArtifactSnapshotDeployURL());
			artifactController.artifactUsername.setText(sp_.getArtifactUsername());
			artifactController.artifactPassword.setText(new String(sp_.getArtifactPassword()));

			if (artifactDialog.showAndWait().orElse(null) == ButtonType.OK)
			{
				sp_.setArtifactReadURL(artifactController.artifactReadUrl.getText());
				sp_.setArtifactSnapshotDeployURL(artifactController.artifactSnapshotDeployUrl.getText());
				sp_.setArtifactReleaseDeployURL(artifactController.artifactReleaseDeployUrl.getText());
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
			gitDialog.initOwner(cm_.getPrimaryStage().getOwner());
			gitDialog.setResizable(true);

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

		Task<Void> taskOne = MavenArtifactUtils.readAvailableMetadataVersions(sp_, results ->
		{
			Platform.runLater(() -> {
				databaseMetadataVersion.getItems().clear();
				results.forEach(value -> databaseMetadataVersion.getItems().add(value));
				databaseMetadataVersion.getSelectionModel().select(databaseMetadataVersion.getItems().size() - 1);
			});
		});
		Task<Void> taskTwo = readSourceUploadExistingVersions();
		Task<Void> taskThree = MavenArtifactUtils.readAvailableIBDFFiles(false, sp_, (results) -> 
		{
			ibdfFiles_.clear();
			ibdfFiles_.addAll(results);
		});
		Task<Void> taskFour = MavenArtifactUtils.readAvailableSourceFiles(sp_, items -> 
		{
			sdoSourceFiles_.clear();
			sdoSourceFiles_.addAll(items);
		});
		Task<Void> taskFive = MavenArtifactUtils.readAvailableConverterVersions(sp_, results -> 
		{
			Platform.runLater(() ->
			{
				sourceConversionConverterVersion.getItems().clear();
				deltaCalculatorVersion.getItems().clear();
				results.forEach(version -> 
				{
					sourceConversionConverterVersion.getItems().add(version);
					//Its not _quite_ correct to use these versions for the delta calculator version, since it may not be present, 
					//but its close enough for our use case.
					deltaCalculatorVersion.getItems().add(version);
				});
				sourceConversionConverterVersion.getSelectionModel().select(Math.max(0, sourceConversionConverterVersion.getItems().size() - 1));
				deltaCalculatorVersion.getSelectionModel().select(Math.max(0, deltaCalculatorVersion.getItems().size() - 1));
			});
		});
		
		//these will only appear for ones that aren't done yet
		FxUtils.waitWithProgress("Reading Metadata Versions", "Reading available Metadata Versions", taskOne, cm_.getPrimaryStage().getScene().getWindow());
		FxUtils.waitWithProgress("Reading Source Upload Existing Versions", "Reading available Source Upload Existing Versions", taskTwo, cm_.getPrimaryStage().getScene().getWindow());
		FxUtils.waitWithProgress("Reading IBDF Files", "Reading available IBDF Files", taskThree, cm_.getPrimaryStage().getScene().getWindow());
		FxUtils.waitWithProgress("Reading SDO Source Files", "Reading available SDO Source Files", taskFour, cm_.getPrimaryStage().getScene().getWindow());
		FxUtils.waitWithProgress("Reading Available Converter Versions", "Reading Available Converter Versions", taskFive, cm_.getPrimaryStage().getScene().getWindow());
	}
}
