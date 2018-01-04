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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.IndexQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.PerFieldAnalyzer;

/**
 * Lucene Manager which specializes in indexing descriptions.
 * 
 * This has been redesigned such that is now creates multiple columns within the index
 * 
 * There is a 'everything' column, which gets all descriptions, to support the standard search where you want to match on a text value
 * anywhere it appears.
 * 
 * There are 3 columns to support FULLY_QUALIFIED_NAME / Synonym / Definition - to support searching that subset of descriptions. There are
 * also data-defined columns to support extended definition types - for example - loinc description types - to support searching terminology
 * specific fields.
 * 
 * Each of the columns above is also x2, as everything is indexed both with a standard analyzer, and with a whitespace analyzer.
 * 
 * @author kec
 * @author aimeefurber
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "description index")
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class DescriptionIndexer extends LuceneIndexer
        implements IndexQueryService {

   /** The Constant FIELD_INDEXED_STRING_VALUE. */
   private static final String FIELD_INDEXED_STRING_VALUE = "_string_content_";
   
   private static final String FIELD_INDEXED_DESCRIPTION_TYPE_NID = "_desc_type_nid_";
   
   private static final String FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID = "_extended_desc_type_uuid_";
   
   /** The Constant INDEX_NAME. */
   public static final String INDEX_NAME = "descriptions-index";
   
   /** The desc extended type sequence. */
   private int descExtendedTypeNid= 0;

   public DescriptionIndexer() throws IOException {
      super(INDEX_NAME);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void addFields(Chronology chronicle, Document doc) {
      if (chronicle instanceof SemanticChronology) {
         final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

         if (semanticChronology.getVersionType() == VersionType.DESCRIPTION) {
            indexDescription(doc, semanticChronology);
            incrementIndexedItemCount("Description");
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
      doc.add(new IntPoint(FIELD_SEMANTIC_ASSEMBLAGE_NID, semanticChronology.getAssemblageNid()));

      String                      lastDescText     = null;
      String                      lastDescType     = null;
      
      // Add a metadata marker for concepts that are metadata, to vastly improve performance of various prefix / filtering searches we want to
      // support in the isaac-rest API
      if (Get.taxonomyService().wasEverKindOf(semanticChronology.getReferencedComponentNid(), MetaData.METADATA____SOLOR.getNid())) {
         doc.add(new TextField(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE, Field.Store.NO));
      }
       
      final Set<Integer> uniqueDescriptionTypes = new HashSet<>();

      for (final StampedVersion stampedVersion : semanticChronology.getVersionList()) {
         DescriptionVersion descriptionVersion = (DescriptionVersion) stampedVersion;

         // No need to index if the text is the same as the previous version.
         if ((lastDescText == null) || (lastDescType == null) || !lastDescText.equals(descriptionVersion.getText())) {
            // Add to the field that carries all text
            addField(doc, FIELD_INDEXED_STRING_VALUE, descriptionVersion.getText(), true);
            uniqueDescriptionTypes.add(descriptionVersion.getDescriptionTypeConceptNid());
            lastDescText = descriptionVersion.getText();
         }
      }
      
      for (Integer i : uniqueDescriptionTypes)
      {
         addField(doc, FIELD_INDEXED_DESCRIPTION_TYPE_NID, i.toString(), false);
      }

      final Set<String> uniqueExtensionTypes = new HashSet<>();

      
      Get.assemblageService().getSemanticChronologyStreamForComponentFromAssemblage(semanticChronology.getNid(), getDescriptionExtendedTypeNid()).forEach(nestedSemantic -> {
         for (Version nestedVersions : nestedSemantic.getVersionList()) {
            // this is a UUID, but we want to treat it as a string anyway
            uniqueExtensionTypes.add(((DynamicVersion<?>) nestedVersions).getData()[0].getDataObject().toString());
         }
      });

      for (String s : uniqueExtensionTypes)
      {
         addField(doc, FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID, s, false);
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

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean indexChronicle(Chronology chronicle) {
      if (chronicle instanceof SemanticChronology && ((SemanticChronology)chronicle).getVersionType() == VersionType.DESCRIPTION) {
         return true;
      }
      return false;
   }

   
   /**
    * An extended query option from the Description Indexer, which allows the following additional criteria:
    * 
    * - specifying that search results should be from the metadata
    * - specifying the description type to search (FQN, Definition, Regular)
    * - specifying the extended description type (description type assemblage concepts from non-snomed terminologies)
    * 
    * Everything else is the same as @see #query(String, boolean, Integer[], Predicate, AmpRestriction, Integer, Integer, Long)
    *
    * @param query The query to apply.
    * @param prefixSearch if true, utilize a search algorithm that is optimized for prefix searching, such as the searching that would be done
    *           to implement a type-ahead style search. Does not use the Lucene Query parser. Every term (or token) that is part of the query
    *           string will be required to be found in the result.
    *
    *           Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a space character
    *           following it, that word will be required as a complete term. If the last word of the query does not have a space character
    *           following it, that word will be required as a prefix match only.
    *
    *           For example: The query "family test" will return results that contain 'Family Testudinidae' The query "family test " will not
    *           match on 'Testudinidae', so that will be excluded.
    * 
    * @param query - The query to apply
    * @param assemblageConcepts - optional - The concept nid(s) of the assemblage that you wish to search within. If null, searches all indexed
    *           content in this index. This could be set to {@link MetaData#DESCRIPTION_ASSEMBLAGE____SOLOR} and/or
    *           {@link MetaData#SCTID____SOLOR} for example, to limit a search to content in those particular assemblages.
    * @param filter - Optional - a parameter that allows application of exclusionary criteria to the returned result. Predicate implementations
    *           will be passed the nids of chronologies which met all other search criteria. To include the chronology in the result, return
    *           true, or false, to have the item excluded.
    * @param amp - optional - The stamp criteria to restrict the search, or no restriction if not provided.
    * @param metadataOnly - Only search descriptions on concepts which are part of the {@link MetaData#ISAAC_METADATA} tree when true,
    *           otherwise, search all descriptions.
    * @param descriptionTypes - optional - if specified, will only match descriptions of the specified type(s).
    * @param extendedDescriptionType - optional - if specified, will only match descriptions with an extension semantic of the specified type(s)
    * @param pageNum - optional - The desired page number of results. Page numbers start with 1.
    * @param sizeLimit - optional - The maximum size of the result list. Pass Integer.MAX_VALUE for unlimited results. Note, utilizing a small
    *           size limit with and passing pageNum is the recommended way of handling large result sets.
    * @param targetGeneration - optional - target generation that must be waited for prior to performing the search or Long.MIN_VALUE if there
    *           is no need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in progress
    *           indexing operations are completed - and then use the latest index. Null behaves the same as Long.MIN_VALUE. See
    *           {@link IndexQueryService#getIndexedGenerationCallable(int)}
    * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that match relative to other
    *         matches.
    */
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         Integer[] assemblageConcepts,
         Predicate<Integer> filter,
         AmpRestriction amp,
         boolean metadataOnly,
         Integer[] descriptionTypes,
         Integer[] extendedDescriptionTypes,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {

      Query q = buildTokenizedStringQuery(query, FIELD_INDEXED_STRING_VALUE, prefixSearch, metadataOnly);

      q = restrictToSemantic(q, assemblageConcepts);

      if (descriptionTypes != null && descriptionTypes.length > 0) {
         final BooleanQuery.Builder outerWrapQueryBuilder = new BooleanQuery.Builder();
         outerWrapQueryBuilder.add(q, Occur.MUST);
         
         final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
         for (Integer i : descriptionTypes)
         {
            innerQueryBuilder.add(new TermQuery(new Term(FIELD_INDEXED_DESCRIPTION_TYPE_NID, i.toString())), Occur.SHOULD);
         }
         
         outerWrapQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
         q = outerWrapQueryBuilder.build();
      }
      
      if (extendedDescriptionTypes != null && extendedDescriptionTypes.length > 0) {
         final BooleanQuery.Builder outerWrapQueryBuilder = new BooleanQuery.Builder();
         outerWrapQueryBuilder.add(q, Occur.MUST);
         
         final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();
         for (int i : extendedDescriptionTypes)
         {
            for (UUID uuid : Get.identifierService().getUuidsForNid(i))
            {
               innerQueryBuilder.add(new TermQuery(new Term(FIELD_INDEXED_EXTENDED_DESCRIPTION_TYPE_UUID, uuid.toString())), Occur.SHOULD);
            }
         }
         
         outerWrapQueryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
         q = outerWrapQueryBuilder.build();
      }
      return search(q, filter, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         Integer[] assemblageConcepts,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      return query(query, prefixSearch, assemblageConcepts, filter, amp, false, null, null, pageNum, sizeLimit, targetGeneration);
   }
   
   public int getDescriptionExtendedTypeNid()
   {
      if (this.descExtendedTypeNid == 0)
      {
         this.descExtendedTypeNid = DynamicConstants.get().DYNAMIC_EXTENDED_DESCRIPTION_TYPE.getNid();
      }
      return this.descExtendedTypeNid;
   }
}
