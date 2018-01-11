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
package sh.isaac.api.observable.semantic.version.brittle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import sh.isaac.api.component.semantic.version.brittle.Str1_Nid2_Nid3_Nid4_Version;
import sh.isaac.api.observable.semantic.version.ObservableSemanticVersion;

/**
 *
 * @author kec
 */
public interface Observable_Str1_Nid2_Nid3_Nid4_Version  
   extends ObservableSemanticVersion,  Str1_Nid2_Nid3_Nid4_Version {
   StringProperty str1Property();
   IntegerProperty nid2Property();
   IntegerProperty nid3Property();
   IntegerProperty nid4Property();
}
