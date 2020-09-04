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
package sh.isaac.komet.gui.semanticViewer;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;
import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.DetailNodeFactory;
import sh.komet.gui.contract.DetailType;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.DetailNode;

/**
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service(name = "Semantic Tree Table View")
@Singleton
public class SemanticViewerFactory implements DetailNodeFactory
{

	@Override
	public DetailNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences nodePreferences)
	{
		return new SemanticViewerNode(viewProperties, activityFeed, nodePreferences);
	}
	
	@Override
	public String[] getDefaultActivityFeed()
	{
		return new String[] {ViewProperties.NAVIGATION, ViewProperties.SEARCH};
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public String getMenuText()
	{
		return "Semantic Tree Table View";
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Node getMenuIcon()
	{
		return Iconography.TAXONOMY_CLICK_TO_OPEN.getIconographic();
	}
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public DetailType getSupportedType()
	{
		return DetailType.Concept;
	}

	@Override
	public ConceptSpecification getPanelType() {
		return MetaData.SEMANTIC_TREE_TABLE_PANEL____SOLOR;
	}
}
