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

import java.util.AbstractMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.text.Text;
import sh.isaac.api.Get;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;
import sh.isaac.komet.gui.semanticViewer.SemanticGUI;
import sh.isaac.komet.gui.semanticViewer.SemanticGUIColumnType;
import sh.isaac.komet.gui.util.CustomClipboard;
import sh.komet.gui.drag.drop.DragRegistry;

/**
 * {@link AttachedDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class AttachedDataCell extends TreeTableCell<SemanticGUI, SemanticGUI>
{
	private Hashtable<UUID, List<DynamicColumnInfo>> columnInfo_;
	private int listItem_;
	private static Logger logger_ = LogManager.getLogger(AttachedDataCell.class);

	public AttachedDataCell(Hashtable<UUID, List<DynamicColumnInfo>> columnInfo, int listItem)
	{
		super();
		columnInfo_ = columnInfo;
		listItem_ = listItem;
	}

	/**
	 * @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean)
	 */
	@Override
	protected void updateItem(SemanticGUI item, boolean empty)
	{
		super.updateItem(item, empty);
		
		if (empty || item == null)
		{
			setText("");
			setGraphic(null);
		}
		else if (item != null)
		{
			try
			{
				for (UUID uuid : columnInfo_.keySet())
				{
					if (Get.identifierService().getNidForUuids(uuid) == item.getSememe().getAssemblageNid())
					{
						List<DynamicColumnInfo> colInfo =  columnInfo_.get(uuid);
						Integer refexColumnOrder = (colInfo.size() > listItem_ ? 
								(SemanticGUI.getData(item.getSememe()).length <= colInfo.get(listItem_).getColumnOrder() ? null 
									: colInfo.get(listItem_).getColumnOrder()): null);
						DynamicData data = (refexColumnOrder == null ? null : SemanticGUI.getData(item.getSememe())[refexColumnOrder]); 
						if (data != null)
						{
							if (data instanceof DynamicNid)
							{
								conceptLookup(item, refexColumnOrder);
							}
							else if (data instanceof DynamicUUID)
							{
								conceptLookup(item, refexColumnOrder);
							}
							else
							{
								AbstractMap.SimpleImmutableEntry<String, String> texts = item.getDisplayStrings(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder);
								
								if (texts == null || texts.getKey() == null)
								{
									setText("");
									setGraphic(null);
								}
								else
								{
									//default text is a label, which doesn't wrap properly.
									setText(null);
									Text textHolder = new Text(texts.getKey());
									textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
									setGraphic(textHolder);
									ContextMenu cm = new ContextMenu();
									MenuItem mi = new MenuItem("Copy");
									mi.setGraphic(Images.COPY.createImageView());
									mi.setOnAction((action) -> 
									{
										CustomClipboard.set(texts.getKey());
									});
									cm.getItems().add(mi);
									setContextMenu(cm);
									if (texts.getValue() != null && texts.getValue().length() > 0)
									{
										setTooltip(new Tooltip(texts.getValue()));
									}
								}
							}
						}
						else
						{
							//Not applicable, for the current row.
							setText("");
							setGraphic(null);
						}
						return;
					}
				}
			}
			catch (Exception e)
			{
				logger_.error("Unexpected error rendering data cell", e);
				setText("-ERROR-");
				setGraphic(null);
			}
			//Not applicable, for the current row.
			setText("");
			setGraphic(null);
		}
	}
	
	private void conceptLookup(final SemanticGUI item, final Integer refexColumnOrder)
	{
		setGraphic(new ProgressBar());
		setText(null);
		ContextMenu cm = new ContextMenu();
		Get.workExecutors().getExecutor().execute(() ->
		{
			AbstractMap.SimpleImmutableEntry<String, String> value = item.getDisplayStrings(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder);
				
			//TODO support common menus
//			CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
//			{
//				
//				@Override
//				public Collection<Integer> getNIds()
//				{
//					int nid = item.getNidFetcher(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder).applyAsInt(item.getSememe());
//
//					ArrayList<Integer> nids = new ArrayList<>();
//					if (nid != 0)
//					{
//						nids.add(nid);
//					}
//					return nids;
//				}
//			});

			Platform.runLater(() ->
			{
				if (isEmpty() || getItem() == null)
				{
					//We are updating a cell that has sense been changed to empty - abort!
					return;
				}
				if (value.getValue() != null && value.getValue().length() > 0)
				{
					setTooltip(new Tooltip(value.getValue()));
				}
				if (cm.getItems().size() > 0)
				{
					setContextMenu(cm);
				}
				Text textHolder = new Text(value.getKey());
				textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
				setGraphic(textHolder);
				Get.service(DragRegistry.class).setupDragOnly(textHolder, () -> 
				{
					return item.getNidFetcher(SemanticGUIColumnType.ATTACHED_DATA, refexColumnOrder).applyAsInt(item.getSememe());
				});
				setText(null);
			});
		});
	}
}
