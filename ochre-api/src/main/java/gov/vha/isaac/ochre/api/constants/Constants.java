package gov.vha.isaac.ochre.api.constants;

/**
 * Created by kec on 9/11/14.
 * 
 * A typical datastore structure would look like this:
 * 
 * somePath/data/object-chronicles/cradle/{content}
 * somePath/data/search/lucene/{content}
 * 
 * {@link #DATA_STORE_ROOT_LOCATION_PROPERTY} can be utilized to set the absolute path to the location of the 'data' portion of the paths in the
 * example above. {@link #DEFAULT_CHRONICLE_FOLDER} will be automatically appended to the path to create the chronicle path. {@link
 * #DEFAULT_SEARCH_FOLDER} will be automatically appended to the path to create the search path.
 * 
 * If you have a data structure where the object-chronicles folder does not share a common parent folder with the search folder, then you may utilize
 * the {@link #CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY} and {@link #SEARCH_ROOT_LOCATION_PROPERTY} to specify the absolute path to the
 * 'object-chronicles' and 'search' components of the above examples, respectively.
 * 
 * @author kec
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class Constants
{
	/**
	 * If set to true, reads HK2 inhabitant files from the jar files to locate HK2 services.  Otherwise, scans the classpath
	 * for classes with HK2 annotations - ignoring the inhabitant files.
	 */
	public static final String READ_INHABITANT_FILES = "gov.vha.isaac.ochre.api.constants.READ_INHABITANT_FILES";
	
	/**
	 * The default HK2 configuration only scans classes in the packages 'gov.vha', 'org.ihtsdo' and 'org.glassfish' when looking for HK2
	 * annotated classes.  Specify additional packages to search with this property, separating unique packages with a ';'. 
	 */
	public static final String EXTRA_PACKAGES_TO_SEARCH = "gov.vha.isaac.ochre.api.constants.EXTRA_PACKAGES_TO_SEARCH";
	
	/**
	 * Used to specify the location of the data store folder, which contains subfolders for 'object-chronicles' and 'search'
	 */
	public static final String DATA_STORE_ROOT_LOCATION_PROPERTY = "gov.vha.isaac.ochre.api.constants.data-store-root-location";

	/**
	 * Appended to the path specified by the {@link #DATA_STORE_ROOT_LOCATION_PROPERTY} 
	 */
	public static final String DEFAULT_CHRONICLE_FOLDER = "object-chronicles";

	/**
	 * Appended to the path specified by the {@link #DATA_STORE_ROOT_LOCATION_PROPERTY}
	 */
	public static final String DEFAULT_SEARCH_FOLDER = "search";

	/**
	 * May be optionally used to specify an absolute path for the 'object-chronicles' folder.  If this is specified, the 
	 * {@link #DATA_STORE_ROOT_LOCATION_PROPERTY} {@link #DEFAULT_CHRONICLE_FOLDER} values are ignored.
	 */
	public static final String CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY = "gov.vha.isaac.ochre.api.constants.chronicle-collection-root-location";
	
	/**
	 * May be optionally used to specify an absolute path for the 'search' folder.  If this is specified, the 
	 * {@link #DATA_STORE_ROOT_LOCATION_PROPERTY} {@link #DEFAULT_SEARCH_FOLDER} values are ignored.
	 */
	public static final String SEARCH_ROOT_LOCATION_PROPERTY = "gov.vha.isaac.ochre.api.constants.search-root-location";
	
	/**
	 * May be optionally used to enable console-level, extremely verbose debug output.  
	 * This typically turns on and off things that were hacked in with System.Out calls for tracing ugly problems....
	 */
	public static final String ISAAC_DEBUG = "gov.vha.isaac.ochre.api.constants.isaac-debug";
	
}
