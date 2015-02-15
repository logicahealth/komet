/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.otf.mojo;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.dto.taxonomy.Taxonomy;

/**
 *
 * @author kec
 */
@Mojo( name = "export-taxonomy")
public class ExportTaxonomy extends AbstractMojo {
    
    @Parameter(required = true)
    private String taxonomyClass;
    
    @Parameter(required = true, defaultValue = "${project.build.directory}") 
    File buildDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Taxonomy taxonomy = (Taxonomy) Class.forName(taxonomyClass).newInstance();
            File javaDir = new File(buildDirectory, "src/generated");
            javaDir.mkdirs();
            File metadataDirectory = new File(buildDirectory, "generated-resources");
            metadataDirectory.mkdirs();
            File metadataBinaryDataFile = new File(metadataDirectory, taxonomy.getClass().getSimpleName() + ".econ");
            File metadataXmlDataFile = new File(metadataDirectory, taxonomy.getClass().getSimpleName() + ".xml");
            String bindingFileDirectory = taxonomyClass.replace('.', '/');
            File bindingFile = new File(javaDir, bindingFileDirectory + "Binding.java");
            bindingFile.getParentFile().mkdirs();
            try (Writer writer = new BufferedWriter(new FileWriter(bindingFile));
                 DataOutputStream binaryData = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(metadataBinaryDataFile)));
                 DataOutputStream xmlData = new DataOutputStream(
                         new BufferedOutputStream(new FileOutputStream(metadataXmlDataFile)))) {
                
                taxonomy.exportJavaBinding(writer, taxonomy.getClass().getPackage().getName(), 
                        taxonomy.getClass().getSimpleName() + "Binding");
                
                taxonomy.exportEConcept(binaryData);
                taxonomy.exportJaxb(xmlData);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }

    
}
