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
package sh.komet.gui.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * {@link SimpleValidBooleanProperty}
 * Basically the same thing as a SimpleBooleanProperty - but this also carries a field that tells WHY the property
 * is set to false - useful for doing GUI validator flags, where a reason is also needed.
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class SimpleValidBooleanProperty extends SimpleBooleanProperty
{
	private static final Logger log_ = LogManager.getLogger(SimpleValidBooleanProperty.class);
	private ReadOnlyStringWrapper reasonWhyInvalid_ = new ReadOnlyStringWrapper("");

	public SimpleValidBooleanProperty(boolean initialValue, String reasonWhyInvalid)
	{
		super(initialValue);
		if (!initialValue)
		{
			if (reasonWhyInvalid == null || reasonWhyInvalid.length() == 0)
			{
				throw new RuntimeException("Supply a reason when setting the SimpleValidBooleanBinding to false");
			}
			reasonWhyInvalid_.set(reasonWhyInvalid);
		}
		set(initialValue);
	}

	public ReadOnlyStringProperty getReasonWhyInvalid()
	{
		return reasonWhyInvalid_.getReadOnlyProperty();
	}

	public void setValid()
	{
		super.set(true);
		reasonWhyInvalid_.set("");
	}

	public void setInvalid(String reasonWhyInvalid)
	{
		if (reasonWhyInvalid == null || reasonWhyInvalid.length() == 0)
		{
			throw new RuntimeException("Supply a reason when setting the SimpleValidBooleanBinding to false");
		}
		super.set(false);
		reasonWhyInvalid_.set(reasonWhyInvalid);
	}

	/**
	 * @see javafx.beans.property.BooleanPropertyBase#set(boolean)
	 */
	@Override
	public void set(boolean newValue)
	{
		if (newValue)
		{
			setValid();
		}
		else
		{
			log_.error("API misuse - please call setInvalid(...) instead of set");
			setInvalid("?");
		}
	}

	/**
	 * @see javafx.beans.property.BooleanProperty#setValue(java.lang.Boolean)
	 */
	@Override
	public void setValue(Boolean v)
	{
		if (v == null)
		{
			log_.error("API misuse - please call setInvalid(...) instead of set (and don't use null)");
			set(false);
		}
		else
		{
			set(v.booleanValue());
		}
	}

	/**
	 * @see javafx.beans.property.BooleanPropertyBase#fireValueChangedEvent()
	 */
	@Override
	protected void fireValueChangedEvent()
	{
		try
		{
			super.fireValueChangedEvent();
		}
		catch (Exception e)
		{
			log_.error("Severe API messup - exception within one of the bindings attached to this binding!", e);
		}
	}
}
