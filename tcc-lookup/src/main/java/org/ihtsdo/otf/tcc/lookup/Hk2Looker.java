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
package org.ihtsdo.otf.tcc.lookup;

import javafx.embed.swing.JFXPanel;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import java.lang.annotation.Annotation;

/**
 *
 * @author aimeefurber
 */
public class Hk2Looker {
    private static ServiceLocator looker = null;
    public static ServiceLocator get() {
        if (looker == null) {
            JFXPanel initFxPanel = new JFXPanel();
            looker = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        }
        return looker;
    }

    public static <T> T getService(Class<T> contractOrImpl) {
        return get().getService(contractOrImpl, new Annotation[0]);
    }

    public static RunLevelController getRunLevelController() {
        return getService(RunLevelController.class);
    }
}
