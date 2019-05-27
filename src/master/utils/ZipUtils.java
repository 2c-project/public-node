package master.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public static void zipFolder(ZipOutputStream zipOutputStream, File inputFolder, String parentName) throws IOException {

        String myname = parentName + inputFolder.getName() + "/";

        ZipEntry folderZipEntry = new ZipEntry(myname);
        zipOutputStream.putNextEntry(folderZipEntry);

        File[] contents = inputFolder.listFiles();

        for (File f : contents) {
            if (f.isFile())
                zipFile(f, myname, zipOutputStream);
            else if (f.isDirectory())
                zipFolder(zipOutputStream, f, myname);
        }
        zipOutputStream.closeEntry();
    }

    public static void zipFile(File inputFile, String parentName, ZipOutputStream zipOutputStream) throws IOException {

        // A ZipEntry represents a file entry in the zip archive
        // We name the ZipEntry after the original file's name
        ZipEntry zipEntry = new ZipEntry(parentName + inputFile.getName());
        zipOutputStream.putNextEntry(zipEntry);

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        byte[] buf = new byte[1024];
        int bytesRead;

        // Read the input file by chucks of 1024 bytes
        // and write the read bytes to the zip stream
        while ((bytesRead = fileInputStream.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, bytesRead);
        }

        // close ZipEntry to store the stream to the file
        zipOutputStream.closeEntry();
    }

    private static void extractFile(ZipInputStream in, String outdir, String name) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        File destinationFile = new File(outdir, name);
        if (!destinationFile.exists()) {
            if (!destinationFile.getParentFile().exists() && !destinationFile.getParentFile().mkdirs())
                throw new RuntimeException("Can't create parent directory to sync");
            if (!destinationFile.createNewFile()) throw new RuntimeException("Can't create file to sync");
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count;
        while ((count = in.read(buffer)) != -1)
            out.write(buffer, 0, count);
        out.close();
    }

    private static void mkdirs(String outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists())
            d.mkdirs();
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }

    /***
     * Extract zipfile to outdir with complete directory structure
     * @param zipfile Input .zip file
     * @param outdir Output directory
     */
    public static void extract(byte[] zipfile, String outdir) {
        try {
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipfile));
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (entry.isDirectory()) {
                    mkdirs(outdir, name);
                    continue;
                }
                /* this part is necessary because file entry can come before
                 * directory entry where is file located
                 * i.e.:
                 *   /foo/foo.txt
                 *   /foo/
                 */
                dir = dirpart(name);
                if (dir != null)
                    mkdirs(outdir, dir);

                extractFile(zin, outdir, name);
            }
            zin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
