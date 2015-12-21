/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.vha.isaac.ochre.query.provider.lucene.indexers;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import gov.vha.isaac.ochre.query.provider.lucene.LuceneIndexer;
import gov.vha.isaac.ochre.query.provider.lucene.PerFieldAnalyzer;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBooleanBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArrayBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDoubleBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloatBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeIntegerBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLongBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNidBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememePolymorphicBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequenceBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeStringBI;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUIDBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeString;

/**
 * {@link DynamicSememeIndexer} An indexer that can be used both to index the arbitrary attached column
 * data of dynamic sememes
 *
 * This class provides specialized query methods for handling very specific queries against DynamicSememe data.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@Service(name = "Dynamic Sememe indexer")
@RunLevel(value = 2)
public class DynamicSememeIndexer extends LuceneIndexer
{
	private static final Logger log = LogManager.getLogger();

	public static final String INDEX_NAME = "dynamicSememe";
	private static final String COLUMN_FIELD_DATA = "colData";

	@Inject private DynamicSememeIndexerConfiguration lric;

	private DynamicSememeIndexer() throws IOException
	{
		//For HK2
		super(INDEX_NAME);
	}

	@Override
	protected boolean indexChronicle(ObjectChronology<?> chronicle)
	{
		if (chronicle instanceof SememeChronology<?>)
		{
			SememeChronology<?> sememeChronology = (SememeChronology<?>) chronicle;
			if (sememeChronology.getSememeType() == SememeType.DYNAMIC)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected void addFields(ObjectChronology<?> chronicle, Document doc)
	{
		SememeChronology<?> sememeChronology = (SememeChronology<?>) chronicle;
		doc.add(new TextField(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, sememeChronology.getAssemblageSequence() + "", Field.Store.NO));

		for (Object o : sememeChronology.getVersionList())
		{
			if (o instanceof DynamicSememe)
			{
				DynamicSememe<?> dsv = (DynamicSememe<?>) o;

				Integer[] columns = lric.whatColumnsToIndex(dsv.getAssemblageSequence());
				if (columns != null)
				{
					int dataColCount = dsv.getData().length;
					for (int col : columns)
					{
						DynamicSememeDataBI dataCol = col >= dataColCount ? null : dsv.getData(col);

						//Not the greatest design for diskspace / performance... but if we want to be able to support searching across 
						//all fields / all sememes - and also support searching per-field within a single sememe, we need to double index 
						//all of the data.  Once with a standard field name, and once with a field name that includes the column number.
						//at search time, restricting to certain field matches is only allowed if they are also restricting to an assemblage,
						//so we can compute the correct field number list at search time.

						//the cheaper option from a disk space perspective (maybe, depending on the data) would be to create a document per 
						//column.  The queries would be trivial to write then, but we would be duplicating the component nid and assemblage nid
						//in each document, which is also expensive.  It also doesn't fit the model in OTF, of a document per component.

						//We also duplicate again, on string fields by indexing with the white space analyzer, in addition to the normal one.

						handleType(doc, dataCol, col, dsv);
					}
				}
			}
			else
			{
				log.error("Unexpected type handed to addFields in Dynamic Sememe Indexer: " + sememeChronology.toString());
			}
		}
	}

	private void handleType(Document doc, DynamicSememeDataBI dataCol, int colNumber, DynamicSememe<?> dsv)
	{
		if (dataCol == null)
		{
			//noop
		}
		else if (dataCol instanceof DynamicSememeBooleanBI)
		{
			doc.add(new StringField(COLUMN_FIELD_DATA, ((DynamicSememeBooleanBI) dataCol).getDataBoolean() + "", Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeBooleanBI) dataCol).getDataBoolean() + "", Store.NO));
		}
		else if (dataCol instanceof DynamicSememeByteArrayBI)
		{
			log.warn("Dynamic Sememe Indexer configured to index a field that isn''t indexable (byte array) in {}", dsv.toUserString());
		}
		else if (dataCol instanceof DynamicSememeDoubleBI)
		{
			doc.add(new DoubleField(COLUMN_FIELD_DATA, ((DynamicSememeDoubleBI) dataCol).getDataDouble(), Store.NO));
			doc.add(new DoubleField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeDoubleBI) dataCol).getDataDouble(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeFloatBI)
		{
			doc.add(new FloatField(COLUMN_FIELD_DATA, ((DynamicSememeFloatBI) dataCol).getDataFloat(), Store.NO));
			doc.add(new FloatField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeFloatBI) dataCol).getDataFloat(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeIntegerBI)
		{
			doc.add(new IntField(COLUMN_FIELD_DATA, ((DynamicSememeIntegerBI) dataCol).getDataInteger(), Store.NO));
			doc.add(new IntField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeIntegerBI) dataCol).getDataInteger(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeSequenceBI)
		{
			doc.add(new IntField(COLUMN_FIELD_DATA, ((DynamicSememeSequenceBI) dataCol).getDataSequence(), Store.NO));
			doc.add(new IntField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeSequenceBI) dataCol).getDataSequence(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeLongBI)
		{
			doc.add(new LongField(COLUMN_FIELD_DATA, ((DynamicSememeLongBI) dataCol).getDataLong(), Store.NO));
			doc.add(new LongField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeLongBI) dataCol).getDataLong(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeNidBI)
		{
			//No need for ranges on a nid
			doc.add(new StringField(COLUMN_FIELD_DATA, ((DynamicSememeNidBI) dataCol).getDataNid() + "", Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeNidBI) dataCol).getDataNid() + "", Store.NO));
		}
		else if (dataCol instanceof DynamicSememePolymorphicBI)
		{
			log.error("This should have been impossible (polymorphic?)");
		}
		else if (dataCol instanceof DynamicSememeStringBI)
		{
			doc.add(new TextField(COLUMN_FIELD_DATA, ((DynamicSememeStringBI) dataCol).getDataString(), Store.NO));
			doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeStringBI) dataCol).getDataString(), Store.NO));
			//yes, indexed 4 different times - twice with the standard analyzer, twice with the whitespace analyzer.
			doc.add(new TextField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeStringBI) dataCol).getDataString(), Store.NO));
			doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeStringBI) dataCol).getDataString(),
					Store.NO));
		}
		else if (dataCol instanceof DynamicSememeUUIDBI)
		{
			//Use the whitespace analyzer on UUIDs
			doc.add(new StringField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeUUIDBI) dataCol).getDataUUID().toString(), Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
					((DynamicSememeUUIDBI) dataCol).getDataUUID().toString(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeArrayBI<?>)
		{
			for (DynamicSememeDataBI nestedData : ((DynamicSememeArrayBI<?>) dataCol).getDataArray())
			{
				handleType(doc, nestedData, colNumber, dsv);
			}
		}
		else
		{
			log.error("This should have been impossible (no match on col type) {}", dataCol);
		}
	}

	/**
	 * @param queryDataLower
	 * @param queryDataLowerInclusive
	 * @param queryDataUpper
	 * @param queryDataUpperInclusive
	 * @param sememeConceptSequence (optional) limit the search to the specified assemblage
	 * @param searchColumns (optional) limit the search to the specified columns of attached data
	 * @param sizeLimit
	 * @param targetGeneration (optional) wait for an index to build, or null to not wait
	 * @return
	 * @throws java.io.IOException
	 * @throws org.apache.lucene.queryparser.classic.ParseException
	 */
	public final List<SearchResult> queryNumericRange(final DynamicSememeDataBI queryDataLower, final boolean queryDataLowerInclusive,
			final DynamicSememeDataBI queryDataUpper, final boolean queryDataUpperInclusive, Integer sememeConceptSequence, Integer[] searchColumns, int sizeLimit,
			Long targetGeneration) throws IOException, ParseException
	{
		BooleanQuery bq = new BooleanQuery();

		if (sememeConceptSequence != null)
		{
			bq.add(new TermQuery(new Term(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, sememeConceptSequence + "")), Occur.MUST);
		}

		bq.add(new QueryWrapperForColumnHandling()
		{
			@Override
			Query buildQuery(String columnName) throws ParseException
			{
				return buildNumericQuery(queryDataLower, queryDataLowerInclusive, queryDataUpper, queryDataUpperInclusive, columnName);
			}
		}.buildColumnHandlingQuery(searchColumns), Occur.MUST);

		return search(bq, sizeLimit, targetGeneration);
	}

	/**
	 * A convenience method.
	 * 
	 * Search DynamicSememeData columns, treating them as text - and handling the search in the same mechanism as if this were a
	 * call to the method {@link LuceneIndexer#query(String, boolean, Integer, int, long)}
	 * 
	 * Calls the method {@link #query(DynamicSememeDataBI, Integer, boolean, Integer[], int, long) with a null parameter for
	 * the searchColumns, and wraps the queryString into a DynamicSememeString.
	 * 
	 * @param queryString
	 * @param assemblageNid
	 * @param prefixSearch
	 * @param sizeLimit
	 * @param targetGeneration
	 * @return
	 * @throws java.io.IOException
	 * @throws org.apache.lucene.queryparser.classic.ParseException
	 */
	public final List<SearchResult> query(String queryString, Integer sememeConceptSequence, boolean prefixSearch, int sizeLimit, Long targetGeneration)
			throws IOException, ParseException
	{
		return query(new DynamicSememeString(queryString), sememeConceptSequence, prefixSearch, null, sizeLimit, targetGeneration);
	}

	/**
	 * 
	 * @param queryData - The query data object (string, int, etc)
	 * @param sememeConceptSequence (optional) limit the search to the specified assemblage
	 * @param prefixSearch see {@link LuceneIndexer#query(String, boolean, ComponentProperty, int, Long)} for a description.
	 * @param searchColumns (optional) limit the search to the specified columns of attached data
	 * @param sizeLimit
	 * @param targetGeneration (optional) wait for an index to build, or null to not wait
	 * @return
	 * @throws java.io.IOException
	 * @throws org.apache.lucene.queryparser.classic.ParseException
	 */
	public final List<SearchResult> query(final DynamicSememeDataBI queryData, Integer sememeConceptSequence, final boolean prefixSearch, Integer[] searchColumns,
			int sizeLimit, Long targetGeneration) throws IOException, ParseException
	{
		BooleanQuery bq = new BooleanQuery();
		if (sememeConceptSequence != null)
		{
			bq.add(new TermQuery(new Term(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, sememeConceptSequence + "")), Occur.MUST);
		}

		if (queryData instanceof DynamicSememeStringBI)
		{
			bq.add(new QueryWrapperForColumnHandling()
			{
				@Override
				Query buildQuery(String columnName) throws ParseException, IOException
				{
					//This is the only query type that needs tokenizing, etc.
					String queryString = ((DynamicSememeStringBI) queryData).getDataString();
					//'-' signs are operators to lucene... but we want to allow nid lookups.  So escape any leading hyphens
					//and any hyphens that are preceeded by spaces.  This way, we don't mess up UUID handling.
					//(lucene handles UUIDs ok, because the - sign is only treated special at the beginning, or when preceeded by a space)

					if (queryString.startsWith("-"))
					{
						queryString = "\\" + queryString;
					}
					queryString = queryString.replaceAll("\\s-", " \\\\-");
					log.debug("Modified search string is: ''{}''", queryString);
					return buildTokenizedStringQuery(queryString, columnName, prefixSearch);
				}
			}.buildColumnHandlingQuery(searchColumns), Occur.MUST);
		}
		else
		{
			if (queryData instanceof DynamicSememeBooleanBI || queryData instanceof DynamicSememeNidBI || queryData instanceof DynamicSememeUUIDBI)
			{
				bq.add(new QueryWrapperForColumnHandling()
				{
					@Override
					Query buildQuery(String columnName) throws ParseException
					{
						return new TermQuery(new Term(columnName, queryData.getDataObject().toString()));
					}
				}.buildColumnHandlingQuery(searchColumns), Occur.MUST);
			}
			else if (queryData instanceof DynamicSememeDoubleBI || queryData instanceof DynamicSememeFloatBI || queryData instanceof DynamicSememeIntegerBI
					|| queryData instanceof DynamicSememeLongBI)
			{
				bq.add(new QueryWrapperForColumnHandling()
				{
					@Override
					Query buildQuery(String columnName) throws ParseException
					{
						Query temp = buildNumericQuery(queryData, true, queryData, true, columnName);

						if ((queryData instanceof DynamicSememeLongBI && ((DynamicSememeLongBI) queryData).getDataLong() < 0)
								|| (queryData instanceof DynamicSememeIntegerBI && ((DynamicSememeIntegerBI) queryData).getDataInteger() < 0))
						{
							//Looks like a nid... wrap in an or clause that would do a match on the exact term if it was indexed as a nid, rather than a numeric
							BooleanQuery wrapper = new BooleanQuery();
							wrapper.add(new TermQuery(new Term(columnName, queryData.getDataObject().toString())), Occur.SHOULD);
							wrapper.add(temp, Occur.SHOULD);
							temp = wrapper;
						}
						return temp;
					}
				}.buildColumnHandlingQuery(searchColumns), Occur.MUST);
			}
			else if (queryData instanceof DynamicSememeByteArrayBI)

			{
				throw new ParseException("DynamicSememeByteArray isn't indexed");
			}
			else if (queryData instanceof DynamicSememePolymorphicBI)

			{
				throw new ParseException("This should have been impossible (polymorphic?)");
			}
			else

			{
				log.error("This should have been impossible (no match on col type)");
				throw new ParseException("unexpected error, see logs");
			}

		}
		return search(bq, sizeLimit, targetGeneration);
	}

	private Query buildNumericQuery(DynamicSememeDataBI queryDataLower, boolean queryDataLowerInclusive, DynamicSememeDataBI queryDataUpper,
			boolean queryDataUpperInclusive, String columnName) throws ParseException
	{
		//Convert both to the same type (if they differ) - go largest data type to smallest, so we don't lose precision
		//Also - if they pass in longs that would fit in an int, also generate an int query.
		//likewise, with Double - if they pass in a double, that would fit in a float, also generate a float query.
		try
		{
			BooleanQuery bq = new BooleanQuery();
			boolean fitsInFloat = false;
			boolean fitsInInt = false;
			if (queryDataLower instanceof DynamicSememeDoubleBI || queryDataUpper instanceof DynamicSememeDoubleBI)
			{
				Double upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeDoubleBI ? ((DynamicSememeDoubleBI) queryDataUpper).getDataDouble()
								: ((Number) queryDataUpper.getDataObject()).doubleValue()));
				Double lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeDoubleBI ? ((DynamicSememeDoubleBI) queryDataLower).getDataDouble()
								: ((Number) queryDataLower.getDataObject()).doubleValue()));
				bq.add(NumericRangeQuery.newDoubleRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);

				if ((upperVal != null && upperVal <= Float.MAX_VALUE && upperVal >= Float.MIN_VALUE)
						|| (lowerVal != null && lowerVal <= Float.MAX_VALUE && lowerVal >= Float.MIN_VALUE))
				{
					fitsInFloat = true;
				}
			}

			if (fitsInFloat || queryDataLower instanceof DynamicSememeFloatBI || queryDataUpper instanceof DynamicSememeFloatBI)
			{
				Float upperVal = (queryDataUpper == null ? null
						: (queryDataUpper == null ? null
								: (queryDataUpper instanceof DynamicSememeFloatBI ? ((DynamicSememeFloatBI) queryDataUpper).getDataFloat()
										: (fitsInFloat && ((Number) queryDataUpper.getDataObject()).doubleValue() > Float.MAX_VALUE ? Float.MAX_VALUE
												: ((Number) queryDataUpper.getDataObject()).floatValue()))));
				Float lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeFloatBI ? ((DynamicSememeFloatBI) queryDataLower).getDataFloat()
								: (fitsInFloat && ((Number) queryDataLower.getDataObject()).doubleValue() < Float.MIN_VALUE ? Float.MIN_VALUE
										: ((Number) queryDataLower.getDataObject()).floatValue())));
				bq.add(NumericRangeQuery.newFloatRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);
			}

			if (queryDataLower instanceof DynamicSememeLongBI || queryDataUpper instanceof DynamicSememeLongBI)
			{
				Long upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeLongBI ? ((DynamicSememeLongBI) queryDataUpper).getDataLong()
								: ((Number) queryDataUpper.getDataObject()).longValue()));
				Long lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeLongBI ? ((DynamicSememeLongBI) queryDataLower).getDataLong()
								: ((Number) queryDataLower.getDataObject()).longValue()));
				bq.add(NumericRangeQuery.newLongRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);
				if ((upperVal != null && upperVal <= Integer.MAX_VALUE && upperVal >= Integer.MIN_VALUE)
						|| (lowerVal != null && lowerVal <= Integer.MAX_VALUE && lowerVal >= Integer.MIN_VALUE))
				{
					fitsInInt = true;
				}
			}

			if (fitsInInt || queryDataLower instanceof DynamicSememeIntegerBI || queryDataUpper instanceof DynamicSememeIntegerBI)
			{
				Integer upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeIntegerBI ? ((DynamicSememeIntegerBI) queryDataUpper).getDataInteger()
								: (fitsInInt && ((Number) queryDataUpper.getDataObject()).longValue() > Integer.MAX_VALUE ? Integer.MAX_VALUE
										: ((Number) queryDataUpper.getDataObject()).intValue())));
				Integer lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeIntegerBI ? ((DynamicSememeIntegerBI) queryDataLower).getDataInteger()
								: (fitsInInt && ((Number) queryDataLower.getDataObject()).longValue() < Integer.MIN_VALUE ? Integer.MIN_VALUE
										: ((Number) queryDataLower.getDataObject()).intValue())));
				bq.add(NumericRangeQuery.newIntRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);
			}
			if (bq.getClauses().length == 0)
			{
				throw new ParseException("Not a numeric data type - can't perform a range query");
			}
			else
			{
				BooleanQuery must = new BooleanQuery();
				must.add(bq, Occur.MUST);
				return must;
			}
		}
		catch (ClassCastException e)
		{
			throw new ParseException("One of the values is not a numeric data type - can't perform a range query");
		}
	}

	private abstract class QueryWrapperForColumnHandling
	{
		abstract Query buildQuery(String columnName) throws ParseException, IOException;

		protected Query buildColumnHandlingQuery(Integer[] searchColumns) throws ParseException, IOException
		{
			if (searchColumns == null || searchColumns.length == 0)
			{
				return buildQuery(COLUMN_FIELD_DATA);
			}
			else
			{
				BooleanQuery group = new BooleanQuery();
				for (int i : searchColumns)
				{
					group.add(buildQuery(COLUMN_FIELD_DATA + "_" + i), Occur.SHOULD);
				}
				return group;
			}
		}
	}
}
