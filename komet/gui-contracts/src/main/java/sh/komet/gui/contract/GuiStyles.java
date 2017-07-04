/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.komet.gui.contract;

import javafx.scene.paint.Color;

/**
 *
 * @author kec
 */
public class GuiStyles {
   public static final String BLUEBERRY_RGB_CSS_COLOR = "#4f80d9";
   public static final Color BLUEBERRY_FX_COLOR = Color.web(BLUEBERRY_RGB_CSS_COLOR);
   
   public static final String QUERY_AND_CSS_COLOR = "tomato";
   public static final Color QUERY_AND_FX_COLOR = Color.web(QUERY_AND_CSS_COLOR);
   
   public static final String QUERY_AND_SELECTED_CSS_COLOR = colorToHex(QUERY_AND_FX_COLOR.invert());
   public static final Color QUERY_AND_SELECTED_FX_COLOR = Color.web(QUERY_AND_SELECTED_CSS_COLOR);

   public static final String QUERY_OR_CSS_COLOR = "honeydew";
   public static final Color QUERY_OR_FX_COLOR = Color.web(QUERY_OR_CSS_COLOR);
   
   public static final String QUERY_OR_SELECTED_CSS_COLOR = colorToHex(QUERY_OR_FX_COLOR.invert());
   public static final Color QUERY_OR_SELECTED_FX_COLOR = Color.web(QUERY_OR_SELECTED_CSS_COLOR);

   public static final String FOCUSED_CELL_CSS_COLOR = "#e8ffff";
   public static final Color FOCUSED_CELL_FX_COLOR = Color.web(FOCUSED_CELL_CSS_COLOR);

   public static final String EMPTY_BACKGROUND_CSS_COLOR = "#eef1f3";
   public static final Color EMPTY_BACKGROUND_FX_COLOR = Color.web(EMPTY_BACKGROUND_CSS_COLOR);

   
   public static String colorToHex(Color color) {
        return colorChanelToHex(color.getRed())
                + colorChanelToHex(color.getGreen())
                + colorChanelToHex(color.getBlue())
                + colorChanelToHex(color.getOpacity());
    }

    private static String colorChanelToHex(double chanelValue) {
        String rtn = Integer.toHexString((int) Math.min(Math.round(chanelValue * 255), 255));
        if (rtn.length() == 1) {
            rtn = "0" + rtn;
        }
        return rtn;
    }   
}
