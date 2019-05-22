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

import java.io.IOException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.preferences.IsaacPreferences;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.manifold.Manifold.ManifoldGroup;
import sh.komet.gui.util.FxGet;
import sh.komet.gui.contract.ConceptSearchNodeFactory;
import sh.komet.gui.interfaces.ConceptExplorationNode;

/**
 * A search viewer evolved from the old lego editor, to the previous JavaFX gui,
 * to komet now. many features that need to be merged with the new search view
 * and/or the query builder.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Service
@PerLookup
public class ExtendedSearchViewFactory implements ConceptSearchNodeFactory {

    private ExtendedSearchViewController esvc_;
    private Manifold manifold_;
    private final Logger LOG = LogManager.getLogger(this.getClass());

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
     */
    @Override
    public boolean isEnabled() {
        return FxGet.fxConfiguration().isShowBetaFeaturesEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ManifoldGroup[] getDefaultManifoldGroups() {
        return new ManifoldGroup[]{ManifoldGroup.SEARCH};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PanelPlacement getPanelPlacement() {
        return PanelPlacement.RIGHT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConceptExplorationNode createNode(Manifold manifold, IsaacPreferences preferencesNode) {
        manifold_ = manifold;
        esvc_ = ExtendedSearchViewController.init(manifold_);
        return new ExtendedSearchConceptExplorationNode(esvc_, manifold_);
    }

    @Override
    public ConceptSpecification getPanelType() {
        return MetaData.EXTENDED_SEARCH_PANEL____SOLOR;
    }
}
