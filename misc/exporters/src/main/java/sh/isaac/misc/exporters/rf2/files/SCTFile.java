package sh.isaac.misc.exporters.rf2.files;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SCTFile extends RF2File
{
	public SCTFile(File rootFolder, String contentType, RF2ReleaseType releaseType, Optional<String> languageCode, String namespace, String versionDate, String ...colNames) throws IOException
	{
		super(rootFolder, "Terminology", "sct2", contentType, Optional.empty(), languageCode, releaseType, namespace, versionDate, colNames);
	}
	
	public SCTFile(File rootFolder, String contentType, Optional<String> contentSubType, RF2ReleaseType releaseType, Optional<String> languageCode, String namespace, String versionDate, String ...colNames) throws IOException
	{
		super(rootFolder, "Terminology", "sct2", contentType, contentSubType, languageCode, releaseType, namespace, versionDate, colNames);
	}
}
