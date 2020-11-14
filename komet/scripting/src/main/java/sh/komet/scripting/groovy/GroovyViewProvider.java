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
package sh.komet.scripting.groovy;

import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.control.property.ActivityFeed;
import sh.komet.gui.control.property.ViewProperties;
import sh.komet.gui.interfaces.ExplorationNodeAbstract;
import sh.komet.scripting.ScriptingController;

/**
 *
 * @author kec
 */
public class GroovyViewProvider extends ExplorationNodeAbstract {
    {
        toolTipProperty.setValue("Groovy runner");
        super.getTitle().setValue("Groovy");
        menuIconProperty.setValue(Iconography.JAVASCRIPT.getIconographic());
    }
   private final ScriptingController scriptingController;
   private final Node titleNode = Iconography.JAVASCRIPT.getIconographic();


    public GroovyViewProvider(ViewProperties viewProperties) {
        super(viewProperties);
       try {
           FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/scripting/scriptrunner.fxml"));
           loader.load();
           this.scriptingController = loader.getController();
           this.scriptingController.setEngine("groovy");
           this.scriptingController.setScript("import sh.isaac.api.Get\n"
                   + "println(Get.conceptDescriptionText(-2147483643))");
       } catch (IOException ex) {
           throw new RuntimeException(ex);
       }
    }

    @Override
    public Node getMenuIconGraphic() {
        return Iconography.JAVASCRIPT.getIconographic();
    }

    @Override
    public void revertPreferences() {

    }

    @Override
    public void savePreferences() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Node getNode() {
        return scriptingController.getTopPane();
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleNode);
    }


    @Override
    public void close() {
        // nothing to do...
    }
    @Override
    public ActivityFeed getActivityFeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canClose() {
        return true;
    }
}
