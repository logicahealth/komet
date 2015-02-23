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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.coordinate;

/**
 *
 * @author kec
 */
public enum StampPrecedence {
    
    TIME("time precedence","<html>If two versions are both on a route to the destination, " +
    		"the version with the later time has higher precedence."),
    PATH("path precedence","<html>If two versions are both on route to the destination, " +
    		"but one version is on a path that is closer to the destination, " +
    		"the version on the closer path has higher precedence.<br><br>If two versions " +
        	"are on the same path, the version with the later time has higher precedence.");
    
    private final String label;
    private final String description;
    
    private StampPrecedence(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label;
    }

}
