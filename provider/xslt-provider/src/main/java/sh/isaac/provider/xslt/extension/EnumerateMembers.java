package sh.isaac.provider.xslt.extension;


import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.value.SequenceType;
import sh.isaac.api.Get;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import net.sf.saxon.trans.XPathException;

import javax.xml.stream.XMLStreamException;
import java.util.UUID;

public class EnumerateMembers extends ExtensionFunctionDefinition {

    final ManifoldCoordinateImmutable manifoldCoordinate;

    public EnumerateMembers(ManifoldCoordinateImmutable manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName("", "http://xslt.solor.io", "enumerateElements");
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] { SequenceType.SINGLE_STRING };
    }

    @Override
    public SequenceType getResultType(SequenceType[] sequenceTypes) {
        return SequenceType.NODE_SEQUENCE;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {
            @Override
            public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {

                try{
                    String assemblageUuidStr = arguments[0].head().getStringValue();
                    int assemblageNid = Get.nidForUuids(UUID.fromString(assemblageUuidStr));
                    int[] conceptNidsForElements = Get.assemblageService().getReferencedComponentNidStreamFromAssemblage(
                            Get.identifierService().getNidForUuids(UUID.fromString(assemblageUuidStr)), true).toArray();

                    final DocumentBuilder db = new Processor(context.getConfiguration()).newDocumentBuilder();
                    db.setTreeModel(TreeModel.LINKED_TREE);
                    final BuildingStreamWriter bsw = db.newBuildingStreamWriter();
                    bsw.setDefaultNamespace("http://solor.io/tinkar/2020");
                    bsw.writeStartElement("assemblage");
                    bsw.writeAttribute("id", UUID.randomUUID().toString());
                    bsw.writeAttribute("desc", Get.getTextForComponent(assemblageNid, manifoldCoordinate));
                        for (int conceptNid: conceptNidsForElements) {
                            bsw.writeStartElement("element");
                            bsw.writeAttribute("id", Get.identifierService().getUuidPrimordialStringForNid(conceptNid));
                            bsw.writeAttribute("desc", Get.getTextForComponent(conceptNid, manifoldCoordinate));
                            bsw.writeEndElement();
                        }
                    bsw.writeEndElement();

                    return bsw.getDocumentNode().getUnderlyingNode();
                } catch(final SaxonApiException sae){
                    throw new XPathException(sae);
                } catch(final XMLStreamException xse){
                    throw new XPathException(xse);
                }
            }
        };
    }

}
