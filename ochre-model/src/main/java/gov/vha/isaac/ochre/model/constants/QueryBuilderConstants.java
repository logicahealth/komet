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

/**
 * Search
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 */
package gov.vha.isaac.ochre.model.constants;

/**
 * Search
 * 
 * @author <a href="mailto:joel.kniaz@gmail.com">Joel Kniaz</a>
 *
 */
public class QueryBuilderConstants {

	//TODO Dan didn't port this over with the ochre changes... suspect it will go away / be rewritten at some point.
	//Otherwise, needs to be ported in the same way as IsaacMappingConstants, for example
	/*
	 * Note - the order of definition matters - make sure that anything referenced is already declared higher in the class, 
	 * otherwise, you get null pointers in the class initializer at runtime.
	 */
	private QueryBuilderConstants() {
		//Not intended to be constructed
	}
	
//	//an organizational concept for all of the new concepts being added to the Refset Auxiliary Concept tree
//	public static ConceptSpec STORED_QUERY_METADATA = new ConceptSpec("stored query metadata", 
//			UUID.fromString("d0ad2246-2110-597c-83ca-61b2f19c9749"), 
//			DynamicSememe.DYNAMIC_SEMEME_METADATA);
//	
//	//used as salt for generating other UUIDs
//	public static ConceptSpec STORED_QUERY_NAMESPACE = new ConceptSpec("stored query namespace", 
//			UUID.fromString("c8d45733-b7af-5d21-a2df-53fd6577bff1"), STORED_QUERY_METADATA);
//		
//	public static ConceptSpec STORED_QUERIES = new ConceptSpec("stored queries", 
//			UUID.fromString("c47282db-18e3-55bc-9094-0256c0168caf"), 
//			DynamicSememe.DYNAMIC_SEMEME_ASSEMBLAGES);
//	
//	public static ConceptSpec VIEW_COORDINATE_COLUMN = new ConceptSpecWithDescriptions("view coordinate", 
//			UUID.fromString("5010f18f-c469-5315-8c5e-f7d9b65373c5"),
//			new String[] { "view coordinate" }, 
//			new String[] { "view coordinate column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static ConceptSpec MAX_RESULTS_COLUMN = new ConceptSpecWithDescriptions("max results", 
//			UUID.fromString("63981b45-bbbe-5247-b571-d7fee02aad79"),
//			new String[] { "max results" }, 
//			new String[] { "maximum displayable results column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static ConceptSpec DROOLS_EXPR_COLUMN = new ConceptSpecWithDescriptions("drools", 
//			UUID.fromString("c0091cf4-f063-5964-85c5-0fdf14b5bb00"),
//			new String[] { "drools" }, 
//			new String[] { "drools expression column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//
//	public static ConceptSpec UUID_COLUMN = DynamicSememe.DYNAMIC_SEMEME_DT_UUID;
//
//	public static DynamicRefexConceptSpec STORED_QUERY_GLOBAL_ATTRIBUTES = new DynamicRefexConceptSpec("stored query global attributes", 
//			UUID.fromString("74906b03-518f-5647-aecf-94e5b6437f2d"),
//			true, 
//			"Stored Query Global Attributes is for attributes effecting all filters on a search concept", 
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.VIEW_COORDINATE_COLUMN.getUuids()[0], DynamicSememeDataType.BYTEARRAY, null, false, null, null),
//				new DynamicSememeColumnInfo(1, Search.MAX_RESULTS_COLUMN.getUuids()[0], DynamicSememeDataType.INTEGER, null, false, null, null),
//				new DynamicSememeColumnInfo(2, Search.DROOLS_EXPR_COLUMN.getUuids()[0], DynamicSememeDataType.STRING, null, false, null, null)},
//			STORED_QUERY_METADATA);
//	
//	public static ConceptSpec ORDER_COLUMN = DynamicSememe.DYNAMIC_SEMEME_COLUMN_ORDER;
//	
//	public static ConceptSpec FILTER_INVERT_COLUMN = new ConceptSpecWithDescriptions("invert", 
//			UUID.fromString("59e916fc-4632-5574-97c2-6e63b74a2ca3"),
//			new String[] { "invert" }, 
//			new String[] { "invert filter/match results column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static DynamicRefexConceptSpec SEARCH_FILTER_ATTRIBUTES = new DynamicRefexConceptSpec("stored query attributes", 
//			UUID.fromString("d6f8eac0-a37a-5ea0-bad4-3dc89d86cc62"),
//			true, 
//			"Stored Query Attributes is for attributes effecting all filters of a certain type such as Lucene or RegExp",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.ORDER_COLUMN.getUuids()[0], DynamicSememeDataType.INTEGER, null, false, null, null),
//				new DynamicSememeColumnInfo(1, Search.FILTER_INVERT_COLUMN.getUuids()[0], DynamicSememeDataType.BOOLEAN, null, false, null, null)},
//			STORED_QUERY_METADATA);
//
//	public static ConceptSpec PARAMETER_COLUMN = new ConceptSpecWithDescriptions("param",
//			UUID.fromString("e28f2c45-1c0b-569a-a329-304ea04ade17"),
//			new String[] { "param" }, 
//			new String[] { "parameter column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static DynamicRefexConceptSpec SEARCH_SEMEME_CONTENT_FILTER = new DynamicRefexConceptSpec("Search Sememe Content Filter", 
//			UUID.fromString("1723aa79-ac7f-520f-a2f5-cd9e03dc4142"),
//			true, 
//			"Search Sememe Content Filter is for attributes effecting this Sememe Content search",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], DynamicSememeDataType.STRING, null, true, null, null),
//				new DynamicSememeColumnInfo(1, Search.UUID_COLUMN.getUuids()[0], DynamicSememeDataType.UUID, null, false, null, null)
//			},
//			STORED_QUERY_METADATA);
//	
//	public static DynamicRefexConceptSpec SEARCH_LUCENE_FILTER = new DynamicRefexConceptSpec("Search Lucene Filter", 
//			UUID.fromString("4ece37d7-1ae0-5c5e-b475-f8e3bdce4d86"),
//			true, 
//			"Search Lucene Filter is for attributes effecting this Lucene search",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], DynamicSememeDataType.STRING, null, false, null, null)},
//			STORED_QUERY_METADATA);
//
//	public static DynamicRefexConceptSpec SEARCH_REGEXP_FILTER = new DynamicRefexConceptSpec("Search RegExp Filter", 
//			UUID.fromString("39c21ff8-cd48-5ac8-8110-40b7d8b30e61"),
//			true, 
//			"Search RegExp Filter is for attributes effecting this RegExp search",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.PARAMETER_COLUMN.getUuids()[0], DynamicSememeDataType.STRING, null, false, null, null)},
//			STORED_QUERY_METADATA);
//
//	public static ConceptSpec ANCESTOR_COLUMN = new ConceptSpecWithDescriptions("ancestor",
//			UUID.fromString("fdcac37e-e22f-5f51-b7a6-f8de283c6cf0"),
//			new String[] { "ancestor" }, 
//			new String[] { "ancestor concept column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static DynamicRefexConceptSpec SEARCH_ISDESCENDANTOF_FILTER = new DynamicRefexConceptSpec("Search IsDescendantOf Filter", 
//			UUID.fromString("58bea66c-65fb-5c52-bf71-d742aebe3822"),
//			true, 
//			"Search IsDescendantOf Filter is for attributes effecting this IsDescendantOf search",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.ANCESTOR_COLUMN.getUuids()[0], DynamicSememeDataType.UUID, null, false, null, null)},
//			STORED_QUERY_METADATA);
//	
//	public static ConceptSpec MATCH_COLUMN = new ConceptSpecWithDescriptions("match",
//			UUID.fromString("53b89cac-54c4-5cf8-bf87-baee591729f5"),
//			new String[] { "match" }, 
//			new String[] { "matching concept column" },
//			DynamicSememe.DYNAMIC_SEMEME_COLUMNS);
//	
//	public static DynamicRefexConceptSpec SEARCH_ISA_FILTER = new DynamicRefexConceptSpec("Search IsA Filter", 
//			UUID.fromString("77823bc2-5924-544e-9496-bb54cad41d63"),
//			true, 
//			"Search IsA Filter is for attributes effecting this IsA search",
//			new DynamicSememeColumnInfo[] {
//				new DynamicSememeColumnInfo(0, Search.MATCH_COLUMN.getUuids()[0], DynamicSememeDataType.UUID, null, false, null, null)},
//			STORED_QUERY_METADATA);
}