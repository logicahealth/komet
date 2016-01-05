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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.cedarsoftware.util.io.JsonWriter;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataWriterService;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizable;

/**
 * {@link JsonDataWriterService} - serialize to JSON
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class JsonDataWriterService implements BinaryDataWriterService
{
	private JsonWriter json_;

	public JsonDataWriterService(File path) throws IOException
	{
		Map<String, Object> args = new HashMap<>();
		args.put(JsonWriter.PRETTY_PRINT, true);
		json_ = new JsonWriter(new FileOutputStream(path), args);
	}

	@Override
	public void put(OchreExternalizable ochreObject)
	{
		json_.write(ochreObject);
	}

	@Override
	public void close()
	{
		json_.close();
	}
}
