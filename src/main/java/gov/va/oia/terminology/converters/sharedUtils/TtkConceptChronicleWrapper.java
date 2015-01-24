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
package gov.va.oia.terminology.converters.sharedUtils;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.ihtsdo.otf.tcc.api.coordinate.Status;
import org.ihtsdo.otf.tcc.dto.TtkConceptChronicle;
import org.ihtsdo.otf.tcc.dto.component.description.TtkDescriptionChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.TtkRefexAbstractMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_string.TtkRefexStringMemberChronicle;
import org.ihtsdo.otf.tcc.dto.component.refex.type_uuid.TtkRefexUuidMemberChronicle;

/**
 * {@link TtkConceptChronicleWrapper}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class TtkConceptChronicleWrapper extends TtkConceptChronicle
{
	private EConceptUtility econUtil_;
	
	public TtkConceptChronicleWrapper(EConceptUtility util)
	{
		super();
		econUtil_ = util;
	}

	/**
	 * @see org.ihtsdo.otf.tcc.dto.TtkConceptChronicle#writeExternal(java.io.DataOutput)
	 */
	@Override
	public void writeExternal(DataOutput out) throws IOException
	{
		validate();
		super.writeExternal(out);
	}
	
	
	private void validate()
	{
		boolean havePreferredFSN = false;
		TtkDescriptionChronicle preferredFSN = null;
		boolean havePreferredSynonym = false;

		//Check and see if we have a preferred FSN
		for (TtkDescriptionChronicle d : getDescriptions())
		{
			if (havePreferredFSN && havePreferredSynonym)
			{
				break;
			}
			if (!havePreferredFSN && d.getTypeUuid().equals(EConceptUtility.fullySpecifiedNameUuid_))
			{
				if (isPreferred(d.getAnnotations()))
				{
					preferredFSN = d;
					havePreferredFSN = true;
				}
			}
			else if (!havePreferredSynonym && d.getTypeUuid().equals(EConceptUtility.synonymUuid_))
			{
				if (isPreferred(d.getAnnotations()))
				{
					havePreferredSynonym = true;
				}
			}
		}
		
		if (!havePreferredFSN)
		{
			//These two UUIDs are special cases - we are just writing out a skeleton concept of them - so that it merges - so we don't 
			//actually include the FSN, or anything else about them (just a new refset entry)
			if (!getPrimordialUuid().equals(EConceptUtility.pathOriginRefSetUUID_) && !getPrimordialUuid().equals(EConceptUtility.pathRefSetUUID_))
			{
				throw new RuntimeException("The concept " + this + " was created without an FSN");
			}
		}
		else if (!havePreferredSynonym)
		{
			//create one from the preferredFSN
			TtkDescriptionChronicle desc =  econUtil_.addDescription(this, preferredFSN.getText(), EConceptUtility.DescriptionType.SYNONYM, 
					true, null, null, Status.ACTIVE);
			
			//Copy over any extra annotations
			for (TtkRefexAbstractMemberChronicle<?> nestedAnnotation : preferredFSN.getAnnotations())
			{
				if (nestedAnnotation instanceof TtkRefexUuidMemberChronicle)
				{
					//Skip en-us - that is done automatically during the desc build.
					TtkRefexUuidMemberChronicle member = (TtkRefexUuidMemberChronicle)nestedAnnotation;
					if (!member.getRefexExtensionUuid().equals(EConceptUtility.usEnRefsetUuid_))
					{
						econUtil_.addUuidAnnotation(desc, member.getUuid1(), member.getRefexExtensionUuid());
					}
				}
				else if (nestedAnnotation instanceof TtkRefexStringMemberChronicle)
				{
					TtkRefexStringMemberChronicle member = (TtkRefexStringMemberChronicle)nestedAnnotation;
					econUtil_.addStringAnnotation(desc, member.getString1(), member.getRefexExtensionUuid(), member.getStatus());
				}
				else
				{
					throw new RuntimeException("Unhandled nested type: " + nestedAnnotation);
				}
			}
			
			econUtil_.getLoadStats().incDescriptionCopiedFromFSNCount();
		}
	}
	
	private boolean isPreferred(List<TtkRefexAbstractMemberChronicle<?>> annotations)
	{
		for (TtkRefexAbstractMemberChronicle<?> annotation: annotations)
		{
			if (annotation.getRefexExtensionUuid().equals(EConceptUtility.usEnRefsetUuid_)
					&& annotation instanceof TtkRefexUuidMemberChronicle 
					&& ((TtkRefexUuidMemberChronicle)annotation).getUuid1().equals(EConceptUtility.descriptionPreferredUuid_))
			{
				return true;
			}
		}
		return false;
	}
}
