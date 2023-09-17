package utils;

public class RelativePathGetter {
    public static String getRelativePath(String absolutePath, String projectPath) {
        if (absolutePath.length() > projectPath.length() && absolutePath.startsWith(projectPath)) {
            return absolutePath.substring(projectPath.length());
        }
        return absolutePath;
    }
}
