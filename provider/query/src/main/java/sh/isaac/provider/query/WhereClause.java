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



package sh.isaac.provider.query;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

//~--- classes ----------------------------------------------------------------

/**
 * The Class WhereClause.
 *
 * @author kec
 */
@XmlRootElement(name = "clause")
@XmlAccessorType(value = XmlAccessType.PROPERTY)
public class WhereClause {
   /** The let keys. */
   List<String> letKeys = new ArrayList<>();

   /** The children. */
   List<WhereClause> children = new ArrayList<>();

   /** The semantic. */
   ClauseSemantic semantic;

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the children.
    *
    * @return the children
    */
   public List<WhereClause> getChildren() {
      return this.children;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the children.
    *
    * @param children the new children
    */
   public void setChildren(List<WhereClause> children) {
      this.children = children;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the let keys.
    *
    * @return the let keys
    */
   public List<String> getLetKeys() {
      return this.letKeys;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the let keys.
    *
    * @param letKeys the new let keys
    */
   public void setLetKeys(List<String> letKeys) {
      this.letKeys = letKeys;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the semantic.
    *
    * @return the semantic
    */
   @XmlTransient
   public ClauseSemantic getSemantic() {
      return this.semantic;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the semantic.
    *
    * @param semantic the new semantic
    */
   public void setSemantic(ClauseSemantic semantic) {
      this.semantic = semantic;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the semantic string.
    *
    * @return the semantic string
    */
   public String getSemanticString() {
      return this.semantic.name();
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the semantic string.
    *
    * @param semanticName the new semantic string
    */
   public void setSemanticString(String semanticName) {
      this.semantic = ClauseSemantic.valueOf(semanticName);
   }
}

