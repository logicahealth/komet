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
package gov.va.oia.terminology.converters.sharedUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import org.dwfa.util.id.Type5UuidFactory;

public class UuidFromName
{

	/**
	 * This is how to get a UUID that the WB expects from a string in a pom....
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench development path"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench development origin"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench release candidate path"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "Project Refsets"));

	}

}
