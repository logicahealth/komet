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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.mahout.math.set.OpenIntHashSet;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import sh.isaac.api.LookupService;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.ComponentNidVersion;
import sh.isaac.api.component.semantic.version.DynamicVersion;
import sh.isaac.api.component.semantic.version.LogicGraphVersion;
import sh.isaac.api.component.semantic.version.LongVersion;
import sh.isaac.api.component.semantic.version.StringVersion;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicBoolean;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicByteArray;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicDouble;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicFloat;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicInteger;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicLong;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNid;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicNumeric;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicPolymorphic;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicString;
import sh.isaac.api.component.semantic.version.dynamic.types.DynamicUUID;
import sh.isaac.api.index.AmpRestriction;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.index.SemanticIndexerConfiguration;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.PerFieldAnalyzer;

//~--- classes ----------------------------------------------------------------

/**
 * This class provides indexing for all String, Nid, Long and Logic Graph sememe types.
 *
 * Additionally, this class provides flexible indexing of all DynamicVersion data types, 
 * including all columns within each type..
 * 
 * This indexer does NOT index descriptions, as those are better handled by the {@link DescriptionIndexer}
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "semantic index")
@RunLevel(value = LookupService.SL_L2_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class SemanticIndexer
        extends LuceneIndexer implements IndexSemanticQueryService {

   private static final Logger LOG = LogManager.getLogger();

   /** The Constant INDEX_NAME. */
   public static final String INDEX_NAME = "semantics-index";

   /** The Constant COLUMN_FIELD_DATA. */
   private static final String COLUMN_FIELD_DATA = "colData";

   @Inject
   private SemanticIndexerConfiguration lric;

   /**
    * Instantiates a new semantic indexer.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   private SemanticIndexer()
            throws IOException {
      // For HK2
      super(INDEX_NAME);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void addFields(Chronology chronicle, Document doc) {
      final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

      doc.add(new TextField(FIELD_SEMANTIC_ASSEMBLAGE_NID,
                            semanticChronology.getAssemblageNid() + "",
                            Field.Store.NO));

      for (final Version sv: semanticChronology.getVersionList()) {
         if (sv instanceof DynamicVersion) {
            final DynamicVersion<?> dsv     = (DynamicVersion<?>) sv;
            final Integer[]        columns = this.lric.whatColumnsToIndex(dsv.getAssemblageNid());

            if (columns != null) {
               final int dataColCount = dsv.getData().length;

               for (final int col: columns) {
                  final DynamicData dataCol = (col >= dataColCount) ? null
                        : dsv.getData(col);

                  // Only pass in a column number if we were asked to index more than one column for this sememe
                  handleType(doc, dataCol, (columns.length > 1) ? col
                        : -1);
               }
            }
         }

         // TODO [DAN 3] enhance the index configuration to allow us to configure Static semantics as indexed, or not indexed
         // static sememe types are never more than 1 column, always pass -1
         else if (sv instanceof StringVersion) {
            final StringVersion ssv = (StringVersion) sv;

            handleType(doc, new DynamicStringImpl(ssv.getString()), -1);
            incrementIndexedItemCount("Sememe String");
         } else if (sv instanceof LongVersion) {
            final LongVersion lsv = (LongVersion) sv;

            handleType(doc, new DynamicLongImpl(lsv.getLongValue()), -1);
            incrementIndexedItemCount("Sememe Long");
         } else if (sv instanceof ComponentNidVersion) {
            final ComponentNidVersion csv = (ComponentNidVersion) sv;

            handleType(doc, new DynamicNidImpl(csv.getComponentNid()), -1);
            incrementIndexedItemCount("Sememe Component Nid");
         } else if (sv instanceof LogicGraphVersion) {
            final LogicGraphVersion lgsv = (LogicGraphVersion) sv;
            final OpenIntHashSet  css  = new OpenIntHashSet();

            lgsv.getLogicalExpression().processDepthFirst((LogicNode logicNode,TreeNodeVisitData data) -> {
                                      logicNode.addConceptsReferencedByNode(css);
                                   });
            css.forEachKey(sequence -> {
                           handleType(doc, new DynamicNidImpl(sequence), -1);
                           return true;
                        });
         } else {
            LOG.error("Unexpected type handed to addFields in Sememe Indexer: " + semanticChronology.toString());
         }
      }

      // Due to indexing all of the versions, we may have added duplicate field name/value combinations to the document.
      // Remove the dupes.
      final Iterator<IndexableField> it           = doc.iterator();
      final HashSet<String>          uniqueFields = new HashSet<>();

      while (it.hasNext()) {
         final IndexableField field = it.next();
         final String         temp  = field.name() + "::" + field.stringValue();

         if (uniqueFields.contains(temp)) {
            it.remove();
         } else {
            uniqueFields.add(temp);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean indexChronicle(Chronology chronicle) {
      if (chronicle instanceof SemanticChronology) {
         final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

         if ((semanticChronology.getVersionType() == VersionType.DYNAMIC) ||
               (semanticChronology.getVersionType() == VersionType.STRING) ||
               (semanticChronology.getVersionType() == VersionType.LONG) ||
               (semanticChronology.getVersionType() == VersionType.COMPONENT_NID) ||
               (semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Builds the numeric query.
    *
    * @param queryDataLower the query data lower
    * @param queryDataLowerInclusive the query data lower inclusive
    * @param queryDataUpper the query data upper
    * @param queryDataUpperInclusive the query data upper inclusive
    * @param columnName the column name
    * @return the query
    */
   private Query buildNumericQuery(Number queryDataLower,
                                    boolean queryDataLowerInclusive,
                                   Number queryDataUpper,
                                   boolean queryDataUpperInclusive,
                                   String columnName) {
      // Convert both to the same type (if they differ) - go largest data type to smallest, so we don't lose precision
      // Also - if they pass in longs that would fit in an int, also generate an int query.
      // likewise, with Double - if they pass in a double, that would fit in a float, also generate a float query.
      try {
         final BooleanQuery.Builder bqBuilder          = new BooleanQuery.Builder();
         boolean            fitsInFloat = false;
         boolean            fitsInInt   = false;

         if ((queryDataLower instanceof Double) || (queryDataUpper instanceof Double)) {
            final double upperVal = ((queryDataUpper == null) ? Double.POSITIVE_INFINITY
                  : queryDataUpperInclusive ? queryDataUpper.doubleValue() : DoublePoint.nextDown(queryDataUpper.doubleValue()));
            final double lowerVal = ((queryDataLower == null) ? Double.NEGATIVE_INFINITY
                  : queryDataLowerInclusive ? queryDataLower.doubleValue() : DoublePoint.nextUp(queryDataLower.doubleValue()));

            bqBuilder.add(DoublePoint.newRangeQuery(columnName, lowerVal, upperVal), Occur.SHOULD);

            if (((queryDataUpper != null) && (queryDataUpper.floatValue() <= Float.MAX_VALUE) && (queryDataUpper.floatValue() >= Float.MIN_VALUE))
                  || ((queryDataLower != null) && (queryDataLower.doubleValue() <= Float.MAX_VALUE) && (queryDataLower.doubleValue() >= Float.MIN_VALUE))) {
               fitsInFloat = true;
            }
         }

         if (fitsInFloat || (queryDataLower instanceof Float) || (queryDataUpper instanceof Float)) {
            final float upperVal = (queryDataUpper == null ? Float.POSITIVE_INFINITY
                  : (queryDataUpper instanceof Float ? queryDataUpperInclusive ? queryDataUpper.floatValue() : FloatPoint.nextDown(queryDataUpper.floatValue())
                        : ((fitsInFloat && (queryDataUpper.doubleValue() > Float.MAX_VALUE) ? Float.MAX_VALUE : 
                           queryDataUpperInclusive ? queryDataUpper.floatValue() : FloatPoint.nextDown(queryDataUpper.floatValue())))));
            final float lowerVal = (queryDataLower == null ? Float.NEGATIVE_INFINITY
                  : (queryDataLower instanceof Float ? queryDataLowerInclusive ? queryDataLower.floatValue() : FloatPoint.nextUp(queryDataLower.floatValue())
                        : ((fitsInFloat && (queryDataLower.doubleValue() < Float.MIN_VALUE) ? Float.MIN_VALUE : 
                           queryDataLowerInclusive ? queryDataLower.floatValue() : FloatPoint.nextUp(queryDataLower.floatValue())))));

            bqBuilder.add(FloatPoint.newRangeQuery(columnName, lowerVal, upperVal), Occur.SHOULD);
         }

         if ((queryDataLower instanceof Long) || (queryDataUpper instanceof Long)) {
            final long upperVal = ((queryDataUpper == null) ? Long.MAX_VALUE
                  : queryDataUpperInclusive ? queryDataUpper.longValue() : Math.addExact(queryDataUpper.longValue(),  -1));
            final long lowerVal = ((queryDataLower == null) ? Long.MIN_VALUE
                  : queryDataLowerInclusive ? queryDataLower.longValue() : Math.addExact(queryDataLower.longValue(),  1));

            bqBuilder.add(LongPoint.newRangeQuery(columnName, lowerVal, upperVal), Occur.SHOULD);

            if (((queryDataUpper != null) && (queryDataUpper.longValue() <= Integer.MAX_VALUE) && (queryDataUpper.longValue() >= Integer.MIN_VALUE))
                  || ((queryDataLower != null) && (queryDataLower.longValue() <= Integer.MAX_VALUE) && (queryDataLower.longValue() >= Integer.MIN_VALUE))) {
               fitsInInt = true;
            }
         }

         if (fitsInInt || (queryDataLower instanceof Integer) || (queryDataUpper instanceof Integer)) {
            final int upperVal = ((queryDataUpper == null) ? Integer.MAX_VALUE
                  : ((queryDataUpper instanceof Integer) ? queryDataUpperInclusive ? queryDataUpper.intValue() : Math.addExact(queryDataUpper.intValue(), -1)
                        : ((fitsInInt && queryDataUpper.longValue() > Integer.MAX_VALUE) ? Integer.MAX_VALUE
                              : queryDataUpperInclusive ? queryDataUpper.intValue() : Math.addExact(queryDataUpper.intValue(), -1))));
            final int lowerVal = ((queryDataLower == null) ? Integer.MIN_VALUE
                  : ((queryDataLower instanceof Integer) ? queryDataLowerInclusive ? queryDataLower.intValue() : Math.addExact(queryDataLower.intValue(), 1)
                        : ((fitsInInt && queryDataLower.longValue() < Integer.MIN_VALUE) ? Integer.MIN_VALUE
                              : queryDataLowerInclusive ? queryDataLower.intValue() : Math.addExact(queryDataLower.intValue(), 1))));

            bqBuilder.add(IntPoint.newRangeQuery(columnName, lowerVal, upperVal), Occur.SHOULD);
         }
         BooleanQuery bq = bqBuilder.build();
         if (bq.clauses().isEmpty()) {
            throw new RuntimeException("Not a numeric data type - can't perform a range query");
         } else {
            final BooleanQuery.Builder must = new BooleanQuery.Builder();

            must.add(bq, Occur.MUST);
            return must.build();
         }
      } catch (final ClassCastException e) {
         throw new RuntimeException("One of the values is not a numeric data type - can't perform a range query", e);
      }
   }

   /**
    * Handle type.
    *
    * @param doc the doc
    * @param dataCol the data col
    * @param colNumber the col number
    */
   private void handleType(Document doc, DynamicData dataCol, int colNumber) {
      // Not the greatest design for diskspace / performance... but if we want to be able to support searching across
      // all fields / all sememes - and also support searching per-field within a single sememe, we need to double index
      // all of the data.  Once with a standard field name, and once with a field name that includes the column number.
      // at search time, restricting to certain field matches is only allowed if they are also restricting to an assemblage,
      // so we can compute the correct field number list at search time.
      // Note, we optimize by only doing the double indexing in cases where the sememe has more than one column to begin with.
      // At query time, we construct the query appropriately to handle this optimization.
      // the cheaper option from a disk space perspective (maybe, depending on the data) would be to create a document per
      // column.  The queries would be trivial to write then, but we would be duplicating the component nid and assemblage nid
      // in each document, which is also expensive.  It also doesn't fit the model in OTF, of a document per component.
      // We also duplicate again, on string fields by indexing with the white space analyzer, in addition to the normal one.
      if (dataCol == null) {
         // noop
      } else if (dataCol instanceof DynamicBoolean) {
         doc.add(new StringField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                 ((DynamicBoolean) dataCol).getDataBoolean() + "",
                                 Store.NO));

         if (colNumber >= 0) {
            doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                    ((DynamicBoolean) dataCol).getDataBoolean() + "",
                                    Store.NO));
         }

         incrementIndexedItemCount("Dynamic Boolean");
      } else if (dataCol instanceof DynamicByteArray) {
         LOG.warn("Sememe Indexer configured to index a field that isn''t indexable (byte array)");
      } else if (dataCol instanceof DynamicDouble) {
         doc.add(new DoublePoint(COLUMN_FIELD_DATA, ((DynamicDouble) dataCol).getDataDouble()));

         if (colNumber >= 0) {
            doc.add(new DoublePoint(COLUMN_FIELD_DATA + "_" + colNumber,
                                    ((DynamicDouble) dataCol).getDataDouble()));
         }

         incrementIndexedItemCount("Dynamic Double");
      } else if (dataCol instanceof DynamicFloat) {
         doc.add(new FloatPoint(COLUMN_FIELD_DATA, ((DynamicFloat) dataCol).getDataFloat()));

         if (colNumber >= 0) {
            doc.add(new FloatPoint(COLUMN_FIELD_DATA + "_" + colNumber,
                                   ((DynamicFloat) dataCol).getDataFloat()));
         }

         incrementIndexedItemCount("Dynamic Float");
      } else if (dataCol instanceof DynamicInteger) {
         doc.add(new IntPoint(COLUMN_FIELD_DATA, ((DynamicInteger) dataCol).getDataInteger()));

         if (colNumber >= 0) {
            doc.add(new IntPoint(COLUMN_FIELD_DATA + "_" + colNumber,
                                 ((DynamicInteger) dataCol).getDataInteger()));
         }

         incrementIndexedItemCount("Dynamic Integer");
      } else if (dataCol instanceof DynamicLong) {
         doc.add(new LongPoint(COLUMN_FIELD_DATA, ((DynamicLong) dataCol).getDataLong()));

         if (colNumber >= 0) {
            doc.add(new LongPoint(COLUMN_FIELD_DATA + "_" + colNumber,
                                  ((DynamicLong) dataCol).getDataLong()));
         }

         incrementIndexedItemCount("Dynamic Long");
      } else if (dataCol instanceof DynamicNid) {
         // No need for ranges on a nid, no need for tokenization (so textField, instead of string field).
         doc.add(new StringField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                 ((DynamicNid) dataCol).getDataNid() + "",
                                 Store.NO));

         if (colNumber >= 0) {
            doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                    ((DynamicNid) dataCol).getDataNid() + "",
                                    Store.NO));
         }

         incrementIndexedItemCount("Dynamic Nid");
      } else if (dataCol instanceof DynamicPolymorphic) {
         LOG.error("This should have been impossible (polymorphic?)");
      } else if (dataCol instanceof DynamicString) {
         doc.add(new TextField(COLUMN_FIELD_DATA, ((DynamicString) dataCol).getDataString(), Store.NO));

         if (colNumber >= 0) {
            doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber,
                                  ((DynamicString) dataCol).getDataString(),
                                  Store.NO));
         }

         // yes, indexed 4 different times - twice with the standard analyzer, twice with the whitespace analyzer.
         doc.add(new TextField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                               ((DynamicString) dataCol).getDataString(),
                               Store.NO));

         if (colNumber >= 0) {
            doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                  ((DynamicString) dataCol).getDataString(),
                                  Store.NO));
         }

         incrementIndexedItemCount("Dynamic String");
      } else if (dataCol instanceof DynamicUUID) {
         // Use the whitespace analyzer on UUIDs
         doc.add(new StringField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                 ((DynamicUUID) dataCol).getDataUUID().toString(),
                                 Store.NO));

         if (colNumber >= 0) {
            doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
                                    ((DynamicUUID) dataCol).getDataUUID().toString(),
                                    Store.NO));
         }

         incrementIndexedItemCount("Dynamic UUID");
      } else if (dataCol instanceof DynamicArray) {
         for (final DynamicData nestedData: ((DynamicArray<?>) dataCol).getDataArray()) {
            handleType(doc, nestedData, colNumber);
         }
      } else {
         LOG.error("This should have been impossible (no match on col type) {}", dataCol);
      }
   }

   //~--- inner classes -------------------------------------------------------

   /**
    * The Class QueryWrapperForColumnHandling.
    */
   private abstract class QueryWrapperForColumnHandling {
      /**
       * Builds the query.
       *
       * @param columnName the column name
       * @return the query
       */
      abstract Query buildQuery(String columnName);

      /**
       * Builds the column handling query.
       *
       * @param assemblageConceptNid the sememe concept sequence
       * @param searchColumns the search columns
       * @return the query
       */
      protected Query buildColumnHandlingQuery(int[] assemblageConceptNid, int[] searchColumns) {
         Integer[] assemblageIndexedColumns = null;

         if ((searchColumns != null) && (searchColumns.length > 0)) {
            // If they provide a search column - then they MUST provide one and only one assemblageConceptNid
            if ((assemblageConceptNid == null) || (assemblageConceptNid.length != 1)) {
               throw new RuntimeException(
                   "If a list of search columns is provided, then the assemblageConceptNid variable must contain 1 (and only 1) assemblage id");
            } else {
               assemblageIndexedColumns = SemanticIndexer.this.lric.whatColumnsToIndex(assemblageConceptNid[0]);
            }
         }

         // If only 1 column was indexed from a sememe, we don't create field specific columns.
         if ((searchColumns == null) ||
               (searchColumns.length == 0) ||
               (assemblageIndexedColumns == null) ||
               (assemblageIndexedColumns.length < 2)) {
            return buildQuery(COLUMN_FIELD_DATA);
         } else  // If they passed a specific column to search AND the Dynamic type has more than 1 indexed column, then do a column specific search.
         {
            final BooleanQuery.Builder group = new BooleanQuery.Builder();

            for (final int i: searchColumns) {
               group.add(buildQuery(COLUMN_FIELD_DATA + "_" + i), Occur.SHOULD);
            }

            return group.build();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> query(String query,
         boolean prefixSearch,
         int[] assemblageConcept,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      return queryData(new DynamicStringImpl(query), prefixSearch, assemblageConcept, null, filter, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> queryNumericRange(Number queryDataLower,
         boolean queryDataLowerInclusive,
         Number queryDataUpper,
         boolean queryDataUpperInclusive,
         int[] assemblageConcepts,
         int[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      Query q = new QueryWrapperForColumnHandling()
      {
         @Override
         Query buildQuery(String columnName)
         {
            return buildNumericQuery(queryDataLower, queryDataLowerInclusive, queryDataUpper, queryDataUpperInclusive, columnName);
         }
      }.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
      
      
      return search(restrictToSemantic(q, assemblageConcepts), filter, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> queryNidReference(int nid,
         int[] assemblageConcepts,
         int[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      
      final Query q = new QueryWrapperForColumnHandling() {
         @Override
         Query buildQuery(String columnName) {
            return new TermQuery(new Term(columnName + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, nid + ""));
         }
      }.buildColumnHandlingQuery(assemblageConcepts, searchColumns);

      return search(restrictToSemantic(q, assemblageConcepts), filter, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> queryData(DynamicData queryData,
         boolean prefixSearch,
         int[] assemblageConcept,
         int[] searchColumns,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      return queryData(queryData, prefixSearch, assemblageConcept, searchColumns, null, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> queryData(DynamicData queryData,
         boolean prefixSearch,
         int[] assemblageConcepts,
         int[] searchColumns,
         Predicate<Integer> filter,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      Query q = null;

      if (queryData instanceof DynamicString) {
         q = new QueryWrapperForColumnHandling() {
            @Override
            Query buildQuery(String columnName) {
               // This is the only query type that needs tokenizing, etc.
               String queryString = ((DynamicString) queryData).getDataString();

               // '-' signs are operators to lucene... but we want to allow nid lookups. So escape any leading hyphens
               // and any hyphens that are preceeded by spaces. This way, we don't mess up UUID handling.
               // (lucene handles UUIDs ok, because the - sign is only treated special at the beginning, or when preceeded by a space)
               if (queryString.startsWith("-")) {
                  queryString = "\\" + queryString;
               }

               queryString = queryString.replaceAll("\\s-", " \\\\-");
               LOG.debug("Modified search string is: ''{}''", queryString);
               return buildTokenizedStringQuery(queryString, columnName, prefixSearch, false);
            }
         }.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
      } else {
         if ((queryData instanceof DynamicBoolean) || (queryData instanceof DynamicNid) || (queryData instanceof DynamicUUID)) {
            q = new QueryWrapperForColumnHandling() {
               @Override
               Query buildQuery(String columnName) {
                  return new TermQuery(new Term(columnName + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, queryData.getDataObject().toString()));
               }
            }.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
            //By checking for DynamicNumeric, we inadvertently capture nid, but that is already handled in the if above.
         } else if ((queryData instanceof DynamicNumeric)) {  
            q = new QueryWrapperForColumnHandling() {
               @Override
               Query buildQuery(String columnName) {
                  Query temp = buildNumericQuery(((DynamicNumeric) queryData).getDataNumeric(), true, ((DynamicNumeric) queryData).getDataNumeric(), true, columnName);

                  if (((queryData instanceof DynamicLong) && ((DynamicLong) queryData).getDataLong() < 0)
                        || ((queryData instanceof DynamicInteger) && ((DynamicInteger) queryData).getDataInteger() < 0)) {
                     // Looks like a nid... wrap in an or clause that would do a match on the exact term if it was indexed as a nid, rather than a numeric
                     final BooleanQuery.Builder wrapper = new BooleanQuery.Builder();

                     wrapper.add(new TermQuery(new Term(columnName, queryData.getDataObject().toString())), Occur.SHOULD);
                     wrapper.add(temp, Occur.SHOULD);
                     temp = wrapper.build();
                  }

                  return temp;
               }
            }.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
         } else if (queryData instanceof DynamicByteArray) {
            throw new RuntimeException("DynamicSememeByteArray isn't indexed");
         } else if (queryData instanceof DynamicPolymorphic) {
            throw new RuntimeException("This should have been impossible (polymorphic?)");
         } else if (queryData instanceof DynamicArray) {
            throw new RuntimeException("DynamicSememeArray isn't a searchable type");
         } else {
            LOG.error("This should have been impossible (no match on col type)");
            throw new RuntimeException("unexpected error, see logs");
         }
      }

      return search(restrictToSemantic(q, assemblageConcepts), filter, amp, pageNum, sizeLimit, targetGeneration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<SearchResult> queryData(String queryString,
         boolean prefixSearch,
         int[] assemblageConcept,
         AmpRestriction amp,
         Integer pageNum,
         Integer sizeLimit,
         Long targetGeneration) {
      return queryData(new DynamicStringImpl(queryString), prefixSearch, assemblageConcept, null, null, amp, pageNum, sizeLimit, targetGeneration);
   }
}

