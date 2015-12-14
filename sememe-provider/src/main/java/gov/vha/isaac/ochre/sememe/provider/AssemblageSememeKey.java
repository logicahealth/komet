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
package gov.vha.isaac.ochre.sememe.provider;

/**
 *
 * @author kec
 */
public class AssemblageSememeKey implements Comparable<AssemblageSememeKey> {
    int assemblageSequence;
    int sememeSequence;

    public AssemblageSememeKey(int assemblageKey, int sememeSequence) {
        this.assemblageSequence = assemblageKey;
        this.sememeSequence = sememeSequence;
    }

    @Override
    public int compareTo(AssemblageSememeKey o) {
        if (assemblageSequence != o.assemblageSequence) {
            if (assemblageSequence < o.assemblageSequence) {
                return -1;
            }
            return 1;
        }
        if (sememeSequence == o.sememeSequence) {
            return 0;
        }
        if (sememeSequence < o.sememeSequence) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssemblageSememeKey otherKey = (AssemblageSememeKey) o;

        if (assemblageSequence != otherKey.assemblageSequence) return false;
        return sememeSequence == otherKey.sememeSequence;
    }

    @Override
    public int hashCode() {
        int result = assemblageSequence;
        result = 31 * result + sememeSequence;
        return result;
    }

    public int getAssemblageSequence() {
        return assemblageSequence;
    }

    public int getSememeSequence() {
        return sememeSequence;
    }

    @Override
    public String toString() {
        return "AssemblageSememeKey{" +
                "assemblageSequence=" + assemblageSequence +
                ", sememeSequence=" + sememeSequence +
                '}';
    }
}