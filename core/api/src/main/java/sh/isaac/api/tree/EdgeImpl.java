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
package sh.isaac.api.tree;

import sh.isaac.api.Edge;

/**
 *
 * @author kec
 */
public class EdgeImpl implements Edge {
    final int typeNid;
    final int destinationNid;

    public EdgeImpl(int typeNid, int destinationNid) {
        this.typeNid = typeNid;
        this.destinationNid = destinationNid;
    }

    @Override
    public int getTypeNid() {
        return typeNid;
    }

    @Override
    public int getDestinationNid() {
        return destinationNid;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.typeNid;
        hash = 79 * hash + this.destinationNid;
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
        if (!(obj instanceof Edge)) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (this.typeNid != other.getTypeNid()) {
            return false;
        }
        if (this.destinationNid != other.getDestinationNid()) {
            return false;
        }
        return true;
    }
}
