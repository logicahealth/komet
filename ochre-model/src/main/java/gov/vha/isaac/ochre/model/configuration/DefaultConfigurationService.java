/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.vha.isaac.ochre.model.configuration;

import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.constants.Constants;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableEditCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLanguageCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableLogicCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableStampCoordinate;
import gov.vha.isaac.ochre.api.observable.coordinate.ObservableTaxonomyCoordinate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

/**
 * The default implementation of {@link ConfigurationService} which is used to
 * specify where the datastore location is, among other things.
 *
 * Note that this default implementation has a {@link Rank} of 0. To override
 * this implementation with any other, simply provide another implementation on
 * the classpath with a higher rank.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "Cradle Default Configuration Service")
@Rank(value = 0)
@Singleton
public class DefaultConfigurationService implements ConfigurationService {

    private Path dataStoreFolderPath_ = null;
    DefaultCoordinateProvider defaultCoordinateProvider_ = new DefaultCoordinateProvider();

    private static final Logger LOG = LogManager.getLogger();

    private volatile boolean initComplete_ = false;

    private DefaultConfigurationService() {
        //only for HK2
    }

    /**
     * @see
     * gov.vha.isaac.ochre.api.ConfigurationService#getDataStoreFolderPath()
     */
    @Override
    public Optional<Path> getDataStoreFolderPath() {
        if (dataStoreFolderPath_ == null && !initComplete_) {
            synchronized (this) {
                if (dataStoreFolderPath_ == null && !initComplete_) {
                    String dataStoreRootFolder = System.getProperty(Constants.DATA_STORE_ROOT_LOCATION_PROPERTY);
                    if (!StringUtils.isBlank(dataStoreRootFolder)) {
                        dataStoreFolderPath_ = Paths.get(dataStoreRootFolder);
                        if (!Files.exists(dataStoreFolderPath_)) {
                            try {
                                Files.createDirectories(dataStoreFolderPath_);
                            } catch (IOException e) {
                                throw new RuntimeException("Failure creating dataStoreRootFolder folder: " + dataStoreFolderPath_.toString(), e);
                            }
                        }

                        if (!Files.isDirectory(dataStoreFolderPath_)) {
                            throw new IllegalStateException("The specified path to the db folder appears to be a file, rather than a folder, as expected.  " + " Found: "
                                    + dataStoreFolderPath_.toAbsolutePath().toString());
                        }
                    }

                    initComplete_ = true;
                }
            }
        }
        return Optional.ofNullable(dataStoreFolderPath_);
    }

    /**
     * @see
     * gov.vha.isaac.ochre.api.ConfigurationService#setDataStoreFolderPath(java.nio.file.Path)
     */
    @Override
    public void setDataStoreFolderPath(Path dataStoreFolderPath) throws IllegalStateException, IllegalArgumentException {
        LOG.info("setDataStoreFolderPath called with " + dataStoreFolderPath);
        if (LookupService.isIsaacStarted()) {
            throw new IllegalStateException("Can only set the dbFolderPath prior to starting Isaac. Runlevel: " + LookupService.getCurrentRunLevel());
        }

        if (Files.exists(dataStoreFolderPath) && !Files.isDirectory(dataStoreFolderPath)) {
            throw new IllegalArgumentException("The specified path to the db folder appears to be a file, rather than a folder, as expected.  " + " Found: "
                    + dataStoreFolderPath_.toAbsolutePath().toString());
        }
        try {
            Files.createDirectories(dataStoreFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("Failure creating dataStoreFolderPath folder: " + dataStoreFolderPath.toString(), e);
        }

        dataStoreFolderPath_ = dataStoreFolderPath;
    }

    @Override
    public void setDefaultUser(int conceptId) {
        defaultCoordinateProvider_.setDefaultUser(conceptId);
    }

    @Override
    public void setDefaultModule(int conceptId) {
        defaultCoordinateProvider_.setDefaultModule(conceptId);
    }

    @Override
    public void setDefaultTime(long timeInMs) {
        defaultCoordinateProvider_.setDefaultTime(timeInMs);
    }

    @Override
    public void setDefaultPath(int conceptId) {
        defaultCoordinateProvider_.setDefaultPath(conceptId);
    }

    @Override
    public void setDefaultLanguage(int conceptId) {
        defaultCoordinateProvider_.setDefaultLanguage(conceptId);
    }

    @Override
    public void setDefaultDialectAssemblagePreferenceList(int[] dialectAssemblagePreferenceList) {
        defaultCoordinateProvider_.setDefaultDialectAssemblagePreferenceList(dialectAssemblagePreferenceList);
    }

    @Override
    public void setDefaultDescriptionTypePreferenceList(int[] descriptionTypePreferenceList) {
        defaultCoordinateProvider_.setDefaultDescriptionTypePreferenceList(descriptionTypePreferenceList);
    }

    @Override
    public void setDefaultStatedAssemblage(int conceptId) {
        defaultCoordinateProvider_.setDefaultStatedAssemblage(conceptId);
    }

    @Override
    public void setDefaultInferredAssemblage(int conceptId) {
        defaultCoordinateProvider_.setDefaultInferredAssemblage(conceptId);
    }

    @Override
    public void setDefaultDescriptionLogicProfile(int conceptId) {
        defaultCoordinateProvider_.setDefaultDescriptionLogicProfile(conceptId);
    }

    @Override
    public void setDefaultClassifier(int conceptId) {
        defaultCoordinateProvider_.setDefaultClassifier(conceptId);
    }

    @Override
    public ObservableEditCoordinate getDefaultEditCoordinate() {
        return defaultCoordinateProvider_.getDefaultEditCoordinate();
    }

    @Override
    public ObservableLanguageCoordinate getDefaultLanguageCoordinate() {
        return defaultCoordinateProvider_.getDefaultLanguageCoordinate();
    }

    @Override
    public ObservableLogicCoordinate getDefaultLogicCoordinate() {
        return defaultCoordinateProvider_.getDefaultLogicCoordinate();
    }

    @Override
    public ObservableStampCoordinate getDefaultStampCoordinate() {
        return defaultCoordinateProvider_.getDefaultStampCoordinate();
    }

    @Override
    public ObservableTaxonomyCoordinate getDefaultTaxonomyCoordinate() {
        return defaultCoordinateProvider_.getDefaultTaxonomyCoordinate();
    }

}
