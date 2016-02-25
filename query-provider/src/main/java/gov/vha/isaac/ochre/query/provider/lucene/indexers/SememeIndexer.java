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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.TermQuery;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.query.provider.lucene.LuceneIndexer;
import gov.vha.isaac.ochre.query.provider.lucene.PerFieldAnalyzer;

/**
 * This class provides indexing for all String, Nid Long and Logic Graph sememe types.
 *
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service(name = "sememe indexer")
@RunLevel(value = 2)
public class SememeIndexer extends LuceneIndexer {

    private static final Logger log = LogManager.getLogger();
    private SememeIndexer() throws IOException {
        //For HK2
        super("sememes");
    }

    @Override
    protected boolean indexChronicle(ObjectChronology<?> chronicle) {
        if (chronicle instanceof SememeChronology<?>) {
            SememeChronology<?> sememeChronology = (SememeChronology<?>) chronicle;
            if (sememeChronology.getSememeType() == SememeType.STRING || sememeChronology.getSememeType() == SememeType.LONG
                    || sememeChronology.getSememeType() == SememeType.COMPONENT_NID || sememeChronology.getSememeType() == SememeType.LOGIC_GRAPH) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls {@link #query(long, Integer, long) with null for the sememeConceptSequence and Long.MIN_VALUE for target generation.
     */
    public List<SearchResult> query(Long query, int sizeLimit) {
        return query(query, null, sizeLimit, Long.MIN_VALUE);
    }

    /**
     * Calls {@link #query(long, long, Integer, int, long) with the same value for min and max
     */
    public List<SearchResult> query(long query, Integer[] sememeConceptSequence, int sizeLimit, Long targetGeneration) {
        return query(query, query, sememeConceptSequence, sizeLimit, targetGeneration);
    }
    
    /**
     * Search for matches to the specified id.  Note that in the current implementation, you will only find matches to sememes 
     * of type {@link SememeType#COMPONENT_NID} or {@link SememeType#LOGIC_GRAPH}.
     * 
     *  If searching a component nid sememe, you must pass a nid (no sequences)
     *  If searching a logic graph sememe, you must pass a concept sequence (no nids).
     * 
     * @param id the id reference to search for
     * @param semeneConceptSequence optional - The concept seqeuence of the sememe that you wish to search within. If null,
     * searches all indexed content. This would be set to the concept sequence like  {@link MetaData#EL_PLUS_PLUS_STATED_FORM_ASSEMBLAGE}
     * @param sizeLimit The maximum size of the result list.
     * @param targetGeneration target generation that must be included in the search or Long.MIN_VALUE if there is no need 
     * to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait  until any in-progress 
     * indexing operations are completed - and then use the latest index.
     * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that 
     * match relative to other matches.  Note that scores are pointless for exact id matches - they will all be the same.
     */
    public List<SearchResult> query(int id, Integer[] sememeConceptSequence, int sizeLimit, Long targetGeneration) {
        return search(restrictToSememe(new TermQuery(new Term(FIELD_INDEXED_ID_VALUE, id + "")), sememeConceptSequence),
                sizeLimit, targetGeneration);
    }

    /**
     * Search for matches within the specified range of long values.  Pass the same value to min and max to search for a 
     * specific value.  Note that in the current implementation, you will only find matches to sememes of type {@link SememeType#LONG}
     * 
     * @param min inclusive minimum
     * @param max inclusive maximum
     * @param semeneConceptSequence optional - The concept seqeuence of the sememe that you wish to search within. 
     * If null, searches all indexed content. This would be set to a concept sequence like 
     * {@link MetaData#DESCRIPTION_ASSEMBLAGE} or the concept sequence {@link MetaData#SNOMED_INTEGER_ID} for example.
     * @param sizeLimit The maximum size of the result list.
     * @param targetGeneration target generation that must be included in the search or Long.MIN_VALUE if there is no 
     * need to wait for a target generation. Long.MAX_VALUE can be passed in to force this query to wait until any in
     * -progress indexing operations are completed - and then use the latest index.
     * @return a List of {@code SearchResult} that contains the nid of the component that matched, and the score of that 
     * match relative to other matches.
     */
    public List<SearchResult> query(long min, long max, Integer[] sememeConceptSequence, int sizeLimit, Long targetGeneration) {
        return search(restrictToSememe(NumericRangeQuery.newLongRange(FIELD_INDEXED_LONG_VALUE, min, max, true, true), sememeConceptSequence), 
                sizeLimit, targetGeneration);
    }

    @Override
    protected void addFields(ObjectChronology<?> chronicle, Document doc) {
        SememeChronology<?> sememeChronology = (SememeChronology<?>) chronicle;
        doc.add(new TextField(FIELD_SEMEME_ASSEMBLAGE_SEQUENCE, sememeChronology.getAssemblageSequence() + "", Field.Store.NO));

        for (Object sv : sememeChronology.getVersionList()) {
            if (sv instanceof StringSememe) {
                StringSememe<?> ssv = (StringSememe<?>) sv;
                //index twice per field - once with the standard analyzer, once with the whitespace analyzer.
                doc.add(new TextField(FIELD_INDEXED_STRING_VALUE, ssv.getString(), Field.Store.NO));
                doc.add(new TextField(FIELD_INDEXED_STRING_VALUE + PerFieldAnalyzer.WHITE_SPACE_FIELD_MARKER, ssv.getString(), Field.Store.NO));
            }
            else if (sv instanceof LongSememe) {
                LongSememe<?> lsv = (LongSememe<?>) sv;
                doc.add(new LongField(FIELD_INDEXED_LONG_VALUE, lsv.getLongValue(), FIELD_TYPE_LONG_INDEXED_NOT_STORED));
            }
            else if (sv instanceof ComponentNidSememe) {
                ComponentNidSememe<?> csv = (ComponentNidSememe<?>) sv;
                //No need to range query an id, cheaper to index as a string
                doc.add(new TextField(FIELD_INDEXED_ID_VALUE, csv.getComponentNid() + "", Field.Store.NO));
            }
            else if (sv instanceof LogicGraphSememe) {
                LogicGraphSememe<?> lgsv = (LogicGraphSememe<?>) sv;
                ConceptSequenceSet css = new ConceptSequenceSet();
                lgsv.getLogicalExpression().processDepthFirst((LogicNode logicNode, TreeNodeVisitData data) ->
                {
                    logicNode.addConceptsReferencedByNode(css);
                });
                css.stream().forEach(sequence -> 
                {
                    //No need to range query an id, cheaper to index as a string
                    doc.add(new TextField(FIELD_INDEXED_ID_VALUE, sequence + "", Field.Store.NO));
                });
                
            }
            else {
                log.error("Programmer Error - unsupported type passed in to add fields! :" + sv.getClass() );
            }
        }
    }
}
