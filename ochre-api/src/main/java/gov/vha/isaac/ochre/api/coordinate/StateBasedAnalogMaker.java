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
package gov.vha.isaac.ochre.api.coordinate;

import gov.vha.isaac.ochre.api.State;

/**
 * Analog: A structural derivative that often differs by a single element.
 *
 * @author kec
 * @param <T> The type of object to make a time-based analog from.
 */
public interface StateBasedAnalogMaker<T> {

    /**
     * @param state the allowed states for the resulting analog
     * @return a new {@code <T>} with the specified allowed states.
     */
    T makeAnalog(State... state);

}
