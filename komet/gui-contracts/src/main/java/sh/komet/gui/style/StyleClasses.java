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

/**
 *
 * @author kec
 */
public enum StyleClasses {
   AND_CLAUSE, OR_CLAUSE, AND_CLAUSE_CHILD, OR_CLAUSE_CHILD,
   COMPONENT_BADGE, COMPONENT_PANEL, CONCEPT_DETAIL_PANE,
   COMPONENT_DETAIL_BACKGROUND, COMPONENT_VERSION_WHAT_CELL,
   COMPONENT_TEXT, VERSION_PANEL, HEADER_PANEL, HEADER_TEXT, ADD_DESCRIPTION_BUTTON,
   ADD_ATTACHMENT, STAMP_INDICATOR, VERSION_GRAPH_TOGGLE, ADD_TAB_MENU_BUTTON,
   MULTI_PARENT_TREE_NODE, TOP_GRID_PANE, EDIT_COMPONENT_BUTTON, HBOX, ASSEMBLAGE_DETAIL,
   CONCEPT_LABEL, ASSEMBLAGE_NAME_TEXT, DEFINITION_TEXT, CONCEPT_TEXT, SEMEME_TEXT, ERROR_TEXT, 
   DATE_TEXT, CONCEPT_COMPONENT_REFERENCE, SEMEME_COMPONENT_REFERENCE, CANCEL_BUTTON, COMMIT_BUTTON,
   ALERT_PANE, ALERT_TITLE, ALERT_DESCRIPTION, MORE_ALERT_DETAILS, ALERT_ICON,
   
   DEF_ROLE, DEF_ROLE_GROUP, DEF_CONCEPT, DEF_EMBEDDED_CONCEPT, DEF_FEATURE,
   DEF_SUFFICIENT_SET, DEF_NECESSARY_SET
   ;
   
   @Override   
   public String toString() {
      return name().toLowerCase().replace('_', '-');
   }

}
