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
package sh.komet.gui.search.extended;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sh.isaac.MetaData;
import sh.isaac.api.Status;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.index.AuthorModulePathRestriction;
import sh.isaac.utility.Frills;
import sh.komet.gui.control.property.ViewProperties;
import tornadofx.control.DateTimePicker;

/**
 * JavaFX controller class for selecting STAMP info for extended search
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class StampSelectionController
{
	@FXML
	private ChoiceBox<String> status;

	@FXML
	private ListView<Integer> authors;

	@FXML
	private ListView<Integer> paths;

	@FXML
	private VBox modules;

	@FXML
	private DateTimePicker timeStart;

	@FXML
	private DateTimePicker timeEnd;

	@FXML
	private ComboBox<String> timeSelectStart;

	@FXML
	private ComboBox<String> timeSelectEnd;
	
	@FXML
	private GridPane gridPane;
	
	private ViewProperties readViewProperties;
	
	private TreeItem<Integer> rootModule;
	
	private final String ACTIVE = "Active Only";
	private final String ACTIVE_AND_INACTIVE = "Active and Inactive";
	private final String INACTIVE = "Inactive Only";

	@FXML
	void initialize()
	{
		assert status != null : "fx:id=\"status\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert authors != null : "fx:id=\"authors\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert paths != null : "fx:id=\"paths\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert modules != null : "fx:id=\"modules\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert timeStart != null : "fx:id=\"timeStart\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert timeEnd != null : "fx:id=\"timeEnd\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert timeSelectStart != null : "fx:id=\"timeSelectStart\" was not injected: check your FXML file 'StampSelection.fxml'.";
		assert timeSelectEnd != null : "fx:id=\"timeSelectEnd\" was not injected: check your FXML file 'StampSelection.fxml'.";
		
		status.getItems().add(ACTIVE);
		status.getItems().add(ACTIVE_AND_INACTIVE);
		status.getItems().add(INACTIVE);
		status.getSelectionModel().select(ACTIVE);
		
		timeSelectStart.getItems().add("Any");
		timeSelectStart.getItems().add("Newer Than");
		timeSelectStart.getSelectionModel().clearAndSelect(0);
		
		timeSelectStart.setOnAction((action) -> 
		{
			if (timeSelectStart.getSelectionModel().getSelectedIndex() == 0)
			{
				timeStart.setVisible(false);
				timeStart.setManaged(false);
			}
			else
			{
				timeStart.setVisible(true);
				timeStart.setManaged(true);
			}
		});
		
		timeStart = new DateTimePicker();
		timeStart.setVisible(false);
		timeStart.setManaged(false);
		timeStart.setMaxWidth(Double.MAX_VALUE);
		timeStart.setValue(LocalDate.of(2010, 1, 1));
		
		GridPane.setConstraints(timeStart, 1, 2);
		gridPane.getChildren().add(timeStart);
		
		
		timeSelectEnd.getItems().add("Any");
		timeSelectEnd.getItems().add("Older Than");
		timeSelectEnd.getSelectionModel().clearAndSelect(0);
		
		timeSelectEnd.setOnAction((action) ->
		{
			if (timeSelectEnd.getSelectionModel().getSelectedIndex() == 0)
			{
				timeEnd.setVisible(false);
				timeEnd.setManaged(false);
			}
			else
			{
				timeEnd.setVisible(true);
				timeEnd.setManaged(true);
			}
		});
		
		timeEnd = new DateTimePicker();
		timeEnd.setMaxWidth(Double.MAX_VALUE);
		timeEnd.setVisible(false);
		timeEnd.setManaged(false);
		timeEnd.setValue(LocalDate.now());
		GridPane.setConstraints(timeEnd, 1, 4);
		gridPane.getChildren().add(timeEnd);
	}
	
	protected void finishSetup(ViewProperties readManifoldCoordinate, AuthorModulePathRestriction amp, TimeStatusRestriction tsr)
	{
		this.readViewProperties = readManifoldCoordinate;
		
		authors.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>()
		{
			@Override
			public ListCell<Integer> call(ListView<Integer> param)
			{
				return new ListCell<Integer>()
				{
					@Override
					protected void updateItem(final Integer item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText("");
							setGraphic(null);
						}
						else
						{
							setText(item == Integer.MAX_VALUE ? " - Any Author - " : readManifoldCoordinate.getDescriptionText(item).orElse(""));
						}
					}
				};
			}
		});
		
		authors.getItems().addAll(Frills.getAllChildrenOfConcept(MetaData.USER____SOLOR.getNid(), true, true, readManifoldCoordinate.getStampFilter()));
		authors.getItems().add(MetaData.USER____SOLOR.getNid());
		Collections.sort(authors.getItems(), new Comparator<Integer>()
		{
			@Override
			public int compare(Integer o1, Integer o2)
			{
				return readManifoldCoordinate.getDescriptionText(o1).orElse("").compareTo(readManifoldCoordinate.getDescriptionText(o2).orElse(""));
			}
		});
		
		authors.getItems().add(0, Integer.MAX_VALUE);
		authors.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		authors.getSelectionModel().select(0);
		
		
		CheckBox temp = new CheckBox("- Any Module - ");
		rootModule = new TreeItem<Integer>(Integer.MAX_VALUE, temp);
		temp.setSelected(true);
		temp.setDisable(true);
		
		populateModules(rootModule, 0);
		
		paths.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>()
		{
			@Override
			public ListCell<Integer> call(ListView<Integer> param)
			{
				return new ListCell<Integer>()
				{
					@Override
					protected void updateItem(final Integer item, boolean empty)
					{
						super.updateItem(item, empty);
						if (item == null || empty)
						{
							setText("");
							setGraphic(null);
						}
						else
						{
							setText(item == Integer.MAX_VALUE ? " - Any Path - " : readManifoldCoordinate.getDescriptionText(item).orElse(""));
						}
					}
				};
			}
		});
		
		paths.getItems().addAll(Frills.getAllChildrenOfConcept(MetaData.PATH____SOLOR.getNid(), true, true, readManifoldCoordinate.getStampFilter()));
		Collections.sort(paths.getItems(), new Comparator<Integer>()
		{
			@Override
			public int compare(Integer o1, Integer o2)
			{
				return readManifoldCoordinate.getDescriptionText(o1).orElse("").compareTo(readManifoldCoordinate.getDescriptionText(o2).orElse(""));
			}
		});
		paths.getItems().add(0, Integer.MAX_VALUE);
		paths.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		paths.getSelectionModel().select(0);
		
		
		if (amp != null)
		{
			if (amp.getAuthors() != null)
			{
				NidSet set = amp.getAuthors();
				authors.getSelectionModel().clearSelection();
				for (int i = 0; i < authors.getItems().size(); i++)
				{
					if (set.contains(authors.getItems().get(i).intValue()))
					{
						authors.getSelectionModel().select(i);
					}
				}
			}
			if (amp.getPaths() != null)
			{
				NidSet set = amp.getPaths();
				paths.getSelectionModel().clearSelection();
				for (int i = 0; i < paths.getItems().size(); i++)
				{
					if (set.contains(paths.getItems().get(i).intValue()))
					{
						paths.getSelectionModel().select(i);
					}
				}
			}
			if (amp.getModules() != null)
			{
				checkModuleBoxes(amp.getModules(), rootModule);
			}
		}
		
		if (tsr != null)
		{
			if (tsr.getAllowedStates() == null || tsr.getAllowedStates().equals(Status.ANY_STATUS_SET))
			{
				status.getSelectionModel().select(ACTIVE_AND_INACTIVE);
			}
			else if (tsr.getAllowedStates().equals(Status.ACTIVE_ONLY_SET))
			{
				status.getSelectionModel().select(ACTIVE);
			}
			else
			{
				status.getSelectionModel().select(INACTIVE);
			}
			
			if (tsr.afterTime == null)
			{
				timeSelectStart.getSelectionModel().select(0);
			}
			else
			{
				timeSelectStart.getSelectionModel().select(1);
				timeStart.setDateTimeValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(tsr.afterTime), ZoneId.systemDefault()));
				timeStart.setVisible(true);
				timeStart.setManaged(true);
			}
			
			if (tsr.beforeTime == null)
			{
				timeSelectEnd.getSelectionModel().select(0);
			}
			else
			{
				timeSelectEnd.getSelectionModel().select(1);
				timeEnd.setDateTimeValue(LocalDateTime.ofInstant(Instant.ofEpochMilli(tsr.beforeTime), ZoneId.systemDefault()));
				timeEnd.setVisible(true);
				timeEnd.setManaged(true);
			}
		}
		
	}
	
	private void checkModuleBoxes(NidSet selectedModules, TreeItem<Integer> treeNode)
	{
		for (TreeItem<Integer> ti : treeNode.getChildren())
		{
			if (selectedModules.contains(ti.getValue()))
			{
				((CheckBox)ti.getGraphic()).setSelected(true);
			}
			checkModuleBoxes(selectedModules, ti);
		}
	}

	private void populateModules(TreeItem<Integer> treeItem, int depth)
	{
		modules.getChildren().add(treeItem.getGraphic());
		VBox.setMargin(treeItem.getGraphic(), new Insets(0, 0, 0, (10 * depth)));
		for (int nid : Frills.getAllChildrenOfConcept((treeItem.getValue() == Integer.MAX_VALUE ? MetaData.MODULE____SOLOR.getNid() : treeItem.getValue()), 
				false, false, readViewProperties.getStampFilter()))
		{
			TreeItem<Integer> child = new TreeItem<Integer>(nid, new CheckBox(readViewProperties.getDescriptionText(nid).orElse("")));
			((CheckBox)child.getGraphic()).selectedProperty().addListener((change, oldV, newV) -> {
				if (change.getValue().booleanValue())
				{
					selectChildren(child);
					((CheckBox)rootModule.getGraphic()).setSelected(false);
				}
				else
				{
					uncheckRootIfAnyChecked(rootModule);
				}
			});
			treeItem.getChildren().add(child);
			populateModules(child, depth + 1);
		}
		treeItem.getChildren().sort(new Comparator<TreeItem<Integer>>()
		{
			@Override
			public int compare(TreeItem<Integer> o1, TreeItem<Integer> o2)
			{
				return readViewProperties.getDescriptionText(o1.getValue()).orElse("").compareTo(readViewProperties.getDescriptionText(o2.getValue()).orElse(""));
			}
		});
	}
	
	private boolean uncheckRootIfAnyChecked(TreeItem<Integer> treeItem)
	{
		for (TreeItem<Integer> ti : treeItem.getChildren())
		{
			if (((CheckBox)ti.getGraphic()).isSelected())
			{
				((CheckBox)rootModule.getGraphic()).setSelected(false);
				return true;
			}
			if (uncheckRootIfAnyChecked(ti))
			{
				return true;
			}
		}
		if (treeItem == rootModule)
		{
			((CheckBox)rootModule.getGraphic()).setSelected(true);
		}
		return false;
	}

	private void selectChildren(TreeItem<Integer> ti)
	{
		for (TreeItem<Integer> child : ti.getChildren())
		{
			((CheckBox)child.getGraphic()).setSelected(true);
			selectChildren(child);
		}
	}

	public AuthorModulePathRestriction getAmpRestriction()
	{
		NidSet authorRestriction;
		if (authors.getSelectionModel().getSelectedIndices().contains(0))
		{
			authorRestriction = null;
		}
		else
		{
			authorRestriction = new NidSet();
			authors.getSelectionModel().getSelectedItems().forEach(item -> authorRestriction.add(item));
		}
		
		NidSet moduleRestriction;
		if (((CheckBox)rootModule.getGraphic()).isSelected())
		{
			moduleRestriction = null;
		}
		else
		{
			moduleRestriction = new NidSet();
			toNidSet(rootModule, moduleRestriction);
		}
		
		NidSet pathRestriction;
		if (paths.getSelectionModel().getSelectedIndices().contains(0))
		{
			pathRestriction = null;
		}
		else
		{
			pathRestriction = new NidSet();
			paths.getSelectionModel().getSelectedItems().forEach(item -> pathRestriction.add(item));
		}
		return AuthorModulePathRestriction.restrict(authorRestriction, moduleRestriction, pathRestriction);
	}
	
	private void toNidSet(TreeItem<Integer> treeItem, NidSet moduleRestriction)
	{
		for (TreeItem<Integer> ti : treeItem.getChildren())
		{
			if (((CheckBox)ti.getGraphic()).isSelected())
			{
				moduleRestriction.add(ti.getValue());
			}
			toNidSet(ti, moduleRestriction);
		}
	}

	public TimeStatusRestriction getTimeStatusRestriction()
	{
		Set<Status> allowedStatuses;
		switch (status.getSelectionModel().getSelectedItem())
		{
			case ACTIVE:
				allowedStatuses = Status.makeActiveOnlySet();
				break;
			case ACTIVE_AND_INACTIVE:
				allowedStatuses = Status.makeAnyStateSet();
				break;
			case INACTIVE:
				allowedStatuses = Status.INACTIVE_STATUS_SET;
				break;
			default :
				throw new UnsupportedOperationException(status.getSelectionModel().getSelectedItem());

		}
		return new TimeStatusRestriction(timeSelectStart.getSelectionModel().getSelectedIndex() == 0 ? null 
				: timeStart.getDateTimeValue().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli(), 
				timeSelectEnd.getSelectionModel().getSelectedIndex() == 0 ? null 
						: timeEnd.getDateTimeValue().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli(), 
						allowedStatuses, readViewProperties.getManifoldCoordinate()
							);
	}

}
