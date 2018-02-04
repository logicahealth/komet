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
package sh.komet.javascript;

import java.io.IOException;
import java.util.Optional;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.interfaces.ExplorationNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
public class JavascriptViewProvider implements ExplorationNode {
   private Manifold manifold;
   private final SimpleStringProperty toolTipProperty = new SimpleStringProperty("javascript runner");
   private final SimpleStringProperty titleProperty = new SimpleStringProperty("javascript");
   private final JavascriptController javascriptController;
   private final Node titleNode = Iconography.JAVASCRIPT.getIconographic();
    public JavascriptViewProvider(Manifold manifold) {
       try {
           this.manifold = manifold;
           
           FXMLLoader loader = new FXMLLoader(
                 getClass().getResource("/sh/komet/javascript/scriptrunner.fxml"));
           loader.load();
           this.javascriptController = loader.getController();
       } catch (IOException ex) {
           throw new RuntimeException(ex);
       }
    }
    
    @Override
    public Manifold getManifold() {
        return this.manifold;
    }

    @Override
    public Node getNode() {
        return javascriptController.getTopPane();
    }

    @Override
    public ReadOnlyProperty<String> getToolTip() {
        return toolTipProperty;
    }

    @Override
    public ReadOnlyProperty<String> getTitle() {
        return titleProperty;
    }

    @Override
    public Optional<Node> getTitleNode() {
        return Optional.of(titleNode);
    }
    
}
