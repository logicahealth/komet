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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.PremiseType;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.NodeSemantic;
import sh.isaac.api.util.DescriptionToToken;

/**
 *
 * @author kec
 */
public class DocBook {

    public static String getInlineEntry(ConceptSpecification concept,
                                        ManifoldCoordinate manifold) {
        boolean defined = isDefined(concept.getNid(), manifold);
        boolean multiParent = isMultiparent(concept.getNid(), manifold);
        String conceptChar;
        String conceptCharColor;
        String conceptUuid = concept.getPrimordialUuid().toString();
        if (defined) {
            if (multiParent) {
                conceptChar = "&#xF060; ";
                conceptCharColor = "#5ec200;";
            } else {
                conceptChar = "&#xF12F; ";
                conceptCharColor = "#5ec200;";
            }
        } else {
            if (multiParent) {
                conceptChar = "&#xF061; ";
                conceptCharColor = "#FF4E08;";
            } else {
                conceptChar = "&#xF2D8; ";
                conceptCharColor = "#FF4E08;";
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<link xlink:href=\"#ge_solor_");
        builder.append(DescriptionToToken.get(manifold.getPreferredDescriptionText(concept)));
        builder.append("_");
        builder.append(conceptUuid);
        builder.append("\"><inlinemediaobject>\n");
        builder.append("            <imageobject>\n");
        builder.append("                <imagedata>\n");
        builder.append("                    <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"126px\"\n");
        builder.append("                        height=\"14px\">\n");
        builder.append("                        <text x=\"1\" y=\"7\"\n");
        builder.append("                          style=\"font-size: 9pt; font-family: Open Sans Condensed Light, Symbol, Material Design Icons; baseline-shift: sub;\"\n");
        builder.append("                          >[<tspan dy=\"1.5\"\n");
        builder.append("                          style=\"font-family: Material Design Icons; fill: ");
        builder.append(conceptCharColor);
        builder.append(" stroke: ");
        builder.append(conceptCharColor);
        builder.append(" \"\n");
        builder.append("                          >");
        builder.append(conceptChar);
        builder.append("</tspan>\n");
        builder.append("                          <tspan dy=\"-1.5\"/>");
        builder.append(manifold.getPreferredDescriptionText(concept));
        builder.append("]</text>\n");
        builder.append("                    </svg>\n");
        builder.append("                </imagedata>\n");
        builder.append("            </imageobject>\n");
        builder.append("        </inlinemediaobject></link> \n");

        return builder.toString();
    }
    
    private static String makeGlossentry(ConceptSpecification concept,
                                         ManifoldCoordinate manifold, String definitionSvg) {
        StringBuilder builder = new StringBuilder();
        builder.append("<glossentry xml:id=\"ge_solor_");
        builder.append(DescriptionToToken.get(manifold.getPreferredDescriptionText(concept)));
        builder.append("_");
        builder.append(concept.getPrimordialUuid());
        builder.append("\">\n");
        builder.append("   ").append("<glossterm>");
        builder.append(manifold.getPreferredDescriptionText(concept));
        builder.append("</glossterm>\n");
        builder.append("   ").append("<glossdef>");
        builder.append("\n   <informaltable frame=\"topbot\" rowsep=\"0\" colsep=\"0\">");
        builder.append("\n   <?dbfo keep-together=\"always\" ?>");
        builder.append("\n      <tgroup cols=\"2\" align=\"left\">");
        builder.append("\n      <colspec colname=\"c1\" colnum=\"1\" colwidth=\"15pt\"/>");
        builder.append("\n      <colspec colname=\"c2\" colnum=\"2\" colwidth=\"260pt\"/>");
        builder.append("\n          <tbody>");
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Descriptions:</entry></row>");
        // add row for each description
        addDescriptions(builder, concept, manifold);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Codes:</entry></row>");
        // add row for each code
        addCodes(builder, concept, manifold);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Text definition:</entry></row>");
        // add row for each text definition
        addTextDefinition(builder, concept, manifold);
        builder.append("\n          <row><entry namest=\"c1\" nameend=\"c2\">Axioms:</entry></row>");
        builder.append(definitionSvg);
       builder.append("\n          </tbody>");
        builder.append("\n      </tgroup>");
        builder.append("\n   </informaltable>");
        builder.append("\n   </glossdef>");
        builder.append("\n</glossentry>");
        return builder.toString();
    }

    public static String getGlossentry(int conceptNid,
                                       ManifoldCoordinate manifold, String svgString) {
        return getGlossentry(Get.concept(conceptNid), manifold, svgString);
    }
    public static String getGlossentry(ConceptSpecification concept,
                                       ManifoldCoordinate manifold, String svgString) {
        StringBuilder builder = new StringBuilder();
            builder.append("\n          <row><entry/><entry>");
            builder.append(svgString);
            builder.append("\n          </entry></row>");
        return makeGlossentry(concept, manifold, builder.toString());

    }

    public static String getGlossentry(ConceptSpecification concept,
                                       ManifoldCoordinate manifold) {
        StringBuilder builder = new StringBuilder();
        addInferredDefinition(builder, Get.concept(concept), manifold);
        return makeGlossentry(concept, manifold, builder.toString());

    }

    private static void addDescriptions(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        List<SemanticChronology> descriptions = Get.concept(concept).getConceptDescriptionList();
        HashMap<Integer, DescriptionVersion> nidDescriptionVersionMap = new HashMap<>();
        for (SemanticChronology descriptionChronology: descriptions) {
            LatestVersion<DescriptionVersion> latestDescriptionVersion = descriptionChronology.getLatestVersion(manifold.getStampFilter());
            if (latestDescriptionVersion.isPresent()) {
                DescriptionVersion descriptionVersion = latestDescriptionVersion.get();
                if (descriptionVersion.getDescriptionTypeConceptNid() == TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid() 
                        || descriptionVersion.getDescriptionTypeConceptNid() == TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid()) {
                    nidDescriptionVersionMap.put(descriptionVersion.getNid(), descriptionVersion);
                }
            }
        }
        
        LatestVersion<DescriptionVersion> latestFQN = manifold.getFullyQualifiedDescription(concept);
        if (latestFQN.isPresent()) {
            DescriptionVersion fqn = latestFQN.get();
            addDescriptionText(builder, fqn.getText());
            nidDescriptionVersionMap.remove(fqn.getNid());
        }
        LatestVersion<DescriptionVersion> latestPreferredName = manifold.getPreferredDescription(concept);
        if (latestPreferredName.isPresent()) {
            DescriptionVersion name = latestPreferredName.get();
            addDescriptionText(builder, name.getText());
            nidDescriptionVersionMap.remove(name.getNid());
        }
        for (DescriptionVersion name: nidDescriptionVersionMap.values()) {
            addDescriptionText(builder, name.getText());
        }
    }

    private static void addDescriptionText(StringBuilder builder, String fullySpecifiedText) {
        builder.append("          ").append("<row><entry/><entry>");
        builder.append(fullySpecifiedText);
        builder.append("</entry></row>");
    }

    private static void addCodes(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        for (UUID uuid : Get.identifierService().getUuidArrayForNid(concept.getNid())) {
            builder.append("          ").append("<row><entry/><entry>UUID: ");
            builder.append(uuid.toString());
            builder.append("</entry></row>");
        }

        Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(concept.getNid(), TermAux.SNOMED_IDENTIFIER.getNid())
                .forEach(((semanticChronology) -> {
                    LatestVersion<StringVersion> latest = semanticChronology.getLatestVersion(manifold.getStampFilter());
                    if (latest.isPresent()) {
                        builder.append("          ").append("<row><entry/><entry>SCTID: ");
                        builder.append(latest.get().getString());
                        builder.append("</entry></row>");
                    }
                }));
    }

    private static void addTextDefinition(StringBuilder builder, ConceptSpecification concept, ManifoldCoordinate manifold) {
        LatestVersion<DescriptionVersion> definition = manifold.getLanguageCoordinate().getDefinitionDescription(Get.concept(concept).getConceptDescriptionList(), manifold.getStampFilter());
        if (definition.isPresent() && definition.get().getDescriptionTypeConceptNid() == TermAux.DEFINITION_DESCRIPTION_TYPE.getNid()) {
            addDescriptionText(builder, definition.get().getText());
        } else {
            addDescriptionText(builder, "Ø");
        }
        
    }

    private static void addStatedDefinition(StringBuilder builder, ConceptChronology concept, ManifoldCoordinate manifold) {
        LatestVersion<LogicGraphVersion> definition = concept.getLogicalDefinition(manifold.getStampFilter(), PremiseType.STATED, manifold.getLogicCoordinate());
        if (definition.isPresent()) {
            LogicGraphVersion logicGraph = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraph.getLogicalExpression().toSimpleString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }

    private static void addInferredDefinition(StringBuilder builder, ConceptChronology concept, ManifoldCoordinate manifold) {
        LatestVersion<LogicGraphVersion> definition = manifold.getLogicalDefinition(concept, PremiseType.INFERRED);
        if (definition.isPresent()) {
            LogicGraphVersion logicGraph = definition.get();
            builder.append("          ").append("<row><entry/><entry><literallayout><emphasis>");
            builder.append(logicGraph.getLogicalExpression().toSimpleString());
            builder.append("</emphasis></literallayout></entry></row>");
        }
    }

    public static boolean isDefined(int conceptNid, ManifoldCoordinate manifold) {
        Optional<LogicalExpression> conceptExpression = manifold.getLogicalExpression(conceptNid, PremiseType.STATED);
        if (!conceptExpression.isPresent()) {
            return false;
        }
        return conceptExpression.get().contains(NodeSemantic.SUFFICIENT_SET);
    }

    public static boolean isMultiparent(int conceptNid, ManifoldCoordinate manifold) {
        if (conceptNid == -1
                || conceptNid == TermAux.UNINITIALIZED_COMPONENT_ID.getNid()) {
            return false;
        }
        int[] parents = Get.taxonomyService().getSnapshot(manifold).getTaxonomyParentConceptNids(conceptNid);
        Optional<LogicalExpression> conceptExpression = manifold.getLogicalExpression(conceptNid, PremiseType.STATED);
        if (!conceptExpression.isPresent()) {
            return false;
        }
        return parents.length > 1;
    }
}
