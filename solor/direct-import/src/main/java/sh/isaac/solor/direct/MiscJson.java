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
package sh.isaac.solor.direct;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 *
 * @author kec
 */
public class MiscJson extends TimedTaskWithProgressTracker<Void> {

    private final File jsonFile;
    private final HashSet<Integer> refIdSet = new HashSet();

    public MiscJson(File jsonFile) {
        this.jsonFile = jsonFile;
    }

    @Override
    protected Void call() throws Exception {
        try (JsonReader reader = new JsonReader(new FileInputStream(jsonFile))) {
            Object jsonObject = reader.readObject();
            LOG.info(recursiveToString(jsonObject));
            LOG.info("Concept count: " + conceptCount);
            LOG.info("Entity count: " + entityCount);
            LOG.info("Attribute count: " + attributeCount);
            LOG.info("Relationship count: " + relationshpCount);
        }
        return null;
    }

    public String recursiveToString(Object jsonObject) {
        StringBuilder builder = new StringBuilder();
        recursiveToString(jsonObject, builder, 0);
        return builder.toString();
    }

    private void recursiveToString(Object obj, StringBuilder builder, int depth) {
        if (obj instanceof Object[]) {
            pad(builder, depth).append("[").append(obj.getClass().getSimpleName()).append("\n");
            Object[] objectArray = (Object[]) obj;
            for (Object arrayObject : objectArray) {
                recursiveToString(arrayObject, builder, depth + 1);
            }
            pad(builder, depth).append("]\n");
        } else if (obj instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) obj;
            if (jsonObject.containsKey("type")) {
                if (jsonObject.get("type").equals("Concept")) {
                    handleConcept(jsonObject, builder, depth);
                    return;
                }
                if (jsonObject.get("type").equals("Entity")) {
                    handleEntity(jsonObject, builder, depth);
                    return;
                }
                if (jsonObject.get("type").equals("Attribute")) {
                    handleAttribute(jsonObject, builder, depth);
                    return;
                }
                if (jsonObject.get("type").equals("Relationship")) {
                    handleRelationship(jsonObject, builder, depth);
                    return;
                }
            }
            pad(builder, depth).append("[\n");
            for (Object key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (value instanceof String
                        || value instanceof Number) {
                    pad(builder, depth + 1).append(key.toString());
                    builder.append(": ").append(value).append("\n");
                } else {
                    pad(builder, depth + 1).append(key.toString());
                    builder.append(": \n");
                    recursiveToString(value, builder, depth + 1);
                }
            }
            pad(builder, depth).append("]\n");
        } else {
            pad(builder, depth).append(obj);
        }
    }

    private StringBuilder pad(StringBuilder builder, int depth) {
        for (int i = 0; i < depth; i++) {
            builder.append("   ");
        }
        return builder;
    }
    
    int conceptCount = 0;
    private void handleConcept(JsonObject concept, StringBuilder builder, int depth) {
        conceptCount++;
        pad(builder, depth).append("Concept: ").append(concept.get("name")).append("\n");
        handleRefId(concept, builder, depth);
        
    }
    int entityCount = 0;
    private void handleEntity(JsonObject entity, StringBuilder builder, int depth) {
        entityCount++;
        pad(builder, depth).append("Entity: ").append(entity.get("name")).append("\n");
        handleRefId(entity, builder, depth);
        
    }

    private void handleRefId(JsonObject entity, StringBuilder builder, int depth) throws NumberFormatException {
        String refId = (String) entity.get("ref_id");
        Integer refIdInt = Integer.parseInt(refId);
        if (refIdSet.contains(refIdInt)) {
            pad(builder, depth+1).append("Duplicate ref_id: ").append(refIdInt).append("\n");
        } else {
            refIdSet.add(refIdInt);
        }
    }
    int attributeCount = 0;
    private void handleAttribute(JsonObject attribute, StringBuilder builder, int depth) {
        attributeCount++;
        pad(builder, depth).append("Attribute: ").append(attribute.get("name")).append("\n");
        handleRefId(attribute, builder, depth);
        
    }
    int relationshpCount = 0;
    private void handleRelationship(JsonObject relationship, StringBuilder builder, int depth) {
        relationshpCount++;
        pad(builder, depth).append("Relationship: ").append(relationship.get("name")).append("\n");
        handleRefId(relationship, builder, depth);
        
    }
}
