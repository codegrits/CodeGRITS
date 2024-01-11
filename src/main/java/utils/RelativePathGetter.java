package utils;

/**
 * This class is used to get the relative path of a file compared to the project path.
 */
public class RelativePathGetter {
    /**
     * Get the relative path of a file compared to the project path. If the project path is not a prefix of the file path, the absolute path of the file is returned.
     *
     * @param absolutePath The absolute path of the file.
     * @param projectPath  The absolute path of the project.
     * @return The relative path of the file compared to the project path.
     */
    public static String getRelativePath(String absolutePath, String projectPath) {
        if (absolutePath.length() > projectPath.length() && absolutePath.startsWith(projectPath)) {
            return absolutePath.substring(projectPath.length());
        }
        return absolutePath;
    }
}
