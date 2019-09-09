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
package sh.isaac.provider.query.lucene;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import sh.isaac.api.Get;
import sh.isaac.api.externalizable.IsaacObjectType;

/**
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class IsaacFilteredCollectorManager implements CollectorManager<IsaacFilteredCollectorManager.IsaacFilteredCollector, TopDocs> {

   final Predicate<Integer> filter;
   final int sizeLimit;
   final ScoreDoc after;
   final ArrayList<IsaacFilteredCollector> collectors = new ArrayList<>();
   protected static final Logger LOG = LogManager.getLogger();

   /**
    * 
    * @param filter - optional - arbitrary filtering
    * @param sizeLimit - max number of results to return
    * @param after - optional - specify to begin on a future page of results.  Should be the last ScoreDoc from a previous query.
    */
   public IsaacFilteredCollectorManager(Predicate<Integer> filter, int sizeLimit, ScoreDoc after) {
      this.filter = filter;
      this.sizeLimit = sizeLimit;
      this.after = after;
   }
   
   TopDocs getTopDocs() throws IOException {
      return reduce(collectors);
   }

   @Override
   public IsaacFilteredCollector newCollector() throws IOException {
      IsaacFilteredCollector collector =  new IsaacFilteredCollector();
      collectors.add(collector);
      return collector;
   }

   @Override
   public TopDocs reduce(Collection<IsaacFilteredCollectorManager.IsaacFilteredCollector> collectors) throws IOException {
      final TopDocs[] topDocs = new TopDocs[collectors.size()];
      int i = 0;
      int totalSize = 0;
      for (IsaacFilteredCollectorManager.IsaacFilteredCollector collector : collectors) {
         topDocs[i] = collector.topDocs();
         totalSize = totalSize + topDocs[i++].scoreDocs.length;
      }
      return TopDocs.merge(0, Math.min(sizeLimit, totalSize), topDocs, true);
   }

   public class IsaacFilteredCollector implements Collector {
      HashMap<LeafReaderContext, LeafCollector> leafDelegates = new HashMap<>();
      TopScoreDocCollector delegateCollector = TopScoreDocCollector.create(sizeLimit, after);
      
      @Override
      public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
         if (leafDelegates.get(context) == null)
         {
            leafDelegates.put(context, new LeafCollector() 
            {
               LeafCollector internalDelegate = delegateCollector.getLeafCollector(context);
               
               @Override
               public void setScorer(Scorer scorer) throws IOException {
                  internalDelegate.setScorer(scorer);
                  
               }
               
               @Override
               public void collect(int doc) throws IOException {
                  boolean filterPass = false;
                  int afterDoc = (after == null ? 0 : after.doc) - context.docBase;
                  if (doc < afterDoc)
                  {
                     //This little optimization prevents us from re-evaluating filters and from re-collecting the same docs more than once
                      LOG.trace("skipping filter eval and collect for item before page 1");
                      filterPass = false;
                  }
                  if (filter != null)
                  {
                    final Document document = context.reader().document(doc);
                    final int componentNid = document.getField(LuceneIndexer.FIELD_COMPONENT_NID)
                            .numericValue()
                            .intValue();
                     if (Get.identifierService().getObjectTypeForComponent(componentNid) == IsaacObjectType.SEMANTIC) {
                        try {
                           filterPass = filter.test(componentNid);
                        } catch (NoSuchElementException | IllegalStateException e) {
                           StringBuilder b = new StringBuilder();
                           b.append(e.getLocalizedMessage()).append("\n");
                           b.append("Filtering document: ").append(document).append("\n");
                           b.append("Evaluating: ").append(componentNid).append(" uuids: ")
                                   .append(Arrays.toString(Get.identifierService().getUuidArrayForNid(componentNid)));
                           LOG.error(b.toString());
                        }
                     } else {
                        LOG.warn("Concept nid in search results: " + componentNid + " " + document);
                     }
                  }
                  else
                  {
                     filterPass = true;
                  }
                  
                  if (filterPass)
                  {
                     internalDelegate.collect(doc);
                  }
               }
            });
         }
         return leafDelegates.get(context);
      }

      @Override
      public boolean needsScores() {
         return delegateCollector.needsScores();
      }
      
      public TopDocs topDocs() {
         return delegateCollector.topDocs();
      }
   }
}
