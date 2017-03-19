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



package sh.isaac.api.coordinate;

/**
 * The Enum StampPrecedence.
 *
 * @author kec
 */
public enum StampPrecedence {
   /** The time. */
   TIME("time precedence",
        "<html>If two versions are both on a route to the destination, " +
        "the version with the later time has higher precedence."),

   /** The path. */
   PATH("path precedence",
        "<html>If two versions are both on route to the destination, " +
        "but one version is on a path that is closer to the destination, " +
        "the version on the closer path has higher precedence.<br><br>If two versions " +
        "are on the same path, the version with the later time has higher precedence.");

   /** The label. */
   private final String label;

   /** The description. */
   private final String description;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new stamp precedence.
    *
    * @param label the label
    * @param description the description
    */
   private StampPrecedence(String label, String description) {
      this.label       = label;
      this.description = description;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * To string.
    *
    * @return the string
    */
   @Override
   public String toString() {
      return this.label;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the description.
    *
    * @return the description
    */
   public String getDescription() {
      return this.description;
   }
}

