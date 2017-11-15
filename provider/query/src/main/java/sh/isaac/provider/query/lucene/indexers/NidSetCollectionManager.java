/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.provider.query.lucene.indexers;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Optional;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.provider.query.lucene.LuceneIndexer;

import static sh.isaac.provider.query.lucene.LuceneIndexer.DOCVALUE_COMPONENT_NID;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kec
 */
public class NidSetCollectionManager
         implements CollectorManager<NoScoreCollector, NidSet> {
   /**
    * The Constant LOG.
    */
   protected static final Logger LOG = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   final IndexSearcher searcher;

   //~--- constructors --------------------------------------------------------

   public NidSetCollectionManager(IndexSearcher searcher) {
      this.searcher = searcher;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public NoScoreCollector newCollector()
            throws IOException {
      return new NoScoreCollector();
   }

   @Override
   public NidSet reduce(Collection<NoScoreCollector> collectors)
            throws IOException {
      SequenceSet docIds = new SequenceSet();

      for (NoScoreCollector collector: collectors) {
         docIds.or(collector.getDocIds());
      }

      NidSet nids = new NidSet();

      for (LeafReaderContext context: searcher.getIndexReader()
            .leaves()) {
         LeafReader       leafReader         = context.reader();
         NumericDocValues componentNidValues = DocValues.getNumeric(leafReader, DOCVALUE_COMPONENT_NID);

         for (int docId: docIds.asArray()) {
            if ((docId - context.docBase >= 0) && componentNidValues.advanceExact(docId - context.docBase)) {
               try {
                  nids.add((int) componentNidValues.longValue());
               } catch (Throwable ex) {
                  try {
                     Document  doc          = searcher.doc(docId);
                     final int componentNid = doc.getField(LuceneIndexer.FIELD_COMPONENT_NID)
                                                 .numericValue()
                                                 .intValue();

                     LOG.error("DocId: " + docId + ", doc is: " + doc + ", nid is: " + componentNid, ex);
                     nids.add(componentNid);

                     Optional<? extends Chronology> object = Get.identifiedObjectService()
                                                                .getIdentifiedObjectChronology(componentNid);

                     if (object.isPresent()) {
                        Chronology typedObject = object.get();

                        switch (object.get()
                                      .getVersionType()) {
                        case DESCRIPTION:
                           DescriptionVersion descriptionVersion = (DescriptionVersion) typedObject.getVersionList()
                                                                                                   .get(0);

                           LOG.error("Doc is: " + doc + ", object is description: " + descriptionVersion.getText());
                           break;

                        default:
                           LOG.error("Doc is: " + doc + ", object is: " + object.get().getVersionType());
                        }
                     } else {
                        LOG.error("Doc is: " + doc + ", with no object.");
                     }
                  } catch (IOException ex1) {
                     LOG.error("DocId: " + docId + " context.docBase: " + context.docBase, ex1);
                     LOG.error("Error retrieving doc", ex1);
                     LOG.error(ex);
                     throw new RuntimeException(ex);
                  }
               }
            }
         }
      }

      return nids;
   }
}

