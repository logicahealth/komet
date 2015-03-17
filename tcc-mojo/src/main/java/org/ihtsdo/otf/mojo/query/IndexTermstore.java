/*
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
package org.ihtsdo.otf.mojo.query;

//~--- non-JDK imports --------------------------------------------------------

import java.io.IOException;
import java.util.ArrayList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

//~--- JDK imports ------------------------------------------------------------


import java.util.List;
import org.glassfish.hk2.api.MultiException;

/**
 * Goal which indexes a database using indexer services on the classpath.
 * 
 *
 */
@Mojo( name = "index-termstore",
        defaultPhase = LifecyclePhase.PROCESS_SOURCES)

public class IndexTermstore extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        try {

            TerminologyStoreDI store    = Hk2Looker.get().getService(TerminologyStoreDI.class);
            getLog().info("Found store: " + store);
                        
            List<IndexerBI>    indexers = Hk2Looker.get().getAllServices(IndexerBI.class);
            List<String> indexerNames = new ArrayList<>();
            indexers.stream().forEach((IndexerBI i) -> indexerNames.add(i.getIndexerName()));
            getLog().info("Found indexers: " + indexerNames);

            getLog().info("Starting indexing. ");
            store.index();
            getLog().info("Finished indexing. ");

        } catch (MultiException | IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
    
    
}
