/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.komet.gui.interfaces;

import sh.isaac.komet.gui.interfaces.EmbeddableViewI;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link TaxonomyViewI}
 * 
 * An interface that specifies the methods that a TaxonomyViewer should implement
 * to be useful for other parts of the application.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface TaxonomyViewI extends EmbeddableViewI
{
	/**
	 * Locate and highlight the requested concept in the taxonomy view
	 * @param uuid
	 * @param busyIndicator (optional) if this taxonomy view does the location in a background thread, 
	 * and a busyIndicator is provided, it will be set to false, when the location process completes.
	 */
	public void locateConcept(UUID uuid, final BooleanProperty busyIndicator);
	
	/**
	 * Locate and highlight the requested concept in the taxonomy view
	 * @param nid
	 * @param busyIndicator (optional) if this taxonomy view does the location in a background thread, 
	 * and a busyIndicator is provided, it will be set to false, when the location process completes.
	 */
	public void locateConcept(int nid, final BooleanProperty busyIndicator);
	
	/**
	 * Get the default implementation that computes the graphics for nodes, and allows 
	 * nodes to be filtered out of the hierarchy
	 */
	public MultiParentTreeItemDisplayPolicies getDefaultDisplayPolicies();
	
	/**
	 * Replace the default behavior that computes the graphics for nodes and allows filtering of nodes with custom code 
	 */
	public void setDisplayPolicies(MultiParentTreeItemDisplayPolicies policies);
	
	/**
	 * Rebuild the tree from scratch
	 */
	public void refresh();
}