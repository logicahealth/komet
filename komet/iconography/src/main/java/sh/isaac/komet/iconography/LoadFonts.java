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



package sh.isaac.komet.iconography;

//~--- JDK imports ------------------------------------------------------------

import java.awt.GraphicsEnvironment;

//~--- non-JDK imports --------------------------------------------------------

import javafx.scene.text.Font;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class LoadFonts {
   public static boolean load() {
      GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font.loadFont(getFontUrl("OpenSans-Bold.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-BoldItalic.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-ExtraBold.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-ExtraBoldItalic.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-Italic.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-Light.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-LightItalic.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-Regular.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-SemiBold.ttf"), 10);
      Font.loadFont(getFontUrl("OpenSans-SemiBoldItalic.ttf"), 10);      
      return true;
   }
   
   
   public static String getFontUrl(String file) {
      return LoadFonts.class.getResource("/sh/isaac/komet/fonts/" + file).toString();
   }
}

