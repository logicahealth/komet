

package sh.komet.fx.tabpane;


public class SystemUtils {

        private static final boolean MACOS = "Mac OS X".equals(System.getProperty("os.name"));
        private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
        private static final boolean LINUX = System.getProperty("os.name").toLowerCase().contains("linux");

        public static boolean isMacOS() {
		return MACOS;
	}

	public static boolean isWindows() {
		return WINDOWS;
	}
	public static boolean isLinux() {
		return LINUX;
	}


}
