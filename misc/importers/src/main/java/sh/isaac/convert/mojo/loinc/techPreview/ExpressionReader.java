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



package sh.isaac.convert.mojo.loinc.techPreview;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

//~--- non-JDK imports --------------------------------------------------------

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ExpressionReader}.
 *
 * @author Tony Weida
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExpressionReader {

    private static final Logger LOG = LogManager.getLogger();
   /** The Constant necessarySctid. */
   private static final String necessarySctid = "900000000000074008";

   /** The Constant sufficientSctid. */
   private static final String sufficientSctid = "900000000000073002";

   //~--- methods -------------------------------------------------------------

   /**
    * Read.
    *
    * @param file the file
    * @return the stream
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static Stream<ParseTree> read(File file)
            throws IOException {
      final AtomicInteger lineCount = new AtomicInteger(0);

      return Files.lines(file.toPath())
                  .filter(line -> {
                             lineCount.getAndIncrement();

                             if (line.startsWith("#")) {
                                return false;
                             }

                             if (line.startsWith("id")) {
                                final String[] strTokens = line.split("\t");

                                if (!(strTokens[6].equals("mapTarget") &&
                                      (strTokens[7].equals("Expression") &&
                                       (strTokens[8].equals("definitionStatusId"))))) {
                                   throw new RuntimeException("First line is not the expected header!");
                                }

                                return false;
                             }

                             return true;
                          })
                  .map(line -> {
                          try {
                             final String[] strTokens = line.split("\t");

                             // 34353-3  works
                             // 43734-3  works
                             // 25491-2  works
                             // 39579-8  works
                             // if(! strTokens[6].equals("25491-2")) {
                             // continue;
                             // }
                             LOG.debug("\n\nLOINC EXPRESSION SERVICE> " + lineCount + ". LOINC CODE " +
                             strTokens[6] + " = " + strTokens[7] + "; STATUS = " + strTokens[8] + "\n");

                             final String definitionSctid = strTokens[8];

                             switch (definitionSctid) {
                                case sufficientSctid:
                                   return SNOMEDCTParserUtil.parseExpression(strTokens[7]);
                                case necessarySctid:
                                   return SNOMEDCTParserUtil.parseExpression("<<< " + strTokens[7]);
                                default:
                                   throw new RuntimeException("Unexpected definition status: " + definitionSctid +
                                           " on line " + lineCount);
                             }
                          } catch (final Exception e) {
                             throw new RuntimeException(e);
                          }
                       });
   }
}

