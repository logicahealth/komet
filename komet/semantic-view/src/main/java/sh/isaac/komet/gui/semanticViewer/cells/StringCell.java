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
package sh.isaac.komet.gui.semanticViewer.cells;

import java.util.function.Function;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableCell;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.komet.gui.semanticViewer.SemanticGUI;
import sh.isaac.komet.gui.util.CustomClipboard;

/**
 * {@link StringCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class StringCell extends TreeTableCell<SemanticGUI, SemanticGUI>
{
	private Function<SemanticGUI, String> stringFetcher_;
	
	public StringCell(Function<SemanticGUI, String> stringFetcher)
	{
		stringFetcher_ = stringFetcher;
	}
	
	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(SemanticGUI item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(null);
		if (empty || item == null)
		{
			setText("");
		}
		else if (item != null)
		{
			setText(stringFetcher_.apply(item));
			ContextMenu cm = new ContextMenu();
			MenuItem mi = new MenuItem("Copy");
			mi.setGraphic(Images.COPY.createImageView());
			mi.setOnAction((action) -> 
			{
				CustomClipboard.set(stringFetcher_.apply(item));
			});
			cm.getItems().add(mi);
			setContextMenu(cm);
		}
	}
}
