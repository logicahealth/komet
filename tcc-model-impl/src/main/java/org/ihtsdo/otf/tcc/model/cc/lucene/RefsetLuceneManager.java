/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.otf.tcc.model.cc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;

/**
 *
 * @author dylangrald
 */
public class RefsetLuceneManager extends LuceneManager {

    protected static File refsetLuceneMutableDirFile = new File("target/berkeley-db/mutable/lucene");
    protected static File refsetLuceneReadOnlyDirFile = new File("target/berkeley-db/read-only/lucene");
    protected static String refsetMutableDirectorySuffix = "mutable/lucene";
    protected static String refsetReadOnlyDirectorySuffix = "read-only/lucene";
    public static int matchLimit = 10000;

    protected static void writeToLuceneNoLock(Collection<RefexMember> refsets) throws CorruptIndexException, IOException {
        if (refsetWriter == null) {
            refsetLuceneMutableDir = setupRefsetWriter(refsetLuceneMutableDirFile, refsetLuceneMutableDir);
            //descWriter.setUseCompoundFile(true);
            //descWriter.setMergeFactor(15);
            //descWriter.setMaxMergeDocs(Integer.MAX_VALUE);
            //descWriter.setMaxBufferedDocs(1000);
        }

        IndexWriter writerCopy = refsetWriter;
        if (writerCopy != null) {
            for (RefexMember refset : refsets) {
                if (refset != null) {
                    writerCopy.deleteDocuments(new Term("rnid", Integer.toString(refset.getNid())));
                    writerCopy.addDocument(RefsetIndexGenerator.createDoc(refset));
                }
            }
            writerCopy.commit();
        }

        /*if (descSearcher != null) {
         descSearcher.close();
         AceLog.getAppLog().info("Closing lucene desc Searcher");
         }
         descSearcher = null;*/
    }
}
