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
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.Get;
import sh.isaac.api.LookupService;
import sh.isaac.api.Status;
import sh.isaac.api.bootstrap.TermAux;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.collections.NidSet;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.constants.DynamicConstants;
import sh.isaac.api.coordinate.StampPrecedence;
import sh.isaac.api.identity.StampedVersion;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.IndexDescriptionQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.util.SemanticTags;
import sh.isaac.model.coordinate.ManifoldCoordinateImpl;
import sh.isaac.model.coordinate.StampCoordinateImpl;
import sh.isaac.model.coordinate.StampPositionImpl;
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
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class DescriptionIndexer extends LuceneIndexer
        implements IndexDescriptionQueryService {

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
   private void indexDescription(Document doc,SemanticChronology semanticChronology) {
      doc.add(new TextField(FIELD_SEMANTIC_ASSEMBLAGE_NID, semanticChronology.getAssemblageNid() + "", Field.Store.NO));

      String                      lastDescText     = null;
      String                      lastDescType     = null;
      
      // Add a metadata marker for concepts that are metadata, to vastly improve performance of various prefix / filtering searches we want to
      // support in the isaac-rest API
      //TODO [DAN 2] switch back to using wasEverKindOf
//      if (Get.taxonomyService().wasEverKindOf(semanticChronology.getReferencedComponentNid(), TermAux.SOLOR_METADATA.getNid())) {
//         doc.add(new TextField(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE, Field.Store.NO));
//      }
      
      boolean isMetadata = false;
      
      final Set<Integer> uniqueDescriptionTypes = new HashSet<>();

      for (final StampedVersion stampedVersion : semanticChronology.getVersionList()) {
         DescriptionVersion descriptionVersion = (DescriptionVersion) stampedVersion;

         if (!isMetadata)
         {
            isMetadata = Get.taxonomyService().getSnapshot(new ManifoldCoordinateImpl(
                  new StampCoordinateImpl(StampPrecedence.PATH, new StampPositionImpl(stampedVersion.getTime(), stampedVersion.getPathNid()), 
                        NidSet.of(stampedVersion.getModuleNid()), Status.ACTIVE_ONLY_SET), null))
            .isKindOf(semanticChronology.getReferencedComponentNid(), TermAux.SOLOR_METADATA.getNid());
         }
         
         // No need to index if the text is the same as the previous version.
         if ((lastDescText == null) || (lastDescType == null) || !lastDescText.equals(descriptionVersion.getText())) {
            // Add to the field that carries all text
            addField(doc, FIELD_INDEXED_STRING_VALUE, descriptionVersion.getText(), true);
            uniqueDescriptionTypes.add(descriptionVersion.getDescriptionTypeConceptNid());
            lastDescText = descriptionVersion.getText();
         }
      }
      
      if (isMetadata)
      {
         doc.add(new TextField(FIELD_CONCEPT_IS_METADATA, FIELD_CONCEPT_IS_METADATA_VALUE, Field.Store.NO));
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
    * {@inheritDoc}
    */
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcepts,
         Predicate<Integer> filter,
         AmpRestriction amp,
         boolean metadataOnly,
         int[] descriptionTypes,
         int[] extendedDescriptionTypes,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      if (!prefixSearch && SemanticTags.containsSemanticTag(query))
      {
         //If they include a semantic tag, adjust their query so that the tag is not treated like a lucene grouping rule.
         //Note, grouping rules are still allowed, so long as they aren't at the very end of the query (so they don't look like a semantic tag)
         query = SemanticTags.stripSemanticTagIfPresent(query) + " \\(" + SemanticTags.findSemanticTagIfPresent(query).get() + "\\)";
      }

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
         int[] assemblageConcepts,
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
