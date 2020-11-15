package sh.isaac.api.importers;

import sh.isaac.api.coordinate.StampFilter;
import sh.isaac.api.transaction.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface TerminologyConverter {

    /**
     * If this was constructed via HK2, then you must call the configure method prior to calling {@link #convertContent(Consumer, BiConsumer)}
     * Maven executions do not use this method.
     *
     * @param outputDirectory - optional - if provided, debug info will be written here
     * @param inputFolder - the folder to search for the source file(s).  Implementors should only utilize
     * {@link Path} operations on the inputFolder, incase the input folder is coming from a {@link FileSystems} that
     * doesn't suport toFile, such as zip.
     * @param converterSourceArtifactVersion - the version number of the source file being passed in
     * @param stampFilter - the coordinate to use for readback in cases where content merges into existing content
     * @param transaction - transaction to use for this conversion run
     */
    void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampFilter stampFilter, Transaction transaction);

    /**
     * Run the actual conversion
     * @param statusUpdates - optional - if provided, the converter should post status updates here.
     * @param progresUpdates - optional - if provided, the converter should post progress on workDone here, the first argument
     * is work done, the second argument is work total.
     * @throws IOException
     */
    void convertContent(Consumer<String> statusUpdates, BiConsumer<Double, Double> progresUpdates) throws IOException;

    /**
     * @return the type of content this converter can handle
     */
    ConverterInfo[] getSupportedTypes();

    /**
     * Return any options that must be set, prior to executing this converter
     * @return the list of required options
     */
    ConverterOption[] getConverterOptions();

    /**
     * Set a value (or values) corresponding to one of the options from {@link #getConverterOptions()}.
     * @param internalName - Align with {@link ConverterOptionParam#getInternalName()}
     * @param values 1 or more values for this option.
     */
    void setConverterOption(String internalName, String ... values);
}
