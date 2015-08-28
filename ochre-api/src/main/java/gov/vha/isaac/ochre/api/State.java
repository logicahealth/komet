/*
 * Copyright 2015 kec.
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
package gov.vha.isaac.ochre.api;

import java.util.EnumSet;

/**
 *
 * @author kec
 */
public enum State {
    
		/**
		 * Currently inactive.
		 */
       INACTIVE(false), 
		 /**
		  * Currently active.
		  */
       ACTIVE(true), 
		 /**
		  * Not yet created. 
		  */
       PRIMORDIAL(false);

    boolean isActive;

    State(boolean isActive) {
        this.isActive = isActive;
    }
    public boolean getBoolean() {
        return isActive;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public static EnumSet<State> ACTIVE_ONLY_SET = EnumSet.of(State.ACTIVE);
    public static EnumSet<State> ANY_STATE_SET = EnumSet.allOf(State.class);
    
}
 

