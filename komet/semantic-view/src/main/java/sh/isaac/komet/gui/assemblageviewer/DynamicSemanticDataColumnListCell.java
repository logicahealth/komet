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

import org.apache.logging.log4j.LogManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicValidatorType;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicByteArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.utility.Frills;

/**
 * {@link DynamicSemanticDataColumnListCell}
 *
 * Display code for a data column of a Dynamic Sememe, when shown within a list view (one cell per refex column)
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicSemanticDataColumnListCell extends ListCell<DynamicColumnInfo>
{
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(DynamicColumnInfo item, boolean empty)
	{
		super.updateItem(item, empty);
		if (item != null)
		{
			setText("");

			GridPane gp = new GridPane();
			gp.setHgap(0.0);
			gp.setVgap(0.0);
			gp.setPadding(new Insets(5, 5, 5, 5));
			gp.setMinWidth(250);

			ColumnConstraints constraint1 = new ColumnConstraints();
			constraint1.setFillWidth(false);
			constraint1.setHgrow(Priority.NEVER);
			constraint1.setMinWidth(160);
			constraint1.setMaxWidth(160);
			gp.getColumnConstraints().add(constraint1);

			ColumnConstraints constraint2 = new ColumnConstraints();
			constraint2.setFillWidth(true);
			constraint2.setHgrow(Priority.ALWAYS);
			gp.getColumnConstraints().add(constraint2);

			int row = 0;

			gp.add(wrapAndStyle(makeBoldLabel("Attribute Name"), row), 0, row);
			Label name = new Label(item.getColumnName());
			name.setWrapText(true);
			name.maxWidthProperty().bind(this.widthProperty().subtract(210));
			gp.add(wrapAndStyle(name, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Attribute Description"), row), 0, row);
			Label description = new Label(item.getColumnDescription());
			description.setWrapText(true);
			description.maxWidthProperty().bind(this.widthProperty().subtract(210));

			gp.add(wrapAndStyle(description, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Attribute Order"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getColumnOrder() + 1 + ""), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Data Type"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getColumnDataType().getDisplayName()), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Attribute Required"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.isColumnRequired() + ""), row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Default Value"), row), 0, row);
			String temp = "";
			if (item.getDefaultColumnValue() != null)
			{
				if (item.getColumnDataType() == DynamicDataType.BYTEARRAY)
				{
					temp = "Byte array of size " + ((DynamicByteArray) item.getDefaultColumnValue()).getDataByteArray().length;
				}
				else if (item.getColumnDataType() == DynamicDataType.NID)
				{
					temp = Frills.getDescription(((DynamicNid)item.getDefaultColumnValue()).getDataNid(), null)
							.orElse("NID: " + item.getDefaultColumnValue().getDataObject().toString());
				}
				else if (item.getColumnDataType() == DynamicDataType.UUID)
				{
					temp = Frills.getDescription(((DynamicUUID)item.getDefaultColumnValue()).getDataUUID(), null, null)
							.orElse("UUID: " + item.getDefaultColumnValue().getDataObject().toString());
				}
				else
				{
					temp = item.getDefaultColumnValue().getDataObject().toString();
				}
			}
			Label defaultValue = new Label(temp);
			defaultValue.setWrapText(true);
			defaultValue.maxWidthProperty().bind(this.widthProperty().subtract(210));
			gp.add(wrapAndStyle(defaultValue, row), 1, row++);

			gp.add(wrapAndStyle(makeBoldLabel("Validator"), row), 0, row);
			gp.add(wrapAndStyle(new Label(item.getValidator() == null ? "" : 
				(valUnwrap(item) == DynamicValidatorType.EXTERNAL ? "Drools" : valUnwrap(item).getDisplayName())),
				row), 1, row++);

			if (item.getValidator() != null)
			{
				gp.add(wrapAndStyle(makeBoldLabel("Validator Data"), row), 0, row);
				String validatorData = "";
				if (valDataUnwrap(item) == null)
				{
					validatorData = "[null]";
					LogManager.getLogger(this.getClass()).warn("Null validator data on " + item.getColumnName() + " - " + item.getColumnOrder());
					//I saw this case once, but had a odd DB state at the time.  Leave warning, as it shouldn't happen in normal use.
				}
				else if (valDataUnwrap(item).getDynamicDataType() == DynamicDataType.BYTEARRAY)
				{
					validatorData = "Byte array of size " + ((DynamicByteArray) valDataUnwrap(item)).getDataByteArray().length;
				}
				else if (valDataUnwrap(item).getDynamicDataType() == DynamicDataType.NID)
				{
					validatorData = Frills.getDescription(((DynamicNid)valDataUnwrap(item)).getDataNid(), null, null)
							.orElse("NID: " + valDataUnwrap(item).getDataObject().toString());
				}
				else if (valDataUnwrap(item).getDynamicDataType() == DynamicDataType.UUID)
				{
					validatorData = Frills.getDescription(((DynamicUUID)valDataUnwrap(item)).getDataUUID(), null, null)
							.orElse("UUID: " + valDataUnwrap(item).getDataObject().toString());
				}
				else if (valUnwrap(item) == DynamicValidatorType.EXTERNAL)
				{
					//noop at the moment
				}
						
				else
				{
					validatorData = valDataUnwrap(item).getDataObject().toString();
				}
				Label valData = new Label(validatorData);
				valData.setWrapText(true);
				valData.maxWidthProperty().bind(this.widthProperty().subtract(210));
				gp.add(wrapAndStyle(valData, row), 1, row++);
			}

			setGraphic(gp);
			this.setStyle("-fx-border-width:  0 0 2 0; -fx-border-color: grey; ");

		}
		else
		{
			setText("");
			setGraphic(null);
			this.setStyle("");
		}
	}

	private Node wrapAndStyle(Region node, int rowNumber)
	{
		Pane p = new Pane(node);
		node.setPadding(new Insets(5.0));
		GridPane.setFillWidth(p, true);
		p.minHeightProperty().bind(node.heightProperty());
		//Hack - wrapped labels don't seem to fire their height property changes at the right time - leaving the surrounding Pane node too small.
		//this seems to help...
		Platform.runLater(() -> p.autosize());
		p.getStyleClass().add(((rowNumber % 2 == 0) ? "evenGridRow" : "oddGridRow"));
		return p;
	}

	private Label makeBoldLabel(String labelText)
	{
		Label l = new Label(labelText);
		l.getStyleClass().add("boldLabel");
		return l;
	}
	
	
	//TODO get rid of these next two methods when we properly support multiple validators
	private DynamicValidatorType valUnwrap(DynamicColumnInfo info)
	{
		if (info.getValidator() == null || info.getValidator().length == 0)
		{
			return null;
		}
		return info.getValidator()[0];
	}
	
	private DynamicData valDataUnwrap(DynamicColumnInfo info)
	{
		if (info.getValidatorData() == null || info.getValidatorData().length == 0)
		{
			return null;
		}
		return info.getValidatorData()[0];
	}
}
