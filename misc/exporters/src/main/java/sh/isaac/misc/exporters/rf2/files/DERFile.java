package sh.isaac.misc.exporters.rf2.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DERFile extends RF2File
{
	public DERFile(File rootFolder, String relativePath, String contentType, Optional<String> contentSubType, Optional<String> languageCode, RF2ReleaseType releaseType, 
			String namespace, String versionDate, String ...colNames) throws IOException
	{
		super(rootFolder, "Refset/" + relativePath, "der2", contentType, contentSubType, languageCode, releaseType, namespace, versionDate, colNames);
	}
}
