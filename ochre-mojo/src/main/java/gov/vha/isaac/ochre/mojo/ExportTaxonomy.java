/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.ochre.mojo;

import gov.vha.isaac.ochre.api.IsaacTaxonomy;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author kec
 */
@Mojo( name = "export-taxonomy")
public class ExportTaxonomy extends AbstractMojo {
    
    @Parameter(required = true)
    private String taxonomyClass;
    
    @Parameter(required = true)
    private String bindingPackage;
    
    @Parameter(required = true)
    private String bindingClass;
    
    @Parameter(required = true, defaultValue = "${project.build.directory}") 
    File buildDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            IsaacTaxonomy taxonomy = (IsaacTaxonomy) Class.forName(taxonomyClass).newInstance();
            File javaDir = new File(buildDirectory, "src/generated");
            javaDir.mkdirs();
            File metadataDirectory = new File(buildDirectory, "generated-resources");
            metadataDirectory.mkdirs();
            File metadataXmlDataFile = new File(metadataDirectory, taxonomy.getClass().getSimpleName() + ".xml");
            String bindingFileDirectory = bindingPackage.concat(".").concat(bindingClass).replace('.', '/');
            File bindingFile = new File(javaDir, bindingFileDirectory + ".java");
            bindingFile.getParentFile().mkdirs();
            try (Writer writer = new BufferedWriter(new FileWriter(bindingFile));

                 DataOutputStream xmlData = new DataOutputStream(
                         new BufferedOutputStream(new FileOutputStream(metadataXmlDataFile)))) {
                
                taxonomy.exportJavaBinding(writer, bindingPackage,  bindingClass);

                //taxonomy.exportJaxb(xmlData);
            }
            Path ibdfPath = Paths.get(metadataDirectory.getAbsolutePath(), taxonomy.getClass().getSimpleName() + ".ibdf");
            taxonomy.exportIBDF(ibdfPath);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }

    
}
