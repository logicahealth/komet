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
package sh.isaac.komet.gui.semanticViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import com.sun.javafx.collections.ObservableMapWrapper;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Platform;
import javafx.beans.binding.FloatBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.index.IndexedGenerationCallable;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.dbConfigBuilder.fx.fxUtil.UpdateableBooleanBinding;
import sh.isaac.komet.gui.semanticViewer.HeaderNode.Filter;
import sh.isaac.komet.gui.semanticViewer.cells.AttachedDataCellFactory;
import sh.isaac.komet.gui.semanticViewer.cells.ComponentDataCell;
import sh.isaac.komet.gui.semanticViewer.cells.StatusCell;
import sh.isaac.komet.gui.semanticViewer.cells.StringCell;
import sh.isaac.komet.gui.util.TableHeaderRowTooltipInstaller;
import sh.isaac.komet.iconography.Iconography;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.komet.gui.contract.DetailNodeFactory;
import sh.komet.gui.contract.DetailType;
import sh.komet.gui.contract.DialogService;
import sh.komet.gui.control.ManifoldLinkedConceptLabel;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;

/**
 * 
 * DynamicSememeView
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@SuppressWarnings({ "unused", "restriction" })
@Service
@PerLookup
public class SemanticViewer implements DetailNodeFactory
{
	private VBox rootNode_ = null;
	private TreeTableView<SemanticGUI> ttv_;
	private TreeItem<SemanticGUI> treeRoot_;
	private Button retireButton_, addButton_, commitButton_, cancelButton_, editButton_, viewUsageButton_;
	private Label summary_ = new Label("");
	private ToggleButton stampButton_, activeOnlyButton_, historyButton_;
	private Button displayFSNButton_;
	private UpdateableBooleanBinding currentRowSelected_, selectedRowIsActive_;
	private UpdateableBooleanBinding showStampColumns_, showActiveOnly_, showFullHistory_, showViewUsageButton_;
	private TreeTableColumn<SemanticGUI, String> stampColumn_;
	private BooleanProperty hasUncommitted_ = new SimpleBooleanProperty(false);

	private Text placeholderText = new Text("No Dynamic Sememes were found associated with the component");
	private ProgressBar progressBar_;
	
	private Button clearColumnHeaderNodesButton_ = new Button("Clear Filters");
	
	private Logger logger_ = LogManager.getLogger(this.getClass());

	ViewFocus viewFocus_;
	int viewFocusNid_;
	private Manifold manifoldConcept_;
	private ManifoldLinkedConceptLabel titleLabel = null;
	private final SimpleStringProperty titleProperty = new SimpleStringProperty("empty");
	private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("empty");

	IndexedGenerationCallable newComponentIndexGen_ = null; //Useful when adding from the assemblage perspective - if they are using an index, we need to wait till the new thing is indexed
	private AtomicInteger noRefresh_ = new AtomicInteger(0);
	
	// Display refreshes on change of UserProfileBindings.getViewCoordinatePath() or UserProfileBindings.getDisplayFSN()
//	private UpdateableBooleanBinding refreshRequiredListenerHack;

	private final ObservableMap<ColumnId, Filter<?>> filterCache_ = new ObservableMapWrapper<>(new WeakHashMap<>());
	
	BooleanProperty displayFSN_ =  new SimpleBooleanProperty(false);
	
	// Display refreshes on change of UserProfileBindings.getDisplayFSN().
	// If UserProfileBindings.getDisplayFSN() at time of refresh has changed since last refresh
	// then all filters must be cleared, because they may contain outdated display text values
	private boolean filterCacheLastBuildDisplayFSNValue_ = displayFSN_.get();
	
	private final MapChangeListener<ColumnId, Filter<?>> filterCacheListener_ = new MapChangeListener<ColumnId, Filter<?>>() {
		@Override
		public void onChanged(
				javafx.collections.MapChangeListener.Change<? extends ColumnId, ? extends Filter<?>> c) {
			if (c.wasAdded() || c.wasRemoved()) {
				refresh();
			}
		}
	};
	private final ListChangeListener<Object> filterListener_ = new ListChangeListener<Object>() {
		@Override
		public void onChanged(
				javafx.collections.ListChangeListener.Change<? extends Object> c) {
			while (c.next()) {
				if (c.wasPermutated()) {
					// irrelevant
				} else if (c.wasUpdated()) {
					// irrelevant
				} else {
					refresh();
					break;
				}
			}
		}
	};
	private void addFilterCacheListeners() {
		removeFilterCacheListeners();
		filterCache_.addListener(filterCacheListener_);
		for (HeaderNode.Filter<?> filter : filterCache_.values()) {
			filter.getFilterValues().addListener(filterListener_);
		}
	}
	private void removeFilterCacheListeners() {
		filterCache_.removeListener(filterCacheListener_);
		for (HeaderNode.Filter<?> filter : filterCache_.values()) {
			filter.getFilterValues().removeListener(filterListener_);
		}
	}

	private SemanticViewer() 
	{
		//Created by HK2 - no op - delay till getView called
	}
	
	private void initialInit()
	{
		if (rootNode_ == null)
		{
			noRefresh_.getAndIncrement();
			ttv_ = new TreeTableView<>();
			ttv_.setTableMenuButtonVisible(true);
			ttv_.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	
			treeRoot_ = new TreeItem<>();
			treeRoot_.setExpanded(true);
			ttv_.setShowRoot(false);
			ttv_.setRoot(treeRoot_);
			progressBar_ = new ProgressBar(-1);
			progressBar_.setPrefWidth(200);
			progressBar_.setPadding(new Insets(15, 15, 15, 15));
			ttv_.setPlaceholder(progressBar_);
			
			rootNode_ = new VBox();
			rootNode_.setFillWidth(true);
			rootNode_.getChildren().add(ttv_);
			VBox.setVgrow(ttv_, Priority.ALWAYS);
			
			ToolBar t = new ToolBar();
			
			clearColumnHeaderNodesButton_.setOnAction(event -> {
				removeFilterCacheListeners();
				for (HeaderNode.Filter<?> filter : filterCache_.values()) {
					filter.getFilterValues().clear();
				}
				refresh();
			});
			t.getItems().add(clearColumnHeaderNodesButton_);
			
			currentRowSelected_ = new UpdateableBooleanBinding()
			{
				{
					addBinding(ttv_.getSelectionModel().getSelectedItems());
				}
				@Override
				protected boolean computeValue()
				{
					if (ttv_.getSelectionModel().getSelectedItems().size() > 0 && 
							ttv_.getSelectionModel().getSelectedItem() != null && 
							ttv_.getSelectionModel().getSelectedItem().getValue() != null)
					{
						return ttv_.getSelectionModel().getSelectedItem().getValue().isCurrent();
					}
					else
					{
						return false;
					}
				}
			};
			
			selectedRowIsActive_ = new UpdateableBooleanBinding()
			{
				{
					addBinding(ttv_.getSelectionModel().getSelectedItems());
				}
				@Override
				protected boolean computeValue()
				{
					if (ttv_.getSelectionModel().getSelectedItems().size() > 0 && ttv_.getSelectionModel().getSelectedItem() != null 
							&& ttv_.getSelectionModel().getSelectedItem().getValue() != null)
					{
						return ttv_.getSelectionModel().getSelectedItem().getValue().getSememe().getStatus() == Status.ACTIVE;
					}
					else
					{
						return false;
					}
				}
			};
			
			retireButton_ = new Button(null, Images.MINUS.createImageView());
			retireButton_.setTooltip(new Tooltip("Retire Selected Sememe Extension(s)"));
			retireButton_.setDisable(true);
//TODO implement edit features?
//			retireButton_.disableProperty().bind(selectedRowIsActive_.and(currentRowSelected_).not());
//			retireButton_.setOnAction((action) ->
//			{
//				try
//				{
//					YesNoDialog dialog = new YesNoDialog(rootNode_.getScene().getWindow());
//					DialogResponse dr = dialog.showYesNoDialog("Retire?", "Do you want to retire the selected sememe entries?");
//					if (DialogResponse.YES == dr)
//					{
//						ObservableList<TreeItem<SemanticGUI>> selected = ttv_.getSelectionModel().getSelectedItems();
//						if (selected != null && selected.size() > 0)
//						{
//							for (TreeItem<SemanticGUI> refexTreeItem : selected)
//							{
//								SemanticGUI refex = refexTreeItem.getValue();
//								if (refex.getSememe().getStatus() == State.INACTIVE)
//								{
//									continue;
//								}
//								if (refex.getSememe().getChronology().getSememeType() == SememeType.DYNAMIC)
//								{
//									MutableDynamicSememe<?> mds = ((SememeChronology<DynamicSememe>)refex.getSememe().getChronology())
//											.createMutableVersion(MutableDynamicSememe.class, State.INACTIVE, ExtendedAppContext.getUserProfileBindings().getEditCoordinate().get());
//									mds.setData(((DynamicSememe<?>)refex.getSememe()).getData());
//									Get.commitService().addUncommitted(refex.getSememe().getChronology());
//									Get.commitService().commit("retire dynamic sememe").get();
//								}
//								else
//								{
//									//TODO
//									throw new RuntimeException("Not yet supported");
//								}
//							}
//							refresh();
//						}
//					}
//				}
//				catch (Exception e)
//				{
//					logger_.error("Unexpected error retiring sememe", e);
//					AppContext.getCommonDialogs().showErrorDialog("Error", "There was an unexpected error retiring the sememe", e.getMessage(), rootNode_.getScene().getWindow());
//				}
//			});
			
			t.getItems().add(retireButton_);
			
			addButton_ = new Button(null, Images.PLUS.createImageView());
			addButton_.setTooltip(new Tooltip("Add a new Sememe Extension"));
			addButton_.setDisable(true);
			//TODO implement edit features?
//			addButton_.setOnAction((action) ->
//			{
//				AddSememePopup arp = AppContext.getService(AddSememePopup.class);
//				arp.finishInit(viewFocus_, viewFocusNid_, this);
//				arp.showView(rootNode_.getScene().getWindow());
//			});
			
			addButton_.setDisable(true);
			t.getItems().add(addButton_);
			
			editButton_ = new Button(null, Images.EDIT.createImageView());
			editButton_.setTooltip(new Tooltip("Edit a Sememe"));
			editButton_.setDisable(true);
			//TODO implement edit features?
//			editButton_.disableProperty().bind(currentRowSelected_.not());
//			editButton_.setOnAction((action) ->
//			{
//				AddSememePopup arp = AppContext.getService(AddSememePopup.class);
//				arp.finishInit(ttv_.getSelectionModel().getSelectedItem().getValue(), this);
//				arp.showView(rootNode_.getScene().getWindow());
//			});
			t.getItems().add(editButton_);
			
			viewUsageButton_ = new Button(null, Images.SEARCH.createImageView());
			viewUsageButton_.setTooltip(new Tooltip("The displayed concept also defines a dynamic sememe itself.  Click to see the usage of this sememe."));
			viewUsageButton_.setOnAction((action) ->
			{
				try
				{
					SemanticViewer driv = Get.service(SemanticViewer.class);
					driv.setAssemblage(viewFocusNid_, null, null, null, true);
					driv.showView(null);
				}
				catch (Exception e)
				{
					logger_.error("Error launching sememe dynamic member viewer", e);
					Get.service(DialogService.class).showErrorDialog("Error", "There was an unexpected launching the sememe member viewer", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
			});
			showViewUsageButton_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean show = false;
					if (viewFocus_ != null && viewFocus_ == ViewFocus.REFERENCED_COMPONENT && Get.conceptService().getOptionalConcept(viewFocusNid_).isPresent())
					{
						//Need to find out if this component has a the dynamic sememe definition annotation on it.
						try
						{
							LookupService.getService(DynamicUtility.class).readDynamicUsageDescription(viewFocusNid_);
							show = true;
						}
						catch (Exception e)
						{
							//noop - this concept simply isn't configured as a dynamic sememe concept.
						}
					}
					return show;
				}
			};
			viewUsageButton_.visibleProperty().bind(showViewUsageButton_);
			t.getItems().add(viewUsageButton_);
			
			t.getItems().add(summary_);
			
			//fill to right
			Region r = new Region();
			HBox.setHgrow(r, Priority.ALWAYS);
			t.getItems().add(r);
			
			stampButton_ = new ToggleButton("");
			stampButton_.setGraphic(Images.STAMP.createImageView());
			stampButton_.setTooltip(new Tooltip("Show / Hide Stamp Attributes"));
			stampButton_.setVisible(false);
			stampButton_.setSelected(true);
			t.getItems().add(stampButton_);
			
			showStampColumns_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean visible = false;
					if (listeningTo.size() > 0)
					{
						visible = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					if (stampColumn_ != null)
					{
						stampColumn_.setVisible(visible);
						for (TreeTableColumn<SemanticGUI, ?> nested : stampColumn_.getColumns())
						{
							nested.setVisible(visible);
						}
					}
					return visible;
				}
			};
			
			activeOnlyButton_ = new ToggleButton("");
			activeOnlyButton_.setGraphic(Images.FILTER_16.createImageView());
			activeOnlyButton_.setTooltip(new Tooltip("Show Active Only / Show All"));
			activeOnlyButton_.setVisible(false);
			activeOnlyButton_.setSelected(true);
			t.getItems().add(activeOnlyButton_);
			
			showActiveOnly_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean showActive = false;
					if (listeningTo.size() > 0)
					{
						showActive = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					refresh();
					return showActive;
				}
			};
			
			historyButton_ = new ToggleButton("");
			historyButton_.setGraphic(Images.HISTORY.createImageView());
			historyButton_.setTooltip(new Tooltip("Show Current Only / Show Full History"));
			historyButton_.setVisible(false);
			t.getItems().add(historyButton_);
			
			showFullHistory_ = new UpdateableBooleanBinding()
			{
				{
					setComputeOnInvalidate(true);
				}
				@Override
				protected boolean computeValue()
				{
					boolean showFullHistory = false;
					if (listeningTo.size() > 0)
					{
						showFullHistory = ((ReadOnlyBooleanProperty)listeningTo.iterator().next()).get();
					}
					refresh();
					return showFullHistory;
				}
			};
			
			displayFSNButton_ = new Button("");
			ImageView displayFsn = Images.DISPLAY_FSN.createImageView();
			Tooltip.install(displayFsn, new Tooltip("Displaying the Fully Specified Name - click to display the Preferred Term"));
			displayFsn.visibleProperty().bind(displayFSN_);
			ImageView displayPreferred = Images.DISPLAY_PREFERRED.createImageView();
			displayPreferred.visibleProperty().bind(displayFSN_.not());
			Tooltip.install(displayPreferred, new Tooltip("Displaying the Preferred Term - click to display the Fully Specified Name"));
			displayFSNButton_.setGraphic(new StackPane(displayFsn, displayPreferred));
			displayFSNButton_.prefHeightProperty().bind(historyButton_.heightProperty());
			displayFSNButton_.prefWidthProperty().bind(historyButton_.widthProperty());
			displayFSNButton_.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					//TODO Dan displayFSN fixes are not yet complete
					displayFSN_.set(displayFSN_.not().get());
				}
			});
			t.getItems().add(displayFSNButton_);
			
			cancelButton_ = new Button("Cancel");
			cancelButton_.setDisable(true);
			//TODO figure out to handle cancel
			//TODO implement edit features?
			//cancelButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(cancelButton_);
			cancelButton_.setOnAction((action) ->
			{
				try
				{
//					Get.commitService().cancel();
//					forgetAllUncommitted(treeRoot_.getChildren());
//					HashSet<Integer> assemblageNids = getAllAssemblageNids(treeRoot_.getChildren());
//					for (Integer i : assemblageNids)
//					{
//						ConceptVersionBI cv = ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinate(), i);
//						if (!cv.isAnnotationStyleRefex() && cv.isUncommitted())
//						{
//							cv.cancel();
//						}
//					}
				}
				catch (Exception e)
				{
					logger_.error("Error cancelling", e);
					Get.service(DialogService.class).showErrorDialog("Error", "There was an unexpected during cancel", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
				refresh();
			});
			
			commitButton_ = new Button("Commit");
			commitButton_.setDisable(true);
			//TODO implement edit features?
			//commitButton_.disableProperty().bind(hasUncommitted_.not());
			t.getItems().add(commitButton_);
			
			commitButton_.setOnAction((action) ->
			{
				try
				{
					//TODO ochre commit issues
//					HashSet<Integer> componentNids = getAllComponentNids(treeRoot_.getChildren());
//					for (Integer i : componentNids)
//					{
//						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConceptForNid(i);
//						if (cc.isUncommitted() || cc.getConceptAttributes().isUncommitted())
//						{
//							ExtendedAppContext.getDataStore().commit(/* cc */);
//						}
//					}
//					
//					HashSet<Integer> assemblageNids = getAllAssemblageNids(treeRoot_.getChildren());
//					for (Integer i : assemblageNids)
//					{
//						ConceptChronicleBI cc = ExtendedAppContext.getDataStore().getConcept(i);
//						if (!cc.isAnnotationStyleRefex() && cc.isUncommitted())
//						{
//							ExtendedAppContext.getDataStore().commit();
//						}
//					}
//					Get.commitService().commit("commit of dynamic sememe").get();
				}
				catch (Exception e)
				{
					logger_.error("Error committing", e);
					Get.service(DialogService.class).showErrorDialog("Error", "There was an unexpected during commit", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
				refresh();
			});
			
			rootNode_.getChildren().add(t);
			
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					//TODO hack for bug in javafx - need to start as true, then later toggle to false.
					stampButton_.setSelected(false);
				}
			});
			
//			refreshRequiredListenerHack = new UpdateableBooleanBinding()
//			{
//				{
//					setComputeOnInvalidate(true);
//					addBinding(ExtendedAppContext.getUserProfileBindings().getViewCoordinatePath(), ExtendedAppContext.getUserProfileBindings().getLanguageCoordinate());
//				}
//
//				@Override
//				protected boolean computeValue()
//				{
//					logger_.debug("DynRefex refresh() due to change of an observed user property");
//					refresh();
//					return false;
//				}
//			};
			noRefresh_.decrementAndGet();
		}
	}

	public Region getView()
	{
		//setting up the binding stuff is causing refresh calls
		initialInit();
		return rootNode_;
	}
	
	public void showView(Window parent)
	{
		Stage stage = new Stage(StageStyle.DECORATED);
		stage.initModality(Modality.NONE);
		stage.initOwner(parent);
		
		BorderPane root = new BorderPane();
		
		Label title = new Label("Sememe View");
		title.getStyleClass().add("titleLabel");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		title.setPadding(new Insets(5, 5, 5, 5));
		root.setTop(title);
		root.setCenter(getView());
		
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setTitle("Sememe View");
		stage.getScene().getStylesheets().add(SemanticViewer.class.getResource("/css/isaac-shared-styles.css").toString());
		stage.setWidth(800);
		stage.setHeight(600);
		stage.onHiddenProperty().set((eventHandler) ->
		{
			stage.setScene(null);
			viewDiscarded();
		});
		stage.show();
	}

	public void setComponent(int componentNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_.getAndIncrement();
		initialInit();
		viewFocus_ = ViewFocus.REFERENCED_COMPONENT;
		viewFocusNid_ = componentNid;
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory, displayFSNButton);
		showViewUsageButton_.invalidate();
		noRefresh_.getAndDecrement();
		initColumnsLoadData();
	}

	public void setAssemblage(int assemblageConceptNid, ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		//disable refresh, as the bindings mucking causes many refresh calls
		noRefresh_.getAndIncrement();
		initialInit();
		viewFocus_ = ViewFocus.ASSEMBLAGE;
		viewFocusNid_ = assemblageConceptNid;
		handleExternalBindings(showStampColumns, showActiveOnly, showFullHistory, displayFSNButton);
		noRefresh_.getAndDecrement();
		initColumnsLoadData();
	}
	
	private void handleExternalBindings(ReadOnlyBooleanProperty showStampColumns, ReadOnlyBooleanProperty showActiveOnly, 
			ReadOnlyBooleanProperty showFullHistory, boolean displayFSNButton)
	{
		showStampColumns_.clearBindings();
		showActiveOnly_.clearBindings();
		showFullHistory_.clearBindings();
		if (showStampColumns == null)
		{
			//Use our own button
			stampButton_.setVisible(true);
			showStampColumns_.addBinding(stampButton_.selectedProperty());
		}
		else
		{
			stampButton_.setVisible(false);
			showStampColumns_.addBinding(showStampColumns);
		}
		if (showActiveOnly == null)
		{
			//Use our own button
			activeOnlyButton_.setVisible(true);
			showActiveOnly_.addBinding(activeOnlyButton_.selectedProperty());
		}
		else
		{
			activeOnlyButton_.setVisible(false);
			showActiveOnly_.addBinding(showActiveOnly);
		}
		if (showFullHistory == null)
		{
			//Use our own button
			historyButton_.setVisible(true);
			showFullHistory_.addBinding(historyButton_.selectedProperty());
		}
		else
		{
			historyButton_.setVisible(false);
			showFullHistory_.addBinding(showFullHistory);
		}
		
		if (displayFSNButton)
		{
			//Use our own button
			displayFSNButton_.setVisible(true);
		}
		else
		{
			displayFSNButton_.setVisible(false);
		}
	}
	
	protected void refresh()
	{
		if (noRefresh_.get() > 0)
		{
			logger_.info("Skip refresh of dynamic sememe due to wait count {}", noRefresh_.get());
			return;
		}
		else
		{
			noRefresh_.getAndIncrement();
		}
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			try
			{
				loadRealData(); // calls addFilterCacheListeners()
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error building the sememe display", e);
				//null check, as the error may happen before the scene is visible
				Get.service(DialogService.class).showErrorDialog("Error", "There was an unexpected error building the sememe display", e.getMessage(), 
						(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
			}
			finally
			{
				noRefresh_.decrementAndGet();
			}
		});
	}
	
	private void storeTooltip(Map<String, List<String>> store, String key, String value)
	{
		List<String> list = store.get(key);
		if (list == null)
		{
			list = new ArrayList<>();
			store.put(key, list);
		}
		list.add(value);
	}

	private void initColumnsLoadData()
	{
		noRefresh_.getAndIncrement();
		Get.workExecutors().getExecutor().execute(() -> {
			try
			{
				removeFilterCacheListeners();
				
				final ArrayList<TreeTableColumn<SemanticGUI, ?>> treeColumns = new ArrayList<>();
				Map<String, List<String>> toolTipStore = new HashMap<>();
				
				TreeTableColumn<SemanticGUI, SemanticGUI> ttStatusCol = new TreeTableColumn<>(SemanticGUIColumnType.STATUS_CONDENSED.toString());
				storeTooltip(toolTipStore, ttStatusCol.getText(), "Status Markers - for active / inactive and current / historical and uncommitted");

				ttStatusCol.setSortable(true);
				ttStatusCol.setResizable(true);
				ttStatusCol.setComparator(new Comparator<SemanticGUI>()
				{
					@Override
					public int compare(SemanticGUI o1, SemanticGUI o2)
					{
						return o1.compareTo(SemanticGUIColumnType.STATUS_CONDENSED, null, o2);
					}
				});
				ttStatusCol.setCellFactory((colInfo) -> 
				{
					return new StatusCell();
				});
				ttStatusCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<SemanticGUI>(callback.getValue().getValue());
				});
				treeColumns.add(ttStatusCol);
				
				//UUID column
				TreeTableColumn<SemanticGUI, SemanticGUI> uuidCol = new TreeTableColumn<>();
				uuidCol.setText(SemanticGUIColumnType.UUID.toString());
				//TODO this doesn't work, because the column is disabled by default
				storeTooltip(toolTipStore, uuidCol.getText(), "The status of the sememe instance");
				uuidCol.setSortable(true);
				uuidCol.setResizable(true);
				uuidCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyObjectWrapper<>(callback.getValue().getValue());
				});
				uuidCol.setCellFactory((colInfo) -> 
				{
					return new StringCell(data ->  {return data.getSememe().getPrimordialUuid().toString();});
				});
				uuidCol.setVisible(false);
				treeColumns.add(uuidCol);
				
				//Create columns for basic info
				if (viewFocus_ == ViewFocus.ASSEMBLAGE)
				{
					//the assemblage is always the same - don't show.
					TreeTableColumn<SemanticGUI, SemanticGUI>  ttCol = buildComponentCellColumn(SemanticGUIColumnType.COMPONENT);
					storeTooltip(toolTipStore, ttCol.getText(), "The Referenced component of this Sememe");
					HeaderNode<String> headerNode = new HeaderNode<String>(
							filterCache_,
							ttCol,
							ColumnId.getInstance(SemanticGUIColumnType.COMPONENT),
							rootNode_.getScene(),
							new HeaderNode.DataProvider<String>() {
						@Override
						public String getData(SemanticGUI source) {
							return source.getDisplayStrings(SemanticGUIColumnType.COMPONENT, null).getKey();
						}
					});
					ttCol.setGraphic(headerNode.getNode());
					
					treeColumns.add(ttCol);
				}
				else
				{
					//the component is always the same - don't show.
					TreeTableColumn<SemanticGUI, SemanticGUI>  ttCol = buildComponentCellColumn(SemanticGUIColumnType.ASSEMBLAGE);
					storeTooltip(toolTipStore, ttCol.getText(), "The Assemblage concept that defines this Sememe");
					HeaderNode<String> headerNode = new HeaderNode<>(
							filterCache_,
							ttCol,
							ColumnId.getInstance(SemanticGUIColumnType.ASSEMBLAGE),
							rootNode_.getScene(),
							new HeaderNode.DataProvider<String>() {
						@Override
						public String getData(SemanticGUI source) {
							return source.getDisplayStrings(SemanticGUIColumnType.ASSEMBLAGE, null).getKey();
						}
					});
					ttCol.setGraphic(headerNode.getNode());

					treeColumns.add(ttCol);
				}
				
				TreeTableColumn<SemanticGUI, String> ttStringCol = new TreeTableColumn<>();
				ttStringCol = new TreeTableColumn<>();
				ttStringCol.setText(SemanticGUIColumnType.ATTACHED_DATA.toString());
				storeTooltip(toolTipStore, ttStringCol.getText(), "The various data fields attached to this Sememe instance");
				ttStringCol.setSortable(false);
				ttStringCol.setResizable(true);
				//don't add yet - we might not need this column.  throw away later, if we don't need it
				
				/**
				 * The key of the first hashtable is the column description concept, while the key of the second hashtable is the assemblage concept
				 * Since the same column could be used in multiple assemblages - need to keep those separate, even though the rest of the column details 
				 * will be the same.  The List in the third level is for cases where a single assemblage concept re-uses the same column description 
				 * details multiple times.
				*/
				Hashtable<UUID, Hashtable<UUID, List<DynamicColumnInfo>>> uniqueColumns;
				
				if (Get.identifiedObjectService().getChronology(viewFocusNid_).get().isUncommitted())
				{
					hasUncommitted_.set(true);
				}
				else
				{
					hasUncommitted_.set(false);
				}
				
				if (viewFocus_ == ViewFocus.REFERENCED_COMPONENT)
				{
					uniqueColumns = getUniqueColumns(viewFocusNid_);
				}
				else
				{
					//This case is easy - as there is only one assemblage.  The 3 level mapping stuff is way overkill for this case... but don't
					//want to rework it at this point... might come back and cleanup this mess later.
					uniqueColumns = new Hashtable<>();
					
					DynamicUsageDescription rdud;
					//Normally, we can read the info necessary from the assemblage - but in the case where we are displaying a non-dynamic sememe
					//I need to read an instance of the sememe, to find out what type it is (and then assume, that it is only used as that type)
					//yes, dangerous, bad code... the static sememes need work....
					
					try
					{
						rdud = LookupService.getService(DynamicUtility.class).readDynamicUsageDescription(viewFocusNid_);
					}
					catch (Exception e)
					{
						//Its either a mis-configured dynamic sememe, or its a static sememe.  Check and see...
						Optional<SemanticChronology> sc = Get.assemblageService().getSemanticChronologyStream(viewFocusNid_).findAny();
						if (sc.isPresent())
						{
							rdud = DynamicUsageDescriptionImpl.mockOrRead(sc.get());
						}
						else
						{
							//TODO need to figure out how to handle the case where the thing they click on isn't used as an assemblage, and isn't a dynamic assemblage
							throw new RuntimeException("Keyword", e);  //HACK alert (look in the catch)
						}
					}
					for (DynamicColumnInfo col : rdud.getColumnInfo())
					{
						Hashtable<UUID, List<DynamicColumnInfo>> nested = uniqueColumns.get(col.getColumnDescriptionConcept());
						if (nested == null)
						{
							nested = new Hashtable<>();
							uniqueColumns.put(col.getColumnDescriptionConcept(), nested);
						}
						
						UUID assemblyUUID = Get.identifierService().getUuidPrimordialForNid(rdud.getDynamicUsageDescriptorNid());
						List<DynamicColumnInfo> doubleNested = nested.get(assemblyUUID);
						if (doubleNested == null)
						{
							doubleNested = new ArrayList<>();
							nested.put(assemblyUUID, doubleNested);
						}
						doubleNested.add(col);
					}
				}
				
				ArrayList<Hashtable<UUID, List<DynamicColumnInfo>>> sortedUniqueColumns = new ArrayList<>();
				sortedUniqueColumns.addAll(uniqueColumns.values());
				Collections.sort(sortedUniqueColumns, new Comparator<Hashtable<UUID, List<DynamicColumnInfo>>>()
					{
						@Override
						public int compare(Hashtable<UUID, List<DynamicColumnInfo>> o1, Hashtable<UUID, List<DynamicColumnInfo>> o2)
						{
							return Integer.compare(o1.values().iterator().next().get(0).getColumnOrder(),o2.values().iterator().next().get(0).getColumnOrder()); 
						}
					});
				
				//Create columns for every different type of data column we see in use
				for (Hashtable<UUID, List<DynamicColumnInfo>> col : sortedUniqueColumns)
				{
					int max = 0;
					for (List<DynamicColumnInfo> item : col.values())
					{
						if (item.size() > max)
						{
							max = item.size();
						}
					}
					
					for (int i = 0; i < max; i++)
					{
						final String name = col.values().iterator().next().get(0).getColumnName(); //all the same, just pick the first
						TreeTableColumn<SemanticGUI, SemanticGUI> nestedCol = new TreeTableColumn<>(name);
						storeTooltip(toolTipStore, nestedCol.getText(), col.values().iterator().next().get(0).getColumnDescription());
						
						// FILTER ID
						final ColumnId columnKey = ColumnId.getInstance(col.values().iterator().next().get(0).getColumnDescriptionConcept(), i);

						final Integer listItem = i;
						HeaderNode<String> ttNestedColHeaderNode = new HeaderNode<>(
								filterCache_,
								nestedCol,
								columnKey,
								rootNode_.getScene(),
								new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(SemanticGUI source) {
								if (source == null) {
									return null;
								}
								try
								{
									for (UUID uuid : col.keySet())
									{
										assert source != null;
										assert source.getSememe() != null;
										
										if (Get.identifierService().getNidForUuids(uuid) == source.getSememe().getAssemblageNid())
										{
											List<DynamicColumnInfo> colInfo =  col.get(uuid);
											Integer refexColumnOrder = (colInfo.size() > listItem ? 
													(SemanticGUI.getData(source.getSememe()).length <= colInfo.get(listItem).getColumnOrder() ? null 
														: colInfo.get(listItem).getColumnOrder()): null);
											
											if (refexColumnOrder != null)
											{
												return source.getDisplayStrings(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder).getKey();
											}
										}
									}
								}
								catch (Exception e)
								{
									logger_.error("Unexpected error getting string data from attribute", e);
								}
								return null;  //not applicable / blank row
							}
						});
						nestedCol.setGraphic(ttNestedColHeaderNode.getNode());
						
						nestedCol.setSortable(true);
						nestedCol.setResizable(true);
						nestedCol.setComparator(new Comparator<SemanticGUI>()
						{
							@Override
							public int compare(SemanticGUI o1, SemanticGUI o2)
							{
								try
								{
									for (UUID uuid : col.keySet())
									{
										assert o1 != null;
										assert o1.getSememe() != null;
										
										if (Get.identifierService().getNidForUuids(uuid) == o1.getSememe().getAssemblageNid())
										{
											List<DynamicColumnInfo> colInfo =  col.get(uuid);
											Integer refexColumnOrder = (colInfo.size() > listItem ? 
													(SemanticGUI.getData(o1.getSememe()).length <= colInfo.get(listItem).getColumnOrder() ? null 
														: colInfo.get(listItem).getColumnOrder()): null);
											
											if (refexColumnOrder != null)
											{
												return o1.compareTo(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder, o2);
											}
										}
									}
								}
								catch (Exception e)
								{
									logger_.error("Unexpected error sorting data attributes", e);
								}
								return 1;  //not applicable / blank row
								
							}
						});
						
						nestedCol.setCellFactory(new AttachedDataCellFactory(col, i));
						
						nestedCol.setCellValueFactory((callback) ->
						{
							return new ReadOnlyObjectWrapper<>(callback.getValue().getValue());
						}); 
						
						ttStringCol.getColumns().add(nestedCol);
					}
				}
				
				//Only add attached data column if necessary
				if (ttStringCol.getColumns().size() > 0)
				{
					treeColumns.add(ttStringCol);
				}
				
				//Create the STAMP columns
				ttStringCol = new TreeTableColumn<>();
				ttStringCol.setText("STAMP");
				storeTooltip(toolTipStore, "STAMP", "The Status, Time, Author, Module and Path columns");
				ttStringCol.setSortable(false);
				ttStringCol.setResizable(true);
				stampColumn_ = ttStringCol;
				treeColumns.add(ttStringCol);
				
				TreeTableColumn<SemanticGUI, String> nestedCol = new TreeTableColumn<>();
				nestedCol.setText(SemanticGUIColumnType.STATUS_STRING.toString());
				storeTooltip(toolTipStore, nestedCol.getText(), "The status of the instance");
				HeaderNode<String> nestedColHeaderNode = new HeaderNode<>(
						filterCache_,
						nestedCol,
						ColumnId.getInstance(SemanticGUIColumnType.STATUS_STRING),
						rootNode_.getScene(),
						new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(SemanticGUI source) {
								return source.getDisplayStrings(SemanticGUIColumnType.STATUS_STRING, null).getKey();
							}
						});
				nestedCol.setGraphic(nestedColHeaderNode.getNode());
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getDisplayStrings(SemanticGUIColumnType.STATUS_STRING, null).getKey());
				});
				ttStringCol.getColumns().add(nestedCol);
				

				nestedCol = new TreeTableColumn<>();
				nestedCol.setText(SemanticGUIColumnType.TIME.toString());
				storeTooltip(toolTipStore, nestedCol.getText(), "The time when the instance was created or edited");
				nestedColHeaderNode = new HeaderNode<>(
						filterCache_,
						nestedCol,
						ColumnId.getInstance(SemanticGUIColumnType.TIME),
						rootNode_.getScene(),
						new HeaderNode.DataProvider<String>() {
							@Override
							public String getData(SemanticGUI source) {
								return source.getDisplayStrings(SemanticGUIColumnType.TIME, null).getKey();
							}
						});
				nestedCol.setGraphic(nestedColHeaderNode.getNode());
				nestedCol.setSortable(true);
				nestedCol.setResizable(true);
				nestedCol.setCellValueFactory((callback) ->
				{
					return new ReadOnlyStringWrapper(callback.getValue().getValue().getDisplayStrings(SemanticGUIColumnType.TIME, null).getKey());
				});
				ttStringCol.getColumns().add(nestedCol);
				
				TreeTableColumn<SemanticGUI, SemanticGUI> nestedIntCol = buildComponentCellColumn(SemanticGUIColumnType.AUTHOR); 
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The author of the instance");
				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = buildComponentCellColumn(SemanticGUIColumnType.MODULE);
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The module of the instance");
				ttStringCol.getColumns().add(nestedIntCol);
				
				nestedIntCol = buildComponentCellColumn(SemanticGUIColumnType.PATH); 
				storeTooltip(toolTipStore, nestedIntCol.getText(), "The path of the instance");
				ttStringCol.getColumns().add(nestedIntCol);

				Platform.runLater(() ->
				{
					ttv_.getColumns().clear();
					for (TreeTableColumn<SemanticGUI, ?> tc : treeColumns)
					{
						ttv_.getColumns().add(tc);
					}
					
					//Horrible hack to set a reasonable default size on the columns.
					//Min width to the width of the header column.
					Font f = new Font("System Bold", 13.0);
					for (final TreeTableColumn<SemanticGUI, ?> col : ttv_.getColumns())
					{
						for (TreeTableColumn<SemanticGUI, ?> nCol : col.getColumns())
						{
							String text = (nCol.getGraphic() != null 
									&& (nCol.getGraphic() instanceof Label || nCol.getGraphic() instanceof HBox) 
										? (nCol.getGraphic() instanceof Label ? ((Label)nCol.getGraphic()).getText() 
												: ((Label)((HBox)nCol.getGraphic()).getChildren().get(0)).getText()) 
										: nCol.getText());
							nCol.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 70);
						}
						
						if (col.getColumns().size() > 0)
						{
							FloatBinding binding = new FloatBinding()
							{
								{
									for (TreeTableColumn<SemanticGUI, ?> nCol : col.getColumns())
									{
										if (nCol.getText().equals("String"))
										{
											nCol.setPrefWidth(250);  //these are common, and commonly long
										}
										bind(nCol.widthProperty());
										bind(nCol.visibleProperty());
									}
								}
								@Override
								protected float computeValue()
								{
									float temp = 0;
									for (TreeTableColumn<SemanticGUI, ?> nCol : col.getColumns())
									{
										if (nCol.isVisible())
										{
											temp += nCol.getWidth();
										}
									}
									float parentColWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(col.getText(), f) + 70;
									if (temp < parentColWidth)
									{
										//bump the size of the first nested column, so the parent doesn't get clipped
										col.getColumns().get(0).setMinWidth(parentColWidth);
									}
									return temp;
								}
							};
							col.minWidthProperty().bind(binding);
						}
						else
						{
							String text = col.getText();
							
							if (text == null) {
								text = (col.getGraphic() != null && (col.getGraphic() instanceof Label || col.getGraphic() instanceof HBox)
										? (col.getGraphic() instanceof Label ? ((Label)col.getGraphic()).getText() : ((Label)((HBox)col.getGraphic()).getChildren().get(0)).getText()) 
										: col.getText());
							}
							
							if (text.equalsIgnoreCase(SemanticGUIColumnType.ASSEMBLAGE.toString()) 
									|| text.equalsIgnoreCase(SemanticGUIColumnType.COMPONENT.toString())
									|| text.equalsIgnoreCase(SemanticGUIColumnType.UUID.toString()))
							{
								col.setPrefWidth(250);
							}
							if (text.equalsIgnoreCase("s"))
							{
								col.setPrefWidth(32);
								col.setMinWidth(32);
							}
							else
							{
								col.setMinWidth(Toolkit.getToolkit().getFontLoader().computeStringWidth(text, f) + 70);
							}
						}
					}
					
					showStampColumns_.invalidate();
				});
				
				TableHeaderRowTooltipInstaller.installTooltips(ttv_, toolTipStore);

				loadRealData();
			}
			catch (Exception e)
			{
				if (e.getMessage().equals("Keyword"))
				{
					logger_.info("The specified concept is not specified correctly as a dynamic sememe, and is not utilized as a static sememe", e);
					Platform.runLater(() ->
					{
						addButton_.setDisable(true);
						treeRoot_.getChildren().clear();
						summary_.setText(0 + " entries");
						placeholderText.setText("The specified concept is not specified correctly as a dynamic sememe, and is not utilized as a static sememe");
						ttv_.setPlaceholder(placeholderText);
						ttv_.getSelectionModel().clearSelection();
					});
				}
				else
				{
					logger_.error("Unexpected error building the sememe display", e);
					//null check, as the error may happen before the scene is visible
					Get.service(DialogService.class).showErrorDialog("Error", "There was an unexpected error building the sememe display", e.getMessage(), 
							(rootNode_.getScene() == null ? null : rootNode_.getScene().getWindow()));
				}
			}
			finally
			{
				noRefresh_.getAndDecrement();
			}
		});
	}
	
	private synchronized void loadRealData() throws IOException, NumberFormatException, InterruptedException, ParseException
	{
		Platform.runLater(() ->
		{
			ttv_.setPlaceholder(progressBar_);
			treeRoot_.getChildren().clear();
		});
		// If UserProfileBindings.getDisplayFSN() has changed since last data load
		// then clear all filters, because they may contain outdated display text values
		boolean currentDisplayFSNPreferenceValue = displayFSN_.get();
		if (currentDisplayFSNPreferenceValue != filterCacheLastBuildDisplayFSNValue_) {
			logger_.debug("Clearing header node filter cache during refresh because displayFSN preference value changed to {}", currentDisplayFSNPreferenceValue);
			filterCacheLastBuildDisplayFSNValue_ = currentDisplayFSNPreferenceValue;
			removeFilterCacheListeners();
			for (HeaderNode.Filter<?> filter : filterCache_.values()) {
				filter.getFilterValues().clear();
			}
		}

		//Now add the data
		ArrayList<TreeItem<SemanticGUI>> rowData = getDataRows(viewFocusNid_);
		
		logger_.info("Found {} rows of data in a dynamic sememe", rowData.size());
		
		Get.workExecutors().getExecutor().execute(() ->
		{
			checkForUncommittedRefexes(rowData);
		});
		
		Platform.runLater(() ->
		{
			addButton_.setDisable(false);
			treeRoot_.getChildren().addAll(rowData);
			summary_.setText(rowData.size() + " entries");
			ttv_.setPlaceholder(placeholderText);
			ttv_.getSelectionModel().clearSelection();
		});
		
		// ADD LISTENERS TO headerNode.getUserFilters() TO EXECUTE REFRESH WHENEVER FILTER SETS CHANGE
		addFilterCacheListeners();
	}
	
	private TreeTableColumn<SemanticGUI, SemanticGUI> buildComponentCellColumn(SemanticGUIColumnType type)
	{
		TreeTableColumn<SemanticGUI, SemanticGUI> ttCol = new TreeTableColumn<>(type.toString());
		HeaderNode<String> headerNode = new HeaderNode<>(
				filterCache_,
				ttCol,
				ColumnId.getInstance(type),
				rootNode_.getScene(),
				new HeaderNode.DataProvider<String>() {
					@Override
					public String getData(SemanticGUI source) {
						return source.getDisplayStrings(type, null).getKey();
					}
				});
		ttCol.setGraphic(headerNode.getNode());
		
		ttCol.setSortable(true);
		ttCol.setResizable(true);
		ttCol.setComparator(new Comparator<SemanticGUI>()
		{
			@Override
			public int compare(SemanticGUI o1, SemanticGUI o2)
			{
				return o1.compareTo(type, null, o2);
			}
		});
		ttCol.setCellFactory((colInfo) -> {return new ComponentDataCell(type);});
		ttCol.setCellValueFactory((callback) -> {return new ReadOnlyObjectWrapper<SemanticGUI>(callback.getValue().getValue());});
		return ttCol;
	}

	
	private ArrayList<TreeItem<SemanticGUI>> getDataRows(int componentNid, TreeItem<SemanticGUI> nestUnder) 
			throws IOException
	{
		ArrayList<TreeItem<SemanticGUI>> rowData = createFilteredRowData(Get.assemblageService().getSemanticChronologyStreamForComponent(componentNid));
		
		if (nestUnder != null)
		{
			nestUnder.getChildren().addAll(rowData);
			return null;
		}
		else
		{
			return rowData;
		}
	}
	
	private ArrayList<TreeItem<SemanticGUI>> createFilteredRowData(Stream<SemanticChronology> sememes) throws IOException
	{
		ArrayList<TreeItem<SemanticGUI>> rowData = new ArrayList<>();
		ArrayList<SemanticVersion> allVersions = new ArrayList<>();
		
		sememes.forEach(sememeC ->
		{
			for (Version ds :  sememeC.getVersionList())
			{
				allVersions.add((SemanticVersion)ds);
			}
		});
		
		//Sort the newest to the top.
		Collections.sort(allVersions, new Comparator<SemanticVersion>()
		{
			@Override
			public int compare(SemanticVersion o1, SemanticVersion o2)
			{
				if (o1.getPrimordialUuid().equals(o2.getPrimordialUuid()))
				{
					return ((Long)o2.getTime()).compareTo(o1.getTime());
				}
				else
				{
					return o1.getPrimordialUuid().compareTo(o2.getPrimordialUuid());
				}
			}
		});
		
		UUID lastSeenRefex = null;
		
		for (SemanticVersion r : allVersions)
		{
			if (!showFullHistory_.get() && r.getPrimordialUuid().equals(lastSeenRefex))
			{
				continue;
			}
			if (showActiveOnly_.get() == false || r.getStatus() == Status.ACTIVE)
			{
				SemanticGUI newRefexDynamicGUI = new SemanticGUI(r, !r.getPrimordialUuid().equals(lastSeenRefex));  //first one we see with a new UUID is current, others are historical
				
				// HeaderNode FILTERING DONE HERE
				boolean filterOut = false;
				for (HeaderNode.Filter<?> filter : filterCache_.values()) {
					if (filter.getFilterValues().size() > 0) {
						if (! filter.accept(newRefexDynamicGUI)) {
							filterOut = true;
							break;
						}
					}
				}
				
				if (! filterOut) {
					TreeItem<SemanticGUI> ti = new TreeItem<>();

					ti.setValue(newRefexDynamicGUI);
					//recurse
					getDataRows(r.getNid(), ti);  
					rowData.add(ti);
				}
			}
			lastSeenRefex = r.getPrimordialUuid();
		}

		return rowData;
	}

	/**
	 * The key of the first hashtable is the column description concept, while the key of the second hashtable is the assemblage concept
	 * Since the same column could be used in multiple assemblages - need to keep those separate, even though the rest of the column details 
	 * will be the same.  The List in the third level is for cases where a single assemblage concept re-uses the same column description 
	 * details multiple times.
	 */
	private Hashtable<UUID, Hashtable<UUID, List<DynamicColumnInfo>>> getUniqueColumns(int componentNid)
	{
		Hashtable<UUID, Hashtable<UUID, List<DynamicColumnInfo>>> columns = new Hashtable<>();
		
		Get.assemblageService().getSemanticChronologyStreamForComponent(componentNid).forEach(sememeC ->
		{
			boolean assemblageWasNull = false;
			for (DynamicColumnInfo column :  DynamicUsageDescriptionImpl.mockOrRead(sememeC).getColumnInfo())
			{
				Hashtable<UUID, List<DynamicColumnInfo>> inner = columns.get(column.getColumnDescriptionConcept());
				if (inner == null)
				{
					inner = new Hashtable<>();
					columns.put(column.getColumnDescriptionConcept(), inner);
				}
				List<DynamicColumnInfo> innerValues = inner.get(column.getAssemblageConcept());
				if (innerValues == null)
				{
					assemblageWasNull = true;
					innerValues = new ArrayList<>();
					inner.put(column.getAssemblageConcept(), innerValues);
				}
				if (assemblageWasNull)  //We only want to populate this on the first pass - the columns on an assemblage will never change from one pass to another.
				{
					innerValues.add(column);
				}
			}
			
			//recurse
			Hashtable<UUID, Hashtable<UUID, List<DynamicColumnInfo>>> nested = getUniqueColumns(sememeC.getNid());
			for (Entry<UUID, Hashtable<UUID, List<DynamicColumnInfo>>> nestedItem : nested.entrySet())
			{
				if (columns.get(nestedItem.getKey()) == null)
				{
					columns.put(nestedItem.getKey(), nestedItem.getValue());
				}
				else
				{
					Hashtable<UUID, List<DynamicColumnInfo>> mergeInto = columns.get(nestedItem.getKey());
					for (Entry<UUID, List<DynamicColumnInfo>> toMergeItem : nestedItem.getValue().entrySet())
					{
						if (mergeInto.get(toMergeItem.getKey()) == null)
						{
							mergeInto.put(toMergeItem.getKey(), toMergeItem.getValue());
						}
						else
						{
							//don't care - we already have this assemblage concept - the column values will be the same as what we already have.
						}
					}
				}
			}
		});
			
		return columns;
	}
	
	private void checkForUncommittedRefexes(List<TreeItem<SemanticGUI>> items)
	{
		if (hasUncommitted_.get())
		{
			return;
		}
		if (items == null)
		{
			return;
		}
		for (TreeItem<SemanticGUI> item : items)
		{
			if (item.getValue() != null && item.getValue().getSememe().isUncommitted())
			{
				//TODO add some indication that this is either running / finished
				Platform.runLater(() ->
				{
					hasUncommitted_.set(true);
				});
				return;
			}
			checkForUncommittedRefexes(item.getChildren());
		}
	}
	
	private HashSet<Integer> getAllAssemblageSequences(List<TreeItem<SemanticGUI>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<SemanticGUI> item : items)
		{
			if (item.getValue() != null)
			{
				SemanticVersion refex = item.getValue().getSememe();
				results.add(refex.getAssemblageNid());
			}
			results.addAll(getAllAssemblageSequences(item.getChildren()));
		}
		return results;
	}
	
	private void forgetAllUncommitted(List<TreeItem<SemanticGUI>> items) throws IOException
	{
		
		if (items == null)
		{
			return;
		}
//		for (TreeItem<RefexDynamicGUI> item : items)
//		{
			//TODO commit / cancel / forget?
//			if (item.getValue() != null)
//			{
//				DynamicSememeVersionBI<? extends DynamicSememeVersionBI<?>> refex = item.getValue().getRefex();
//				if (refex.isUncommitted())
//				{
//					ExtendedAppContext.getDataStore().forget(refex);
//					ConceptVersionBI cv = ExtendedAppContext.getDataStore().getConceptVersion(OTFUtility.getViewCoordinate(), refex.getReferencedComponentNid());
//					//if assemblageNid != concept nid - this means it is an annotation style refex
//                                        // TODO There are no more annotation refexes, they are all stored the same...
//                                        cv.cancel();
////					if ((refex.getAssemblageNid() != refex.getConceptNid()) && cv.isUncommitted())
////					{
////						cv.cancel();
////					}
//				}
//			}
//			forgetAllUncommitted(item.getChildren());
//		}
	}
	
	private HashSet<Integer> getAllComponentNids(List<TreeItem<SemanticGUI>> items)
	{
		HashSet<Integer> results = new HashSet<Integer>();
		if (items == null)
		{
			return results;
		}
		for (TreeItem<SemanticGUI> item : items)
		{
			if (item.getValue() != null)
			{
				SemanticVersion refex = item.getValue().getSememe();
				results.add(refex.getReferencedComponentNid());
			}
			results.addAll(getAllComponentNids(item.getChildren()));
		}
		return results;
	}
	

	private ArrayList<TreeItem<SemanticGUI>> getDataRows(int nid) 
			throws IOException, InterruptedException, NumberFormatException, ParseException
	{
		Platform.runLater(() ->
		{
			progressBar_.setProgress(-1);
			ttv_.setPlaceholder(progressBar_);
		});
		
		
		ArrayList<TreeItem<SemanticGUI>> rowData = createFilteredRowData(viewFocus_ == ViewFocus.ASSEMBLAGE ? 
				Get.assemblageService().getSemanticChronologyStream(nid) :
					Get.assemblageService().getSemanticChronologyStreamForComponent(nid));

		if (rowData.size() == 0)
		{
			placeholderText.setText("No Dynamic Sememes were found using this Assemblage");
		}
		return rowData;
	}
	
	public void viewDiscarded()
	{
		noRefresh_.incrementAndGet();
		//refreshRequiredListenerHack.clearBindings();
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getMenuText()
	{
		return "Semantic Tree Table View";
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Node getMenuIcon()
	{
		return Iconography.PAPERCLIP.getIconographic();
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public EnumSet<DetailType> getSupportedTypes()
	{
		return EnumSet.of(DetailType.Concept);
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DetailNode createDetailNode(Manifold manifold, Consumer<Node> nodeConsumer, DetailType type)
	{
		manifoldConcept_= manifold;

		if (manifold.getFocusedConcept() != null)
		{
			setComponent(manifold.getFocusedConcept().getNid(), null, null, null, true);
		}
		
		manifoldConcept_.focusedConceptProperty().addListener((change) ->
		{
			setComponent(manifold.getFocusedConcept().getNid(), null, null, null, true);
			titleProperty.set(manifoldConcept_.getPreferredDescriptionText(manifold.getFocusedConcept()));
			toolTipProperty.set("attached semantics for: " + this.manifoldConcept_.getFullySpecifiedDescriptionText(manifold.getFocusedConcept()));
			displayFSN_.set(manifoldConcept_.getLanguageCoordinate().isFQNPreferred());
		});
		
		nodeConsumer.accept(getView());
		
		return new DetailNode()
		{
			@Override
			public boolean selectInTabOnChange()
			{
				return true;
			}
			
			@Override
			public ReadOnlyProperty<String> getToolTip()
			{
				return toolTipProperty;
			}
			
			@Override
			public Optional<Node> getTitleNode()
			{
				if (titleLabel == null)
				{
					titleLabel = new ManifoldLinkedConceptLabel(manifold, ManifoldLinkedConceptLabel::setPreferredText, () -> new ArrayList<>());
					titleLabel.setGraphic(Iconography.CONCEPT_DETAILS.getIconographic());
					titleProperty.set("");
				}

				return Optional.of(titleLabel);
			}
			
			@Override
			public ReadOnlyProperty<String> getTitle()
			{
				return titleProperty;
			}
			
			@Override
			public Manifold getManifold()
			{
				return manifoldConcept_;
			}
		};
	}
}
