/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.api.metadata.binding;

import java.util.UUID;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;

/**
 * 
 * {@link RefexDynamic}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RefexDynamic
{
	public static ConceptSpec UNKNOWN_CONCEPT = new ConceptSpec("unknown (null) concept", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	
	public static ConceptSpec REFEX_DYNAMIC_NAMESPACE = new ConceptSpec("Refex Dynamic Namespace", UUID.fromString("9c76af37-671c-59a3-93bf-dfe0c5c58bfa"));
	
	public static ConceptSpec REFEX_DT_STRING = new ConceptSpec("String (foundation metadata concept)", 
			UUID.fromString("a46aaf11-b37a-32d6-abdc-707f084ec8f5"));
	public static ConceptSpec REFEX_DT_INTEGER = new ConceptSpec("Signed integer (foundation metadata concept)", 
			UUID.fromString("1d1c2073-d98b-3dd3-8aad-a19c65aa5a0c"));
	public static ConceptSpec REFEX_DT_UUID = new ConceptSpec("Universally Unique Identifier (foundation metadata concept)", 
			UUID.fromString("845274b5-9644-3799-94c6-e0ea37e7d1a4"));
	
	//TODO [REFEX] define all of these concepts... figure out where they need to be created in the DB
	public static ConceptSpec REFEX_DT_NID = new ConceptSpec("nid", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_BOOLEAN = new ConceptSpec("boolean", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_LONG = new ConceptSpec("long", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_BYTE_ARRAY = new ConceptSpec("byte array", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_FLOAT = new ConceptSpec("float", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_DOUBLE = new ConceptSpec("double", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DT_POLYMORPHIC = new ConceptSpec("polymorphic", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	
	public static ConceptSpec REFEX_DYNAMIC_DEFINITION = new ConceptSpec("dynamic refex definition", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DYNAMIC_DEFINITION_DESCRIPTION = new ConceptSpec("dynamic refex definition description", 
			UUID.fromString("00000000-0000-0000-C000-000000000046"));
	
	public static ConceptSpec REFEX_DYNAMIC_TYPES = new ConceptSpec("dynamic refex types", UUID.fromString("00000000-0000-0000-C000-000000000046"));
	public static ConceptSpec REFEX_DYNAMIC_COLUMNS = new ConceptSpec("dynamic refex columns", UUID.fromString("7670172e-ca32-5689-b742-d9c2a26677cc"));
	public static ConceptSpec REFEX_IDENTITY = new ConceptSpec("refset identity", UUID.fromString("3e0cd740-2cc6-3d68-ace7-bad2eb2621da"));
}
