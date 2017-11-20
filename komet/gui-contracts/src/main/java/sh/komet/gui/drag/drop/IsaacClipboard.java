/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.komet.gui.drag.drop;

//~--- JDK imports ------------------------------------------------------------
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

//~--- non-JDK imports --------------------------------------------------------
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import sh.isaac.api.chronicle.Version;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.externalizable.ByteArrayDataBuffer;
import sh.isaac.api.externalizable.IsaacExternalizable;
import sh.isaac.api.externalizable.StampUniversal;
import sh.isaac.api.identity.IdentifiedObject;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.SemanticVersion;

//~--- classes ----------------------------------------------------------------
/**
 *
 * @author kec
 */
public class IsaacClipboard
        extends ClipboardContent {

   public static final DataFormat ISAAC_CONCEPT = new DataFormat("application/isaac-concept");
   public static final DataFormat ISAAC_DESCRIPTION = new DataFormat("application/isaac-description");
   public static final DataFormat ISAAC_GRAPH = new DataFormat("application/isaac-graph");
   public static final DataFormat ISAAC_SEMEME = new DataFormat("application/isaac-sememe");
   public static final DataFormat ISAAC_CONCEPT_VERSION = new DataFormat("application/isaac-concept-version");
   public static final DataFormat ISAAC_DESCRIPTION_VERSION = new DataFormat("application/isaac-description-version");
   public static final DataFormat ISAAC_GRAPH_VERSION = new DataFormat("application/isaac-graph-version");
   public static final DataFormat ISAAC_SEMEME_VERSION = new DataFormat("application/isaac-sememe-version");
   
   public static final Set<DataFormat> CONCEPT_TYPES = new HashSet<>(Arrays.asList(ISAAC_CONCEPT, ISAAC_CONCEPT_VERSION));
   public static final Set<DataFormat> DESCRIPTION_TYPES = new HashSet<>(Arrays.asList(ISAAC_DESCRIPTION, ISAAC_DESCRIPTION_VERSION));
   public static final Set<DataFormat> GRAPH_TYPES = new HashSet<>(Arrays.asList(ISAAC_GRAPH, ISAAC_GRAPH_VERSION));
   public static final Set<DataFormat> SEMEME_TYPES = new HashSet<>(Arrays.asList(ISAAC_SEMEME, ISAAC_SEMEME_VERSION, 
           ISAAC_GRAPH, ISAAC_GRAPH_VERSION, ISAAC_DESCRIPTION, ISAAC_DESCRIPTION_VERSION));
   
   public static boolean containsAny(Collection<?> c1,
                               Collection<?> c2) {
      return !Collections.disjoint(c1, c2);
   }
   
   
   private static final HashMap<DataFormat, Function<? super IdentifiedObject, ? extends Object>> GENERATOR_MAP
           = new HashMap<>();

   //~--- static initializers -------------------------------------------------
   static {
      GENERATOR_MAP.put(
              DataFormat.HTML,
              (t) -> {
                 throw new UnsupportedOperationException();
              });
      GENERATOR_MAP.put(
              DataFormat.PLAIN_TEXT,
              (t) -> {
                 return t.getPrimordialUuid();
              });
   }

   //~--- constructors --------------------------------------------------------
   public IsaacClipboard(IdentifiedObject identifiedObject) {
      if (identifiedObject instanceof IsaacExternalizable) {
         IsaacExternalizable externalizableObject = (IsaacExternalizable) identifiedObject;
         ByteArrayDataBuffer dataBuffer = new ByteArrayDataBuffer();
         externalizableObject.putExternal(dataBuffer);
         

         if (identifiedObject instanceof ConceptChronology) {
            this.put(ISAAC_CONCEPT, ByteBuffer.wrap(dataBuffer.getData()));
         } else if (identifiedObject instanceof SemanticChronology) {
            SemanticChronology semanticChronology = (SemanticChronology) identifiedObject;

            if (null == semanticChronology.getVersionType()) {
               throw new IllegalStateException("SememeType cannot be null");
            } else {
               switch (semanticChronology.getVersionType()) {
                  case DESCRIPTION:
                     this.put(ISAAC_DESCRIPTION, ByteBuffer.wrap(dataBuffer.getData()));
                     break;

                  case LOGIC_GRAPH:
                     this.put(ISAAC_GRAPH, ByteBuffer.wrap(dataBuffer.getData()));
                     break;

                  default:
                     this.put(ISAAC_SEMEME, ByteBuffer.wrap(dataBuffer.getData()));
               }
            }
         }

         // Add in the conversions supported for this object.
         // addExtra(DataFormat.PLAIN_TEXT);
      } else if (identifiedObject instanceof Version) {
         ByteArrayDataBuffer dataBuffer = new ByteArrayDataBuffer();
         Version version = (Version) identifiedObject;
         StampUniversal universalStamp = new StampUniversal(version);
         universalStamp.writeExternal(dataBuffer);
         if (version instanceof ConceptVersion) {
            ConceptVersion conceptVersion = (ConceptVersion) version;
            conceptVersion.getChronology().putExternal(dataBuffer);
            this.put(ISAAC_CONCEPT_VERSION, ByteBuffer.wrap(dataBuffer.getData()));
         } else if (version instanceof SemanticVersion) {
            SemanticVersion sememeVersion = (SemanticVersion) version;
            sememeVersion.getChronology().putExternal(dataBuffer);
            switch (sememeVersion.getChronology().getVersionType()) {
               case DESCRIPTION:
                  this.put(ISAAC_DESCRIPTION_VERSION, ByteBuffer.wrap(dataBuffer.getData()));
                  break;
               case LOGIC_GRAPH:
                  this.put(ISAAC_GRAPH_VERSION, ByteBuffer.wrap(dataBuffer.getData()));
                  break;
               default:
                  this.put(ISAAC_SEMEME_VERSION, ByteBuffer.wrap(dataBuffer.getData()));
                  break;
                     
            }
            
         }
         
      }
   }

   //~--- methods -------------------------------------------------------------
   private void addExtra(DataFormat format, IdentifiedObject identifiedObject) {
      put(format, GENERATOR_MAP.get(format)
              .apply(identifiedObject));
   }
   
   
}
