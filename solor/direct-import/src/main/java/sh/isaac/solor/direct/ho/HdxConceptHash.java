/*
 * Copyright 2019 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
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
package sh.isaac.solor.direct.ho;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author kec
 */
public class HdxConceptHash {
    final String name;
    final int[] parents;
    final String refid;

    public HdxConceptHash(String name, int[] parents, String refid) {
        this.name = name;
        Arrays.sort(parents);
        this.parents = parents.clone();
        this.refid = refid;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.name);
        hash = 89 * hash + Arrays.hashCode(this.parents);
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
        final HdxConceptHash other = (HdxConceptHash) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Arrays.equals(this.parents, other.parents);
    }
    
    
}
