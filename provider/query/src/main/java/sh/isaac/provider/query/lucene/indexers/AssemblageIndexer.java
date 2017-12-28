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
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampCoordinate;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.SearchResult;
import sh.isaac.provider.query.lucene.LuceneDescriptionType;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.PerFieldAnalyzer;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.index.IndexService;

/**
 * Lucene Manager for an assemblage index. Provides the description indexing
 service also.

 This has been redesigned such that is now creates multiple columns within the
 index

 There is a 'everything' column, which gets all descriptions, to support the
 standard search where you want to match on a text value anywhere it appears.

 There are 3 columns to support FULLY_QUALIFIED_NAME / Synonym / Definition - to support
 searching that subset of descriptions. There are also data-defined columns to
 support extended definition types - for example - loinc description types -
 to support searching terminology specific fields.

 Each of the columns above is also x2, as everything is indexed both with a
 standard analyzer, and with a whitespace analyzer.
 
 TODO: use IntPoint for description types, and other aspects of the search, rather than creating redundant
 columns. 
 * 
 * @author kec
 * @author aimeefurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "assemblage index")
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class AssemblageIndexer extends LuceneIndexer
        implements IndexService {
   
   /** The Constant SETUP_NIDS_SEMAPHORE. */
   private static final Semaphore SETUP_NIDS_SEMAPHORE = new Semaphore(1);

   /** The Constant SEQUENCES_SETUP. */
   private static final AtomicBoolean SEQUENCES_SETUP = new AtomicBoolean(false);

   /** The Constant FIELD_INDEXED_STRING_VALUE. */
   private static final String FIELD_INDEXED_STRING_VALUE = "_string_content_";

   /** The sequence type map. */
   private final HashMap<Integer, String> sequenceTypeMap = new HashMap<>();
   
      /** The desc extended type sequence. */
   private int descExtendedTypeSequence;

   // TODO persist dataStoreId.
   private final UUID dataStoreId = UUID.randomUUID();

   @Override
   public UUID getDataStoreId() {
      return dataStoreId;
   }

   public AssemblageIndexer() throws IOException {
      super("assemblage-index");
   }

   @Override
   protected void addFields(Chronology chronicle, Document doc) {
      for (UUID uuid: chronicle.getUuidList()) {
         //TODO add UUID to index... ?
      }
      
      if (chronicle instanceof SemanticChronology) {
         final SemanticChronology semanticChronology = (SemanticChronology) chronicle;
         incrementIndexedItemCount("Assemblage");
         

         if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
            indexDescription(doc,
                             (SemanticChronology) semanticChronology);
            incrementIndexedItemCount("Description");
         }
      }
   }

   /**
    * Setup nid constants.
    */
   private void setupNidConstants() {
      // Can't put these in the start me, because if the database is not yet imported, then these calls will fail.
      // TODO: could put them in service setup, if service levels are set properly. 
      if (!SEQUENCES_SETUP.get()) {
         SETUP_NIDS_SEMAPHORE.acquireUninterruptibly();

         try {
            if (!SEQUENCES_SETUP.get()) {
               this.sequenceTypeMap.put(TermAux.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.getNid(),
                                        LuceneDescriptionType.FULLY_QUALIFIED_NAME.name());
               this.sequenceTypeMap.put(TermAux.DEFINITION_DESCRIPTION_TYPE.getNid(),
                                        LuceneDescriptionType.DEFINITION.name());
               this.sequenceTypeMap.put(TermAux.REGULAR_NAME_DESCRIPTION_TYPE.getNid(), LuceneDescriptionType.REGULAR_NAME.name());
               this.descExtendedTypeSequence = DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE
                     .getNid();
            }

            SEQUENCES_SETUP.set(true);
         } finally {
            SETUP_NIDS_SEMAPHORE.release();
         }
      }
   }

   /**
    * Index description.
    *
    * @param doc the doc
    * @param semanticChronology the semantic chronology
    */
   private void indexDescription(Document doc,
                                 SemanticChronology semanticChronology) {
      doc.add(new IntPoint(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, semanticChronology.getAssemblageNid()));

      String                      lastDescText     = null;
      String                      lastDescType     = null;
      
      // Add a metadata marker for concepts that are metadata, to vastly improve performance of various prefix / filtering searches we want to
      // support in the isaac-rest API
      if (Get.taxonomyService().wasEverKindOf(semanticChronology.getReferencedComponentNid(), MetaData.METADATA____SOLOR.getNid())) {
         doc.add(new TextField(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE, Field.Store.NO));
      }
       
      final TreeMap<Long, String> uniqueTextValues = new TreeMap<>();

      for (final StampedVersion stampedVersion:
            semanticChronology.getVersionList()) {
         DescriptionVersion descriptionVersion = (DescriptionVersion) stampedVersion;
         final String descType = this.sequenceTypeMap.get(descriptionVersion.getDescriptionTypeConceptNid());

         // No need to index if the text is the same as the previous version.
         if ((lastDescText == null) ||
               (lastDescType == null) ||
               !lastDescText.equals(descriptionVersion.getText()) ||
               !lastDescType.equals(descType)) {
            // Add to the field that carries all text
            addField(doc, FIELD_INDEXED_STRING_VALUE, descriptionVersion.getText(), true);

            // Add to the field that carries type-only text
            // TODO using IntPoint with description type? 
            addField(doc, FIELD_INDEXED_STRING_VALUE + "_" + descType, descriptionVersion.getText(), true);
            uniqueTextValues.put(descriptionVersion.getTime(), descriptionVersion.getText());
            lastDescText = descriptionVersion.getText();
            lastDescType = descType;
         }
      }
   }


   /**
    * Adds the field.
    *
    * @param doc the doc
    * @param fieldName the field name
    * @param value the value
    * @param tokenize the tokenize
    */
   private void addField(Document doc, String fieldName, String value, boolean tokenize) {
      // index twice per field - once with the standard analyzer, once with the whitespace analyzer.
      if (tokenize) {
         doc.add(new TextField(fieldName, value, Field.Store.NO));
      }

      doc.add(new TextField(fieldName + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, value, Field.Store.NO));
   }

   @Override
   protected boolean indexChronicle(Chronology chronicle) {
      setupNidConstants();
      return chronicle instanceof SemanticChronology;
   }
   
   /**
    * Search the specified description type.
    *
    * @param query The query to apply
    * @param descriptionType - The type of description to search. If this is
    * passed in as null, this falls back to a standard description search that
    * searches all description types
    * @param sizeLimit The maximum size of the result list.
    * @param targetGeneration target generation that must be included in the
    * search or Long.MIN_VALUE if there is no need to wait for a target
    * generation. Long.MAX_VALUE can be passed in to force this query to wait
    * until any in progress indexing operations are completed - and then use
    * the latest index.
    * @return a List of <code>SearchResult</codes> that contains the nid of the
    * component that matched, and the score of that match relative to other
    * matches.
    */
   public final List<SearchResult> query(String query,
         LuceneDescriptionType descriptionType,
         int sizeLimit,
         Long targetGeneration) {
      if (descriptionType == null) {
         return super.query(query, (Integer[]) null, sizeLimit, targetGeneration, null);
      } else {
         return search(buildTokenizedStringQuery(query,
               FIELD_INDEXED_STRING_VALUE + "_" + descriptionType.name(),
               false, false),
                       sizeLimit,
                       targetGeneration,
                       null);
      }
   }

   /**
    * Search the specified description type.
    *
    *
    * @param query The query to apply
    * @param extendedDescriptionType - The UUID of an extended description type
    * - should be a child of the concept "Description type in source
    * terminology (ISAAC)" If this is passed in as null,
    * this falls back to a standard description search that searches all
    * description types
    * @param sizeLimit The maximum size of the result list.
    * @param targetGeneration target generation that must be included in the
    * search or Long.MIN_VALUE if there is no need to wait for a target
    * generation. Long.MAX_VALUE can be passed in to force this query to wait
    * until any in progress indexing operations are completed - and then use
    * the latest index.
    * @return a List of <code>SearchResult</codes> that contains the nid of the
    * component that matched, and the score of that match relative to other
    * matches.
    */
   public final List<SearchResult> query(String query,
         UUID extendedDescriptionType,
         int sizeLimit,
         Long targetGeneration) {
      if (extendedDescriptionType == null) {
         return super.query(query, (Integer[]) null, sizeLimit, targetGeneration, null);
      } else {
         return search(buildTokenizedStringQuery(query,
               FIELD_INDEXED_STRING_VALUE + "_" + extendedDescriptionType.toString(),
               false, false),
                       sizeLimit,
                       targetGeneration,
                       null);
      }
   }

   /**
    * A generic query API that handles most common cases.  The cases handled for various component property types
    * are detailed below.
    *
    * NOTE - subclasses of LuceneIndexer may have other query(...) methods that allow for more specific and or complex
    * queries.  Specifically both {@link DynamicSememeIndexer} and {@link DescriptionIndexer} have their own
    * query(...) methods which allow for more advanced queries.
    *
    * @param query The query to apply.
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching
    * that would be done to implement a type-ahead style search.  Does not use the Lucene Query parser.  Every term (or token)
    * that is part of the query string will be required to be found in the result.
    *
    * Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a
    * space character following it, that word will be required as a complete term.  If the last word of the query does not
    * have a space character following it, that word will be required as a prefix match only.
    *
    * For example:
    * The query "family test" will return results that contain 'Family Testudinidae'
    * The query "family test " will not match on  'Testudinidae', so that will be excluded.
    * @param sememeConceptSequence the sememe concept sequence
    * @param sizeLimit The maximum size of the result list.
    * @param targetGeneration target generation that must be included in the search or Long.MIN_VALUE if there is no need
    * to wait for a target generation.  Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    * indexing operations are completed - and then use the latest index.
    * @return a List of {@link SearchResult} that contains the nid of the component that matched, and the score of that match relative
    * to other matches.
    */
   @Override
   public List<SearchResult> query(String query,
                                   boolean prefixSearch,
                                   Integer[] sememeConceptSequence,
                                   int sizeLimit,
                                   Long targetGeneration,
                                   StampCoordinate sc) {
      return search(restrictToSememe(buildTokenizedStringQuery(query, FIELD_INDEXED_STRING_VALUE, prefixSearch, false),
                                     sememeConceptSequence),
                    sizeLimit,
                    targetGeneration,
                    null);
   }

   /**
    * A generic query API that handles most common cases.  The cases handled for various component property types
    * are detailed below.
    *
    * NOTE - subclasses of LuceneIndexer may have other query(...) methods that allow for more specific and or complex
    * queries.  Specifically both {@link DynamicSememeIndexer} and {@link DescriptionIndexer} have their own
    * query(...) methods which allow for more advanced queries.
    *
    * @param query The query to apply.
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching
    * that would be done to implement a type-ahead style search.  Does not use the Lucene Query parser.  Every term (or token)
    * that is part of the query string will be required to be found in the result.
    *
    * Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a
    * space character following it, that word will be required as a complete term.  If the last word of the query does not
    * have a space character following it, that word will be required as a prefix match only.
    *
    * For example:
    * The query "family test" will return results that contain 'Family Testudinidae'
    * The query "family test " will not match on  'Testudinidae', so that will be excluded.
    * @param sememeConceptSequence the sememe concept sequence
    * @param sizeLimit The maximum size of the result list.
    * @param targetGeneration target generation that must be included in the search or Long.MIN_VALUE if there is no need
    * to wait for a target generation.  Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    * indexing operations are completed - and then use the latest index.
    * @param filter - an optional filter on results - if provided, the filter should expect nids, and can return true, if
    * the nid should be allowed in the result, false otherwise.  Note that this may cause large performance slowdowns, depending
    * on the implementation of your filter
    * @param metadataOnly - Only search descriptions on concepts which are part of the {@link MetaData#ISAAC_METADATA} tree when true, 
     * otherwise, search all descriptions.
     * @param sc - the optional stamp coordinate to use during the search.
    * @return a List of {@link SearchResult} that contains the nid of the component that matched, and the score of that match relative
    * to other matches.
    */
   public List<SearchResult> query(String query,
                                   boolean prefixSearch,
                                   Integer[] sememeConceptSequence,
                                   int sizeLimit,
                                   Long targetGeneration,
                                   Predicate<Integer> filter,
                                   boolean metadataOnly,
                                   StampCoordinate sc) {
      return search(restrictToSememe(buildTokenizedStringQuery(query, FIELD_INDEXED_STRING_VALUE, prefixSearch, metadataOnly),
                                     sememeConceptSequence),
                    sizeLimit,
                    targetGeneration,
                    filter);
      
      
   }

   @Override
   public Future<Void> sync() {
      throw new UnsupportedOperationException();
   }
}
