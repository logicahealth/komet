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
package sh.komet.gui.search.extended;

import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ConceptExplorationNode;

import java.io.IOException;

/**
 * A search viewer evolved from the old lego editor, to the previous JavaFX gui,
 * to komet now. many features that need to be merged with the new search view
 * and/or the query builder.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Service(name = "Extended Search Provider")
@PerLookup
public class ExtendedSearchViewFactory implements ConceptSearchNodeFactory {

    protected static final Logger LOG = LogManager.getLogger();

    public ExtendedSearchViewFactory() throws IOException {
        // created by HK2
        long startTime = System.currentTimeMillis();
        LOG.debug(this.getClass().getSimpleName() + " construct time (blocking GUI): {}", System.currentTimeMillis() - startTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMenuText() {
        return "Extended Search";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getMenuIcon() {
        return Iconography.TARGET.getIconographic();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public String[] getDefaultActivityFeed() {
        return new String[] {ViewProperties.SEARCH};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConceptExplorationNode createNode(ViewProperties viewProperties, ActivityFeed activityFeed, IsaacPreferences preferencesNode) {
        ExtendedSearchViewController extendedSearchViewController = ExtendedSearchViewController.init(viewProperties, activityFeed);
        ExtendedSearchConceptExplorationNode conceptExplorationNode = new ExtendedSearchConceptExplorationNode(extendedSearchViewController, viewProperties);
        return conceptExplorationNode;
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.EXTENDED_SEARCH_PANEL____SOLOR;
    }
}
