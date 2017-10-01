/*
 * Copyright 2017 Organizations participating in ISAAC, ISAAC's KOMET, and SOLOR development include the
         US Veterans Health Administration, OSHERA, and the Health Services Platform Consortium..
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
package sh.isaac.provider.query.lucene.indexers;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.IndexSearcher;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.collections.SequenceSet;
import sh.isaac.api.component.sememe.version.DescriptionVersion;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import static sh.isaac.provider.query.lucene.LuceneIndexer.DOCVALUE_COMPONENT_NID;

/**
 *
 * @author kec
 */
public class NidSetCollectionManager implements CollectorManager<NoScoreCollector, NidSet> {

   /**
    * The Constant LOG.
    */
   protected static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

   final IndexSearcher searcher;

   public NidSetCollectionManager(IndexSearcher searcher) {
      this.searcher = searcher;
   }

   @Override
   public NoScoreCollector newCollector() throws IOException {
      return new NoScoreCollector();
   }

   @Override
   public NidSet reduce(Collection<NoScoreCollector> collectors) throws IOException {
      SequenceSet docIds = new SequenceSet();
      for (NoScoreCollector collector : collectors) {
         docIds.or(collector.getDocIds());
      }
      NidSet nids = new NidSet();

      for (LeafReaderContext context : searcher.getIndexReader().leaves()) {

         LeafReader leafReader = context.reader();
         NumericDocValues componentNidValues = DocValues.getNumeric(leafReader, DOCVALUE_COMPONENT_NID);
         docIds.stream().filter((docId) -> docId - context.docBase >= 0).filter((docId) -> {
            try {
               return componentNidValues.advanceExact(docId - context.docBase);
            } catch (IOException ex) {
               LOG.error(ex);
               throw new RuntimeException(ex);
            }
         }).forEach((docId) -> {
            try {
               nids.add((int) componentNidValues.longValue());
            } catch (Throwable ex) {
               try {
                  Document doc = searcher.doc(docId);
                  final int componentNid = doc.getField(LuceneIndexer.FIELD_COMPONENT_NID)
                     .numericValue()
                     .intValue();
                  LOG.error("DocId: " + docId +", doc is: " + doc + ", nid is: " + componentNid);
                  nids.add(componentNid);
                  Optional<? extends Chronology> object = Get.identifiedObjectService().getIdentifiedObjectChronology(componentNid);
                  if (object.isPresent()) {
                     Chronology typedObject = object.get();
                     
                     switch (object.get().getVersionType()) {
                        case DESCRIPTION:
                           DescriptionVersion descriptionVersion =   (DescriptionVersion) typedObject.getVersionList().get(0);
                           LOG.error("Doc is: " + doc + ", object is description: " + descriptionVersion.getText());
                           break;
                        default:
                           LOG.error("Doc is: " + doc + ", object is: " + object.get().getVersionType());
                     }
                     
                  } else {
                     LOG.error("Doc is: " + doc + ", with no object.");
                  }
               } catch (IOException ex1) {
                  LOG.error("DocId: " + docId + " context.docBase: " + context.docBase);
                  LOG.error("Error retrieving doc", ex1);
                  LOG.error(ex);
                  throw new RuntimeException(ex);
               }
            }
         });

      }
      return nids;
   }

}
