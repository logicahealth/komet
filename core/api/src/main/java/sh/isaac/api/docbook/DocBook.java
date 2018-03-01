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
package sh.isaac.api.docbook;

import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.util.DescriptionToToken;

/**
 *
 * @author kec
 */
public class DocBook {

    public static String getGlossentry(ConceptSpecification concept,
            ManifoldCoordinate manifold) {
        ConceptChronology conceptChronology = Get.concept(concept);
        StringBuilder builder = new StringBuilder();
        builder.append("<glossentry xml:id=\"ge_solor_");
        builder.append(DescriptionToToken.get(manifold.getPreferredDescriptionText(concept)));
        builder.append("_");
        builder.append(concept.getPrimordialUuid());
        builder.append("\">\n");
        builder.append("   ").append("<glossterm><emphasis>[");
        builder.append(manifold.getPreferredDescriptionText(concept));
        builder.append("]</emphasis></glossterm>\n");
        builder.append("   ").append("<glossdef>");
        builder.append("   ").append("<informaltable frame=\"topbot\" rowsep=\"0\" colsep=\"0\">");
        builder.append("   ").append("<?dbfo keep-together=\"always\" ?>");
        builder.append("      ").append("<tgroup cols=\"2\" align=\"left\">");
        builder.append("      ").append("<colspec colname=\"c1\" colnum=\"1\" colwidth=\"15pt\"/>");
        builder.append("      ").append("<colspec colname=\"c2\" colnum=\"2\" colwidth=\"260pt\"/>");
        builder.append("          ").append("<tbody>");
        builder.append("          ").append("<row><entry namest=\"c1\" nameend=\"c2\">Descriptions:</entry></row>");
        // add row for each description
        addDescriptions(builder, concept, manifold);
        builder.append("          ").append("<row><entry namest=\"c1\" nameend=\"c2\">Codes:</entry></row>");
        // add row for each code
        addCodes(builder, concept, manifold);
        builder.append("          ").append("<row><entry namest=\"c1\" nameend=\"c2\">Text definition:</entry></row>");
        // add row for each text definition
        addTextDefinition(builder, concept, manifold);
        builder.append("          ").append("<row><entry namest=\"c1\" nameend=\"c2\">Stated definition:</entry></row>");
        // add row for each code
        addStatedDefinition(builder, conceptChronology, manifold);
        builder.append("          ").append("<row><entry namest=\"c1\" nameend=\"c2\">Inferred definition:</entry></row>");
        // add row for each code
        addInferredDefinition(builder, conceptChronology, manifold);

        builder.append("          ").append("</tbody>");
        builder.append("      ").append("</tgroup>");
        builder.append("   ").append("</informaltable>");
        builder.append("   ").append("</glossdef>");
        builder.append("</glossentry>");
        return builder.toString();
    }

    private static void addDescriptions(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        addDescriptionText(builder, manifold.getFullySpecifiedDescriptionText(concept));
        addDescriptionText(builder, manifold.getPreferredDescriptionText(concept));
    }

    private static void addDescriptionText(StringBuilder builder, String fullySpecifiedText) {
        builder.append("          ").append("<row><entry/><entry>");
        builder.append(fullySpecifiedText);
        builder.append("</entry></row>");
    }

    private static void addCodes(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        for (UUID uuid : Get.identifierService().getUuidArrayForNid(concept.getNid())) {
            builder.append("          ").append("<row><entry/><entry>");
            builder.append(uuid.toString());
            builder.append("</entry></row>");
        }

        Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(concept.getNid(), TermAux.SNOMED_IDENTIFIER.getNid())
                .forEach(((semanticChronology) -> {
                    LatestVersion<StringVersion> latest = semanticChronology.getLatestVersion(manifold);
                    if (latest.isPresent()) {
                        builder.append("          ").append("<row><entry/><entry>SCTID: ");
                        builder.append(latest.get().getString());
                        builder.append("</entry></row>");
                    }
                }));
    }

    private static void addTextDefinition(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        addDescriptionText(builder, "todo");
    }

    private static void addStatedDefinition(StringBuilder builder, ConceptChronology concept, ManifoldCoordinate manifold) {
        LatestVersion<LogicGraphVersion> definition = concept.getLogicalDefinition(manifold, PremiseType.STATED, manifold);
        if (definition.isPresent()) {
            LogicGraphVersion logicGraph = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraph.getLogicalExpression().toSimpleString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }

private static void addInferredDefinition(StringBuilder builder, ConceptChronology concept, ManifoldCoordinate manifold) {
        LatestVersion<LogicGraphVersion> definition = concept.getLogicalDefinition(manifold, PremiseType.INFERRED, manifold);
        if (definition.isPresent()) {
            LogicGraphVersion logicGraph = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraph.getLogicalExpression().toSimpleString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }
}
