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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import sh.isaac.dbConfigBuilder.fx.fxUtil.Images;

/**
 * HeaderNode
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 * @param <T> 
 *
 *
 */
@SuppressWarnings("restriction")
public class HeaderNode<T>
{
	public static interface DataProvider<T>
	{
		public T getData(SemanticGUI source);
	}
	public static class Filter<T>
	{
		private final Set<T> allPotentialFilterValues = new HashSet<>();
		private final ObservableList<Object> filterValues = new ObservableListWrapper<>(new ArrayList<>());
		private final ColumnId columnId;
		private DataProvider<T> dataProvider;

		/**
		 * @param columnId 
		 * @param dataProvider 
		 */
		public Filter(ColumnId columnId, DataProvider<T> dataProvider)
		{
			super();
			this.columnId = columnId;
			this.dataProvider = dataProvider;
		}

		public boolean accept(SemanticGUI data)
		{
			if (filterValues.size() > 0)
			{
				return filterValues.contains(dataProvider.getData(data));
			}
			else
			{
				return true;
			}
		}

		public ObservableList<Object> getFilterValues()
		{
			return filterValues;
		}

		/**
		 * @return the allPotentialFilterValues
		 */
		public Set<T> getAllPotentialFilterValues()
		{
			return allPotentialFilterValues;
		}

		/**
		 * @return the columnId
		 */
		public ColumnId getColumnId()
		{
			return columnId;
		}
	}

	private final TreeTableColumn<SemanticGUI, ?> column;
	private final Scene scene;
	private final DataProvider<T> dataProvider;
	private final Button filterConfigurationButton = new Button();
	private final Filter<T> filter;

	private final ImageView image = Images.FILTER_16.createImageView();

	@SuppressWarnings("unchecked")
	private Filter<T> castFilterFromCache(Filter<?> filter)
	{
		return (Filter<T>) filter;
	}

	public HeaderNode(ObservableMap<ColumnId, Filter<?>> filterCache, TreeTableColumn<SemanticGUI, ?> col, ColumnId columnId, Scene scene,
			DataProvider<T> dataProvider)
	{
		this.column = col;
		this.scene = scene;

		this.image.setFitHeight(8);
		this.image.setFitWidth(8);
		this.dataProvider = dataProvider;

		if (filterCache.get(columnId) != null)
		{
			this.filter = castFilterFromCache(filterCache.get(columnId));
			this.filter.dataProvider = dataProvider;
		}
		else
		{
			this.filter = new Filter<>(columnId, dataProvider);
			filterCache.put(columnId, filter);
		}

		filterConfigurationButton.setGraphic(image);
		Platform.runLater(() -> {
			filterConfigurationButton.setTooltip(new Tooltip("Press to configure filters for " + col.getText()));
		});

		filter.getFilterValues().addListener(new ListChangeListener<Object>()
		{
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Object> c)
			{
				updateButton();
			}
		});
		updateButton();

		filterConfigurationButton.setOnAction(event -> {
			setUserFilters(column.getText());
		});
	}

	private void updateButton()
	{
		if (filter.getFilterValues().size() > 0)
		{
			filterConfigurationButton.setStyle("-fx-background-color: red;" + "-fx-padding: 0 0 0 0;");
		}
		else
		{
			filterConfigurationButton.setStyle("-fx-background-color: white;" + "-fx-padding: 0 0 0 0;");
		}
	}

	private static <T> Set<T> getUniqueDisplayObjects(TreeItem<SemanticGUI> item, DataProvider<T> dataProvider)
	{
		Set<T> stringSet = new HashSet<>();

		if (item == null)
		{
			return stringSet;
		}

		if (item.getValue() != null)
		{
			stringSet.add(dataProvider.getData(item.getValue()));
		}

		for (TreeItem<SemanticGUI> childItem : item.getChildren())
		{
			stringSet.addAll(getUniqueDisplayObjects(childItem, dataProvider));
		}

		return stringSet;
	}

	private void setUserFilters(String text)
	{
		List<String> testList = new ArrayList<String>();

		for (T obj : getUniqueDisplayObjects(column.getTreeTableView().getRoot(), dataProvider))
		{
			if (obj != null)
			{
				filter.allPotentialFilterValues.add(obj);
			}
		}

		// TODO allPotentialFilterValues should be populated on initial, unfiltered, load, not deferred until HeaderNode activation
		for (T obj : filter.allPotentialFilterValues)
		{
			testList.add(obj.toString());
		}

		Collections.sort(testList);

		SemanticContentFilterPrompt prompt = new SemanticContentFilterPrompt(text, testList, filter.getFilterValues());

		if (prompt.showUserPrompt(scene.getWindow(), "Select Filters"))
		{
			filter.getFilterValues().setAll(prompt.getSelectedValues());
		}
	}

	public Button getButton()
	{
		return filterConfigurationButton;
	}

	public TreeTableColumn<SemanticGUI, ?> getColumn()
	{
		return column;
	}

	public ObservableList<Object> getUserFilters()
	{
		return filter.getFilterValues();
	}

	public Node getNode()
	{
		return filterConfigurationButton;
	}
}
