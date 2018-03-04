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
package sh.komet.gui.provider.concept.detail.logic;

import java.util.EnumSet;
import java.util.function.Consumer;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.LookupService;
import sh.isaac.komet.iconography.Iconography;
import sh.komet.gui.contract.DetailNodeFactory;
import sh.komet.gui.contract.DetailType;
import sh.komet.gui.interfaces.DetailNode;
import sh.komet.gui.manifold.Manifold;

/**
 *
 * @author kec
 */
@Service(name = "Logic Detail Provider")
@RunLevel(value = LookupService.SL_L5_ISAAC_STARTED_RUNLEVEL)
public class LogicDetailProviderFactory implements DetailNodeFactory {

   @Override
   public EnumSet<DetailType> getSupportedTypes() {
      return EnumSet.of(DetailType.Concept);
   }

   @Override
   public DetailNode createDetailNode(Manifold manifold, Consumer<Node> nodeConsumer, DetailType type) {
      if (type != DetailType.Concept) {
         throw new UnsupportedOperationException("ak Can't handle: " + type); 
      }
      return new LogicDetailNode(manifold, nodeConsumer);
   }
   @Override
   public String getMenuText() {
      return "Logic Details"; 
   }

   @Override
   public Node getMenuIcon() {
      return Iconography.LAMBDA.getIconographic();
   }
   
}
