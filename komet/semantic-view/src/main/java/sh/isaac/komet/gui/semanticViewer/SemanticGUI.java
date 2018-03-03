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
package sh.isaac.komet.gui.semanticViewer;

import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.Status;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicByteArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicDouble;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicFloat;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicInteger;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicLong;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.util.AlphanumComparator;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.utility.Frills;
import sh.isaac.utility.NumberUtilities;

/**
 * {@link SemanticGUI}
 * 
 * A Wrapper for a DynamicSememeVersionBI - because the versioned refex provides no information
 * about whether or not it is an old version, or if it is the latest version.  Add a flag for 
 * is latest.
 * 
 * Also used in cases where we are constructing a new Refex - up front, we know a NID (which is either the assemblyNid or 
 * the referenced component nid.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class SemanticGUI
{
	private static Logger logger_ = LogManager.getLogger(SemanticGUI.class);
	
	//These variables are used when we are working with a refex that already exists
	private SemanticVersion refex_;
	private boolean isCurrent_;
	private HashMap<String, AbstractMap.SimpleImmutableEntry<String, String>> stringCache_ = new HashMap<>();
	
	//These variables are used when we are creating a new refex which doesn't yet exist.
	private Integer buildFromReferenceNid_;
	private boolean referenceIsAssemblyNid_;
	
	protected SemanticGUI(SemanticVersion refex, boolean isCurrent)
	{
		refex_ = refex;
		isCurrent_ = isCurrent;
	}
	
	protected SemanticGUI(int buildFromReferenceNid, boolean referenceIsAssemblyNid)
	{
		refex_ = null;
		isCurrent_ = false;
		buildFromReferenceNid_ = buildFromReferenceNid;
		referenceIsAssemblyNid_ = referenceIsAssemblyNid;
	}

	/**
	 * Contains the refex reference when this object was constructed based on an existing refex
	 * @return the semantic
	 */
	public SemanticVersion getSememe()
	{
		return refex_;
	}

	/**
	 * If this was constructed based off of an existing refex, is this the most current refex?  Or a historical one?
	 * This is meaningless if {@link #getSememe()} return null.
	 * @return true if current
	 */
	public boolean isCurrent()
	{
		return isCurrent_;
	}

	/**
	 * If this was constructed with just a nid (building a new refex from scratch) this returns it - otherwise, returns null.
	 * @return the nid, or null
	 */
	public Integer getBuildFromReferenceNid()
	{
		return buildFromReferenceNid_;
	}

	/**
	 * @return If this was constructed with just a nid - this returns true of the nid is pointing to an assemblage concept - false if it is
	 * pointing to a component reference.  The value is meaningless if {@link #getBuildFromReferenceNid()} returns null.
	 */
	public boolean getReferenceIsAssemblyNid()
	{
		return referenceIsAssemblyNid_;
	}
	
	/**
	 * For cases when it was built from an existing refex only
	 * @param columnTypeToCompare 
	 * @param attachedDataColumn - optional - ignored (can be null) except applicable to {@link SemanticGUIColumnType#ATTACHED_DATA}
	 * @param other 
	 * @return negative or positive for sorting purposes
	 */
	public int compareTo(SemanticGUIColumnType columnTypeToCompare, Integer attachedDataColumn, SemanticGUI other)
	{
		switch (columnTypeToCompare)
		{
			case STATUS_CONDENSED:
			{
				//sort by uncommitted first, then current / historical, then active / inactive
				if (this.getSememe().getTime() == Long.MAX_VALUE)
				{
					return -1;
				}
				else if (other.getSememe().getTime() == Long.MAX_VALUE)
				{
					return 1;
				}
				
				if (this.isCurrent() && !other.isCurrent())
				{
					return -1;
				}
				else if (!this.isCurrent() && other.isCurrent())
				{
					return 1;
				}
				
				if (this.getSememe().getStatus() == Status.ACTIVE && other.getSememe().getStatus() == Status.INACTIVE)
				{
					return -1;
				}
				else if (this.getSememe().getStatus() == Status.INACTIVE && other.getSememe().getStatus() == Status.ACTIVE)
				{
					return 1;
				}
				return 0;
			}
			case TIME:
			{
				if (this.getSememe().getTime() < other.getSememe().getTime())
				{
					return -1;
				}
				else if (this.getSememe().getTime() > other.getSememe().getTime())
				{
					return -1;
				}
				else
				{
					return 0;
				}
			}
			case COMPONENT: case ASSEMBLAGE: case STATUS_STRING: case AUTHOR: case MODULE: case PATH: case UUID:
			{
				String myString = this.getDisplayStrings(columnTypeToCompare, null).getKey();
				String otherString = other.getDisplayStrings(columnTypeToCompare, null).getKey();
				return AlphanumComparator.compare(myString, otherString, true);
			}
			case ATTACHED_DATA:
			{
				if (attachedDataColumn == null)
				{
					throw new RuntimeException("API misuse");
				}
				DynamicData myData = getData(this.refex_).length > attachedDataColumn ? getData(this.refex_)[attachedDataColumn] : null;
				DynamicData otherData = getData(other.refex_).length > attachedDataColumn ? getData(other.refex_)[attachedDataColumn] : null;
				
				if (myData == null && otherData != null)
				{
					return -1;
				}
				else if (myData != null && otherData == null)
				{
					return 1;
				}
				else if (myData == null && otherData == null)
				{
					return 0;
				}
				else if (myData instanceof DynamicFloat && otherData instanceof DynamicFloat)
				{
					return NumberUtilities.compare(((DynamicFloat) myData).getDataFloat(), ((DynamicFloat) otherData).getDataFloat());
				}
				else if (myData instanceof DynamicDouble && otherData instanceof DynamicDouble) 
				{
					return NumberUtilities.compare(((DynamicDouble) myData).getDataDouble(), ((DynamicDouble) otherData).getDataDouble());
				}
				else if (myData instanceof DynamicInteger && otherData instanceof DynamicInteger) 
				{
					return NumberUtilities.compare(((DynamicInteger) myData).getDataInteger(), ((DynamicInteger) otherData).getDataInteger());
				}
				else if (myData instanceof DynamicLong && otherData instanceof DynamicLong)
				{
					return NumberUtilities.compare(((DynamicLong) myData).getDataLong(), ((DynamicLong) otherData).getDataLong());
				}
				else
				{
					String myString = this.getDisplayStrings(columnTypeToCompare, attachedDataColumn).getKey();
					String otherString = other.getDisplayStrings(columnTypeToCompare, attachedDataColumn).getKey();
					return AlphanumComparator.compare(myString, otherString, true);
				}
			}

			default:
				throw new RuntimeException("Missing implementation: " + columnTypeToCompare);
		}
	}
	
	/**
	 * @param desiredColumn 
	 * @param attachedDataColumn should be null for most types - applicable to {@link SemanticGUIColumnType#ATTACHED_DATA}
	 * @return Returns the string for display, and the tooltip, if applicable.  Either / or may be null.
	 * Key is for the display, value is for the tooltip.
	 */
	public AbstractMap.SimpleImmutableEntry<String, String> getDisplayStrings(SemanticGUIColumnType desiredColumn, Integer attachedDataColumn)
	{
		String cacheKey = desiredColumn.name() + attachedDataColumn;  //null is ok on the attachedDataColumn...
		
		AbstractMap.SimpleImmutableEntry<String, String> returnValue = stringCache_.get(cacheKey);
		if (returnValue != null)
		{
			return returnValue;
		}
		
		switch (desiredColumn)
		{
			case STATUS_CONDENSED:
			{
				//Just easier to leave the impl in StatusCell for this one.  We don't need filters on this column either.
				throw new RuntimeException("No text for this field");
			}
			case COMPONENT: case ASSEMBLAGE: case AUTHOR: case PATH: case MODULE:
			{
				String text = getComponentText(getNidFetcher(desiredColumn, attachedDataColumn));
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(text, text);
				break;
			}
			case UUID:
			{
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(refex_.getPrimordialUuid().toString(), "");
				break;
			}
			case STATUS_STRING:
			{
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(refex_.getStatus().toString(), null);
				break;
			}
			case TIME:
			{
				returnValue = new AbstractMap.SimpleImmutableEntry<String, String>((refex_.getTime() == Long.MAX_VALUE ? "-Uncommitted-" : 
					new Date(refex_.getTime()).toString()), null);
				break;
			}
			case ATTACHED_DATA:
			{
				if (attachedDataColumn == null)
				{
					throw new RuntimeException("API misuse");
				}
				DynamicData data = getData(this.refex_).length > attachedDataColumn ? getData(this.refex_)[attachedDataColumn] : null;
				if (data != null)
				{
					if (data instanceof DynamicByteArray)
					{
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("[Binary]", null);
					}
					else if (data instanceof DynamicNid)
					{
						String desc = getComponentText(((DynamicNid)data).getDataNid());
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(desc, data.getDataObject().toString());
					}
					else if (data instanceof DynamicUUID)
					{
						String desc;
						if (Get.identifierService().hasUuid(((DynamicUUID)data).getDataUUID()))
						{
							desc = getComponentText(Get.identifierService().getNidForUuids(((DynamicUUID)data).getDataUUID()));
						}
						else
						{
							desc = ((DynamicUUID)data).getDataUUID() + "";
						}
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(desc, data.getDataObject().toString());
					}
					else if (data instanceof DynamicArray<?>)
					{
						DynamicArray<?> instanceData = (DynamicArray<?>)data;
						StringBuilder sb = new StringBuilder();
						sb.append("[");
						
						for (DynamicData dsd : instanceData.getDataArray())
						{
							switch (dsd.getDynamicDataType())
							{
								case ARRAY:
									//Could recurse... but I can't imagine a use case at the moment.
									sb.append("[Nested Array], ");
									break;
								case STRING: case BOOLEAN: case DOUBLE: case FLOAT: case INTEGER: case LONG: case NID: case UUID:
								{
									//NID and UUID could be turned into strings... but, unusual use case... leave like this for now.
									sb.append(dsd.getDataObject().toString());
									sb.append(", ");
									break;
								}
								
								case BYTEARRAY:
									sb.append("[Binary of size " + dsd.getData().length + "], ");
									break;
								case UNKNOWN: case POLYMORPHIC:
								{
									//shouldn't happen - but just do the toString
									sb.append("Unknown Type, ");
									break;
								}
								
								default:
									sb.append("Unhandled case: " + dsd.getDynamicDataType() + ", ");
									logger_.warn("Missing toString case!");
									break;
							}
						}
						
						if (sb.length() > 1)
						{
							sb.setLength(sb.length() - 2);
						}
						sb.append("]");
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(sb.toString(), "Array of " 
								+ instanceData.getDataArray().length + " items: " + sb.toString());
					}
					else
					{
						returnValue = new AbstractMap.SimpleImmutableEntry<String, String>(data.getDataObject().toString(), data.getDataObject().toString());
					}
				}
				else
				{
					returnValue = new AbstractMap.SimpleImmutableEntry<String, String>("", null);
				}
				break;
			}

			default:
				throw new RuntimeException("Missing implementation: " + desiredColumn);
		}
		
		stringCache_.put(cacheKey, returnValue);
		return returnValue;
		
	}
	
	private String getComponentText(ToIntFunction<SemanticVersion> nidFetcher)
	{
		return getComponentText(nidFetcher.applyAsInt(this.refex_));
	}
	
	private String getComponentText(int nid)
	{
		String text;
		
		try
		{
			//This may be a different component - like a description, or another refex... need to handle.
			Optional<? extends Chronology> oc = Get.identifiedObjectService().getChronology(nid);
			if (!oc.isPresent())
			{
				text = "[NID] " + nid + " not on path";
			}
			else if (oc.get() instanceof ConceptChronology)
			{
				Optional<String> conDesc = Frills.getDescription(oc.get().getNid(), StampCoordinates.getDevelopmentLatest(), null);
				text = (conDesc.isPresent() ? conDesc.get() : "off path [NID]:" + oc.get().getNid());
			}
			else if (oc.get() instanceof SemanticChronology)
			{
				SemanticChronology sc = (SemanticChronology)oc.get();
				switch (sc.getVersionType()) {
					case COMPONENT_NID:
						text = "Component NID Sememe using assemblage: " + Frills.getDescription(sc.getAssemblageNid(), null);
						break;
					case DESCRIPTION:
						LatestVersion<DescriptionVersion> ds = sc.getLatestVersion(StampCoordinates.getDevelopmentLatest());
						text = "Description Sememe: " + (ds.isPresent() ? ds.get().getText() : "off path [NID]: " + sc.getNid());
						break;
					case DYNAMIC:
						text = "Dynamic Sememe using assemblage: " + Frills.getDescription(sc.getAssemblageNid(), null);
						break;
					case LOGIC_GRAPH:
						text = "Logic Graph Sememe [NID]: " + oc.get().getNid();
						break;
					case LONG:
						LatestVersion<LongVersion> sl = sc.getLatestVersion(StampCoordinates.getDevelopmentLatest());
						text = "String Sememe: " + (sl.isPresent() ? sl.get().getLongValue() : "off path [NID]: " + sc.getNid());
						break;
					case MEMBER:
						text = "Member Sememe using assemblage: " + Frills.getDescription(sc.getAssemblageNid(), null);
						break;
					case STRING:
						LatestVersion<StringVersion> ss = sc.getLatestVersion(StampCoordinates.getDevelopmentLatest());
						text = "String Sememe: " + (ss.isPresent() ? ss.get().getString() : "off path [NID]: " + sc.getNid());
						break;
					case UNKNOWN:
					default :
						logger_.warn("The sememe type " + sc.getVersionType() + " is not handled yet!");
						//TODO should handle other types of common sememes
						text = oc.get().toUserString();
						break;
				}
			}
			else if (oc.get() instanceof DynamicVersion<?>)
			{
				DynamicVersion<?> nds = (DynamicVersion<?>) oc.get();
				text = "Nested Sememe Dynamic: using assemblage " + Frills.getDescription(nds.getAssemblageNid(), null);
			}
			else
			{
				logger_.warn("The component type " + oc.get().getClass() + " is not handled yet!");
				//TODO should handle other types of common sememes
				text = oc.get().toUserString();
			}
		}
		catch (Exception e)
		{
			logger_.error("Unexpected error", e);
			text = "-ERROR-";
		}
		return text;
	}
	
	/**
	 * @param desiredColumn 
	 * @param attachedDataColumn null for most types - applicable to {@link SemanticGUIColumnType#ATTACHED_DATA}
	 * @return the nid for the column
	 */
	public ToIntFunction<SemanticVersion> getNidFetcher(SemanticGUIColumnType desiredColumn, Integer attachedDataColumn)
	{
		switch (desiredColumn)
		{
			case STATUS_CONDENSED:
			{
				throw new RuntimeException("Improper API usage");
			}
			case COMPONENT:
			{
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						return refex_.getReferencedComponentNid();
					}
				};
			}
			case ASSEMBLAGE:
			{
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						return refex_.getAssemblageNid();
					}
				};
			}
			case AUTHOR:
			{
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						return refex_.getAuthorNid();
					}
				};
			}
			case MODULE:
			{
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						return refex_.getModuleNid();
					}
				};
			}
			case PATH:
			{
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						return refex_.getPathNid();
					}
				};
			}
			
			case ATTACHED_DATA:
			{
				if (attachedDataColumn == null)
				{
					throw new RuntimeException("API misuse");
				}
				return new ToIntFunction<SemanticVersion>()
				{
					@Override
					public int applyAsInt(SemanticVersion value)
					{
						DynamicData data = getData(refex_).length > attachedDataColumn ? getData(refex_)[attachedDataColumn] : null;
						if (data != null)
						{
							if (data instanceof DynamicNid)
							{
								return ((DynamicNid)data).getDataNid();
							}
							else if (data instanceof DynamicUUID)
							{
								if (Get.identifierService().hasUuid(((DynamicUUID)data).getDataUUID()))
								{
									return Get.identifierService().getNidForUuids(((DynamicUUID)data).getDataUUID());
								}
							}
						}
						return 0;
					}
				};
				
			}

			default:
				throw new RuntimeException("Missing implementation: " + desiredColumn);
		}
	}
	
	/**
	 * A method to read the data from a sememe of an arbitrary type, mocking up static sememes as dynamic sememems, if necessary
	 * @param sememe
	 * @return the data in a Dynamic Container
	 */
	public static DynamicData[] getData(SemanticVersion sememe)
	{
		switch (sememe.getChronology().getVersionType())
		{
			case COMPONENT_NID:
				return new DynamicData[] {new DynamicNidImpl(((ComponentNidVersion)sememe).getComponentNid())};
			case DESCRIPTION:
				return new DynamicData[] {new DynamicStringImpl(((DescriptionVersion)sememe).getText())};
			case DYNAMIC:
				return ((DynamicVersion<?>)sememe).getData();
			case LONG:
				return new DynamicData[] {new DynamicLongImpl(((LongVersion)sememe).getLongValue())};
			case MEMBER:
				return new DynamicData[] {};
			case STRING:
				return new DynamicData[] {new DynamicStringImpl(((StringVersion)sememe).getString())};
			case LOGIC_GRAPH:
				return new DynamicData[] {new DynamicStringImpl(((LogicGraphVersion)sememe).toString())};
			case UNKNOWN:
			case CONCEPT:
			//TODO if these things stick around, we should map them to dynamics per above.... but I hope they go away....
			case Int1_Int2_Str3_Str4_Str5_Nid6_Nid7:
			case LOINC_RECORD:
			case MEASURE_CONSTRAINTS:
			case Nid1_Int2:
			case Nid1_Int2_Str3_Str4_Nid5_Nid6:
			case Nid1_Nid2:
			case Nid1_Nid2_Int3:
			case Nid1_Nid2_Str3:
			case Nid1_Str2:
			case RF2_RELATIONSHIP:
			case Str1_Nid2_Nid3_Nid4:
			case Str1_Str2:
			case Str1_Str2_Nid3_Nid4:
			case Str1_Str2_Nid3_Nid4_Nid5:
			case Str1_Str2_Str3_Str4_Str5_Str6_Str7:
			default :
				throw new UnsupportedOperationException();

		}
			
	}
}
