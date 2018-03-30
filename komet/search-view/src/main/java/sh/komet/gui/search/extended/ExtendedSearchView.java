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
package sh.komet.gui.search.extended;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ExplorationNodeFactory;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.util.FxGet;


/**
 * A search viewer evolved from the old lego editor, to the previous JavaFX gui, 
 * to komet now.  many features that need to be merged with the new search view and/or 
 * the query builder.
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Service
@PerLookup
public class ExtendedSearchView implements ExplorationNodeFactory 
{
	private ExtendedSearchViewController esvc_;
	private Manifold manifold_;
	private final Logger LOG = LogManager.getLogger(this.getClass());
	
	private ExtendedSearchView() throws IOException
	{
		//created by HK2
		long startTime = System.currentTimeMillis();
		LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", System.currentTimeMillis() - startTime);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getMenuText()
	{
		return "Extended Search";
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Node getMenuIcon()
	{
		return Iconography.TARGET.getIconographic();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled()
	{
		return FxGet.fxConfiguration().isShowBetaFeaturesEnabled();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public ExplorationNode createExplorationNode(Manifold manifold, Consumer<Node> nodeConsumer)
	{
		manifold_ = manifold;
		esvc_ = ExtendedSearchViewController.init(manifold_);
		nodeConsumer.accept(esvc_.getRoot());
		
		return new ExplorationNode()
		{
			@Override
			public ReadOnlyProperty<String> getToolTip()
			{
				return new SimpleStringProperty("Shows all of the Dynamic Semantics in the system");
			}
			
			@Override
			public Optional<Node> getTitleNode()
			{
				return Optional.empty();
			}
			
			@Override
			public ReadOnlyProperty<String> getTitle()
			{
				return new SimpleStringProperty(getMenuText());
			}
			
			@Override
			public Node getNode()
			{
				return esvc_.getRoot();
			}
			
			@Override
			public Manifold getManifold()
			{
				return manifold_;
			}
		};
	}
}
