package sh.isaac.convert.mojo.rf2Direct.solor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.util.UuidT5Generator;
import sh.isaac.convert.directUtils.DirectConverter;
import sh.isaac.convert.directUtils.DirectConverterBaseMojo;
import sh.isaac.converters.sharedUtils.stats.ConverterUUID;
import sh.isaac.model.configuration.StampCoordinates;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 2019-08-02
 * aks8m - https://github.com/aks8m
 */
@PerLookup
@Service
public class RF2SolorImportDirect extends DirectConverterBaseMojo implements DirectConverter {

    private Logger log = LogManager.getLogger();

    /**
     * For maven and HK2
     */
    protected RF2SolorImportDirect()
    {

    }

    @Override
    public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate) {

        this.outputDirectory = outputDirectory;
        this.inputFileLocationPath = inputFolder;
        this.converterSourceArtifactVersion = converterSourceArtifactVersion;
        this.converterUUID = new ConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
        this.readbackCoordinate = stampCoordinate == null ? StampCoordinates.getDevelopmentLatest() : stampCoordinate;
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
        return new RF2SolorDirectConfigOptions().getConfigOptions();
    }

    @Override
    public void setConverterOption(String internalName, String... values) {

    }
}
