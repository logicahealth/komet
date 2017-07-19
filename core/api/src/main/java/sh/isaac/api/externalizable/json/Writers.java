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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Writer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import com.cedarsoftware.util.io.JsonWriter;

import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.component.sememe.version.DynamicSememe;
import sh.isaac.api.component.sememe.version.SememeVersion;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.api.component.sememe.version.ComponentNidVersion;
import sh.isaac.api.component.sememe.version.LogicGraphVersion;
import sh.isaac.api.component.sememe.version.LongVersion;
import sh.isaac.api.component.sememe.version.StringVersion;

//~--- classes ----------------------------------------------------------------

/**
 * {@link Writers}
 *
 * An experimental class that doesn't work yet due to upstream bugs.
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
         output.write("\"conceptSequence\":\"");
         output.write(cc.getConceptSequence() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"uuidList\":[");
         mainWriter.tabIn();

         final StringBuilder temp = new StringBuilder();

         for (final UUID uuid: cc.getUuidList()) {
            temp.append("\"" + uuid);
            temp.append("\", ");
         }

         if (temp.length() > 2) {
            temp.setLength(temp.length() - 2);
         }

         output.write(temp.toString());
         mainWriter.tabOut();
         output.write("]");
      }
   }


   /**
    * This representation of a concept is only intended to aid in debugging, it is not rich enough to represent all of the data.
    */
   public static class SememeChronologyJsonWriter
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
         @SuppressWarnings("unchecked")
         final SememeChronology<SememeVersion> sc         = (SememeChronology<SememeVersion>) obj;
         final JsonWriter                         mainWriter = Support.getWriter(args);

         output.write("\"sememeType\":\"");
         output.write(sc.getSememeType()
                        .name());
         output.write("\",");
         mainWriter.newLine();
         output.write("\"nid\":\"");
         output.write(sc.getNid() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"sememeSequence\":\"");
         output.write(sc.getSememeSequence() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"uuidList\":[");
         mainWriter.tabIn();

         final StringBuilder temp = new StringBuilder();

         for (final UUID uuid: sc.getUuidList()) {
            temp.append("\"" + uuid);
            temp.append("\", ");
         }

         if (temp.length() > 2) {
            temp.setLength(temp.length() - 2);
         }

         output.write(temp.toString());
         mainWriter.tabOut();
         output.write("],");
         mainWriter.newLine();
         output.write("\"assemblageSequence\":\"");
         output.write(sc.getAssemblageSequence() + "");
         output.write("\",");
         mainWriter.newLine();
         output.write("\"referencedComponentNid\":\"");
         output.write(sc.getReferencedComponentNid() + "");
         output.write("\",");

         @SuppressWarnings("unchecked")
         final List<SememeVersion> versions = (List<SememeVersion>) sc.getVersionList();

         mainWriter.newLine();
         output.write("\"versions\":[");
         mainWriter.tabIn();

         boolean first = true;

         for (final SememeVersion sv: versions) {
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

            if (sv instanceof DescriptionVersion) {
               final DescriptionVersion ds = (DescriptionVersion) sv;

               output.write("\"caseSignificanceSequence\":\"");
               output.write(ds.getCaseSignificanceConceptSequence() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"languageConceptSequence\":\"");
               output.write(ds.getLanguageConceptSequence() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"descriptionTypeConceptSequence\":\"");
               output.write(ds.getDescriptionTypeConceptSequence() + "");
               output.write("\",");
               mainWriter.newLine();
               output.write("\"text\":\"");
               output.write(ds.getText() + "");
               output.write("\"");
            } else if (sv instanceof ComponentNidVersion<?>) {
               final ComponentNidVersion<?> cns = (ComponentNidVersion<?>) sv;

               output.write("\"componentNid\":\"");
               output.write(cns.getComponentNid() + "");
               output.write("\"");
            } else if (sv instanceof DynamicSememe<?>) {
               final DynamicSememe<?> ds = (DynamicSememe<?>) sv;

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

               for (final LogicNode necessaryOrSufficient: root.getChildren()) {
                  for (final LogicNode connector: necessaryOrSufficient.getChildren()) {
                     for (final LogicNode target: connector.getChildren()) {
                        if (target.getNodeSemantic() == NodeSemantic.CONCEPT) {
                           // Hack ALERT!
                           // This should look like this: Concept[1] ISAAC metadata (ISAAC) <14>
                           final String conceptString = target.toString();

                           if (conceptString.contains("<") && conceptString.contains(">")) {
                              output.write("\"parentConceptSequence\":\"");
                              output.write(conceptString.substring(conceptString.lastIndexOf('<') + 1,
                                    conceptString.lastIndexOf('>')));
                              output.write("\"");
                           } else {
                              output.write("\"logicGraph\":\"NOT_YET_REPRESENTED\"");
                           }
                        } else {
                           output.write("\"logicGraph\":\"NOT_YET_REPRESENTED\"");
                        }
                     }
                  }
               }
            } else if (sv instanceof LongVersion<?>) {
               final LongVersion<?> ls = (LongVersion<?>) sv;

               output.write("\"long\":\"");
               output.write(ls.getLongValue() + "");
               output.write("\"");
            } else if (sv instanceof StringVersion) {
               final StringVersion ss = (StringVersion) sv;

               output.write("\"string\":\"");
               output.write(ss.getString());
               output.write("\"");
            } else {
               // Sememe Version - no extra fields
            }

            mainWriter.tabOut();
            output.write("}");
         }

         mainWriter.tabOut();
         output.write("]");
      }
   }
}

