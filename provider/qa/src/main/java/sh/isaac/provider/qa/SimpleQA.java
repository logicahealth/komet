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

import java.util.regex.Pattern;
import sh.isaac.MetaData;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.LatestVersion;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
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

	Pattern illegalChars = Pattern.compile(".*[\\t\\r\\n@$#\\\\].*");
	QAResults results = new QAResults();

	public SimpleQA(ManifoldCoordinate coordinate)
	{
		this.coordinate = coordinate;
	}

	@Override
	protected QAResults call() throws Exception
	{
		//Just a one-off rule for now, for testing the overall API flow
		Get.assemblageService().getSemanticChronologyStream().parallel().forEach((SemanticChronology semantic) -> {
			if (semantic.getVersionType() == VersionType.DESCRIPTION)
			{
				LatestVersion<DescriptionVersion> dv = semantic.getLatestVersion(coordinate);
				if (dv.isPresent() && dv.get().isActive())
				{
					checkVersion(dv.get());
				}
			}
		});
		return results;
	}
	
	@Override
	public QAResults checkVersion(Version v)
	{
		if (v instanceof DescriptionVersion
				&& Frills.getTerminologyTypeForModule(v.getModuleNid(), coordinate) == MetaData.SNOMED_CT_CORE_MODULES____SOLOR.getNid()
				&& illegalChars.matcher(((DescriptionVersion)v).getText()).matches())
		{
			synchronized (results)
			{
				results.addResult(new QAInfo(Severity.ERROR, v.getNid(),
						"An active term should not contain tabs, newlines, or characters @, $, #, \\\\.", ((DescriptionVersion)v).getText()));
			}
		}
		return results;
	}
}
