/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the 
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
package sh.komet.gui.contract;

import java.util.EnumSet;
import java.util.function.Consumer;
import javafx.scene.Node;
import sh.komet.gui.manifold.Manifold;
import sh.komet.gui.interfaces.DetailNode;
import org.jvnet.hk2.annotations.Contract;

/**
 * A factory to enable creation of plug-in libraries to provide fx nodes, that represent a detail view of the 
 * {@code Manifold}'s focused object. 
 * @author kec
 */
@Contract
public interface DetailNodeFactory {
   
   EnumSet<DetailType> getSupportedTypes();
   
   /**
    * 
    * @param manifold the manifold that determines the current coordinates and focus. 
    * @param nodeConsumer function that will add the Node to the proper parent Node.  
    * @param type the type of the detail node to return
    * @return the detail node, after it has been added to the parent. 
    */
   DetailNode createDetailNode(Manifold manifold, Consumer<Node> nodeConsumer, DetailType type);
}
