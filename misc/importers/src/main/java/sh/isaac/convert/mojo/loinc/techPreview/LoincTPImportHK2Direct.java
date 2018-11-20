/*
 * Copyright 2018 VetsEZ Inc, Sagebits LLC
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

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import sh.isaac.convert.directUtils.DirectConverter;

/**
 * {@link LoincTPImportHK2Direct}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@PerLookup
@Service
public class LoincTPImportHK2Direct extends LoincTPImportMojoDirect implements DirectConverter
{
	private LoincTPImportHK2Direct()
	{
		//For HK2
	}
}
