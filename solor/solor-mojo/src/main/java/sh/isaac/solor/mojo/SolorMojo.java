/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.isaac.solor.mojo;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Future;
import javafx.concurrent.Task;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sh.isaac.api.ConfigurationService;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.ConfigurationService.BuildMode;
import sh.isaac.api.classifier.ClassifierResults;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.index.IndexBuilderService;
import sh.isaac.solor.direct.ImportType;
import sh.isaac.solor.direct.LoincDirectImporter;
import sh.isaac.solor.direct.DirectImporter;
import sh.isaac.solor.direct.Rf2RelationshipTransformer;

/**
 *
 * @author kec
 */
@Mojo(
        name = "solor-import",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class SolorMojo extends AbstractMojo {

    /**
     * Location of the folder that contains the files to import.
     */
    @Parameter(required = false, defaultValue = "${project.build.directory}/data")
    private String importFolderLocation;

    /**
     * This value, if present, is passed in to {@link ConfigurationService#setDataStoreFolderPath(Path)}
     *
     * @parameter
     * @optional
     */
    @Parameter(required = false)
    private File dataStoreLocation;
    
    @Parameter(required = true)
    private String importType;
    
    @Parameter(required = false, defaultValue = "false")
    private boolean transform;

    public SolorMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Get.configurationService().setDBBuildMode(BuildMode.IBDF);
        try {
            Get.configurationService().setIBDFImportPathFolderPath(new File(importFolderLocation).toPath());
            
            if (this.dataStoreLocation != null)
            {
               Get.configurationService().setDataStoreFolderPath(dataStoreLocation.toPath());
            }

            getLog().info("  Setup AppContext, data store location = " + Get.configurationService().getDataStoreFolderPath().toFile().getCanonicalPath());
            LookupService.startupIsaac();
            //TODO We aren't yet making use of semantic indexes, so no reason to build them.  Disable for performance reasons.
            //However, once the index-config-per-assemblage framework is fixed, this should be removed, and the indexers will
            //be configured at the assemblage level.
            LookupService.getService(IndexBuilderService.class, "semantic index").setEnabled(false);
            DirectImporter rf2Importer = new DirectImporter(ImportType.valueOf(importType));
            getLog().info("  Importing RF2 files.");
            rf2Importer.run();
            LookupService.syncAll();
            
            LoincDirectImporter loincImporter = new LoincDirectImporter();
            getLog().info("  Importing LOINC files.");
            loincImporter.run();
            LookupService.syncAll();
            if (transform) {
                getLog().info("  Transforming RF2 relationships to SOLOR.");
                Rf2RelationshipTransformer transformer = new Rf2RelationshipTransformer(ImportType.valueOf(importType));
                Future<?> transformTask = Get.executor().submit(transformer);
                transformTask.get();

                getLog().info("  Classifying stated forms.");

                //TODO change how we get the edit coordinates. 
                ManifoldCoordinate coordinate = Get.coordinateFactory().createDefaultStatedManifoldCoordinate();
                EditCoordinate editCoordinate = Get.coordinateFactory().createDefaultUserSolorOverlayEditCoordinate();

                Task<ClassifierResults> classifierResultsTask
                        = Get.logicService().getClassifierService(coordinate, editCoordinate).classify();
                ClassifierResults classifierResults = classifierResultsTask.get();
                getLog().info(classifierResults.toString());
            }

            LookupService.syncAll();  //This should be unnecessary....
            LookupService.shutdownIsaac();
        } catch (Throwable throwable) {
            throw new MojoFailureException("solor-import failed", throwable);
        }
    }
}
