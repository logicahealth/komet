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



package sh.isaac.converters.sharedUtils;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

//~--- classes ----------------------------------------------------------------

/**
 * {@link ConsoleUtil}
 *
 * Utility code for writing to the console in a more intelligent way, including detecting running without a real console,
 * and changing the behavior as appropriate.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ConsoleUtil {
   /** The progress line. */
   private static boolean progressLine = false;

   /** The prints since return. */
   private static int printsSinceReturn = 0;

   /** The progress line used. */
   private static boolean progressLineUsed = false;

   /** The console output cache. */
   private static StringBuilder consoleOutputCache = new StringBuilder();

   /** The eol. */
   private static String eol = System.getProperty("line.separator");

   /** The disable fancy. */
   public static boolean disableFancy = (System.console() == null);

   /** The last status. */
   private static int lastStatus;

   //~--- methods -------------------------------------------------------------

   /**
    * Prints the.
    *
    * @param string the string
    */
   public static void print(String string) {
      if (progressLine) {
         if (disableFancy) {
            if (progressLineUsed) {
               System.out.println();
               printsSinceReturn = 0;
            }
         } else {
            System.out.print("\r \r");
         }

         progressLine = false;
      }

      System.out.print(string);
      consoleOutputCache.append(string);
   }

   /**
    * Prints the errorln.
    *
    * @param string the string
    */
   public static void printErrorln(String string) {
      if (progressLine) {
         if (disableFancy) {
            if (progressLineUsed) {
               System.out.println();
               printsSinceReturn = 0;
            }
         } else {
            System.out.print("\r \r");
         }

         progressLine = false;
      }

      System.err.println(string);
      consoleOutputCache.append("ERROR->");
      consoleOutputCache.append(string);
      consoleOutputCache.append(eol);
      printsSinceReturn = 0;
      progressLine      = true;
      progressLineUsed  = false;
   }

   /**
    * Println.
    *
    * @param string the string
    */
   public static void println(String string) {
      if (progressLine) {
         if (disableFancy) {
            if (progressLineUsed) {
               System.out.println();
               printsSinceReturn = 0;
            }
         } else {
            System.out.print("\r \r");
         }
      }

      System.out.println(string);
      consoleOutputCache.append(string);
      consoleOutputCache.append(eol);
      progressLine     = true;
      progressLineUsed = false;
   }

   /**
    * Show progress.
    */
   public static void showProgress() {
      char c;

      switch (lastStatus) {
      case 0:
         c = '/';
         break;

      case 1:
         c = '-';
         break;

      case 2:
         c = '\\';
         break;

      case 3:
         c = '|';
         break;

      default:  // shouldn't be used
         c = '-';
         break;
      }

      lastStatus++;

      if (lastStatus > 3) {
         lastStatus = 0;
      }

      if (!progressLine) {
         System.out.println();
         printsSinceReturn = 0;
      }

      if (disableFancy) {
         System.out.print(".");
         printsSinceReturn++;

         if (printsSinceReturn >= 75) {
            System.out.println();
            printsSinceReturn = 0;
         }
      } else {
         System.out.print("\r" + c);
      }

      progressLine     = true;
      progressLineUsed = true;
   }

   /**
    * Write output to file.
    *
    * @param path the path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public static void writeOutputToFile(Path path)
            throws IOException {
      final BufferedWriter bw = Files.newBufferedWriter(path,
                                                        Charset.forName("UTF-8"),
                                                        new OpenOption[] { StandardOpenOption.CREATE });

      bw.append(consoleOutputCache.toString());
      bw.close();
      consoleOutputCache.setLength(0);
      printsSinceReturn = 0;
   }
}

