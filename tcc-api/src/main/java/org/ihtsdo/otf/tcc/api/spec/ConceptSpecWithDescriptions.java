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
package org.ihtsdo.otf.tcc.api.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;

/**
 * {@link ConceptSpecWithDescriptions}
 * 
 * This class extends ConceptSpec purely to add handling for FSN / preferred description / non-preferred description / definition
 * in a _simple_ way so that we can define this sort of stuff in one place for generation during database creation.
 * 
 * It serves no other purpose - and in fact, it not a proper implementation for the added fields, as .hashCode / .equals / serialization
 * is not implemented for any of these fields... it will just revert to what ConceptSpec supports in those cases.
 * 
 * User beware!
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConceptSpecWithDescriptions extends ConceptSpec
{
	private static final long serialVersionUID = 1L;

	private transient String fsn_;
	private transient List<String> synonyms_ = new ArrayList<>();
	private transient List<String> definitions_ = new ArrayList<>();

	/**
	 * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
	 * Optional - not used if null is passed.
	 */
	public ConceptSpecWithDescriptions(String fsn, UUID uuid, String[] synonyms, String[] definitions, ConceptSpec parentConcept)
	{
		super(fsn, uuid, parentConcept);
		if (parentConcept == null)
		{
			setRelSpecs(new RelSpec[0]);
		}
		fsn_ = fsn;
		if (synonyms != null)
		{
			synonyms_.addAll(Arrays.asList(synonyms));
		}
		if (definitions != null)
		{
			definitions_.addAll(Arrays.asList(definitions));
		}
	}

	/**
	 * @return the fsn
	 */
	public String getFsn()
	{
		return fsn_;
	}

	/**
	 * @return the preferredSynonyms_
	 */
	public List<String> getSynonyms()
	{
		return synonyms_;
	}

	/**
	 * @return the definitions
	 */
	public List<String> getDefinitions()
	{
		return definitions_;
	}
}
