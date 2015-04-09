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
package gov.vha.isaac.ochre.api;

import com.sun.javafx.application.PlatformImpl;

import java.awt.*;
import java.lang.annotation.Annotation;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 *
 * @author kec
 */
public class LookupService {
    private static ServiceLocator looker = null;
    public static final int ISAAC_STARTED_RUNLEVEL = 4;
    public static final int ISAAC_STOPPED_RUNLEVEL = -1;
    
    public static ServiceLocator get() {
        if (looker == null) {
            if (GraphicsEnvironment.isHeadless()) {
                System.setProperty("javafx.toolkit", "com.sun.javafx.tk.DummyToolkit");
            }
            PlatformImpl.startup(() -> {
                // No need to do anything here
            });
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
    
    public static void startupIsaac() {
        getRunLevelController().proceedTo(ISAAC_STARTED_RUNLEVEL);
    }
    public static void shutdownIsaac() {
        getRunLevelController().proceedTo(ISAAC_STOPPED_RUNLEVEL);        
    }
}
