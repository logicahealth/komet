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
package sh.komet.gui.search.flwor;

import java.util.Objects;
import java.util.function.BiFunction;
import sh.isaac.api.coordinate.StampCoordinate;

/**
 *
 * @author kec
 */
public class CellFunction {
    final String functionName;
    final BiFunction<String,StampCoordinate, String> function;

    public CellFunction(String functionName, BiFunction<String, StampCoordinate, String> function) {
        this.functionName = functionName;
        this.function = function;
    }
    
    public String apply(String dataIn, StampCoordinate stampCoordinate) {
        return function.apply(dataIn, stampCoordinate);
    }

    @Override
    public String toString() {
        return functionName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.functionName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellFunction other = (CellFunction) obj;
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        return true;
    }
    
    
}
