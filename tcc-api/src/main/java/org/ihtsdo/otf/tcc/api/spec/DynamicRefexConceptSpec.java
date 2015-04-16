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

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.metadata.ComponentType;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.refexDynamic.data.RefexDynamicColumnInfo;

/**
 * {@link DynamicRefexConceptSpec}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class DynamicRefexConceptSpec extends ConceptSpecWithDescriptions
{
	private static final long serialVersionUID = 1L;
	private boolean annotationStyle_;
	private String refexDescription_;
	private RefexDynamicColumnInfo[] refexColumns_;
	private ComponentType referencedComponentRestriction_;
	private Integer[] requiresIndex_;

	/**
	 * 
	 * @param fsn - Used as the fsn and preferred synonym
	 * @param uuid
	 * @param annotationStyle
	 * @param refexDescription
	 * @param columns
	 * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
	 */
	public DynamicRefexConceptSpec(String fsn, UUID uuid, boolean annotationStyle, String refexDescription,
			RefexDynamicColumnInfo[] columns, ConceptSpec parentConcept)
	{
		this(fsn, uuid, new String[] {fsn}, null, annotationStyle, refexDescription, columns, parentConcept);
	}
	
	/**
	 * 
	 * @param fsn - Used as the fsn and preferred synonym
	 * @param uuid
	 * @param annotationStyle
	 * @param refexDescription
	 * @param columns
	 * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
	 * @param requiresIndex - optional - used to specify that this particular DynamicRefex should always be indexed.  If null - no indexing will 
	 * be performed.  If passed as an empty set, then the refex WILL be indexed - but no columns of the refex will be indexed.  Otherwise, the Integer 
	 * array should be something like "new Integer[]{0, 2, 3}" - where the 0 indexed values correspond to the columns that should also be indexed.
	 */
	public DynamicRefexConceptSpec(String fsn, UUID uuid, boolean annotationStyle, String refexDescription,
			RefexDynamicColumnInfo[] columns, ConceptSpec parentConcept, Integer[] requiresIndex)
	{
		this(fsn, uuid, new String[] {fsn}, null, annotationStyle, refexDescription, columns, parentConcept, null, requiresIndex);
	}
	
	/**
	 * @param fsn
	 * @param uuid
	 * @param synonyms
	 * @param definitions
	 * @param annotationStyle - true to build this as an annotation style, false for member style
	 * @param refexDescription - describe the purpose of the use of this refex
	 * @param columns - The definitions of the attached data columns that are allowed on this refex
	 * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
	 */
	public DynamicRefexConceptSpec(String fsn, UUID uuid, String[] synonyms, String[] definitions, boolean annotationStyle, String refexDescription,
			RefexDynamicColumnInfo[] columns, ConceptSpec parentConcept)
	{
		this(fsn, uuid, synonyms, definitions, annotationStyle, refexDescription, columns, parentConcept, null, null);
	}
	
	/**
	 * @param fsn
	 * @param uuid
	 * @param synonyms
	 * @param definitions
	 * @param annotationStyle - true to build this as an annotation style, false for member style
	 * @param refexDescription - describe the purpose of the use of this refex
	 * @param columns - The definitions of the attached data columns that are allowed on this refex
	 * @param parentConcept - used as the destination in a relspec, with a type of {@link Snomed#IS_A} and a source of this spec being created.
	 * @param referencedComponentRestriction - optional - used to limit the type of nid that can be used as the referenced component in an instance
	 * of this sememe.
	 * @param requiresIndex - optional - used to specify that this particular DynamicRefex should always be indexed.  If null - no indexing will 
	 * be performed.  If passed as an empty set, then the refex WILL be indexed - but no columns of the refex will be indexed.  Otherwise, the Integer 
	 * array should be something like "new Integer[]{0, 2, 3}" - where the 0 indexed values correspond to the columns that should also be indexed.
	 */
	public DynamicRefexConceptSpec(String fsn, UUID uuid, String[] synonyms, String[] definitions, boolean annotationStyle, String refexDescription,
			RefexDynamicColumnInfo[] columns, ConceptSpec parentConcept, ComponentType referencedComponentRestriction, Integer[] requiresIndex)
	{
		super(fsn, uuid, synonyms, definitions, parentConcept);
		annotationStyle_ = annotationStyle;
		refexDescription_ = refexDescription;
		refexColumns_ = columns;
		referencedComponentRestriction_ = referencedComponentRestriction;
		requiresIndex_ = requiresIndex;
	}

	/**
	 * @return the annotationStyle_
	 */
	public boolean isAnnotationStyle()
	{
		return annotationStyle_;
	}

	/**
	 * @return the refexDescription_
	 */
	public String getRefexDescription()
	{
		return refexDescription_;
	}

	/**
	 * @return the refexColumns_
	 */
	public RefexDynamicColumnInfo[] getRefexColumns()
	{
		return refexColumns_;
	}
	
	/**
	 * @return The limit (if any) on which {@link ComponentType} this refex is restricted to.
	 */
	public ComponentType getReferencedComponentTypeRestriction()
	{
		return referencedComponentRestriction_;
	}
	
	/**
	 * @return null if no index is required, an empty set if an index is required (but no columns are required) otherwise an array 
	 * that describes which columns should be indexed
	 */
	public Integer[] getRequiredIndexes()
	{
		return requiresIndex_;
	}
}
