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

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;
import org.ihtsdo.otf.tcc.api.concept.ConceptFetcherBI;
import org.ihtsdo.otf.tcc.api.coordinate.StandardViewCoordinates;
import org.ihtsdo.otf.tcc.api.refex.RefexChronicleBI;

/**
 *
 * @author dylangrald
 */
public class RefsetIndexGenerator extends IndexGenerator {

    private int refsetCounter = 0;
    private int conceptCounter = 0;
    private int feedbackInterval = 1000;
    public static boolean consoleFeedback = false;

    public RefsetIndexGenerator(IndexWriter writer) throws IOException {
        super(writer);
    }

    @Override
    public boolean allowCancel() {
        return false;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
       conceptCounter++;

        for (RefexChronicleBI r : fetcher.fetch().getRefsetMembers()) {
            writer.addDocument(createDoc(r));
            refsetCounter++;

            if (consoleFeedback && refsetCounter % feedbackInterval == 0) {
                System.out.print(".");
                lineCounter++;

                if (lineCounter > 80) {
                    lineCounter = 0;
                    System.out.println();
                    System.out.print("c:" + conceptCounter + " d:" + refsetCounter);
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return "Generating refset index";
    }

    public static Document createDoc(RefexChronicleBI r) throws IOException {
      Document doc = new Document();

        doc.add(new StringField("rnid", Integer.toString(r.getNid()), Field.Store.YES));
        doc.add(new StringField("cnid", Integer.toString(r.getConceptNid()), Field.Store.YES));

        String lastRefex = null;
        
        for (ComponentVersionBI tuple : r.getRefexMembersActive(StandardViewCoordinates.getSnomedInferredLatest())){
            if((lastRefex == null) || (lastRefex.equals(tuple.toString()) == false)){
                doc.add(new TextField("refset", tuple.toString(), Field.Store.NO));
            }
        }
        
        //doc.add(new TextField("refset", r.toString(), Field.Store.NO));

        /*
        for (ComponentVersionBI tuple : r.getV) {
            if ((lastRefex == null) || (lastRefex.equals(tuple.getText()) == false)) {
                doc.add(new TextField("refset", tuple.getText(), Field.Store.NO));
            }
        }
        */

        return doc;
    }
}
