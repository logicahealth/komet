package org.ihtsdo.otf.tcc.model.cc.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.ihtsdo.otf.tcc.model.cc.description.Description;
import static org.ihtsdo.otf.tcc.model.cc.lucene.LuceneManager.descWriter;
import org.ihtsdo.otf.tcc.model.cc.termstore.SearchType;

public class DescriptionLuceneManager extends LuceneManager {

    protected static File descLuceneMutableDirFile = new File("target/berkeley-db/mutable/lucene");
    protected static File descLuceneReadOnlyDirFile = new File("target/berkeley-db/read-only/lucene");
    protected static String descMutableDirectorySuffix = "mutable/lucene";
    protected static String descReadOnlyDirectorySuffix = "read-only/lucene";
    public static int matchLimit = 10000;

    protected static void writeToLuceneNoLock(Collection<Description> descriptions) throws CorruptIndexException, IOException {
        if (descWriter == null) {
            descLuceneMutableDir = setupWriter(descLuceneMutableDirFile, descLuceneMutableDir, SearchType.DESCRIPTION);
        }

        if (descWriter != null) {
            for (Description desc : descriptions) {
                if (desc != null) {
                    descWriter.deleteDocuments(new Term("dnid", Integer.toString(desc.getNid())));
                    descWriter.addDocument(DescriptionIndexGenerator.createDoc(desc));
                }
            }
        }
    }
}
