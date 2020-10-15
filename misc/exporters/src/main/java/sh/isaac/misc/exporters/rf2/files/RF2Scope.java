/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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

package sh.isaac.misc.exporters.rf2.files;

/**
 * 
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public enum RF2Scope
{
	/**
	 * See https://confluence.ihtsdotools.org/display/DOCRELFMT/3.3.1+Release+Package+Naming+Conventions
	 */
	Edition, //The release files included in the package fully resolve all dependencies of all modules included in the package.
	Extension; //The release files included in the package needs to be combined with the International Edition release package and any other packages required 
	//to resolve the dependencies declared by the Module Dependency Reference Set.
}
