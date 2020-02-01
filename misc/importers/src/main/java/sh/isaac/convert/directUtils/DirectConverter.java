/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government
 * employees, or under US Veterans Health Administration contracts.
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government
 * employees are USGovWork (17USC §105). Not subject to copyright.
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.convert.directUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.jvnet.hk2.annotations.Contract;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.transaction.Transaction;
import sh.isaac.pombuilder.converter.ConverterOptionParam;
import sh.isaac.pombuilder.converter.SupportedConverterTypes;

/**
 * {@link DirectConverter}
 * An HK2 contract to allow runtime enumeration of the available direct converters
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Contract
public interface DirectConverter
{
	/**
	 * Used to set the transaction in the case of a no-arg constructor.
	 * @param transaction
	 */
	void setTransaction(Transaction transaction);

	/**
	 * @param outputDirectory - optional - if provided, debug info will be written here
	 * @param inputFolder - the folder to search for the source file(s).  Implementors should only utilize 
	 * {@link Path} operations on the inputFolder, incase the input folder is coming from a {@link FileSystems} that
	 * doesn't suport toFile, such as zip.
	 * @param converterSourceArtifactVersion - the version number of the source file being passed in
	 * @param stampCoordinate - the coordinate to use for readback in cases where content merges into existing content
	 */
	void configure(File outputDirectory, Path inputFolder, String converterSourceArtifactVersion, StampCoordinate stampCoordinate);

	/**
	 * Run the actual conversion
	 * @param statusUpdates - optional - if provided, the converter should post status updates here.
	 * @param progresUpdates - optional - if provided, the converter should post progress on workDone here, the first argument
	 * is work done, the second argument is work total.
	 * @throws IOException
	 */
	void convertContent(Transaction transaction, Consumer<String> statusUpdates, BiConsumer<Double, Double> progresUpdates) throws IOException;
	
	/**
	 * @return the type of content this converter can handle
	 */
	SupportedConverterTypes[] getSupportedTypes();
	
	/**
	 * Return any options that must be set, prior to executing this converter
	 * @return the list of required options
	 */
	ConverterOptionParam[] getConverterOptions();
	
	/**
	 * Set a value (or values) corresponding to one of the options from {@link #getConverterOptions()}.
	 * @param internalName - Align with {@link ConverterOptionParam#getInternalName()}
	 * @param values 1 or more values for this option.
	 */
	void setConverterOption(String internalName, String ... values);
}
