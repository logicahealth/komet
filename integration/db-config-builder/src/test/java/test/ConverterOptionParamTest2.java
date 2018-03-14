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
package test;

import java.io.File;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * Adhoc testing code
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ConverterOptionParamTest2
{
	public static void main(String[] args) throws Exception
	{
		
		// testing fromLocal
		ConverterOptionParam[] cop = ConverterOptionParam.fromArtifact(new File("/mnt/STORAGE/Work/VetsEZ/Maven/repository"), SupportedConverterTypes.ICD10_CM, 
				"4.48-SNAPSHOT", null, null, null);
		System.out.println("Read " + cop.length + " options");
		for (ConverterOptionParam cop1 : cop)
		{
			System.out.println(cop1);
		}

		// testing fromArtifact
		ConverterOptionParam[] cop2 = ConverterOptionParam.fromArtifact(null, SupportedConverterTypes.ICD10_CM, 
				"4.48-SNAPSHOT", "https://sagebits.net/nexus/repository/tmp-content/", "foo", "foo".toCharArray());
		System.out.println("Read " + cop2.length + " options");
		for (ConverterOptionParam cop1 : cop2)
		{
			System.out.println(cop1);
		}
		System.exit(0);
	}
}