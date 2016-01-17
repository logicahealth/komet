/*
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
package gov.vha.isaac.ochre.mojo;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;

/**
 * Goal which indexes a database using indexer services on the classpath.
 */
@Mojo(name = "index-termstore", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)

public class IndexTermstore extends AbstractMojo
{
	@Override
	public void execute() throws MojoExecutionException
	{
		try
		{
			List<IndexServiceBI> indexers = LookupService.get().getAllServices(IndexServiceBI.class);
			List<String> indexerNames = new ArrayList<>();
			indexers.stream().forEach((IndexServiceBI i) -> indexerNames.add(i.getIndexerName()));
			getLog().info("Found indexers: " + indexerNames);

			getLog().info("Starting indexing. ");
			Get.startIndexTask((Class<? extends IndexServiceBI>[]) null).get();
			getLog().info("Finished indexing. ");

		}
		catch (Exception ex)
		{
			throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
		}
	}
}
