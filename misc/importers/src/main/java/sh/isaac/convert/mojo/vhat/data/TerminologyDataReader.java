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

package sh.isaac.convert.mojo.vhat.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import sh.isaac.convert.mojo.vhat.data.dto.CodeSystem;
import sh.isaac.convert.mojo.vhat.data.dto.ConceptImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationExtendedImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.DesignationImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapEntryImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.MapSetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.PropertyImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.RelationshipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.SubsetMembershipImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TerminologyDTO;
import sh.isaac.convert.mojo.vhat.data.dto.TypeImportDTO;
import sh.isaac.convert.mojo.vhat.data.dto.Version;


public class TerminologyDataReader extends DefaultHandler
{
	private static final String TERMINOLOGY_ELEMENT = "Terminology";
	private static final String CODESYSTEM_ELEMENT = "CodeSystem";
	private static final String VERSION_ELEMENT = "Version";
	private static final String CODEDCONCEPT_ELEMENT = "CodedConcept";
	private static final String CODEDCONCEPTS_ELEMENT = "CodedConcepts";
	private static final String MAPSET_ELEMENT = "MapSet";
	private static final String MAPSETS_ELEMENT = "MapSets";
	private static final String MAPENTRY_ELEMENT = "MapEntry";
	private static final String MAPENTRIES_ELEMENT = "MapEntries";
	
	private static final String DESIGNATION_ELEMENT = "Designation";
	private static final String PROPERTY_ELEMENT = "Property";
	private static final String RELATIONSHIP_ELEMENT = "Relationship";
	private static final String TYPE_ELEMENT = "Type";
	private static final String TYPES_ELEMENT = "Types";
	private static final String SUBSET_MEMBERSHIP_ELEMENT = "SubsetMembership";
	private static final String SUBSET_MEMBERSHIPS_ELEMENT = "SubsetMemberships";
	private static final String MOVE_FROM_CONCEPT_CODE_ELEMENT = "MoveFromConceptCode";

	private static final String DESIGNATIONS_ELEMENT = "Designations";
	private static final String PROPERTIES_ELEMENT = "Properties";
	private static final String RELATIONSHIPS_ELEMENT = "Relationships";
	
	private static final String SUBSET_ELEMENT = "Subset";
	private static final String SUBSETS_ELEMENT = "Subsets";

	private static final String CODE_ELEMENT = "Code";
	private static final String NAME_ELEMENT = "Name";
	private static final String VUID_ELEMENT = "VUID";
	
	private static final String DESCRIPTION_ELEMENT = "Description";
	private static final String COPYRIGHT_ELEMENT = "Copyright";
	private static final String COPYRIGHT_URL_ELEMENT = "CopyrightURL";
	
	private static final String EFFECTIVE_DATE_ELEMENT = "EffectiveDate";
	private static final String RELEASE_DATE_ELEMENT = "ReleaseDate";
	private static final String SOURCE_ELEMENT = "Source";
	private static final String APPEND_ELEMENT = "Append";	
	private static final String ACTIVE_ELEMENT = "Active";

	private static final String TYPE_NAME_ELEMENT = "TypeName";
	private static final String VALUE_NEW_ELEMENT = "ValueNew";
	private static final String VALUE_OLD_ELEMENT = "ValueOld";

	private static final String SOURCE_CODE_ELEMENT = "SourceCode";
	private static final String TARGET_CODE_ELEMENT = "TargetCode";
	private static final String SEQUENCE_ELEMENT = "Sequence";
	private static final String GROUPING_ELEMENT = "Grouping";

	private static final String NEW_TARGETCODE_ELEMENT = "NewTargetCode";
	private static final String OLD_TARGETCODE_ELEMENT = "OldTargetCode";

	private static final String SOURCE_CODE_SYSTEM = "SourceCodeSystem";
	private static final String SOURCE_VERSION_NAME = "SourceVersionName";
	private static final String TARGET_CODE_SYSTEM = "TargetCodeSystem";
	private static final String TARGET_VERSION_NAME = "TargetVersionName";
	
	private static final String PREFERRED_DESIGNATION_TYPE_ELEMENT = "PreferredDesignationType";
	private static final String KIND_ELEMENT = "Kind";
	
	private static final String ACTION_ELEMENT = "Action";
//	private static final String ACTION_ADD = "add";
//	private static final String ACTION_UPDATE = "update";
//	private static final String ACTION_NONE = "none";
//	private static final String ACTION_REMOVE = "remove";

	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Stack<HierarchyData> elementDataStack = new Stack<>();
	
	private File inputFile;
	private String schemaName;
	
	private TerminologyDTO terminology = new TerminologyDTO();

	public TerminologyDataReader(File inputFile)
	{
		this.inputFile = inputFile;
		this.schemaName = "/TerminologyData.xsd";
	}

	public TerminologyDTO process() throws Exception 
	{
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		
		try
		{
			if (schemaName != null)
			{
				URL url = this.getClass().getResource(this.schemaName);
				if (url == null)
				{
					throw new FileNotFoundException("Unable to locate file: " + schemaName);
				}

  			
				SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);	  
				Schema schema = factory.newSchema(url);	  
				Validator validator = schema.newValidator();	  

				SAXParser parser = parserFactory.newSAXParser();
				
				if (inputFile.isDirectory())
				{
					ArrayList<File> files = new ArrayList<File>();
					for (File f : inputFile.listFiles())
					{
						if (f.isFile() && f.getName().toLowerCase().endsWith(".xml"))
						{
							files.add(f);
						}
					}
					
					if (files.size() != 1)
					{
						throw new Exception(files.size() + " xml files were found inside of " + inputFile.getAbsolutePath() 
								+ " but this implementation requires 1 and only 1 xml files to be present.");
					}
					
					System.out.println("Processing: " + files.get(0).getAbsolutePath());
					validator.validate(new StreamSource(new FileInputStream(files.get(0))));
					parser.parse(files.get(0), this);
					
				}
				else
				{
					System.out.println("Processing: " + inputFile.getAbsolutePath());
					validator.validate(new StreamSource(new FileInputStream(inputFile)));
					parser.parse(inputFile, this);
				}
			}
		}
		catch (SAXParseException e)
		{
			throw new Exception("The import file did not validate against the Schema file: "
					+ schemaName + " at line " +  e.getLineNumber() + ", column " + e.getColumnNumber() + ". The error is: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			throw new Exception(e.getMessage(), e);
		}
		return terminology;

	}

	@Override
	public void startElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) throws SAXException
	{
		if (!ignoreElement(qualifiedName))
		{
			//We will make an object, after we gather the data.
			elementDataStack.push(new HierarchyData(qualifiedName));
		}
	}
	
	private boolean ignoreElement(String qualifiedName)
	{
		//ignore container elements
		return qualifiedName.equals(TYPES_ELEMENT) || qualifiedName.equals(SUBSETS_ELEMENT) || qualifiedName.equals(CODEDCONCEPTS_ELEMENT)
				|| qualifiedName.equals(DESIGNATIONS_ELEMENT) || qualifiedName.equals(RELATIONSHIPS_ELEMENT) || qualifiedName.equals(PROPERTIES_ELEMENT)
				|| qualifiedName.equals(SUBSET_MEMBERSHIPS_ELEMENT) || qualifiedName.equals(MAPSETS_ELEMENT) || qualifiedName.equals(MAPENTRIES_ELEMENT)
				|| qualifiedName.equals(TERMINOLOGY_ELEMENT);
	}

	@Override
	public void endElement(String namespaceUri, String localName, String qualifiedName) throws SAXException
	{
		if (!ignoreElement(qualifiedName))
		{
			try
			{
				if (qualifiedName.equals(TYPE_ELEMENT))
				{
					terminology.getTypes().add(readType());
				}
				else if (qualifiedName.equals(SUBSET_ELEMENT))
				{
					terminology.getSubsets().add(readSubset());
				}
				else if (qualifiedName.equals(CODESYSTEM_ELEMENT))
				{
					terminology.setCodeSystem(readCodeSystem());
				}
				else if (qualifiedName.equals(VERSION_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readVersion());
				}
				else if (qualifiedName.equals(CODEDCONCEPT_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readCodedConcept());
				}
				else if (qualifiedName.equals(DESIGNATION_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readDesignation());
				}
				else if (qualifiedName.equals(RELATIONSHIP_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readRelationship());
				}
				else if (qualifiedName.equals(PROPERTY_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readProperty());
				}
				else if (qualifiedName.equals(SUBSET_MEMBERSHIP_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readSubsetMembership());
				}
				else if (qualifiedName.equals(MAPSET_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readMapSet());
				}
				else if (qualifiedName.equals(MAPENTRY_ELEMENT))
				{
					elementDataStack.get(elementDataStack.size() - 2).createdChildObjects.add(readMapEntry());
				}
				else
				{
					elementDataStack.get(elementDataStack.size() - 2).makeAttribute(qualifiedName, elementDataStack.peek().elementDataBuffer.toString());
					elementDataStack.peek().elementDataBuffer.setLength(0);
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
			HierarchyData hd = elementDataStack.pop();
			hd.checkEmpty();
			if (!hd.elementName.equals(qualifiedName))
			{
				throw new RuntimeException("Pop misalignment!");
			}
		}
	}

	@Override
	public void characters(char[] chars, int startIndex, int endIndex)
	{
		String s = new String(chars, startIndex, endIndex);
		if (!elementDataStack.isEmpty())
		{
			elementDataStack.peek().elementDataBuffer.append(s);
		}
		else if (!StringUtils.isWhitespace(s))
		{
			throw new RuntimeException("Unhandled data! '" + s + "'");
		}
	}

	
	private Version readVersion() throws ParseException
	{
		Version version = new Version(
				elementDataStack.peek().attributes.remove(NAME_ELEMENT),
				elementDataStack.peek().attributes.remove(SOURCE_ELEMENT),
				elementDataStack.peek().attributes.remove(DESCRIPTION_ELEMENT),
				sdf.parse(elementDataStack.peek().attributes.remove(EFFECTIVE_DATE_ELEMENT)),
				sdf.parse(elementDataStack.peek().attributes.remove(RELEASE_DATE_ELEMENT)),
				parseBoolean(elementDataStack.peek().attributes.remove(APPEND_ELEMENT)));
		
		for (Object o : elementDataStack.peek().createdChildObjects)
		{
			if (o instanceof ConceptImportDTO)
			{
				version.getConcepts().add((ConceptImportDTO)o);
			}
			else if (o instanceof MapSetImportDTO)
			{
				version.getMapsets().add((MapSetImportDTO)o);
			}
			else
			{
				throw new RuntimeException("Unexpected child object type " + o);
			}
		}
		elementDataStack.peek().createdChildObjects.clear();
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return version;
	}
	

	private MapEntryImportDTO readMapEntry() throws ParseException
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		Long vuid = null;
		
		if (active == null)
		{
			active = Boolean.TRUE.toString();
		}
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}

		String sourceCode = elementDataStack.peek().attributes.remove(SOURCE_CODE_ELEMENT);
		String targetCode = elementDataStack.peek().attributes.remove(TARGET_CODE_ELEMENT);
		String sequenceString = elementDataStack.peek().attributes.remove(SEQUENCE_ELEMENT);
		String groupingString = elementDataStack.peek().attributes.remove(GROUPING_ELEMENT);
		String effectiveDateString = elementDataStack.peek().attributes.remove(EFFECTIVE_DATE_ELEMENT);
		Date effectiveDate = null;
		int sequence = 0;
		Long grouping = null;
		if (sequenceString != null)
		{
			sequence = Integer.valueOf(sequenceString);
		}
		if (groupingString != null)
		{
			grouping = Long.valueOf(groupingString);
		}
		if (StringUtils.isNotBlank(effectiveDateString))
		{
			effectiveDate = sdf.parse(effectiveDateString);
		}

		MapEntryImportDTO me = new MapEntryImportDTO(action, vuid, parseBoolean(active),sourceCode, targetCode, sequence, grouping, effectiveDate);
		
		for (Object o : elementDataStack.peek().createdChildObjects)
		{
			if (o instanceof PropertyImportDTO)
			{
				me.getProperties().add((PropertyImportDTO)o);
			}
			else if (o instanceof DesignationExtendedImportDTO)
			{
				me.getDesignations().add((DesignationExtendedImportDTO)o);
			}
			else if (o instanceof RelationshipImportDTO)
			{
				RelationshipImportDTO r = (RelationshipImportDTO)o;
				me.getRelationships().add(r);
			}
			else
			{
				throw new RuntimeException("Unexpected child object type " + o);
			}
		}
		elementDataStack.peek().createdChildObjects.clear();
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return me;
	}
	
	private DesignationImportDTO readDesignation()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String typeName = elementDataStack.peek().attributes.remove(TYPE_NAME_ELEMENT);
		String code = elementDataStack.peek().attributes.remove(CODE_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String valueOld = elementDataStack.peek().attributes.remove(VALUE_OLD_ELEMENT);
		String valueNew = elementDataStack.peek().attributes.remove(VALUE_NEW_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		String moveFromConceptCode = elementDataStack.peek().attributes.remove(MOVE_FROM_CONCEPT_CODE_ELEMENT);

		Long vuid = null;
		if (active == null)
		{
			// default of 'Active' element is true
			active = "true";
		}
		
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}
		
		DesignationImportDTO designationDTO;
		if (elementDataStack.get(elementDataStack.size() - 2).elementName.equals(CODEDCONCEPT_ELEMENT))
		{
			DesignationExtendedImportDTO temp = new DesignationExtendedImportDTO(action, typeName, code, valueOld, valueNew, vuid, moveFromConceptCode, parseBoolean(active));
			for (Object o : elementDataStack.peek().createdChildObjects)
			{
				if (o instanceof PropertyImportDTO)
				{
					temp.getProperties().add((PropertyImportDTO)o);
				}
				else if (o instanceof SubsetMembershipImportDTO)
				{
					temp.getSubsets().add((SubsetMembershipImportDTO)o);
				}
				else
				{
					throw new RuntimeException("Unexpected child type " + o);
				}
			}
			elementDataStack.peek().createdChildObjects.clear();
			designationDTO = temp;
		}
		else
		{
			designationDTO = new DesignationImportDTO(action, typeName, code, valueOld, valueNew, vuid, moveFromConceptCode, parseBoolean(active));
		}
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return designationDTO;
	}
	
	private ConceptImportDTO readCodedConcept()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String name = elementDataStack.peek().attributes.remove(NAME_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String code = elementDataStack.peek().attributes.remove(CODE_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		Long vuid = null;
		
		if (active == null)
		{
			active = Boolean.TRUE.toString();
		}
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}
		ConceptImportDTO ci = new ConceptImportDTO(action, name, code, vuid, parseBoolean(active));
		for (Object o : elementDataStack.peek().createdChildObjects)
		{
			if (o instanceof PropertyImportDTO)
			{
				ci.getProperties().add((PropertyImportDTO)o);
			}
			else if (o instanceof DesignationExtendedImportDTO)
			{
				ci.getDesignations().add((DesignationExtendedImportDTO)o);
			}
			else if (o instanceof RelationshipImportDTO)
			{
				ci.getRelationships().add((RelationshipImportDTO)o);
			}
			else
			{
				throw new RuntimeException("Unexpected child object type " + o);
			}
		}
		elementDataStack.peek().createdChildObjects.clear();
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return ci;
	}
	
	private CodeSystem readCodeSystem()
	{
		String codeSystemName = elementDataStack.peek().attributes.remove(NAME_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String description = elementDataStack.peek().attributes.remove(DESCRIPTION_ELEMENT);
		String copyright = elementDataStack.peek().attributes.remove(COPYRIGHT_ELEMENT);
		String copyrightURL = elementDataStack.peek().attributes.remove(COPYRIGHT_URL_ELEMENT);
		String preferredDesignationType = elementDataStack.peek().attributes.remove(PREFERRED_DESIGNATION_TYPE_ELEMENT);
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		
		Long vuid = null;
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}
		CodeSystem cs = new CodeSystem(codeSystemName, vuid, description, copyright, copyrightURL, preferredDesignationType, action);
		
		if (elementDataStack.peek().createdChildObjects.size() == 1)
		{
			cs.setVersion((Version)elementDataStack.peek().createdChildObjects.remove(0));
		}
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return cs;
	}
	
	private TypeImportDTO readType()
	{
		String name = elementDataStack.peek().attributes.remove(NAME_ELEMENT);
		String kind = elementDataStack.peek().attributes.remove(KIND_ELEMENT);
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return new TypeImportDTO(kind, name);
	}
	
	private SubsetImportDTO readSubset()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String subsetName = elementDataStack.peek().attributes.remove(NAME_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		Long vuid = null;
		
		if (active == null)
		{
			// default of 'Active' element is true
			active = "true";
		}
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return new SubsetImportDTO(action, vuid, subsetName, parseBoolean(active));
	}
	
	private PropertyImportDTO readProperty()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String typeName = elementDataStack.peek().attributes.remove(TYPE_NAME_ELEMENT);
		String valueOld = elementDataStack.peek().attributes.remove(VALUE_OLD_ELEMENT);
		String valueNew = elementDataStack.peek().attributes.remove(VALUE_NEW_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		if (active == null)
		{
			// default of 'Active' element is true
			active = "true";
		}
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return new PropertyImportDTO(action, typeName, valueOld, valueNew, parseBoolean(active));
	}

	private RelationshipImportDTO readRelationship()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String typeName = elementDataStack.peek().attributes.remove(TYPE_NAME_ELEMENT);
		String newTargetCode = elementDataStack.peek().attributes.remove(NEW_TARGETCODE_ELEMENT);
		String oldTargetCode = elementDataStack.peek().attributes.remove(OLD_TARGETCODE_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);

		if (active == null)
		{
			// default of 'Active' element is true
			active = "true";
		}
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return new RelationshipImportDTO(action, typeName, oldTargetCode, newTargetCode, parseBoolean(active));
	}

	private SubsetMembershipImportDTO readSubsetMembership()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return new SubsetMembershipImportDTO(action, Long.valueOf(vuidString), parseBoolean(active));
	}
	
	private MapSetImportDTO readMapSet()
	{
		String action = elementDataStack.peek().attributes.remove(ACTION_ELEMENT);
		String name = elementDataStack.peek().attributes.remove(NAME_ELEMENT);
		String vuidString = elementDataStack.peek().attributes.remove(VUID_ELEMENT);
		String code = elementDataStack.peek().attributes.remove(CODE_ELEMENT);
		String active = elementDataStack.peek().attributes.remove(ACTIVE_ELEMENT);
		String sourceCodeSystem = elementDataStack.peek().attributes.remove(SOURCE_CODE_SYSTEM);
		String sourceVersionName = elementDataStack.peek().attributes.remove(SOURCE_VERSION_NAME);
		String targetCodeSystem = elementDataStack.peek().attributes.remove(TARGET_CODE_SYSTEM);
		String targetVersionName = elementDataStack.peek().attributes.remove(TARGET_VERSION_NAME);
		
		Long vuid = null;
		
		if (active == null)
		{
			active = Boolean.TRUE.toString();
		}
		if (vuidString != null)
		{
			vuid = Long.valueOf(vuidString);
		}
		
		MapSetImportDTO ms = new MapSetImportDTO(action, name, code, vuid, parseBoolean(active), sourceCodeSystem, sourceVersionName, targetCodeSystem, targetVersionName);
		
		for (Object o : elementDataStack.peek().createdChildObjects)
		{
			if (o instanceof PropertyImportDTO)
			{
				ms.getProperties().add((PropertyImportDTO)o);
			}
			else if (o instanceof DesignationImportDTO)
			{
				ms.getDesignations().add((DesignationImportDTO)o);
			}
			else if (o instanceof RelationshipImportDTO)
			{
				ms.getRelationships().add((RelationshipImportDTO)o);
			}
			else if (o instanceof MapEntryImportDTO)
			{
				ms.getMapEntries().add((MapEntryImportDTO)o);
			}
			else
			{
				throw new RuntimeException("Unexpected child object type " + o);
			}
		}
		elementDataStack.peek().createdChildObjects.clear();
		elementDataStack.peek().elementDataBuffer.setLength(0);
		return ms;
	}
	
	private boolean parseBoolean(String value)
	{
		boolean result = false;
		if (value == null || value.equalsIgnoreCase("true") || value.equals("1"))
		{
			result = true;
		}
		
		return result;
	}
	
	private class HierarchyData
	{
		String elementName;
		StringBuffer elementDataBuffer = new StringBuffer();
		HashMap<String, String> attributes = new HashMap<>();
		List<Object> createdChildObjects = new ArrayList<>();
		
		HierarchyData(String elementName)
		{
			this.elementName = elementName;
		}

		public void checkEmpty()
		{
			if (elementDataBuffer.length() > 0 || attributes.size() > 0 || createdChildObjects.size() > 0)
			{
				throw new RuntimeException("Unhandled data in a " + toString());
			}
		}
		public void makeAttribute(String qualifiedName, String value)
		{
			attributes.put(qualifiedName, value);
		}

		@Override
		public String toString()
		{
			return "HierarchyData [elementName=" + elementName + ", elementDataBuffer=" + elementDataBuffer + ", attributes=" + attributes + "]";
		}
		
		
	}
}
