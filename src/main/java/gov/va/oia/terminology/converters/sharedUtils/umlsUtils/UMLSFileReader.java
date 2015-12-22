package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import gov.va.oia.terminology.converters.sharedUtils.sql.TerminologyFileReader;

public class UMLSFileReader implements TerminologyFileReader
{
	private BufferedReader reader_;
	private List<String> nextLine_;

	public UMLSFileReader(BufferedReader reader)
	{
		reader_ = reader;
	}

	/**
	 * @see gov.va.oia.terminology.converters.sharedUtils.sql.TerminologyFileReader#hasNextRow()
	 */
	@Override
	public boolean hasNextRow() throws IOException
	{
		if (nextLine_ == null)
		{
			readNextLine();
		}
		return nextLine_ != null;
	}

	/**
	 * @see gov.va.oia.terminology.converters.sharedUtils.sql.TerminologyFileReader#getNextRow()
	 */
	@Override
	public List<String> getNextRow() throws IOException
	{
		if (nextLine_ == null)
		{
			readNextLine();
		}
		List<String> temp = nextLine_;
		nextLine_ = null;
		return temp;
	}

	/**
	 * @see gov.va.oia.terminology.converters.sharedUtils.sql.TerminologyFileReader#close()
	 */
	@Override
	public void close() throws IOException
	{
		reader_.close();
	}

	private void readNextLine() throws IOException
	{
		String line = reader_.readLine();
		if (line != null)
		{
			String[] cols = line.split("\\|", -1);
			// remove the last because the files have a trailing separator, with no data after it
			nextLine_ = new ArrayList<>(cols.length - 1);
			for (String s : cols)
			{
				if ((nextLine_.size() == cols.length - 1) && (s.length() == 0 || s == null))
				{
					break;
				}
				nextLine_.add(s);
			}
		}
	}
}
