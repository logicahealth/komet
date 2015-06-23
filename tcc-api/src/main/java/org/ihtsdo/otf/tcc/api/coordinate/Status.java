/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY_STATE_SET KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.coordinate;

import gov.vha.isaac.ochre.api.State;
import java.util.EnumSet;

/**
 *
 * @author kec
 */
public enum Status {
    INACTIVE(false, State.INACTIVE), 
    ACTIVE(true, State.ACTIVE);

    boolean isActive;
    State state;

    Status(boolean isActive, State state) {
        this.isActive = isActive;
        this.state = state;
    }
    public boolean getBoolean() {
        return isActive;
    }

    public static Status getFromBoolean(boolean isActive) {
        if (isActive) {
            return ACTIVE;
        }
        return INACTIVE;
    }

    public State getState() {
        return state;
    }
    
    public static Status getStatusFromState(State state) {
        switch(state) {
            case ACTIVE: return ACTIVE;
            case INACTIVE: return INACTIVE;
                default: 
                    throw new UnsupportedOperationException("Can't handle: " + state);
        }
    }
    
    public static EnumSet<State> getStateSet(EnumSet<Status> statusSet) {
       EnumSet<State> allowedStates = EnumSet.noneOf(State.class);
        statusSet.forEach((status) -> { allowedStates.add(status.getState());});
        return allowedStates;
    }
}

