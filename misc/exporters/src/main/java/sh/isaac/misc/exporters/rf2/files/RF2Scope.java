package sh.isaac.misc.exporters.rf2.files;

public enum RF2Scope
{
	/**
	 * See https://confluence.ihtsdotools.org/display/DOCRELFMT/3.3.1+Release+Package+Naming+Conventions
	 */
	Edition, //The release files included in the package fully resolve all dependencies of all modules included in the package.
	Extension; //The release files included in the package needs to be combined with the International Edition release package and any other packages required 
	//to resolve the dependencies declared by the Module Dependency Reference Set.
}
