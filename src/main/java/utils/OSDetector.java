package utils;

/**
 * This class is used to detect the operating system.
 */
public class OSDetector {
    /**
     * The operating system name.
     */
    private static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Check if the operating system is Windows.
     *
     * @return {@code true} if the operating system is Windows, {@code false} otherwise.
     */
    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    /**
     * Check if the operating system is Mac.
     *
     * @return {@code true} if the operating system is Mac, {@code false} otherwise.
     */
    public static boolean isMac() {
        return (OS.contains("mac"));
    }

    /**
     * Check if the operating system is Unix.
     *
     * @return {@code true} if the operating system is Unix, {@code false} otherwise.
     */
    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }
}
