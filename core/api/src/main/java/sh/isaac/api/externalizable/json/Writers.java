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



package sh.isaac.api.externalizable.json;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.cedarsoftware.util.io.JsonWriter;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;

/**
 * {@link Writers}
 *
 * Some custom json writers for concepts and semantics
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Writers {
   /**
    * This representation of a concept is only intended to aid in debugging, it is not rich enough to represent all of the data.
    */
   public static class ConceptChronologyJsonWriter
            implements JsonWriter.JsonClassWriterEx {
      /**
       * Write.
       *
       * @param obj the obj
       * @param showType the show type
       * @param output the output
       * @param args the args
       * @throws IOException Signals that an I/O exception has occurred.
       * @see com.cedarsoftware.util.io.JsonWriter.JsonClassWriterEx#write(java.lang.Object, boolean, java.io.Writer, java.util.Map)
       */
      @Override
      public void write(Object obj, boolean showType, Writer output, Map<String, Object> args)
               throws IOException {
         final ConceptChronology cc         = (ConceptChronology) obj;
         final JsonWriter           mainWriter = Support.getWriter(args);

         output.write("\"nid\":\"");
         output.write(cc.getNid() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"uuidList\":[");
         mainWriter.tabIn();

         final StringBuilder temp = new StringBuilder();

         cc.getUuidList().stream().map((uuid) -> {
            temp.append("\"").append(uuid);
            return uuid;
         }).forEachOrdered((_item) -> {
            temp.append("\", ");
         });

         if (temp.length() > 2) {
            temp.setLength(temp.length() - 2);
         }

         output.write(temp.toString());
         mainWriter.tabOut();
         output.write("],");
         
         mainWriter.newLine();
         output.write("\"versions\":[");
         mainWriter.tabIn();
         
         boolean first = true;
         
         for (final Version sv: cc.getVersionList()) {
             if (first) {
                first = false;
                output.write("{");
             } else {
                output.write(",");
                mainWriter.newLine();
                output.write("{");
             }

             mainWriter.tabIn();

             if (showType) {
                output.write("\"@type\":\"");
                output.write(sv.getClass()
                               .getName());
                output.write("\",");
                mainWriter.newLine();
             }
             
             output.write("\"status\":\"");
             output.write(sv.getStatus().toString() + "");
             output.write("\",");
             mainWriter.newLine();
             output.write("\"time\":\"");
             output.write(ZonedDateTime.ofInstant(Instant.ofEpochMilli(sv.getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT));
             output.write("\"");
             mainWriter.tabOut();
             output.write("}");
         }
         mainWriter.tabOut();
         output.write("]");
       }
   }


   /**
    * This representation of a concept is only intended to aid in debugging, it is not rich enough to represent all of the data.
    */
   public static class SemanticChronologyJsonWriter
            implements JsonWriter.JsonClassWriterEx {
      /**
       * Write.
       *
       * @param obj the obj
       * @param showType the show type
       * @param output the output
       * @param args the args
       * @throws IOException Signals that an I/O exception has occurred.
       * @see com.cedarsoftware.util.io.JsonWriter.JsonClassWriterEx#write(java.lang.Object, boolean, java.io.Writer, java.util.Map)
       */
      @Override
      public void write(Object obj, boolean showType, Writer output, Map<String, Object> args)
               throws IOException {
         final SemanticChronology sc         = (SemanticChronology) obj;
         final JsonWriter                         mainWriter = Support.getWriter(args);

         output.write("\"semanticType\":\"");
         output.write(sc.getVersionType()
                        .name());
         output.write("\",");
         mainWriter.newLine();
         output.write("\"nid\":\"");
         output.write(sc.getNid() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"uuidList\":[");
         mainWriter.tabIn();

         final StringBuilder temp = new StringBuilder();

         sc.getUuidList().stream().map((uuid) -> {
            temp.append("\"").append(uuid);
            return uuid;
         }).forEachOrdered((_item) -> {
            temp.append("\", ");
         });

         if (temp.length() > 2) {
            temp.setLength(temp.length() - 2);
         }

         output.write(temp.toString());
         mainWriter.tabOut();
         output.write("],");
         mainWriter.newLine();
         output.write("\"assemblageNid\":\"");
         output.write(sc.getAssemblageNid() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"referencedComponentNid\":\"");
         output.write(sc.getReferencedComponentNid() + "");
         output.write("\",");

         final List<SemanticVersion> versions = sc.getVersionList();

         mainWriter.newLine();
         output.write("\"versions\":[");
         mainWriter.tabIn();

         boolean first = true;

         for (final SemanticVersion sv: versions) {
            if (first) {
               first = false;
               output.write("{");
            } else {
               output.write(",");
               mainWriter.newLine();
               output.write("{");
            }

            mainWriter.tabIn();

            if (showType) {
               output.write("\"@type\":\"");
               output.write(sv.getClass()
                              .getName());
               output.write("\",");
               mainWriter.newLine();
            }
            
            output.write("\"status\":\"");
            output.write(sv.getStatus().toString() + "");
            output.write("\",");
            mainWriter.newLine();
            output.write("\"time\":\"");
            output.write(ZonedDateTime.ofInstant(Instant.ofEpochMilli(sv.getTime()), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT));
            output.write("\",");
            mainWriter.newLine();

            if (sv instanceof DescriptionVersion) {
               final DescriptionVersion ds = (DescriptionVersion) sv;

               output.write("\"caseSignificanceNid\":\"");
               output.write(ds.getCaseSignificanceConceptNid() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"languageConceptNid\":\"");
               output.write(ds.getLanguageConceptNid() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"descriptionTypeConceptNid\":\"");
               output.write(ds.getDescriptionTypeConceptNid() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"text\":");
               mainWriter.write(ds.getText());
            } else if (sv instanceof ComponentNidVersion) {
               final ComponentNidVersion cns = (ComponentNidVersion) sv;

               output.write("\"componentNid\":\"");
               output.write(cns.getComponentNid() + "");
               output.write("\"");
            } else if (sv instanceof DynamicVersion) {
               final DynamicVersion ds = (DynamicVersion) sv;

               output.write("\"data\":\"");
               output.write(ds.dataToString());
               output.write("\"");
            } else if (sv instanceof LogicGraphVersion) {
               // A hack for the moment, to just write out the parent of the concept from the logic graph,
               // as that is often what is wanted for debugging.
               // TODO represent the entire logic graph in JSON?
               final LogicGraphVersion lgs  = (LogicGraphVersion) sv;
               final LogicalExpression   le   = lgs.getLogicalExpression();
               final LogicNode           root = le.getRoot();

               ArrayList<String> parents = new ArrayList<>();
               for (final LogicNode necessaryOrSufficient: root.getChildren()) {
                  for (final LogicNode connector: necessaryOrSufficient.getChildren()) {
                     for (final LogicNode target: connector.getChildren()) {
                        if (target.getNodeSemantic() == NodeSemantic.CONCEPT) {
                           // Hack ALERT!
                           // This should look like this: Concept[1] ISAAC metadata (ISAAC) <14>
                           final String conceptString = target.toString();
                           if (conceptString.contains("<") && conceptString.contains(">")) {
                              parents.add(conceptString.substring(conceptString.lastIndexOf('<') + 1,
                                    conceptString.lastIndexOf('>')));
                           } else {
                              output.write("\"logicGraph\":\"NOT_YET_REPRESENTED\"");
                           }
                        } else {
                           output.write("\"logicGraph\":\"NOT_YET_REPRESENTED\"");
                        }
                     }
                  }
               }
               
               if (parents.size() > 0)
               {
                  output.write("\"parentConceptNid\":[");
                  mainWriter.tabIn();
                  for (int i = 0; i < parents.size(); i++)
                  {
                     output.write("\"" + parents.get(i) + "\"");
                     if (i < parents.size() - 1) {
                        output.write(",");
                        mainWriter.newLine();
                     }
                  }
                  mainWriter.tabOut();
                  output.write("]");
               }
            } else if (sv instanceof LongVersion) {
               final LongVersion ls = (LongVersion) sv;

               output.write("\"long\":\"");
               output.write(ls.getLongValue() + "");
               output.write("\"");
            } else if (sv instanceof StringVersion) {
               final StringVersion ss = (StringVersion) sv;

               output.write("\"string\":\"");
               output.write(ss.getString());
               output.write("\"");
            } else {
               // Semantic Version - no extra fields
            }

            mainWriter.tabOut();
            output.write("}");
         }

         mainWriter.tabOut();
         output.write("]");
      }
   }
}

