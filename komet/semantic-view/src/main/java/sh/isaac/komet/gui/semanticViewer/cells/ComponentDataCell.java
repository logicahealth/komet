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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeTableCell;
import javafx.scene.text.Text;
import sh.isaac.api.Get;
import sh.isaac.komet.gui.semanticViewer.SemanticGUI;
import sh.isaac.komet.gui.semanticViewer.SemanticGUIColumnType;
import sh.komet.gui.drag.drop.DragRegistry;

/**
 * {@link ComponentDataCell}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ComponentDataCell extends TreeTableCell<SemanticGUI, SemanticGUI>
{
	private static Logger logger_ = LogManager.getLogger(ComponentDataCell.class);
	
	private SemanticGUIColumnType type_;
	
	public ComponentDataCell(SemanticGUIColumnType type)
	{
		type_ = type;
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
			conceptLookup(item);
		}
	}
	
	private void conceptLookup(SemanticGUI item)
	{
		ProgressBar pb = new ProgressBar();
		pb.setMaxWidth(Double.MAX_VALUE);
		setGraphic(pb);
		
		setText(null);
		Task<Void> t = new Task<Void>()
		{
			String text;
			boolean setStyle = false;
			boolean configureDragAndDrop = false;
			ContextMenu cm = new ContextMenu();
			int nid = item.getNidFetcher(type_, null).applyAsInt(item.getSememe());
			
			@Override
			protected Void call() throws Exception
			{
				try
				{
					text = item.getDisplayStrings(type_, null).getKey();
					
					switch (Get.identifierService().getObjectTypeForComponent(nid))
					{
						case CONCEPT:
						{
							//TODO support indexing config
//							if (SemanticGUIColumnType.ASSEMBLAGE == type_ && DynamicUsageDescriptionImpl.isDynamicSememe(nid))
//							{
//								MenuItem mi = new MenuItem("Configure Sememe Indexing");
//								mi.setOnAction((action) ->
//								{
//									new ConfigureDynamicRefexIndexingView(nid).showView(null);
//								});
//								mi.setGraphic(Images.CONFIGURE.createImageView());
//								cm.getItems().add(mi);
//							}
							
							configureDragAndDrop = true;
	
							//TODO common menus support?
//							CommonMenus.addCommonMenus(cm, new CommonMenusNIdProvider()
//							{
//								@Override
//								public Collection<Integer> getNIds()
//								{
//									return Arrays.asList(new Integer[] {item.getNidFetcher(type_, null).applyAsInt(item.getSememe())});
//								}
//							});
							setStyle = true;
							break;
						}
						case SEMANTIC:
						{
							//TODO support common menus
//							@SuppressWarnings({ "unchecked", "rawtypes" })
//							Optional<LatestVersion<SememeVersion<?>>> sv = Get.assemblageService().getSemanticChronology(nid)
//									.getLatestVersion(ExtendedAppContext.getUserProfileBindings().getStampCoordinate().get());
//							if (sv.isPresent())
//							{
//								CommonMenuBuilderI menuBuilder = CommonMenus.CommonMenuBuilder.newInstance();
//								menuBuilder.setMenuItemsToExclude(CommonMenuItem.COPY_SCTID);
//								
//								CommonMenus.addCommonMenus(cm, menuBuilder, new CommonMenusNIdProvider()
//								{
//									
//									@Override
//									public Collection<Integer> getNIds()
//									{
//										//TODO won't work for nested sememes!  need to recurse
//										return Arrays.asList(new Integer[] {sv.get().value().getReferencedComponentNid()});
//									}
//								});
//							}
							break;
						}
						default :
						{
							logger_.warn("Unexpected chronology type! " + Get.identifierService().getObjectTypeForComponent(nid).toString());
							return null;
						}
						
					}
				}
				catch (Exception e)
				{
					logger_.error("Unexpected error", e);
					text= "-ERROR-";
				}
				return null;
			}

			/**
			 * @see javafx.concurrent.Task#succeeded()
			 */
			@Override
			protected void succeeded()
			{
				//default text is a label, which doesn't wrap properly.
				if (isEmpty() || getItem() == null)
				{
					//We are updating a cell that has sense been changed to empty - abort!
					return;
				}
				setText(null);
				Text textHolder = new Text(text);
				textHolder.wrappingWidthProperty().bind(widthProperty().subtract(10));
				if (cm.getItems().size() > 0)
				{
					setContextMenu(cm);
				}
				setGraphic(textHolder);
				if (setStyle)
				{
					if (item.isCurrent())
					{
						getTreeTableRow().getStyleClass().removeAll("historical");
					}
					else
					{
						if (!getTreeTableRow().getStyleClass().contains("historical"))
						{
							getTreeTableRow().getStyleClass().add("historical");
						}
					}
				}
				if (configureDragAndDrop)
				{
					Get.service(DragRegistry.class).setupDragOnly(textHolder);
				}
			}
		};
		Get.workExecutors().getExecutor().execute(t);
	}
}
