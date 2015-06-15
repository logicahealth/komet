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
package gov.vha.isaac.ochre.api.observable;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.MutableStampedVersion;
import gov.vha.isaac.ochre.api.commit.CommitStates;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;

/**
 *
 * @author kec
 */
public interface ObservableVersion extends MutableStampedVersion {
    
    ObjectProperty<State> stateProperty();
    
    LongProperty timeProperty();
    
    IntegerProperty authorSequenceProperty();
    
    IntegerProperty moduleSequenceProperty();
    
    IntegerProperty pathSequenceProperty();
    
    IntegerProperty stampSequenceProperty();
    
    ObjectProperty<CommitStates> commitStateProperty();
    
    ObservableChronology<? extends ObservableVersion> getChronology();
    
}
