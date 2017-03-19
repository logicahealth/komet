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

import java.util.function.Predicate;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;

import sh.isaac.provider.query.lucene.LuceneIndexer;

//~--- classes ----------------------------------------------------------------

/**
 * {@link TopDocsFilteredCollector}.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class TopDocsFilteredCollector
        extends Collector {
   /** The collector. */
   TopScoreDocCollector collector_;

   /** The searcher. */
   IndexSearcher searcher_;

   /** The filter. */
   Predicate<Integer> filter_;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new top docs filtered collector.
    *
    * @param numHits - how many results to return
    * @param query - needed to setup the TopScoreDocCollector properly
    * @param searcher - needed to read the nids out of the matching documents
    * @param filter - a predicate that should return true, if the given nid should be allowed in the results, false, if not.
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TopDocsFilteredCollector(int numHits,
                                   Query query,
                                   IndexSearcher searcher,
                                   Predicate<Integer> filter)
            throws IOException {
      this.collector_ = TopScoreDocCollector.create(numHits,
            null,
            !searcher.createNormalizedWeight(query)
                     .scoresDocsOutOfOrder());
      this.searcher_ = searcher;
      this.filter_   = filter;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Accepts docs out of order.
    *
    * @return true, if successful
    */
   @Override
   public boolean acceptsDocsOutOfOrder() {
      return this.collector_.acceptsDocsOutOfOrder();
   }

   /**
    * Collect.
    *
    * @param docId the doc id
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void collect(int docId)
            throws IOException {
      final Document document     = this.searcher_.doc(docId);
      final int      componentNid = document.getField(LuceneIndexer.FIELD_COMPONENT_NID)
                                            .numericValue()
                                            .intValue();

      if (this.filter_.test(componentNid)) {
         this.collector_.collect(docId);
      }
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the next reader.
    *
    * @param context the new next reader
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void setNextReader(AtomicReaderContext context)
            throws IOException {
      this.collector_.setNextReader(context);
   }

   /**
    * Sets the scorer.
    *
    * @param scorer the new scorer
    * @throws IOException Signals that an I/O exception has occurred.
    */
   @Override
   public void setScorer(Scorer scorer)
            throws IOException {
      this.collector_.setScorer(scorer);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the top docs.
    *
    * @return the top docs
    */
   public TopDocs getTopDocs() {
      return this.collector_.topDocs();
   }
}

