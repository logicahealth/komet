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

package sh.isaac.komet.gui.semanticViewer;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.Callback;
import sh.isaac.api.Get;
import sh.komet.gui.contract.DialogService;

public class SememeContentFilterPrompt 
{
	public enum UserPromptResponse { APPROVE, CANCEL };
	protected UserPromptResponse buttonSelected = UserPromptResponse.CANCEL;
	final ListView<String> selectedValues = new ListView<>();
	private String columnName;
	private List<String> allValues = new ArrayList<String>();
	protected static Font boldFont = new Font("System Bold", 13.0);

	final List<String> alreadySelectedValues = new ArrayList<>();

	protected SememeContentFilterPrompt(String columnName, List<String> allValues, List<?> alreadySelectedValues)
	{
		this.columnName = columnName;
		for (String s : allValues)
		{
			if (!s.trim().isEmpty())
			{
				this.allValues.add(s.trim());
			}
		}

		for (Object obj : alreadySelectedValues)
		{
			if (obj != null)
			{
				this.alreadySelectedValues.add(obj.toString());
			}
		}
	}
	
	//TODO need to redo this since it doesn't extend UserPrompt

	protected Node createUserInterface()
	{
		VBox vb = new VBox(10);
		vb.setAlignment(Pos.CENTER);
		vb.setPadding(new Insets(15));

		Label panelName = createLabel("Filter Selection", 16);

		HBox columnHBox = new HBox(10);
		columnHBox.setAlignment(Pos.CENTER);
		Label columnAttrLabel = createLabel("Attribute:");
		Label columnValLabel = new Label(columnName);
		columnHBox.getChildren().addAll(columnAttrLabel, columnValLabel);

		vb.getChildren().addAll(panelName, columnHBox, createCheckBoxListView());

		return vb;
	}
	
	protected Label createLabel(String str) {
		Label l = new Label(str);
		l.setFont(boldFont);
		return l;
	}
	
	protected Label createLabel(String str, int fontSize) {
		Label l = new Label(str);
		Font f = new Font("System Bold", fontSize);
		l.setFont(f);
		
		return l;
	}

	private static class CheckableText
	{
		final String text;
		final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

		public CheckableText(String text)
		{
			this.text = text;
		}

		/**
		 * @return the text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * @return the selectedProperty
		 */
		public BooleanProperty getSelectedProperty()
		{
			return selectedProperty;
		}

		public void setSelected(boolean selected)
		{
			selectedProperty.set(selected);
		}

		@Override
		public String toString()
		{
			return text;
		}
	}

	private ListView<CheckableText> createCheckBoxListView()
	{
		ListView<CheckableText> listView = new ListView<>();

		final ObservableList<CheckableText> data = FXCollections.observableArrayList();

		listView.setCellFactory(new Callback<ListView<CheckableText>, ListCell<CheckableText>>()
		{
			@Override
			public ListCell<CheckableText> call(ListView<CheckableText> param)
			{
				ListCell<CheckableText> cell = new ListCell<CheckableText>()
				{
					@Override
					public void updateItem(CheckableText item, boolean empty)
					{
						super.updateItem(item, empty);
						if (empty)
						{
							setText(null);
							setGraphic(null);
						}
						else
						{
							setText(null);
							CheckBox checkBox = new CheckBox();
							checkBox.selectedProperty().bindBidirectional(item.selectedProperty);
							Label label = new Label(item.getText().replaceAll("\n", " "));
							label.setMaxWidth(280);
							label.setTooltip(new Tooltip(item.getText()));

							HBox graphic = new HBox();
							graphic.getChildren().addAll(checkBox, label);
							setGraphic(graphic);
						}
					}
				};

				return cell;
			}
		});
		listView.setItems(data);

		for (String value : allValues)
		{
			CheckableText item = new CheckableText(value);
			item.getSelectedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
				{
					if (!newValue)
					{
						selectedValues.getItems().remove(item.getText());
					}
					else
					{
						selectedValues.getItems().add(item.getText());
					}
				}
			});
			if (alreadySelectedValues.contains(value))
			{
				item.setSelected(true);
			}

			data.add(item);
		}

		return listView;
	}

	public ObservableList<String> getSelectedValues()
	{
		return selectedValues.getItems();
	}

	protected void displayInvalidMessage()
	{
		Get.service(DialogService.class).showInformationDialog("No Filters Selected", "Must select at least one filter or select Cancel Button");
	}

	/**
	 * @param window
	 * @param string
	 */
	public void showUserPrompt(Window window, String string)
	{
		// TODO Auto-generated method stub
		
	}
	
	public UserPromptResponse getButtonSelected() {
		return buttonSelected;
	}
}
