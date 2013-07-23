/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.ihtsdo.otf.tcc.api.conflict;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

/**
 * Implements the original ACE conflict resolution strategy. This is also used
 * as the default
 * value.
 * <p>
 * Essentially this considers conflict to exist when there is more than one
 * state for any given entity on the user's configured view paths.
 * <p>
 * Conflict resolution is not performed, so the result of resolution is the same
 * as the passed parameter.
 * 
 * @author Dion
 * 
 */
public class IdentifyAllConflictStrategy extends ContradictionManagementStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "<html>This resolution strategy has two main characteristics"
            + "<ul><li>contradiction is considered to exist when there is more than one state for an entity on the user's configured view paths</li>"
            + "<li>resolution is not performed, therefore the content displayed is not filtered or altered</li></ul>"
            + "This strategy is useful for expert users or for independant authoring.</html>";
    }

    @Override
    public String getDisplayName() {
        return "Identify all conflicts";
    }

    /**
     * Determines if there is a conflict as defined by this resolution strategy
     * for the tuples representing an entity. Will return true if there is
     * more than 1 tuple representing different entity states aside from
     * version and path. Will return false if there is less than 2 tuples or
     * the tuples passed only differ in version and path.
     * 
     * @param <T>
     *            type of the objects in the passed list - extension of
     *            I_AmTuple
     * @param tuples
     *            tuples to check for a conflict
     * @return true if there are alternate states represented by the tuples,
     *         false if there are less than 2 tuples or the tuples all represent
     *         the same state aside from version timestamp and path
     */
//    protected <T extends I_AmPart> boolean doesConflictExist(List<T> versions) {
//        if (versions.size() < 2) {
//            return false;
//        }
//
//        for (Map<Integer, T> map : getLatestTuplesByEntityByPath(versions).values()) {
//            if (map.values().size() > 1) {
//                Iterator<T> tupleIterator = map.values().iterator();
//                T first = tupleIterator.next();
//
//                I_AmPart firstDuplicate = (I_AmPart) first.makeAnalog(1, 0, 0);
//
//                while (tupleIterator.hasNext()) {
//                    T tuple = (T) tupleIterator.next();
//                    I_AmPart tupleDuplicate = (I_AmPart) tuple.makeAnalog(1, 0, 0);
//
//                    if (tupleDuplicate.equals(firstDuplicate)) {
//                        continue; // identical, no conflict with this one, check
//                        // for more
//                    } else {
//                        return true; // different data - conflict, don't need to
//                        // look for more
//                    }
//                }
//            }
//        }
//        // this means that all parts had the same date aside from version and
//        // path
//        // no conflict
//        return false;
//    }

//    <T extends I_AmPart> Map<Integer, Map<Integer, T>> getLatestTuplesByEntityByPath(List<T> tuples) {
//        Map<Integer, Map<Integer, T>> sortedTuples = new HashMap<Integer, Map<Integer, T>>();
//
//        for (T t : tuples) {
//            int termId;
//            if (t instanceof I_AmTuple) {
//                termId = ((I_AmTuple) t).getNid();
//            } else {
//                termId = 0;
//            }
//
//            Map<Integer, T> map = sortedTuples.get(termId);
//            if (map == null) {
//                map = new HashMap<Integer, T>();
//            }
//            T existingTuple = map.get(t.getPathNid());
//            if (existingTuple == null || existingTuple.getTime() < t.getTime()) {
//                map.put(t.getPathNid(), t);
//                sortedTuples.put(termId, map);
//            }
//        }
//        return sortedTuples;
//    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
        return versions;
    }

    @Override
    public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
        ArrayList<T> values = new ArrayList<>();
        values.add(part1);
        values.add(part2);
        return values;
    }
}
