package gov.va.oia.terminology.converters.sharedUtils.sql;

import java.io.IOException;
import java.util.List;

public interface TerminologyFileReader
{

	boolean hasNextRow() throws IOException;

	List<String> getNextRow() throws IOException;

	void close() throws IOException;

}