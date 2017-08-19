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
package sh.komet.gui.style;

import javafx.css.PseudoClass;

/**
 *
 * @author kec
 */
public class PseudoClasses {
   public static final PseudoClass INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("inactive");
   /**
    * may be active, but superceded by a different component, so not current in the display
    */
   public static final PseudoClass SUPERCEDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("superceded");
   public static final PseudoClass CONTRADICTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("contradicted");

   public static final PseudoClass UNCOMMITTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("uncommitted");
   public static final PseudoClass UNCOMMITTED_WITH_ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("uncommitted-with-error");

   public static final PseudoClass LOGICAL_DEFINITION_PSEUDO_CLASS = PseudoClass.getPseudoClass("logical-definition-version");
   public static final PseudoClass DESCRIPTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("description-version");
   public static final PseudoClass CONCEPT_PSEUDO_CLASS = PseudoClass.getPseudoClass("concept-version");
}
