package sh.isaac.provider.xslt;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.Processor;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.XsltTransformer;
import sh.isaac.api.coordinate.ManifoldCoordinateImmutable;
import sh.isaac.provider.xslt.extension.EnumerateMembers;
import sh.isaac.provider.xslt.extension.SimpleExtensionFunction;

import javax.inject.Singleton;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;

/**
 *
 */

@Service
@Singleton
public class XsltTransformerProvider implements XsltTransformer {

    @Override
    public Transformer getTransformer(Source xsltSource, ManifoldCoordinateImmutable manifoldCoordinate) throws TransformerConfigurationException {
        TransformerFactoryImpl factory = new TransformerFactoryImpl();

        // Get the currently used processor
        Configuration saxonConfig = factory.getConfiguration();
        //File configurationFile = new File(System.getProperty("user.home")  + "/Solor", "saxon-license.lic");
        //saxonConfig.setConfigurationProperty(Feature.LICENSE_FILE_LOCATION, configurationFile.getAbsolutePath());
        Processor processor = (Processor) saxonConfig.getProcessor();

        // Here extension happens, test comes from class SimpleExtensionFunction -> SimpleExtensionFunction.java
        ExtensionFunction test = new SimpleExtensionFunction(manifoldCoordinate);
        processor.registerExtensionFunction(test);

        EnumerateMembers enumerateMembers = new EnumerateMembers(manifoldCoordinate);
        processor.registerExtensionFunction(enumerateMembers);

        return factory.newTransformer(xsltSource);
    }
}
