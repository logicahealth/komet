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
package sh.komet.gui.search.extended;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.index.IndexStatusListener;
import sh.isaac.api.util.Interval;
import sh.isaac.api.util.NumericUtils;
import sh.isaac.api.util.TaskCompleteCallback;
import sh.isaac.model.index.SemanticIndexerConfiguration;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.provider.query.search.CompositeSearchResult;
import sh.isaac.provider.query.search.SearchHandle;
import sh.isaac.provider.query.search.SearchHandler;
import sh.isaac.utility.Frills;
import sh.isaac.utility.NumericUtilsDynamic;
import sh.isaac.utility.SimpleDisplayConcept;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.ConceptNode;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.util.ValidBooleanBinding;

/**
 * Controller class for the Extended Search View.
 * <p>
 * Logic was initially copied LEGO {@code SnomedSearchController}. 
 * Has been enhanced / rewritten much since then.
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ExtendedSearchViewController implements TaskCompleteCallback<SearchHandle>, IndexStatusListener
{
	private static final Logger LOG = LogManager.getLogger(ExtendedSearchViewController.class);

	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private BorderPane borderPane;
	@FXML private TextField searchText;
	@FXML private Button searchButton;
	@FXML private ProgressIndicator searchProgress;
	@FXML private ChoiceBox<SearchInOptions> searchIn;
	@FXML private ChoiceBox<Integer> searchLimit;
	@FXML private ListView<CompositeSearchResult> searchResults;
	@FXML private TitledPane optionsPane;
	@FXML private HBox searchInRefexHBox;
	@FXML private VBox optionsContentVBox;
	@FXML private HBox searchInDescriptionHBox;
	@FXML private HBox searchInIdentifierHBox;
	@FXML private ChoiceBox<SimpleDisplayConcept> descriptionTypeSelection;
	@FXML private CheckBox treatAsString;
	@FXML private ChoiceBox<SimpleDisplayConcept> searchInIdentifiers;	
	@FXML private ToolBar toolBar;
	@FXML private Label statusLabel;

	private final BooleanProperty searchRunning = new SimpleBooleanProperty(false);
	private SearchHandle ssh = null;
	private ConceptNode searchInSemantics;
	private ObservableList<SimpleDisplayConcept> dynamicRefexList_ = new ObservableListWrapper<>(new ArrayList<>());
	private Tooltip searchTextTooltip = new Tooltip();
	private Integer currentlyEnteredAssemblageNid = null;
	private FlowPane searchInColumnsHolder = new FlowPane();
	private enum SearchInOptions {Descriptions, Identifiers, Semantics};
	private SimpleBooleanProperty displayIndexConfigMenu_ = new SimpleBooleanProperty(false);
	private Manifold manifold;

	public static ExtendedSearchViewController init(Manifold manifold) 
	{
		// Load from FXML.
		try
		{
			URL resource = ExtendedSearchViewController.class.getResource("ExtendedSearchView.fxml");
			FXMLLoader loader = new FXMLLoader(resource);
			loader.load();
			ExtendedSearchViewController esvc = loader.getController();
			esvc.manifold = manifold;
			return esvc;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unexpected", e);
		}
	}

	@FXML
	public void initialize()
	{
		assert borderPane != null : "fx:id=\"borderPane\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchText != null : "fx:id=\"searchText\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchButton != null : "fx:id=\"searchButton\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchProgress != null : "fx:id=\"searchProgress\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchIn != null : "fx:id=\"searchIn\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchResults != null : "fx:id=\"searchResults\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert optionsPane != null : "fx:id=\"optionsPane\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert searchInRefexHBox != null : "fx:id=\"searchInRefexHBox\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";
		assert optionsContentVBox != null : "fx:id=\"optionsContentVBox\" was not injected: check your FXML file 'ExtendedSearchView.fxml'.";

		borderPane.getStylesheets().add(ExtendedSearchViewController.class.getResource("/styles/extendedSearch.css").toString());
		
		searchIn.getItems().add(SearchInOptions.Descriptions);
		searchIn.getItems().add(SearchInOptions.Identifiers);
		searchIn.getItems().add(SearchInOptions.Semantics);

		searchIn.getSelectionModel().select(0);
		
		searchTextTooltip.setText("Enter the description text to search for.  Advanced query syntax such as 'AND', 'NOT' is supported.  You may also enter UUIDs for concepts.");
		searchTextTooltip.setWrapText(true);
		searchTextTooltip.setMaxWidth(600);
		searchText.setTooltip(searchTextTooltip);
		
		optionsContentVBox.getChildren().remove(searchInRefexHBox);
		optionsContentVBox.getChildren().remove(searchInIdentifierHBox);
		optionsContentVBox.getChildren().remove(treatAsString);
		
		
		searchIn.valueProperty().addListener((change) ->
		{
			if (searchIn.getSelectionModel().getSelectedItem() == SearchInOptions.Descriptions)
			{
				searchTextTooltip.setText("Enter the description text to search for.  Advanced query syntax such as 'AND', 'NOT', 'OR' is supported.  You may also enter UUIDs "
						+ "or NIDs for concepts.");
				optionsContentVBox.getChildren().remove(searchInRefexHBox);
				optionsContentVBox.getChildren().remove(searchInColumnsHolder);
				optionsContentVBox.getChildren().remove(treatAsString);
				optionsContentVBox.getChildren().remove(searchInIdentifierHBox);
				optionsContentVBox.getChildren().add(searchInDescriptionHBox);
				searchInSemantics.clear();  //make sure an invalid state here doesn't prevent the search, when the field is hidden.
			}
			else if (searchIn.getSelectionModel().getSelectedItem() == SearchInOptions.Identifiers)
			{
				optionsContentVBox.getChildren().remove(searchInRefexHBox);
				optionsContentVBox.getChildren().remove(searchInColumnsHolder);
				optionsContentVBox.getChildren().remove(treatAsString);
				optionsContentVBox.getChildren().remove(searchInDescriptionHBox);
				optionsContentVBox.getChildren().add(searchInIdentifierHBox);
			}
			else if (searchIn.getSelectionModel().getSelectedItem() == SearchInOptions.Semantics)
			{
				searchTextTooltip.setText("Enter the sememe value to search for.  Advanced query syntax such as 'AND', 'NOT', 'OR' is supported for sememe data fields that "
						+ "are indexed as string values.  For numeric values, mathematical interval syntax is supported - such as [4,6] or (-5,10]."
						+ "  You may also search for 1 or more UUIDs and/or NIDs.");
				optionsContentVBox.getChildren().remove(searchInDescriptionHBox);
				optionsContentVBox.getChildren().remove(searchInIdentifierHBox);
				optionsContentVBox.getChildren().add(treatAsString);
				
				if (!searchInRefexHBox.getChildren().contains(searchInSemantics.getNode()))
				{
					searchInRefexHBox.getChildren().add(searchInSemantics.getNode());
				}
				optionsContentVBox.getChildren().add(searchInRefexHBox);
				
				if (searchInColumnsHolder.getChildren().size() > 0)
				{
					optionsContentVBox.getChildren().add(searchInColumnsHolder);
				}
			}
			else
			{
				throw new RuntimeException("oops");
			}
		});
		
		searchInIdentifiers.getItems().add(new SimpleDisplayConcept("Any", Integer.MIN_VALUE));
		searchInIdentifiers.getItems().add(new SimpleDisplayConcept("Nid", MetaData.NID____SOLOR.getNid()));
		for (ConceptChronology cc : Frills.getIdentifierAssemblages())
		{
			searchInIdentifiers.getItems().add(new SimpleDisplayConcept(cc.getNid()));
		}
		
		searchInIdentifiers.getItems().sort(new Comparator<SimpleDisplayConcept>()
		{
			@Override
			public int compare(SimpleDisplayConcept o1, SimpleDisplayConcept o2)
			{
				if (o1.getNid() == Integer.MIN_VALUE)
				{
					return -1;
				}
				else if (o2.getNid() == Integer.MIN_VALUE)
				{
					return 1;
				}
				
				else if (o1.getNid() == MetaData.UUID____SOLOR.getNid())
				{
					return -1;
				}
				else if (o2.getNid() == MetaData.UUID____SOLOR.getNid())
				{
					return 1;
				}
				
				else if (o1.getNid() == MetaData.NID____SOLOR.getNid())
				{
					return -1;
				}
				else if (o2.getNid() == MetaData.NID____SOLOR.getNid())
				{
					return 1;
				}
				else
				{
					return o1.getDescription().compareTo(o2.getDescription());
				}
			}});
		
		searchInIdentifiers.getSelectionModel().clearAndSelect(0);
		
		searchInSemantics = new ConceptNode(null, false, dynamicRefexList_, null, () -> {return manifold;}, false);
		searchInSemantics.getConceptProperty().addListener(new InvalidationListener()
		{
			@Override
			public void invalidated(Observable observable)
			{
				ConceptSnapshot newValue = searchInSemantics.getConceptProperty().get();
				if (newValue != null)
				{
					searchInColumnsHolder.getChildren().clear();
					try
					{
						DynamicUsageDescription rdud = LookupService.get().getService(DynamicUtility.class).readDynamicUsageDescription(newValue.getNid());
						displayIndexConfigMenu_.set(true);
						currentlyEnteredAssemblageNid = rdud.getDynamicUsageDescriptorNid();
						Integer[] indexedColumns = SemanticIndexerConfiguration.readIndexInfo(currentlyEnteredAssemblageNid);
						if (indexedColumns == null || indexedColumns.length == 0)
						{
							searchInSemantics.isValid().setInvalid("Sememe searches can only be performed on indexed columns in the sememe.  The selected "
									+ "sememe does not contain any indexed data columns.  Please configure the indexes to search this sememe.");
							optionsContentVBox.getChildren().remove(searchInColumnsHolder);
						}
						else
						{
							Label l = new Label("Search in Columns");
							searchInColumnsHolder.getChildren().add(l);
							l.minWidthProperty().bind(((Label)searchInRefexHBox.getChildren().get(0)).widthProperty());
							DynamicColumnInfo[] rdci = rdud.getColumnInfo();
							if (rdci.length > 0)
							{
								Arrays.sort(rdci);  //We will depend on them being in the correct order later.
								HashSet<Integer> indexedColumnsSet = new HashSet<>(Arrays.asList(indexedColumns));
								int indexNumber = 0;
								for (DynamicColumnInfo ci : rdci)
								{
									StackPane cbStack = new StackPane();
									CheckBox cb = new CheckBox(ci.getColumnName());
									if (ci.getColumnDataType() == DynamicDataType.BYTEARRAY || !indexedColumnsSet.contains(indexNumber))
									{
										cb.setDisable(true);  //No index on this column... not searchable
										Tooltip.install(cbStack, new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName() + " is not indexed"));
									}
									else
									{
										cb.setSelected(true);
										cb.setTooltip(new Tooltip("Column Datatype: " + ci.getColumnDataType().getDisplayName()));
									}
									cbStack.getChildren().add(cb);
									searchInColumnsHolder.getChildren().add(cbStack);
									indexNumber++;
								}
								if (!optionsContentVBox.getChildren().contains(searchInColumnsHolder))
								{
									optionsContentVBox.getChildren().add(searchInColumnsHolder);
								}
							}
							else
							{
								searchInSemantics.isValid().setInvalid("Sememe searches can only be performed on the data in the sememe.  The selected "
										+ "sememe does not contain any data columns.");
								optionsContentVBox.getChildren().remove(searchInColumnsHolder);
							}
						}
					}
					catch (Exception e1)
					{
						//Not a dynamic sememe, treat as a static sememe.
						currentlyEnteredAssemblageNid = newValue.getNid();
						displayIndexConfigMenu_.set(false);
						optionsContentVBox.getChildren().remove(searchInColumnsHolder);
						searchInColumnsHolder.getChildren().clear();
					}
				}
				else
				{
					currentlyEnteredAssemblageNid = null;
					displayIndexConfigMenu_.set(false);
					optionsContentVBox.getChildren().remove(searchInColumnsHolder);
					searchInColumnsHolder.getChildren().clear();
				}
			}
		});
		
		//TODO port index config view?
//		MenuItem configureIndex =  new MenuItem("Configure Sememe Indexing");
//		configureIndex.setOnAction((action) ->
//		{
//			ConceptSnapshot c = searchInSemantics.getConceptProperty().get();
//			if (c != null)
//			{
//				new ConfigureDynamicRefexIndexingView(c.getNid()).showView(null);
//			}
//		});
//		configureIndex.setGraphic(Images.CONFIGURE.createImageView());
//		configureIndex.visibleProperty().bind(displayIndexConfigMenu_);
//		searchInSemantics.addMenu(configureIndex);
		
		searchLimit.setConverter(new StringConverter<Integer>()
		{
			@Override
			public String toString(Integer object)
			{
				return object == Integer.MAX_VALUE ? "No Limit" : object.toString(); 
			}

			@Override
			public Integer fromString(String string)
			{
				// not needed
				return null;
			}
		});
		
		searchLimit.getItems().add(100);
		searchLimit.getItems().add(500);
		searchLimit.getItems().add(1000);
		searchLimit.getItems().add(10000);
		searchLimit.getItems().add(100000);
		searchLimit.getItems().add(Integer.MAX_VALUE);
		searchLimit.getSelectionModel().select(0);
		
		searchInRefexHBox.getChildren().add(searchInSemantics.getNode());
		HBox.setHgrow(searchInSemantics.getNode(), Priority.ALWAYS);
		
		descriptionTypeSelection.getItems().add(new SimpleDisplayConcept("All", Integer.MIN_VALUE));
		descriptionTypeSelection.getItems().add(new SimpleDisplayConcept("Fully Qualified Name", MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid()));
		descriptionTypeSelection.getItems().add(new SimpleDisplayConcept("Regular Name", MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid()));
		descriptionTypeSelection.getItems().add(new SimpleDisplayConcept("Definition", MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid()));
		descriptionTypeSelection.getItems().add(new SimpleDisplayConcept("--- Terminology Types ---", Integer.MAX_VALUE));
		
		descriptionTypeSelection.valueProperty().addListener((change) ->
		{
			if (descriptionTypeSelection.getValue().getNid() == Integer.MAX_VALUE)
			{
				descriptionTypeSelection.getSelectionModel().clearAndSelect(0);
			}
		});
		
		descriptionTypeSelection.getSelectionModel().clearAndSelect(0);
		
		searchInColumnsHolder.setHgap(10);
		searchInColumnsHolder.setVgap(5.0);

		searchResults.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		searchResults.setCellFactory(new Callback<ListView<CompositeSearchResult>, ListCell<CompositeSearchResult>>()
		{
			@Override
			public ListCell<CompositeSearchResult> call(ListView<CompositeSearchResult> arg0)
			{
				return new ListCell<CompositeSearchResult>()
				{
					@Override
					protected void updateItem(final CompositeSearchResult item, boolean empty)
					{
						super.updateItem(item, empty);
						if (!empty)
						{
							VBox box = new VBox();
							box.setFillWidth(true);
							
							String conceptText = item.getContainingConceptText();

							HBox hb = new HBox();
							Label concept = new Label("Referenced Concept");
							concept.getStyleClass().add("boldLabel");
							hb.getChildren().add(concept);
							hb.getChildren().add(new Label("  " + conceptText));
							
							box.getChildren().add(hb);
						
							List<String> strings = item.getMatchingStrings();
							List<Version> versions = item.getMatchingComponentVersions();
							
							for (int i = 0; i < strings.size(); i++)
							{
								if (versions.get(i).getChronology().getIsaacObjectType() == IsaacObjectType.SEMANTIC)
								{
									HBox assemblageConBox = new HBox();
									Label assemblageCon = new Label("Assemblage Concept");
									assemblageCon.getStyleClass().add("boldLabel");
									HBox.setMargin(assemblageCon, new Insets(0.0, 0.0, 0.0, 10.0));
									assemblageConBox.getChildren().add(assemblageCon);
									assemblageConBox.getChildren().add(new Label("  " + Get.conceptDescriptionText(versions.get(i).getAssemblageNid())));
									box.getChildren().add(assemblageConBox);
								}

								Label attachedData = new Label("Attached Data");
								attachedData.getStyleClass().add("boldLabel");
								VBox.setMargin(attachedData, new Insets(0.0, 0.0, 0.0, 10.0));
								box.getChildren().add(attachedData);
								Label l = new Label(strings.get(i));
								VBox.setMargin(l, new Insets(0.0, 0.0, 0.0, 20.0));
								box.getChildren().add(l);
							}
							
							StringBuilder tooltip = new StringBuilder();
							tooltip.append("Modules:\r");
							HashSet<Integer> modules = new HashSet<>();
							for (Chronology chronology : item.getMatchingComponents())
							{
								for (Version sv : chronology.getVersionList())
								{
									modules.add(sv.getModuleNid());
								}
							}
							
							for (int i : modules)
							{
								tooltip.append(Frills.getDescription(i, manifold).orElse("Unknown module") + "\r");
							}
							
							tooltip.setLength(tooltip.length() - 1);
							
							Tooltip.install(box, new Tooltip(tooltip.toString()));
							setGraphic(box);

							// Also show concept details on double-click.
							setOnMouseClicked(new EventHandler<MouseEvent>()
							{
								@Override
								public void handle(MouseEvent mouseEvent)
								{
									if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
									{
										if (mouseEvent.getClickCount() == 2)
										{
											 manifold.setFocusedConceptChronology(item.getContainingConcept());
										}
									}
								}
							});

							//TODO port menus?
//							ContextMenu cm = new ContextMenu();
//							CommonMenusDataProvider dp = new CommonMenusDataProvider()
//							{
//								@Override
//								public String[] getStrings()
//								{
//									List<String> items = new ArrayList<>();
//									for (CompositeSearchResult currentItem : searchResults.getSelectionModel().getSelectedItems())
//									{
//										Optional<ConceptSnapshot> currentWbConcept = currentItem.getContainingConcept();
//										if (!currentWbConcept.isPresent())
//										{
//											//not on path, most likely
//											continue;
//										}
//										items.add(currentWbConcept.get().getConceptDescriptionText());
//									}
//
//									String[] itemArray = items.toArray(new String[items.size()]);
//
//									return itemArray;
//								}
//							};
//							CommonMenusNIdProvider nidProvider = new CommonMenusNIdProvider()
//							{
//								@Override
//								public Set<Integer> getNIds()
//								{
//									Set<Integer> nids = new HashSet<>();
//
//									for (CompositeSearchResult r : searchResults.getSelectionModel().getSelectedItems())
//									{
//										if (r.getContainingConcept().isPresent())
//										{
//											nids.add(r.getContainingConcept().get().getNid());
//										}
//										
//									}
//									return nids;
//								}
//							}; 
//							CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
//				
//							menuBuilder.setMenuItemsToExclude(
//									CommonMenus.CommonMenuItem.LOINC_REQUEST_VIEW, 
//									CommonMenus.CommonMenuItem.USCRS_REQUEST_VIEW,
//									CommonMenus.CommonMenuItem.WORKFLOW_INITIALIZATION_VIEW);
//							CommonMenus.addCommonMenus(cm, menuBuilder, dp, nidProvider);
//
//							setContextMenu(cm);
						}
						else
						{
							setText("");
							setGraphic(null);
						}
					}
				};
			}
		});

		//TODO integrate drag and drop?
//		AppContext.getService(DragRegistry.class).setupDragOnly(searchResults, new SingleConceptIdProvider()
//		{
//			@Override
//			public String getConceptId()
//			{
//				CompositeSearchResult dragItem = searchResults.getSelectionModel().getSelectedItem();
//				if (dragItem != null)
//				{
//					if (dragItem.getContainingConcept().isPresent())
//					{
//						return dragItem.getContainingConcept().get().getNid() + "";
//					}
//				}
//				return null;
//			}
//		});

		final ValidBooleanBinding searchTextValid = new ValidBooleanBinding()
		{
			{
				bind(searchText.textProperty(), searchIn.valueProperty());
				setComputeOnInvalidate(true);
			}
			@Override
			protected boolean computeValue()
			{
				//Just piggy backing to update another field on change...
				if (searchIn.getValue() == SearchInOptions.Semantics)
				{
					if (NumericUtils.isNumber(searchText.getText()))
					{
						treatAsString.setDisable(false);
						treatAsString.setSelected(false);
					}
					else if (Interval.isInterval(searchText.getText()))
					{
						treatAsString.setDisable(false);
						treatAsString.setSelected(false);
					}
					else
					{
						//can't treat it as a number or interval
						treatAsString.setDisable(true);
						treatAsString.setSelected(true);
					}
				}
				
				if ((searchIn.getValue() == SearchInOptions.Semantics && searchText.getText().length() > 0) || searchText.getText().length() > 1)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};
		
		searchProgress.visibleProperty().bind(searchRunning);
		searchButton.disableProperty().bind(searchTextValid.not().or(searchInSemantics.isValid().not()));

		// Perform search or cancel when button pressed.
		searchButton.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (searchRunning.get() && ssh != null)
				{
					ssh.cancel();
				}
				else
				{
					search();
				}
			}
		});

		// Change button text while search running.
		searchRunning.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				if (searchRunning.get())
				{
					searchButton.setText("Cancel");
				}
				else
				{
					searchButton.setText("Search");
				}
			}
		});

		// Perform search on Enter keypress.
		searchText.setOnAction(e -> 
		{
			if (searchTextValid.getValue() && !searchRunning.get())
			{
				search();
			}
		});
	}

	public BorderPane getRoot()
	{
		//delay this
		if (dynamicRefexList_.size() == 0)
		{
			populateDynamicSememeList();
		}
		if (descriptionTypeSelection.getItems().size() == 5)
		{
			populateExtendedDescriptionList();
		}
		return borderPane;
	}

	@Override
	public void taskComplete(SearchHandle sh, long taskStartTime, Integer taskId)
	{
		// Run on JavaFX thread.
		Platform.runLater(() -> 
		{
			try
			{
				if (!ssh.isCancelled())
				{
					Collection<CompositeSearchResult> results = ssh.getResults();
					
					searchResults.getItems().addAll(results);
					long time = System.currentTimeMillis() - ssh.getSearchStartTime();
					float inSeconds = (float)time / 1000f;
					inSeconds = ((float)((int)(inSeconds * 100f)) / 100f);
					
					String statusMsg = ssh.getHitCount() + " in " + inSeconds + " seconds";
					if (ssh.getOffPathFilteredCount() > 0) {
						statusMsg += "; " + ssh.getOffPathFilteredCount() + " off-path entries ignored";
					}
					statusLabel.setText(statusMsg);
				}
				else
				{
					statusLabel.setText("Search Cancelled");
				}
			}
			catch (Exception ex)
			{
				String title = "Unexpected Search Error";
				LOG.error(title, ex);
				FxGet.dialogs().showErrorDialog(title, "There was an unexpected error running the search", ex.toString());
				searchResults.getItems().clear();
				statusLabel.setText("Search Failed");
			}
			finally
			{
				searchRunning.set(false);
			}
		});
	}
	
	private int[] getSearchColumns()
	{
		if (searchInColumnsHolder.getChildren().size() > 1)
		{
			ArrayList<Integer> result = new ArrayList<>();
			int deselectedCount = 0;
			for (int i = 1; i < searchInColumnsHolder.getChildren().size(); i++)
			{
				CheckBox cb = ((CheckBox)((StackPane)searchInColumnsHolder.getChildren().get(i)).getChildren().get(0));
				if (cb.isSelected())
				{
					result.add(i - 1);
				}
				else if (!cb.isDisable())
				{
					deselectedCount++;
				}
			}
			//If they didn't uncheck any, its more efficient to query without the column filter.
			if (deselectedCount == 0)
			{
				return null;
			}
			else
			{
				int[] resultToReturn = new int[result.size()];
				for (int i = 0; i < resultToReturn.length; i++)
				{
					resultToReturn[i] = result.get(i);
				}
				return resultToReturn;
			}
		}
		return null;
	}

	private synchronized void search()
	{
		try
		{
			// Sanity check if search already running.
			if (searchRunning.get())
			{
				return;
			}
	
			searchRunning.set(true);
			searchResults.getItems().clear();
			// we get called back when the results are ready.
			
			if (searchIn.getValue() == SearchInOptions.Descriptions)
			{
				int[] descriptionTypeRestriction;
				int[] extendedDescriptionTypeRestriction;
				
				if (descriptionTypeSelection.getValue().getNid() == Integer.MIN_VALUE)
				{
					LOG.debug("Doing a description search across all description types");
					descriptionTypeRestriction = null;
					extendedDescriptionTypeRestriction = null;
				}
				else if (descriptionTypeSelection.getValue().getNid() == MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid())
				{
					LOG.debug("Doing a description search on FQN");
					descriptionTypeRestriction = new int[] {descriptionTypeSelection.getValue().getNid()};
					extendedDescriptionTypeRestriction = null;
				}
				else if (descriptionTypeSelection.getValue().getNid() == MetaData.DEFINITION_DESCRIPTION_TYPE____SOLOR.getNid())
				{
					LOG.debug("Doing a description search on Definition");
					descriptionTypeRestriction = new int[] {descriptionTypeSelection.getValue().getNid()};
					extendedDescriptionTypeRestriction = null;
				}
				else if (descriptionTypeSelection.getValue().getNid() == MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid())
				{
					LOG.debug("Doing a description search on Synonym");
					descriptionTypeRestriction = new int[] {descriptionTypeSelection.getValue().getNid()};
					extendedDescriptionTypeRestriction = null;
				}
				else
				{
					LOG.debug("Doing a description search on the extended type {}", descriptionTypeSelection.getValue().getDescription());
					descriptionTypeRestriction = null;
					extendedDescriptionTypeRestriction = new int[] {descriptionTypeSelection.getValue().getNid()};
				}
				
				ssh = SearchHandler.search(() -> 
				{
					return Get.service(IndexDescriptionQueryService.class).query(searchText.getText(), false, null, 
							null, null, false, descriptionTypeRestriction, 
							extendedDescriptionTypeRestriction, 1, searchLimit.getValue(), null);
				}, 
				((searchHandle) -> {taskComplete(null, searchHandle.getSearchStartTime(), searchHandle.getTaskId());}),
				null, null, true, manifold.getManifoldCoordinate(), true);
			}
			else if (searchIn.getValue() == SearchInOptions.Identifiers)
			{
				LOG.debug("Doing an identifier search");
				String searchString = searchText.getText().trim();

				ssh = SearchHandler.searchIdentifiers(searchString, 
						searchInIdentifiers.getSelectionModel().getSelectedItem().getNid() == Integer.MIN_VALUE ? null 
								: new int[] {searchInIdentifiers.getSelectionModel().getSelectedItem().getNid()}, 
								((searchHandle) -> {taskComplete(null, searchHandle.getSearchStartTime(), searchHandle.getTaskId());}),
						null, null, true, manifold.getManifoldCoordinate(), true, searchLimit.getValue());
			}
			else if (searchIn.getValue() == SearchInOptions.Semantics)
			{
				String searchString = searchText.getText().trim();
				if (NumericUtils.isNumber(searchString) && !treatAsString.isSelected())
				{
					DynamicData data = NumericUtilsDynamic.wrapIntoRefexHolder(NumericUtilsDynamic.parseUnknown(searchString));
					LOG.debug("Doing a sememe search with a numeric value");
					ssh = SearchHandler.search(() -> 
					{
						return Get.service(IndexSemanticQueryService.class).queryData(data, false, 
								currentlyEnteredAssemblageNid == null ? null : new int[] {currentlyEnteredAssemblageNid}, 
										getSearchColumns(), null, 1, searchLimit.getValue(), null);
					}, 
					((searchHandle) -> {taskComplete(null, searchHandle.getSearchStartTime(), searchHandle.getTaskId());}),
					null, null, true, manifold.getManifoldCoordinate(), true);
				}
				else if (Interval.isInterval(searchString) && !treatAsString.isSelected())
				{
					Interval interval = new Interval(searchString);
					LOG.debug("Doing a sememe search with an interval value");
					ssh = SearchHandler.search(() -> 
					{
						return Get.service(IndexSemanticQueryService.class).queryNumericRange(
								interval.getLeft(), interval.isLeftInclusive(), 
								interval.getRight(), interval.isRightInclusive(), 
								currentlyEnteredAssemblageNid == null ? null : new int[] {currentlyEnteredAssemblageNid}, 
								getSearchColumns(), null, null, 1, searchLimit.getValue(), null);
					}, 
					((searchHandle) -> {taskComplete(null, searchHandle.getSearchStartTime(), searchHandle.getTaskId());}),
					null, null, true, manifold.getManifoldCoordinate(), true);
					
				}
				else
				{
					//run it as a string search
					LOG.debug("Doing a sememe search as a string search");
					ssh = SearchHandler.search(() -> 
					{
						return Get.service(IndexSemanticQueryService.class).queryData(new DynamicStringImpl(searchString), false, 
								currentlyEnteredAssemblageNid == null ? null : new int[] {currentlyEnteredAssemblageNid}, 
										getSearchColumns(), null, 1, searchLimit.getValue(), null);
					}, 
					((searchHandle) -> {taskComplete(null, searchHandle.getSearchStartTime(), searchHandle.getTaskId());}),
					null, null, true, manifold.getManifoldCoordinate(), true);
				}
			}
			else
			{
				throw new RuntimeException("oops");
			}
		}
		catch (Exception e)
		{
			LOG.error("Search imploded unexpectedly...", e);
			ssh = null;  //force a null ptr in taskComplete, so an error is displayed.
			taskComplete(null, 0, null);
		}
	}
	
	//TODO a listener to trigger this after a user makes a new one...
	private void populateDynamicSememeList()
	{
		Task<Void> t = new Task<Void>()
		{
			HashSet<SimpleDisplayConcept> dynamicRefexAssemblages = new HashSet<>();

			@Override
			protected Void call() throws Exception
			{
				dynamicRefexAssemblages = new HashSet<>();
				dynamicRefexAssemblages.addAll(Frills.getAllDynamicSemanticAssemblageConcepts());
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				dynamicRefexList_.clear();
				dynamicRefexList_.addAll(dynamicRefexAssemblages);
				dynamicRefexList_.sort(new Comparator<SimpleDisplayConcept>()
				{
					@Override
					public int compare(SimpleDisplayConcept o1, SimpleDisplayConcept o2)
					{
						return o1.getDescription().compareToIgnoreCase(o2.getDescription());
					}
				});
			}
		};
		
		Get.workExecutors().getExecutor().execute(t);
	}
	
	private void populateExtendedDescriptionList()
	{
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				Set<Integer> extendedDescriptionTypes = Frills.getAllChildrenOfConcept(
						MetaData.DESCRIPTION_TYPE_IN_SOURCE_TERMINOLOGY____SOLOR.getNid(), true, true, manifold.getStampCoordinate());
				ArrayList<SimpleDisplayConcept> temp = new ArrayList<>();
				for (Integer c : extendedDescriptionTypes)
				{
					temp.add(new SimpleDisplayConcept(c));
				}
				Collections.sort(temp);
				Platform.runLater(() ->
				{
					descriptionTypeSelection.getItems().addAll(temp);
				});
			}
			catch (Exception e1)
			{
				LOG.error("Error reading extended description types", e1);
			}
		});
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void indexConfigurationChanged(IndexQueryService indexConfigurationThatChanged)
	{
		Platform.runLater(() -> 
		{
			if (indexConfigurationThatChanged instanceof IndexSemanticQueryService)
			{
				//swap the concept in and out, to fire our change listener, so we recheck if the referenced concept is configured in a valid way.
				searchInSemantics.revalidate();
			}
		});
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void reindexBegan(IndexQueryService index)
	{
		//noop
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void reindexCompleted(IndexQueryService index)
	{
		//noop
	}
}
