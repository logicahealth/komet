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
package sh.isaac.dbConfigBuilder.fx.fxUtil;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 * Redirect a stream to a TextArea
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class StreamRedirect extends OutputStream
{
	private static Logger log = LogManager.getLogger();
	private TextArea ta_;
	private StringBuilder buffer = new StringBuilder();
	
	public StreamRedirect(TextArea ta)
	{
		ta_ = ta;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		buffer.append((char)b);
	}

	@Override
	public void flush() throws IOException
	{
		StringBuilder newBuffer = new StringBuilder();
		final StringBuilder oldBuffer = buffer;
		buffer = newBuffer;
		log.info("Maven Execution: " + oldBuffer.toString());
		
		Platform.runLater(() ->
		{
			ta_.appendText(oldBuffer.toString());
		});
	}

	@Override
	public void close() throws IOException
	{
		flush();
		super.close();
	}

}
