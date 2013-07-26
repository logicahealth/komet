package org.ihtsdo.otf.tcc.chronicle.cc.lucene;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionChronicleBI;
import org.ihtsdo.otf.tcc.api.description.DescriptionVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

public class DescriptionIndexGenerator extends IndexGenerator {
    private int descCounter      = 0;
    private int conceptCounter   = 0;
    private int feedbackInterval = 1000;
    public static boolean consoleFeedback = false;

    public DescriptionIndexGenerator(IndexWriter writer) throws IOException {
        super(writer);
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        conceptCounter++;

        for (DescriptionChronicleBI d : fetcher.fetch().getDescriptions()) {
            writer.addDocument(createDoc(d));
            descCounter++;

            if (consoleFeedback && descCounter % feedbackInterval == 0) {
                System.out.print(".");
                lineCounter++;

                if (lineCounter > 80) {
                    lineCounter = 0;
                    System.out.println();
                    System.out.print("c:" + conceptCounter + " d:" + descCounter);
                }
            }
        }
    }

    public static Document createDoc(DescriptionChronicleBI desc) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("dnid", Integer.toString(desc.getNid()), Field.Store.YES));
        doc.add(new StringField("cnid", Integer.toString(desc.getConceptNid()), Field.Store.YES));

        String lastDesc = null;

        for (DescriptionVersionBI tuple : desc.getVersions()) {
            if ((lastDesc == null) || (lastDesc.equals(tuple.getText()) == false)) {
                doc.add(new TextField("desc", tuple.getText(), Field.Store.NO));
            }
        }

        return doc;
    }

    @Override
    public boolean allowCancel() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Generating description index";
    }
}
