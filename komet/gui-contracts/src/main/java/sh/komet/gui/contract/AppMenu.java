/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.contract;

import javafx.scene.control.Menu;
import sh.komet.gui.menu.MenuWithText;

/**
 * A enum of the menus provided at the top level of the app. Used in the {@link MenuProvider} interface.
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public enum AppMenu
{
	APP("_SOLOR Viewer"),
	FILE("_File"),
	EDIT("_Edit"),
	TOOLS("_Tools"),
	WINDOW("_Window"),
	NEW_WINDOW("_New Window"), // menu within the window menu
	HELP("_Help"),
	TASK("Task"); // Task menu button...

	private final String menuName;
	private Menu menu = null;

	private AppMenu(String niceName)
	{
		this.menuName = niceName;
	}

	public Menu getMenu()
	{
		if (menu == null)
		{
			menu = new MenuWithText(menuName);
			menu.setMnemonicParsing(true);
		}
		return menu;
	}
}
