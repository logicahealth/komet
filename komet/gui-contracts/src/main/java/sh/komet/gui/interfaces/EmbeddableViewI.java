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
package sh.komet.gui.interfaces;

import javafx.scene.layout.Region;
import org.jvnet.hk2.annotations.Contract;

/**
 * {@link EmbeddableViewI}
 * 
 * Represents an ISAAC graphical component which can be embedded into other components. 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface EmbeddableViewI extends IsaacViewI
{
	/**
	 * Get a reference to the JavaFX Region component that is created by this view.
	 */
	public Region getView();
	
	/**
	 * May be called by a parent window to inform the embedded view that it is no longer required.
	 * An embedded view may wish to disable all listeners that are triggering expensive refresh operations,
	 * for example, after a view has been discarded.
	 * 
	 * Otherwise, the bindings may continue firing, and causing refresh operations until the next iteration
	 * of the garbage collector.
	 */
	public void viewDiscarded();
}
