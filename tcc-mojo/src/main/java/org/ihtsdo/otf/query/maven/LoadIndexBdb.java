package org.ihtsdo.otf.query.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.ihtsdo.otf.tcc.api.io.FileIO;
import org.ihtsdo.otf.tcc.api.store.TerminologyStoreDI;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.ihtsdo.otf.tcc.lookup.Hk2Looker;
import org.ihtsdo.otf.tcc.model.index.service.IndexerBI;

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
import java.io.File;

import java.util.List;

/**
 * Goal which loads a database from an eConcept file, and generates the indexes.
 * 
 * Dan Notes:  This class doesn't really belong in this package, and yet it does...
 * this needs to depend on OTF-Query-Service - otherwise, it won't generate any indexes.
 * Yet, OTF-Query-Service depends on OTF-Versioning-Service - and we would have a confusing
 * circular dependency.  So, while it makes sense that the BDB builder mojo should be with the 
 * BDB impl... it can't be complete with out the indexing tooling.
 * 
 * The only way this loader will properly build indexes is if you run it with a classpath 
 * configured in such a way that the OTF-Query-Service jar files are on the classpath, so 
 * HK2 can find them.
 * 
 *
 * @goal load-index-bdb
 *
 * @phase process-sources
 */
public class LoadIndexBdb extends AbstractMojo {

    /**
     * true if the mutable database should replace the read-only database after
     * load is complete.
     *
     * @parameter default-value=true
     * @required
     */
    private boolean moveToReadOnly = true;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/berkeley-db"
     * @required
     */
    private String bdbFolderLocation;

    /**
     * <code>eConcept format</code> files to import.
     *
     * @parameter
     * @required
     */
    private String[] econFileStrings;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            File    bdbFolderFile = new File(bdbFolderLocation);
            boolean dbExists      = bdbFolderFile.exists();

            System.setProperty("org.ihtsdo.otf.tcc.datastore.bdb-location", bdbFolderLocation);

            TerminologyStoreDI store    = Hk2Looker.get().getService(TerminologyStoreDI.class);
            List<IndexerBI>    indexers = Hk2Looker.get().getAllServices(IndexerBI.class);

            if (!dbExists) {
                for (IndexerBI indexer : indexers) {
                    indexer.setEnabled(false);
                }
                //Dan notes: pretty sure this needs to be outside the if statement...
                store.loadEconFiles(econFileStrings);
            }

            for (IndexerBI indexer : indexers) {
                indexer.setEnabled(true);
            }
            //Dan notes: and wouldn't we _not_ want to do this, if we already indexed above, during the load?
            store.index();

            Ts.close();

            if (!dbExists && moveToReadOnly) {
                getLog().info("moving mutable to read-only");

                File readOnlyDir = new File(bdbFolderLocation, "read-only");

                FileIO.recursiveDelete(readOnlyDir);

                File mutableDir = new File(bdbFolderLocation, "mutable");

                mutableDir.renameTo(readOnlyDir);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
    }
}
