package sh.isaac.provider.xslt.extension;

import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;

public class SimpleExtensionFunction implements ExtensionFunction {

    final ManifoldCoordinateImmutable manifoldCoordinate;

    public SimpleExtensionFunction(ManifoldCoordinateImmutable manifoldCoordinate) {
        this.manifoldCoordinate = manifoldCoordinate;
    }

    @Override
    public QName getName() {
        return new QName("http://xslt.solor.io", "test");
    }

    @Override
    public SequenceType getResultType() {
        return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {};
    }

    @Override
    public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
        String result = "Solor extension to Saxon. Manifold: " + manifoldCoordinate.toUserString();
        return new XdmAtomicValue(result);
    }

}