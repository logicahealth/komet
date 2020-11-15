package sh.komet.converter.beer;

import sh.isaac.api.coordinate.Coordinates;
import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.importers.ConverterInfo;
import sh.isaac.api.importers.UploadFileInfo;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.api.util.UuidT5Generator;
import sh.komet.converter.turtle.TempConverterInfo;
import sh.komet.converter.turtle.TempConverterUUID;
import sh.komet.converter.turtle.TurtleConverter;

import java.io.File;
import java.nio.file.Path;

public class BeerConverter extends TurtleConverter {
    @Override
    public void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampFilter stampFilter, Transaction transaction) {
        this.outputDirectory = outputDirectory;
        this.inputFileLocationPath = inputFolder;
        this.converterSourceArtifactVersion = converterSourceArtifactVersion;
        this.converterUUID = new TempConverterUUID(UuidT5Generator.PATH_ID_FROM_FS_DESC, false);
        this.readbackCoordinate = stampFilter == null ? Coordinates.Filter.DevelopmentLatest() : stampFilter;
        this.transaction = transaction;
    }

    @Override
    public ConverterInfo[] getSupportedTypes() {
        return new ConverterInfo[] {
                new TempConverterInfo("bevon-src-data", ".*$",
                        "A typical bevon version number is '0.8'.  There are no enforced restrictions on the format of this value.",
                        new String[] {}, new String[] {}, new UploadFileInfo[] {
                        new UploadFileInfo("", "https://github.com/jgkim/bevon",
                                "https://raw.githubusercontent.com/jgkim/bevon/master/0.8/ttl",
                                "The file name is ignored - it just needs to be a turtle formatted file which ends with .ttl.",
                                ".*.ttl$", true)
                }, "bevon-ibdf", "convert-turtle-to-ibdf", "Beverage Ontology",
                        new String[] {"shared/licenses/bevon.xml"},
                        new String[] {"shared/noticeAdditions/bevon-NOTICE-addition.txt"})
        };
    }
}
