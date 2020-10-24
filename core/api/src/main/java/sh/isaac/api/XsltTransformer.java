package sh.isaac.api;

import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

@Contract
public interface XsltTransformer {
    Transformer getTransformer(Source xsltSource, ManifoldCoordinateImmutable manifoldCoordinate) throws TransformerConfigurationException;
}
