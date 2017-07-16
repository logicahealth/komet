/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright 
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.interfaces;

import sh.komet.gui.interfaces.CheckMenuItemI;
import org.jvnet.hk2.annotations.Contract;

/**
 * DockedViewI
 *
 * A View type that is intended to be treated as a docked window within the parent 
 * app.  The docked view is not responsible for displaying itself - it simply provides
 * the view object which will have its visibility managed by the parent application. 
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Contract
public interface DockedViewI extends EmbeddableViewI, IsaacViewWithMenusI
{
	/**
	 * Get a reference to the MenuItemI spec that should be used to create the menu that will show this view.
	 * 
	 * Note that while the MenuItemI spec provides a method that will be called when the menu is selected, implementers
	 * of this method do not need to do anything when this method is called (though they may, if desired) as the parent
	 * application is expected to make the view visible when the menu is selected it whatever way is appropriate.  The 
	 * docked view itself wouldn't have the information necessary to know how to make itself visible.
	 * 
	 * @return the menu details - may return null, if no menu is requested to launch this view.
	 */
	public CheckMenuItemI getMenuBarMenuToShowView();
	
	/**
	 * A string suitable for use as the title of the window showing the view
	 */
	public String getViewTitle();
}
