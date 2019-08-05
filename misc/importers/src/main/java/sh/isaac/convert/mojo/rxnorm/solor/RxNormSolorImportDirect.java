package sh.isaac.convert.mojo.rxnorm.solor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 2019-08-05
 * aks8m - https://github.com/aks8m
 */
public class RxNormSolorImportDirect extends DirectConverterBaseMojo implements DirectConverter {

    private Logger log = LogManager.getLogger();

    /**
     * For maven and HK2
     */
    protected RxNormSolorImportDirect(){

    }

    @Override
    public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate) {

    }

    @Override
    public void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progresUpdates) throws IOException {

    }

    @Override
    public SupportedConverterTypes[] getSupportedTypes() {
        return new SupportedConverterTypes[0];
    }

    @Override
    public ConverterOptionParam[] getConverterOptions() {
        return new ConverterOptionParam[0];
    }

    @Override
    public void setConverterOption(String internalName, String... values) {

    }
}
