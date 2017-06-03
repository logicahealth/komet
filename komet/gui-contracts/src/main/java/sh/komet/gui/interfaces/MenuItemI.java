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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Window;

/**
 * MenuItemI
 * 
 * An interface for views to provide specs for a menu that should be created on behalf of the view
 * in the main application
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public abstract class MenuItemI implements Comparable<MenuItemI>
{
	/**
	 * The FxMenuId of the MenuItem this menu should be added to.  Leave null for this to be treated as a new top-level
	 * menu in the applications menu bar.
	 * 
	 * Currently standard known top level menus are listed in {@link ApplicationMenus}
	 */
	public abstract String getParentMenuId();
	
	/**
	 * The FxMenuId of this menu item, should uniquely identify the menu across the application
	 */
	public abstract String getMenuId();
	
	
	/**
	 * The text to use for the Menu itself
	 */
	public abstract String getMenuName();
	
	/**
	 * Should the MenuName be parsed for mnemonics
	 */
	public abstract boolean enableMnemonicParsing();
	
	/**
	 * Desired sort order for this menu item, relative to other menu items in the same parent menu.
	 */
	public abstract int getSortOrder();
	
	/**
	 * Called when the user selects the menu.  Hands back a reference to the JavaFX menu.
	 */
	public abstract void handleMenuSelection(Window parent, MenuItem menuItem);
	
	/**
	 * The image that should be used with this menu.  Null is allowed.
	 * Not abstract, as it was added later, and I didn't want to break everything
	 */
	public Node getImage()
	{
		return null;
	}
	
	/**
	 * Allow the menu creator to specify a binding that will enable / disable this menu
	 * Null is allowed (and is the default value)
	 */
	public ObservableValue<? extends Boolean> getDisableBinding()
	{
		return null;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MenuItemI o)
	{
		int i = getSortOrder() - o.getSortOrder();
		if (i == 0)
		{
			return getMenuName().compareTo(o.getMenuName());
		}
		else
		{
			return i;
		}
	}
}
