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
package org.ihtsdo.otf.tcc.datastore.temp;

import java.awt.Component;
import java.util.logging.Level;


public class AceLogWithAlerts extends LogWithAlerts {

    public AceLogWithAlerts(String logName) {
        super(logName);
    }

    public void nonModalAlertAndLogException(Component parent, Throwable ex) {
        nonModalAlertAndLogException(parent, Level.SEVERE, ex.getLocalizedMessage(), ex);
    }

    public void nonModalAlertAndLogException(Throwable ex) {
        nonModalAlertAndLogException(null, Level.SEVERE, ex.getLocalizedMessage(), ex);
    }

    public void nonModalAlertAndLogException(Level level, String message, Throwable ex) {
        nonModalAlertAndLogException(null, level, message, ex);
    }

    public void nonModalAlertAndLogException(Component parent, Level level, String message, Throwable ex) {
        getLogger().log(level, message, ex);
        // get front frame...
        if (level.intValue() <= Level.INFO.intValue()) {
            message = "<html>" + message;
        } else if (level.intValue() <= Level.WARNING.intValue()) {
            message = "<html><font color='red'>" + message;
        } else {
            message = "<html><font color='red'>" + message;
        }
    }
}
