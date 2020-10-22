package sh.isaac.solor.direct.rxnorm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.component.concept.ConceptBuilder;
import sh.isaac.api.component.concept.ConceptSpecification;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.coordinate.WriteCoordinate;
import sh.isaac.api.logic.LogicalExpression;
import sh.isaac.api.logic.LogicalExpressionBuilder;
import sh.isaac.api.logic.assertions.Assertion;
import sh.isaac.api.logic.assertions.ConceptAssertion;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT3Generator;
import sh.isaac.api.util.UuidT5Generator;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static sh.isaac.solor.direct.rxnorm.RxNormDomImporter.childElements;

public class RxNormClassHandler {
    private static final Logger LOG = LogManager.getLogger();

    private static final ConcurrentSkipListSet<String> forwardReferences =  new ConcurrentSkipListSet<>();


    public static UUID aboutToUuid(String aboutString) {
        // TODO: why "http://snomed.info/id/null" ?
        if (aboutString.equals("http://snomed.info/id/null")) {
            return TermAux.UNINITIALIZED_COMPONENT_ID.getPrimordialUuid();
        }
        if (aboutString.startsWith("http://snomed.info/id/")) {
            aboutString = aboutString.substring("http://snomed.info/id/".length());
            return UuidT3Generator.fromSNOMED(aboutString);
        }

        return UuidT5Generator.get(aboutString);
    }

    public static int aboutToNid(String aboutString) {
        if (aboutString == null || aboutString.isBlank()) {
            throw new IllegalStateException("aboutString cannot be null or blank.");
        }
        try {
            return Get.nidForUuids(aboutToUuid(aboutString));
        } catch (NoSuchElementException e) {
            forwardReferences.add(aboutString);
            return Get.nidWithAssignment(aboutToUuid(aboutString));
        }
    }

    private HashMap<String, String> equivalentClassMap = new HashMap<>();
    HashSet<String> childTags = new HashSet<>();
    ChildTagSet equivalentClassChildTags = new ChildTagSet("equivalentClass");
    ChildTagSet classInEquivalentClassChildTags = new ChildTagSet("Class in equivalentClass");
    ChildTagSet intersectionOfChildTags = new ChildTagSet("intersectionOf");

    final Transaction transaction;
    final ManifoldCoordinate manifoldCoordinate;

    public RxNormClassHandler(Transaction transaction, ManifoldCoordinate manifoldCoordinate) {
        this.transaction = transaction;
        this.manifoldCoordinate = manifoldCoordinate;
    }

    public void handleTopClass(Element topClassElement) {
        List<Element> equivalentClassElements = new ArrayList<>();
        List<Element> subClassElements = new ArrayList<>();
        List<Element> assemblageElements = new ArrayList<>();

        String rdfAbout = topClassElement.getAttribute("rdf:about");
        if (equivalentClassMap.containsKey(rdfAbout)) {
            // Essentially a duplicate concept definition, here encountering the second one...
            // Nothing to do at the moment... So warn and return.
            LOG.warn("Equivalent class encountered for: " + rdfAbout);
            return;
        }


        UUID conceptPrimordialUuid = aboutToUuid(rdfAbout);
        if (rdfAbout.startsWith("http://snomed.info/id/")) {
            if (!rdfAbout.contains("Rx")) {
                // Verify a stated definition.
                if (Get.identifierService().hasUuid(conceptPrimordialUuid)) {
                    int nid = Get.identifierService().getNidForUuids(conceptPrimordialUuid);
                    List<SemanticChronology> chronologies = Get.assemblageService().getSemanticChronologiesForComponent(nid);
                    if (!chronologies.isEmpty()) {
                        return; // Stop processing if SNOMED concept, they are already imported and have a stated definition...
                    }
                }
            }
        }
        String conceptName = null;

        boolean newConcept = true;

        List<Element> childElements = childElements(topClassElement);
        if (childElements.isEmpty()) {
            // as an example: <Class rdf:about="http://snomed.info/id/105590001"/>
            // 105590001 |Substance (substance)|
            newConcept = false;
            if (!Get.identifierService().hasUuid(conceptPrimordialUuid)) {
                LOG.error("Reference to existing concept: " + rdfAbout + " " + conceptPrimordialUuid + " does not exist...");
            }
        }

        for (Element childElement : childElements) {
            childTags.add(childElement.getTagName());

            switch (childElement.getTagName()) {
                case "deprecated--equivalentClass":
                case "deprecated--owl:equivalentClass":
                    // a sufficient set?
                    if (childElements(childElement).isEmpty()) {
/* Example with no children, but an rdf:resource

    <Class rdf:about="https://mor.nlm.nih.gov/Rx108088">
        <rdfs:subClassOf rdf:resource="http://snomed.info/id/105590001"/> ## Adds subclass of Substance
        <rdfs:label>Alclometasone</rdfs:label>                            ## Already a SNOMED description
    </Class>

    <Class rdf:about="http://snomed.info/id/395956000">
        <equivalentClass rdf:resource="https://mor.nlm.nih.gov/Rx108088"/>
    </Class>
 */

/*
Another example to handle...
    <Class rdf:about="http://snomed.info/id/4000030-FS">
        <equivalentClass rdf:resource="http://snomed.info/id/732981002"/>
        <rdfs:subClassOf rdf:resource="http://snomed.info/id/258681007"/>
        <rdfs:label>ACTUAT</rdfs:label>
    </Class>
 */
                        String rdfResource = childElement.getAttribute("rdf:resource");
                        equivalentClassMap.put(rdfAbout, rdfResource);
                        equivalentClassMap.put(rdfResource, rdfAbout);
                        UUID alternativeUuid = aboutToUuid(rdfResource);
                        if (Get.identifierService().hasUuid(conceptPrimordialUuid)) {
                            try {
                                Get.identifierService().addUuidForNid(alternativeUuid, Get.nidForUuids(conceptPrimordialUuid));
                            } catch (IllegalArgumentException e) {
                                LOG.error(e.getLocalizedMessage());
                            }
                        } else if (Get.identifierService().hasUuid(alternativeUuid)) {
                            Get.identifierService().addUuidForNid(conceptPrimordialUuid, Get.nidForUuids(alternativeUuid));
                        } else {
                            LOG.error("No entry for: " + rdfAbout + " uuid: " + conceptPrimordialUuid);
                        }

                        newConcept = false;
                    } else {
                        equivalentClassElements.add(childElement);
                    }
                    break;

                case "rdfs:label":
                    // This is the description of the concept...
                    conceptName = childElement.getTextContent();
                    break;

                    // Change to treat equivalent classes as subclass relationships.
                    // This example is a canonical reason (for saying it is equivalent to more than one concept):
                /*
    <!-- http://snomed.info/id/Rx119246 -->

    <owl:Class rdf:about="http://snomed.info/id/Rx119246">
        <owl:equivalentClass rdf:resource="http://snomed.info/id/120709004"/>
        <owl:equivalentClass rdf:resource="http://snomed.info/id/767467006"/>
        <rdfs:subClassOf rdf:resource="http://snomed.info/id/105590001"/>
        <id:MapsToCode>120709004</id:MapsToCode>
        <id:MapsToCode>767467006</id:MapsToCode>
        <id:MapsToName>Respiratory syncytial virus antibody (substance)</id:MapsToName>
        <id:MapsToName>Respiratory syncytial virus immune globulin (substance)</id:MapsToName>
        <rdfs:label>respiratory syncytial virus immune globulin intravenous</rdfs:label>
    </owl:Class>
                 */
                case "equivalentClass":
                case "owl:equivalentClass":
                    equivalentClassElements.add(childElement);
                    break;
                case "rdfs:subClassOf":
                    // for the necessary set...
                    subClassElements.add(childElement);
                    break;

                case "id:ActiveIngDifferent":
                case "id:DoseFormDifferent":
                case "id:VetOnly":
                case "id:HasNDC":
                case "id:SubstanceNotExist":
                case "id:IsVaccine":
                case "id:HasYarExCUI":
                case "id:BossSubstanceDifferent":
                case "id:Allergenic":
                case "id:ValuesDifferent":
                case "id:Asserted":
                case "id:IsPrescribable":
                case "id:Inferred":
                case "id:UnitsDifferent":
                case "id:MapsToCode":
                case "id:MapsToName":
                case "id:PresUnitDifferent":
                case "id:CountOfBaseDifferent":
                case "skos:prefLabel":
                case "skos:altLabel":
                case "skos:definition":

                    assemblageElements.add(childElement);
                    break;

                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }

        if (newConcept) {
            forwardReferences.remove(rdfAbout);
            LogicalExpressionBuilder expressionBuilder = Get.logicalExpressionBuilderService().getLogicalExpressionBuilder();

            for (Element equivalentClass: equivalentClassElements) {
                try {
                    handleEquivalentClass(expressionBuilder, equivalentClass);
                } catch (IllegalStateException e) {
                    LOG.error(e);
                }
            }
            handleNecessarySet(expressionBuilder, subClassElements);

            if (conceptName == null) {
                LOG.error("No concept name for: " + topClassElement + " " + rdfAbout);
            }


            LogicalExpression logicalExpression = expressionBuilder.build();

            ConceptBuilder conceptBuilder = Get.conceptBuilderService().getDefaultConceptBuilder(conceptName, "RxNorm", logicalExpression, TermAux.SOLOR_CONCEPT_ASSEMBLAGE.getAssemblageNid());
            conceptBuilder.setPrimordialUuid(conceptPrimordialUuid);
            for (Element assemblageElement: assemblageElements) {
                switch (assemblageElement.getTagName()) {
                    case "id:HasYarExCUI":
                        addStringSemantic(conceptBuilder, MetaData.RXNORM_CUI____SOLOR, assemblageElement.getTextContent());
                        break;

                    case "id:ActiveIngDifferent":
                        /*
<id:ActiveIngDifferent>missing Rx AI in BoSS Carbon Dioxide : 2034</id:ActiveIngDifferent>
<id:ActiveIngDifferent>missing Rx AI in BoSS Oxygen : 7806</id:ActiveIngDifferent>
                         */
                        addStringSemantic(conceptBuilder, MetaData.ACTIVE_INGREDIENT_IS_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;

                    case "id:DoseFormDifferent":
/*
        <id:DoseFormDifferent>cannot map SCT manufactured dose form</id:DoseFormDifferent>
 */
                        addStringSemantic(conceptBuilder, MetaData.DOSE_FORM_IS_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;

                    case "id:VetOnly":
/*
        <id:VetOnly>true</id:VetOnly>
 */
                    if (assemblageElement.getTextContent().equalsIgnoreCase("true")) {
                        addMembershipSemantic(conceptBuilder, MetaData.VETERINARY_MEDICINE_ONLY____SOLOR);
                    } else {
                        LOG.error("Unexpected value: " + assemblageElement.getTextContent() + " for <id:VetOnly> in " + rdfAbout);
                    }
                    break;

                    case "id:HasNDC":
/*
       <id:HasNDC>false</id:HasNDC>

 */
                        addStringSemantic(conceptBuilder, MetaData.NDC_CODES_AVAILABLE____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:SubstanceNotExist":
/*
        <id:SubstanceNotExist>substance does not exist in SCT</id:SubstanceNotExist>
 */
                        addStringSemantic(conceptBuilder, MetaData.SUBSTANCE_DOES_NOT_EXIST____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:IsVaccine":
/*
        <id:IsVaccine>true</id:IsVaccine>
 */
                        addMembershipSemantic(conceptBuilder, MetaData.VACCINE____SOLOR);
                        break;
                    case "id:BossSubstanceDifferent":
/*
       <id:BossSubstanceDifferent>ing not found in RxNorm</id:BossSubstanceDifferent>
 */
                        addStringSemantic(conceptBuilder, MetaData.BOSS_SUBSTANCES_ARE_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;


                    case "id:Allergenic":
/*
        <id:Allergenic>true</id:Allergenic>
 */
                        if (assemblageElement.getTextContent().equalsIgnoreCase("true")) {
                            addMembershipSemantic(conceptBuilder, MetaData.ALLERGEN____SOLOR);
                        } else {
                            LOG.error("Unexpected value: " + assemblageElement.getTextContent() + " for <id:Allergenic> in " + rdfAbout);
                        }
                        break;

                    case "id:ValuesDifferent":
/*
        <id:ValuesDifferent>no SCT number class</id:ValuesDifferent>
 */
                        addStringSemantic(conceptBuilder, MetaData.VALUES_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;

                    case "id:Asserted":
/*
        <id:Asserted>true</id:Asserted>
        <id:Asserted>false</id:Asserted>
 */
                        addStringSemantic(conceptBuilder, MetaData.RXNORM_ASSERTED____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:IsPrescribable":
/*
       <id:IsPrescribable>false</id:IsPrescribable>
       <id:IsPrescribable>true</id:IsPrescribable>
 */
                        addStringSemantic(conceptBuilder, MetaData.PRESCRIBABLE____SOLOR, assemblageElement.getTextContent());
                        break;

                    case "id:Inferred":
//         <id:Inferred>true</id:Inferred>
                        addStringSemantic(conceptBuilder, MetaData.RXNORM_INFERRED____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:UnitsDifferent":
//         <id:UnitsDifferent>sct: Capsule (unit of presentation)</id:UnitsDifferent>
                        addStringSemantic(conceptBuilder, MetaData.UNITS_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:MapsToCode":
//        <id:MapsToCode>703368006</id:MapsToCode>
                        addStringSemantic(conceptBuilder, MetaData.MAPS_TO_CODE____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:MapsToName":
//         <id:MapsToName>Medroxyprogesterone acetate (substance)</id:MapsToName>
                        addStringSemantic(conceptBuilder, MetaData.MAPS_TO_NAME____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:PresUnitDifferent":
//        <id:PresUnitDifferent>sct: Tablet (unit of presentation)</id:PresUnitDifferent>
                        addStringSemantic(conceptBuilder, MetaData.PRESENTATION_UNIT_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "id:CountOfBaseDifferent":
//         <id:CountOfBaseDifferent>sct: 1</id:CountOfBaseDifferent>
                        addStringSemantic(conceptBuilder, MetaData.COUNT_OF_BASE_DIFFERENT____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "skos:prefLabel":
                        addStringSemantic(conceptBuilder, MetaData.SKOS_PREFERRED_LABEL____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "skos:altLabel":
                        addStringSemantic(conceptBuilder, MetaData.SKOS_ALTERNATE_LABEL____SOLOR, assemblageElement.getTextContent());
                        break;
                    case "skos:definition":
                        addStringSemantic(conceptBuilder, MetaData.SKOS_DEFINITION____SOLOR, assemblageElement.getTextContent());
                        break;
                    default:
                        LOG.error("Can't handle: " + assemblageElement.getTagName() + " in " + rdfAbout);

                }

            }

            WriteCoordinate writeCoordinate = this.manifoldCoordinate.getWriteCoordinate(this.transaction);
            ArrayList<Chronology> buildObjects = new ArrayList<>();
            conceptBuilder.buildAndWrite(writeCoordinate, buildObjects);
        }

    }

    private void addStringSemantic(ConceptBuilder conceptBuilder, ConceptSpecification assemblage, String value) {
        conceptBuilder.addStringSemantic(value, assemblage);
    }

    private void addMembershipSemantic(ConceptBuilder conceptBuilder,ConceptSpecification assemblage) {
        conceptBuilder.addAssemblageMembership(assemblage);
    }

    private void handleNecessarySet(LogicalExpressionBuilder eb, List<Element> subClassElements) {
        List<Assertion> subclasses = new ArrayList<Assertion>();
        for (int i = 0; i < subClassElements.size(); i++) {
            Element subclass = subClassElements.get(i);
            String subclassAttribute = subclass.getAttribute("rdf:resource");
            if (subclassAttribute == null || subclassAttribute.isEmpty()) {
                NodeList subClassChildren = subclass.getChildNodes();
                for (int j = 0; j < subClassChildren.getLength(); j++) {
                    Node possibleClass = subClassChildren.item(j);
                    if (possibleClass.getNodeName().contains("Class")) {
                        NodeList classChildren = possibleClass.getChildNodes();
                        for (int k = 0; k < classChildren.getLength(); k++) {
                            Node possibleIntersectionOf = classChildren.item(k);
                            if (possibleIntersectionOf.getNodeName().contains("intersectionOf")) {
                                NodeList intersectionOfChildren = possibleIntersectionOf.getChildNodes();
                                for (int l = 0; l < intersectionOfChildren.getLength(); l++) {
                                    Node possibleDescription = intersectionOfChildren.item(l);
                                    if (possibleDescription.getNodeName().contains("Description")) {
                                        String rdfAbout = possibleDescription.getAttributes().getNamedItem("rdf:about").getNodeValue();
                                        ConceptAssertion subclassAssertion = eb.conceptAssertion(aboutToNid(rdfAbout));
                                        subclasses.add(subclassAssertion);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                subclasses.add(eb.conceptAssertion(aboutToNid(subclassAttribute)));
            }
        }
        eb.necessarySet(eb.and(subclasses.toArray(new Assertion[subclasses.size()])));
    }

    private void handleEquivalentClass(LogicalExpressionBuilder eb, Element element) {
        equivalentClassChildTags.processElement(element);
        for (Element childElement : childElements(element)) {
            switch (childElement.getTagName()) {
                case "Class":
                case "owl:Class":
                    handleClassInEquivalentClass(eb, childElement);
                    break;

                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }
    }

    private Assertion handleRestrictionClass(LogicalExpressionBuilder eb, Element element) {
        List<String> cantHandleElementList = new ArrayList<>();
        for (Element childElement : childElements(element)) {
            switch (childElement.getTagName()) {
                case "Class":
                case "owl:Class":
                    return handleClassInRestriction(eb, childElement);

                default:
                    cantHandleElementList.add(childElement.getTagName());
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }

        throw new IllegalStateException("Expecting Class element. Found elements: " + cantHandleElementList);
    }
    private Assertion handleClassInRestriction(LogicalExpressionBuilder eb, Element element) {
        for (Element childElement : childElements(element)) {
            switch (childElement.getTagName()) {
                case "intersectionOf":
                case "owl:intersectionOf":
                    return eb.and(handleIntersectionOf(eb, childElement));
                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }
        throw new IllegalStateException("Expecting intersectionOf element. ");
    }

    private void handleClassInEquivalentClass(LogicalExpressionBuilder eb, Element element) {
        classInEquivalentClassChildTags.processElement(element);
        for (Element childElement : childElements(element)) {
            switch (childElement.getTagName()) {
                case "owl:intersectionOf":
                case "intersectionOf":
                    eb.sufficientSet(eb.and(handleIntersectionOf(eb, childElement)));
                    break;
                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }
    }
    private Assertion[] handleIntersectionOf(LogicalExpressionBuilder eb, Element element) {
        List<Element> childElements = childElements(element);
        ArrayList<Assertion> assertionList = new ArrayList<>(childElements.size());
        intersectionOfChildTags.processElement(element);
        for (Element childElement : childElements) {
            switch (childElement.getTagName()) {
                case "Restriction":
                case "owl:Restriction":
                    // some restriction
                    assertionList.add(handleRestriction(eb, childElement));
                    break;

                case "rdf:Description":
                    // is-a concept
                    String rdfAbout = childElement.getAttribute("rdf:about");
                    assertionList.add(eb.conceptAssertion(aboutToNid(rdfAbout)));
                    break;

                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }
        return assertionList.toArray(new Assertion[assertionList.size()]);
    }

    private Assertion handleRestriction(LogicalExpressionBuilder eb, Element element) {
        OptionalInt roleTypeNid = OptionalInt.empty();
        Optional<Assertion> roleRestriction = Optional.empty();
        for (Element childElement : childElements(element)) {
            String rdfResource = childElement.getAttribute("rdf:resource");
            switch (childElement.getTagName()) {
                case "onProperty":
                case "owl:onProperty":
                    // some restriction. Always has an rdf:resource:
                    // <onProperty rdf:resource="http://snomed.info/id/732943007"/>
                    roleTypeNid = OptionalInt.of(aboutToNid(rdfResource));
                    break;

                case "someValuesFrom":
                case "owl:someValuesFrom":
                    // someValuesFrom either has a child of <Class>, or an rdf:resource="http://snomed.info/id/609096000"
                    if (rdfResource.isBlank()) {
                        // should have a child <Class>
                        roleRestriction = Optional.of(handleRestrictionClass(eb, childElement));
                    } else {
                        // is-a concept
                        roleRestriction = Optional.of(eb.conceptAssertion(aboutToNid(rdfResource)));
                    }
                    break;

                default:
                    LOG.error("Can't handle: " + childElement.getTagName());
            }
        }
        if (roleTypeNid.isEmpty() || roleRestriction.isEmpty()) {
            throw new IllegalStateException("Missing data for restriction: " + roleTypeNid + " " + roleRestriction);
        }
        return eb.someRole(roleTypeNid.getAsInt(), roleRestriction.get());
    }

    public void report() {
        LOG.info(equivalentClassChildTags);
        LOG.info(classInEquivalentClassChildTags);
        LOG.info(intersectionOfChildTags);
        LOG.info("Unresolved forward references: " + forwardReferences);
    }

}
