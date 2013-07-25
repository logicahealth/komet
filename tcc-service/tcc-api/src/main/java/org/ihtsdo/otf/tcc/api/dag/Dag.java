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



package org.ihtsdo.otf.tcc.api.dag;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

/**
 *
 * @author kec
 */
public class Dag {
   public static void depthFirstSearch(ViewCoordinate vc, ComponentVersionBI node, int refexNid)
           throws IOException, MalformedDagException {
      for (RefexVersionBI<?> edge : node.getAnnotationsActive(vc, refexNid)) {
         Collection<? extends RefexVersionBI<?>> children = edge.getAnnotationsActive(vc,
                                                               refexNid);

         if (children.size() != 1) {
            throw new MalformedDagException("Children count != 1: " + children);
         }

         depthFirstSearch(vc, children.iterator().next(), refexNid);
      }
   }
}
