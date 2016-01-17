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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.query.provider.lucene.indexers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeSnapshotService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.MutableDynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.api.index.IndexStatusListenerBI;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.constants.IsaacMetadataConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeIntegerImpl;

/**
 * {@link DynamicSememeIndexerConfiguration} Holds a cache of the configuration for the dynamic sememe indexer (which is read from the DB, and may
 * be changed at any point
 * the user wishes). Keeps track of which assemblage types need to be indexing, and what attributes should be indexed on them.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Service
@Singleton
public class DynamicSememeIndexerConfiguration
{
	private static final Logger log = LogManager.getLogger();

	//store assemblage sequences that should be indexed - and then - for COLUMN_DATA keys, keep the 0 indexed column order numbers that need to be indexed.
	private HashMap<Integer, Integer[]> whatToIndexSequenceToCol_ = new HashMap<>();

	private volatile AtomicInteger readNeeded_ = new AtomicInteger(1);  //0 means no readNeeded, anything greater than 0 means it does need a re-read

	protected boolean needsIndexing(int assemblageConceptSequence)
	{
		initCheck();
		return whatToIndexSequenceToCol_.containsKey(assemblageConceptSequence);
	}

	protected Integer[] whatColumnsToIndex(int assemblageConceptSequence)
	{
		initCheck();
		return whatToIndexSequenceToCol_.get(assemblageConceptSequence);
	}

	private void initCheck()
	{
		if (readNeeded_.get() > 0)
		{
			//During bulk index, prevent all threads from doing this at the same time...
			synchronized (readNeeded_)
			{
				if (readNeeded_.get() > 0)
				{
					log.debug("Reading Dynamic Sememe Index Configuration");
					try
					{
						HashMap<Integer, Integer[]> updatedWhatToIndex = new HashMap<>();
						Stream<SememeChronology<? extends SememeVersion<?>>> sememeCs = 
								Get.sememeService().getSememesFromAssemblage(IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getSequence());
						
						sememeCs.forEach(sememeC ->
						{
							if (sememeC.getSememeType() == SememeType.DYNAMIC)
							{
								@SuppressWarnings({ "unchecked", "rawtypes" })
								Optional<LatestVersion<DynamicSememe>> dsv = ((SememeChronology)sememeC).getLatestVersion(DynamicSememe.class, StampCoordinates.getDevelopmentLatest());
								
								if (dsv.isPresent() && dsv.get().value().getState() == State.ACTIVE)
								{
									int assemblageToIndex = Get.identifierService().getConceptSequence(dsv.get().value().getReferencedComponentNid());
									Integer[] finalCols = new Integer[] {};
									DynamicSememeData[] data = dsv.get().value().getData();
									if (data != null && data.length > 0)
									{
										@SuppressWarnings("unchecked")
										DynamicSememeInteger[] colsToIndex = ((DynamicSememeArray<DynamicSememeInteger>) data[0]).getDataArray();
										
										finalCols = new Integer[colsToIndex.length];
										for (int i = 0; i < colsToIndex.length; i++)
										{
											finalCols[i] = colsToIndex[i].getDataInteger();
										}
									}
									else
									{
										log.warn("The assemblage concept {} was entered for indexing without specifying what columns to index.  Nothing to do!", assemblageToIndex);
									}
									updatedWhatToIndex.put(assemblageToIndex, finalCols);
								}
							}
							
						});
		
						whatToIndexSequenceToCol_ = updatedWhatToIndex;
						readNeeded_.decrementAndGet();
					}
					catch (Exception e)
					{
						log.error("Unexpected error reading Dynamic Sememe Index Configuration - generated index will be incomplete!", e);
					}
				}
			}
		}
	}

	/**
	 * for the given assemblage sequence, which columns should be indexed - note - columnsToIndex must be provided 
	 * it doesn't make any sense to index sememes any longer in ochre without indexing column content
	 * 
	 * @param skipReindex - if true - does not do a full DB reindex (useful if you are enabling an index on a new sememe that has never been used)
	 * otherwise - leave false - so that a full reindex occurs (on this thread) and the index becomes valid.
	 * 
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static void configureColumnsToIndex(int assemblageNidOrSequence, Integer[] columnsToIndex, boolean skipReindex) throws RuntimeException
	{
		LookupService.get().getService(DynamicSememeIndexerConfiguration.class).readNeeded_.incrementAndGet();
		List<IndexStatusListenerBI> islList = LookupService.get().getAllServices(IndexStatusListenerBI.class);
		for (IndexStatusListenerBI isl : islList)
		{
			isl.indexConfigurationChanged(LookupService.get().getService(DynamicSememeIndexer.class));
		}

		ConceptChronology<? extends ConceptVersion<?>> referencedAssemblageConceptC = Get.conceptService().getConcept(assemblageNidOrSequence);
		
		log.info("Configuring index for dynamic sememe assemblage '" + referencedAssemblageConceptC.toUserString() + "' on columns " + Arrays.deepToString(columnsToIndex));

		DynamicSememeData[] data = null;
		if (columnsToIndex != null)
		{
			DynamicSememeIntegerImpl[] cols = new DynamicSememeIntegerImpl[columnsToIndex.length];
			for (int i = 0; i < columnsToIndex.length; i++)
			{
				cols[i] = new DynamicSememeIntegerImpl(columnsToIndex[i]);
			}

			if (cols.length > 0)
			{
				data = new DynamicSememeData[] {new DynamicSememeArrayImpl<DynamicSememeIntegerImpl>(cols)};
			}
		}
		else if ((columnsToIndex == null || columnsToIndex.length == 0))
		{
			throw new RuntimeException("It doesn't make sense to index a dynamic sememe without indexing any column data");
		}
		
		SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> sb = 
				Get.sememeBuilderService().getDynamicSememeBuilder(assemblageNidOrSequence,
						IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getSequence(), data);
		
		sb.build(EditCoordinates.getDefaultUserMetadata(), ChangeCheckerMode.ACTIVE);
		Get.commitService().commit("Index Config Change");
		
		if (!skipReindex)
		{
			Get.startIndexTask(new Class[] {DynamicSememeIndexer.class});
		}
	}
	
	/**
	 * Read the indexing configuration for the specified dynamic sememe.
	 * 
	 * Returns null, if the assemblage is not indexed at all.  Returns an empty array, if the assemblage is indexed (but no columns are indexed)
	 * Returns an integer array of the column positions of the refex that are indexed, if any.
	 * 
	 */
	public static Integer[] readIndexInfo(int assemblageSequence) throws RuntimeException
	{
		return LookupService.get().getService(DynamicSememeIndexerConfiguration.class).whatColumnsToIndex(assemblageSequence);
	}
	
	private static DynamicSememe<? extends DynamicSememe<?>> findCurrentIndexConfigRefex(int assemblageNidOrSequence) throws RuntimeException
	{
		@SuppressWarnings("rawtypes")
		SememeSnapshotService<DynamicSememe> sss = Get.sememeService().getSnapshot(DynamicSememe.class, StampCoordinates.getDevelopmentLatest());
		@SuppressWarnings("rawtypes")
		Stream<LatestVersion<DynamicSememe>> sememes = sss.getLatestSememeVersionsForComponentFromAssemblage(assemblageNidOrSequence, 
				IsaacMetadataConstants.DYNAMIC_SEMEME_INDEX_CONFIGURATION.getSequence());

		@SuppressWarnings("rawtypes")
		Optional<LatestVersion<DynamicSememe>> ds = sememes.findAny();

		if (ds.isPresent())
		{
			return ds.get().value();
		}
		return null;
	}
	
	/**
	 * Disable all indexing of the specified refex.  To change the index config, use the {@link #configureColumnsToIndex(int, Integer[]) method.
	 * 
	 * Note that this causes a full DB reindex, on this thread.
	 * 
	 * @throws IOException 	 
	 * @throws ContradictionException 
	 * @throws InvalidCAB 
	 * @throws ExecutionException 
	 * @throws InterruptedException */
	@SuppressWarnings("unchecked")
	public static void disableIndex(int assemblageConceptSequence) throws RuntimeException
	{
		log.info("Disabling index for dynamic sememe assemblage concept '" + assemblageConceptSequence + "'");
		
		DynamicSememe<?> rdv = findCurrentIndexConfigRefex(assemblageConceptSequence);
		
		if (rdv != null && rdv.getState() == State.ACTIVE)
		{
			LookupService.get().getService(DynamicSememeIndexerConfiguration.class).readNeeded_.incrementAndGet();
			List<IndexStatusListenerBI> islList = LookupService.get().getAllServices(IndexStatusListenerBI.class);
			for (IndexStatusListenerBI isl : islList)
			{
				isl.indexConfigurationChanged(LookupService.get().getService(DynamicSememeIndexer.class));
			}
			
			((SememeChronology)rdv.getChronology()).createMutableVersion(MutableDynamicSememe.class, State.INACTIVE, EditCoordinates.getDefaultUserMetadata());
			Get.commitService().addUncommitted(rdv.getChronology());
			Get.commitService().commit("Index Config Change");
			log.info("Index disabled for dynamic sememe assemblage concept '" + assemblageConceptSequence + "'");

			Get.startIndexTask(new Class[] {DynamicSememeIndexer.class});
			return;
		}
		else
		{
			log.info("No index configuration was found to disable for dynamic sememe assemblage concept '" + assemblageConceptSequence + "'");
		}
	}
	
	public static boolean isColumnTypeIndexable(DynamicSememeDataType dataType)
	{
		if (dataType == DynamicSememeDataType.BYTEARRAY)
		{
			return false;
		}
		return true;
	}
	
	public static boolean isAssemblageIndexed(int assemblageConceptSequence)
	{
		return LookupService.get().getService(DynamicSememeIndexerConfiguration.class).needsIndexing(assemblageConceptSequence);
	}
}
