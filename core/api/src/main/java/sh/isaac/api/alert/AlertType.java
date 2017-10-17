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

package sh.isaac.api.alert;

/**
 * 
 * FILTERs: Accept, Deny, Neutral
 * 
 * Context wide filters, 
 * Logger filters,
 * Appender filters,
 * Appender reference filters
 * 
 * ThreadContext Map -> For example,
if the user's loginId is being captured in the ThreadContext Map then it is possible to enable debug
logging for only that user
* 
* MapFilter
The MapFilter allows filtering against data elements that are in a MapMessage.
 * 
 * 
 * MarkerFilter
The MarkerFilter compares the configured Marker value against the Marker that is included in the
LogEvent. A match occurs when the Marker name matches either the Log Event's Marker or one of its
parents.
* 
* LogEvent
 * A subset of javafx.scene.control.Alert.AlertType
 * @author kec
 */
public enum AlertType {
   /** An information alert. */
   INFORMATION,

   /** A warning alert. */
   WARNING,

   /** An error alert. */
   ERROR,

   /** A confirmation alert. Not sure about this one...
    confirmation alerts would need some type of time out perhaps...
    */
   CONFIRMATION, 
   
   /**
    * Indicate success of an activity such as a commit or another automated process. 
    */
   SUCCESS;
}

