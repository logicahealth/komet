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
import java.util.List;
import java.util.UUID;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.sememe.SememeChronology;
import sh.isaac.api.index.AssemblageIndexService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.provider.query.lucene.LuceneIndexer;

/**
 *
 * @author kec
 */
@Service(name = "assemblage index")
@RunLevel(value = 2)
public class AssemblageIndexer extends LuceneIndexer
        implements AssemblageIndexService {
   
   public final String ASSEMBLAGE_COMPONENT_COORDINATE = "assemblage-component-coordinate";

   public AssemblageIndexer() throws IOException {
      super("assemblage-index");
   }

   @Override
   public List<SearchResult> query(String query, boolean prefixSearch, Integer[] sememeConceptSequence, int sizeLimit, Long targetGeneration) {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

   @Override
   protected void addFields(Chronology chronicle, Document doc) {
      for (UUID uuid: chronicle.getUuidList()) {
         //TODO add UUID to index...
      }
      
      if (chronicle instanceof SememeChronology) {
         final SememeChronology sememeChronology = (SememeChronology) chronicle;
         incrementIndexedItemCount("Assemblage");
         
         // Field component nid was already added by calling method. Just need to add additional fields. 
         doc.add(new IntPoint(ASSEMBLAGE_COMPONENT_COORDINATE, sememeChronology.getAssemblageSequence(), sememeChronology.getReferencedComponentNid()));
      }
   }

   @Override
   protected boolean indexChronicle(Chronology chronicle) {
      return chronicle instanceof SememeChronology;
   }
   
   @Override
   public NidSet getAttachmentNidsForComponent(int componentNid) {
      return getAttachmentsForComponent(componentNid, Long.MAX_VALUE);
   }

   @Override
   public NidSet getAttachmentNidsInAssemblage(int assemblageSequence) {
      return getAttachmentsInAssemblage(assemblageSequence, Long.MAX_VALUE);
   }
   
   @Override
   public NidSet getAttachmentsForComponentInAssemblage(int componentNid, int assemblageSequence) {
      return getAttachmentsForComponentInAssemblage(componentNid, assemblageSequence, Long.MAX_VALUE);
   }
   
   private NidSet getAttachmentsForComponent(int componentNid, Long targetGeneration) {
      try {
         // assemblage, component
         Query query = IntPoint.newRangeQuery(ASSEMBLAGE_COMPONENT_COORDINATE, new int[] {Integer.MIN_VALUE, componentNid}, new int[] {Integer.MAX_VALUE, componentNid});
         IndexSearcher searcher = getIndexSearcher(targetGeneration);
         NidSetCollectionManager collectionManager = new NidSetCollectionManager(searcher);
         return searcher.search(query, collectionManager);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
  }

   private NidSet getAttachmentsInAssemblage(int assemblageSequence, Long targetGeneration) {
      try {
      Query query = IntPoint.newRangeQuery(ASSEMBLAGE_COMPONENT_COORDINATE, new int[] {assemblageSequence, Integer.MIN_VALUE}, new int[] {assemblageSequence, Integer.MAX_VALUE});
         IndexSearcher searcher = getIndexSearcher(targetGeneration);
         NidSetCollectionManager collectionManager = new NidSetCollectionManager(searcher);
         return searcher.search(query, collectionManager);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }
   
   private NidSet getAttachmentsForComponentInAssemblage(int componentNid, int assemblageSequence, Long targetGeneration) {
      try {
      Query query = IntPoint.newRangeQuery(ASSEMBLAGE_COMPONENT_COORDINATE, new int[] {assemblageSequence, componentNid}, new int[] {assemblageSequence, componentNid});
         IndexSearcher searcher = getIndexSearcher(targetGeneration);
         NidSetCollectionManager collectionManager = new NidSetCollectionManager(searcher);
         return searcher.search(query, collectionManager);
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }
}
