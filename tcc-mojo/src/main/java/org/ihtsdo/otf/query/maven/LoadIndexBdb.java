package org.ihtsdo.ttk.mojo;

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

                store.loadEconFiles(econFileStrings);
            }

            for (IndexerBI indexer : indexers) {
                indexer.setEnabled(true);
            }

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
