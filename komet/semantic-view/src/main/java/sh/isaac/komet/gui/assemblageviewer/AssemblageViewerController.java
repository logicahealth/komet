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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.component.concept.ConceptSnapshot;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUtility;
import sh.isaac.api.util.WorkExecutors;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.komet.gui.semanticViewer.SemanticViewer;
import sh.isaac.utility.Frills;
import sh.isaac.utility.SimpleDisplayConcept;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.ConceptNode;
import sh.komet.gui.util.FxGet;

/**
 * {@link AssemblageViewerController}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

public class AssemblageViewerController
{
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private ListView<SimpleDisplayConcept> semanticList;
	@FXML private Label referencedComponentTypeLabel;
	@FXML private Label referencedComponentSubTypeLabel;
	@FXML private AnchorPane rootPane;
	@FXML private Button clearFilterButton;
	@FXML private TextField descriptionMatchesFilter;
	@FXML private Button viewUsage;
	@FXML private Label statusLabel;
	@FXML private Label selectedSemanticDescriptionLabel;
	@FXML private ListView<DynamicColumnInfo> extensionFields;
	@FXML private ToolBar executeOperationsToolbar;
	@FXML private Label selectedSemanticNameLabel;
	@FXML private VBox conceptNodeFilterPlaceholder;
	@FXML private ProgressIndicator readingSemanticProgress;
	@FXML private ProgressIndicator selectedSemanticProgressIndicator;
	
	private Manifold manifold_;

	private enum PendingRead
	{
		IDLE, FILTER_UPDATE_PROGRESS, FULL_READ_IN_PROGRESS, DO_FILTER_READ, DO_FULL_READ
	};

	//TODO this needs some sort of hook to refresh the list when a new one is defined
	
	private ConceptNode conceptNode;
	private volatile boolean disableRead = true;
	private volatile PendingRead readStatusTracker = PendingRead.IDLE;
	private Object readStatusLock = new Object();
	private int currentlyRenderedRefexNid = 0;
	private ContextMenu semanticDefinitionsContextMenu_;

	private HashSet<SimpleDisplayConcept> allRefexDefinitions;

	private final Logger log = LogManager.getLogger(AssemblageViewerController.class);

	protected static AssemblageViewerController construct(Manifold manifold) throws IOException
	{
		// Load from FXML.
		URL resource = AssemblageViewerController.class.getResource("AssemblageViewer.fxml");
		FXMLLoader loader = new FXMLLoader(resource);
		loader.load();
		AssemblageViewerController controller = loader.getController();
		controller.manifold_ = manifold;
		return controller;
	}

	@FXML
	void initialize()
	{
		assert semanticList != null : "fx:id=\"semanticList\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert clearFilterButton != null : "fx:id=\"clearFilterButton\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert descriptionMatchesFilter != null : "fx:id=\"descriptionMatchesFilter\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert viewUsage != null : "fx:id=\"viewUsage\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert statusLabel != null : "fx:id=\"statusLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedSemanticDescriptionLabel != null : "fx:id=\"selectedSemanticDescriptionLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert extensionFields != null : "fx:id=\"extensionFields\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert executeOperationsToolbar != null : "fx:id=\"executeOperationsToolbar\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert selectedSemanticNameLabel != null : "fx:id=\"selectedSemanticNameLabel\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert conceptNodeFilterPlaceholder != null : "fx:id=\"conceptNodeFilterPlaceholder\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";
		assert readingSemanticProgress != null : "fx:id=\"readingSemanticProgress\" was not injected: check your FXML file 'DynamicRefexListView.fxml'.";

		descriptionMatchesFilter.textProperty().addListener((change) -> {
			rebuildList(false);
		});

		conceptNode = new ConceptNode(null, false, () -> manifold_, true);
		conceptNode.getConceptProperty().addListener((invalidation) -> {
			ConceptSnapshot cv = conceptNode.getConceptProperty().get();  //Need to do a get after each invalidation, otherwise, we won't get the next invalidation
			if (cv != null)
			{
				//see if it is a valid Dynamic Semantic Assemblage
				try
				{
					LookupService.getService(DynamicUtility.class).readDynamicUsageDescription(cv.getNid());
				}
				catch (Exception e)
				{
					conceptNode.isValid().setInvalid("The specified concept is not constructed as a Dynamic Semantic Assemblage concept");
				}
			}
			rebuildList(false);
		});

		conceptNodeFilterPlaceholder.getChildren().add(conceptNode.getNode());

		statusLabel.setText("Reading Semantics");

		clearFilterButton.setOnAction((event) -> {
			disableRead = true;
			descriptionMatchesFilter.setText("");
			conceptNode.set((ConceptSnapshot) null);
			disableRead = false;
			rebuildList(true);
		});

		semanticList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		semanticList.getSelectionModel().selectedItemProperty().addListener((change) -> {
			showSemanticDetails(semanticList.getSelectionModel().getSelectedItem());
		});
		
		semanticDefinitionsContextMenu_ = new ContextMenu();
		semanticDefinitionsContextMenu_.setAutoHide(true);
		
		MenuItem mi = new MenuItem("View Usage");
		mi.setOnAction((action) ->
		{
			SimpleDisplayConcept sdc = semanticList.getSelectionModel().getSelectedItem();
			if (sdc != null)
			{
				SemanticViewer driv = LookupService.get().getService(SemanticViewer.class);
				Manifold mf = manifold_.deepClone();
				mf.setFocusedConceptChronology(Get.concept(sdc.getNid()));
				driv.setAssemblage(sdc.getNid(), mf, null, null, null, true);
				driv.showView(null);
			}
		});
		mi.setGraphic(Images.SEARCH.createImageView());
		semanticDefinitionsContextMenu_.getItems().add(mi);

		//TODO indexing config see if it makes sense to port this view as part of fixing customization of indexing
//		mi = new MenuItem("Configure Semantic Indexing");
//		mi.setOnAction((action) ->
//		{
//			SimpleDisplayConcept sdc = semanticList.getSelectionModel().getSelectedItem();
//			if (sdc != null)
//			{
//				new ConfigureDynamicRefexIndexingView(sdc.getNid()).showView(null);
//			}
//		});
//		mi.setGraphic(Images.CONFIGURE.createImageView());
//		semanticDefinitionsContextMenu_.getItems().add(mi);
		
		//TODO common menu support?
//		CommonMenus.addCommonMenus(semanticDefinitionsContextMenu_, new CommonMenusNIdProvider()
//		{
//			@Override
//			public Collection<Integer> getNIds()
//			{
//				SimpleDisplayConcept sdc = semanticList.getSelectionModel().getSelectedItem();
//				return Arrays.asList(sdc == null ? new Integer[] {} : new Integer[] {sdc.getNid()});
//			}
//		});
		
		semanticList.addEventHandler(MouseEvent.MOUSE_CLICKED, (mouseEvent) -> 
		{
			if (mouseEvent.getButton().equals(MouseButton.SECONDARY) && semanticList.getSelectionModel().getSelectedItem() != null)
			{
				semanticDefinitionsContextMenu_.show(semanticList, mouseEvent.getScreenX(), mouseEvent.getScreenY());
			}
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY))
			{
				if (semanticDefinitionsContextMenu_.isShowing())
				{
					semanticDefinitionsContextMenu_.hide();
				}
			}
		});
		
		viewUsage.setDisable(true);
		viewUsage.setOnAction((event) -> {
			SemanticViewer driv = LookupService.get().getService(SemanticViewer.class);
			Manifold mf = manifold_.deepClone();
			mf.setFocusedConceptChronology(Get.concept(semanticList.getSelectionModel().getSelectedItem().getNid()));
			driv.setAssemblage(semanticList.getSelectionModel().getSelectedItem().getNid(), mf, null, null, null, true);
			driv.showView(null);
		});
		extensionFields.setCellFactory(new Callback<ListView<DynamicColumnInfo>, ListCell<DynamicColumnInfo>>()
		{
			@Override
			public ListCell<DynamicColumnInfo> call(ListView<DynamicColumnInfo> param)
			{
				return new DynamicSemanticDataColumnListCell();
			}
		});

		disableRead = false;
		rebuildList(true);
	}

	private void rebuildList(boolean fullRebuild)
	{
		if (disableRead)
		{
			log.debug("Skip rebuild");
			return;
		}

		synchronized (readStatusLock)
		{
			if (readStatusTracker != PendingRead.IDLE)
			{
				//already a read in progress.  Add this request to the list, return.
				if (fullRebuild && readStatusTracker != PendingRead.DO_FULL_READ)
				{
					readStatusTracker = PendingRead.DO_FULL_READ;
				}
				else if (readStatusTracker != PendingRead.DO_FULL_READ && readStatusTracker != PendingRead.DO_FILTER_READ)
				{
					readStatusTracker = PendingRead.DO_FILTER_READ;
				}
				log.debug("Queued rebuild " + readStatusTracker);
				return;
			}
			else
			{
				readStatusTracker = (fullRebuild ? PendingRead.FULL_READ_IN_PROGRESS : PendingRead.FILTER_UPDATE_PROGRESS);
			}
		}

		statusLabel.setText("Reading Semantics");
		readingSemanticProgress.setVisible(true);
		SimpleDisplayConcept selectedBefore = semanticList.getSelectionModel().getSelectedItem();
		semanticList.getSelectionModel().clearSelection();
		semanticList.getItems().clear();

		Task<Void> t = new Task<Void>()
		{
			ArrayList<SimpleDisplayConcept> filteredList;

			@Override
			protected Void call() throws Exception
			{
				log.debug("Rebuild request running: " + readStatusTracker);
				if (fullRebuild)
				{
					allRefexDefinitions = null;
				}

				if (allRefexDefinitions == null)
				{
					allRefexDefinitions = new HashSet<>();
					allRefexDefinitions.addAll(Frills.getAllDynamicSemanticAssemblageConcepts());
				}
				
				//This code for adding the concept from the concept filter panel can be removed, if we fix the above code to actually
				//find all dynamic semantics in the system.
				boolean conceptFromOutsideTheList = true;
				SimpleDisplayConcept enteredConcept = null;
				if (conceptNode.getConcept() != null && conceptNode.isValid().get())
				{
					enteredConcept = new SimpleDisplayConcept(conceptNode.getConcept());
				}

				filteredList = new ArrayList<>();
				for (SimpleDisplayConcept sdc : allRefexDefinitions)
				{
					if (enteredConcept != null && sdc.getNid() == enteredConcept.getNid())
					{
						conceptFromOutsideTheList = false;
					}
					if (passesFilters(sdc))
					{
						filteredList.add(sdc);
					}
				}
				
				if (enteredConcept != null && conceptFromOutsideTheList)
				{
					filteredList.add(enteredConcept);
				}
				
				Collections.sort(filteredList);
				
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				finished();
			}

			/**
			 * @see javafx.concurrent.Task#failed()
			 */
			@Override
			protected void failed()
			{
				log.error("Unexpected error building Semantic List", this.getException());
				FxGet.dialogs().showErrorDialog("Error reading Dynamic semantics", this.getException());
				finished();
			}

			private void finished()
			{
				log.debug("Semantic Definition refresh complete");
				semanticList.getItems().addAll(filteredList);
				if (selectedBefore != null && semanticList.getItems().contains(selectedBefore))
				{
					semanticList.getSelectionModel().select(selectedBefore);
				}
				showSemanticDetails(semanticList.getSelectionModel().getSelectedItem());
				statusLabel.setText("Showing " + filteredList.size() + " of " + allRefexDefinitions.size() + " Semantics");
				readingSemanticProgress.setVisible(false);
				synchronized (readStatusLock)
				{
					if (readStatusTracker == PendingRead.DO_FILTER_READ || readStatusTracker == PendingRead.DO_FULL_READ)
					{
						boolean rebuild = readStatusTracker == PendingRead.DO_FULL_READ ? true : false;
						readStatusTracker = PendingRead.IDLE;
						//Another request came in while we were running.  Run again.
						rebuildList(rebuild);
					}
					else
					{
						readStatusTracker = PendingRead.IDLE;
					}
				}
			}
		};

		LookupService.getService(WorkExecutors.class).getExecutor().execute(t);
	}

	private boolean passesFilters(SimpleDisplayConcept sdc) throws IOException
	{
		if (!conceptNode.isValid().get())
		{
			return false;
		}
		else if (conceptNode.getConcept() != null && conceptNode.getConcept().getNid() != sdc.getNid())
		{
			return false;
		}
		else if (descriptionMatchesFilter.getText().length() > 0)
		{
			if (!sdc.getDescription().toLowerCase().contains(descriptionMatchesFilter.getText().toLowerCase()))
			{
				return false;
			}
		}
		return true;
	}

	private void showSemanticDetails(SimpleDisplayConcept sdn)
	{
		if (sdn != null && sdn.getNid() == currentlyRenderedRefexNid)
		{
			return;
		}
		else
		{
			currentlyRenderedRefexNid = (sdn == null ? 0 : sdn.getNid());
		}
		selectedSemanticNameLabel.setText("");
		selectedSemanticDescriptionLabel.setText("");
		referencedComponentTypeLabel.setText("");
		referencedComponentSubTypeLabel.setText("");
		extensionFields.getItems().clear();
		
		if (sdn == null)
		{
			viewUsage.setDisable(true);
			return;
		}
		else
		{
			viewUsage.setDisable(false);
		}
		selectedSemanticProgressIndicator.setVisible(true);
		selectedSemanticNameLabel.setText(sdn.getDescription());

		Task<Void> t = new Task<Void>()
		{
			ArrayList<DynamicColumnInfo> tempColumnInfo = new ArrayList<>();
			
			@Override
			protected Void call() throws Exception
			{
				DynamicUsageDescription rdud = LookupService.getService(DynamicUtility.class).readDynamicUsageDescription(sdn.getNid());
				//fill in the header stuff
				Platform.runLater(() -> 
				{
					selectedSemanticNameLabel.setText(rdud.getDynamicName());
					selectedSemanticDescriptionLabel.setText(rdud.getDynamicUsageDescription());
					referencedComponentTypeLabel.setText(rdud.getReferencedComponentTypeRestriction() == null ? "No restriction" : 
						"Must be " + rdud.getReferencedComponentTypeRestriction().toString());
					referencedComponentSubTypeLabel.setText(rdud.getReferencedComponentTypeSubRestriction() == null ? "No restriction" : 
						"Must be " + rdud.getReferencedComponentTypeSubRestriction().toString());
				});
				
				//now fill in the data column details...
				
				for (DynamicColumnInfo rdci : rdud.getColumnInfo())
				{
					//force the read on the column info - this may have to be read from the DB.
					rdci.getColumnName();
					tempColumnInfo.add(rdci);
				}
				
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				extensionFields.getItems().addAll(tempColumnInfo);
				extensionFields.scrollTo(0);
				finished();
			}

			/**
			 * @see javafx.concurrent.Task#failed()
			 */
			@Override
			protected void failed()
			{
				log.error("Unexpected error building selected semantic", this.getException());
				FxGet.dialogs().showErrorDialog("Error reading Dynamic Semantic", this.getException());
				finished();
			}

			private void finished()
			{
				selectedSemanticProgressIndicator.setVisible(false);
			}
		};

		
		LookupService.getService(WorkExecutors.class).getExecutor().execute(t);
	}
	
	public Region getRoot()
	{
		return rootPane;
	}
}
