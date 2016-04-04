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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.ibdf.provider;

import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.externalizable.StampAlias;
import gov.vha.isaac.ochre.api.externalizable.StampComment;
import gov.vha.isaac.ochre.model.concept.ConceptChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;

/**
 * {@link OchreExternalizableUnparsed}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class OchreExternalizableUnparsed
{
	private ByteArrayDataBuffer data_;
	OchreExternalizableObjectType type_;

	public OchreExternalizableUnparsed(OchreExternalizableObjectType type, ByteArrayDataBuffer data)
	{
		data_ = data;
		type_ = type;
	}

	public OchreExternalizable parse()
	{
		switch (type_)
		{
			case CONCEPT:
				return ConceptChronologyImpl.make(data_);
			case SEMEME:
				return SememeChronologyImpl.make(data_);
			case STAMP_ALIAS:
				return new StampAlias(data_);
			case STAMP_COMMENT:
				return new StampComment(data_);
			default :
				throw new UnsupportedOperationException("Can't handle: " + type_);
		}
	}
}
