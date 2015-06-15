/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api.observable.sememe.version;

import gov.vha.isaac.ochre.api.component.sememe.SememeObject;
import gov.vha.isaac.ochre.api.observable.ObservableVersion;
import gov.vha.isaac.ochre.api.observable.sememe.ObservableSememeChronology;

/**
 *
 * @author kec
 * @param <V>
 */
public interface ObservableSememeVersion<V extends ObservableSememeVersion> 
    extends ObservableVersion, SememeObject {

    @Override
    ObservableSememeChronology<V> getChronology();

}
