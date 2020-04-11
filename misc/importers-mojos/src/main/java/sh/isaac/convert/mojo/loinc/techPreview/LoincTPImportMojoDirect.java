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



package sh.isaac.convert.mojo.loinc.techPreview;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import sh.isaac.convert.directUtils.DirectConverter;

import java.io.File;
import java.nio.file.Path;

/**
 * TODO:
 * KEC: This class needs to be rewritten so that it does not depend on sh.isaac.provider.logic.IsaacVisitor from a
 * provider module. It must depend on the api and possibly the model class. Metadata is ok.
 * Loader code to convert Loinc into the ISAAC datastore.
 * 
 * See {@link LoincTPMojoRunner} for a way to run this via a main for testing / debug
 */
@Mojo( name = "convert-loinc-tech-preview-to-ibdf", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class LoincTPImportMojoDirect extends LoincTPImportHK2Direct
{
	/**
	 * This constructor is for maven and should not be used at runtime. You should
	 * get your reference of this class from HK2, and then call the {@link DirectConverter#configure(File, Path, String, sh.isaac.api.coordinate.StampFilter)} method on it.
	 */
	public LoincTPImportMojoDirect()
	{
		//for maven
	}
}