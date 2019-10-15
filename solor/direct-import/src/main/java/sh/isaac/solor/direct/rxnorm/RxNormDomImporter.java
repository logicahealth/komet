package sh.isaac.solor.direct.rxnorm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.transaction.Transaction;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;


public class RxNormDomImporter {
    private static final Logger LOG = LogManager.getLogger();


    public static void importRxNorm(InputStream inputStream, Transaction transaction, EditCoordinate editCoordinate) {
        RxNormDomImporter rxNormDomImporter = new RxNormDomImporter(inputStream, transaction, editCoordinate);
        rxNormDomImporter.process();
    }

    int objectPropertyCount = 0;
    int annotationPropertyCount = 0;
    int classCount = 0;
    int rdfDescriptionCount = 0;

    final RxNormClassHandler rxNormClassHandler;
    final InputStream inputStream;

    public RxNormDomImporter(InputStream inputStream, Transaction transaction, EditCoordinate editCoordinate) {
        this.rxNormClassHandler = new RxNormClassHandler(transaction, editCoordinate);
        this.inputStream = inputStream;
    }

    public void process() {
        HashSet<String> tagNames = new HashSet<>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            Element element = doc.getDocumentElement();
            NodeList nodeList = element.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element) childNode;
                    tagNames.add(childElement.getTagName());
                    switch (childElement.getTagName()) {
                        case "Ontology":
                            // nothing to do...
                            break;

                        case "ObjectProperty":
                            handleObjectProperty(childElement);
                            break;

                        case "AnnotationProperty":
                            handleAnnotationProperty(childElement);
                            break;

                        case "Class":
                            handleClass(childElement);
                            break;

                        case "rdf:Description":
                            handleRdfDescription(childElement);
                            break;

                        default:
                            LOG.error("Can't handle:  " + childElement.getTagName());
                    }
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        LOG.info("tagNames: " + tagNames);
        LOG.info("Processed " + classCount + " classes, " + objectPropertyCount + " object properties, " +
                annotationPropertyCount + " annotation properties, " + rdfDescriptionCount + " rdf descriptions."
        );

        LOG.info("Child elements of Class:  " + rxNormClassHandler.childTags);
        rxNormClassHandler.report();
    }

    private void handleObjectProperty(Element element) {
        objectPropertyCount++;
        // verify it exists...
    }

    private void handleAnnotationProperty(Element element) {
        annotationPropertyCount++;
    }

    private void handleClass(Element element) {
        classCount++;
        try {
            rxNormClassHandler.handleTopClass(element);
        } catch (NoSuchElementException e) {
            LOG.error(e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRdfDescription(Element element) {
        rdfDescriptionCount++;
    }

    public static List<Element> childElements(Element element) {
        NodeList childNodes = element.getChildNodes();
        ArrayList<Element> childElements = new ArrayList<>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) currentNode);
            }
        }
        return childElements;
    }

}
