/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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
package sh.isaac.convert.directUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.RDFNode;
import javafx.util.Pair;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.api.externalizable.IsaacObjectType;
import sh.isaac.model.semantic.types.DynamicFloatImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;

/**
 * Some helper code for constructing DynamicSemantics from anonymous entries in RDF data sources.
 * 
 * TODO to be cleaned up a bit, and made more automated
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DynamicSemanticHelper
{
	private String niceName;
	private Function<Object, DynamicData[]> dataColumnBuilderFunction;
	private DynamicColumnInfo[] columnConstructionInfo;
	private IsaacObjectType referencedComponentTypeRestriction; 
	private VersionType referencedComponentTypeSubRestriction;
	private String[] nestedAnonKeys = null;
	private BiFunction<String, Object, String[]> nestedAnonRefsFetcher = null;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("Z"));
	
	public DynamicSemanticHelper(String niceName, Function<Object, DynamicData[]> dataColumnBuilderFunction, DynamicColumnInfo[] columnConstructionInfo,
			IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction)
	{
		this(niceName, dataColumnBuilderFunction, columnConstructionInfo, referencedComponentTypeRestriction, referencedComponentTypeSubRestriction, null, null);
	}
	
	public DynamicSemanticHelper(String niceName, Function<Object, DynamicData[]> dataColumnBuilderFunction, DynamicColumnInfo[] columnConstructionInfo,
			IsaacObjectType referencedComponentTypeRestriction, VersionType referencedComponentTypeSubRestriction, String[] nestedAnonKeys, 
			BiFunction<String, Object, String[]> nestedAnonRefs)
	{
		this.niceName = niceName;
		this.dataColumnBuilderFunction = dataColumnBuilderFunction;
		this.columnConstructionInfo = columnConstructionInfo;
		this.referencedComponentTypeRestriction = referencedComponentTypeRestriction;
		this.referencedComponentTypeSubRestriction = referencedComponentTypeSubRestriction;
		this.nestedAnonKeys = nestedAnonKeys;
		this.nestedAnonRefsFetcher = nestedAnonRefs;
	}
	
	public DynamicSemanticHelper(String niceName, RDFNode exampleData, UUID columnIdentifier, IsaacObjectType referencedComponentTypeRestriction, 
			VersionType referencedComponentTypeSubRestriction)
	{
		this.niceName = niceName;
		this.referencedComponentTypeRestriction = referencedComponentTypeRestriction;
		this.referencedComponentTypeSubRestriction = referencedComponentTypeSubRestriction;
		
		final DynamicDataType dataType;
		final Function<Object, DynamicData[]> conversionFunction;
		if (exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#nonNegativeInteger"))
		{
			dataType = DynamicDataType.INTEGER;
			conversionFunction = data -> 
			{
				return new DynamicData[] {new DynamicIntegerImpl(((RDFNode)data).asLiteral().getInt())};
			};
		}
		else if (exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#gYear"))
		{
			dataType = DynamicDataType.INTEGER;
			conversionFunction = data -> 
			{
				return new DynamicData[] {new DynamicIntegerImpl(((XSDDateTime)((RDFNode)data).asLiteral().getValue()).asCalendar().get(Calendar.YEAR))};
			};
		}
		else if (exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#date"))
		{
			dataType = DynamicDataType.STRING;
			conversionFunction = data -> 
			{
				return new DynamicData[] {new DynamicStringImpl(
						formatter.format(Instant.ofEpochMilli(((XSDDateTime)((RDFNode)data).asLiteral().getValue()).asCalendar().getTimeInMillis())))};
			};
		}
		else if (exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#decimal"))
		{
			dataType = DynamicDataType.FLOAT;
			conversionFunction = data -> 
			{
				return new DynamicData[] {new DynamicFloatImpl(((RDFNode)data).asLiteral().getFloat())};
			};
		}
		//TODO will I have to trim the lang part?
		else if (exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString") 
				|| exampleData.asLiteral().getDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string"))
		{
			dataType = DynamicDataType.STRING;
			conversionFunction = data -> 
			{
				return new DynamicData[] {new DynamicStringImpl(((RDFNode)data).asLiteral().getString())};
			};
		}
		else
		{
			throw new RuntimeException("Unknown data type: " + exampleData.asLiteral().getDatatypeURI());
		}
		
		this.dataColumnBuilderFunction  = data -> 
		{
			return conversionFunction.apply(data);
		};
		
		this.columnConstructionInfo = new DynamicColumnInfo[] {new DynamicColumnInfo(0, columnIdentifier, dataType, null, true, true)};
	}
	
	public String getNiceName()
	{
		return niceName;
	}
	
	public DynamicData[] getDataColumns(Object input)
	{
		return dataColumnBuilderFunction.apply(input);
	}
	
	public DynamicColumnInfo[] getColumnConstructionInfo()
	{
		return columnConstructionInfo;
	}

	public IsaacObjectType getReferencedComponentTypeRestriction()
	{
		return referencedComponentTypeRestriction;
	}

	public VersionType getReferencedComponentTypeSubRestriction()
	{
		return referencedComponentTypeSubRestriction;
	}
	
	public Pair<String, String[]>[] getNestedAnonRefs(String key, Object data)
	{
		@SuppressWarnings("unchecked")
		Pair<String, String[]>[] result = new Pair[nestedAnonRefsFetcher == null ? 0 : nestedAnonKeys.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = new Pair<String, String[]>(nestedAnonKeys[i], nestedAnonRefsFetcher.apply(nestedAnonKeys[i], data));
		}
		
		return result;
	}
}
