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
package sh.isaac.convert.mojo.turtle;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicDataType;
import sh.isaac.model.semantic.types.DynamicFloatImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.model.semantic.types.DynamicUUIDImpl;

/**
 * Mapping rules from common RDF Types to dynamic column types
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class DynamicTypeMap
{
	private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("Z"));
	private Logger log = LogManager.getLogger();

	private DynamicDataType dynamicDataType;
	private Function<RDFNode, DynamicData> conversionFunction;
	
	public DynamicTypeMap(String typeURI, RDFNode exampleData, Function<String, UUID> uriToUUIDFunction, Consumer<Resource> conceptThatMustBeCreated)
	{
		if (typeURI.equals("http://purl.org/dc/terms/replaces"))
		{
			//this usually points to a different version of our root concept which all merge onto the same concept.  Don't want to create this as a concept, 
			//that ends up under the unresolvedRefs area, as that leads to a strange hierarchy when diffs are merged in.
			dynamicDataType = DynamicDataType.STRING;
			conversionFunction = data -> {
				return new DynamicStringImpl(data.asResource().getURI());
			};
		}
		else if (exampleData.isLiteral())
		{
			switch (exampleData.asLiteral().getDatatypeURI())
			{
				case "http://www.w3.org/2001/XMLSchema#nonNegativeInteger" :
					dynamicDataType = DynamicDataType.INTEGER;
					conversionFunction = data -> {
						return new DynamicIntegerImpl(data.asLiteral().getInt());
					};
				break;
				
				case "http://www.w3.org/2001/XMLSchema#gYear" :
					dynamicDataType = DynamicDataType.INTEGER;
					conversionFunction = data -> {
						return new DynamicIntegerImpl(((XSDDateTime) data.asLiteral().getValue()).asCalendar().get(Calendar.YEAR));
					};
					break;
				
				case "http://purl.org/goodrelations/v1#hasValue" :  //Could be a bit sketchy saying this is an int, maybe need to use a bigger type?  Really, its just poor RDF...
				case "http://www.w3.org/2001/XMLSchema#int" :
					dynamicDataType = DynamicDataType.INTEGER;
					conversionFunction = data -> {
						return new DynamicIntegerImpl(data.asLiteral().getInt());
					};
					break;
					
				case "http://www.w3.org/2001/XMLSchema#date" :
					dynamicDataType = DynamicDataType.STRING;
					conversionFunction = data -> {
						return new DynamicStringImpl(
								formatter.format(Instant.ofEpochMilli(((XSDDateTime) data.asLiteral().getValue()).asCalendar().getTimeInMillis())));
					};
					break;
					
				case "http://www.w3.org/2001/XMLSchema#decimal" :
				case "http://www.w3.org/2006/time#years" :
					dynamicDataType = DynamicDataType.FLOAT;
					conversionFunction = data -> {
						return new DynamicFloatImpl(data.asLiteral().getFloat());
					};
					break;
				case "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString" :  //TODO should I put language somewhere?
				case "http://www.w3.org/2001/XMLSchema#string" :
				case "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML" :
					dynamicDataType = DynamicDataType.STRING;
					conversionFunction = data -> {
						return new DynamicStringImpl(data.asLiteral().getString());
					};
				break;
	 			
				default :
					log.warn("No mapping for type {}, attempting to treat as string", exampleData.asLiteral().getDatatypeURI());
					dynamicDataType = DynamicDataType.STRING;
					conversionFunction = data -> {
						return new DynamicStringImpl(data.asLiteral().getString());
					};
			}
		}
		else if (exampleData.isResource())
		{
			//URIs to treat as strings
			if (exampleData.asResource().getURI().equals("http://xmlns.com/foaf/0.1/homepage"))
			{
				dynamicDataType = DynamicDataType.STRING;
				conversionFunction = data -> {
					return new DynamicStringImpl(data.asResource().getURI());
				};
			}
			else
			{
				dynamicDataType = DynamicDataType.UUID;
				conversionFunction = data -> {
					conceptThatMustBeCreated.accept(data.asResource());
					return new DynamicUUIDImpl(uriToUUIDFunction.apply(data.asResource().getURI()));
				};
			}
		}
	}

	public DynamicDataType getDynamicDataType()
	{
		return dynamicDataType;
	}

	public DynamicData convertData(RDFNode data)
	{
		return conversionFunction.apply(data);
	}
}
