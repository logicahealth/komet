/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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
package gov.vha.isaac.ochre.api;

import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;

/**
 * Class for programatically creating and exporting a taxonomy.
 *
 * @author kec
 */
public class IsaacTaxonomy {

    private final TreeMap<String, ConceptBuilder> conceptBuilders = new TreeMap<>();
    private final List<SememeBuilder<?>> sememeBuilders = new ArrayList<>();
    private final List<ConceptBuilder> conceptBuildersInInsertionOrder = new ArrayList<>();
    private final Stack<ConceptBuilder> parentStack = new Stack<>();
    private ConceptBuilder current;
    private final ConceptSpecification isaTypeSpec;
    private final ConceptSpecification moduleSpec;
    private final ConceptSpecification pathSpec;
    private final ConceptSpecification authorSpec;
    private final String semanticTag;
    private final LanguageCode lang;

    public IsaacTaxonomy(ConceptSpecification path, ConceptSpecification author, ConceptSpecification module,
            ConceptSpecification isaType, String semanticTag, LanguageCode lang) {
        this.pathSpec = path;
        this.authorSpec = author;
        this.moduleSpec = module;
        this.isaTypeSpec = isaType;
        this.semanticTag = semanticTag;
        this.lang = lang;
    }

    protected final ConceptBuilder createConcept(ConceptSpecification specification) {
        ConceptBuilder builder = createConcept(specification.getConceptDescriptionText());
        builder.setPrimordialUuid(specification.getUuidList().get(0));
        builder.addUuids(specification.getUuidList().toArray(new UUID[0]));
        return builder;
    }

    protected final ConceptBuilder createConcept(String name) {
        checkConceptDescriptionText(name);

        if (parentStack.isEmpty()) {
            current = Get.conceptBuilderService().getDefaultConceptBuilder(name, semanticTag, null);
        } else {
            LogicalExpressionBuilderService expressionBuilderService
                    = LookupService.getService(LogicalExpressionBuilderService.class);
            LogicalExpressionBuilder defBuilder = expressionBuilderService.getLogicalExpressionBuilder();

            NecessarySet(And(ConceptAssertion(parentStack.lastElement(), defBuilder)));

            LogicalExpression logicalExpression = defBuilder.build();

            current = Get.conceptBuilderService().getDefaultConceptBuilder(name, semanticTag, logicalExpression);
        }

        conceptBuilders.put(name, current);
        conceptBuildersInInsertionOrder.add(current);

        return current;
    }

    private void checkConceptDescriptionText(String name) {
        if (conceptBuilders.containsKey(name)) {
            throw new RuntimeException("Concept is already added");
        }
    }

    protected final ConceptBuilder current() {
        return current;
    }

    protected final void popParent() {
        parentStack.pop();
    }

    protected final void pushParent(ConceptBuilder parent) {
        parentStack.push(parent);
    }

    protected void export(DataOutputStream dataOutputStream) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    protected void addPath(ConceptBuilder pathAssemblageConcept, ConceptBuilder pathConcept) {
        sememeBuilders.add(Get.sememeBuilderService().getMembershipSememeBuilder(pathConcept.getNid(),
                pathAssemblageConcept.getConceptSequence()));
    }

    public void exportJavaBinding(Writer out, String packageName,
            String className)
            throws IOException {
        out.append("package " + packageName + ";\n");
        out.append("\n\nimport gov.vha.isaac.ochre.api.component.concept.ConceptSpecification;\n");
        out.append("import gov.vha.isaac.ochre.api.ConceptProxy;\n");
        out.append("\n\npublic class " + className + " {\n");

        for (ConceptBuilder concept : conceptBuildersInInsertionOrder) {
            String preferredName = concept.getConceptDescriptionText();
            String constantName = preferredName.toUpperCase();

            if (preferredName.indexOf("(") > 0 || preferredName.indexOf(")") > 0) {
                throw new RuntimeException("The metadata concept '" + preferredName + "' contains parens, which is illegal.");
            }

            constantName = constantName.replace(" ", "_");
            constantName = constantName.replace("-", "_");
            constantName = constantName.replace("+", "_PLUS");
            constantName = constantName.replace("/", "_AND");
            out.append("\n\n   /** Java binding for the concept described as <strong><em>"
                    + preferredName
                    + "</em></strong>;\n    * identified by UUID: {@code \n    * "
                    + "<a href=\"http://localhost:8080/terminology/rest/concept/"
                    + concept.getPrimordialUuid()
                    + "\">\n    * "
                    + concept.getPrimordialUuid()
                    + "</a>}.*/");

            out.append("\n   public static ConceptSpecification " + constantName + " =");
            out.append("\n             new ConceptProxy(\"" + preferredName
                    + "\",\""
                    + concept.getPrimordialUuid().toString() + "\");");
        }

        out.append("\n}\n");
    }

    public void exportIBDF(Path exportFilePath) throws FileNotFoundException {
        long exportTime = System.currentTimeMillis();
        int stampSequence = Get.stampService().getStampSequence(State.ACTIVE, exportTime,
                authorSpec.getConceptSequence(),
                moduleSpec.getConceptSequence(),
                pathSpec.getConceptSequence());
        
        CommitService commitService = Get.commitService();
        SememeService sememeService = Get.sememeService();
        ConceptService conceptService = Get.conceptService();

        commitService.setComment(stampSequence, "Generated by maven from java sources");
        for (ConceptBuilder builder : conceptBuildersInInsertionOrder) {
            buildAndWrite(builder, stampSequence, conceptService, sememeService);
        }
        for (SememeBuilder<?> builder : sememeBuilders) {
            buildAndWrite(builder, stampSequence, conceptService, sememeService);
        }

        int stampAliasForPromotion = Get.stampService().getStampSequence(State.ACTIVE, exportTime + (1000 * 60),
                authorSpec.getConceptSequence(),
                moduleSpec.getConceptSequence(),
                pathSpec.getConceptSequence());
        
        commitService.addAlias(stampSequence, stampAliasForPromotion, "promoted by maven");
        try (BinaryDataWriterService writer = Get.binaryDataWriter(exportFilePath)) {
            conceptService.getConceptChronologyStream().forEach((conceptChronology) -> writer.put(conceptChronology));
            sememeService.getSememeChronologyStream().forEach((sememeChronology) -> writer.put(sememeChronology));
            commitService.getStampAliasStream().forEach((stampAlias) -> writer.put(stampAlias));
            commitService.getStampCommentStream().forEach((stampComment) -> writer.put(stampComment));
        }
    }

    private void buildAndWrite(IdentifiedComponentBuilder builder, int stampCoordinate, ConceptService conceptService, SememeService sememeService) throws IllegalStateException {
        List<?> builtObjects = new ArrayList<>();
        builder.build(stampCoordinate, builtObjects);
        builtObjects.forEach((builtObject) -> {
            if (builtObject instanceof ConceptChronology) {
                conceptService.writeConcept((ConceptChronology<? extends ConceptVersion<?>>) builtObject);
            } else if (builtObject instanceof SememeChronology) {
                sememeService.writeSememe((SememeChronology) builtObject);
            } else {
                throw new UnsupportedOperationException("Can't handle: " + builtObject);
            }
        });
    }

    public void exportJaxb(DataOutputStream out) throws Exception {
//        UuidDtoBuilder dtoBuilder = new UuidDtoBuilder(time,
//                authorSpec.getUuids()[0],
//                pathSpec.getUuids()[0],
//                moduleSpec.getUuids()[0]);
//
//        Marshaller marshaller = JaxbForDto.get().createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "not-generated-yet.xsd");
//
//        ArrayList<TtkConceptChronicle> taxonomyList = new ArrayList<>();
//        for (ConceptCB concept : conceptBpsInInsertionOrder) {
//            TtkConceptChronicle ttkConcept = dtoBuilder.construct(concept);
//            addDynamicSememes(ttkConcept);
//            taxonomyList.add(ttkConcept);
//        }
//
//        QName qName = new QName("taxonomy");
//        Wrapper wrapper = new Wrapper(taxonomyList);
//        JAXBElement<Wrapper> jaxbElement = new JAXBElement<>(qName,
//                Wrapper.class, wrapper);
//        marshaller.marshal(jaxbElement, out);

        for (ConceptBuilder builder : conceptBuildersInInsertionOrder) {

        }
        for (SememeBuilder<?> builder : sememeBuilders) {

        }

    }

}
