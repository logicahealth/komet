/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import jakarta.annotation.PreDestroy;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import javafx.concurrent.Task;
import sh.isaac.api.Get;
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
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion;
import sh.isaac.api.component.semantic.version.brittle.BrittleVersion.BrittleDataTypes;
import sh.isaac.api.component.semantic.version.dynamic.DynamicColumnInfo;
import sh.isaac.api.component.semantic.version.dynamic.DynamicData;
import sh.isaac.api.component.semantic.version.dynamic.DynamicUsageDescription;
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
import sh.isaac.api.index.AuthorModulePathRestriction;
import sh.isaac.api.index.IndexSemanticQueryService;
import sh.isaac.api.index.SearchResult;
import sh.isaac.api.logic.LogicNode;
import sh.isaac.api.tree.TreeNodeVisitData;
import sh.isaac.model.semantic.DynamicUsageDescriptionImpl;
import sh.isaac.model.semantic.types.DynamicBooleanImpl;
import sh.isaac.model.semantic.types.DynamicFloatImpl;
import sh.isaac.model.semantic.types.DynamicIntegerImpl;
import sh.isaac.model.semantic.types.DynamicLongImpl;
import sh.isaac.model.semantic.types.DynamicNidImpl;
import sh.isaac.model.semantic.types.DynamicStringImpl;
import sh.isaac.provider.query.lucene.LuceneIndexer;
import sh.isaac.provider.query.lucene.PerFieldAnalyzer;

/**
 * This class provides indexing for all String, Nid, Long and Logic Graph semantic types.
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
@RunLevel(value = LookupService.SL_L3_DATABASE_SERVICES_STARTED_RUNLEVEL)
public class SemanticIndexer extends LuceneIndexer implements IndexSemanticQueryService
{
	public static final String INDEX_NAME = "semantics-index";

	private static final String COLUMN_STRING_FIELD_DATA = "sColData";
	private static final String COLUMN_STRING_FIELD_DATA_TOKENIZED = "sColDataT";
	private static final String COLUMN_INT_FIELD_DATA = "iColData";
	private static final String COLUMN_LONG_FIELD_DATA = "lColData";
	private static final String COLUMN_FLOAT_FIELD_DATA = "fColData";
	private static final String COLUMN_DOUBLE_FIELD_DATA = "dColData";

	private static final String INDEX_CONFIG = "INDEX_CONFIG_STORE";

	/**
	 * A cache that records the columns to be indexed for each assemblage in the system. This is dynamically created as needed.
	 * The only data that is stored, is the information about what columns to NOT index, which is stored in the database metadata store.
	 */
	private ConcurrentHashMap<Integer, Integer[]> columnsToIndexCache = new ConcurrentHashMap<>();

	/**
	 * Ref to the metacontent store instanced that stores our exclude configs
	 */
	private ConcurrentMap<Integer, Integer[]> store;

	/**
	 * Instantiates a new semantic indexer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private SemanticIndexer() throws IOException
	{
		// For HK2
		super(INDEX_NAME);
		store = Get.metaContentService().<Integer, Integer[]> openStore(INDEX_CONFIG);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(Chronology chronicle, Document doc, Set<Integer> pathNids)
	{
		final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

		doc.add(new TextField(FIELD_SEMANTIC_ASSEMBLAGE_NID, semanticChronology.getAssemblageNid() + "", Field.Store.NO));

		for (final Version sv : semanticChronology.getVersionList())
		{
			if (sv instanceof DynamicVersion)
			{
				final DynamicVersion<?> dsv = (DynamicVersion<?>) sv;
				final Integer[] columns = getColumnsToIndex(semanticChronology, dsv.getAssemblageNid()); 

				if (columns != null)
				{
					final int dataColCount = dsv.getData().length;

					for (final int col : columns)
					{
						final DynamicData dataCol = (col >= dataColCount) ? null : dsv.getData(col);

						// Only pass in a column number if we were asked to index more than one column for this semantic
						handleType(doc, dataCol, (columns.length > 1) ? col : -1);
					}
				}
			}

			// TODO [DAN 3] enhance the index configuration to allow us to configure Static semantics as indexed, or not indexed
			// static semantic types are never more than 1 column, always pass -1
			else if (sv instanceof StringVersion)
			{
				final StringVersion ssv = (StringVersion) sv;

				handleType(doc, new DynamicStringImpl(ssv.getString()), -1);
				incrementIndexedItemCount("Semantic String");
			}
			else if (sv instanceof LongVersion)
			{
				final LongVersion lsv = (LongVersion) sv;

				handleType(doc, new DynamicLongImpl(lsv.getLongValue()), -1);
				incrementIndexedItemCount("Semantic Long");
			}
			else if (sv instanceof ComponentNidVersion)
			{
				final ComponentNidVersion csv = (ComponentNidVersion) sv;

				handleType(doc, new DynamicNidImpl(csv.getComponentNid()), -1);
				incrementIndexedItemCount("Semantic Component Nid");
			}
			else if (sv instanceof LogicGraphVersion)
			{
				final LogicGraphVersion lgsv = (LogicGraphVersion) sv;
				final RoaringBitmap css  = new RoaringBitmap();

				lgsv.getLogicalExpression().processDepthFirst((LogicNode logicNode, TreeNodeVisitData data) -> {
					logicNode.addConceptsReferencedByNode(css);
				});
				css.forEach((IntConsumer) sequence -> {
					handleType(doc, new DynamicNidImpl(sequence), -1);
				});
			}
			else if (sv instanceof BrittleVersion)
			{
				BrittleVersion bv = (BrittleVersion) sv;

				BrittleDataTypes[] types = bv.getFieldTypes();
				Object[] fieldData = bv.getDataFields();
				for (int i = 0; i < fieldData.length; i++)
				{
					if (fieldData[i] == null)
					{
						continue;
					}
					if (null == types[i])
					{
						LOG.error("Unexpected type handed to addFields in Semantic Indexer: " + types[i]);
					}
					else
						switch (types[i])
						{
							case STRING:
								handleType(doc, new DynamicStringImpl((String) fieldData[i]), types.length > 1 ? i : -1);
								break;
							case NID:
								handleType(doc, new DynamicNidImpl((Integer) fieldData[i]), types.length > 1 ? i : -1);
								break;
							case INTEGER:
								handleType(doc, new DynamicIntegerImpl((Integer) fieldData[i]), types.length > 1 ? i : -1);
								break;
							case FLOAT:
								handleType(doc, new DynamicFloatImpl((Float) fieldData[i]), types.length > 1 ? i : -1);
								break;
							case BOOLEAN:
								handleType(doc, new DynamicBooleanImpl((Boolean) fieldData[i]), types.length > 1 ? i : -1);
								break;
							default :
								LOG.error("Unexpected type handed to addFields in Semantic Indexer: " + types[i]);
								break;
						}
				}
				incrementIndexedItemCount(sv.getSemanticType().name());
			}
			else
			{
				LOG.error("Unexpected type handed to addFields in Semantic Indexer: " + semanticChronology.toString());
			}
		}

		// Due to indexing all of the versions, we may have added duplicate field name/value combinations to the document.
		// Remove the dupes.
		final Iterator<IndexableField> it = doc.iterator();
		final HashSet<String> uniqueFields = new HashSet<>();

		while (it.hasNext())
		{
			final IndexableField field = it.next();
			final String temp = field.name() + "::" + field.stringValue();

			if (uniqueFields.contains(temp))
			{
				it.remove();
			}
			else
			{
				uniqueFields.add(temp);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean indexChronicle(Chronology chronicle)
	{
		if (chronicle instanceof SemanticChronology)
		{
			final SemanticChronology semanticChronology = (SemanticChronology) chronicle;

			if ((semanticChronology.getVersionType() == VersionType.DYNAMIC) || (semanticChronology.getVersionType() == VersionType.STRING)
					|| (semanticChronology.getVersionType() == VersionType.LONG) || (semanticChronology.getVersionType() == VersionType.COMPONENT_NID)
					|| (semanticChronology.getVersionType() == VersionType.LOGIC_GRAPH))
			{
				return true;
			}
			else if (semanticChronology instanceof BrittleVersion)
			{
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
	 * @param columnNamePostFixIfAny the column position idenifier, if any.
	 * @return the query
	 */
	private Query buildNumericQuery(Number queryDataLower, boolean queryDataLowerInclusive, Number queryDataUpper, boolean queryDataUpperInclusive,
			String columnNamePostFixIfAny)
	{
		// Convert both to the same type (if they differ) - go largest data type to smallest, so we don't lose precision
		// Also - if they pass in longs that would fit in an int, also generate an int query.
		// likewise, with Double - if they pass in a double, that would fit in a float, also generate a float query.
		try
		{
			final BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
			boolean fitsInFloat = false;
			boolean fitsInInt = false;

			if ((queryDataLower instanceof Double) || (queryDataUpper instanceof Double))
			{
				final double upperVal = ((queryDataUpper == null) ? Double.POSITIVE_INFINITY
						: queryDataUpperInclusive ? queryDataUpper.doubleValue() : DoublePoint.nextDown(queryDataUpper.doubleValue()));
				final double lowerVal = ((queryDataLower == null) ? Double.NEGATIVE_INFINITY
						: queryDataLowerInclusive ? queryDataLower.doubleValue() : DoublePoint.nextUp(queryDataLower.doubleValue()));

				bqBuilder.add(DoublePoint.newRangeQuery(COLUMN_DOUBLE_FIELD_DATA + columnNamePostFixIfAny, lowerVal, upperVal), Occur.SHOULD);

				if (((queryDataUpper != null) && (queryDataUpper.floatValue() <= Float.MAX_VALUE) && (queryDataUpper.floatValue() >= Float.MIN_VALUE))
						|| ((queryDataLower != null) && (queryDataLower.doubleValue() <= Float.MAX_VALUE) && (queryDataLower.doubleValue() >= Float.MIN_VALUE)))
				{
					fitsInFloat = true;
				}
			}

			if (fitsInFloat || (queryDataLower instanceof Float) || (queryDataUpper instanceof Float))
			{
				final float upperVal = (queryDataUpper == null ? Float.POSITIVE_INFINITY
						: (queryDataUpper instanceof Float
								? queryDataUpperInclusive ? queryDataUpper.floatValue() : FloatPoint.nextDown(queryDataUpper.floatValue())
								: ((fitsInFloat && (queryDataUpper.doubleValue() > Float.MAX_VALUE) ? Float.MAX_VALUE
										: queryDataUpperInclusive ? queryDataUpper.floatValue() : FloatPoint.nextDown(queryDataUpper.floatValue())))));
				final float lowerVal = (queryDataLower == null ? Float.NEGATIVE_INFINITY
						: (queryDataLower instanceof Float
								? queryDataLowerInclusive ? queryDataLower.floatValue() : FloatPoint.nextUp(queryDataLower.floatValue())
								: ((fitsInFloat && (queryDataLower.doubleValue() < Float.MIN_VALUE) ? Float.MIN_VALUE
										: queryDataLowerInclusive ? queryDataLower.floatValue() : FloatPoint.nextUp(queryDataLower.floatValue())))));

				bqBuilder.add(FloatPoint.newRangeQuery(COLUMN_FLOAT_FIELD_DATA + columnNamePostFixIfAny, lowerVal, upperVal), Occur.SHOULD);
			}

			if ((queryDataLower instanceof Long) || (queryDataUpper instanceof Long))
			{
				final long upperVal = ((queryDataUpper == null) ? Long.MAX_VALUE
						: queryDataUpperInclusive ? queryDataUpper.longValue() : Math.addExact(queryDataUpper.longValue(), -1));
				final long lowerVal = ((queryDataLower == null) ? Long.MIN_VALUE
						: queryDataLowerInclusive ? queryDataLower.longValue() : Math.addExact(queryDataLower.longValue(), 1));

				bqBuilder.add(LongPoint.newRangeQuery(COLUMN_LONG_FIELD_DATA + columnNamePostFixIfAny, lowerVal, upperVal), Occur.SHOULD);

				if (((queryDataUpper != null) && (queryDataUpper.longValue() <= Integer.MAX_VALUE) && (queryDataUpper.longValue() >= Integer.MIN_VALUE))
						|| ((queryDataLower != null) && (queryDataLower.longValue() <= Integer.MAX_VALUE) && (queryDataLower.longValue() >= Integer.MIN_VALUE)))
				{
					fitsInInt = true;
				}
			}

			if (fitsInInt || (queryDataLower instanceof Integer) || (queryDataUpper instanceof Integer))
			{
				final int upperVal = ((queryDataUpper == null) ? Integer.MAX_VALUE
						: ((queryDataUpper instanceof Integer)
								? queryDataUpperInclusive ? queryDataUpper.intValue() : Math.addExact(queryDataUpper.intValue(), -1)
								: ((fitsInInt && queryDataUpper.longValue() > Integer.MAX_VALUE) ? Integer.MAX_VALUE
										: queryDataUpperInclusive ? queryDataUpper.intValue() : Math.addExact(queryDataUpper.intValue(), -1))));
				final int lowerVal = ((queryDataLower == null) ? Integer.MIN_VALUE
						: ((queryDataLower instanceof Integer)
								? queryDataLowerInclusive ? queryDataLower.intValue() : Math.addExact(queryDataLower.intValue(), 1)
								: ((fitsInInt && queryDataLower.longValue() < Integer.MIN_VALUE) ? Integer.MIN_VALUE
										: queryDataLowerInclusive ? queryDataLower.intValue() : Math.addExact(queryDataLower.intValue(), 1))));

				bqBuilder.add(IntPoint.newRangeQuery(COLUMN_INT_FIELD_DATA + columnNamePostFixIfAny, lowerVal, upperVal), Occur.SHOULD);
			}
			BooleanQuery bq = bqBuilder.build();
			if (bq.clauses().isEmpty())
			{
				throw new RuntimeException("Not a numeric data type - can't perform a range query");
			}
			else
			{
				final BooleanQuery.Builder must = new BooleanQuery.Builder();

				must.add(bq, Occur.MUST);
				return must.build();
			}
		}
		catch (final ClassCastException e)
		{
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
	private void handleType(Document doc, DynamicData dataCol, int colNumber)
	{
		// Not the greatest design for diskspace / performance... but if we want to be able to support searching across
		// all fields / all semantics - and also support searching per-field within a single semantic, we need to double index
		// all of the data.  Once with a standard field name, and once with a field name that includes the column number.
		// at search time, restricting to certain field matches is only allowed if they are also restricting to an assemblage,
		// so we can compute the correct field number list at search time.
		// Note, we optimize by only doing the double indexing in cases where the semantic has more than one column to begin with.
		// At query time, we construct the query appropriately to handle this optimization.
		// the cheaper option from a disk space perspective (maybe, depending on the data) would be to create a document per
		// column.  The queries would be trivial to write then, but we would be duplicating the component nid and assemblage nid
		// in each document, which is also expensive.  It also doesn't fit the model in OTF, of a document per component.
		// We also duplicate again, on string fields by indexing with the white space analyzer, in addition to the normal one, 
		// when indexing strings.
		// And we cannot just put the column numbers in their own field, and adjust the queries, because the queries match on the document.
		// And with multiple data fields per semantic / per document, we wouldn't be able to properly restrict the search on columns
		// without column specific indexing.
		if (dataCol == null)
		{
			// noop
		}
		else if (dataCol instanceof DynamicBoolean)
		{
			doc.add(new StringField(COLUMN_STRING_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicBoolean) dataCol).getDataBoolean() + "",
					Store.NO));

			if (colNumber >= 0)
			{
				doc.add(new StringField(COLUMN_STRING_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
						((DynamicBoolean) dataCol).getDataBoolean() + "", Store.NO));
			}

			incrementIndexedItemCount("Dynamic Boolean");
		}
		else if (dataCol instanceof DynamicByteArray)
		{
			LOG.warn("Semantic Indexer configured to index a field that isn''t indexable (byte array)");
		}
		else if (dataCol instanceof DynamicDouble)
		{
			doc.add(new DoublePoint(COLUMN_DOUBLE_FIELD_DATA, ((DynamicDouble) dataCol).getDataDouble()));

			if (colNumber >= 0)
			{
				doc.add(new DoublePoint(COLUMN_DOUBLE_FIELD_DATA + "_" + colNumber, ((DynamicDouble) dataCol).getDataDouble()));
			}

			incrementIndexedItemCount("Dynamic Double");
		}
		else if (dataCol instanceof DynamicFloat)
		{
			doc.add(new FloatPoint(COLUMN_FLOAT_FIELD_DATA, ((DynamicFloat) dataCol).getDataFloat()));

			if (colNumber >= 0)
			{
				doc.add(new FloatPoint(COLUMN_FLOAT_FIELD_DATA + "_" + colNumber, ((DynamicFloat) dataCol).getDataFloat()));
			}

			incrementIndexedItemCount("Dynamic Float");
		}
		else if (dataCol instanceof DynamicInteger)
		{
			doc.add(new IntPoint(COLUMN_INT_FIELD_DATA, ((DynamicInteger) dataCol).getDataInteger()));

			if (colNumber >= 0)
			{
				doc.add(new IntPoint(COLUMN_INT_FIELD_DATA + "_" + colNumber, ((DynamicInteger) dataCol).getDataInteger()));
			}

			incrementIndexedItemCount("Dynamic Integer");
		}
		else if (dataCol instanceof DynamicLong)
		{
			doc.add(new LongPoint(COLUMN_LONG_FIELD_DATA, ((DynamicLong) dataCol).getDataLong()));

			if (colNumber >= 0)
			{
				doc.add(new LongPoint(COLUMN_LONG_FIELD_DATA + "_" + colNumber, ((DynamicLong) dataCol).getDataLong()));
			}

			incrementIndexedItemCount("Dynamic Long");
		}
		else if (dataCol instanceof DynamicNid)
		{
			// No need for ranges on a nid, no need for tokenization (so string Field, instead of text field).
			doc.add(new StringField(COLUMN_STRING_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicNid) dataCol).getDataNid() + "", Store.NO));

			if (colNumber >= 0)
			{
				doc.add(new StringField(COLUMN_STRING_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
						((DynamicNid) dataCol).getDataNid() + "", Store.NO));
			}

			incrementIndexedItemCount("Dynamic Nid");
		}
		else if (dataCol instanceof DynamicPolymorphic)
		{
			LOG.error("This should have been impossible (polymorphic?)");
		}
		else if (dataCol instanceof DynamicString)
		{
			doc.add(new TextField(COLUMN_STRING_FIELD_DATA_TOKENIZED, ((DynamicString) dataCol).getDataString(), Store.NO));

			if (colNumber >= 0)
			{
				doc.add(new TextField(COLUMN_STRING_FIELD_DATA_TOKENIZED + "_" + colNumber, ((DynamicString) dataCol).getDataString(), Store.NO));
			}

			// yes, indexed 4 different times - twice with the standard analyzer, twice with the whitespace analyzer.
			doc.add(new TextField(COLUMN_STRING_FIELD_DATA_TOKENIZED + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicString) dataCol).getDataString(), Store.NO));

			if (colNumber >= 0)
			{
				doc.add(new TextField(COLUMN_STRING_FIELD_DATA_TOKENIZED + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
						((DynamicString) dataCol).getDataString(), Store.NO));
			}

			incrementIndexedItemCount("Dynamic String");
		}
		else if (dataCol instanceof DynamicUUID)
		{
			// Use the whitespace analyzer on UUIDs
			doc.add(new StringField(COLUMN_STRING_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicUUID) dataCol).getDataUUID().toString(),
					Store.NO));

			if (colNumber >= 0)
			{
				doc.add(new StringField(COLUMN_STRING_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
						((DynamicUUID) dataCol).getDataUUID().toString(), Store.NO));
			}

			incrementIndexedItemCount("Dynamic UUID");
		}
		else if (dataCol instanceof DynamicArray)
		{
			for (final DynamicData nestedData : ((DynamicArray<?>) dataCol).getDataArray())
			{
				handleType(doc, nestedData, colNumber);
			}
		}
		else
		{
			LOG.error("This should have been impossible (no match on col type) {}", dataCol);
		}
	}

	//~--- inner classes -------------------------------------------------------

	/**
	 * The Class QueryWrapperForColumnHandling.
	 */
	private abstract class QueryWrapperForColumnHandling
	{
		/**
		 * Builds the query.
		 *
		 * @param columnNamePostFixIfAny the column postfix identifier, if necessary
		 * @return the query
		 */
		abstract Query buildQuery(String columnNamePostFixIfAny);

		/**
		 * Builds the column handling query.
		 *
		 * @param assemblageConceptNid the semantic concept nid
		 * @param searchColumns the search columns
		 * @return the query
		 */
		protected Query buildColumnHandlingQuery(int[] assemblageConceptNid, int[] searchColumns)
		{
			Integer[] assemblageIndexedColumns = null;

			if ((searchColumns != null) && (searchColumns.length > 0))
			{
				// If they provide a search column - then they MUST provide one and only one assemblageConceptNid
				if ((assemblageConceptNid == null) || (assemblageConceptNid.length != 1))
				{
					throw new RuntimeException(
							"If a list of search columns is provided, then the assemblageConceptNid variable must contain 1 (and only 1) assemblage id");
				}
				else
				{
					assemblageIndexedColumns = getColumnsToIndex(assemblageConceptNid[0]);
				}
			}

			// If only 1 column was indexed from a semantic, we don't create field specific columns.
			if ((searchColumns == null) || (searchColumns.length == 0) || (assemblageIndexedColumns == null) || (assemblageIndexedColumns.length < 2))
			{
				return buildQuery("");
			}
			else  // If they passed a specific column to search AND the Dynamic type has more than 1 indexed column, then do a column specific search.
			{
				final BooleanQuery.Builder group = new BooleanQuery.Builder();

				for (final int i : searchColumns)
				{
					group.add(buildQuery("_" + i), Occur.SHOULD);
				}

				return group.build();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> query(String query, boolean prefixSearch, int[] assemblageConcept, Predicate<Integer> filter, AuthorModulePathRestriction amp,
			Integer pageNum, Integer sizeLimit, Long targetGeneration)
	{
		return queryData(new DynamicStringImpl(query), prefixSearch, assemblageConcept, null, filter, amp, pageNum, sizeLimit, targetGeneration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> queryNumericRange(Number queryDataLower, boolean queryDataLowerInclusive, Number queryDataUpper, boolean queryDataUpperInclusive,
			int[] assemblageConcepts, int[] searchColumns, Predicate<Integer> filter, AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit,
			Long targetGeneration)
	{

		Query q = new QueryWrapperForColumnHandling()
		{
			@Override
			Query buildQuery(String columnNamePostFixIfAny)
			{
				return buildNumericQuery(queryDataLower, queryDataLowerInclusive, queryDataUpper, queryDataUpperInclusive, columnNamePostFixIfAny);
			}
		}.buildColumnHandlingQuery(assemblageConcepts, searchColumns);

		return search(restrictToSemantic(q, assemblageConcepts), filter, amp, pageNum, sizeLimit, targetGeneration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> queryNidReference(int nid, int[] assemblageConcepts, int[] searchColumns, Predicate<Integer> filter,
			AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit, Long targetGeneration)
	{

		final Query q = new QueryWrapperForColumnHandling()
		{
			@Override
			Query buildQuery(String columnNamePostFixIfAny)
			{
				return new TermQuery(new Term(COLUMN_STRING_FIELD_DATA + columnNamePostFixIfAny + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, nid + ""));
			}
		}.buildColumnHandlingQuery(assemblageConcepts, searchColumns);

		return search(restrictToSemantic(q, assemblageConcepts), filter, amp, pageNum, sizeLimit, targetGeneration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> queryData(DynamicData queryData, boolean prefixSearch, int[] assemblageConcepts, int[] searchColumns, Predicate<Integer> filter,
			AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit, Long targetGeneration)
	{
		Query q = null;

		if (queryData instanceof DynamicString)
		{
			q = new QueryWrapperForColumnHandling()
			{
				@Override
				Query buildQuery(String columnNamePostFixIfAny)
				{
					// This is the only query type that needs tokenizing, etc.
					String queryString = ((DynamicString) queryData).getDataString();

					LOG.debug("Modified search string is: ''{}''", queryString);
					//We don't know if the string they are searching for was a string that was tokenized, or one that wasn't (like a UUID), so need to search both types.
					BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
					booleanQueryBuilder.add(
							new BooleanClause(buildTokenizedStringQuery(queryString, COLUMN_STRING_FIELD_DATA_TOKENIZED 
									+ columnNamePostFixIfAny, prefixSearch, false, false), Occur.SHOULD));
					booleanQueryBuilder.add(
							new BooleanClause(buildTokenizedStringQuery(queryString, COLUMN_STRING_FIELD_DATA 
									+ columnNamePostFixIfAny, prefixSearch, false, true), Occur.SHOULD));
					return booleanQueryBuilder.build();
				}
			}.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
		}
		else
		{
			if ((queryData instanceof DynamicBoolean) || (queryData instanceof DynamicNid) || (queryData instanceof DynamicUUID))
			{
				q = new QueryWrapperForColumnHandling()
				{
					@Override
					Query buildQuery(String columnNamePostFixIfAny)
					{
						return new TermQuery(new Term(COLUMN_STRING_FIELD_DATA + columnNamePostFixIfAny + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
								queryData.getDataObject().toString()));
					}
				}.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
				//By checking for DynamicNumeric, we inadvertently capture nid, but that is already handled in the if above.
			}
			else if ((queryData instanceof DynamicNumeric))
			{
				q = new QueryWrapperForColumnHandling()
				{
					@Override
					Query buildQuery(String columnNamePostFixIfAny)
					{
						Query temp = buildNumericQuery(((DynamicNumeric) queryData).getDataNumeric(), true, ((DynamicNumeric) queryData).getDataNumeric(), true,
								columnNamePostFixIfAny);

						if (((queryData instanceof DynamicLong) && ((DynamicLong) queryData).getDataLong() < 0)
								|| ((queryData instanceof DynamicInteger) && ((DynamicInteger) queryData).getDataInteger() < 0))
						{
							// Looks like a nid... wrap in an or clause that would do a match on the exact term if it was indexed as a nid, rather than a numeric
							final BooleanQuery.Builder wrapper = new BooleanQuery.Builder();

							wrapper.add(new TermQuery(new Term(COLUMN_STRING_FIELD_DATA + columnNamePostFixIfAny, queryData.getDataObject().toString())),
									Occur.SHOULD);
							wrapper.add(temp, Occur.SHOULD);
							temp = wrapper.build();
						}

						return temp;
					}
				}.buildColumnHandlingQuery(assemblageConcepts, searchColumns);
			}
			else if (queryData instanceof DynamicByteArray)
			{
				throw new RuntimeException("DynamicSemanticByteArray isn't indexed");
			}
			else if (queryData instanceof DynamicPolymorphic)
			{
				throw new RuntimeException("This should have been impossible (polymorphic?)");
			}
			else if (queryData instanceof DynamicArray)
			{
				throw new RuntimeException("DynamicSemanticArray isn't a searchable type");
			}
			else
			{
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
	public List<SearchResult> queryData(String queryString, boolean prefixSearch, int[] assemblageConcept, Predicate<Integer> filter,
			AuthorModulePathRestriction amp, Integer pageNum, Integer sizeLimit, Long targetGeneration)
	{
		return queryData(new DynamicStringImpl(queryString), prefixSearch, assemblageConcept, null, filter, amp, pageNum, sizeLimit, targetGeneration);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Task<Void> setColumnsToExclude(int assemblageConceptNid, Integer[] columnsToExclude)
	{
		Integer[] old;
		if (columnsToExclude == null || columnsToExclude.length == 0)
		{
			old = store.remove(assemblageConceptNid);
		}
		else
		{
			old = store.put(assemblageConceptNid, columnsToExclude);
		}

		columnsToIndexCache.remove(assemblageConceptNid);

		LOG.info("Set exclude columns for semantic {} to {}.  Old value was {}", assemblageConceptNid,
				columnsToExclude == null ? "none" : Arrays.toString(columnsToExclude), old == null ? "none" : Arrays.toString(old));

		fireIndexConfigurationChanged();
		return Get.startIndexTask(new Class[] { IndexSemanticQueryService.class });
	}

	@Override
	public Integer[] getColumnsToIndex(int assemblageConceptNid)
	{
		return getColumnsToIndex(null, assemblageConceptNid);
	}

	/**
	 * @param sc - optional - more efficient if provided
	 * @param assemblageConceptNid
	 * @return
	 */
	private Integer[] getColumnsToIndex(final SemanticChronology sc, final int assemblageConceptNid)
	{
		return columnsToIndexCache.computeIfAbsent(assemblageConceptNid, nidAgain -> {
			try
			{
				DynamicUsageDescription dud = sc == null ? DynamicUsageDescriptionImpl.mockOrRead(assemblageConceptNid) : DynamicUsageDescriptionImpl.mockOrRead(sc);
	
				HashSet<Integer> exclude = new HashSet<>();
				Integer[] exclusions = store.get(assemblageConceptNid);
				if (exclusions != null)
				{
					for (Integer i : exclusions)
					{
						exclude.add(i);
					}
				}
	
				ArrayList<Integer> colsToIndex = new ArrayList<>(dud.getColumnInfo().length);
				for (DynamicColumnInfo dci : dud.getColumnInfo())
				{
					if (!exclude.contains(Integer.valueOf(dci.getColumnOrder())) && !getUnsupportedDataTypes().contains(dci.getColumnDataType()))
					{
						colsToIndex.add(dci.getColumnOrder());
					}
				}
				return colsToIndex.toArray(new Integer[colsToIndex.size()]);
			}
			catch (Exception e)
			{
				LOG.error("Error determining columns to index, semantic {}, {}, will not be indexed.  Error was: {}", sc, assemblageConceptNid, e);
				return new Integer[] {};
			}
		});
	}

	@Override
	@PreDestroy
	protected void stopMe()
	{
		columnsToIndexCache.clear();
		super.stopMe();
	}
}
