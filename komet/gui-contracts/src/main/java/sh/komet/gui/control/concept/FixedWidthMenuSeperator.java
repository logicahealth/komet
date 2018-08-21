/*
 * Copyright 2018 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.komet.gui.control.concept;

import javafx.geometry.Orientation;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Separator;

/**
 *
 * @author kec
 */
public class FixedWidthMenuSeperator extends CustomMenuItem {

    FixedSizeSeparator seperator;
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default SeparatorMenuItem instance.
     */
    public FixedWidthMenuSeperator() {
        super(new FixedSizeSeparator(Orientation.HORIZONTAL), false);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        seperator = (FixedSizeSeparator) getContent();
    }

    public void setWidth(double value) {
        seperator.setWidth(value);
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "separator-menu-item";
    
    private static class FixedSizeSeparator extends Separator {

        public FixedSizeSeparator() {
        }

        public FixedSizeSeparator(Orientation orientation) {
            super(orientation);
        }

        @Override
        public void setWidth(double value) {
            super.setWidth(value); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected double computePrefWidth(double height) {
            return getWidth();
        }

        @Override
        protected double computeMaxWidth(double height) {
            return getWidth();
        }

        @Override
        protected double computeMinWidth(double height) {
            return getWidth();
        }
        
    }
}
