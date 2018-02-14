/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

package sh.isaac.convert.mojo.hl7v3.propertyTypes;

import java.util.UUID;
import sh.isaac.MetaData;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.convert.mojo.hl7v3.HL7v3Constants;
import sh.isaac.converters.sharedUtils.IBDFCreationUtility;
import sh.isaac.converters.sharedUtils.propertyTypes.BPT_Annotations;
import sh.isaac.converters.sharedUtils.propertyTypes.Property;

public class PT_Annotations extends BPT_Annotations
{
	// These hacks are here, because maven is broken, and can't scan perfectly valid java code when this is inside an enumeration constructor.
	// sigh.
	private static String[] historyHackColNames = new String[] { "datetime", "responsible person name", "id", "is substantive change",
			"is backward compatible change", "description" };
	private static DynamicDataType[] historyHackColTypes = new DynamicDataType[] { DynamicDataType.STRING, DynamicDataType.STRING, DynamicDataType.STRING,
			DynamicDataType.BOOLEAN, DynamicDataType.BOOLEAN, DynamicDataType.STRING };

	private static String[] releasedVersionColNames = new String[] { "release date", "publisher version", "hl7 maintained indicator",
			"complete codes indicator", "hl7 approved indicator" };
	private static DynamicDataType[] releasedVersionColTypes = new DynamicDataType[] { DynamicDataType.STRING, DynamicDataType.STRING, DynamicDataType.BOOLEAN,
			DynamicDataType.BOOLEAN, DynamicDataType.BOOLEAN };

	private static String[] supportedConceptPropertyColNames = new String[] { "property name", "type", "is mandatory indicator",
			"is apply to value sets indicator", "default value" };
	private static DynamicDataType[] supportedConceptPropertyColTypes = new DynamicDataType[] { DynamicDataType.NID, DynamicDataType.NID,
			DynamicDataType.BOOLEAN, DynamicDataType.BOOLEAN, DynamicDataType.STRING };

	public enum Attribute
	{
		OID("OID", "HL7 Object Identifier", true), Code(MetaData.CODE____SOLOR, true),

		VOCABULARY_MODEL("vocabulary model", "name", "title", "package kind", "definition kind", "schema version"),
		// The [] is a stupid hack I put in, to allow me to specify array of strings, rather than array, when setting up the dynamic sememe.
		PACKAGE_LOCATION("package location", "combined id", "root", "artifact", "[]realm namespace", "version"),
		RENDERING_INFORMATION("rendering information", "rendering time", "appliation"), LEGALESE("legalese", "copyright owner", "[]copyright years"),
		HISTORY_ITEM("history item", historyHackColNames, historyHackColTypes),

		// some items for associations
		IS_NAVIGABLE("is navigable", DynamicDataType.BOOLEAN), REFLEXIVITY("reflexivity", DynamicDataType.NID), SYMMETRY("symmertry", DynamicDataType.NID),
		TRANSITIVITY("transitivity", DynamicDataType.NID), RELATIONSHIP_KIND("relationship kind", DynamicDataType.NID),

		RELEASED_VERSION("released version", releasedVersionColNames, releasedVersionColTypes),
		SUPPORTED_CONCEPT_RELATIONSHIP("supported concept relationship", DynamicDataType.NID),
		SUPPORTED_CONCEPT_PROPERTY("supported concept property", supportedConceptPropertyColNames, supportedConceptPropertyColTypes),

		// property type enum values
		SUPPORTED_CONCEPT_PROPERTY_TYPE("supported concept property type"), PROPERTY_DEFAULT_HANDLING_KIND("property default handling kind"),

		IS_SELECTABLE("is selectable", DynamicDataType.BOOLEAN);

		private Property property_;
		private String[] orderedColumnNames_ = null;
		private DynamicDataType[] orderedColumnTypes_ = null;
		private String niceName_ = null;

		private Attribute(String niceName, String... columnLabels)
		{
			// must build these later
			property_ = null;
			orderedColumnNames_ = columnLabels;
			niceName_ = niceName;
		}

		private Attribute(String niceName, DynamicDataType type)
		{
			// must build these later
			property_ = null;
			niceName_ = niceName;
			orderedColumnTypes_ = new DynamicDataType[] { type };
		}

		private Attribute(String niceName, String[] columnLabels, DynamicDataType[] columnTypes)
		{
			// must build these later
			property_ = null;
			orderedColumnNames_ = columnLabels;
			orderedColumnTypes_ = columnTypes;
			niceName_ = niceName;
		}

		private Attribute(String niceName)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below during addProperty.
			property_ = new Property(null, niceName);
			niceName_ = niceName;
		}

		private Attribute(ConceptSpecification cs, boolean isIdentifier)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below, below during addProperty.
			property_ = new Property(null, cs, isIdentifier);
			niceName_ = cs.getRegularName().get();
		}

		private Attribute(String fsn, String altName, boolean isIdentifier)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below, below during addProperty.
			property_ = new Property(fsn, altName, (String) null, isIdentifier);
			niceName_ = fsn;
		}

		public Property getProperty()
		{
			return property_;
		}

		public UUID getUUID()
		{
			return property_.getUUID();
		}
	}

	public PT_Annotations(IBDFCreationUtility importUtil)
	{
		super(HL7v3Constants.TERMINOLOGY_NAME);
		for (Attribute attr : Attribute.values())
		{
			if (attr.getProperty() == null)
			{
				if (attr.orderedColumnNames_ != null)
				{
					attr.property_ = importUtil.createMultiColumnDynamicStringSememe(attr.niceName_, attr.orderedColumnNames_, attr.orderedColumnTypes_);
				}
				else if (attr.orderedColumnTypes_ != null && attr.orderedColumnTypes_.length == 1)
				{
					DynamicColumnInfo[] cols = new DynamicColumnInfo[1];
					cols[0] = new DynamicColumnInfo(0, DynamicConstants.get().DYNAMIC_COLUMN_VALUE.getPrimordialUuid(), attr.orderedColumnTypes_[0], null, true,
							true);
					attr.property_ = new Property(null, attr.niceName_, null, null, false, Integer.MAX_VALUE, cols);
				}
			}
			addProperty(attr.getProperty());
		}
	}
}
