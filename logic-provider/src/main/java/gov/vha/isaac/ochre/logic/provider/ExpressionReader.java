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
package gov.vha.isaac.ochre.logic.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.antlr.v4.runtime.tree.ParseTree;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;

/**
 * 
 * {@link ExpressionReader}
 *
 * @author Tony Weida
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExpressionReader 
{
	private static final String necessarySctid = "900000000000074008";
	private static final String sufficientSctid = "900000000000073002";
	
	public static Stream<ParseTree> read(File file) throws IOException
	{
		AtomicInteger lineCount = new AtomicInteger(0);
		return Files.lines(file.toPath()).filter(line -> 
		{
			lineCount.getAndIncrement();
			if (line.startsWith("#"))
			{
				return false;
			}
			if (line.startsWith("id"))
			{
				String[] strTokens = line.split("\t");

				if (! (strTokens[6].equals("mapTarget") && 
						(strTokens[7].equals("Expression") &&
								(strTokens[8].equals("definitionStatusId"))))) {
					throw new RuntimeException("First line is not the expected header!");
				}
				return false;
			}
			return true;
		}).map(line ->
		{
			try
			{
				String[] strTokens = line.split("\t");
				
//			34353-3  works
//			43734-3  works
//			25491-2  works
//			39579-8  works
				
//			if(! strTokens[6].equals("25491-2")) {
//				continue;
//			}
				
				System.out.println("\n\nLOINC EXPRESSION SERVICE> " 
						+ lineCount + ". LOINC CODE " + strTokens[6] +" = " + strTokens[7] +"; STATUS = " + strTokens[8] + "\n");
				
				String definitionSctid = strTokens[8];
				if (definitionSctid.equals(sufficientSctid)) {
					return SNOMEDCTParserUtil.parseExpression(strTokens[7]);
				}
				else if (definitionSctid.equals(necessarySctid)) {
					return SNOMEDCTParserUtil.parseExpression("<<< " + strTokens[7]);
				}
				else {
					throw new RuntimeException("Unexpected definition status: " + definitionSctid + " on line " + lineCount);
				}
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		});
	}
}
