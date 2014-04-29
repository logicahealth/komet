/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.dataTypes;

import java.beans.PropertyVetoException;
import org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData;

/**
 * 
 * {@link TtkRefexPolymorphic}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TtkRefexPolymorphic extends TtkRefexDynamicData
{
	public TtkRefexPolymorphic(byte[] data)
	{
		super(data);
		if (data.length > 0)
		{
			throw new RuntimeException("Instances of polymorphic cannot have data");
		}
	}

	public TtkRefexPolymorphic() throws PropertyVetoException
	{
		super();
	}

	/**
	 * @see org.ihtsdo.otf.tcc.dto.component.refexDynamic.data.TtkRefexDynamicData#getDataObject()
	 */
	@Override
	public Object getDataObject()
	{
		return null;
	}
}
