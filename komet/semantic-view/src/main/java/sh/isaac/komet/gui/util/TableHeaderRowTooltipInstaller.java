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
package sh.isaac.komet.gui.util;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import sh.isaac.api.Get;

/**
 * {@link TableHeaderRowTooltipInstaller}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@SuppressWarnings("restriction")
public class TableHeaderRowTooltipInstaller
{
	private static Logger logger_ = LogManager.getLogger(TableHeaderRowTooltipInstaller.class);

	// TODO this mechanism won't work right if some of the columns are disabled by default. would need an alternate approach
	// maybe while scanning here, register a listener on column visibility flags?
	/**
	 * Installs tooltips on the header row of things like treeTableView, listView, etc.
	 * 
	 * Nasty hack code that is brittle, as it digs into JavaFX provided objects.
	 * 
	 * @param node - Typically a TreeTableView, ListView, something along those lines.
	 * @param nameToTooltipMap - The tooltip strings to put on each header row. Tooltips
	 *            are the values - they are a list to handle cases where multiple columns have the same name.
	 *            The first item in the list will be used on the first encountered col match, the next on the next.
	 */
	public static void installTooltips(Parent node, Map<String, List<String>> nameToTooltipMap)
	{
		// Background thread this work
		Get.workExecutors().getExecutor().execute(new Runnable()
		{
			@Override
			public void run()
			{
				installTooltipsThreaded(node, nameToTooltipMap);
			}
		});
	}

	private static void installTooltipsThreaded(Parent node, Map<String, List<String>> nameToTooltipMap)
	{
		int tries = 0;
		while (tries++ < 5 && nameToTooltipMap.size() > 0)
		{
			logger_.debug(nameToTooltipMap.size() + " tooltips to install");
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					processLevelOne(node, nameToTooltipMap);
				}
			});

			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{
				logger_.warn("Interruppted while installing tooltips?");
				break;
			}
		}
		if (nameToTooltipMap.size() > 0)
		{
			// This happens for various reasons at the moment... don't log a warn
			logger_.debug("Failed to install at least " + nameToTooltipMap.size() + " tooltips");
		}
		else
		{
			logger_.debug("Successfully installed all tooltips");
		}
	}

	private static void processLevelOne(Parent node, Map<String, List<String>> nameToTooltipMap)
	{
		for (Node subNode : node.getChildrenUnmodifiable())
		{
			if (subNode instanceof TableHeaderRow)
			{
				processLevelDeeper((TableHeaderRow) subNode, nameToTooltipMap);
			}
		}
	}

	private static void processLevelDeeper(Parent node, Map<String, List<String>> nameToTooltipMap)
	{
		if (node instanceof Label)
		{
			installTooltip((Label) node, nameToTooltipMap);
		}
		else
		{
			for (Node subNode : node.getChildrenUnmodifiable())
			{
				if (subNode instanceof Parent)
				{
					processLevelDeeper((Parent) subNode, nameToTooltipMap);
				}
			}
		}
	}

	private static void installTooltip(Label l, Map<String, List<String>> nameToTooltipMap)
	{
		if (l.getText().length() == 0)
		{
			return;
		}
		List<String> values = nameToTooltipMap.get(l.getText());
		String value;
		if (values != null)
		{
			if (values.size() == 0)
			{
				nameToTooltipMap.remove(l.getText());
				logger_.info("No tooltip text available for column " + l.getText());
				return;
			}
			else
			{
				value = values.remove(0);
				if (values.size() == 0)
				{
					nameToTooltipMap.remove(l.getText());
				}
				Tooltip t = new Tooltip(value);
				t.setMaxWidth(500);
				t.setWrapText(true);
				Tooltip.install(l, t);
			}
		}
		else
		{
			logger_.info("No tooltip text available for column " + l.getText());
			return;
		}
	}
}
