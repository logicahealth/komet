package sh.komet.gui.contract;

/**
 * A simple enum to identify the executing operating system for Komet. This is helpful to make menu declarations more
 * straightforward and clear.
 *
 * AKS - 11/24/20
 */
public enum OSType {

    MAC("macOS", "^Mac.*"),
    WINDOWS("Windows OS","^Windows.*"),
    LINUX("Linux","^Linux.*");

    private String name;
    private String regex;

    /**
     * OSType Enumeration Constructor
     * @param name - common name of operating system (e.g., macOS vs Mac OSX)
     * @param regex - regular expression that is used to identify OS based on System.getProperty("os.name").
     *              The regular expression enables extending the OSType to more specific named distributions.
     */
    OSType(String name, String regex){
        this.name = name;
        this.regex = regex;
    }

    public String getName() {
        return this.name;
    }

    public String getRegex() {
        return this.regex;
    }

}
