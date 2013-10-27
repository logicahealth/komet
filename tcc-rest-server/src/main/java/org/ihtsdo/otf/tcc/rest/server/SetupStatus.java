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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.rest.server;

/**
 *
 * @author dylangrald
 */
public enum SetupStatus {
    BUILDING("The server application is downloading and/or processing dependencies."),
    OPENING_DB("The server application is opening the database."),
    DB_OPEN("The database is open."),
    CLOSING_DB("Closing the database."),
    DB_OPEN_FAILED("Setting up or opening the database failed.");
    
      private final String text;

    SetupStatus(String text) {
        this.text = text;
    }
    
    @Override
    public String toString(){
        return text;
    }
}
