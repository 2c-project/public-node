package master.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class FileUtils {
    public static Path getFirstFileByRegexp(String directoryPath, String regexp) {
        try {
            Iterator<Path> iterator = Files.newDirectoryStream(Path.of(directoryPath), regexp).iterator();
            return iterator.hasNext() ? iterator.next() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
