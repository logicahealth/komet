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
 */
package sh.isaac.provider.qa;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.concept.ConceptChronology;
import sh.isaac.api.component.concept.ConceptVersion;
import sh.isaac.api.component.semantic.SemanticChronology;
import sh.isaac.api.component.semantic.version.DescriptionVersion;
import sh.isaac.api.coordinate.ManifoldCoordinate;
import sh.isaac.api.qa.QAInfo;
import sh.isaac.api.qa.QAResults;
import sh.isaac.api.qa.Severity;
import sh.isaac.utility.Frills;

public class SimpleQA extends QATask
{
	ManifoldCoordinate coordinate;

	private final Pattern illegalChars = Pattern.compile(".*[\\t\\r\\n@$#\\\\].*");
	private final QAResults results = new QAResults();
	
	private HashMap<String, Integer> uniqueFQNs = new HashMap<>();

	public SimpleQA(ManifoldCoordinate coordinate)
	{
		this.coordinate = coordinate;
	}

	@Override
	protected QAResults call() throws Exception
	{
		//Just a one-off rule for now, for testing the overall API flow
		
		Get.conceptService().getConceptChronologyStream().parallel().forEach((ConceptChronology concept) -> {
			
			LatestVersion<ConceptVersion> cv = concept.getLatestVersion(coordinate);
			if (cv.isPresent() && cv.get().isActive())
			{
				final int termType = Frills.getTerminologyTypeForModule(cv.get().getModuleNid(), coordinate);
				if ((termType == MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getNid() || termType == MetaData.US_EXTENSION_MODULES____SOLOR.getNid() 
						|| termType == MetaData.SOLOR_MODULE____SOLOR.getNid()))
				{
					AtomicInteger fqnCount = new AtomicInteger();
					AtomicInteger rnCount = new AtomicInteger();
					Get.assemblageService().getSemanticChronologyStreamForComponent(concept.getNid()).forEach((SemanticChronology semantic) -> {
						if (semantic.getVersionType() == VersionType.DESCRIPTION)
						{
							LatestVersion<DescriptionVersion> dv = semantic.getLatestVersion(coordinate);
							if (dv.isPresent() && dv.get().isActive())
							{
								checkVersion(dv.get());
								
								if (dv.get().getDescriptionTypeConceptNid() == MetaData.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE____SOLOR.getNid())
								{
									fqnCount.getAndIncrement();
									Integer existing = uniqueFQNs.put(dv.get().getText() + dv.get().getLanguageConceptNid(), dv.get().getNid());
									if (existing != null)
									{
										addResult(new QAInfo(Severity.ERROR, existing, "Duplicate Fully Qualified Name", dv.get().getText()));
									}
								}
								else if (dv.get().getDescriptionTypeConceptNid() == MetaData.REGULAR_NAME_DESCRIPTION_TYPE____SOLOR.getNid())
								{
									rnCount.getAndIncrement();
								}
							}
						}
					});
					
					if (fqnCount.get() < 1)
					{
						addResult(new QAInfo(Severity.ERROR, concept.getNid(), "No active Fully Quallifed Name"));
					}
					if (rnCount.get() < 1)
					{
						addResult(new QAInfo(Severity.ERROR, concept.getNid(), "No active Regular Name"));
					}
				}
			}
		});
		return results;
	}
	
	@Override
	public QAResults checkVersion(Version v)
	{
		if (v instanceof DescriptionVersion)
		{
			//TODO I need a list of parent modules that snomed QA rules should apply to
			final String descriptionText = ((DescriptionVersion)v).getText();
			if (illegalChars.matcher(descriptionText).matches())
			{
				addResult(new QAInfo(Severity.ERROR, v.getNid(),
						"An active term should not contain tabs, newlines, or characters @, $, #, \\\\.", descriptionText));
			}
			if (descriptionText.contains("  "))
			{
				addResult(new QAInfo(Severity.WARNING, v.getNid(), "An active term should not contain double spaces", descriptionText));
			}
		}
		return results;
	}
	
	private void addResult(QAInfo result)
	{
		synchronized (results)
		{
			results.addResult(result);
		}
	}
}
