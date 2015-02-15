package org.ihtsdo.otf.mojo.termstore;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;

//~--- JDK imports ------------------------------------------------------------

/*
* Copyright 2001-2005 The Apache Software Foundation.
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

/**
 * Goal which loads a database from eConcept files.
 */
@Mojo( name = "load-termstore",
        defaultPhase = LifecyclePhase.PROCESS_SOURCES)

public class LoadTermstore extends AbstractMojo {

    /**
     * {@code eConcept format} files to import.
     *
     */
    @Parameter(required = true)
    private String[] econFileStrings;

    @Override
    public void execute() throws MojoExecutionException {
        try {

            TerminologyStoreDI store    = Hk2Looker.get().getService(TerminologyStoreDI.class);
            
            getLog().info("Found store: " + store);
            
            getLog().info("Before load, store populated with " + store.getConceptCount() + " concepts.");
            
            store.loadEconFiles(econFileStrings);

            getLog().info("After load, store populated with " + store.getConceptCount() + " concepts.");
            
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
}
