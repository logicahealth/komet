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
package gov.va.oia.terminology.converters.sharedUtils.gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;

/**
 * Simple wrapper class to allow us to serialize to multiple formats at once
 * {@link MultipleDataWriterService}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class MultipleDataWriterService implements BinaryDataWriterService
{
	ArrayList<BinaryDataWriterService> writers_ = new ArrayList<>();
	
	public MultipleDataWriterService(Optional<File> gsonPath, Optional<Path> ibdfPath) throws IOException
	{
		if (gsonPath.isPresent())
		{
			writers_.add(new JsonDataWriterService(gsonPath.get()));
		}
		if (ibdfPath.isPresent())
		{
			writers_.add(Get.binaryDataWriter(ibdfPath.get()));
		}
	}
	
	@Override
	public void put(OchreExternalizable ochreObject)
	{
		for (BinaryDataWriterService writer : writers_)
		{
			writer.put(ochreObject);
		}
	}

	@Override
	public void close()
	{
		for (BinaryDataWriterService writer : writers_)
		{
			writer.close();
		}
	}
}
