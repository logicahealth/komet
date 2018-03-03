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
package sh.isaac.komet.gui.util;

import java.util.ArrayList;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

/**
 * {@link CustomClipboard} A wrapper simplifying access to a {@link Clipboard} object.
 * 
 * @author ocarlsen
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class CustomClipboard
{
	private final static Clipboard clipboard_ = Clipboard.getSystemClipboard();
	private static Object object_ = null;
	private static DataFormat type_ = new DataFormat("type");
	
	private static ArrayList<Binding<Boolean>> bindings_ = new ArrayList<Binding<Boolean>>();
	
	public static BooleanBinding containsObject = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return object_ != null;
		}
	};
	
	/**
	 * Note, this binding cannot be trusted 100%.  If the user is simply copying and pasting 
	 * strings within our application, it will be accurate.  However, if the user copies and pastes
	 * outside of the application, this will be invalid.  Callers will have to call {@link #getString}
	 * themselves to verify the content, before doing anything with the string. 
	 */
	public static BooleanBinding containsString = new BooleanBinding()
	{
		@Override
		protected boolean computeValue()
		{
			return getString().length() > 0;
		}
	};
	
	static
	{
		bindings_.add(containsObject);
		bindings_.add(containsString);
	}
	
	public static void updateBindings()
	{
		for (Binding<Boolean> b : bindings_)
		{
			b.invalidate();
		}
	}

	public static void set(String content)
	{
		if (content == null)
		{
			return;
		}
		object_ = null;
		ClipboardContent cc = new ClipboardContent();
		cc.putString(content);
		clipboard_.setContent(cc);
		updateBindings();
	}

	public static void set(Object value, String stringValue)
	{
		object_ = value;
		ClipboardContent cc = new ClipboardContent();
		cc.putString(stringValue);
		cc.put(type_, value.getClass().getName());
		clipboard_.setContent(cc);
		updateBindings();
	}
	
	public static boolean containsType(Class<?> clazz)
	{
		String value = (String) clipboard_.getContent(type_);
		if (value == null)
		{
			return false;
		}
		else
		{
			return value.equals(clazz.getName());
		}
	}
	
	public static String getString()
	{
		String temp = clipboard_.getString();
		return (temp == null ? "" : temp);
	}
	
	public static Object getObject()
	{
		return object_;
	}
}
