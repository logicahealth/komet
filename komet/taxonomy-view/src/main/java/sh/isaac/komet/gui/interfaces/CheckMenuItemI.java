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

import javafx.scene.control.CheckMenuItem;
import javafx.stage.Window;

/**
 * CheckMenuItemI
 * 
 * An interface for views to provide specs for a menu that should be created on behalf of the view
 * in the main application - and should be created as a "Check" menu item.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class CheckMenuItemI extends MenuItemI
{
	/**
	 * True, if the menu should be checked initially, false if not.
	 * Default (if not overridden) is false
	 */
	public boolean initialState()
	{
		return false;
	}
	/**
	 * Called when the user selects the menu.  Hands back a reference to the JavaFX menu.
	 * Not abstract, so users don't have to implement it, if they don't want to.
	 */
	public void handleMenuSelection(Window parent, CheckMenuItem menuItem)
	{
		//noop
	}
}
