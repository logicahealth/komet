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



package org.ihtsdo.otf.tcc.ddo.context;

//~--- non-JDK imports --------------------------------------------------------

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.scene.Node;

import org.ihtsdo.otf.tcc.ddo.concept.ConceptChronicleDdo;
import org.ihtsdo.ttk.lookup.Looker;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

/**
 *
 * @author kec
 */
public class TerminologyContext {

   /** Field description */
   private final ObjectProperty<ConceptChronicleDdo> context = new SimpleObjectProperty<>(this, "context");

   /** Field description */
   private String contextName;

   /** Field description */
   private String windowName;

   /** Field description */
   private Node graphic;

   /**
    * Constructs ...
    *
    *
    * @param contextName
    * @param windowName
    * @param graphic
    */
   public TerminologyContext(String contextName, String windowName, Node graphic) {
      this.contextName = contextName;
      this.windowName  = windowName;
      this.graphic     = graphic;
      Looker.add(this, UUID.randomUUID(), windowName + ": " + contextName);
   }

   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public String toString() {
      return "TerminologyContext{" + windowName + ": " + contextName + "=" + context.getValue() + '}';
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public ConceptChronicleDdo getContext() {
      return context.get();
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public String getContextName() {
      return contextName;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public ObjectProperty<ConceptChronicleDdo> getContextProperty() {
      return context;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public Node getGraphic() {
      return graphic;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public String getWindowName() {
      return windowName;
   }
}
