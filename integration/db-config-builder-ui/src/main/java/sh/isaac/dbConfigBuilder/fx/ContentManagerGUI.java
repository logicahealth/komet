package sh.isaac.dbConfigBuilder.fx;

import java.io.IOException;

/**
 * Just a hack class to workaround issues launching javafx in module land
 * {@link ContentManagerGUI}
 *
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
public class ContentManagerGUI
{
	public static void main(final String[] args) throws ClassNotFoundException, IOException
	{
		ContentManager.main(args);
	}
}
