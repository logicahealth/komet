package sh.isaac.solor.direct.rxnorm;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;

public class ChildTagSet {
    HashSet<String> childTags = new HashSet<>();
    private final String setName;

    public ChildTagSet(String setName) {
        this.setName = setName;
    }

    public void processElement(Element element) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) currentNode;
                childTags.add(childElement.getTagName());
            }
        }
    }

    @Override
    public String toString() {
        return setName + " children: " + childTags;
    }
}
