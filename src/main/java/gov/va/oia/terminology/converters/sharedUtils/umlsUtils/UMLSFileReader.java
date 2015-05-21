package gov.va.oia.terminology.converters.sharedUtils.umlsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UMLSFileReader
{
	private BufferedReader reader_;
	private List<String> nextLine_;

	public UMLSFileReader(BufferedReader reader)
	{
		reader_ = reader;
	}

	public boolean hasNextRow() throws IOException
	{
		if (nextLine_ == null)
		{
			readNextLine();
		}
		return nextLine_ != null;
	}

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
