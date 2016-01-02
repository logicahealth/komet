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

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.*;
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
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;

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
						DynamicSememeData dataCol = col >= dataColCount ? null : dsv.getData(col);

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

	private void handleType(Document doc, DynamicSememeData dataCol, int colNumber, DynamicSememe<?> dsv)
	{
		if (dataCol == null)
		{
			//noop
		}
		else if (dataCol instanceof DynamicSememeBoolean)
		{
			doc.add(new StringField(COLUMN_FIELD_DATA, ((DynamicSememeBoolean) dataCol).getDataBoolean() + "", Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeBoolean) dataCol).getDataBoolean() + "", Store.NO));
		}
		else if (dataCol instanceof DynamicSememeByteArray)
		{
			log.warn("Dynamic Sememe Indexer configured to index a field that isn''t indexable (byte array) in {}", dsv.toUserString());
		}
		else if (dataCol instanceof DynamicSememeDouble)
		{
			doc.add(new DoubleField(COLUMN_FIELD_DATA, ((DynamicSememeDouble) dataCol).getDataDouble(), Store.NO));
			doc.add(new DoubleField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeDouble) dataCol).getDataDouble(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeFloat)
		{
			doc.add(new FloatField(COLUMN_FIELD_DATA, ((DynamicSememeFloat) dataCol).getDataFloat(), Store.NO));
			doc.add(new FloatField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeFloat) dataCol).getDataFloat(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeInteger)
		{
			doc.add(new IntField(COLUMN_FIELD_DATA, ((DynamicSememeInteger) dataCol).getDataInteger(), Store.NO));
			doc.add(new IntField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeInteger) dataCol).getDataInteger(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeSequence)
		{
			doc.add(new IntField(COLUMN_FIELD_DATA, ((DynamicSememeSequence) dataCol).getDataSequence(), Store.NO));
			doc.add(new IntField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeSequence) dataCol).getDataSequence(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeLong)
		{
			doc.add(new LongField(COLUMN_FIELD_DATA, ((DynamicSememeLong) dataCol).getDataLong(), Store.NO));
			doc.add(new LongField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeLong) dataCol).getDataLong(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeNid)
		{
			//No need for ranges on a nid
			doc.add(new StringField(COLUMN_FIELD_DATA, ((DynamicSememeNid) dataCol).getDataNid() + "", Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeNid) dataCol).getDataNid() + "", Store.NO));
		}
		else if (dataCol instanceof DynamicSememePolymorphic)
		{
			log.error("This should have been impossible (polymorphic?)");
		}
		else if (dataCol instanceof DynamicSememeString)
		{
			doc.add(new TextField(COLUMN_FIELD_DATA, ((DynamicSememeString) dataCol).getDataString(), Store.NO));
			doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber, ((DynamicSememeString) dataCol).getDataString(), Store.NO));
			//yes, indexed 4 different times - twice with the standard analyzer, twice with the whitespace analyzer.
			doc.add(new TextField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeString) dataCol).getDataString(), Store.NO));
			doc.add(new TextField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeString) dataCol).getDataString(),
					Store.NO));
		}
		else if (dataCol instanceof DynamicSememeUUID)
		{
			//Use the whitespace analyzer on UUIDs
			doc.add(new StringField(COLUMN_FIELD_DATA + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ((DynamicSememeUUID) dataCol).getDataUUID().toString(), Store.NO));
			doc.add(new StringField(COLUMN_FIELD_DATA + "_" + colNumber + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER,
					((DynamicSememeUUID) dataCol).getDataUUID().toString(), Store.NO));
		}
		else if (dataCol instanceof DynamicSememeArray<?>)
		{
			for (DynamicSememeData nestedData : ((DynamicSememeArray<?>) dataCol).getDataArray())
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
	public final List<SearchResult> queryNumericRange(final DynamicSememeData queryDataLower, final boolean queryDataLowerInclusive,
			final DynamicSememeData queryDataUpper, final boolean queryDataUpperInclusive, Integer sememeConceptSequence, Integer[] searchColumns, int sizeLimit,
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
		return query(new DynamicSememeStringImpl(queryString), sememeConceptSequence, prefixSearch, null, sizeLimit, targetGeneration);
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
	public final List<SearchResult> query(final DynamicSememeData queryData, Integer sememeConceptSequence, final boolean prefixSearch, Integer[] searchColumns,
			int sizeLimit, Long targetGeneration) throws IOException, ParseException
	{
		BooleanQuery bq = new BooleanQuery();
		if (sememeConceptSequence != null)
		{
			bq.add(new TermQuery(new Term(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, sememeConceptSequence + "")), Occur.MUST);
		}

		if (queryData instanceof DynamicSememeString)
		{
			bq.add(new QueryWrapperForColumnHandling()
			{
				@Override
				Query buildQuery(String columnName) throws ParseException, IOException
				{
					//This is the only query type that needs tokenizing, etc.
					String queryString = ((DynamicSememeString) queryData).getDataString();
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
			if (queryData instanceof DynamicSememeBoolean || queryData instanceof DynamicSememeNid || queryData instanceof DynamicSememeUUID)
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
			else if (queryData instanceof DynamicSememeDouble || queryData instanceof DynamicSememeFloat || queryData instanceof DynamicSememeInteger
					|| queryData instanceof DynamicSememeLong)
			{
				bq.add(new QueryWrapperForColumnHandling()
				{
					@Override
					Query buildQuery(String columnName) throws ParseException
					{
						Query temp = buildNumericQuery(queryData, true, queryData, true, columnName);

						if ((queryData instanceof DynamicSememeLong && ((DynamicSememeLong) queryData).getDataLong() < 0)
								|| (queryData instanceof DynamicSememeInteger && ((DynamicSememeInteger) queryData).getDataInteger() < 0))
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
			else if (queryData instanceof DynamicSememeByteArray)

			{
				throw new ParseException("DynamicSememeByteArray isn't indexed");
			}
			else if (queryData instanceof DynamicSememePolymorphic)

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

	private Query buildNumericQuery(DynamicSememeData queryDataLower, boolean queryDataLowerInclusive, DynamicSememeData queryDataUpper,
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
			if (queryDataLower instanceof DynamicSememeDouble || queryDataUpper instanceof DynamicSememeDouble)
			{
				Double upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeDouble ? ((DynamicSememeDouble) queryDataUpper).getDataDouble()
								: ((Number) queryDataUpper.getDataObject()).doubleValue()));
				Double lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeDouble ? ((DynamicSememeDouble) queryDataLower).getDataDouble()
								: ((Number) queryDataLower.getDataObject()).doubleValue()));
				bq.add(NumericRangeQuery.newDoubleRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);

				if ((upperVal != null && upperVal <= Float.MAX_VALUE && upperVal >= Float.MIN_VALUE)
						|| (lowerVal != null && lowerVal <= Float.MAX_VALUE && lowerVal >= Float.MIN_VALUE))
				{
					fitsInFloat = true;
				}
			}

			if (fitsInFloat || queryDataLower instanceof DynamicSememeFloat || queryDataUpper instanceof DynamicSememeFloat)
			{
				Float upperVal = (queryDataUpper == null ? null
						: (queryDataUpper == null ? null
								: (queryDataUpper instanceof DynamicSememeFloat ? ((DynamicSememeFloat) queryDataUpper).getDataFloat()
										: (fitsInFloat && ((Number) queryDataUpper.getDataObject()).doubleValue() > Float.MAX_VALUE ? Float.MAX_VALUE
												: ((Number) queryDataUpper.getDataObject()).floatValue()))));
				Float lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeFloat ? ((DynamicSememeFloat) queryDataLower).getDataFloat()
								: (fitsInFloat && ((Number) queryDataLower.getDataObject()).doubleValue() < Float.MIN_VALUE ? Float.MIN_VALUE
										: ((Number) queryDataLower.getDataObject()).floatValue())));
				bq.add(NumericRangeQuery.newFloatRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);
			}

			if (queryDataLower instanceof DynamicSememeLong || queryDataUpper instanceof DynamicSememeLong)
			{
				Long upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeLong ? ((DynamicSememeLong) queryDataUpper).getDataLong()
								: ((Number) queryDataUpper.getDataObject()).longValue()));
				Long lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeLong ? ((DynamicSememeLong) queryDataLower).getDataLong()
								: ((Number) queryDataLower.getDataObject()).longValue()));
				bq.add(NumericRangeQuery.newLongRange(columnName, lowerVal, upperVal, queryDataLowerInclusive, queryDataUpperInclusive), Occur.SHOULD);
				if ((upperVal != null && upperVal <= Integer.MAX_VALUE && upperVal >= Integer.MIN_VALUE)
						|| (lowerVal != null && lowerVal <= Integer.MAX_VALUE && lowerVal >= Integer.MIN_VALUE))
				{
					fitsInInt = true;
				}
			}

			if (fitsInInt || queryDataLower instanceof DynamicSememeInteger || queryDataUpper instanceof DynamicSememeInteger)
			{
				Integer upperVal = (queryDataUpper == null ? null
						: (queryDataUpper instanceof DynamicSememeInteger ? ((DynamicSememeInteger) queryDataUpper).getDataInteger()
								: (fitsInInt && ((Number) queryDataUpper.getDataObject()).longValue() > Integer.MAX_VALUE ? Integer.MAX_VALUE
										: ((Number) queryDataUpper.getDataObject()).intValue())));
				Integer lowerVal = (queryDataLower == null ? null
						: (queryDataLower instanceof DynamicSememeInteger ? ((DynamicSememeInteger) queryDataLower).getDataInteger()
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
