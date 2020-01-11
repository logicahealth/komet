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

package sh.isaac.mojo;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.api.AssemblageService;
import sh.isaac.api.Get;
import sh.isaac.api.IdentifiedObjectService;
import sh.isaac.api.IdentifierService;

/**
 * @author darmbrust
 *         Simply utility mojo to warn about semantics that reference a missing component.
 */
@Mojo(name = "scan-refs", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ScanForMissingConcepts extends AbstractMojo
{
	/**
	 * Execute.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 */
	@Override
	public void execute() throws MojoExecutionException
	{
		Headless.setHeadless();

		IdentifierService is = Get.identifierService();
		IdentifiedObjectService ios = Get.identifiedObjectService();
		AssemblageService as = Get.assemblageService();

		{
			ArrayList<UUID> missingComponents = new ArrayList<>();
			AtomicInteger semanticScanCount = new AtomicInteger();
			Get.assemblageService().getSemanticChronologyStream().parallel().forEach(sc -> {
				semanticScanCount.getAndIncrement();
				if (ios.getChronology(sc.getReferencedComponentNid()).isEmpty())
				{
					missingComponents.add(is.getUuidPrimordialForNid(sc.getReferencedComponentNid()));
				}
			});
			if (missingComponents.size() > 0)
			{
				getLog().error("There are " + missingComponents.size() + " referenced components which are not present in the database");
				int breaker = 0;
				for (UUID uuid : missingComponents)
				{
					getLog().error("Missing: " + uuid);
					breaker++;
					if (breaker > 100)
					{
						break;
					}
				}
			}
			else
			{
				getLog().info("No missing components found.  Scanned " + semanticScanCount.get() + " semantics");
			}
		}

		{
			AtomicInteger conceptScanCount = new AtomicInteger();
			ArrayList<UUID> missingDescriptions = new ArrayList<>();
			Get.conceptService().getConceptChronologyStream().parallel().forEach(c -> {
				conceptScanCount.getAndIncrement();
				if (as.getDescriptionsForComponent(c.getNid()).size() == 0)
				{
					missingDescriptions.add(is.getUuidPrimordialForNid(c.getNid()));
				}
			});
			if (missingDescriptions.size() > 0)
			{
				getLog().error("There are " + missingDescriptions.size() + " concepts which have 0 descriptions");
				int breaker = 0;
				for (UUID uuid : missingDescriptions)
				{
					getLog().error("Missing Description: " + uuid);
					breaker++;
					if (breaker > 100)
					{
						break;
					}
				}
			}
			else
			{
				getLog().info("No missing descriptions found.  Scanned " + conceptScanCount.get() + " concepts");
			}
		}
	}
}
